package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * SAMOS Compliance Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosComplianceRequest {
    private String paymentId;
    private String requestId;
    private BigDecimal amount;
    private String currency;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private String beneficiaryCountry;
    private String paymentReference;
    private String paymentType;
    private String tenantId;
    private String businessUnitId;
}
