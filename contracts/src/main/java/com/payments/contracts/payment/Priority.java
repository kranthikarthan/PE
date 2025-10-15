package com.payments.contracts.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/** Payment priority enumeration Aligns with domain Priority enum */
@Schema(description = "Payment processing priority")
public enum Priority {
  LOW("LOW", "Low priority"),
  NORMAL("NORMAL", "Normal priority"),
  HIGH("HIGH", "High priority"),
  URGENT("URGENT", "Urgent priority");

  private final String code;
  private final String description;

  Priority(String code, String description) {
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
  public static Priority fromCode(String code) {
    for (Priority priority : values()) {
      if (priority.code.equals(code)) {
        return priority;
      }
    }
    throw new IllegalArgumentException("Unknown priority: " + code);
  }
}
