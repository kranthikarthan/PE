package com.payments.paymentinitiation.saga;

import com.payments.iso20022.canonical.CanonicalPaymentModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Saga Event Publisher for ISO 20022 pain.001/pain.002 flows
 *
 * <p>Publishes saga events to Kafka for distributed saga coordination
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  // Kafka Topics
  private static final String SAGA_STARTED_TOPIC = "payment.saga.started";
  private static final String SAGA_COMPLETED_TOPIC = "payment.saga.completed";
  private static final String SAGA_COMPENSATED_TOPIC = "payment.saga.compensated";
  private static final String PAYMENT_PROCESSING_INITIATED_TOPIC = "payment.processing.initiated";
  private static final String PAYMENT_PROCESSING_CANCELLED_TOPIC = "payment.processing.cancelled";
  private static final String PAYMENT_PROCESSING_COMPLETED_TOPIC = "payment.processing.completed";

  // PayShap-specific topics
  private static final String PAYSHAP_PROCESSING_INITIATED_TOPIC = "payshap.processing.initiated";
  private static final String PAYSHAP_PROCESSING_COMPLETED_TOPIC = "payshap.processing.completed";
  private static final String PAYSHAP_PROCESSING_FAILED_TOPIC = "payshap.processing.failed";

  /** Publish saga started event */
  public void publishSagaStarted(
      String sagaId, String correlationId, CanonicalPaymentModel canonicalPayment) {
    try {
      SagaStartedEvent event =
          SagaStartedEvent.builder()
              .sagaId(sagaId)
              .correlationId(correlationId)
              .paymentId(canonicalPayment.getPaymentId())
              .messageId(canonicalPayment.getMessageId())
              .tenantId(canonicalPayment.getTenantContext().getTenantId())
              .businessUnitId(canonicalPayment.getBusinessUnitId())
              .timestamp(System.currentTimeMillis())
              .build();

      kafkaTemplate.send(SAGA_STARTED_TOPIC, sagaId, event);
      log.info("Published saga started event: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to publish saga started event: {}", sagaId, e);
      throw new RuntimeException("Failed to publish saga started event", e);
    }
  }

  /** Publish saga completed event */
  public void publishSagaCompleted(String sagaId, String paymentStatus) {
    try {
      SagaCompletedEvent event =
          SagaCompletedEvent.builder()
              .sagaId(sagaId)
              .paymentStatus(paymentStatus)
              .timestamp(System.currentTimeMillis())
              .build();

      kafkaTemplate.send(SAGA_COMPLETED_TOPIC, sagaId, event);
      log.info("Published saga completed event: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to publish saga completed event: {}", sagaId, e);
      throw new RuntimeException("Failed to publish saga completed event", e);
    }
  }

  /** Publish saga compensated event */
  public void publishSagaCompensated(String sagaId, String compensationReason) {
    try {
      SagaCompensatedEvent event =
          SagaCompensatedEvent.builder()
              .sagaId(sagaId)
              .compensationReason(compensationReason)
              .timestamp(System.currentTimeMillis())
              .build();

      kafkaTemplate.send(SAGA_COMPENSATED_TOPIC, sagaId, event);
      log.info("Published saga compensated event: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to publish saga compensated event: {}", sagaId, e);
      throw new RuntimeException("Failed to publish saga compensated event", e);
    }
  }

  /** Publish payment processing initiated event */
  public void publishPaymentProcessingInitiated(
      String sagaId, CanonicalPaymentModel canonicalPayment) {
    try {
      PaymentProcessingInitiatedEvent event =
          PaymentProcessingInitiatedEvent.builder()
              .sagaId(sagaId)
              .paymentId(canonicalPayment.getPaymentId())
              .amount(convertMoney(canonicalPayment.getAmount()))
              .sourceAccount(canonicalPayment.getSourceAccount())
              .destinationAccount(canonicalPayment.getDestinationAccount())
              .executionDate(canonicalPayment.getExecutionDate())
              .tenantId(canonicalPayment.getTenantContext().getTenantId())
              .businessUnitId(canonicalPayment.getBusinessUnitId())
              .timestamp(System.currentTimeMillis())
              .build();

      kafkaTemplate.send(PAYMENT_PROCESSING_INITIATED_TOPIC, sagaId, event);
      log.info("Published payment processing initiated event: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to publish payment processing initiated event: {}", sagaId, e);
      throw new RuntimeException("Failed to publish payment processing initiated event", e);
    }
  }

  /** Publish payment processing cancelled event */
  public void publishPaymentProcessingCancelled(String sagaId) {
    try {
      PaymentProcessingCancelledEvent event =
          PaymentProcessingCancelledEvent.builder()
              .sagaId(sagaId)
              .timestamp(System.currentTimeMillis())
              .build();

      kafkaTemplate.send(PAYMENT_PROCESSING_CANCELLED_TOPIC, sagaId, event);
      log.info("Published payment processing cancelled event: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to publish payment processing cancelled event: {}", sagaId, e);
      throw new RuntimeException("Failed to publish payment processing cancelled event", e);
    }
  }

  /** Publish payment processing completed event */
  public void publishPaymentProcessingCompleted(
      String sagaId, String paymentStatus, String statusReason) {
    try {
      PaymentProcessingCompletedEvent event =
          PaymentProcessingCompletedEvent.builder()
              .sagaId(sagaId)
              .paymentStatus(paymentStatus)
              .statusReason(statusReason)
              .timestamp(System.currentTimeMillis())
              .build();

      kafkaTemplate.send(PAYMENT_PROCESSING_COMPLETED_TOPIC, sagaId, event);
      log.info("Published payment processing completed event: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to publish payment processing completed event: {}", sagaId, e);
      throw new RuntimeException("Failed to publish payment processing completed event", e);
    }
  }

  /** Publish PayShap processing initiated event */
  public void publishPayShapProcessingInitiated(
      String sagaId, CanonicalPaymentModel canonicalPayment) {
    try {
      PayShapProcessingInitiatedEvent event =
          PayShapProcessingInitiatedEvent.builder()
              .sagaId(sagaId)
              .paymentId(canonicalPayment.getPaymentId())
              .amount(convertMoney(canonicalPayment.getAmount()))
              .sourceAccount(canonicalPayment.getSourceAccount())
              .destinationAccount(canonicalPayment.getDestinationAccount())
              .executionDate(canonicalPayment.getExecutionDate())
              .tenantId(canonicalPayment.getTenantContext().getTenantId())
              .businessUnitId(canonicalPayment.getBusinessUnitId())
              .clearingSystem("PAYSHAP")
              .timestamp(System.currentTimeMillis())
              .build();

      kafkaTemplate.send(PAYSHAP_PROCESSING_INITIATED_TOPIC, sagaId, event);
      log.info("Published PayShap processing initiated event: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to publish PayShap processing initiated event: {}", sagaId, e);
      throw new RuntimeException("Failed to publish PayShap processing initiated event", e);
    }
  }

  /** Publish PayShap processing completed event */
  public void publishPayShapProcessingCompleted(
      String sagaId, String paymentStatus, String statusReason) {
    try {
      PayShapProcessingCompletedEvent event =
          PayShapProcessingCompletedEvent.builder()
              .sagaId(sagaId)
              .paymentStatus(paymentStatus)
              .statusReason(statusReason)
              .clearingSystem("PAYSHAP")
              .timestamp(System.currentTimeMillis())
              .build();

      kafkaTemplate.send(PAYSHAP_PROCESSING_COMPLETED_TOPIC, sagaId, event);
      log.info("Published PayShap processing completed event: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to publish PayShap processing completed event: {}", sagaId, e);
      throw new RuntimeException("Failed to publish PayShap processing completed event", e);
    }
  }

  /** Publish PayShap processing failed event */
  public void publishPayShapProcessingFailed(String sagaId, String error) {
    try {
      PayShapProcessingFailedEvent event =
          PayShapProcessingFailedEvent.builder()
              .sagaId(sagaId)
              .error(error)
              .clearingSystem("PAYSHAP")
              .timestamp(System.currentTimeMillis())
              .build();

      kafkaTemplate.send(PAYSHAP_PROCESSING_FAILED_TOPIC, sagaId, event);
      log.info("Published PayShap processing failed event: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to publish PayShap processing failed event: {}", sagaId, e);
      throw new RuntimeException("Failed to publish PayShap processing failed event", e);
    }
  }

  // ==================== EVENT CLASSES ====================

  @lombok.Data
  @lombok.Builder
  public static class SagaStartedEvent {
    private String sagaId;
    private String correlationId;
    private String paymentId;
    private String messageId;
    private String tenantId;
    private String businessUnitId;
    private long timestamp;
  }

  @lombok.Data
  @lombok.Builder
  public static class SagaCompletedEvent {
    private String sagaId;
    private String paymentStatus;
    private long timestamp;
  }

  @lombok.Data
  @lombok.Builder
  public static class SagaCompensatedEvent {
    private String sagaId;
    private String compensationReason;
    private long timestamp;
  }

  @lombok.Data
  @lombok.Builder
  public static class PaymentProcessingInitiatedEvent {
    private String sagaId;
    private String paymentId;
    private com.payments.contracts.shared.Money amount;
    private String sourceAccount;
    private String destinationAccount;
    private java.time.LocalDate executionDate;
    private String tenantId;
    private String businessUnitId;
    private long timestamp;
  }

  @lombok.Data
  @lombok.Builder
  public static class PaymentProcessingCancelledEvent {
    private String sagaId;
    private long timestamp;
  }

  @lombok.Data
  @lombok.Builder
  public static class PaymentProcessingCompletedEvent {
    private String sagaId;
    private String paymentStatus;
    private String statusReason;
    private long timestamp;
  }

  // PayShap-specific event classes
  @lombok.Data
  @lombok.Builder
  public static class PayShapProcessingInitiatedEvent {
    private String sagaId;
    private String paymentId;
    private com.payments.contracts.shared.Money amount;
    private String sourceAccount;
    private String destinationAccount;
    private java.time.LocalDate executionDate;
    private String tenantId;
    private String businessUnitId;
    private String clearingSystem;
    private long timestamp;
  }

  @lombok.Data
  @lombok.Builder
  public static class PayShapProcessingCompletedEvent {
    private String sagaId;
    private String paymentStatus;
    private String statusReason;
    private String clearingSystem;
    private long timestamp;
  }

  @lombok.Data
  @lombok.Builder
  public static class PayShapProcessingFailedEvent {
    private String sagaId;
    private String error;
    private String clearingSystem;
    private long timestamp;
  }

  /** Convert domain Money to contract Money */
  private com.payments.contracts.shared.Money convertMoney(
      com.payments.domain.shared.Money domainMoney) {
    if (domainMoney == null) {
      return null;
    }
    return com.payments.contracts.shared.Money.builder()
        .amount(domainMoney.getAmount())
        .currency(domainMoney.getCurrency())
        .build();
  }
}
