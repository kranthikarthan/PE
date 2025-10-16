package com.payments.saga.domain;

import lombok.Value;

import java.util.UUID;

/**
 * Saga identifier value object
 */
@Value
public class SagaId {
    String value;

    public static SagaId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Saga ID cannot be null or empty");
        }
        return new SagaId(value);
    }

    public static SagaId generate() {
        return new SagaId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}






