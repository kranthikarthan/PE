package com.payments.contracts.saga;

import com.payments.contracts.events.BaseEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Saga Completed Event
 *
 * <p>Event published when a saga orchestration completes: - Saga identification - Completion status
 * - Final result - Saga summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Saga completed event")
public class SagaCompletedEvent extends BaseEvent {

  @Schema(description = "Saga identifier", example = "saga-123456")
  private String sagaId;

  @Schema(description = "Saga status", example = "COMPLETED")
  private String sagaStatus;

  @Schema(description = "Saga result", example = "SUCCESS")
  private String sagaResult;

  @Schema(description = "Saga completion timestamp", format = "date-time")
  private Instant completedAt;

  @Schema(description = "Total execution time in milliseconds", example = "5000")
  private Long totalExecutionTimeMs;

  @Schema(description = "Number of steps completed", example = "5")
  private Integer stepsCompleted;

  @Schema(description = "Number of steps failed", example = "0")
  private Integer stepsFailed;

  @Schema(description = "Saga summary")
  private Map<String, Object> sagaSummary;

  @Schema(description = "Final saga context")
  private Map<String, Object> finalContext;
}
