package com.payments.contracts.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/** Validation status enumeration Aligns with domain ValidationStatus enum */
@Schema(description = "Validation result status")
public enum ValidationStatus {
  PENDING("PENDING", "Validation pending"),
  VALIDATED("VALIDATED", "Validation successful"),
  FAILED("FAILED", "Validation failed");

  private final String code;
  private final String description;

  ValidationStatus(String code, String description) {
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
  public static ValidationStatus fromCode(String code) {
    for (ValidationStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown validation status: " + code);
  }
}
