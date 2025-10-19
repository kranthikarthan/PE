package com.payments.domain.valueobjects;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Value object for status change */
@Data
@Builder
@EqualsAndHashCode
public class StatusChange {

  private final PaymentStatus status;
  private final String reason;
  private final Instant timestamp;

  public StatusChange(PaymentStatus status, String reason, Instant timestamp) {
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    if (reason == null || reason.trim().isEmpty()) {
      throw new IllegalArgumentException("Reason cannot be null or empty");
    }
    if (timestamp == null) {
      throw new IllegalArgumentException("Timestamp cannot be null");
    }
    this.status = status;
    this.reason = reason.trim();
    this.timestamp = timestamp;
  }

  // Builder method for compatibility
  public static StatusChangeBuilder builder() {
    return new StatusChangeBuilder();
  }
}
