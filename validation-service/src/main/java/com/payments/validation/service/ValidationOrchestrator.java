package com.payments.validation.service;

import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.contracts.events.PaymentValidatedEvent;
import com.payments.contracts.events.ValidationFailedEvent;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.validation.ValidationResult;
import com.payments.validation.producer.ValidationEventProducer;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Validation Orchestrator
 *
 * <p>Orchestrates the payment validation process: - Coordinates validation steps - Manages
 * validation state - Publishes validation results - Handles validation failures
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationOrchestrator {

  private final ValidationEventProducer eventProducer;
  private final RuleExecutionFacade ruleExecutionFacade;
  private final ValidationResultService validationResultService;

  /**
   * Validate payment
   *
   * @param event Payment initiated event
   * @param correlationId Correlation ID for tracing
   * @param tenantId Tenant ID
   * @param businessUnitId Business unit ID
   */
  public void validatePayment(
      PaymentInitiatedEvent event, String correlationId, String tenantId, String businessUnitId) {

    log.info(
        "Starting validation for payment: {}, tenant: {}, correlation: {}",
        event.getPaymentId().getValue(),
        tenantId,
        correlationId);

    try {
      // Create validation context
      RuleExecutionFacade.ValidationContext context =
          RuleExecutionFacade.ValidationContext.builder()
              .paymentId(new PaymentId(event.getPaymentId().getValue()))
              .tenantContext(
                  TenantContext.builder().tenantId(tenantId).businessUnitId(businessUnitId).build())
              .correlationId(correlationId)
              .validationId(UUID.randomUUID().toString())
              .startedAt(Instant.now())
              .build();

      // Execute validation rules
      ValidationResult result = ruleExecutionFacade.executeRules(context, event);

      // Save validation result
      validationResultService.saveValidationResult(result);

      // Publish validation result event
      if (result.isPassed()) {
        publishValidationSuccess(result, event, correlationId, tenantId, businessUnitId);
      } else {
        publishValidationFailure(result, event, correlationId, tenantId, businessUnitId);
      }

      log.info(
          "Completed validation for payment: {} with result: {}",
          event.getPaymentId().getValue(),
          result.getStatus());

    } catch (Exception e) {
      log.error("Validation failed for payment: {}", event.getPaymentId().getValue(), e);
      publishValidationError(event, correlationId, tenantId, businessUnitId, e);
    }
  }

  /** Publish successful validation result */
  private void publishValidationSuccess(
      ValidationResult result,
      PaymentInitiatedEvent event,
      String correlationId,
      String tenantId,
      String businessUnitId) {

    PaymentValidatedEvent validatedEvent =
        PaymentValidatedEvent.builder()
            .eventId(UUID.randomUUID())
            .eventType("PaymentValidated")
            .timestamp(Instant.now())
            .correlationId(correlationId)
            .source("validation-service")
            .version("1.0.0")
            .tenantId(tenantId)
            .businessUnitId(businessUnitId)
            .paymentId(event.getPaymentId())
            .tenantContext(event.getTenantContext())
            .riskLevel(result.getRiskLevel() != null ? result.getRiskLevel().name() : null)
            .fraudScore(result.getFraudScore() != null ? result.getFraudScore().intValue() : null)
            .build();

    eventProducer.publishPaymentValidated(validatedEvent, correlationId, tenantId, businessUnitId);
  }

  /** Publish failed validation result */
  private void publishValidationFailure(
      ValidationResult result,
      PaymentInitiatedEvent event,
      String correlationId,
      String tenantId,
      String businessUnitId) {

    ValidationFailedEvent failedEvent =
        ValidationFailedEvent.builder()
            .eventId(UUID.randomUUID())
            .eventType("ValidationFailed")
            .timestamp(Instant.now())
            .correlationId(correlationId)
            .source("validation-service")
            .version("1.0.0")
            .tenantId(tenantId)
            .businessUnitId(businessUnitId)
            .paymentId(event.getPaymentId())
            .tenantContext(event.getTenantContext())
            .riskLevel(result.getRiskLevel() != null ? result.getRiskLevel().name() : null)
            .fraudScore(result.getFraudScore() != null ? result.getFraudScore().intValue() : null)
            .failedRules(createFailedRulesDto(result))
            .build();

    eventProducer.publishValidationFailed(failedEvent, correlationId, tenantId, businessUnitId);
  }

  /** Publish validation error */
  private void publishValidationError(
      PaymentInitiatedEvent event,
      String correlationId,
      String tenantId,
      String businessUnitId,
      Exception error) {

    log.error(
        "Publishing validation error for payment: {}", event.getPaymentId().getValue(), error);

    // TODO: Create error event and publish
    // For now, just log the error
  }

  /** Create validation response DTO */
  private com.payments.contracts.events.ValidationResponseDto createValidationResponseDto(
      ValidationResult result) {
    return com.payments.contracts.events.ValidationResponseDto.builder()
        .validationId(result.getValidationId())
        .status(result.getStatus().name())
        .fraudScore(result.getFraudScore())
        .riskScore(result.getRiskScore())
        .riskLevel(result.getRiskLevel() != null ? result.getRiskLevel().name() : null)
        .appliedRules(result.getAppliedRules())
        .failedRules(createFailedRulesDto(result))
        .reason(result.getReason())
        .validatedAt(result.getValidatedAt())
        .validatorService(result.getCreatedBy())
        .build();
  }

  /** Create failed rules DTO */
  private java.util.List<com.payments.contracts.events.FailedRuleDto> createFailedRulesDto(
      ValidationResult result) {
    return result.getFailedRules().stream()
        .map(
            failedRule ->
                com.payments.contracts.events.FailedRuleDto.builder()
                    .ruleId(failedRule.getRuleId())
                    .ruleName(failedRule.getRuleName())
                    .ruleType(failedRule.getRuleType())
                    .failureReason(failedRule.getFailureReason())
                    .field(failedRule.getField())
                    .failedAt(failedRule.getFailedAt())
                    .build())
        .toList();
  }

  /** Validation Context */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class ValidationContext {
    private com.payments.domain.shared.PaymentId paymentId;
    private com.payments.domain.shared.TenantContext tenantContext;
    private String correlationId;
    private String validationId;
    private Instant startedAt;
  }
}
