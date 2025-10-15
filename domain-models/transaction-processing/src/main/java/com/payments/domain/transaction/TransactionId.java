package com.payments.domain.transaction;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Value;

@Embeddable
@Value
public class TransactionId {
  String value;

  private TransactionId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("TransactionId cannot be null or blank");
    }
    this.value = value;
  }

  public static TransactionId of(String value) {
    return new TransactionId(value);
  }

  public static TransactionId generate() {
    return new TransactionId("TXN-" + UUID.randomUUID());
  }
}
