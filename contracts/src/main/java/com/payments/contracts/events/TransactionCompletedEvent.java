package com.payments.contracts.events;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Transaction Completed Event
 *
 * <p>Event published when a transaction is completed: - Transaction identification - Completion
 * status - Final balances - Transaction summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Transaction completed event")
public class TransactionCompletedEvent extends BaseEvent {

  @Schema(description = "Transaction identifier", example = "txn-123456")
  private String transactionId;

  @Schema(description = "Payment identifier", example = "pay-123456")
  private String paymentId;

  @Schema(description = "Transaction status", example = "COMPLETED")
  private String transactionStatus;

  @Schema(description = "Debit account number", example = "1234567890")
  private String debitAccount;

  @Schema(description = "Credit account number", example = "0987654321")
  private String creditAccount;

  @Schema(description = "Transaction amount", example = "1000.00")
  private BigDecimal amount;

  @Schema(description = "Currency code", example = "ZAR")
  private String currency;

  @Schema(description = "Transaction completion timestamp", format = "date-time")
  private Instant completedAt;

  @Schema(description = "Final debit account balance", example = "9000.00")
  private BigDecimal finalDebitBalance;

  @Schema(description = "Final credit account balance", example = "11000.00")
  private BigDecimal finalCreditBalance;

  @Schema(description = "Transaction summary")
  private String transactionSummary;
}
