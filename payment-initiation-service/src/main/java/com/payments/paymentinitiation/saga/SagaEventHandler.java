package com.payments.paymentinitiation.saga;

import com.payments.paymentinitiation.saga.SagaEventPublisher.PaymentProcessingCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Saga Event Handler for ISO 20022 pain.001/pain.002 flows
 *
 * <p>Handles saga events and coordinates the completion of the saga
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventHandler {

  private final Pain001Pain002Saga saga;

  /**
   * Handle payment processing completed event
   *
   * <p>When payment processing is completed, complete the saga with pain.002
   */
  @KafkaListener(topics = "payment.processing.completed", groupId = "pain001-pain002-saga")
  public void handlePaymentProcessingCompleted(PaymentProcessingCompletedEvent event) {
    log.info("Received payment processing completed event for saga: {}", event.getSagaId());

    try {
      // Complete the saga with pain.002 generation
      saga.completeSagaWithPain002(
          event.getSagaId(), event.getPaymentStatus(), event.getStatusReason());

      log.info("Successfully completed saga with pain.002: {}", event.getSagaId());

    } catch (Exception e) {
      log.error("Failed to complete saga with pain.002: {}", event.getSagaId(), e);
      // The saga will handle compensation automatically
    }
  }

  /**
   * Handle payment processing failed event
   *
   * <p>When payment processing fails, compensate the saga
   */
  @KafkaListener(topics = "payment.processing.failed", groupId = "pain001-pain002-saga")
  public void handlePaymentProcessingFailed(PaymentProcessingFailedEvent event) {
    log.info("Received payment processing failed event for saga: {}", event.getSagaId());

    try {
      // Compensate the saga
      saga.compensateSagaSteps(event.getSagaId(), new RuntimeException(event.getFailureReason()));

      log.info("Successfully compensated saga: {}", event.getSagaId());

    } catch (Exception e) {
      log.error("Failed to compensate saga: {}", event.getSagaId(), e);
    }
  }

  /**
   * Handle saga timeout event
   *
   * <p>When saga times out, compensate the saga
   */
  @KafkaListener(topics = "payment.saga.timeout", groupId = "pain001-pain002-saga")
  public void handleSagaTimeout(SagaTimeoutEvent event) {
    log.info("Received saga timeout event for saga: {}", event.getSagaId());

    try {
      // Compensate the saga
      saga.compensateSagaSteps(event.getSagaId(), new RuntimeException("Saga timeout"));

      log.info("Successfully compensated timed-out saga: {}", event.getSagaId());

    } catch (Exception e) {
      log.error("Failed to compensate timed-out saga: {}", event.getSagaId(), e);
    }
  }

  // ==================== EVENT CLASSES ====================

  @lombok.Data
  @lombok.Builder
  public static class PaymentProcessingFailedEvent {
    private String sagaId;
    private String failureReason;
    private long timestamp;
  }

  @lombok.Data
  @lombok.Builder
  public static class SagaTimeoutEvent {
    private String sagaId;
    private long timeoutDuration;
    private long timestamp;
  }
}
