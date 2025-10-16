package com.payments.saga.domain;

import lombok.Value;

import java.util.UUID;

/**
 * Saga step identifier value object
 */
@Value
public class SagaStepId {
    String value;

    public static SagaStepId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Saga step ID cannot be null or empty");
        }
        return new SagaStepId(value);
    }

    public static SagaStepId generate() {
        return new SagaStepId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}






