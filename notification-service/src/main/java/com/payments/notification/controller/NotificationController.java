package com.payments.notification.controller;

import com.payments.notification.domain.model.*;
import com.payments.notification.repository.NotificationPreferenceRepository;
import com.payments.notification.repository.NotificationRepository;
import com.payments.notification.repository.NotificationTemplateRepository;
import com.payments.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * NotificationController provides REST API for notification management.
 *
 * <p>Endpoints:
 * - GET /api/notifications/user - Get user notification history
 * - POST /api/notifications - Send notification
 * - GET /api/notifications/{id} - Get notification details
 * - GET /api/notifications/statistics - Get tenant statistics
 * - GET /api/templates - List templates
 * - POST /api/templates - Create template
 * - GET /api/preferences - Get user preferences
 * - PUT /api/preferences - Update user preferences
 *
 * <p>Security:
 * - OAuth2/OIDC authentication required
 * - Multi-tenancy enforced (X-Tenant-ID header)
 * - RBAC for admin operations
 *
 * @author Payment Engine
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification management API")
@Slf4j
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;
  private final NotificationRepository notificationRepository;
  private final NotificationTemplateRepository templateRepository;
  private final NotificationPreferenceRepository preferenceRepository;

  // ============================================================================
  // Notification Endpoints
  // ============================================================================

  /**
   * Get user notification history.
   *
   * @param tenantId tenant identifier (from header or path)
   * @param userId user identifier (from JWT)
   * @param page page number (0-based, default 0)
   * @param size page size (default 20, max 100)
   * @return paginated list of notifications
   */
  @GetMapping("/user")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(
      summary = "Get user notification history",
      description = "Retrieve paginated list of notifications for the current user")
  @ApiResponse(responseCode = "200", description = "List of user notifications")
  public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    if (size > 100) {
      size = 100; // Cap page size
    }

    String userId = getCurrentUserId();

    log.info("Fetching notifications: tenantId={}, userId={}, page={}, size={}",
        tenantId, userId, page, size);

    Pageable pageable = PageRequest.of(page, size);
    Page<NotificationEntity> notifications =
        notificationRepository.findByTenantIdAndUserIdOrderByCreatedAtDesc(
            tenantId, userId, pageable);

    Page<NotificationResponse> response =
        notifications.map(NotificationResponse::from);

    log.debug("Found {} notifications for user: {}", notifications.getTotalElements(), userId);

    return ResponseEntity.ok(response);
  }

  /**
   * Send notification to user.
   *
   * @param tenantId tenant identifier
   * @param request notification request
   * @return created notification
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Send notification", description = "Create and queue a new notification")
  @ApiResponse(responseCode = "201", description = "Notification created")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  public ResponseEntity<NotificationResponse> sendNotification(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @Valid @RequestBody SendNotificationRequest request) {

    log.info(
        "Creating notification: tenantId={}, userId={}, type={}",
        tenantId,
        request.getUserId(),
        request.getNotificationType());

    // Create notification entity
    NotificationEntity notification =
        NotificationEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .userId(request.getUserId())
            .notificationType(request.getNotificationType())
            .channelType(request.getChannelType())
            .recipientAddress(request.getRecipientAddress())
            .templateData(request.getTemplateData())
            .status(NotificationStatus.PENDING)
            .attempts(0)
            .build();

    NotificationEntity saved = notificationRepository.save(notification);

    // Trigger async processing
    notificationService.processNotification(saved.getId());

    log.info("Notification created: id={}", saved.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(NotificationResponse.from(saved));
  }

  /**
   * Get notification details.
   *
   * @param tenantId tenant identifier
   * @param notificationId notification UUID
   * @return notification details
   */
  @GetMapping("/{notificationId}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Get notification", description = "Retrieve details of a specific notification")
  @ApiResponse(responseCode = "200", description = "Notification details")
  @ApiResponse(responseCode = "404", description = "Notification not found")
  public ResponseEntity<NotificationResponse> getNotification(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable UUID notificationId) {

    log.info("Fetching notification: id={}, tenantId={}", notificationId, tenantId);

    NotificationEntity notification =
        notificationRepository
            .findById(notificationId)
            .filter(n -> n.getTenantId().equals(tenantId))
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Notification not found: " + notificationId));

    return ResponseEntity.ok(NotificationResponse.from(notification));
  }

  /**
   * Get tenant-level notification statistics.
   *
   * @param tenantId tenant identifier
   * @return statistics map
   */
  @GetMapping("/statistics")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
  @Operation(
      summary = "Get notification statistics",
      description = "Retrieve statistics for tenant notifications")
  @ApiResponse(responseCode = "200", description = "Statistics")
  public ResponseEntity<Map<String, Object>> getStatistics(
      @RequestHeader("X-Tenant-ID") String tenantId) {

    log.info("Fetching statistics: tenantId={}", tenantId);

    Map<String, Long> stats = notificationService.getNotificationStatistics(tenantId);

    Map<String, Object> response = new HashMap<>();
    response.put("tenantId", tenantId);
    response.put("statistics", stats);
    response.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(response);
  }

  /**
   * Retry failed notification.
   *
   * @param tenantId tenant identifier
   * @param notificationId notification UUID
   * @return updated notification
   */
  @PostMapping("/{notificationId}/retry")
  @PreAuthorize("hasAnyRole('ADMIN')")
  @Operation(summary = "Retry notification", description = "Manually retry a failed notification")
  @ApiResponse(responseCode = "200", description = "Retry scheduled")
  @ApiResponse(responseCode = "404", description = "Notification not found")
  public ResponseEntity<NotificationResponse> retryNotification(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable UUID notificationId) {

    log.info("Retrying notification: id={}, tenantId={}", notificationId, tenantId);

    NotificationEntity notification =
        notificationRepository
            .findById(notificationId)
            .filter(n -> n.getTenantId().equals(tenantId))
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Notification not found: " + notificationId));

    // Update status to RETRY
    notificationRepository.updateStatus(
        notification.getId(), NotificationStatus.RETRY, java.time.LocalDateTime.now());

    // Trigger processing
    notificationService.processNotification(notification.getId());

    log.info("Notification retry triggered: id={}", notificationId);

    return ResponseEntity.ok(NotificationResponse.from(notification));
  }

  // ============================================================================
  // Template Endpoints
  // ============================================================================

  /**
   * List active templates for tenant.
   *
   * @param tenantId tenant identifier
   * @param page page number
   * @param size page size
   * @return list of templates
   */
  @GetMapping("/templates")
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @Operation(summary = "List templates", description = "Retrieve active notification templates")
  @ApiResponse(responseCode = "200", description = "List of templates")
  public ResponseEntity<Page<TemplateResponse>> listTemplates(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.info("Listing templates: tenantId={}, page={}, size={}", tenantId, page, size);

    if (size > 100) {
      size = 100;
    }

    Pageable pageable = PageRequest.of(page, size);
    Page<NotificationTemplateEntity> templates =
        templateRepository.findByTenantIdAndActiveOrderByCreatedAtDesc(
            tenantId, true, pageable);

    Page<TemplateResponse> response = templates.map(TemplateResponse::from);

    return ResponseEntity.ok(response);
  }

  /**
   * Create or update notification template.
   *
   * @param tenantId tenant identifier
   * @param request template request
   * @return created/updated template
   */
  @PostMapping("/templates")
  @PreAuthorize("hasAnyRole('ADMIN')")
  @Operation(summary = "Create template", description = "Create a new notification template")
  @ApiResponse(responseCode = "201", description = "Template created")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  public ResponseEntity<TemplateResponse> createTemplate(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @Valid @RequestBody CreateTemplateRequest request) {

    log.info(
        "Creating template: tenantId={}, type={}, name={}",
        tenantId,
        request.getNotificationType(),
        request.getName());

    // Check if template already exists
    var existing =
        templateRepository.findActiveTemplateByTenantAndType(
            tenantId, request.getNotificationType());

    NotificationTemplateEntity template;

    if (existing.isPresent()) {
      // Update existing
      template = existing.get();
      template.setName(request.getName());
      template.setEmailSubject(request.getEmailSubject());
      template.setEmailTemplate(request.getEmailTemplate());
      template.setPushTitle(request.getPushTitle());
      template.setPushBody(request.getPushBody());
      template.setSmsTemplate(request.getSmsTemplate());
      template.setActive(true);
    } else {
      // Create new
      template =
          NotificationTemplateEntity.builder()
              .id(UUID.randomUUID())
              .tenantId(tenantId)
              .notificationType(request.getNotificationType())
              .name(request.getName())
              .emailSubject(request.getEmailSubject())
              .emailTemplate(request.getEmailTemplate())
              .pushTitle(request.getPushTitle())
              .pushBody(request.getPushBody())
              .smsTemplate(request.getSmsTemplate())
              .active(true)
              .build();
    }

    NotificationTemplateEntity saved = templateRepository.save(template);

    log.info("Template created/updated: id={}", saved.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(TemplateResponse.from(saved));
  }

  /**
   * Deactivate template.
   *
   * @param tenantId tenant identifier
   * @param templateId template UUID
   * @return success response
   */
  @DeleteMapping("/templates/{templateId}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  @Operation(summary = "Deactivate template", description = "Deactivate a notification template")
  @ApiResponse(responseCode = "204", description = "Template deactivated")
  @ApiResponse(responseCode = "404", description = "Template not found")
  public ResponseEntity<Void> deactivateTemplate(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable UUID templateId) {

    log.info("Deactivating template: id={}, tenantId={}", templateId, tenantId);

    NotificationTemplateEntity template =
        templateRepository
            .findById(templateId)
            .filter(t -> t.getTenantId().equals(tenantId))
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Template not found: " + templateId));

    template.setActive(false);
    templateRepository.save(template);

    log.info("Template deactivated: id={}", templateId);

    return ResponseEntity.noContent().build();
  }

  // ============================================================================
  // Preference Endpoints
  // ============================================================================

  /**
   * Get user notification preferences.
   *
   * @param tenantId tenant identifier
   * @return user preferences
   */
  @GetMapping("/preferences")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Get preferences", description = "Retrieve user notification preferences")
  @ApiResponse(responseCode = "200", description = "User preferences")
  @ApiResponse(responseCode = "404", description = "Preferences not found")
  public ResponseEntity<PreferenceResponse> getPreferences(
      @RequestHeader("X-Tenant-ID") String tenantId) {

    String userId = getCurrentUserId();

    log.info("Fetching preferences: tenantId={}, userId={}", tenantId, userId);

    NotificationPreferenceEntity preferences =
        preferenceRepository
            .findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Preferences not found for user: " + userId));

    return ResponseEntity.ok(PreferenceResponse.from(preferences));
  }

  /**
   * Update user notification preferences.
   *
   * @param tenantId tenant identifier
   * @param request preference update
   * @return updated preferences
   */
  @PutMapping("/preferences")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Update preferences", description = "Update user notification preferences")
  @ApiResponse(responseCode = "200", description = "Preferences updated")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  public ResponseEntity<PreferenceResponse> updatePreferences(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @Valid @RequestBody UpdatePreferenceRequest request) {

    String userId = getCurrentUserId();

    log.info("Updating preferences: tenantId={}, userId={}", tenantId, userId);

    NotificationPreferenceEntity preferences =
        preferenceRepository
            .findByTenantIdAndUserId(tenantId, userId)
            .orElseGet(
                () ->
                    NotificationPreferenceEntity.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .userId(userId)
                        .build());

    // Update fields
    if (request.getPreferredChannels() != null) {
      preferences.setPreferredChannels(request.getPreferredChannels());
    }
    if (request.getTransactionAlertsOptIn() != null) {
      preferences.setTransactionAlertsOptIn(request.getTransactionAlertsOptIn());
    }
    if (request.getMarketingOptIn() != null) {
      preferences.setMarketingOptIn(request.getMarketingOptIn());
    }
    if (request.getSystemNotificationsOptIn() != null) {
      preferences.setSystemNotificationsOptIn(request.getSystemNotificationsOptIn());
    }
    if (request.getQuietHoursStart() != null) {
      preferences.setQuietHoursStart(request.getQuietHoursStart());
    }
    if (request.getQuietHoursEnd() != null) {
      preferences.setQuietHoursEnd(request.getQuietHoursEnd());
    }

    NotificationPreferenceEntity updated = preferenceRepository.save(preferences);

    log.info("Preferences updated: tenantId={}, userId={}", tenantId, userId);

    return ResponseEntity.ok(PreferenceResponse.from(updated));
  }

  // ============================================================================
  // Health Endpoint
  // ============================================================================

  /**
   * Notification service health check.
   *
   * @return health status
   */
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Check notification service health")
  @ApiResponse(responseCode = "200", description = "Service is healthy")
  public ResponseEntity<Map<String, String>> health() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("service", "NotificationService");
    response.put("timestamp", System.currentTimeMillis() + "");
    return ResponseEntity.ok(response);
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  /**
   * Get current user ID from JWT.
   *
   * @return user ID
   */
  private String getCurrentUserId() {
    // TODO: Extract from SecurityContext/JWT
    // For now, return a placeholder
    return org.springframework.security.core.context.SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getName();
  }

  /**
   * Custom exception for not found resources.
   */
  public static class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
      super(message);
    }
  }
}
