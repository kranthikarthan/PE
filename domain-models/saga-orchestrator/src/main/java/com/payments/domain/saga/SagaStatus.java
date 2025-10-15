package com.payments.domain.saga;

/** Saga Status enumeration */
public enum SagaStatus {
  STARTED,
  IN_PROGRESS,
  COMPLETED,
  COMPENSATING,
  COMPENSATED,
  FAILED
}
