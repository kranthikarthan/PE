package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Event published when a saga starts
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaStartedEvent extends SagaEvent {
    private String sagaName;
    private String paymentId;

    public SagaStartedEvent(SagaId sagaId, TenantContext tenantContext, String correlationId, 
                           String sagaName, String paymentId) {
        super(sagaId, tenantContext, correlationId, "SagaStarted");
        this.sagaName = sagaName;
        this.paymentId = paymentId;
    }

    @Override
    public Map<String, Object> getEventData() {
        return Map.of(
            "sagaName", sagaName,
            "paymentId", paymentId
        );
    }
}






