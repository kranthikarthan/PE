package com.payments.domain.transaction;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Value;

@Embeddable
@Value
class LedgerEntryId {
  String value;

  private LedgerEntryId(String value) {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("LedgerEntryId cannot be null or blank");
    this.value = value;
  }

  public static LedgerEntryId of(String value) {
    return new LedgerEntryId(value);
  }

  public static LedgerEntryId generate() {
    return new LedgerEntryId("LED-" + UUID.randomUUID());
  }
}
