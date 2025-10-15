package com.payments.domain.saga;

/** Step Status enumeration */
public enum StepStatus {
  PENDING,
  IN_PROGRESS,
  COMPLETED,
  FAILED,
  COMPENSATING,
  COMPENSATED
}
