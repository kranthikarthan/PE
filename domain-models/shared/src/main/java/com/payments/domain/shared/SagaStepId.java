package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Value;

/** SagaStepId - Value Object (Entity ID) */
@Embeddable
@Value
public class SagaStepId {
  String value;

  private SagaStepId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("SagaStepId cannot be null or blank");
    }
    this.value = value;
  }

  public static SagaStepId of(String value) {
    return new SagaStepId(value);
  }

  public static SagaStepId generate() {
    return new SagaStepId("STEP-" + UUID.randomUUID().toString());
  }
}
