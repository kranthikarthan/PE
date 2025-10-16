package com.payments.saga.domain;

/** Individual saga step execution status */
public enum SagaStepStatus {
  PENDING("Pending"),
  RUNNING("Running"),
  COMPLETED("Completed"),
  FAILED("Failed"),
  COMPENSATING("Compensating"),
  COMPENSATED("Compensated"),
  SKIPPED("Skipped");

  private final String displayName;

  SagaStepStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isTerminal() {
    return this == COMPLETED || this == FAILED || this == COMPENSATED || this == SKIPPED;
  }

  public boolean isActive() {
    return this == PENDING || this == RUNNING || this == COMPENSATING;
  }

  public boolean canTransitionTo(SagaStepStatus targetStatus) {
    return switch (this) {
      case PENDING -> targetStatus == RUNNING || targetStatus == FAILED || targetStatus == SKIPPED;
      case RUNNING -> targetStatus == COMPLETED || targetStatus == FAILED;
      case COMPLETED -> targetStatus == COMPENSATING;
      case FAILED -> targetStatus == COMPENSATING;
      case COMPENSATING -> targetStatus == COMPENSATED || targetStatus == FAILED;
      case COMPENSATED, SKIPPED -> false;
    };
  }
}
