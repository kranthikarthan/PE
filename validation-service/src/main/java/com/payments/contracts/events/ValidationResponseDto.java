package com.payments.contracts.events;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ValidationResponseDto {
    private String validationId;
    private String status;
    private BigDecimal fraudScore;
    private BigDecimal riskScore;
    private String riskLevel;
    private List<String> appliedRules;
    private List<FailedRuleDto> failedRules;
    private String reason;
    private Instant validatedAt;
    private String validatorService;
}






