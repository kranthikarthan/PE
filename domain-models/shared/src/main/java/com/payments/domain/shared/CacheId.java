package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Value;

/** CacheId - Value Object (Entity ID) */
@Embeddable
@Value
public class CacheId {
  String value;

  private CacheId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("CacheId cannot be null or blank");
    }
    this.value = value;
  }

  public static CacheId of(String value) {
    return new CacheId(value);
  }

  public static CacheId generate() {
    return new CacheId("CACHE-" + UUID.randomUUID().toString());
  }
}
