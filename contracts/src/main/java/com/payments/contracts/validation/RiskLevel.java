package com.payments.contracts.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/** Risk level enumeration Aligns with domain RiskLevel enum */
@Schema(description = "Risk level assessment")
public enum RiskLevel {
  LOW("LOW", "Low risk"),
  MEDIUM("MEDIUM", "Medium risk"),
  HIGH("HIGH", "High risk"),
  CRITICAL("CRITICAL", "Critical risk");

  private final String code;
  private final String description;

  RiskLevel(String code, String description) {
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
  public static RiskLevel fromCode(String code) {
    for (RiskLevel level : values()) {
      if (level.code.equals(code)) {
        return level;
      }
    }
    throw new IllegalArgumentException("Unknown risk level: " + code);
  }
}
