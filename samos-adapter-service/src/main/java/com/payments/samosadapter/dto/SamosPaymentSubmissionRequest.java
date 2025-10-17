package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * SAMOS Payment Submission Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosPaymentSubmissionRequest {
    private String paymentId;
    private String requestId;
    private String accountNumber;
    private String beneficiaryAccount;
    private BigDecimal amount;
    private String currency;
    private String paymentReference;
    private String paymentType;
    private Instant requestedAt;
    private String tenantId;
    private String businessUnitId;
}
