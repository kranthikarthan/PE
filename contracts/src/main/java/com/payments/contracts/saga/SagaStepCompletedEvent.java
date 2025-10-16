package com.payments.contracts.saga;

import com.payments.contracts.events.BaseEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Saga Step Completed Event
 *
 * <p>Event published when a saga step completes: - Step identification - Step result -
 * Step metadata - Next step information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Saga step completed event")
public class SagaStepCompletedEvent extends BaseEvent {

  @Schema(description = "Saga identifier", example = "saga-123456")
  private String sagaId;

  @Schema(description = "Step identifier", example = "step-001")
  private String stepId;

  @Schema(description = "Step name", example = "Validate Payment")
  private String stepName;

  @Schema(description = "Step type", example = "VALIDATION")
  private String stepType;

  @Schema(description = "Step status", example = "COMPLETED")
  private String stepStatus;

  @Schema(description = "Step result", example = "SUCCESS")
  private String stepResult;

  @Schema(description = "Step response")
  private Map<String, Object> stepResponse;

  @Schema(description = "Step execution time in milliseconds", example = "1500")
  private Long executionTimeMs;

  @Schema(description = "Next step identifier", example = "step-002")
  private String nextStepId;

  @Schema(description = "Step metadata")
  private Map<String, String> stepMetadata;
}
