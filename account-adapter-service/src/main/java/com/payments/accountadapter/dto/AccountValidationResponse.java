package com.payments.accountadapter.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account Validation Response DTO
 *
 * <p>Response containing account validation results: - Validation status - Account details -
 * Validation results - Response metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountValidationResponse {

  private String accountNumber;
  private String accountHolderName;
  private String accountType;
  private String accountStatus;
  private boolean isValid;
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
