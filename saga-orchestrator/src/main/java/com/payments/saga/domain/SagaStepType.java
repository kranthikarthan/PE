package com.payments.saga.domain;

/** Types of saga steps */
public enum SagaStepType {
  VALIDATION("Validation"),
  ROUTING("Routing"),
  ACCOUNT_ADAPTER("Account Adapter"),
  TRANSACTION_PROCESSING("Transaction Processing"),
  NOTIFICATION("Notification"),
  COMPENSATION("Compensation");

  private final String displayName;

  SagaStepType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isCompensationStep() {
    return this == COMPENSATION;
  }

  public boolean isBusinessStep() {
    return this != COMPENSATION;
  }
}
