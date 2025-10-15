package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Value;

/** BackendSystemId - Value Object (Entity ID) */
@Embeddable
@Value
public class BackendSystemId {
  String value;

  private BackendSystemId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("BackendSystemId cannot be null or blank");
    }
    this.value = value;
  }

  public static BackendSystemId of(String value) {
    return new BackendSystemId(value);
  }

  public static BackendSystemId generate() {
    return new BackendSystemId("BS-" + UUID.randomUUID().toString());
  }
}
