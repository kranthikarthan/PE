package com.payments.domain.shared;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

/**
 * BusinessUnitId - Value Object (Entity ID)
 */
@Embeddable
@Value
public class BusinessUnitId {
    String value;
    
    private BusinessUnitId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("BusinessUnitId cannot be null or blank");
        }
        this.value = value;
    }
    
    public static BusinessUnitId of(String value) {
        return new BusinessUnitId(value);
    }
    
    public static BusinessUnitId generate() {
        return new BusinessUnitId("BU-" + UUID.randomUUID().toString());
    }
}
