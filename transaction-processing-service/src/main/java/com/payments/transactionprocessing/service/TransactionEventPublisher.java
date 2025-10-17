package com.payments.transactionprocessing.service;

import com.payments.domain.transaction.*;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/** Service for publishing transaction events to Kafka */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  // Kafka topics for different event types
  private static final String TRANSACTION_CREATED_TOPIC = "transaction.created";
  private static final String TRANSACTION_PROCESSING_TOPIC = "transaction.processing";
  private static final String TRANSACTION_CLEARING_TOPIC = "transaction.clearing";
  private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
  private static final String TRANSACTION_FAILED_TOPIC = "transaction.failed";

  /** Publishes all events for a transaction */
  public void publishTransactionEvents(Transaction transaction) {
    log.info("Publishing events for transaction {}", transaction.getId().getValue());

    for (TransactionEvent event : transaction.getEvents()) {
      publishEvent(event);
    }

    // Clear domain events after publishing
    transaction.clearDomainEvents();

    log.info("Events published for transaction {}", transaction.getId().getValue());
  }

  /** Publishes a single transaction event */
  public void publishEvent(TransactionEvent event) {
    log.debug(
        "Publishing event {} for transaction {}",
        event.getEventType(),
        event.getTransactionId().getValue());

    try {
      String topic = getTopicForEventType(event.getEventType());
      String key = event.getTransactionId().getValue();

      Map<String, Object> eventPayload = createEventPayload(event);

      kafkaTemplate
          .send(topic, key, eventPayload)
          .whenComplete(
              (result, ex) -> {
                if (ex == null) {
                  log.debug(
                      "Event {} published successfully to topic {}", event.getEventType(), topic);
                } else {
                  log.error(
                      "Failed to publish event {} to topic {}", event.getEventType(), topic, ex);
                }
              });

    } catch (Exception e) {
      log.error(
          "Error publishing event {} for transaction {}",
          event.getEventType(),
          event.getTransactionId().getValue(),
          e);
      throw new RuntimeException("Failed to publish transaction event", e);
    }
  }

  /** Publishes transaction created event */
  public void publishTransactionCreated(TransactionCreatedEvent event) {
    log.info(
        "Publishing transaction created event for transaction {}",
        event.getTransactionId().getValue());

    Map<String, Object> payload = createTransactionCreatedPayload(event);
    kafkaTemplate.send(TRANSACTION_CREATED_TOPIC, event.getTransactionId().getValue(), payload);
  }

  /** Publishes transaction processing event */
  public void publishTransactionProcessing(TransactionProcessingEvent event) {
    log.info(
        "Publishing transaction processing event for transaction {}",
        event.getTransactionId().getValue());

    Map<String, Object> payload = createTransactionProcessingPayload(event);
    kafkaTemplate.send(TRANSACTION_PROCESSING_TOPIC, event.getTransactionId().getValue(), payload);
  }

  /** Publishes transaction clearing event */
  public void publishTransactionClearing(TransactionClearingEvent event) {
    log.info(
        "Publishing transaction clearing event for transaction {}",
        event.getTransactionId().getValue());

    Map<String, Object> payload = createTransactionClearingPayload(event);
    kafkaTemplate.send(TRANSACTION_CLEARING_TOPIC, event.getTransactionId().getValue(), payload);
  }

  /** Publishes transaction completed event */
  public void publishTransactionCompleted(TransactionCompletedEvent event) {
    log.info(
        "Publishing transaction completed event for transaction {}",
        event.getTransactionId().getValue());

    Map<String, Object> payload = createTransactionCompletedPayload(event);
    kafkaTemplate.send(TRANSACTION_COMPLETED_TOPIC, event.getTransactionId().getValue(), payload);
  }

  /** Publishes transaction failed event */
  public void publishTransactionFailed(TransactionFailedEvent event) {
    log.info(
        "Publishing transaction failed event for transaction {}",
        event.getTransactionId().getValue());

    Map<String, Object> payload = createTransactionFailedPayload(event);
    kafkaTemplate.send(TRANSACTION_FAILED_TOPIC, event.getTransactionId().getValue(), payload);
  }

  private String getTopicForEventType(String eventType) {
    return switch (eventType) {
      case "TransactionCreatedEvent" -> TRANSACTION_CREATED_TOPIC;
      case "TransactionProcessingEvent" -> TRANSACTION_PROCESSING_TOPIC;
      case "TransactionClearingEvent" -> TRANSACTION_CLEARING_TOPIC;
      case "TransactionCompletedEvent" -> TRANSACTION_COMPLETED_TOPIC;
      case "TransactionFailedEvent" -> TRANSACTION_FAILED_TOPIC;
      default -> "transaction.unknown";
    };
  }

  private Map<String, Object> createEventPayload(TransactionEvent event) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("eventType", event.getEventType());
    payload.put("transactionId", event.getTransactionId().getValue());
    payload.put("tenantId", event.getTenantContext().getTenantId());
    payload.put("businessUnitId", event.getTenantContext().getBusinessUnitId());
    payload.put("occurredAt", event.getOccurredAt().toString());

    return payload;
  }

  private Map<String, Object> createTransactionCreatedPayload(TransactionCreatedEvent event) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("eventType", "TransactionCreatedEvent");
    payload.put("transactionId", event.getTransactionId().getValue());
    payload.put("tenantId", event.getTenantContext().getTenantId());
    payload.put("businessUnitId", event.getTenantContext().getBusinessUnitId());
    payload.put("occurredAt", event.getOccurredAt().toString());
    payload.put("paymentId", event.getPaymentId().getValue());
    payload.put("debitAccount", event.getDebitAccount().getValue());
    payload.put("creditAccount", event.getCreditAccount().getValue());
    payload.put("amount", event.getAmount().getAmount());
    payload.put("currency", event.getAmount().getCurrency().getCurrencyCode());

    return payload;
  }

  private Map<String, Object> createTransactionProcessingPayload(TransactionProcessingEvent event) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("eventType", "TransactionProcessingEvent");
    payload.put("transactionId", event.getTransactionId().getValue());
    payload.put("tenantId", event.getTenantContext().getTenantId());
    payload.put("businessUnitId", event.getTenantContext().getBusinessUnitId());
    payload.put("occurredAt", event.getOccurredAt().toString());

    return payload;
  }

  private Map<String, Object> createTransactionClearingPayload(TransactionClearingEvent event) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("eventType", "TransactionClearingEvent");
    payload.put("transactionId", event.getTransactionId().getValue());
    payload.put("tenantId", event.getTenantContext().getTenantId());
    payload.put("businessUnitId", event.getTenantContext().getBusinessUnitId());
    payload.put("occurredAt", event.getOccurredAt().toString());
    payload.put("clearingSystem", event.getClearingSystem());
    payload.put("clearingReference", event.getClearingReference());

    return payload;
  }

  private Map<String, Object> createTransactionCompletedPayload(TransactionCompletedEvent event) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("eventType", "TransactionCompletedEvent");
    payload.put("transactionId", event.getTransactionId().getValue());
    payload.put("tenantId", event.getTenantContext().getTenantId());
    payload.put("businessUnitId", event.getTenantContext().getBusinessUnitId());
    payload.put("occurredAt", event.getOccurredAt().toString());

    return payload;
  }

  private Map<String, Object> createTransactionFailedPayload(TransactionFailedEvent event) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("eventType", "TransactionFailedEvent");
    payload.put("transactionId", event.getTransactionId().getValue());
    payload.put("tenantId", event.getTenantContext().getTenantId());
    payload.put("businessUnitId", event.getTenantContext().getBusinessUnitId());
    payload.put("occurredAt", event.getOccurredAt().toString());
    payload.put("failureReason", event.getReason());

    return payload;
  }
}
