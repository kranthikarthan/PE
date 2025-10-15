package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

/** BusinessUnitId - Value Object (Entity ID) */
@Embeddable
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class BusinessUnitId {
  String value;

  private BusinessUnitId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("BusinessUnitId cannot be null or blank");
    }
    this.value = value;
  }

  public static BusinessUnitId of(String value) {
    return new BusinessUnitId(value);
  }

  public static BusinessUnitId generate() {
    String raw = "BU-" + UUID.randomUUID().toString().replace("-", "");
    // Ensure length <= 30
    if (raw.length() > 30) {
      raw = raw.substring(0, 30);
    }
    return new BusinessUnitId(raw);
  }
}
