package com.payments.contracts.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Account Balance Response DTO
 *
 * <p>Response for account balance: - Account balance - Currency - Account status - Response metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account balance response")
public class AccountBalanceResponse {

  @Schema(description = "Account number", example = "1234567890")
  private String accountNumber;

  @Schema(description = "Account balance", example = "1000.00")
  private BigDecimal balance;

  @Schema(description = "Currency", example = "ZAR")
  private String currency;

  @Schema(description = "Account status", example = "ACTIVE")
  private String accountStatus;

  @Schema(description = "Available balance", example = "950.00")
  private BigDecimal availableBalance;

  @Schema(description = "Reserved balance", example = "50.00")
  private BigDecimal reservedBalance;

  @Schema(description = "Balance timestamp", format = "date-time")
  private Instant balanceTimestamp;

  @Schema(description = "Error code if balance retrieval failed", example = "ACCOUNT_NOT_FOUND")
  private String errorCode;

  @Schema(description = "Error message if balance retrieval failed", example = "Account not found")
  private String errorMessage;
}
