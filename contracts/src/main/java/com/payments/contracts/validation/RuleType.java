package com.payments.contracts.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/** Rule type enumeration Aligns with domain RuleType enum */
@Schema(description = "Type of validation rule")
public enum RuleType {
  AMOUNT_LIMIT("AMOUNT_LIMIT", "Amount limit validation"),
  VELOCITY_CHECK("VELOCITY_CHECK", "Velocity check validation"),
  FRAUD_DETECTION("FRAUD_DETECTION", "Fraud detection validation"),
  COMPLIANCE_CHECK("COMPLIANCE_CHECK", "Compliance check validation"),
  ACCOUNT_VALIDATION("ACCOUNT_VALIDATION", "Account validation"),
  CUSTOMER_LIMIT("CUSTOMER_LIMIT", "Customer limit validation");

  private final String code;
  private final String description;

  RuleType(String code, String description) {
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
  public static RuleType fromCode(String code) {
    for (RuleType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown rule type: " + code);
  }
}
