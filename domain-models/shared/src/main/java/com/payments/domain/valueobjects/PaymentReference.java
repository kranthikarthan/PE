package com.payments.domain.valueobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Value object for payment reference
 */
@Data
@EqualsAndHashCode
public class PaymentReference {
    
    private final String value;
    
    private PaymentReference(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment reference cannot be null or empty");
        }
        this.value = value.trim();
    }
    
    public static PaymentReference of(String value) {
        return new PaymentReference(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
