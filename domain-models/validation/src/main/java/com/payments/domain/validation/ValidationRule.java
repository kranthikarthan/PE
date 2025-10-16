package com.payments.domain.validation;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/** Validation Rule (domain-only helper, not persisted by this aggregate) */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class ValidationRule {

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






