package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * SAMOS Settlement Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosSettlementResponse {
    private String settlementId;
    private String status;
    private String statusReason;
    private Instant processedAt;
    private String responseCode;
    private String responseMessage;
    private String correlationId;
    private Long responseTimestamp;
    private String requestId;
}
