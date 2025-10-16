package com.payments.domain.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Failed Rule Domain Model
 * 
 * Represents a validation rule that failed:
 * - Rule identification
 * - Failure reason
 * - Failure timestamp
 * - Field that failed validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedRule {
    
    private String ruleId;
    private String ruleName;
    private String ruleType;
    private String failureReason;
    private String field;
    private Instant failedAt;
    
    /**
     * Create a failed rule with current timestamp
     */
    public static FailedRule create(String ruleId, String ruleName, String ruleType, 
                                  String failureReason, String field) {
        return FailedRule.builder()
                .ruleId(ruleId)
                .ruleName(ruleName)
                .ruleType(ruleType)
                .failureReason(failureReason)
                .field(field)
                .failedAt(Instant.now())
                .build();
    }
    
    /**
     * Create a failed rule with current timestamp and no field
     */
    public static FailedRule create(String ruleId, String ruleName, String ruleType, 
                                  String failureReason) {
        return create(ruleId, ruleName, ruleType, failureReason, null);
    }
}