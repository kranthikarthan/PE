package com.payments.domain.shared;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

/**
 * AccountAdapterId - Value Object (Entity ID)
 */
@Embeddable
@Value
public class AccountAdapterId {
    String value;
    
    private AccountAdapterId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AccountAdapterId cannot be null or blank");
        }
        this.value = value;
    }
    
    public static AccountAdapterId of(String value) {
        return new AccountAdapterId(value);
    }
    
    public static AccountAdapterId generate() {
        return new AccountAdapterId("AA-" + UUID.randomUUID().toString());
    }
}
