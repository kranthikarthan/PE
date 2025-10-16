package com.payments.domain.payment;

public enum PaymentStatus {
  INITIATED,
  VALIDATED,
  SUBMITTED_TO_CLEARING,
  CLEARING,
  CLEARED,
  COMPLETED,
  FAILED
}
