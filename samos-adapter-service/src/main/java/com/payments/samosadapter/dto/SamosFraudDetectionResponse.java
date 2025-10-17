package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * SAMOS Fraud Detection Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosFraudDetectionResponse {
    private String adapterId;
    private String paymentId;
    private Boolean isFraudDetected;
    private Double fraudScore;
    private String fraudStatus;
    private List<String> appliedRules;
    private List<String> fraudAlerts;
    private List<String> fraudWarnings;
    private Instant detectedAt;
    private String tenantId;
    private String businessUnitId;
}
