package com.payments.samosadapter.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SAMOS ISO 20022 Validation Response */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosIso20022ValidationResponse {
  private String messageId;
  private Boolean isValid;
  private String validationStatus;
  private List<String> validationErrors;
  private List<String> validationWarnings;
  private String responseCode;
  private String responseMessage;
  private String correlationId;
  private Long responseTimestamp;
  private String requestId;
  private Instant validatedAt;
}
