package com.payments.contracts.validation;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

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






