package com.payments.batch.error;

/**
 * Enumeration of error severity levels for batch processing operations.
 *
 * <p>This enum defines the severity levels used to categorize and prioritize
 * errors in batch processing operations, enabling appropriate handling,
 * monitoring, and alerting based on error impact.
 *
 * @since PE-404
 */
public enum ErrorSeverity {
  
  /**
   * Critical errors that require immediate attention.
   * 
   * <p>These errors indicate system failures that prevent batch processing
   * from continuing and require immediate intervention. Examples include:
   * database connection failures, file system errors, security violations.
   */
  CRITICAL("Critical", 1, true, true),
  
  /**
   * High severity errors that impact batch processing significantly.
   * 
   * <p>These errors cause batch processing to fail but may be recoverable
   * with intervention. Examples include: file format errors, validation
   * failures, network timeouts.
   */
  HIGH("High", 2, true, true),
  
  /**
   * Medium severity errors that impact individual records or operations.
   * 
   * <p>These errors affect specific records or operations but don't prevent
   * the overall batch from processing. Examples include: individual record
   * validation failures, temporary network issues.
   */
  MEDIUM("Medium", 3, false, true),
  
  /**
   * Low severity errors that are informational or warnings.
   * 
   * <p>These errors don't impact batch processing but indicate potential
   * issues that should be monitored. Examples include: deprecated field
   * usage, performance warnings.
   */
  LOW("Low", 4, false, false);
  
  private final String displayName;
  private final int priority;
  private final boolean requiresAlert;
  private final boolean requiresLogging;
  
  ErrorSeverity(String displayName, int priority, boolean requiresAlert, boolean requiresLogging) {
    this.displayName = displayName;
    this.priority = priority;
    this.requiresAlert = requiresAlert;
    this.requiresLogging = requiresLogging;
  }
  
  /**
   * Gets the display name of the severity level.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return displayName;
  }
  
  /**
   * Gets the priority level (lower number = higher priority).
   *
   * @return the priority level
   */
  public int getPriority() {
    return priority;
  }
  
  /**
   * Checks if this severity level requires alerting.
   *
   * @return true if alerting is required, false otherwise
   */
  public boolean requiresAlert() {
    return requiresAlert;
  }
  
  /**
   * Checks if this severity level requires logging.
   *
   * @return true if logging is required, false otherwise
   */
  public boolean requiresLogging() {
    return requiresLogging;
  }
  
  /**
   * Checks if this severity is higher than the specified severity.
   *
   * @param other the other severity level
   * @return true if this severity is higher, false otherwise
   */
  public boolean isHigherThan(ErrorSeverity other) {
    return this.priority < other.priority;
  }
  
  /**
   * Checks if this severity is lower than the specified severity.
   *
   * @param other the other severity level
   * @return true if this severity is lower, false otherwise
   */
  public boolean isLowerThan(ErrorSeverity other) {
    return this.priority > other.priority;
  }
  
  /**
   * Gets the severity level from its display name.
   *
   * @param displayName the display name
   * @return the severity level, or null if not found
   */
  public static ErrorSeverity fromDisplayName(String displayName) {
    for (ErrorSeverity severity : values()) {
      if (severity.displayName.equalsIgnoreCase(displayName)) {
        return severity;
      }
    }
    return null;
  }
  
  /**
   * Gets the severity level from its priority.
   *
   * @param priority the priority level
   * @return the severity level, or null if not found
   */
  public static ErrorSeverity fromPriority(int priority) {
    for (ErrorSeverity severity : values()) {
      if (severity.priority == priority) {
        return severity;
      }
    }
    return null;
  }
  
  /**
   * Gets the highest severity level from an array of severities.
   *
   * @param severities the severity levels
   * @return the highest severity level
   */
  public static ErrorSeverity getHighest(ErrorSeverity... severities) {
    if (severities == null || severities.length == 0) {
      return LOW;
    }
    
    ErrorSeverity highest = severities[0];
    for (ErrorSeverity severity : severities) {
      if (severity.isHigherThan(highest)) {
        highest = severity;
      }
    }
    
    return highest;
  }
  
  @Override
  public String toString() {
    return displayName;
  }
}
