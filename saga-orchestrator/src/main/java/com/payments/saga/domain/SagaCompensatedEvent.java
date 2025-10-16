package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Event published when saga compensation completes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaCompensatedEvent extends SagaEvent {
    private String sagaName;
    private String paymentId;
    private String reason;
    private int compensatedSteps;

    public SagaCompensatedEvent(SagaId sagaId, TenantContext tenantContext, String correlationId,
                               String sagaName, String paymentId, String reason, int compensatedSteps) {
        super(sagaId, tenantContext, correlationId, "SagaCompensated");
        this.sagaName = sagaName;
        this.paymentId = paymentId;
        this.reason = reason;
        this.compensatedSteps = compensatedSteps;
    }

    @Override
    public Map<String, Object> getEventData() {
        return Map.of(
            "sagaName", sagaName,
            "paymentId", paymentId,
            "reason", reason,
            "compensatedSteps", compensatedSteps
        );
    }
}






