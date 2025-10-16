package com.payments.contracts.events;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transaction Created Event
 *
 * <p>Event published when a transaction is created: - Transaction identification - Transaction
 * details - Account information - Transaction metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Transaction created event")
public class TransactionCreatedEvent extends BaseEvent {

  @Schema(description = "Transaction identifier", example = "txn-123456")
  private String transactionId;

  @Schema(description = "Payment identifier", example = "pay-123456")
  private String paymentId;

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
  private Instant transactionCreatedAt;

  @Schema(description = "Transaction metadata")
  private java.util.Map<String, String> transactionMetadata;
}
