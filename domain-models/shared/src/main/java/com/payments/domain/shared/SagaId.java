package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Value;

/** SagaId - Value Object (Entity ID) */
@Embeddable
@Value
public class SagaId {
  String value;

  private SagaId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("SagaId cannot be null or blank");
    }
    this.value = value;
  }

  public static SagaId of(String value) {
    return new SagaId(value);
  }

  public static SagaId generate() {
    return new SagaId("SAGA-" + UUID.randomUUID().toString());
  }
}
