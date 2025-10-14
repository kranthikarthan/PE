package com.payments.domain.shared;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

/**
 * TenantUserId - Value Object (Entity ID)
 */
@Embeddable
@Value
public class TenantUserId {
    String value;
    
    private TenantUserId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TenantUserId cannot be null or blank");
        }
        this.value = value;
    }
    
    public static TenantUserId of(String value) {
        return new TenantUserId(value);
    }
    
    public static TenantUserId generate() {
        return new TenantUserId("TU-" + UUID.randomUUID().toString());
    }
}
