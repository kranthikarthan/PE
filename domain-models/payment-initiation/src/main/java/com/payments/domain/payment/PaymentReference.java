package com.payments.domain.payment;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Embeddable
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class PaymentReference {
  String value;

  private PaymentReference(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("PaymentReference cannot be null or blank");
    }
    if (value.length() > 35) {
      throw new IllegalArgumentException("PaymentReference max length is 35");
    }
    this.value = value;
  }

  public static PaymentReference of(String value) {
    return new PaymentReference(value);
  }
}
