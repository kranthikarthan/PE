package com.payments.paymentinitiation.saga;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Saga State Management for ISO 20022 pain.001/pain.002 flows
 *
 * <p>Manages the state of each saga step for compensation and recovery
 */
@Data
@Builder
public class SagaState {

  private String sagaId;
  private String correlationId;
  private String paymentId;
  private String tenantId;
  private String businessUnitId;
  private SagaStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String errorMessage;
  @Builder.Default private Map<String, SagaStepState> stepStates = new HashMap<>();

  /** Check if a specific step is completed */
  public boolean isStepCompleted(String stepName) {
    SagaStepState stepState = stepStates.get(stepName);
    return stepState != null && stepState.getStatus() == SagaStepStatus.COMPLETED;
  }

  /** Check if a specific step is failed */
  public boolean isStepFailed(String stepName) {
    SagaStepState stepState = stepStates.get(stepName);
    return stepState != null && stepState.getStatus() == SagaStepStatus.FAILED;
  }

  /** Check if a specific step is compensated */
  public boolean isStepCompensated(String stepName) {
    SagaStepState stepState = stepStates.get(stepName);
    return stepState != null && stepState.getStatus() == SagaStepStatus.COMPENSATED;
  }

  // Convenience methods for specific steps
  public boolean isPain001Parsed() {
    return isStepCompleted("PAIN001_PARSED");
  }

  public boolean isPain001Persisted() {
    return isStepCompleted("PAIN001_PERSISTED");
  }

  public boolean isPaymentProcessingInitiated() {
    return isStepCompleted("PAYMENT_PROCESSING_INITIATED");
  }

  public boolean isPain002Generated() {
    return isStepCompleted("PAIN002_GENERATED");
  }

  public boolean isPain002Persisted() {
    return isStepCompleted("PAIN002_PERSISTED");
  }

  public boolean isSagaCompleted() {
    return isStepCompleted("SAGA_COMPLETED");
  }

  /** Get the last completed step */
  public String getLastCompletedStep() {
    return stepStates.entrySet().stream()
        .filter(entry -> entry.getValue().getStatus() == SagaStepStatus.COMPLETED)
        .map(Map.Entry::getKey)
        .reduce((first, second) -> second) // Get the last one
        .orElse(null);
  }

  /** Get the first failed step */
  public String getFirstFailedStep() {
    return stepStates.entrySet().stream()
        .filter(entry -> entry.getValue().getStatus() == SagaStepStatus.FAILED)
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);
  }

  /** Check if saga can be compensated */
  public boolean canBeCompensated() {
    return status == SagaStatus.ACTIVE || status == SagaStatus.FAILED;
  }

  /** Check if saga is in terminal state */
  public boolean isTerminal() {
    return status == SagaStatus.COMPLETED
        || status == SagaStatus.COMPENSATED
        || status == SagaStatus.COMPENSATION_FAILED;
  }

  // ==================== ENUMS ====================

  public enum SagaStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
    COMPENSATED,
    COMPENSATION_FAILED
  }

  @Data
  @Builder
  public static class SagaStepState {
    private String stepName;
    private SagaStepStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private Object stepData; // Store step-specific data for compensation
  }

  public enum SagaStepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    COMPENSATED,
    COMPENSATION_FAILED
  }
}
