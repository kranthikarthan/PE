package com.payments.domain.validation;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Value;

@Embeddable
@Value
class FailedRuleId {
  String value;

  private FailedRuleId(String value) {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("FailedRuleId cannot be null or blank");
    this.value = value;
  }

  public static FailedRuleId of(String value) {
    return new FailedRuleId(value);
  }

  public static FailedRuleId generate() {
    return new FailedRuleId("FR-" + UUID.randomUUID());
  }
}
