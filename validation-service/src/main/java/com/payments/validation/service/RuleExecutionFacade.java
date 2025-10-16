package com.payments.validation.service;

import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.domain.validation.FailedRule;
import com.payments.domain.validation.RiskLevel;
import com.payments.domain.validation.RuleType;
import com.payments.domain.validation.ValidationResult;
import com.payments.domain.validation.ValidationStatus;
import com.payments.validation.config.RuleEngineConfig;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Rule Execution Facade
 *
 * <p>Provides a unified interface for rule execution: - Rule engine abstraction - Parallel rule
 * execution - Rule result aggregation - Performance monitoring - Error handling and recovery
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleExecutionFacade {

  private final RuleEngineConfig.RuleEngineProperties ruleEngineProperties;
  private final BusinessRuleEngine businessRuleEngine;
  private final ComplianceRuleEngine complianceRuleEngine;
  private final FraudDetectionRuleEngine fraudDetectionRuleEngine;
  private final RiskAssessmentRuleEngine riskAssessmentRuleEngine;
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  /**
   * Execute all validation rules
   *
   * @param context Validation context
   * @param event Payment initiated event
   * @return Validation result
   */
  public ValidationResult executeRules(ValidationContext context, PaymentInitiatedEvent event) {
    log.info(
        "Executing validation rules for payment: {} with context: {}",
        event.getPaymentId().getValue(),
        context.getValidationId());

    try {
      List<CompletableFuture<RuleExecutionResult>> futures = new ArrayList<>();

      // Execute business rules
      if (ruleEngineProperties.isParallelExecution()) {
        futures.add(
            CompletableFuture.supplyAsync(
                () -> executeBusinessRules(context, event), executorService));
      } else {
        RuleExecutionResult businessResult = executeBusinessRules(context, event);
        futures.add(CompletableFuture.completedFuture(businessResult));
      }

      // Execute compliance rules
      if (ruleEngineProperties.isParallelExecution()) {
        futures.add(
            CompletableFuture.supplyAsync(
                () -> executeComplianceRules(context, event), executorService));
      } else {
        RuleExecutionResult complianceResult = executeComplianceRules(context, event);
        futures.add(CompletableFuture.completedFuture(complianceResult));
      }

      // Execute fraud detection rules
      if (ruleEngineProperties.isParallelExecution()) {
        futures.add(
            CompletableFuture.supplyAsync(
                () -> executeFraudDetectionRules(context, event), executorService));
      } else {
        RuleExecutionResult fraudResult = executeFraudDetectionRules(context, event);
        futures.add(CompletableFuture.completedFuture(fraudResult));
      }

      // Execute risk assessment rules
      if (ruleEngineProperties.isParallelExecution()) {
        futures.add(
            CompletableFuture.supplyAsync(
                () -> executeRiskAssessmentRules(context, event), executorService));
      } else {
        RuleExecutionResult riskResult = executeRiskAssessmentRules(context, event);
        futures.add(CompletableFuture.completedFuture(riskResult));
      }

      // Wait for all rule executions to complete
      CompletableFuture<Void> allFutures =
          CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

      allFutures.get(ruleEngineProperties.getMaxExecutionTime(), TimeUnit.MILLISECONDS);

      // Aggregate results
      List<RuleExecutionResult> results = futures.stream().map(CompletableFuture::join).toList();

      return aggregateResults(context, event, results);

    } catch (Exception e) {
      log.error(
          "Error executing validation rules for payment: {}", event.getPaymentId().getValue(), e);
      return createErrorResult(context, event, e);
    }
  }

  /** Execute business rules */
  private RuleExecutionResult executeBusinessRules(
      ValidationContext context, PaymentInitiatedEvent event) {
    log.debug("Executing business rules for payment: {}", event.getPaymentId().getValue());

    try {
      return businessRuleEngine.executeRules(context, event);
    } catch (Exception e) {
      log.error(
          "Error executing business rules for payment: {}", event.getPaymentId().getValue(), e);
      return RuleExecutionResult.builder()
          .ruleType(RuleType.BUSINESS)
          .success(false)
          .errorMessage(e.getMessage())
          .executionTime(0L)
          .build();
    }
  }

  /** Execute compliance rules */
  private RuleExecutionResult executeComplianceRules(
      ValidationContext context, PaymentInitiatedEvent event) {
    log.debug("Executing compliance rules for payment: {}", event.getPaymentId().getValue());

    try {
      return complianceRuleEngine.executeRules(context, event);
    } catch (Exception e) {
      log.error(
          "Error executing compliance rules for payment: {}", event.getPaymentId().getValue(), e);
      return RuleExecutionResult.builder()
          .ruleType(RuleType.COMPLIANCE)
          .success(false)
          .errorMessage(e.getMessage())
          .executionTime(0L)
          .build();
    }
  }

  /** Execute fraud detection rules */
  private RuleExecutionResult executeFraudDetectionRules(
      ValidationContext context, PaymentInitiatedEvent event) {
    log.debug("Executing fraud detection rules for payment: {}", event.getPaymentId().getValue());

    try {
      return fraudDetectionRuleEngine.executeRules(context, event);
    } catch (Exception e) {
      log.error(
          "Error executing fraud detection rules for payment: {}",
          event.getPaymentId().getValue(),
          e);
      return RuleExecutionResult.builder()
          .ruleType(RuleType.FRAUD)
          .success(false)
          .errorMessage(e.getMessage())
          .executionTime(0L)
          .build();
    }
  }

  /** Execute risk assessment rules */
  private RuleExecutionResult executeRiskAssessmentRules(
      ValidationContext context, PaymentInitiatedEvent event) {
    log.debug("Executing risk assessment rules for payment: {}", event.getPaymentId().getValue());

    try {
      return riskAssessmentRuleEngine.executeRules(context, event);
    } catch (Exception e) {
      log.error(
          "Error executing risk assessment rules for payment: {}",
          event.getPaymentId().getValue(),
          e);
      return RuleExecutionResult.builder()
          .ruleType(RuleType.RISK)
          .success(false)
          .errorMessage(e.getMessage())
          .executionTime(0L)
          .build();
    }
  }

  /** Aggregate rule execution results */
  private ValidationResult aggregateResults(
      ValidationContext context, PaymentInitiatedEvent event, List<RuleExecutionResult> results) {
    log.debug(
        "Aggregating rule execution results for payment: {}", event.getPaymentId().getValue());

    List<String> appliedRules = new ArrayList<>();
    List<FailedRule> failedRules = new ArrayList<>();
    int fraudScore = 0;
    int riskScore = 0;

    for (RuleExecutionResult result : results) {
      appliedRules.addAll(result.getAppliedRules());
      failedRules.addAll(result.getFailedRules());
      fraudScore += result.getFraudScore();
      riskScore += result.getRiskScore();
    }

    ValidationStatus status =
        failedRules.isEmpty() ? ValidationStatus.PASSED : ValidationStatus.FAILED;
    RiskLevel riskLevel = calculateRiskLevel(failedRules);

    return ValidationResult.builder()
        .validationId(context.getValidationId())
        .paymentId(context.getPaymentId())
        .tenantContext(context.getTenantContext())
        .status(status)
        .riskLevel(riskLevel)
        .fraudScore(BigDecimal.valueOf(Math.min(fraudScore, 100)))
        .riskScore(BigDecimal.valueOf(Math.min(riskScore, 100)))
        .appliedRules(appliedRules)
        .failedRules(failedRules)
        .validationMetadata(createValidationMetadata(context, event, results))
        .validatedAt(Instant.now())
        .correlationId(context.getCorrelationId())
        .createdBy("validation-service")
        .build();
  }

  /** Create error result */
  private ValidationResult createErrorResult(
      ValidationContext context, PaymentInitiatedEvent event, Exception error) {
    log.error(
        "Creating error result for payment: {} due to: {}",
        event.getPaymentId().getValue(),
        error.getMessage());

    return ValidationResult.builder()
        .validationId(context.getValidationId())
        .paymentId(context.getPaymentId())
        .tenantContext(context.getTenantContext())
        .status(ValidationStatus.FAILED)
        .riskLevel(RiskLevel.CRITICAL)
        .fraudScore(BigDecimal.valueOf(100))
        .riskScore(BigDecimal.valueOf(100))
        .appliedRules(List.of())
        .failedRules(
            List.of(
                FailedRule.builder()
                    .ruleId("SYSTEM_ERROR")
                    .ruleName("System Error")
                    .ruleType(RuleType.BUSINESS.toString())
                    .failureReason("Validation system error: " + error.getMessage())
                    .failedAt(Instant.now())
                    .build()))
        .validationMetadata(createErrorMetadata(context, event, error))
        .validatedAt(Instant.now())
        .correlationId(context.getCorrelationId())
        .createdBy("validation-service")
        .build();
  }

  /** Calculate risk level based on failed rules */
  private RiskLevel calculateRiskLevel(List<FailedRule> failedRules) {
    if (failedRules.isEmpty()) {
      return RiskLevel.LOW;
    }

    long fraudRules =
        failedRules.stream()
            .filter(rule -> RuleType.FRAUD.toString().equals(rule.getRuleType()))
            .count();

    long riskRules =
        failedRules.stream()
            .filter(rule -> RuleType.RISK.toString().equals(rule.getRuleType()))
            .count();

    if (fraudRules > 0) {
      return RiskLevel.CRITICAL;
    } else if (riskRules > 0) {
      return RiskLevel.HIGH;
    } else {
      return RiskLevel.MEDIUM;
    }
  }

  /** Create validation metadata */
  private String createValidationMetadata(
      ValidationContext context, PaymentInitiatedEvent event, List<RuleExecutionResult> results) {
    // TODO: Create proper JSON metadata
    return String.format(
        "{\"validationId\":\"%s\",\"paymentId\":\"%s\",\"ruleCount\":%d}",
        context.getValidationId(), event.getPaymentId().getValue(), results.size());
  }

  /** Create error metadata */
  private String createErrorMetadata(
      ValidationContext context, PaymentInitiatedEvent event, Exception error) {
    return String.format(
        "{\"validationId\":\"%s\",\"paymentId\":\"%s\",\"error\":\"%s\"}",
        context.getValidationId(), event.getPaymentId().getValue(), error.getMessage());
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

  /** Rule Execution Result */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class RuleExecutionResult {
    private RuleType ruleType;
    private boolean success;
    private List<String> appliedRules;
    private List<FailedRule> failedRules;
    private int fraudScore;
    private int riskScore;
    private long executionTime;
    private String errorMessage;
  }
}
