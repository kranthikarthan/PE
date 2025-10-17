package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * SAMOS Settlement Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosSettlementRequest {
    private String settlementId;
    private String requestId;
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private Instant settlementDate;
    private String tenantId;
    private String businessUnitId;
}
