package com.payments.contracts.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Account Validation Response DTO
 *
 * <p>Response for account validation: - Validation result - Account status - Account details -
 * Validation metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account validation response")
public class AccountValidationResponse {

  @Schema(description = "Validation result", example = "VALID")
  private String validationResult;

  @Schema(description = "Account status", example = "ACTIVE")
  private String accountStatus;

  @Schema(description = "Account holder name", example = "John Doe")
  private String accountHolderName;

  @Schema(description = "Account type", example = "CHECKING")
  private String accountType;

  @Schema(description = "Account balance", example = "1000.00")
  private String accountBalance;

  @Schema(description = "Currency", example = "ZAR")
  private String currency;

  @Schema(description = "Validation timestamp", format = "date-time")
  private Instant validatedAt;

  @Schema(description = "Validation reason", example = "Account validation successful")
  private String validationReason;

  @Schema(description = "Error code if validation failed", example = "ACCOUNT_NOT_FOUND")
  private String errorCode;

  @Schema(description = "Error message if validation failed", example = "Account not found")
  private String errorMessage;
}
