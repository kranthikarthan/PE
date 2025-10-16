package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Event published when a saga completes successfully
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaCompletedEvent extends SagaEvent {
    private String sagaName;
    private String paymentId;
    private int totalSteps;
    private int completedSteps;

    public SagaCompletedEvent(SagaId sagaId, TenantContext tenantContext, String correlationId,
                            String sagaName, String paymentId, int totalSteps, int completedSteps) {
        super(sagaId, tenantContext, correlationId, "SagaCompleted");
        this.sagaName = sagaName;
        this.paymentId = paymentId;
        this.totalSteps = totalSteps;
        this.completedSteps = completedSteps;
    }

    @Override
    public Map<String, Object> getEventData() {
        return Map.of(
            "sagaName", sagaName,
            "paymentId", paymentId,
            "totalSteps", totalSteps,
            "completedSteps", completedSteps
        );
    }
}






