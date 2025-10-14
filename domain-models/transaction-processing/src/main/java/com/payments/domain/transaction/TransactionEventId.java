package com.payments.domain.transaction;

import lombok.Value;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
@Value
class TransactionEventId {
    String value;
    private TransactionEventId(String value) { if (value == null || value.isBlank()) throw new IllegalArgumentException("TransactionEventId cannot be null or blank"); this.value = value; }
    public static TransactionEventId of(String value) { return new TransactionEventId(value); }
    public static TransactionEventId generate() { return new TransactionEventId("TE-" + UUID.randomUUID()); }
}


