package com.payments.contracts.validation;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FailedRule {
  private String ruleId;
  private String ruleName;
  private RuleType ruleType;
  private String failureReason;
  private String field;
  private Instant failedAt;
}
