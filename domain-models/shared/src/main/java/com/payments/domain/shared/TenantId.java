package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

/** TenantId - Value Object (Entity ID) */
@Embeddable
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class TenantId {
  String value;

  private TenantId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("TenantId cannot be null or blank");
    }
    this.value = value;
  }

  public static TenantId of(String value) {
    return new TenantId(value);
  }

  public static TenantId generate() {
    return new TenantId("TNT-" + UUID.randomUUID().toString());
  }
}
