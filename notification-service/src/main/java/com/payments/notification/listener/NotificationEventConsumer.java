package com.payments.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.notification.domain.model.NotificationEntity;
import com.payments.notification.domain.model.NotificationStatus;
import com.payments.notification.domain.model.NotificationType;
import com.payments.notification.repository.NotificationRepository;
import com.payments.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.rebalance.ConsumerSeekToCurrentErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka listener for payment events using competing consumers pattern.
 *
 * <p>Architecture:
 * - Multiple instances of this consumer in same Kafka group
 * - Kafka distributes partitions across instances
 * - Each instance processes events in parallel
 * - Automatic failover and load balancing
 * - Manual offset commit for reliability
 *
 * <p>Events consumed:
 * - payment.initiated
 * - payment.cleared
 * - payment.failed
 *
 * @author Payment Engine
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventConsumer {

  private final NotificationService notificationService;
  private final NotificationRepository notificationRepository;
  private final ObjectMapper objectMapper;

  /**
   * Listen for payment events and queue notifications.
   *
   * <p>Competing consumers pattern:
   * - Multiple instances consume from same group
   * - Kafka ensures each partition goes to one instance
   * - Load balancing automatic
   * - Rebalancing handled transparently
   *
   * @param eventJson raw JSON event from Kafka
   * @param partition partition number
   * @param offset message offset
   * @param ack manual acknowledgment (only commit on success)
   */
  @KafkaListener(
      topics = "payment.initiated,payment.cleared,payment.failed",
      groupId = "notification-service-group",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handlePaymentEvent(
      @Payload String eventJson,
      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      Acknowledgment ack) {

    try {
      log.info(
          "Received payment event: partition={}, offset={}, eventSize={}",
          partition,
          offset,
          eventJson.length());

      // 1. Deserialize event
      PaymentEvent event = deserializeEvent(eventJson);
      if (event == null) {
        log.warn("Failed to deserialize event at partition={}, offset={}", partition, offset);
        ack.acknowledge();
        return;
      }

      // 2. Validate event
      if (!validateEvent(event)) {
        log.warn(
            "Invalid event: tenantId={}, userId={}, type={}",
            event.getTenantId(),
            event.getUserId(),
            event.getType());
        ack.acknowledge(); // Acknowledge to avoid reprocessing
        return;
      }

      // 3. Queue notification for delivery
      queueNotification(event);

      log.info(
          "Successfully queued notification: eventId={}, type={}, tenantId={}",
          event.getId(),
          event.getType(),
          event.getTenantId());

      // 4. Commit offset (only on success)
      ack.acknowledge();

    } catch (Exception e) {
      log.error(
          "Error processing payment event at partition={}, offset={}: error={}",
          partition,
          offset,
          e.getMessage(),
          e);
      // Note: NOT acknowledging on exception causes Kafka to retry
      // This implements dead letter queue pattern via reprocessing
    }
  }

  /**
   * Deserialize JSON event from Kafka.
   *
   * @param eventJson raw JSON
   * @return deserialized event, or null if invalid
   */
  private PaymentEvent deserializeEvent(String eventJson) {
    try {
      return objectMapper.readValue(eventJson, PaymentEvent.class);
    } catch (Exception e) {
      log.error("Failed to deserialize event: error={}", e.getMessage());
      return null;
    }
  }

  /**
   * Validate event structure and required fields.
   *
   * @param event the event to validate
   * @return true if valid
   */
  private boolean validateEvent(PaymentEvent event) {
    if (event == null) {
      log.warn("Event is null");
      return false;
    }

    if (event.getId() == null || event.getId().toString().isBlank()) {
      log.warn("Event missing ID");
      return false;
    }

    if (event.getTenantId() == null || event.getTenantId().isBlank()) {
      log.warn("Event missing tenantId: eventId={}", event.getId());
      return false;
    }

    if (event.getUserId() == null || event.getUserId().isBlank()) {
      log.warn("Event missing userId: eventId={}", event.getId());
      return false;
    }

    if (event.getType() == null) {
      log.warn("Event missing type: eventId={}", event.getId());
      return false;
    }

    // Validate email/phone (at least one required for notification)
    if ((event.getUserEmail() == null || event.getUserEmail().isBlank())
        && (event.getUserPhone() == null || event.getUserPhone().isBlank())) {
      log.warn(
          "Event has no email or phone: eventId={}, userId={}",
          event.getId(),
          event.getUserId());
      return false;
    }

    return true;
  }

  /**
   * Queue notification for delivery.
   *
   * <p>Creates NotificationEntity in PENDING status for each payment event. The
   * NotificationService will handle dispatch to channels.
   *
   * @param event the payment event
   */
  private void queueNotification(PaymentEvent event) {
    try {
      // Map payment event type to notification type
      NotificationType notificationType = mapEventType(event.getType());

      // Create notification entity
      NotificationEntity notification =
          NotificationEntity.builder()
              .id(UUID.randomUUID())
              .tenantId(event.getTenantId())
              .userId(event.getUserId())
              .templateId(UUID.fromString("12345678-1234-1234-1234-123456789001"))
              // TODO: look up actual template ID from database
              .notificationType(notificationType)
              .channelType(
                  null) // Will be determined by service based on preferences
              .recipientAddress(
                  event.getUserEmail() != null
                      ? event.getUserEmail()
                      : event.getUserPhone())
              .templateData(
                  objectMapper.writeValueAsString(
                      new TemplateData(
                          event.getId().toString(),
                          event.getAmount(),
                          event.getCurrency(),
                          event.getStatus())))
              .status(NotificationStatus.PENDING)
              .attempts(0)
              .build();

      // Save to database (queued for processing)
      notificationRepository.save(notification);

      log.debug(
          "Queued notification: notificationId={}, tenantId={}, type={}",
          notification.getId(),
          notification.getTenantId(),
          notification.getNotificationType());

    } catch (Exception e) {
      log.error(
          "Failed to queue notification: eventId={}, error={}",
          event.getId(),
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to queue notification", e);
    }
  }

  /**
   * Map payment event type to notification type.
   *
   * @param eventType the payment event type
   * @return corresponding notification type
   */
  private NotificationType mapEventType(String eventType) {
    return switch (eventType.toLowerCase()) {
      case "payment.initiated" -> NotificationType.PAYMENT_INITIATED;
      case "payment.cleared" -> NotificationType.PAYMENT_CLEARED;
      case "payment.failed" -> NotificationType.PAYMENT_FAILED;
      case "payment.validated" -> NotificationType.PAYMENT_VALIDATED;
      case "payment.reversed" -> NotificationType.PAYMENT_REVERSED;
      default -> {
        log.warn("Unknown event type: {}", eventType);
        yield NotificationType.PAYMENT_INITIATED;
      }
    };
  }

  /**
   * DTO for payment event from Kafka.
   */
  public static class PaymentEvent {
    private UUID id;
    private String tenantId;
    private String userId;
    private String type;
    private String status;
    private java.math.BigDecimal amount;
    private String currency;
    private String userEmail;
    private String userPhone;

    // Getters and setters (generated by lombok in real code)
    public UUID getId() {
      return id;
    }

    public void setId(UUID id) {
      this.id = id;
    }

    public String getTenantId() {
      return tenantId;
    }

    public void setTenantId(String tenantId) {
      this.tenantId = tenantId;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public java.math.BigDecimal getAmount() {
      return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
      this.amount = amount;
    }

    public String getCurrency() {
      return currency;
    }

    public void setCurrency(String currency) {
      this.currency = currency;
    }

    public String getUserEmail() {
      return userEmail;
    }

    public void setUserEmail(String userEmail) {
      this.userEmail = userEmail;
    }

    public String getUserPhone() {
      return userPhone;
    }

    public void setUserPhone(String userPhone) {
      this.userPhone = userPhone;
    }
  }

  /**
   * DTO for notification template data.
   */
  public static class TemplateData {
    public String paymentId;
    public java.math.BigDecimal amount;
    public String currency;
    public String status;

    public TemplateData(
        String paymentId, java.math.BigDecimal amount, String currency, String status) {
      this.paymentId = paymentId;
      this.amount = amount;
      this.currency = currency;
      this.status = status;
    }
  }
}
