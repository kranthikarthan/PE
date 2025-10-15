package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

/** UserId - Value Object (Entity ID) */
@Embeddable
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class UserId {
  String value;

  private UserId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("UserId cannot be null or blank");
    }
    this.value = value;
  }

  public static UserId of(String value) {
    return new UserId(value);
  }

  public static UserId generate() {
    return new UserId("USR-" + UUID.randomUUID().toString());
  }
}
