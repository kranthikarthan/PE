package com.payments.contracts.shared;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Saga Status Enum
 *
 * <p>Defines the possible statuses of a saga orchestration: - Started status - Running
 * status - Completed status - Failed status - Compensated status
 */
@Schema(description = "Saga status enumeration")
public enum SagaStatus {
  @Schema(description = "Saga started")
  STARTED,

  @Schema(description = "Saga running")
  RUNNING,

  @Schema(description = "Saga completed")
  COMPLETED,

  @Schema(description = "Saga failed")
  FAILED,

  @Schema(description = "Saga compensated")
  COMPENSATED,

  @Schema(description = "Saga cancelled")
  CANCELLED,

  @Schema(description = "Saga timeout")
  TIMEOUT
}
