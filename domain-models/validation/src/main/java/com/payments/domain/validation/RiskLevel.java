package com.payments.domain.validation;

/**
 * Risk Level Enum
 *
 * <p>Represents the risk level of a payment: - LOW: Low risk payment - MEDIUM: Medium risk payment
 * - HIGH: High risk payment - CRITICAL: Critical risk payment
 */
public enum RiskLevel {
  LOW("LOW", "Low risk", 1),
  MEDIUM("MEDIUM", "Medium risk", 2),
  HIGH("HIGH", "High risk", 3),
  CRITICAL("CRITICAL", "Critical risk", 4);

  private final String code;
  private final String description;
  private final int severity;

  RiskLevel(String code, String description, int severity) {
    this.code = code;
    this.description = description;
    this.severity = severity;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public int getSeverity() {
    return severity;
  }

  /** Check if this risk level is higher than the other */
  public boolean isHigherThan(RiskLevel other) {
    return this.severity > other.severity;
  }

  /** Check if this risk level is lower than the other */
  public boolean isLowerThan(RiskLevel other) {
    return this.severity < other.severity;
  }

  @Override
  public String toString() {
    return code;
  }
}
