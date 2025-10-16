package com.payments.contracts.shared;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Transaction Status Enum
 *
 * <p>Defines the possible statuses of a transaction: - Created status - Processing status -
 * Completed status - Failed status
 */
@Schema(description = "Transaction status enumeration")
public enum TransactionStatus {
  @Schema(description = "Transaction created")
  CREATED,

  @Schema(description = "Transaction processing")
  PROCESSING,

  @Schema(description = "Transaction completed")
  COMPLETED,

  @Schema(description = "Transaction failed")
  FAILED,

  @Schema(description = "Transaction cancelled")
  CANCELLED,

  @Schema(description = "Transaction pending")
  PENDING,

  @Schema(description = "Transaction reversed")
  REVERSED
}
