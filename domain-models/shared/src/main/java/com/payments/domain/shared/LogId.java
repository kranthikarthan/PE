package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Value;

/** LogId - Value Object (Entity ID) */
@Embeddable
@Value
public class LogId {
  String value;

  private LogId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("LogId cannot be null or blank");
    }
    this.value = value;
  }

  public static LogId of(String value) {
    return new LogId(value);
  }

  public static LogId generate() {
    return new LogId("LOG-" + UUID.randomUUID().toString());
  }
}
