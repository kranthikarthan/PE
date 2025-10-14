package com.payments.domain.shared;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

/**
 * RoutingRuleId - Value Object (Entity ID)
 */
@Embeddable
@Value
public class RoutingRuleId {
    String value;
    
    private RoutingRuleId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("RoutingRuleId cannot be null or blank");
        }
        this.value = value;
    }
    
    public static RoutingRuleId of(String value) {
        return new RoutingRuleId(value);
    }
    
    public static RoutingRuleId generate() {
        return new RoutingRuleId("RR-" + UUID.randomUUID().toString());
    }
}
