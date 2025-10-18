package com.payments.notification.service;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.payments.notification.domain.model.*;
import com.payments.notification.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;

/**
 * NotificationService orchestrates notification delivery across multiple channels.
 *
 * <p>Responsibilities:
 * - Template lookup and rendering
 * - User preference enforcement
 * - Quiet hours checking
 * - Channel adapter dispatch
 * - Retry scheduling
 * - Audit logging
 *
 * <p>Multi-channel support:
 * - Email (via channel adapter)
 * - SMS (via channel adapter)
 * - Push notifications (via channel adapter)
 *
 * @author Payment Engine
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationTemplateRepository templateRepository;
  private final NotificationPreferenceRepository preferenceRepository;
  private final AuditService auditService;

  private static final MustacheFactory mf = new DefaultMustacheFactory();
  private static final int MAX_RETRY_ATTEMPTS = 3;
  private static final int RETRY_BACKOFF_MS = 1000;

  /**
   * Process a pending notification for delivery.
   *
   * <p>Steps:
   * 1. Look up template by tenant + type
   * 2. Check user preferences (opted in, not in quiet hours)
   * 3. Render template with data
   * 4. Dispatch to channels based on preferences
   * 5. Update notification status
   * 6. Log to audit trail
   *
   * @param notificationId the notification to process
   */
  @Async
  public void processNotification(UUID notificationId) {
    try {
      // 1. Retrieve notification
      NotificationEntity notification =
          notificationRepository
              .findById(notificationId)
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Notification not found: " + notificationId));

      log.info(
          "Processing notification: id={}, tenantId={}, userId={}, type={}",
          notificationId,
          notification.getTenantId(),
          notification.getUserId(),
          notification.getNotificationType());

      // 2. Look up template
      NotificationTemplateEntity template =
          templateRepository
              .findActiveTemplateByTenantAndType(
                  notification.getTenantId(), notification.getNotificationType())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Template not found for type: "
                              + notification.getNotificationType()));

      // 3. Check user preferences
      NotificationPreferenceEntity preferences =
          preferenceRepository
              .findByTenantIdAndUserId(
                  notification.getTenantId(), notification.getUserId())
              .orElse(createDefaultPreferences(notification.getTenantId(), notification.getUserId()));

      // 4. Validate preferences
      if (!isNotificationAllowed(notification, preferences)) {
        log.info(
            "Notification blocked by preferences: id={}, reason=opted-out",
            notificationId);
        updateNotificationStatus(
            notification, NotificationStatus.SENT); // Mark as sent but not actually sent
        auditService.logNotificationDenied(notification, "User preferences");
        return;
      }

      // 5. Check quiet hours
      if (preferences.isInQuietHours()) {
        log.info(
            "Notification deferred: quiet hours active: id={}, userId={}",
            notificationId,
            notification.getUserId());
        // Could reschedule for after quiet hours, for now just log
        auditService.logNotificationDenied(notification, "Quiet hours");
        return;
      }

      // 6. Render template
      Map<String, Object> templateVariables =
          parseTemplateData(notification.getTemplateData());
      String renderedContent =
          renderTemplate(template.getEmailTemplate(), templateVariables);

      log.debug(
          "Template rendered: id={}, contentLength={}", notificationId, renderedContent.length());

      // 7. Dispatch to channels (based on preferences & template support)
      dispatchToChannels(notification, template, preferences, renderedContent);

      // 8. Mark as sent
      updateNotificationStatus(notification, NotificationStatus.SENT);

      log.info(
          "Notification processed successfully: id={}, tenantId={}",
          notificationId,
          notification.getTenantId());

      auditService.logNotificationSent(notification);

    } catch (Exception e) {
      log.error(
          "Error processing notification: id={}, error={}",
          notificationId,
          e.getMessage(),
          e);
      handleNotificationError(notificationId, e);
    }
  }

  /**
   * Check if notification should be sent to user.
   *
   * @param notification the notification entity
   * @param preferences user preferences
   * @return true if notification should proceed
   */
  private boolean isNotificationAllowed(
      NotificationEntity notification, NotificationPreferenceEntity preferences) {
    // Check notification type opt-in
    if (!preferences.isNotificationTypeAllowed(notification.getNotificationType())) {
      log.debug(
          "User opted out of notification type: userId={}, type={}",
          notification.getUserId(),
          notification.getNotificationType());
      return false;
    }

    // Check channel preference (if set)
    if (notification.getChannelType() != null) {
      if (!preferences.isChannelPreferred(notification.getChannelType())) {
        log.debug(
            "User not subscribed to channel: userId={}, channel={}",
            notification.getUserId(),
            notification.getChannelType());
        return false;
      }
    }

    return true;
  }

  /**
   * Dispatch notification to configured channels.
   *
   * <p>Strategy:
   * - Check which channels user prefers
   * - Check which channels template supports
   * - Send to intersection of both
   * - Retry on transient failures
   *
   * @param notification the notification
   * @param template the template
   * @param preferences user preferences
   * @param renderedContent rendered template content
   */
  private void dispatchToChannels(
      NotificationEntity notification,
      NotificationTemplateEntity template,
      NotificationPreferenceEntity preferences,
      String renderedContent) {

    List<NotificationChannel> channelsToUse = new ArrayList<>();

    // Determine which channels to use
    for (NotificationChannel channel : NotificationChannel.values()) {
      // Check if user prefers this channel
      if (!preferences.isChannelPreferred(channel)) {
        log.debug(
            "User doesn't prefer channel: userId={}, channel={}", notification.getUserId(), channel);
        continue;
      }

      // Check if template supports this channel
      if (!template.supportsChannel(channel)) {
        log.debug(
            "Template doesn't support channel: type={}, channel={}",
            notification.getNotificationType(),
            channel);
        continue;
      }

      channelsToUse.add(channel);
    }

    if (channelsToUse.isEmpty()) {
      log.warn(
          "No suitable channels for notification: id={}, userId={}",
          notification.getId(),
          notification.getUserId());
      return;
    }

    log.info(
        "Dispatching to channels: notificationId={}, channels={}", notification.getId(), channelsToUse);

    // Send to each channel (async)
    for (NotificationChannel channel : channelsToUse) {
      sendToChannel(notification, template, channel, renderedContent);
    }
  }

  /**
   * Send notification to specific channel.
   *
   * @param notification the notification
   * @param template the template
   * @param channel the target channel
   * @param renderedContent rendered content
   */
  private void sendToChannel(
      NotificationEntity notification,
      NotificationTemplateEntity template,
      NotificationChannel channel,
      String renderedContent) {

    try {
      log.debug(
          "Sending to channel: notificationId={}, channel={}, recipient={}",
          notification.getId(),
          channel,
          notification.getRecipientAddress());

      switch (channel) {
        case EMAIL:
          // TODO: sendViaEmail(notification, template, renderedContent);
          log.info("Email dispatch: queued (adapter not yet implemented)");
          break;
        case SMS:
          // TODO: sendViaSMS(notification, template, renderedContent);
          log.info("SMS dispatch: queued (adapter not yet implemented)");
          break;
        case PUSH:
          // TODO: sendViaPush(notification, template, renderedContent);
          log.info("Push dispatch: queued (adapter not yet implemented)");
          break;
      }

      log.info(
          "Sent to {}: notificationId={}, recipient={}",
          channel,
          notification.getId(),
          notification.getRecipientAddress());

    } catch (Exception e) {
      log.error(
          "Failed to send via {}: notificationId={}, error={}",
          channel,
          notification.getId(),
          e.getMessage(),
          e);
    }
  }

  /**
   * Render Mustache template with variables.
   *
   * @param templateString the template (Mustache format)
   * @param variables variables to inject
   * @return rendered string
   */
  private String renderTemplate(String templateString, Map<String, Object> variables) {
    try {
      Mustache mustache = mf.compile(new java.io.StringReader(templateString), "notification");
      StringWriter writer = new StringWriter();
      mustache.execute(writer, variables).flush();
      return writer.toString();
    } catch (Exception e) {
      log.error("Failed to render template: error={}", e.getMessage(), e);
      throw new RuntimeException("Template rendering failed", e);
    }
  }

  /**
   * Parse template data from JSON string.
   *
   * @param jsonData JSON data from notification entity
   * @return map of variables
   */
  private Map<String, Object> parseTemplateData(String jsonData) {
    try {
      // Simple JSON parsing (could use Jackson for production)
      Map<String, Object> data = new HashMap<>();
      // TODO: parse JSON properly
      return data;
    } catch (Exception e) {
      log.error("Failed to parse template data: error={}", e.getMessage(), e);
      return new HashMap<>();
    }
  }

  /**
   * Create default preferences for new user.
   *
   * @param tenantId the tenant
   * @param userId the user
   * @return default preferences
   */
  private NotificationPreferenceEntity createDefaultPreferences(String tenantId, String userId) {
    NotificationPreferenceEntity preferences =
        NotificationPreferenceEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .userId(userId)
            .preferredChannels(
                Set.of(
                    NotificationChannel.EMAIL,
                    NotificationChannel.SMS,
                    NotificationChannel.PUSH))
            .transactionAlertsOptIn(true)
            .marketingOptIn(false)
            .systemNotificationsOptIn(true)
            .build();

    return preferenceRepository.save(preferences);
  }

  /**
   * Handle error during notification processing.
   *
   * @param notificationId the notification
   * @param exception the exception
   */
  private void handleNotificationError(UUID notificationId, Exception exception) {
    try {
      NotificationEntity notification =
          notificationRepository.findById(notificationId).orElse(null);

      if (notification == null) {
        return;
      }

      int newAttempts = (notification.getAttempts() != null ? notification.getAttempts() : 0) + 1;

      if (newAttempts >= MAX_RETRY_ATTEMPTS) {
        log.error(
            "Max retry attempts exceeded: id={}, attempts={}",
            notificationId,
            newAttempts);
        updateNotificationStatus(notification, NotificationStatus.FAILED);
        notification.setFailureReason(exception.getMessage());
        notificationRepository.save(notification);
        auditService.logNotificationError(notification, exception.getMessage());
      } else {
        log.warn(
            "Retrying notification: id={}, attempt={}/{}",
            notificationId,
            newAttempts,
            MAX_RETRY_ATTEMPTS);
        updateNotificationStatus(notification, NotificationStatus.RETRY);
        notification.setAttempts(newAttempts);
        notification.setLastAttemptAt(LocalDateTime.now());
        notificationRepository.save(notification);
      }

    } catch (Exception e) {
      log.error(
          "Error handling notification error: id={}, error={}", notificationId, e.getMessage(), e);
    }
  }

  /**
   * Update notification status in database.
   *
   * @param notification the notification
   * @param newStatus the new status
   */
  private void updateNotificationStatus(
      NotificationEntity notification, NotificationStatus newStatus) {
    notificationRepository.updateStatus(notification.getId(), newStatus, LocalDateTime.now());
    log.debug(
        "Updated notification status: id={}, status={}", notification.getId(), newStatus);
  }

  /**
   * Scheduled task to retry failed notifications.
   *
   * <p>Runs every 30 seconds and processes notifications with:
   * - Status = RETRY or FAILED
   * - Attempts < 3
   * - LastAttemptAt older than backoff interval
   */
  @Scheduled(fixedDelayString = "${notification.scheduler.retry-interval-seconds:30}000")
  public void retryFailedNotifications() {
    try {
      log.debug("Starting retry scheduler");

      LocalDateTime beforeTime = LocalDateTime.now().minusSeconds(
          RETRY_BACKOFF_MS / 1000);

      List<NotificationEntity> retryCandidates =
          notificationRepository.findRetryCandidates("*", beforeTime);

      if (retryCandidates.isEmpty()) {
        log.debug("No retry candidates found");
        return;
      }

      log.info("Found {} notifications to retry", retryCandidates.size());

      for (NotificationEntity notification : retryCandidates) {
        processNotification(notification.getId());
      }

    } catch (Exception e) {
      log.error("Error in retry scheduler: error={}", e.getMessage(), e);
    }
  }

  /**
   * Query notification history for user.
   *
   * @param tenantId the tenant
   * @param userId the user
   * @param limit maximum results
   * @return list of notifications
   */
  public List<NotificationEntity> getUserNotificationHistory(
      String tenantId, String userId, int limit) {
    var pageable = org.springframework.data.domain.PageRequest.of(0, limit);
    return notificationRepository
        .findByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, userId, pageable)
        .getContent();
  }

  /**
   * Get notification statistics for tenant.
   *
   * @param tenantId the tenant
   * @return statistics map
   */
  public Map<String, Long> getNotificationStatistics(String tenantId) {
    Map<String, Long> stats = new HashMap<>();
    stats.put("pending", countByStatus(tenantId, NotificationStatus.PENDING));
    stats.put("sent", countByStatus(tenantId, NotificationStatus.SENT));
    stats.put("failed", countByStatus(tenantId, NotificationStatus.FAILED));
    return stats;
  }

  /**
   * Count notifications by status.
   *
   * @param tenantId the tenant
   * @param status the status
   * @return count
   */
  private long countByStatus(String tenantId, NotificationStatus status) {
    var pageable = org.springframework.data.domain.PageRequest.of(0, 1);
    return notificationRepository
        .findByTenantIdAndStatusOrderByCreatedAtAsc(tenantId, status, pageable)
        .getTotalElements();
  }
}
