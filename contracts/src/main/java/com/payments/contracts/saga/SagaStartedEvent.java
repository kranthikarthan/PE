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
 * Saga Started Event
 *
 * <p>Event published when a saga orchestration starts: - Saga identification - Template
 * information - Initial context - Saga metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Saga started event")
public class SagaStartedEvent extends BaseEvent {

  @Schema(description = "Saga identifier", example = "saga-123456")
  private String sagaId;

  @Schema(description = "Saga template name", example = "PaymentProcessingSaga")
  private String templateName;

  @Schema(description = "Saga template version", example = "1.0.0")
  private String templateVersion;

  @Schema(description = "Saga context")
  private Map<String, Object> sagaContext;

  @Schema(description = "Saga priority", example = "HIGH")
  private String priority;

  @Schema(description = "Saga timeout in seconds", example = "300")
  private Long timeoutSeconds;

  @Schema(description = "Saga description", example = "Payment processing workflow")
  private String description;
}
