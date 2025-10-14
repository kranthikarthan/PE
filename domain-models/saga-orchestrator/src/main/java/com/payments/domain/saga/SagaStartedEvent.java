package com.payments.domain.saga;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Saga Started
 */
@Value
@AllArgsConstructor
public class SagaStartedEvent implements DomainEvent {
    SagaId sagaId;
    String sagaName;
    SagaType sagaType;
    String businessKey;
    Instant startedAt;
    
    @Override
    public String getEventType() {
        return "SagaStarted";
    }
    
    @Override
    public Instant getOccurredAt() {
        return startedAt;
    }
}
