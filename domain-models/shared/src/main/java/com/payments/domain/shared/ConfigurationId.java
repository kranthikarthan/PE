package com.payments.domain.shared;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

/**
 * ConfigurationId - Value Object (Entity ID)
 */
@Embeddable
@Value
public class ConfigurationId {
    String value;
    
    private ConfigurationId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ConfigurationId cannot be null or blank");
        }
        this.value = value;
    }
    
    public static ConfigurationId of(String value) {
        return new ConfigurationId(value);
    }
    
    public static ConfigurationId generate() {
        return new ConfigurationId("CFG-" + UUID.randomUUID().toString());
    }
}
