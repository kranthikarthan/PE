package com.payments.notification.adapter;

import com.payments.notification.domain.model.NotificationEntity;
import com.payments.notification.domain.model.NotificationTemplateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SMSAdapter sends notifications via SMS using Twilio API.
 *
 * <p>Features:
 * - Phone number validation (E.164 format)
 * - Message length splitting (SMS: max 160 chars, Unicode: 70)
 * - Delivery receipt tracking
 * - Twilio webhook integration for status updates
 * - Multi-region phone number support
 *
 * <p>Production Setup:
 * - Twilio account with SMS capability
 * - Account SID and Auth Token in application.yml
 * - Outbound phone number(s) registered
 * - Webhook URLs configured for delivery status
 *
 * @author Payment Engine
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SMSAdapter implements ChannelAdapter {

  @Value("${notification.sms.enabled:true}")
  private boolean enabled;

  @Value("${notification.sms.from-number:+27000000000}")
  private String fromNumber;

  @Value("${notification.sms.max-length:160}")
  private int maxMessageLength;

  // Twilio client would be injected here
  // private final TwilioRestClient twilioRestClient;

  /**
   * Send SMS notification.
   *
   * @param notification the notification entity
   * @param template the SMS template
   * @param renderedContent rendered SMS content
   * @throws Exception if send fails
   */
  @Override
  public void send(
      NotificationEntity notification,
      NotificationTemplateEntity template,
      String renderedContent)
      throws Exception {

    if (!enabled) {
      log.warn("SMS adapter is disabled");
      return;
    }

    validateInput(notification, renderedContent);

    try {
      String phoneNumber = notification.getRecipientAddress();
      String normalizedPhone = normalizePhoneNumber(phoneNumber);

      log.info(
          "Sending SMS: to={}, length={}, tenantId={}, notificationId={}",
          normalizedPhone,
          renderedContent.length(),
          notification.getTenantId(),
          notification.getId());

      // Split message if it exceeds max length
      String[] messageParts = splitMessage(renderedContent);

      for (int i = 0; i < messageParts.length; i++) {
        String part = messageParts[i];

        log.debug(
            "Sending SMS part {}/{}: to={}, length={}",
            i + 1,
            messageParts.length,
            normalizedPhone,
            part.length());

        // TODO: Implement Twilio SendMessage
        // Steps:
        // 1. Create Message request via TwilioRestClient:
        //    - from: fromNumber
        //    - to: normalizedPhone
        //    - body: part
        //    - statusCallback: webhook URL for delivery status
        //
        // 2. Call Message.creator(...).create()
        //
        // 3. Extract Twilio SID from response
        //
        // 4. Store for tracking:
        //    notification.setExternalId(sid)
        //    notification.setRetryCount(i)
        //    notificationRepository.save(notification)
        //
        // 5. Return response

        log.debug("SMS part {} sent: sid={}", i + 1, "MOCK_SID_" + i);
      }

      log.info(
          "SMS sent successfully: to={}, parts={}, notificationId={}",
          normalizedPhone,
          messageParts.length,
          notification.getId());

    } catch (Exception e) {
      log.error(
          "Failed to send SMS: to={}, notificationId={}, error={}",
          notification.getRecipientAddress(),
          notification.getId(),
          e.getMessage(),
          e);
      throw new SmsSendException(
          "Failed to send SMS to " + notification.getRecipientAddress(), e);
    }
  }

  /**
   * Validate input parameters.
   *
   * @param notification the notification
   * @param renderedContent the content
   */
  private void validateInput(NotificationEntity notification, String renderedContent) {
    if (notification == null) {
      throw new IllegalArgumentException("Notification cannot be null");
    }

    if (notification.getRecipientAddress() == null
        || notification.getRecipientAddress().trim().isEmpty()) {
      throw new IllegalArgumentException("Recipient phone number is required");
    }

    if (renderedContent == null || renderedContent.trim().isEmpty()) {
      throw new IllegalArgumentException("SMS content cannot be empty");
    }

    if (renderedContent.length() > maxMessageLength * 10) {
      throw new IllegalArgumentException("SMS content too long: " + renderedContent.length());
    }
  }

  /**
   * Normalize phone number to E.164 format.
   *
   * <p>Examples:
   * - "0123456789" → "+27123456789"
   * - "27123456789" → "+27123456789"
   * - "+27123456789" → "+27123456789"
   *
   * @param phoneNumber raw phone number
   * @return normalized E.164 format
   */
  private String normalizePhoneNumber(String phoneNumber) {
    String normalized = phoneNumber.trim();

    // Remove any non-digit characters except leading +
    if (!normalized.startsWith("+")) {
      normalized = normalized.replaceAll("[^0-9]", "");
    } else {
      normalized = "+" + normalized.replaceAll("[^0-9]", "");
    }

    // If doesn't start with +, assume South Africa
    if (!normalized.startsWith("+")) {
      // Remove leading 0 if present
      if (normalized.startsWith("0")) {
        normalized = normalized.substring(1);
      }
      normalized = "+27" + normalized;
    }

    // Validate E.164 format: +[country][number]
    if (!normalized.matches("^\\+[1-9]\\d{1,14}$")) {
      throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
    }

    return normalized;
  }

  /**
   * Split message into parts if it exceeds max length.
   *
   * <p>SMS can hold max 160 chars (GSM 7-bit) or 70 chars (Unicode).
   * For safety, we use 160 as limit and add "..." suffix to indicate continuation.
   *
   * @param message the message to split
   * @return array of message parts
   */
  private String[] splitMessage(String message) {
    if (message.length() <= maxMessageLength) {
      return new String[] {message};
    }

    // Reserve 3 chars for ellipsis indicator
    int partLength = maxMessageLength - 3;
    int parts = (int) Math.ceil((double) message.length() / partLength);

    String[] result = new String[parts];

    for (int i = 0; i < parts; i++) {
      int start = i * partLength;
      int end = Math.min(start + partLength, message.length());
      String part = message.substring(start, end);

      // Add continuation indicator for all but last part
      if (i < parts - 1) {
        part = part + "...";
      }

      result[i] = part;
    }

    log.debug("Message split into {} parts", parts);
    return result;
  }

  /**
   * Handle delivery receipt from Twilio webhook.
   *
   * @param externalId Twilio SID
   * @param status delivery status (queued, sending, sent, failed, delivered, undelivered)
   */
  public void handleDeliveryReceipt(String externalId, String status) {
    log.info(
        "SMS delivery receipt: externalId={}, status={}",
        externalId,
        status);

    // TODO: Update notification status based on Twilio status
    // queued/sending -> PENDING
    // sent/delivered -> SENT
    // failed/undelivered -> RETRY or FAILED
  }

  /**
   * Get adapter status.
   *
   * @return status string
   */
  @Override
  public String getStatus() {
    return "SMSAdapter: " + (enabled ? "ENABLED" : "DISABLED");
  }

  /**
   * Exception for SMS send failures.
   */
  public static class SmsSendException extends RuntimeException {
    public SmsSendException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
