package com.payments.domain.payment;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Embeddable
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class ClearingSystemReference {
  String value;

  private ClearingSystemReference(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ClearingSystemReference cannot be null or blank");
    }
    this.value = value;
  }

  public static ClearingSystemReference of(String value) {
    return new ClearingSystemReference(value);
  }
}
