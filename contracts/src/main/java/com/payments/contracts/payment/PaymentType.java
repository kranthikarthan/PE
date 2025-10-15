package com.payments.contracts.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/** Payment type enumeration Aligns with domain PaymentType enum */
@Schema(description = "Type of payment")
public enum PaymentType {
  EFT("EFT", "Electronic Funds Transfer"),
  DEBIT_ORDER("DEBIT_ORDER", "Debit Order"),
  STOP_ORDER("STOP_ORDER", "Stop Order"),
  IMMEDIATE_PAYMENT("IMMEDIATE_PAYMENT", "Immediate Payment");

  private final String code;
  private final String description;

  PaymentType(String code, String description) {
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
  public static PaymentType fromCode(String code) {
    for (PaymentType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown payment type: " + code);
  }
}
