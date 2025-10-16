package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Base class for saga events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class SagaEvent {
    private String eventId;
    private SagaId sagaId;
    private TenantContext tenantContext;
    private String correlationId;
    private Instant occurredAt;
    private String eventType;

    protected SagaEvent(SagaId sagaId, TenantContext tenantContext, String correlationId, String eventType) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.sagaId = sagaId;
        this.tenantContext = tenantContext;
        this.correlationId = correlationId;
        this.occurredAt = Instant.now();
        this.eventType = eventType;
    }

    public abstract Map<String, Object> getEventData();
}






