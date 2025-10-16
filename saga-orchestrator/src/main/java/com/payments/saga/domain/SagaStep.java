package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Individual saga step within a saga
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStep {
    private SagaStepId id;
    private SagaId sagaId;
    private String stepName;
    private SagaStepType stepType;
    private SagaStepStatus status;
    private int sequence;
    private String serviceName;
    private String endpoint;
    private String compensationEndpoint;
    private Map<String, Object> inputData;
    private Map<String, Object> outputData;
    private Map<String, Object> errorData;
    private String errorMessage;
    private int retryCount;
    private int maxRetries;
    private Instant startedAt;
    private Instant completedAt;
    private Instant failedAt;
    private Instant compensatedAt;
    private TenantContext tenantContext;
    private String correlationId;

    public boolean isCompleted() {
        return status == SagaStepStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == SagaStepStatus.FAILED;
    }

    public boolean isCompensated() {
        return status == SagaStepStatus.COMPENSATED;
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean canRetry() {
        return retryCount < maxRetries && status == SagaStepStatus.FAILED;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void markAsRunning() {
        this.status = SagaStepStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void markAsCompleted(Map<String, Object> outputData) {
        this.status = SagaStepStatus.COMPLETED;
        this.outputData = outputData;
        this.completedAt = Instant.now();
    }

    public void markAsFailed(String errorMessage, Map<String, Object> errorData) {
        this.status = SagaStepStatus.FAILED;
        this.errorMessage = errorMessage;
        this.errorData = errorData;
        this.failedAt = Instant.now();
    }

    public void markAsCompensating() {
        this.status = SagaStepStatus.COMPENSATING;
    }

    public void markAsCompensated() {
        this.status = SagaStepStatus.COMPENSATED;
        this.compensatedAt = Instant.now();
    }

    public void markAsSkipped() {
        this.status = SagaStepStatus.SKIPPED;
        this.completedAt = Instant.now();
    }

    public boolean hasCompensation() {
        return compensationEndpoint != null && !compensationEndpoint.trim().isEmpty();
    }
}
