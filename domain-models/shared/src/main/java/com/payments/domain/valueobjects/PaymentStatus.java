package com.payments.domain.valueobjects;

/** Value object for payment status */
public enum PaymentStatus {
  PENDING("Pending"),
  INITIATED("Initiated"),
  VALIDATED("Validated"),
  SUBMITTED_TO_CLEARING("Submitted to Clearing"),
  CLEARING("Clearing"),
  CLEARED("Cleared"),
  COMPLETED("Completed"),
  FAILED("Failed");

  private final String description;

  PaymentStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
