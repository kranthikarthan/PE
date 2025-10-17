package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * SAMOS Payment Status Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosPaymentStatusResponse {
    private String paymentId;
    private String transactionId;
    private String status;
    private String statusReason;
    private Instant lastUpdatedAt;
    private String responseCode;
    private String responseMessage;
    private String correlationId;
    private Long responseTimestamp;
    private String requestId;
}
