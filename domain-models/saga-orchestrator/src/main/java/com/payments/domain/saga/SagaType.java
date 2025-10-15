package com.payments.domain.saga;

/** Saga Type enumeration */
public enum SagaType {
  PAYMENT_PROCESSING,
  ACCOUNT_UPDATE,
  TRANSACTION_REVERSAL,
  SETTLEMENT,
  RECONCILIATION,
  BATCH_PROCESSING
}
