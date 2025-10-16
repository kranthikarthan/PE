package com.payments.domain.validation;

import com.payments.domain.shared.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.*;

/**
 * Validation Result Aggregate Root
 *
 * <p>Represents the result of payment validation including fraud assessment, limit checks, and
 * business rule validation.
 */
@Entity
@Table(name = "validation_results")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ValidationResult {

  @EmbeddedId
  @AttributeOverride(name = "value", column = @Column(name = "validation_id"))
  private ValidationId id;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
    @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id"))
  })
  private TenantContext tenantContext;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "payment_id"))
  private PaymentId paymentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "validation_status")
  private ValidationStatus status;

  @Column(name = "fraud_score")
  private BigDecimal fraudScore;

  @Enumerated(EnumType.STRING)
  @Column(name = "risk_level")
  private RiskLevel riskLevel;

  @Transient private List<ValidationRule> appliedRules = new ArrayList<>();

  @Transient private List<FailedRule> failedRules = new ArrayList<>();

  @Column(name = "failed_rules", columnDefinition = "jsonb")
  private String failedRulesJson;

  @Column(name = "validated_at")
  private Instant validatedAt;

  @Column(name = "validator_service")
  private String validatorService;

  @Transient private List<DomainEvent> domainEvents = new ArrayList<>();

  // ─────────────────────────────────────────────────────────
  // FACTORY METHOD
  // ─────────────────────────────────────────────────────────

  public static ValidationResult create(
      ValidationId id, TenantContext tenantContext, PaymentId paymentId) {
    ValidationResult result = new ValidationResult();
    result.id = id;
    result.tenantContext = tenantContext;
    result.paymentId = paymentId;
    result.status = ValidationStatus.PENDING;
    result.validatedAt = Instant.now();

    return result;
  }

  // ─────────────────────────────────────────────────────────
  // BUSINESS METHODS
  // ─────────────────────────────────────────────────────────

  /** Add a validation rule that was applied (domain only) */
  public void addAppliedRule(ValidationRule rule) {
    this.appliedRules.add(rule);
  }

  /** Add a failed validation rule (domain only) */
  public void addFailedRule(FailedRule failedRule) {
    this.failedRules.add(failedRule);
  }

  /** Set fraud assessment results */
  public void setFraudAssessment(BigDecimal fraudScore, RiskLevel riskLevel) {
    this.fraudScore = fraudScore;
    this.riskLevel = riskLevel;
  }

  /** Complete validation with result */
  public void completeValidation(boolean isValid, String validatorService) {
    this.status = isValid ? ValidationStatus.VALID : ValidationStatus.INVALID;
    this.validatorService = validatorService;

    if (isValid) {
      registerEvent(new PaymentValidatedEvent(this.paymentId, this.tenantContext, this));
    } else {
      registerEvent(
          new ValidationFailedEvent(this.paymentId, this.tenantContext, this.failedRules));
    }
  }

  // ─────────────────────────────────────────────────────────
  // QUERY METHODS
  // ─────────────────────────────────────────────────────────

  public boolean isValid() {
    return this.status == ValidationStatus.VALID;
  }

  public boolean isInvalid() {
    return this.status == ValidationStatus.INVALID;
  }

  public boolean isPassed() {
    return this.status == ValidationStatus.PASSED;
  }

  public boolean isFailed() {
    return this.status == ValidationStatus.FAILED;
  }

  public boolean hasFailedRules() {
    return !this.failedRules.isEmpty();
  }

  public boolean isHighRisk() {
    return this.riskLevel == RiskLevel.HIGH || this.riskLevel == RiskLevel.CRITICAL;
  }

  public ValidationId getId() {
    return id;
  }

  public TenantContext getTenantContext() {
    return tenantContext;
  }

  public PaymentId getPaymentId() {
    return paymentId;
  }

  public ValidationStatus getStatus() {
    return status;
  }

  public BigDecimal getFraudScore() {
    return fraudScore;
  }

  public RiskLevel getRiskLevel() {
    return riskLevel;
  }

  public List<FailedRule> getFailedRules() {
    return Collections.unmodifiableList(failedRules);
  }

  public String getReason() {
    return hasFailedRules() ? failedRules.get(0).getReason() : "Validation passed";
  }

  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  public void clearDomainEvents() {
    this.domainEvents.clear();
  }

  // Additional getters for validation service compatibility
  public String getValidationId() {
    return id != null ? id.getValue() : null;
  }

  public BigDecimal getRiskScore() {
    return fraudScore;
  }

  public List<ValidationRule> getAppliedRules() {
    return Collections.unmodifiableList(appliedRules);
  }

  public String getValidationMetadata() {
    return "Validation metadata"; // Placeholder
  }

  public Instant getValidatedAt() {
    return validatedAt;
  }

  public String getCorrelationId() {
    return "correlation-" + id.getValue(); // Placeholder
  }

  public String getCreatedBy() {
    return "validation-service"; // Placeholder
  }

  // Builder method for validation service compatibility
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private ValidationResult result = new ValidationResult();

    public Builder validationId(String validationId) {
      result.id = new ValidationId(validationId);
      return this;
    }

    public Builder paymentId(PaymentId paymentId) {
      result.paymentId = paymentId;
      return this;
    }

    public Builder tenantContext(TenantContext tenantContext) {
      result.tenantContext = tenantContext;
      return this;
    }

    public Builder status(ValidationStatus status) {
      result.status = status;
      return this;
    }

    public Builder fraudScore(BigDecimal fraudScore) {
      result.fraudScore = fraudScore;
      return this;
    }

    public Builder riskScore(BigDecimal riskScore) {
      result.fraudScore = riskScore; // Using fraudScore field for riskScore
      return this;
    }

    public Builder riskLevel(RiskLevel riskLevel) {
      result.riskLevel = riskLevel;
      return this;
    }

    public Builder validatedAt(Instant validatedAt) {
      result.validatedAt = validatedAt;
      return this;
    }

    public Builder validatorService(String validatorService) {
      result.validatorService = validatorService;
      return this;
    }

    public Builder appliedRules(List<String> appliedRules) {
      // Convert String list to ValidationRule list
      result.appliedRules = appliedRules.stream()
          .map(ruleName -> new ValidationRule(ruleName, "BUSINESS", "Applied rule"))
          .collect(java.util.stream.Collectors.toList());
      return this;
    }

    public ValidationResult build() {
      return result;
    }
  }

  // ─────────────────────────────────────────────────────────
  // PRIVATE HELPERS
  // ─────────────────────────────────────────────────────────

  private void registerEvent(DomainEvent event) {
    this.domainEvents.add(event);
  }
}
