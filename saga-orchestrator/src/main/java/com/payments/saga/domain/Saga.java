package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Saga aggregate root for orchestrating payment processing workflows
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Saga {
    private SagaId id;
    private String sagaName;
    private SagaStatus status;
    private TenantContext tenantContext;
    private String correlationId;
    private String paymentId;
    private List<SagaStep> steps;
    private Map<String, Object> sagaData;
    private String errorMessage;
    private Instant startedAt;
    private Instant completedAt;
    private Instant failedAt;
    private Instant compensatedAt;
    private int currentStepIndex;

    public static Saga create(String sagaName, TenantContext tenantContext, String correlationId, String paymentId) {
        return Saga.builder()
                .id(SagaId.generate())
                .sagaName(sagaName)
                .status(SagaStatus.PENDING)
                .tenantContext(tenantContext)
                .correlationId(correlationId)
                .paymentId(paymentId)
                .steps(new ArrayList<>())
                .currentStepIndex(0)
                .startedAt(Instant.now())
                .build();
    }

    public void addStep(SagaStep step) {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        step.setSagaId(this.id);
        step.setSequence(steps.size());
        steps.add(step);
    }

    public void addStep(String stepName, SagaStepType stepType, String serviceName, String endpoint, 
                       String compensationEndpoint, Map<String, Object> inputData) {
        SagaStep step = SagaStep.builder()
                .id(SagaStepId.generate())
                .stepName(stepName)
                .stepType(stepType)
                .status(SagaStepStatus.PENDING)
                .serviceName(serviceName)
                .endpoint(endpoint)
                .compensationEndpoint(compensationEndpoint)
                .inputData(inputData)
                .maxRetries(3)
                .tenantContext(this.tenantContext)
                .correlationId(this.correlationId)
                .build();
        
        addStep(step);
    }

    public Optional<SagaStep> getCurrentStep() {
        if (steps == null || currentStepIndex >= steps.size()) {
            return Optional.empty();
        }
        return Optional.of(steps.get(currentStepIndex));
    }

    public Optional<SagaStep> getStepByType(SagaStepType stepType) {
        return steps.stream()
                .filter(step -> step.getStepType() == stepType)
                .findFirst();
    }

    public List<SagaStep> getCompletedSteps() {
        return steps.stream()
                .filter(SagaStep::isCompleted)
                .toList();
    }

    public List<SagaStep> getFailedSteps() {
        return steps.stream()
                .filter(SagaStep::isFailed)
                .toList();
    }

    public List<SagaStep> getCompensatedSteps() {
        return steps.stream()
                .filter(SagaStep::isCompensated)
                .toList();
    }

    public boolean isCompleted() {
        return status == SagaStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == SagaStatus.FAILED;
    }

    public boolean isCompensated() {
        return status == SagaStatus.COMPENSATED;
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean hasFailedSteps() {
        return !getFailedSteps().isEmpty();
    }

    public boolean allStepsCompleted() {
        return steps.stream().allMatch(step -> step.isCompleted() || step.getStatus() == SagaStepStatus.SKIPPED);
    }

    public void start() {
        if (status != SagaStatus.PENDING) {
            throw new IllegalStateException("Saga can only be started from PENDING status");
        }
        this.status = SagaStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void complete() {
        if (status != SagaStatus.RUNNING) {
            throw new IllegalStateException("Saga can only be completed from RUNNING status");
        }
        this.status = SagaStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void fail(String errorMessage) {
        this.status = SagaStatus.FAILED;
        this.errorMessage = errorMessage;
        this.failedAt = Instant.now();
    }

    public void startCompensation() {
        if (status != SagaStatus.RUNNING && status != SagaStatus.FAILED) {
            throw new IllegalStateException("Saga can only start compensation from RUNNING or FAILED status");
        }
        this.status = SagaStatus.COMPENSATING;
    }

    public void completeCompensation() {
        if (status != SagaStatus.COMPENSATING) {
            throw new IllegalStateException("Saga can only complete compensation from COMPENSATING status");
        }
        this.status = SagaStatus.COMPENSATED;
        this.compensatedAt = Instant.now();
    }

    public void moveToNextStep() {
        this.currentStepIndex++;
    }

    public void moveToPreviousStep() {
        if (currentStepIndex > 0) {
            this.currentStepIndex--;
        }
    }

    public void resetToStep(int stepIndex) {
        if (stepIndex < 0 || stepIndex >= steps.size()) {
            throw new IllegalArgumentException("Invalid step index: " + stepIndex);
        }
        this.currentStepIndex = stepIndex;
    }

    public int getTotalSteps() {
        return steps != null ? steps.size() : 0;
    }

    public int getCompletedStepsCount() {
        return getCompletedSteps().size();
    }

    public int getFailedStepsCount() {
        return getFailedSteps().size();
    }

    public double getProgressPercentage() {
        if (steps == null || steps.isEmpty()) {
            return 0.0;
        }
        return (double) getCompletedStepsCount() / steps.size() * 100.0;
    }
}






