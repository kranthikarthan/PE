package com.payments.samosadapter.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SAMOS Settlement Response */
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
