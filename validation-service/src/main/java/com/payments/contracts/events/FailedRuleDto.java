package com.payments.contracts.events;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

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






