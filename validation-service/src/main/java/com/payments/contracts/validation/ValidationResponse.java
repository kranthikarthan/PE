package com.payments.contracts.validation;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ValidationResponse {
    private PaymentId paymentId;
    private ValidationStatus status;
    private TenantContext tenantContext;
    private Instant validatedAt;
    private RiskLevel riskLevel;
    private Integer fraudScore;
    private List<FailedRule> failedRules;
}






