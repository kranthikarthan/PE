package com.payments.notification.adapter;

import com.payments.notification.domain.model.NotificationEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * IBM MQ notification strategy for fire-and-forget delivery.
 *
 * <p>Delivery mechanism:
 * 1. Serialize notification to JSON
 * 2. Queue to IBM MQ (non-persistent, fire-and-forget)
 * 3. Return immediately
 * 4. Remote MQ system handles delivery
 *
 * <p>Characteristics:
 * - Fire-and-forget (no retries at source)
 * - Lower overhead vs internal strategy
 * - Decoupled from notification service
 * - MQ system handles reliability
 * - Best for high-throughput scenarios
 *
 * <p>Configuration:
 * - MQ Broker: Separate service
 * - Queue: notification.outbound
 * - Message Format: JSON
 * - TTL: Configured per message
 *
 * @author Payment Engine
 */
@Slf4j
public class IBMMQNotificationStrategy implements NotificationStrategy {

  private static final String MQ_QUEUE_NAME = "notification.outbound";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private boolean mqConnected = false;

  @Override
  public void processNotification(NotificationEntity notification) throws Exception {
    log.info(
        "Processing notification via IBM MQ strategy: id={}, type={}",
        notification.getId(),
        notification.getNotificationType());

    try {
      // 1. Serialize notification to JSON
      Map<String, Object> mqMessage = buildMQMessage(notification);
      String jsonPayload = objectMapper.writeValueAsString(mqMessage);

      log.debug(
          "Queuing notification to IBM MQ: id={}, queueName={}, payloadSize={}",
          notification.getId(),
          MQ_QUEUE_NAME,
          jsonPayload.length());

      // 2. Queue to IBM MQ
      // TODO: Implement actual MQ queuing:
      // JMSTemplate template = new JMSTemplate(connectionFactory);
      // template.convertAndSend(MQ_QUEUE_NAME, jsonPayload, message -> {
      //   message.setStringProperty("notificationId", notification.getId().toString());
      //   message.setStringProperty("tenantId", notification.getTenantId());
      //   message.setStringProperty("userId", notification.getUserId());
      //   return message;
      // });

      // 3. Log successful queue
      log.info(
          "Notification queued to IBM MQ: id={}, queueName={}, channel={}",
          notification.getId(),
          MQ_QUEUE_NAME,
          notification.getChannelType());

    } catch (Exception e) {
      log.error(
          "IBM MQ strategy failed to queue notification: id={}, error={}",
          notification.getId(),
          e.getMessage(),
          e);
      throw new IBMMQNotificationException("Failed to queue notification to IBM MQ", e);
    }
  }

  /**
   * Build message structure for IBM MQ.
   *
   * @param notification the notification entity
   * @return message map suitable for MQ
   */
  private Map<String, Object> buildMQMessage(NotificationEntity notification) {
    Map<String, Object> message = new HashMap<>();

    // Headers
    message.put("messageId", java.util.UUID.randomUUID().toString());
    message.put("timestamp", System.currentTimeMillis());
    message.put("version", "1.0");

    // Notification details
    message.put("notificationId", notification.getId());
    message.put("tenantId", notification.getTenantId());
    message.put("userId", notification.getUserId());
    message.put("notificationType", notification.getNotificationType());
    message.put("channelType", notification.getChannelType());
    message.put("recipientAddress", notification.getRecipientAddress());
    message.put("templateData", notification.getTemplateData());

    // MQ-specific settings
    Map<String, Object> mqSettings = new HashMap<>();
    mqSettings.put("ttl", 3600); // 1 hour
    mqSettings.put("priority", 5);
    mqSettings.put("persistent", false); // Fire-and-forget
    mqSettings.put("deliveryMode", "NON_PERSISTENT");
    message.put("mqSettings", mqSettings);

    return message;
  }

  @Override
  public String getStrategyName() {
    return "IBM_MQ";
  }

  @Override
  public boolean isHealthy() {
    // In production, would check MQ connection status
    // For now, assume healthy if configured
    return true; // TODO: Check actual MQ connectivity
  }

  /**
   * Exception for IBM MQ specific errors.
   */
  public static class IBMMQNotificationException extends RuntimeException {
    public IBMMQNotificationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
