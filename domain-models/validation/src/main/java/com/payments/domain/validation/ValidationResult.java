package com.payments.domain.validation;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Validation Result Domain Model
 *
 * <p>Represents the result of payment validation: - Validation status and outcome - Risk and fraud
 * assessment - Applied and failed rules - Validation metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

  private String validationId;
  private PaymentId paymentId;
  private TenantContext tenantContext;
  private ValidationStatus status;
  private RiskLevel riskLevel;
  private BigDecimal fraudScore;
  private BigDecimal riskScore;
  private List<String> appliedRules;
  private List<FailedRule> failedRules;
  private String validationMetadata;
  private Instant validatedAt;
  private String correlationId;
  private String createdBy;
  private String reason;

  /** Check if validation passed */
  public boolean isPassed() {
    return ValidationStatus.PASSED.equals(status);
  }

  /** Check if validation failed */
  public boolean isFailed() {
    return ValidationStatus.FAILED.equals(status);
  }

  /** Get total number of applied rules */
  public int getAppliedRuleCount() {
    return appliedRules != null ? appliedRules.size() : 0;
  }

  /** Get total number of failed rules */
  public int getFailedRuleCount() {
    return failedRules != null ? failedRules.size() : 0;
  }

  /** Check if validation has high risk */
  public boolean isHighRisk() {
    return RiskLevel.HIGH.equals(riskLevel) || RiskLevel.CRITICAL.equals(riskLevel);
  }

  /** Check if validation has fraud indicators */
  public boolean hasFraudIndicators() {
    return fraudScore != null && fraudScore.compareTo(BigDecimal.valueOf(70)) > 0;
  }

  // Convenience factory used by tests and domain flows
  public static ValidationResult create(
      ValidationId validationId, TenantContext tenantContext, PaymentId paymentId) {
    return ValidationResult.builder()
        .validationId(validationId != null ? validationId.getValue() : null)
        .tenantContext(tenantContext)
        .paymentId(paymentId)
        .status(null)
        .build();
  }

  // Complete validation outcome and stamp metadata
  public void completeValidation(boolean passed, String actor) {
    this.status = passed ? ValidationStatus.PASSED : ValidationStatus.FAILED;
    this.validatedAt = Instant.now();
    this.createdBy = actor;
    if (!passed && (this.reason == null || this.reason.isBlank())) {
      this.reason = "Validation failed";
    }
  }
}
