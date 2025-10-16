package com.payments.contracts.shared;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Transaction Type Enum
 *
 * <p>Defines the types of transactions supported: - Payment transactions - Transfer
 * transactions - Adjustment transactions - Reversal transactions
 */
@Schema(description = "Transaction type enumeration")
public enum TransactionType {
  @Schema(description = "Payment transaction")
  PAYMENT,

  @Schema(description = "Transfer transaction")
  TRANSFER,

  @Schema(description = "Adjustment transaction")
  ADJUSTMENT,

  @Schema(description = "Reversal transaction")
  REVERSAL,

  @Schema(description = "Refund transaction")
  REFUND,

  @Schema(description = "Fee transaction")
  FEE,

  @Schema(description = "Interest transaction")
  INTEREST,

  @Schema(description = "Settlement transaction")
  SETTLEMENT
}
