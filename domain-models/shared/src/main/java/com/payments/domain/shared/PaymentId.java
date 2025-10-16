package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Value;

/** PaymentId - Value Object (Entity ID) */
@Embeddable
@Value
public class PaymentId {
  String value;

  public PaymentId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("PaymentId cannot be null or blank");
    }
    this.value = value;
  }

  public static PaymentId of(String value) {
    return new PaymentId(value);
  }

  public static PaymentId generate() {
    return new PaymentId("PAY-" + UUID.randomUUID().toString());
  }
}
