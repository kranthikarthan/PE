package com.payments.domain.shared;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

/**
 * SagaEventId - Value Object (Entity ID)
 */
@Embeddable
@Value
public class SagaEventId {
    String value;
    
    private SagaEventId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SagaEventId cannot be null or blank");
        }
        this.value = value;
    }
    
    public static SagaEventId of(String value) {
        return new SagaEventId(value);
    }
    
    public static SagaEventId generate() {
        return new SagaEventId("EVENT-" + UUID.randomUUID().toString());
    }
}
