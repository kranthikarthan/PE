package com.payments.reconciliation.exception;

/**
 * Exception for reconciliation exception service operations.
 *
 * <p>This exception is thrown when reconciliation exception
 * service operations fail. It provides comprehensive error
 * handling and exception management for exception operations.
 *
 * @since PE-412
 */
public class ReconciliationExceptionServiceException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;
  
  private final String errorCode;
  private final String errorCategory;
  private final String errorSeverity;
  private final String recoverySuggestion;
  
  /**
   * Constructs a new reconciliation exception service exception.
   *
   * @param message the error message
   */
  public ReconciliationExceptionServiceException(String message) {
    super(message);
    this.errorCode = "RECONCILIATION_EXCEPTION_SERVICE_ERROR";
    this.errorCategory = "EXCEPTION_SERVICE";
    this.errorSeverity = "HIGH";
    this.recoverySuggestion = "Check exception data and try again";
  }
  
  /**
   * Constructs a new reconciliation exception service exception.
   *
   * @param message the error message
   * @param cause the cause
   */
  public ReconciliationExceptionServiceException(String message, Throwable cause) {
    super(message, cause);
    this.errorCode = "RECONCILIATION_EXCEPTION_SERVICE_ERROR";
    this.errorCategory = "EXCEPTION_SERVICE";
    this.errorSeverity = "HIGH";
    this.recoverySuggestion = "Check exception data and try again";
  }
  
  /**
   * Constructs a new reconciliation exception service exception.
   *
   * @param message the error message
   * @param errorCode the error code
   * @param errorCategory the error category
   * @param errorSeverity the error severity
   * @param recoverySuggestion the recovery suggestion
   */
  public ReconciliationExceptionServiceException(String message, String errorCode, String errorCategory, String errorSeverity, String recoverySuggestion) {
    super(message);
    this.errorCode = errorCode;
    this.errorCategory = errorCategory;
    this.errorSeverity = errorSeverity;
    this.recoverySuggestion = recoverySuggestion;
  }
  
  /**
   * Constructs a new reconciliation exception service exception.
   *
   * @param message the error message
   * @param cause the cause
   * @param errorCode the error code
   * @param errorCategory the error category
   * @param errorSeverity the error severity
   * @param recoverySuggestion the recovery suggestion
   */
  public ReconciliationExceptionServiceException(String message, Throwable cause, String errorCode, String errorCategory, String errorSeverity, String recoverySuggestion) {
    super(message, cause);
    this.errorCode = errorCode;
    this.errorCategory = errorCategory;
    this.errorSeverity = errorSeverity;
    this.recoverySuggestion = recoverySuggestion;
  }
  
  /**
   * Gets the error code.
   *
   * @return the error code
   */
  public String getErrorCode() {
    return errorCode;
  }
  
  /**
   * Gets the error category.
   *
   * @return the error category
   */
  public String getErrorCategory() {
    return errorCategory;
  }
  
  /**
   * Gets the error severity.
   *
   * @return the error severity
   */
  public String getErrorSeverity() {
    return errorSeverity;
  }
  
  /**
   * Gets the recovery suggestion.
   *
   * @return the recovery suggestion
   */
  public String getRecoverySuggestion() {
    return recoverySuggestion;
  }
  
  /**
   * Gets the error details.
   *
   * @return the error details
   */
  public String getErrorDetails() {
    return String.format("Error Code: %s, Category: %s, Severity: %s, Suggestion: %s", 
        errorCode, errorCategory, errorSeverity, recoverySuggestion);
  }
}
