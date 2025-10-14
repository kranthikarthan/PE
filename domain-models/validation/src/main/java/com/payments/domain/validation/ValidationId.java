package com.payments.domain.validation;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@Value
public class ValidationId {
    String value;
    private ValidationId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ValidationId cannot be null or blank");
        }
        this.value = value;
    }
    public static ValidationId of(String value) { return new ValidationId(value); }
    public static ValidationId generate() { return new ValidationId("VAL-" + UUID.randomUUID()); }
}

