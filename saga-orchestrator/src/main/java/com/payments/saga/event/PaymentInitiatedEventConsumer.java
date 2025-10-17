package com.payments.saga.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.domain.shared.TenantContext;
import com.payments.saga.exception.EventValidationException;
import com.payments.saga.exception.SagaOrchestrationException;
import com.payments.saga.service.SagaOrchestrator;
import com.payments.saga.service.TenantContextResolverInterface;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Consumer for PaymentInitiatedEvent to start payment processing sagas */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentInitiatedEventConsumer {

  private final SagaOrchestrator sagaOrchestrator;
  private final ObjectMapper objectMapper;
  private final SagaEventPublisher eventPublisher;
  private final TenantContextResolverInterface tenantContextResolver;

  @KafkaListener(topics = "payment.initiated", groupId = "saga-orchestrator")
  public void handlePaymentInitiated(
      @Payload String message,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      @Header(value = "X-Tenant-Id", required = false) String tenantId,
      @Header(value = "X-Business-Unit-Id", required = false) String businessUnitId,
      @Header(value = "X-Correlation-Id", required = false) String correlationId) {

    try {
      log.info(
          "Received PaymentInitiatedEvent from topic {} (partition: {}, offset: {})",
          topic,
          partition,
          offset);

      // Validate event payload
      Map<String, Object> eventData = validateAndParseEvent(message);
      String paymentId = extractPaymentId(eventData);
      String sagaTemplate = determineSagaTemplate(eventData);

      // Resolve tenant context properly
      TenantContext tenantContext = tenantContextResolver.resolve(tenantId, businessUnitId);

      // Start the appropriate saga
      sagaOrchestrator.startSaga(
          sagaTemplate,
          tenantContext,
          correlationId != null ? correlationId : generateCorrelationId(),
          paymentId,
          eventData);

      log.info("Successfully started saga for payment {}", paymentId);

    } catch (EventValidationException e) {
      log.error("Event validation failed: {}", e.getMessage(), e);
      // Send to dead letter queue
      eventPublisher.publishToDeadLetterQueue(topic, correlationId, message, e);
    } catch (SagaOrchestrationException e) {
      log.error("Saga orchestration failed: {}", e.getMessage(), e);
      // Send to dead letter queue
      eventPublisher.publishToDeadLetterQueue(topic, correlationId, message, e);
    } catch (Exception e) {
      log.error("Unexpected error processing PaymentInitiatedEvent: {}", e.getMessage(), e);
      // Send to dead letter queue
      eventPublisher.publishToDeadLetterQueue(topic, correlationId, message, e);
    }
  }

  /** Determine the appropriate saga template based on payment characteristics */
  private String determineSagaTemplate(Map<String, Object> eventData) {
    // Extract payment characteristics
    Object amount = eventData.get("amount");
    Object paymentType = eventData.get("paymentType");
    Object priority = eventData.get("priority");

    // Determine template based on business rules
    if (isHighValuePayment(amount)) {
      return "HighValuePaymentSaga";
    } else if (isFastPayment(paymentType, priority)) {
      return "FastPaymentSaga";
    } else {
      return "PaymentProcessingSaga";
    }
  }

  private boolean isHighValuePayment(Object amount) {
    if (amount instanceof Number) {
      return ((Number) amount).doubleValue() > 10000.0; // $10,000 threshold
    }
    return false;
  }

  private boolean isFastPayment(Object paymentType, Object priority) {
    return "FAST".equals(paymentType) || "URGENT".equals(priority);
  }

  private Map<String, Object> validateAndParseEvent(String message) {
    try {
      Map<String, Object> eventData = objectMapper.readValue(message, Map.class);

      // Validate required fields
      if (!eventData.containsKey("paymentId")) {
        throw new EventValidationException("Missing required field: paymentId");
      }
      if (!eventData.containsKey("amount")) {
        throw new EventValidationException("Missing required field: amount");
      }

      return eventData;
    } catch (JsonProcessingException e) {
      throw new EventValidationException("Failed to parse event JSON", e);
    }
  }

  private String extractPaymentId(Map<String, Object> eventData) {
    Object paymentId = eventData.get("paymentId");
    if (paymentId == null) {
      throw new EventValidationException("Payment ID cannot be null");
    }
    return paymentId.toString();
  }

  private String generateCorrelationId() {
    return "corr-"
        + System.currentTimeMillis()
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }
}
