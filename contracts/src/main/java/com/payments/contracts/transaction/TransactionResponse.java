package com.payments.contracts.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Transaction Response DTO
 *
 * <p>Response for transaction operations: - Transaction details - Status information - Response
 * metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction response")
public class TransactionResponse {

  @Schema(description = "Transaction identifier", example = "txn-123456")
  private String transactionId;

  @Schema(description = "Payment identifier", example = "pay-123456")
  private String paymentId;

  @Schema(description = "Transaction status", example = "CREATED")
  private String status;

  @Schema(description = "Debit account number", example = "1234567890")
  private String debitAccount;

  @Schema(description = "Credit account number", example = "0987654321")
  private String creditAccount;

  @Schema(description = "Transaction amount", example = "1000.00")
  private BigDecimal amount;

  @Schema(description = "Currency code", example = "ZAR")
  private String currency;

  @Schema(description = "Transaction type", example = "PAYMENT")
  private String transactionType;

  @Schema(description = "Transaction reference", example = "TXN-123456")
  private String reference;

  @Schema(description = "Transaction description", example = "Payment to supplier")
  private String description;

  @Schema(description = "Transaction creation timestamp", format = "date-time")
  private Instant createdAt;

  @Schema(description = "Transaction completion timestamp", format = "date-time")
  private Instant completedAt;

  @Schema(description = "Error code if transaction failed", example = "INSUFFICIENT_FUNDS")
  private String errorCode;

  @Schema(description = "Error message if transaction failed", example = "Insufficient funds")
  private String errorMessage;
}
