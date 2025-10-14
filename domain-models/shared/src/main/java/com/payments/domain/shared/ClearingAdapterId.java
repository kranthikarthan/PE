package com.payments.domain.shared;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@Value
public class ClearingAdapterId {
    String value;
    private ClearingAdapterId(String value) { if (value == null || value.isBlank()) throw new IllegalArgumentException("ClearingAdapterId cannot be null or blank"); this.value = value; }
    public static ClearingAdapterId of(String value) { return new ClearingAdapterId(value); }
    public static ClearingAdapterId generate() { return new ClearingAdapterId("CLAD-" + UUID.randomUUID()); }
}
