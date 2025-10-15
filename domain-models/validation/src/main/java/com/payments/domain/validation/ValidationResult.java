package com.payments.domain.validation;

import com.payments.domain.shared.*;
import jakarta.persistence.*;
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
  private Double fraudScore;

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
  public void setFraudAssessment(Double fraudScore, RiskLevel riskLevel) {
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

  public Double getFraudScore() {
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

  // ─────────────────────────────────────────────────────────
  // PRIVATE HELPERS
  // ─────────────────────────────────────────────────────────

  private void registerEvent(DomainEvent event) {
    this.domainEvents.add(event);
  }
}

/** Validation Rule (domain-only helper, not persisted by this aggregate) */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class ValidationRule {

  @EmbeddedId private ValidationRuleId id;

  private String ruleName;

  @Enumerated(EnumType.STRING)
  private RuleType ruleType;

  private String ruleDescription;

  private String ruleCondition;

  private Integer priority;

  private Boolean active;

  private Instant createdAt;

  public ValidationRule(
      ValidationRuleId id,
      String ruleName,
      RuleType ruleType,
      String ruleDescription,
      String ruleCondition,
      Integer priority,
      Boolean active) {
    this.id = id;
    this.ruleName = ruleName;
    this.ruleType = ruleType;
    this.ruleDescription = ruleDescription;
    this.ruleCondition = ruleCondition;
    this.priority = priority;
    this.active = active;
    this.createdAt = Instant.now();
  }
}

/** Failed Rule (domain-only helper, not persisted by this aggregate) */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class FailedRule {

  @EmbeddedId private FailedRuleId id;

  private String ruleName;

  private String ruleType;

  private String reason;

  private String field;

  private Instant failedAt;

  public FailedRule(
      FailedRuleId id, String ruleName, String ruleType, String reason, String field) {
    this.id = id;
    this.ruleName = ruleName;
    this.ruleType = ruleType;
    this.reason = reason;
    this.field = field;
    this.failedAt = Instant.now();
  }
}
