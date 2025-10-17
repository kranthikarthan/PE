package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * SAMOS Compliance Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosComplianceResponse {
    private String adapterId;
    private String paymentId;
    private Boolean isCompliant;
    private String complianceStatus;
    private List<String> appliedRules;
    private List<String> complianceErrors;
    private List<String> complianceWarnings;
    private Instant checkedAt;
    private String tenantId;
    private String businessUnitId;
}
