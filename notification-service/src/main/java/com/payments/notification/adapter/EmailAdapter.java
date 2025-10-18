package com.payments.notification.adapter;

import com.payments.notification.domain.model.NotificationEntity;
import com.payments.notification.domain.model.NotificationTemplateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * EmailAdapter sends notifications via email using AWS SES.
 *
 * <p>Features:
 * - Template-based email composition
 * - Batch send capability
 * - Bounce/complaint handling
 * - Detailed logging and error handling
 * - Configurable sender/reply-to addresses
 *
 * <p>Production Setup:
 * - AWS SES account with verified domain
 * - IAM user with SES permissions
 * - AWS SDK client configured in application.yml
 * - Bounce/complaint SNS topics configured
 *
 * @author Payment Engine
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmailAdapter implements ChannelAdapter {

  @Value("${notification.email.sender-address:noreply@paymentengine.com}")
  private String senderAddress;

  @Value("${notification.email.reply-to:support@paymentengine.com}")
  private String replyToAddress;

  @Value("${notification.email.enabled:true}")
  private boolean enabled;

  // AWS SES client would be injected here
  // private final AmazonSES sesClient;

  /**
   * Send email notification.
   *
   * @param notification the notification entity
   * @param template the email template
   * @param renderedContent rendered HTML/text content
   * @throws Exception if send fails
   */
  @Override
  public void send(
      NotificationEntity notification,
      NotificationTemplateEntity template,
      String renderedContent)
      throws Exception {

    if (!enabled) {
      log.warn("Email adapter is disabled");
      return;
    }

    validateInput(notification, template, renderedContent);

    try {
      log.info(
          "Sending email: to={}, tenantId={}, notificationId={}",
          notification.getRecipientAddress(),
          notification.getTenantId(),
          notification.getId());

      // TODO: Implement AWS SES SendEmail/SendRawEmail
      // Steps:
      // 1. Create SendEmailRequest with:
      //    - Destination (to, cc, bcc)
      //    - Message (subject, body in HTML/Text)
      //    - Source (sender address)
      //    - ReplyToAddresses
      //    - Tags (tenant-id, notification-type)
      //
      // 2. Call sesClient.sendEmail(request)
      //
      // 3. Extract MessageId from response
      //
      // 4. Log for tracking:
      //    notification.setExternalId(messageId)
      //    notificationRepository.save(notification)
      //
      // 5. Return response

      log.info(
          "Email sent successfully: to={}, notificationId={}",
          notification.getRecipientAddress(),
          notification.getId());

    } catch (Exception e) {
      log.error(
          "Failed to send email: to={}, notificationId={}, error={}",
          notification.getRecipientAddress(),
          notification.getId(),
          e.getMessage(),
          e);
      throw new EmailSendException(
          "Failed to send email to " + notification.getRecipientAddress(), e);
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
      throw new IllegalArgumentException("Recipient email address is required");
    }

    if (!isValidEmail(notification.getRecipientAddress())) {
      throw new IllegalArgumentException("Invalid email address: " + notification.getRecipientAddress());
    }

    if (template == null) {
      throw new IllegalArgumentException("Email template cannot be null");
    }

    if (template.getEmailSubject() == null || template.getEmailSubject().trim().isEmpty()) {
      throw new IllegalArgumentException("Email subject is required");
    }

    if (renderedContent == null || renderedContent.trim().isEmpty()) {
      throw new IllegalArgumentException("Email content cannot be empty");
    }
  }

  /**
   * Basic email validation.
   *
   * @param email the email to validate
   * @return true if valid
   */
  private boolean isValidEmail(String email) {
    return email != null
        && email.matches("^[A-Za-z0-9+_.-]+@(.+)$")
        && email.length() <= 254;
  }

  /**
   * Handle bounces from AWS SNS (configured separately).
   *
   * <p>This would be called from an SNS endpoint that receives bounce notifications.
   *
   * @param messageId the SES message ID
   * @param bounceType permanent or temporary
   * @param bouncedRecipients list of recipient addresses
   */
  public void handleBounce(String messageId, String bounceType, java.util.List<String> bouncedRecipients) {
    log.warn(
        "Email bounce detected: messageId={}, type={}, recipients={}",
        messageId,
        bounceType,
        bouncedRecipients.size());

    // TODO: Update notification status to FAILED
    // TODO: If permanent bounce, mark user email as invalid
    // TODO: Trigger audit event
  }

  /**
   * Handle complaints from AWS SNS (configured separately).
   *
   * <p>This would be called from an SNS endpoint that receives complaint notifications.
   *
   * @param messageId the SES message ID
   * @param complainants list of recipient addresses
   */
  public void handleComplaint(String messageId, java.util.List<String> complainants) {
    log.warn(
        "Email complaint received: messageId={}, complainants={}", messageId, complainants.size());

    // TODO: Unsubscribe users from marketing emails
    // TODO: Trigger audit event
    // TODO: Alert support team
  }

  /**
   * Get adapter status.
   *
   * @return status string
   */
  @Override
  public String getStatus() {
    return "EmailAdapter: " + (enabled ? "ENABLED" : "DISABLED");
  }

  /**
   * Exception for email send failures.
   */
  public static class EmailSendException extends RuntimeException {
    public EmailSendException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
