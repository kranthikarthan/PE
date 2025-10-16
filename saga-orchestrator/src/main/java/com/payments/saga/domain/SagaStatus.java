package com.payments.saga.domain;

/** Saga execution status */
public enum SagaStatus {
  PENDING("Pending"),
  RUNNING("Running"),
  COMPLETED("Completed"),
  COMPENSATING("Compensating"),
  COMPENSATED("Compensated"),
  FAILED("Failed");

  private final String displayName;

  SagaStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isTerminal() {
    return this == COMPLETED || this == COMPENSATED || this == FAILED;
  }

  public boolean isActive() {
    return this == PENDING || this == RUNNING || this == COMPENSATING;
  }

  public boolean canTransitionTo(SagaStatus targetStatus) {
    return switch (this) {
      case PENDING -> targetStatus == RUNNING || targetStatus == FAILED;
      case RUNNING -> targetStatus == COMPLETED
          || targetStatus == COMPENSATING
          || targetStatus == FAILED;
      case COMPENSATING -> targetStatus == COMPENSATED || targetStatus == FAILED;
      case COMPLETED, COMPENSATED, FAILED -> false;
    };
  }
}
