package com.payments.domain.valueobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Value object for clearing system reference
 */
@Data
@EqualsAndHashCode
public class ClearingSystemReference {
    
    private final String value;
    
    private ClearingSystemReference(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Clearing system reference cannot be null or empty");
        }
        this.value = value.trim();
    }
    
    public static ClearingSystemReference of(String value) {
        return new ClearingSystemReference(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
