package com.payments.contracts.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Create Transaction Request DTO
 *
 * <p>Request for creating a new transaction: - Transaction details - Account information -
 * Amount and currency - Transaction type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create transaction request")
public class CreateTransactionRequest {

  @NotBlank(message = "Tenant ID is required")
  @Schema(description = "Tenant identifier", required = true, example = "tenant-001")
  private String tenantId;

  @NotBlank(message = "Business Unit ID is required")
  @Schema(description = "Business unit identifier", required = true, example = "bu-001")
  private String businessUnitId;

  @NotBlank(message = "Payment ID is required")
  @Schema(description = "Payment identifier", required = true, example = "pay-123456")
  private String paymentId;

  @NotBlank(message = "Debit account is required")
  @Schema(description = "Debit account number", required = true, example = "1234567890")
  private String debitAccount;

  @NotBlank(message = "Credit account is required")
  @Schema(description = "Credit account number", required = true, example = "0987654321")
  private String creditAccount;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  @Schema(description = "Transaction amount", required = true, example = "1000.00")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  @Schema(description = "Currency code", required = true, example = "ZAR")
  private String currency;

  @NotNull(message = "Transaction type is required")
  @Schema(description = "Transaction type", required = true, example = "PAYMENT")
  private String transactionType;

  @Schema(description = "Transaction reference", example = "TXN-123456")
  private String reference;

  @Schema(description = "Transaction description", example = "Payment to supplier")
  private String description;

  @Schema(description = "Correlation ID for tracing", example = "corr-123456")
  private String correlationId;
}
