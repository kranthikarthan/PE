package com.payments.contracts.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/** Payment status enumeration Aligns with domain PaymentStatus enum */
@Schema(description = "Payment processing status")
public enum PaymentStatus {
  INITIATED("INITIATED", "Payment initiated"),
  VALIDATED("VALIDATED", "Payment validated"),
  SUBMITTED_TO_CLEARING("SUBMITTED_TO_CLEARING", "Submitted to clearing"),
  CLEARING("CLEARING", "Payment clearing"),
  CLEARED("CLEARED", "Payment cleared"),
  COMPLETED("COMPLETED", "Payment completed"),
  FAILED("FAILED", "Payment failed");

  private final String code;
  private final String description;

  PaymentStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  @JsonValue
  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  @JsonCreator
  public static PaymentStatus fromCode(String code) {
    for (PaymentStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown payment status: " + code);
  }
}
