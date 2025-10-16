package com.payments.domain.validation;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/** Failed Rule (domain-only helper, not persisted by this aggregate) */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class FailedRule {

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

  public String getReason() {
    return reason;
  }

  // Additional getters for validation service compatibility
  public String getRuleId() {
    return id != null ? id.getValue() : null;
  }

  public String getFailureReason() {
    return reason;
  }

  public String getRuleMetadata() {
    return "Rule metadata"; // Placeholder
  }

  // Builder method for validation service compatibility
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private FailedRule failedRule = new FailedRule();

    public Builder ruleId(String ruleId) {
      failedRule.id = new FailedRuleId(ruleId);
      return this;
    }

    public Builder ruleName(String ruleName) {
      failedRule.ruleName = ruleName;
      return this;
    }

    public Builder ruleType(String ruleType) {
      failedRule.ruleType = ruleType;
      return this;
    }

    public Builder reason(String reason) {
      failedRule.reason = reason;
      return this;
    }

    public Builder failureReason(String failureReason) {
      failedRule.reason = failureReason;
      return this;
    }

    public Builder field(String field) {
      failedRule.field = field;
      return this;
    }

    public Builder failedAt(Instant failedAt) {
      failedRule.failedAt = failedAt;
      return this;
    }

    public FailedRule build() {
      return failedRule;
    }
  }
}
