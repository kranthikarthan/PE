package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Value;

@Embeddable
@Value
public class ClearingRouteId {
  String value;

  private ClearingRouteId(String value) {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("ClearingRouteId cannot be null or blank");
    this.value = value;
  }

  public static ClearingRouteId of(String value) {
    return new ClearingRouteId(value);
  }

  public static ClearingRouteId generate() {
    return new ClearingRouteId("CLR-" + UUID.randomUUID());
  }
}
