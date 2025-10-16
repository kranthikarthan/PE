package com.payments.domain.payment;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Status Change (Entity within Payment Aggregate)
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusChange {
  @Enumerated(EnumType.STRING)
  private PaymentStatus fromStatus;

  @Enumerated(EnumType.STRING)
  private PaymentStatus toStatus;

  private String reason;

  private String changedBy;

  private Instant changedAt;
}
