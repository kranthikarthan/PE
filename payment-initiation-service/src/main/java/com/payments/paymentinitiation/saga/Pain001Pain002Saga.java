package com.payments.paymentinitiation.saga;

import com.payments.domain.shared.TenantContext;
import com.payments.iso20022.canonical.CanonicalPaymentModel;
import com.payments.iso20022.pain001.Pain001MessageParser;
import com.payments.iso20022.pain002.Pain002MessageBuilder;
import com.payments.iso20022.service.Iso20022MarshallerService;
import com.payments.iso20022.util.UetrGenerator;
import com.payments.iso20022.validation.Iso20022Validator;
import com.payments.iso20022.validation.ValidationResult;
import com.payments.paymentinitiation.service.Pain001Pain002DatabaseService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga Orchestrator for ISO 20022 pain.001/pain.002 flows
 *
 * <p>Implements the Saga pattern for distributed payment processing: 1. pain.001 → Canonical Model
 * → Payment Processing → pain.002 2. Each step is a separate transaction with compensation 3.
 * Event-driven with proper error handling and rollback
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Pain001Pain002Saga {

  private final Pain001MessageParser pain001MessageParser;
  private final Pain002MessageBuilder pain002MessageBuilder;
  private final Pain001Pain002DatabaseService databaseService;
  private final Iso20022MarshallerService marshallerService;
  private final Iso20022Validator validator;
  private final SagaEventPublisher sagaEventPublisher;

  /**
   * Start the pain.001 processing saga
   *
   * @param pain001Xml pain.001 XML message
   * @param tenantContext Tenant context
   * @param correlationId Correlation ID for tracing
   * @return Saga ID for tracking
   */
  @Transactional
  public String startPain001ProcessingSaga(
      String pain001Xml, TenantContext tenantContext, String correlationId) {

    String sagaId = UetrGenerator.generateWithContext("SAGA");
    log.info("Starting pain.001 processing saga: {} for correlation: {}", sagaId, correlationId);

    try {
      // Step 1: Parse and validate pain.001 (Saga Step 1)
      CanonicalPaymentModel canonicalPayment = parsePain001Step(pain001Xml, tenantContext, sagaId);

      // Step 2: Persist pain.001 data (Saga Step 2)
      persistPain001Step(canonicalPayment, sagaId);

      // Step 3: Initiate payment processing (Saga Step 3)
      initiatePaymentProcessingStep(canonicalPayment, sagaId);

      // Publish saga started event
      sagaEventPublisher.publishSagaStarted(sagaId, correlationId, canonicalPayment);

      log.info("Pain.001 processing saga started successfully: {}", sagaId);
      return sagaId;

    } catch (Exception e) {
      log.error("Failed to start pain.001 processing saga: {}", sagaId, e);
      // Compensate for any completed steps
      compensateSagaSteps(sagaId, e);
      throw new SagaExecutionException("Failed to start pain.001 processing saga", e);
    }
  }

  /**
   * Complete the saga with pain.002 generation
   *
   * @param sagaId Saga ID
   * @param paymentStatus Final payment status
   * @param statusReason Status reason
   */
  @Transactional
  public void completeSagaWithPain002(String sagaId, String paymentStatus, String statusReason) {
    log.info("Completing saga with pain.002: {}", sagaId);

    try {
      // Step 4: Generate pain.002 response (Saga Step 4)
      generatePain002Step(sagaId, paymentStatus, statusReason);

      // Step 5: Persist pain.002 data (Saga Step 5)
      persistPain002Step(sagaId, paymentStatus, statusReason);

      // Step 6: Complete saga (Saga Step 6)
      completeSagaStep(sagaId);

      // Publish saga completed event
      sagaEventPublisher.publishSagaCompleted(sagaId, paymentStatus);

      log.info("Saga completed successfully with pain.002: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to complete saga with pain.002: {}", sagaId, e);
      // Compensate for any completed steps
      compensateSagaSteps(sagaId, e);
      throw new SagaExecutionException("Failed to complete saga with pain.002", e);
    }
  }

  /**
   * Compensate saga steps in case of failure
   *
   * @param sagaId Saga ID
   * @param error Error that caused compensation
   */
  @Transactional
  public void compensateSagaSteps(String sagaId, Exception error) {
    log.warn("Compensating saga steps for saga: {} due to error: {}", sagaId, error.getMessage());

    try {
      // Get saga state
      SagaState sagaState = databaseService.getSagaState(sagaId);

      // Compensate steps in reverse order
      if (sagaState.isPaymentProcessingInitiated()) {
        compensatePaymentProcessingStep(sagaId);
      }

      if (sagaState.isPain001Persisted()) {
        compensatePain001PersistenceStep(sagaId);
      }

      if (sagaState.isPain001Parsed()) {
        compensatePain001ParsingStep(sagaId);
      }

      // Mark saga as compensated
      databaseService.markSagaCompensated(sagaId, error.getMessage());

      // Publish saga compensated event
      sagaEventPublisher.publishSagaCompensated(sagaId, error.getMessage());

      log.info("Saga compensation completed: {}", sagaId);

    } catch (Exception compensationError) {
      log.error("Failed to compensate saga: {}", sagaId, compensationError);
      // Mark saga as compensation failed
      databaseService.markSagaCompensationFailed(sagaId, compensationError.getMessage());
    }
  }

  // ==================== SAGA STEPS ====================

  /** Saga Step 1: Parse pain.001 message */
  private CanonicalPaymentModel parsePain001Step(
      String pain001Xml, TenantContext tenantContext, String sagaId) {
    log.debug("Saga Step 1: Parsing pain.001 for saga: {}", sagaId);

    try {
      // Validate XML against XSD schema
      ValidationResult validationResult =
          validator.validate(pain001Xml, com.payments.iso20022.config.Iso20022MessageType.PAIN_001);

      if (!validationResult.isValid()) {
        throw new IllegalArgumentException(
            "Invalid pain.001 XML: " + validationResult.getBusinessFriendlyMessage());
      }

      // Parse XML to JAXB object
      Object pain001Document =
          marshallerService.unmarshalWithValidation(
              pain001Xml, com.payments.iso20022.config.Iso20022MessageType.PAIN_001);

      // Convert to canonical payment model
      CanonicalPaymentModel canonicalPayment =
          pain001MessageParser.parseToCanonicalModel(pain001Document, tenantContext);

      // Mark saga step as completed
      databaseService.markSagaStepCompleted(sagaId, "PAIN001_PARSED", canonicalPayment);

      return canonicalPayment;

    } catch (Exception e) {
      log.error("Saga Step 1 failed: pain.001 parsing for saga: {}", sagaId, e);
      databaseService.markSagaStepFailed(sagaId, "PAIN001_PARSED", e.getMessage());
      throw e;
    }
  }

  /** Saga Step 2: Persist pain.001 data */
  private void persistPain001Step(CanonicalPaymentModel canonicalPayment, String sagaId) {
    log.debug("Saga Step 2: Persisting pain.001 data for saga: {}", sagaId);

    try {
      // Persist pain.001 message and related data
      databaseService.savePain001Message(canonicalPayment, sagaId);

      // Mark saga step as completed
      databaseService.markSagaStepCompleted(sagaId, "PAIN001_PERSISTED", null);

    } catch (Exception e) {
      log.error("Saga Step 2 failed: pain.001 persistence for saga: {}", sagaId, e);
      databaseService.markSagaStepFailed(sagaId, "PAIN001_PERSISTED", e.getMessage());
      throw e;
    }
  }

  /** Saga Step 3: Initiate payment processing */
  private void initiatePaymentProcessingStep(
      CanonicalPaymentModel canonicalPayment, String sagaId) {
    log.debug("Saga Step 3: Initiating payment processing for saga: {}", sagaId);

    try {
      // Publish payment processing event (async)
      sagaEventPublisher.publishPaymentProcessingInitiated(sagaId, canonicalPayment);

      // Mark saga step as completed
      databaseService.markSagaStepCompleted(sagaId, "PAYMENT_PROCESSING_INITIATED", null);

    } catch (Exception e) {
      log.error("Saga Step 3 failed: payment processing initiation for saga: {}", sagaId, e);
      databaseService.markSagaStepFailed(sagaId, "PAYMENT_PROCESSING_INITIATED", e.getMessage());
      throw e;
    }
  }

  /** Saga Step 4: Generate pain.002 response */
  private void generatePain002Step(String sagaId, String paymentStatus, String statusReason) {
    log.debug("Saga Step 4: Generating pain.002 for saga: {}", sagaId);

    try {
      // Get canonical payment model
      CanonicalPaymentModel canonicalPayment = databaseService.getCanonicalPaymentBySagaId(sagaId);

      // Update payment status
      canonicalPayment.setStatus(mapStringToPaymentStatus(paymentStatus));
      canonicalPayment.setStatusReason(statusReason);
      canonicalPayment.setUpdatedAt(LocalDateTime.now());

      // Generate pain.002 XML
      String pain002Xml = pain002MessageBuilder.build(canonicalPayment);

      // Store pain.002 response
      databaseService.savePain002Response(sagaId, pain002Xml, paymentStatus);

      // Mark saga step as completed
      databaseService.markSagaStepCompleted(sagaId, "PAIN002_GENERATED", pain002Xml);

    } catch (Exception e) {
      log.error("Saga Step 4 failed: pain.002 generation for saga: {}", sagaId, e);
      databaseService.markSagaStepFailed(sagaId, "PAIN002_GENERATED", e.getMessage());
      throw e;
    }
  }

  /** Saga Step 5: Persist pain.002 data */
  private void persistPain002Step(String sagaId, String paymentStatus, String statusReason) {
    log.debug("Saga Step 5: Persisting pain.002 data for saga: {}", sagaId);

    try {
      // Persist pain.002 status report and correlation
      databaseService.savePain002StatusReport(sagaId, paymentStatus, statusReason);

      // Mark saga step as completed
      databaseService.markSagaStepCompleted(sagaId, "PAIN002_PERSISTED", null);

    } catch (Exception e) {
      log.error("Saga Step 5 failed: pain.002 persistence for saga: {}", sagaId, e);
      databaseService.markSagaStepFailed(sagaId, "PAIN002_PERSISTED", e.getMessage());
      throw e;
    }
  }

  /** Saga Step 6: Complete saga */
  private void completeSagaStep(String sagaId) {
    log.debug("Saga Step 6: Completing saga: {}", sagaId);

    try {
      // Mark saga as completed
      databaseService.markSagaCompleted(sagaId);

      // Mark saga step as completed
      databaseService.markSagaStepCompleted(sagaId, "SAGA_COMPLETED", null);

    } catch (Exception e) {
      log.error("Saga Step 6 failed: saga completion for saga: {}", sagaId, e);
      databaseService.markSagaStepFailed(sagaId, "SAGA_COMPLETED", e.getMessage());
      throw e;
    }
  }

  // ==================== COMPENSATION STEPS ====================

  /** Compensate Step 3: Payment processing initiation */
  private void compensatePaymentProcessingStep(String sagaId) {
    log.debug("Compensating payment processing initiation for saga: {}", sagaId);

    try {
      // Cancel payment processing (if possible)
      sagaEventPublisher.publishPaymentProcessingCancelled(sagaId);

      // Mark compensation as completed
      databaseService.markSagaStepCompensated(sagaId, "PAYMENT_PROCESSING_INITIATED");

    } catch (Exception e) {
      log.error("Failed to compensate payment processing initiation for saga: {}", sagaId, e);
      databaseService.markSagaStepCompensationFailed(
          sagaId, "PAYMENT_PROCESSING_INITIATED", e.getMessage());
    }
  }

  /** Compensate Step 2: Pain.001 persistence */
  private void compensatePain001PersistenceStep(String sagaId) {
    log.debug("Compensating pain.001 persistence for saga: {}", sagaId);

    try {
      // Delete pain.001 data
      databaseService.deletePain001Data(sagaId);

      // Mark compensation as completed
      databaseService.markSagaStepCompensated(sagaId, "PAIN001_PERSISTED");

    } catch (Exception e) {
      log.error("Failed to compensate pain.001 persistence for saga: {}", sagaId, e);
      databaseService.markSagaStepCompensationFailed(sagaId, "PAIN001_PERSISTED", e.getMessage());
    }
  }

  /** Compensate Step 1: Pain.001 parsing */
  private void compensatePain001ParsingStep(String sagaId) {
    log.debug("Compensating pain.001 parsing for saga: {}", sagaId);

    try {
      // No compensation needed for parsing (stateless operation)
      // Mark compensation as completed
      databaseService.markSagaStepCompensated(sagaId, "PAIN001_PARSED");

    } catch (Exception e) {
      log.error("Failed to compensate pain.001 parsing for saga: {}", sagaId, e);
      databaseService.markSagaStepCompensationFailed(sagaId, "PAIN001_PARSED", e.getMessage());
    }
  }

  // ==================== UTILITY METHODS ====================

  /** Map string status to PaymentStatus enum */
  private CanonicalPaymentModel.PaymentStatus mapStringToPaymentStatus(String status) {
    switch (status.toUpperCase()) {
      case "ACCEPTED":
        return CanonicalPaymentModel.PaymentStatus.ACCEPTED;
      case "REJECTED":
        return CanonicalPaymentModel.PaymentStatus.REJECTED;
      case "PENDING":
        return CanonicalPaymentModel.PaymentStatus.PENDING;
      case "PROCESSING":
        return CanonicalPaymentModel.PaymentStatus.PROCESSING;
      case "COMPLETED":
        return CanonicalPaymentModel.PaymentStatus.COMPLETED;
      case "FAILED":
        return CanonicalPaymentModel.PaymentStatus.FAILED;
      default:
        return CanonicalPaymentModel.PaymentStatus.PENDING;
    }
  }

  /** Custom exception for saga execution errors */
  public static class SagaExecutionException extends RuntimeException {
    public SagaExecutionException(String message, Throwable cause) {
      super(message, cause);
    }

    public SagaExecutionException(String message) {
      super(message);
    }
  }
}
