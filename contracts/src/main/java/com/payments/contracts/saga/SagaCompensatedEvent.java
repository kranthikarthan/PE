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
 * Saga Compensated Event
 *
 * <p>Event published when a saga compensation completes: - Saga identification - Compensation
 * status - Compensation result - Compensation summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Saga compensated event")
public class SagaCompensatedEvent extends BaseEvent {

  @Schema(description = "Saga identifier", example = "saga-123456")
  private String sagaId;

  @Schema(description = "Saga status", example = "COMPENSATED")
  private String sagaStatus;

  @Schema(description = "Compensation result", example = "SUCCESS")
  private String compensationResult;

  @Schema(description = "Compensation timestamp", format = "date-time")
  private Instant compensatedAt;

  @Schema(description = "Total compensation time in milliseconds", example = "3000")
  private Long totalCompensationTimeMs;

  @Schema(description = "Number of steps compensated", example = "3")
  private Integer stepsCompensated;

  @Schema(description = "Number of compensation failures", example = "0")
  private Integer compensationFailures;

  @Schema(description = "Compensation summary")
  private Map<String, Object> compensationSummary;

  @Schema(description = "Compensation reason", example = "Payment validation failed")
  private String compensationReason;
}
