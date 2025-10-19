package com.payments.batch.error;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for executing operations with retry logic and circuit breaker patterns.
 *
 * <p>This service provides comprehensive retry and resilience patterns for batch
 * processing operations, including exponential backoff, circuit breaker protection,
 * and detailed error tracking and monitoring.
 *
 * <p><b>Features:</b>
 * <ul>
 *   <li>Configurable retry policies with multiple strategies
 *   <li>Circuit breaker protection against cascading failures
 *   <li>Automatic error classification and handling
 *   <li>Detailed operation metrics and monitoring
 *   <li>Integration with existing error handling framework
 * </ul>
 *
 * @since PE-404
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryService {
  
  private final ConcurrentMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, RetryPolicy> retryPolicies = new ConcurrentHashMap<>();
  
  /**
   * Executes an operation with retry logic and circuit breaker protection.
   *
   * @param operationName the name of the operation for tracking
   * @param operation the operation to execute
   * @param retryPolicy the retry policy to use
   * @return operation result
   * @throws BatchProcessingException if operation fails after all retries
   */
  public <T> T executeWithRetry(String operationName, RetryableOperation<T> operation, RetryPolicy retryPolicy) {
    return executeWithRetry(operationName, operation, retryPolicy, null);
  }
  
  /**
   * Executes an operation with retry logic, circuit breaker protection, and custom error handler.
   *
   * @param operationName the name of the operation for tracking
   * @param operation the operation to execute
   * @param retryPolicy the retry policy to use
   * @param errorHandler custom error handler
   * @return operation result
   * @throws BatchProcessingException if operation fails after all retries
   */
  public <T> T executeWithRetry(String operationName, RetryableOperation<T> operation, 
                               RetryPolicy retryPolicy, Function<Exception, BatchProcessingException> errorHandler) {
    
    CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(operationName);
    RetryPolicy policy = retryPolicy != null ? retryPolicy : RetryPolicy.defaultPolicy();
    
    log.debug("Executing operation '{}' with retry policy: {}", operationName, policy.getSummary());
    
    Exception lastException = null;
    LocalDateTime startTime = LocalDateTime.now();
    
    for (int attempt = 1; attempt <= policy.getMaxAttempts(); attempt++) {
      try {
        // Check circuit breaker before attempting operation
        if (!circuitBreaker.allowRequest()) {
          throw new CircuitBreakerOpenException(
              "Circuit breaker is open for operation: " + operationName,
              operationName,
              circuitBreaker.calculateFailureRate()
          );
        }
        
        // Execute operation through circuit breaker
        T result = circuitBreaker.execute(() -> operation.execute());
        
        // Record success
        circuitBreaker.onSuccess();
        log.debug("Operation '{}' succeeded on attempt {}", operationName, attempt);
        
        return result;
        
      } catch (CircuitBreakerOpenException e) {
        // Circuit breaker is open, don't retry
        log.warn("Circuit breaker is open for operation '{}': {}", operationName, e.getMessage());
        throw e;
        
      } catch (Exception e) {
        lastException = e;
        
        // Check if this exception should be retried
        if (!policy.shouldRetry(e)) {
          log.warn("Operation '{}' failed with non-retryable exception: {}", operationName, e.getMessage());
          throw createBatchProcessingException(operationName, e, errorHandler);
        }
        
        // Record failure in circuit breaker
        circuitBreaker.onFailure();
        
        log.warn("Operation '{}' failed on attempt {}/{}: {}", 
            operationName, attempt, policy.getMaxAttempts(), e.getMessage());
        
        // If this is the last attempt, don't wait
        if (attempt >= policy.getMaxAttempts()) {
          break;
        }
        
        // Calculate delay for next attempt
        long delayMs = policy.calculateDelay(attempt);
        if (delayMs > 0) {
          log.debug("Waiting {}ms before retry attempt {}", delayMs, attempt + 1);
          try {
            Thread.sleep(delayMs);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new BatchProcessingException(
                "RETRY_INTERRUPTED", 
                "Retry operation was interrupted", 
                ie, 
                ErrorSeverity.HIGH, 
                ErrorCategory.SYSTEM, 
                null, 
                new String[]{"Check system resources", "Review operation configuration"}, 
                false
            );
          }
        }
      }
    }
    
    // All retries exhausted
    Duration totalDuration = Duration.between(startTime, LocalDateTime.now());
    log.error("Operation '{}' failed after {} attempts in {}", 
        operationName, policy.getMaxAttempts(), totalDuration);
    
    throw createBatchProcessingException(operationName, lastException, errorHandler);
  }
  
  /**
   * Executes an operation with default retry policy.
   *
   * @param operationName the name of the operation for tracking
   * @param operation the operation to execute
   * @return operation result
   * @throws BatchProcessingException if operation fails after all retries
   */
  public <T> T executeWithRetry(String operationName, RetryableOperation<T> operation) {
    return executeWithRetry(operationName, operation, RetryPolicy.defaultPolicy());
  }
  
  /**
   * Executes an operation with network retry policy.
   *
   * @param operationName the name of the operation for tracking
   * @param operation the operation to execute
   * @return operation result
   * @throws BatchProcessingException if operation fails after all retries
   */
  public <T> T executeNetworkOperation(String operationName, RetryableOperation<T> operation) {
    return executeWithRetry(operationName, operation, RetryPolicy.networkPolicy());
  }
  
  /**
   * Executes an operation with database retry policy.
   *
   * @param operationName the name of the operation for tracking
   * @param operation the operation to execute
   * @return operation result
   * @throws BatchProcessingException if operation fails after all retries
   */
  public <T> T executeDatabaseOperation(String operationName, RetryableOperation<T> operation) {
    return executeWithRetry(operationName, operation, RetryPolicy.databasePolicy());
  }
  
  /**
   * Executes an operation with file retry policy.
   *
   * @param operationName the name of the operation for tracking
   * @param operation the operation to execute
   * @return operation result
   * @throws BatchProcessingException if operation fails after all retries
   */
  public <T> T executeFileOperation(String operationName, RetryableOperation<T> operation) {
    return executeWithRetry(operationName, operation, RetryPolicy.filePolicy());
  }
  
  /**
   * Executes an operation with critical retry policy.
   *
   * @param operationName the name of the operation for tracking
   * @param operation the operation to execute
   * @return operation result
   * @throws BatchProcessingException if operation fails after all retries
   */
  public <T> T executeCriticalOperation(String operationName, RetryableOperation<T> operation) {
    return executeWithRetry(operationName, operation, RetryPolicy.criticalPolicy());
  }
  
  /**
   * Gets or creates a circuit breaker for the specified operation.
   *
   * @param operationName the operation name
   * @return circuit breaker for the operation
   */
  public CircuitBreaker getOrCreateCircuitBreaker(String operationName) {
    return circuitBreakers.computeIfAbsent(operationName, name -> 
        CircuitBreaker.forBatchProcessing(name));
  }
  
  /**
   * Gets circuit breaker statistics for all operations.
   *
   * @return map of operation names to circuit breaker statistics
   */
  public java.util.Map<String, CircuitBreakerStats> getAllCircuitBreakerStats() {
    return circuitBreakers.entrySet().stream()
        .collect(java.util.stream.Collectors.toMap(
            java.util.Map.Entry::getKey,
            entry -> entry.getValue().getStats()
        ));
  }
  
  /**
   * Resets all circuit breakers.
   */
  public void resetAllCircuitBreakers() {
    circuitBreakers.values().forEach(CircuitBreaker::reset);
    log.info("Reset all circuit breakers");
  }
  
  /**
   * Resets a specific circuit breaker.
   *
   * @param operationName the operation name
   */
  public void resetCircuitBreaker(String operationName) {
    CircuitBreaker circuitBreaker = circuitBreakers.get(operationName);
    if (circuitBreaker != null) {
      circuitBreaker.reset();
      log.info("Reset circuit breaker for operation: {}", operationName);
    }
  }
  
  /**
   * Gets or creates a retry policy for the specified operation.
   *
   * @param operationName the operation name
   * @param policyFactory function to create retry policy
   * @return retry policy for the operation
   */
  public RetryPolicy getOrCreateRetryPolicy(String operationName, Function<String, RetryPolicy> policyFactory) {
    return retryPolicies.computeIfAbsent(operationName, policyFactory);
  }
  
  /**
   * Creates a batch processing exception from an operation exception.
   *
   * @param operationName the operation name
   * @param exception the original exception
   * @param errorHandler custom error handler
   * @return batch processing exception
   */
  private BatchProcessingException createBatchProcessingException(String operationName, Exception exception, 
                                                                Function<Exception, BatchProcessingException> errorHandler) {
    if (errorHandler != null) {
      return errorHandler.apply(exception);
    }
    
    // Default error classification
    if (exception instanceof BatchProcessingException) {
      return (BatchProcessingException) exception;
    }
    
    // Classify exception type
    ErrorCategory category = classifyException(exception);
    ErrorSeverity severity = determineSeverity(exception);
    boolean retryable = isRetryableException(exception);
    
    return new BatchProcessingException(
        "OPERATION_FAILED",
        "Operation '" + operationName + "' failed: " + exception.getMessage(),
        exception,
        severity,
        category,
        java.util.Map.of("operationName", operationName),
        retryable ? new String[]{"Retry the operation", "Check system resources"} : 
                   new String[]{"Review operation parameters", "Contact support"},
        retryable
    );
  }
  
  /**
   * Classifies an exception into an error category.
   *
   * @param exception the exception to classify
   * @return error category
   */
  private ErrorCategory classifyException(Exception exception) {
    String className = exception.getClass().getSimpleName().toLowerCase();
    
    if (className.contains("network") || className.contains("timeout") || className.contains("connection")) {
      return ErrorCategory.INTEGRATION;
    } else if (className.contains("validation") || className.contains("format") || className.contains("parse")) {
      return ErrorCategory.VALIDATION;
    } else if (className.contains("security") || className.contains("auth") || className.contains("permission")) {
      return ErrorCategory.SECURITY;
    } else if (className.contains("database") || className.contains("sql") || className.contains("jdbc")) {
      return ErrorCategory.SYSTEM;
    } else if (className.contains("file") || className.contains("io") || className.contains("stream")) {
      return ErrorCategory.SYSTEM;
    } else {
      return ErrorCategory.UNKNOWN;
    }
  }
  
  /**
   * Determines the severity of an exception.
   *
   * @param exception the exception to analyze
   * @return error severity
   */
  private ErrorSeverity determineSeverity(Exception exception) {
    String className = exception.getClass().getSimpleName().toLowerCase();
    
    if (className.contains("critical") || className.contains("fatal")) {
      return ErrorSeverity.CRITICAL;
    } else if (className.contains("timeout") || className.contains("connection")) {
      return ErrorSeverity.HIGH;
    } else if (className.contains("validation") || className.contains("format")) {
      return ErrorSeverity.MEDIUM;
    } else {
      return ErrorSeverity.LOW;
    }
  }
  
  /**
   * Determines if an exception is retryable.
   *
   * @param exception the exception to analyze
   * @return true if retryable, false otherwise
   */
  private boolean isRetryableException(Exception exception) {
    String className = exception.getClass().getSimpleName().toLowerCase();
    
    // Non-retryable exceptions
    if (className.contains("validation") || className.contains("format") || 
        className.contains("security") || className.contains("auth")) {
      return false;
    }
    
    // Retryable exceptions
    if (className.contains("timeout") || className.contains("connection") || 
        className.contains("network") || className.contains("io")) {
      return true;
    }
    
    // Default to retryable for unknown exceptions
    return true;
  }
  
  /**
   * Functional interface for retryable operations.
   */
  @FunctionalInterface
  public interface RetryableOperation<T> {
    T execute() throws Exception;
  }
}
