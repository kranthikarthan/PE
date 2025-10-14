package com.payments.domain.shared;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@Value
public class ClearingMessageId {
    String value;
    private ClearingMessageId(String value) { if (value == null || value.isBlank()) throw new IllegalArgumentException("ClearingMessageId cannot be null or blank"); this.value = value; }
    public static ClearingMessageId of(String value) { return new ClearingMessageId(value); }
    public static ClearingMessageId generate() { return new ClearingMessageId("CLM-" + UUID.randomUUID()); }
}


