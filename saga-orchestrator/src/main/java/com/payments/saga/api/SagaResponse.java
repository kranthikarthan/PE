package com.payments.saga.api;

import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for saga information */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Saga information response")
public class SagaResponse {

  @Schema(description = "Saga ID", example = "saga-123")
  private String sagaId;

  @Schema(description = "Saga name", example = "PaymentProcessingSaga")
  private String sagaName;

  @Schema(description = "Saga status", example = "RUNNING")
  private SagaStatus status;

  @Schema(description = "Tenant ID", example = "tenant-1")
  private String tenantId;

  @Schema(description = "Business Unit ID", example = "bu-1")
  private String businessUnitId;

  @Schema(description = "Correlation ID", example = "corr-123")
  private String correlationId;

  @Schema(description = "Payment ID", example = "pay-456")
  private String paymentId;

  @Schema(description = "Saga data")
  private Map<String, Object> sagaData;

  @Schema(description = "Error message")
  private String errorMessage;

  @Schema(description = "Started at")
  private Instant startedAt;

  @Schema(description = "Completed at")
  private Instant completedAt;

  @Schema(description = "Failed at")
  private Instant failedAt;

  @Schema(description = "Compensated at")
  private Instant compensatedAt;

  @Schema(description = "Current step index")
  private Integer currentStepIndex;

  @Schema(description = "Total steps")
  private Integer totalSteps;

  @Schema(description = "Completed steps count")
  private Integer completedStepsCount;

  @Schema(description = "Progress percentage")
  private Double progressPercentage;

  @Schema(description = "Saga steps")
  private List<SagaStepResponse> steps;

  public static SagaResponse fromDomain(Saga saga) {
    return SagaResponse.builder()
        .sagaId(saga.getId().getValue())
        .sagaName(saga.getSagaName())
        .status(saga.getStatus())
        .tenantId(saga.getTenantContext().getTenantId())
        .businessUnitId(saga.getTenantContext().getBusinessUnitId())
        .correlationId(saga.getCorrelationId())
        .paymentId(saga.getPaymentId())
        .sagaData(saga.getSagaData())
        .errorMessage(saga.getErrorMessage())
        .startedAt(saga.getStartedAt())
        .completedAt(saga.getCompletedAt())
        .failedAt(saga.getFailedAt())
        .compensatedAt(saga.getCompensatedAt())
        .currentStepIndex(saga.getCurrentStepIndex())
        .totalSteps(saga.getTotalSteps())
        .completedStepsCount(saga.getCompletedStepsCount())
        .progressPercentage(saga.getProgressPercentage())
        .build();
  }
}
