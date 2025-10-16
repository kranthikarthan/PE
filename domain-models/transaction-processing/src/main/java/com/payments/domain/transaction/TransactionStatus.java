package com.payments.domain.transaction;

public enum TransactionStatus {
  CREATED,
  PROCESSING,
  CLEARING,
  COMPLETED,
  FAILED
}
