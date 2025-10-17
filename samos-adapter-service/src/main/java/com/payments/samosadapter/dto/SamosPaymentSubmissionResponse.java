package com.payments.samosadapter.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SAMOS Payment Submission Response */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosPaymentSubmissionResponse {
  private String paymentId;
  private String transactionId;
  private String status;
  private String statusReason;
  private Instant submittedAt;
  private String responseCode;
  private String responseMessage;
  private String correlationId;
  private Long responseTimestamp;
  private String requestId;
}
