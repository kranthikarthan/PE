package com.payments.bankservafricaadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * BankservAfrica Risk Assessment Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaRiskAssessmentResponse {
    private String adapterId;
    private String paymentId;
    private Boolean isHighRisk;
    private Double riskScore;
    private String riskLevel;
    private List<String> appliedRules;
    private List<String> riskAlerts;
    private List<String> riskWarnings;
    private Instant assessedAt;
    private String tenantId;
    private String businessUnitId;
}
