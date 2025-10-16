package com.payments.saga.api;

import com.payments.saga.domain.SagaStep;
import com.payments.saga.domain.SagaStepStatus;
import com.payments.saga.domain.SagaStepType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for saga step information */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Saga step information response")
public class SagaStepResponse {

  @Schema(description = "Step ID", example = "step-123")
  private String stepId;

  @Schema(description = "Saga ID", example = "saga-123")
  private String sagaId;

  @Schema(description = "Step name", example = "Validate Payment")
  private String stepName;

  @Schema(description = "Step type", example = "VALIDATION")
  private SagaStepType stepType;

  @Schema(description = "Step status", example = "COMPLETED")
  private SagaStepStatus status;

  @Schema(description = "Sequence number", example = "1")
  private Integer sequence;

  @Schema(description = "Service name", example = "validation-service")
  private String serviceName;

  @Schema(description = "Endpoint", example = "/api/v1/validate")
  private String endpoint;

  @Schema(description = "Compensation endpoint", example = "/api/v1/validate/compensate")
  private String compensationEndpoint;

  @Schema(description = "Input data")
  private Map<String, Object> inputData;

  @Schema(description = "Output data")
  private Map<String, Object> outputData;

  @Schema(description = "Error data")
  private Map<String, Object> errorData;

  @Schema(description = "Error message")
  private String errorMessage;

  @Schema(description = "Retry count", example = "0")
  private Integer retryCount;

  @Schema(description = "Max retries", example = "3")
  private Integer maxRetries;

  @Schema(description = "Started at")
  private Instant startedAt;

  @Schema(description = "Completed at")
  private Instant completedAt;

  @Schema(description = "Failed at")
  private Instant failedAt;

  @Schema(description = "Compensated at")
  private Instant compensatedAt;

  @Schema(description = "Correlation ID", example = "corr-123")
  private String correlationId;

  public static SagaStepResponse fromDomain(SagaStep step) {
    return SagaStepResponse.builder()
        .stepId(step.getId().getValue())
        .sagaId(step.getSagaId().getValue())
        .stepName(step.getStepName())
        .stepType(step.getStepType())
        .status(step.getStatus())
        .sequence(step.getSequence())
        .serviceName(step.getServiceName())
        .endpoint(step.getEndpoint())
        .compensationEndpoint(step.getCompensationEndpoint())
        .inputData(step.getInputData())
        .outputData(step.getOutputData())
        .errorData(step.getErrorData())
        .errorMessage(step.getErrorMessage())
        .retryCount(step.getRetryCount())
        .maxRetries(step.getMaxRetries())
        .startedAt(step.getStartedAt())
        .completedAt(step.getCompletedAt())
        .failedAt(step.getFailedAt())
        .compensatedAt(step.getCompensatedAt())
        .correlationId(step.getCorrelationId())
        .build();
  }
}
