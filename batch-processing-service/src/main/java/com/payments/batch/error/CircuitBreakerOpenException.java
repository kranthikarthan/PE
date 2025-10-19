package com.payments.batch.error;

/**
 * Exception thrown when a circuit breaker is open and requests are rejected.
 *
 * <p>This exception is thrown when an operation is attempted through a circuit
 * breaker that is in the OPEN state, indicating that the circuit breaker has
 * detected too many failures and is protecting the system from further damage.
 *
 * @since PE-404
 */
public class CircuitBreakerOpenException extends BatchProcessingException {
  
  private static final String ERROR_CODE = "CIRCUIT_BREAKER_OPEN";
  
  /**
   * Creates a circuit breaker open exception.
   *
   * @param message the error message
   */
  public CircuitBreakerOpenException(String message) {
    super(ERROR_CODE, message, null, ErrorSeverity.HIGH, ErrorCategory.SYSTEM, 
          null, new String[]{"Wait for circuit breaker to close", "Check system health"}, false);
  }
  
  /**
   * Creates a circuit breaker open exception with context.
   *
   * @param message the error message
   * @param circuitBreakerName the circuit breaker name
   * @param failureRate the current failure rate
   */
  public CircuitBreakerOpenException(String message, String circuitBreakerName, double failureRate) {
    super(ERROR_CODE, message, null, ErrorSeverity.HIGH, ErrorCategory.SYSTEM, 
          java.util.Map.of(
              "circuitBreakerName", circuitBreakerName,
              "failureRate", failureRate
          ), 
          new String[]{
              "Wait for circuit breaker to close",
              "Check system health and dependencies",
              "Review error logs for root cause"
          }, false);
  }
  
  /**
   * Creates a circuit breaker open exception with full context.
   *
   * @param message the error message
   * @param cause the underlying cause
   * @param circuitBreakerName the circuit breaker name
   * @param failureRate the current failure rate
   * @param waitDuration the wait duration before retry
   */
  public CircuitBreakerOpenException(String message, Throwable cause, String circuitBreakerName, 
                                   double failureRate, long waitDuration) {
    super(ERROR_CODE, message, cause, ErrorSeverity.HIGH, ErrorCategory.SYSTEM, 
          java.util.Map.of(
              "circuitBreakerName", circuitBreakerName,
              "failureRate", failureRate,
              "waitDurationMs", waitDuration
          ), 
          new String[]{
              "Wait for circuit breaker to close",
              "Check system health and dependencies",
              "Review error logs for root cause",
              "Consider manual intervention if needed"
          }, false);
  }
}
