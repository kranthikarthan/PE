package com.payments.domain.shared;

import java.time.Instant;

/**
 * DomainEvent - Marker interface for domain events emitted by aggregates.
 */
public interface DomainEvent {
    String getEventType();
    Instant getOccurredAt();
}


