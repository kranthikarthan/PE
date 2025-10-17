package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SAMOS Payment Status Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosPaymentStatusRequest {
    private String paymentId;
    private String requestId;
    private String transactionId;
    private String tenantId;
    private String businessUnitId;
}
