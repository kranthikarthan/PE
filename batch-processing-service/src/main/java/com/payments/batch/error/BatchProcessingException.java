package com.payments.batch.error;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

/**
 * Base exception for batch processing operations with enhanced error context.
 *
 * <p>This exception provides comprehensive error information including error codes,
 * severity levels, context data, and timing information for effective error handling
 * and monitoring in batch processing operations.
 *
 * <p><b>Error Context Includes:</b>
 * <ul>
 *   <li>Error code and category
 *   <li>Severity level (CRITICAL, HIGH, MEDIUM, LOW)
 *   <li>Operation context and parameters
 *   <li>Timing information
 *   <li>Recovery suggestions
 * </ul>
 *
 * @since PE-404
 */
@Getter
public class BatchProcessingException extends RuntimeException {
  
  /** Error code for categorization and handling */
  private final String errorCode;
  
  /** Error severity level */
  private final ErrorSeverity severity;
  
  /** Error category for grouping related errors */
  private final ErrorCategory category;
  
  /** Additional context data */
  private final Map<String, Object> context;
  
  /** Timestamp when the error occurred */
  private final LocalDateTime timestamp;
  
  /** Suggested recovery actions */
  private final String[] recoverySuggestions;
  
  /** Whether this error is retryable */
  private final boolean retryable;
  
  /**
   * Creates a batch processing exception with basic information.
   *
   * @param errorCode the error code
   * @param message the error message
   * @param cause the underlying cause
   */
  public BatchProcessingException(String errorCode, String message, Throwable cause) {
    this(errorCode, message, cause, ErrorSeverity.MEDIUM, ErrorCategory.SYSTEM, null, null, false);
  }
  
  /**
   * Creates a batch processing exception with full context.
   *
   * @param errorCode the error code
   * @param message the error message
   * @param cause the underlying cause
   * @param severity the error severity
   * @param category the error category
   * @param context additional context data
   * @param recoverySuggestions suggested recovery actions
   * @param retryable whether this error is retryable
   */
  public BatchProcessingException(String errorCode, String message, Throwable cause, 
                                 ErrorSeverity severity, ErrorCategory category, 
                                 Map<String, Object> context, String[] recoverySuggestions, 
                                 boolean retryable) {
    super(message, cause);
    this.errorCode = errorCode;
    this.severity = severity;
    this.category = category;
    this.context = context;
    this.timestamp = LocalDateTime.now();
    this.recoverySuggestions = recoverySuggestions;
    this.retryable = retryable;
  }
  
  /**
   * Creates a retryable exception.
   *
   * @param errorCode the error code
   * @param message the error message
   * @param cause the underlying cause
   * @return retryable exception
   */
  public static BatchProcessingException retryable(String errorCode, String message, Throwable cause) {
    return new BatchProcessingException(errorCode, message, cause, 
        ErrorSeverity.MEDIUM, ErrorCategory.SYSTEM, null, 
        new String[]{"Retry the operation", "Check system resources"}, true);
  }
  
  /**
   * Creates a non-retryable exception.
   *
   * @param errorCode the error code
   * @param message the error message
   * @param cause the underlying cause
   * @return non-retryable exception
   */
  public static BatchProcessingException nonRetryable(String errorCode, String message, Throwable cause) {
    return new BatchProcessingException(errorCode, message, cause, 
        ErrorSeverity.HIGH, ErrorCategory.VALIDATION, null, 
        new String[]{"Review input data", "Contact support"}, false);
  }
  
  /**
   * Creates a critical exception.
   *
   * @param errorCode the error code
   * @param message the error message
   * @param cause the underlying cause
   * @return critical exception
   */
  public static BatchProcessingException critical(String errorCode, String message, Throwable cause) {
    return new BatchProcessingException(errorCode, message, cause, 
        ErrorSeverity.CRITICAL, ErrorCategory.SYSTEM, null, 
        new String[]{"Immediate attention required", "Check system status"}, false);
  }
  
  /**
   * Gets a formatted error summary.
   *
   * @return formatted error summary
   */
  public String getErrorSummary() {
    return String.format("[%s] %s: %s (Severity: %s, Category: %s, Retryable: %s)", 
        errorCode, timestamp, getMessage(), severity, category, retryable);
  }
  
  /**
   * Gets recovery suggestions as a formatted string.
   *
   * @return formatted recovery suggestions
   */
  public String getFormattedRecoverySuggestions() {
    if (recoverySuggestions == null || recoverySuggestions.length == 0) {
      return "No recovery suggestions available";
    }
    return String.join("; ", recoverySuggestions);
  }
  
  /**
   * Checks if this is a critical error.
   *
   * @return true if critical, false otherwise
   */
  public boolean isCritical() {
    return severity == ErrorSeverity.CRITICAL;
  }
  
  /**
   * Checks if this is a high severity error.
   *
   * @return true if high severity, false otherwise
   */
  public boolean isHighSeverity() {
    return severity == ErrorSeverity.HIGH || severity == ErrorSeverity.CRITICAL;
  }
  
  /**
   * Gets context value by key.
   *
   * @param key the context key
   * @return context value or null if not found
   */
  public Object getContextValue(String key) {
    return context != null ? context.get(key) : null;
  }
  
  /**
   * Checks if context contains a specific key.
   *
   * @param key the context key
   * @return true if key exists, false otherwise
   */
  public boolean hasContextValue(String key) {
    return context != null && context.containsKey(key);
  }
  
  @Override
  public String toString() {
    return getErrorSummary();
  }
}
