package com.payments.contracts.events;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FailedRuleDto {
  private String ruleId;
  private String ruleName;
  private String ruleType;
  private String failureReason;
  private String field;
  private Instant failedAt;
}
