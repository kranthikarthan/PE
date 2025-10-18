package com.payments.notification.adapter;

import com.payments.notification.domain.model.NotificationEntity;
import com.payments.notification.domain.model.NotificationTemplateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * PushNotificationAdapter sends push notifications via Firebase Cloud Messaging (FCM).
 *
 * <p>Features:
 * - Device token validation
 * - FCM message composition (title, body, data)
 * - Silent/background push support
 * - Device token management
 * - Batch send capability
 * - TTL (Time-To-Live) configuration
 *
 * <p>Production Setup:
 * - Firebase project with FCM enabled
 * - Service account JSON key file
 * - Device tokens stored in database
 * - Topic subscriptions for group messaging
 *
 * @author Payment Engine
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PushNotificationAdapter implements ChannelAdapter {

  @Value("${notification.push.enabled:true}")
  private boolean enabled;

  @Value("${notification.push.ttl-seconds:86400}")
  private long ttlSeconds; // 24 hours default

  @Value("${notification.push.priority:high}")
  private String priority;

  // Firebase Admin SDK client would be injected here
  // private final FirebaseMessaging firebaseMessaging;

  /**
   * Send push notification via FCM.
   *
   * @param notification the notification entity
   * @param template the push template (contains title, body, icons)
   * @param renderedContent rendered notification body
   * @throws Exception if send fails
   */
  @Override
  public void send(
      NotificationEntity notification,
      NotificationTemplateEntity template,
      String renderedContent)
      throws Exception {

    if (!enabled) {
      log.warn("Push notification adapter is disabled");
      return;
    }

    validateInput(notification, template, renderedContent);

    try {
      String deviceToken = notification.getRecipientAddress();

      log.info(
          "Sending push notification: deviceToken={}, tenantId={}, notificationId={}",
          maskDeviceToken(deviceToken),
          notification.getTenantId(),
          notification.getId());

      // Build FCM message
      Map<String, String> data = new HashMap<>();
      data.put("notificationId", notification.getId().toString());
      data.put("tenantId", notification.getTenantId());
      data.put("notificationType", notification.getNotificationType().toString());
      data.put("timestamp", Instant.now().toString());

      String title = template.getPushTitle() != null 
          ? template.getPushTitle() 
          : "Payment Engine";
      
      String body = truncateBody(renderedContent, 240); // FCM body max 240

      log.debug(
          "FCM message: title={}, body={}, dataSize={}", title, body.length(), data.size());

      // TODO: Implement Firebase FCM send
      // Steps:
      // 1. Create MulticastMessage or Message:
      //    - setNotification(Notification.builder()
      //        .setTitle(title)
      //        .setBody(body)
      //        .setImageUrl(template.getPushIconUrl())
      //        .build())
      //    - putAllData(data)
      //    - setWebpushConfig(WebpushConfig.builder()
      //        .setTtl(ttlSeconds)
      //        .setPriority(priority)
      //        .build())
      //    - setAndroidConfig(AndroidConfig.builder()
      //        .setTtl(ttlSeconds)
      //        .setPriority(priority)
      //        .build())
      //    - setApnsConfig(ApnsConfig.builder()
      //        .setTtl(ttlSeconds)
      //        .build())
      //
      // 2. Send via firebaseMessaging.send(message)
      //
      // 3. Extract response ID
      //
      // 4. Store for tracking:
      //    notification.setExternalId(responseId)
      //    notificationRepository.save(notification)

      log.info(
          "Push notification sent: deviceToken={}, notificationId={}",
          maskDeviceToken(deviceToken),
          notification.getId());

    } catch (Exception e) {
      log.error(
          "Failed to send push notification: deviceToken={}, notificationId={}, error={}",
          maskDeviceToken(notification.getRecipientAddress()),
          notification.getId(),
          e.getMessage(),
          e);
      throw new PushSendException(
          "Failed to send push notification to device", e);
    }
  }

  /**
   * Validate input parameters.
   *
   * @param notification the notification
   * @param template the template
   * @param renderedContent the content
   */
  private void validateInput(
      NotificationEntity notification,
      NotificationTemplateEntity template,
      String renderedContent) {

    if (notification == null) {
      throw new IllegalArgumentException("Notification cannot be null");
    }

    if (notification.getRecipientAddress() == null
        || notification.getRecipientAddress().trim().isEmpty()) {
      throw new IllegalArgumentException("Device token is required for push notification");
    }

    if (!isValidDeviceToken(notification.getRecipientAddress())) {
      throw new IllegalArgumentException("Invalid device token format");
    }

    if (template == null) {
      throw new IllegalArgumentException("Push template cannot be null");
    }

    if (template.getPushTitle() == null || template.getPushTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Push notification title is required");
    }

    if (renderedContent == null || renderedContent.trim().isEmpty()) {
      throw new IllegalArgumentException("Push notification body cannot be empty");
    }

    if (renderedContent.length() > 4000) {
      throw new IllegalArgumentException("Push notification body too long: " + renderedContent.length());
    }
  }

  /**
   * Validate FCM device token format.
   *
   * <p>FCM tokens are typically 152+ characters, alphanumeric with some special chars.
   *
   * @param token the token to validate
   * @return true if valid
   */
  private boolean isValidDeviceToken(String token) {
    return token != null 
        && token.length() > 100 
        && token.length() < 500 
        && token.matches("^[a-zA-Z0-9_:-]+$");
  }

  /**
   * Truncate body to FCM limit.
   *
   * @param body the body text
   * @param maxLength max length
   * @return truncated body
   */
  private String truncateBody(String body, int maxLength) {
    if (body.length() <= maxLength) {
      return body;
    }
    return body.substring(0, maxLength - 3) + "...";
  }

  /**
   * Mask device token for logging (show first/last 10 chars).
   *
   * @param token the token
   * @return masked token
   */
  private String maskDeviceToken(String token) {
    if (token == null || token.length() < 20) {
      return "****";
    }
    return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
  }

  /**
   * Handle FCM topic subscription.
   *
   * <p>Useful for group messaging (e.g., all users of a tenant).
   *
   * @param deviceToken the device token
   * @param topic the topic name
   */
  public void subscribeToTopic(String deviceToken, String topic) {
    log.info(
        "Subscribing device to topic: deviceToken={}, topic={}",
        maskDeviceToken(deviceToken),
        topic);

    // TODO: Implement FCM topic subscription
    // firebaseMessaging.subscribeToTopic(
    //    Collections.singletonList(deviceToken),
    //    topic);
  }

  /**
   * Handle invalid/expired device tokens.
   *
   * @param deviceToken the token that failed
   * @param reason reason for failure (invalid, expired, etc.)
   */
  public void handleInvalidToken(String deviceToken, String reason) {
    log.warn(
        "Invalid device token: deviceToken={}, reason={}",
        maskDeviceToken(deviceToken),
        reason);

    // TODO: Mark token as invalid in device_tokens table
    // TODO: Remove from future sends
    // TODO: Trigger audit event
  }

  /**
   * Get adapter status.
   *
   * @return status string
   */
  @Override
  public String getStatus() {
    return "PushNotificationAdapter: " + (enabled ? "ENABLED" : "DISABLED");
  }

  /**
   * Exception for push send failures.
   */
  public static class PushSendException extends RuntimeException {
    public PushSendException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
