package com.payments.domain.validation;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@Value
class ValidationRuleId {
    String value;
    private ValidationRuleId(String value) { if (value == null || value.isBlank()) throw new IllegalArgumentException("ValidationRuleId cannot be null or blank"); this.value = value; }
    public static ValidationRuleId of(String value) { return new ValidationRuleId(value); }
    public static ValidationRuleId generate() { return new ValidationRuleId("VR-" + UUID.randomUUID()); }
}


