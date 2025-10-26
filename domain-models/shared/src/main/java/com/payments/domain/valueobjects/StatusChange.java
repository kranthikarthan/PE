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

  private final PaymentStatus fromStatus;
  private final PaymentStatus toStatus;
  private final String reason;
  private final Instant timestamp;
  private final String changedBy;

  public StatusChange(
      PaymentStatus fromStatus,
      PaymentStatus toStatus,
      String reason,
      Instant timestamp,
      String changedBy) {
    if (toStatus == null) {
      throw new IllegalArgumentException("To status cannot be null");
    }
    if (reason == null || reason.trim().isEmpty()) {
      throw new IllegalArgumentException("Reason cannot be null or empty");
    }
    if (timestamp == null) {
      throw new IllegalArgumentException("Timestamp cannot be null");
    }
    this.fromStatus = fromStatus;
    this.toStatus = toStatus;
    this.reason = reason.trim();
    this.timestamp = timestamp;
    this.changedBy = changedBy;
  }

  // Legacy constructor for backward compatibility
  public StatusChange(PaymentStatus status, String reason, Instant timestamp) {
    this(null, status, reason, timestamp, "system");
  }

  public PaymentStatus getFromStatus() {
    return fromStatus;
  }

  public PaymentStatus getToStatus() {
    return toStatus;
  }

  public String getChangedBy() {
    return changedBy;
  }

  public Instant getChangedAt() {
    return timestamp;
  }

  // Builder method for compatibility
  public static StatusChangeBuilder builder() {
    return new StatusChangeBuilder();
  }
}
