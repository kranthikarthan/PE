package com.payments.batch.error;

import java.time.Duration;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.Data;

/**
 * Configuration for retry policies in batch processing operations.
 *
 * <p>This class defines retry behavior including maximum attempts, backoff strategies,
 * retry conditions, and circuit breaker patterns for resilient batch processing.
 *
 * <p><b>Retry Strategies:</b>
 * <ul>
 *   <li>Fixed delay between retries
 *   <li>Exponential backoff with jitter
 *   <li>Linear backoff with maximum delay
 *   <li>Custom backoff strategies
 * </ul>
 *
 * @since PE-404
 */
@Data
@Builder
public class RetryPolicy {
  
  /** Maximum number of retry attempts */
  private final int maxAttempts;
  
  /** Initial delay between retries in milliseconds */
  private final long initialDelayMs;
  
  /** Maximum delay between retries in milliseconds */
  private final long maxDelayMs;
  
  /** Backoff multiplier for exponential backoff */
  private final double backoffMultiplier;
  
  /** Whether to use jitter to randomize delays */
  private final boolean useJitter;
  
  /** Maximum jitter percentage (0.0 to 1.0) */
  private final double maxJitter;
  
  /** Retry strategy type */
  private final RetryStrategy strategy;
  
  /** Predicate to determine if an exception should be retried */
  private final Predicate<Throwable> retryCondition;
  
  /** Whether to retry on all exceptions */
  private final boolean retryOnAllExceptions;
  
  /** Specific exception types to retry */
  private final Class<? extends Throwable>[] retryOnExceptions;
  
  /** Specific exception types to never retry */
  private final Class<? extends Throwable>[] noRetryOnExceptions;
  
  /**
   * Creates a default retry policy with exponential backoff.
   *
   * @return default retry policy
   */
  public static RetryPolicy defaultPolicy() {
    return RetryPolicy.builder()
        .maxAttempts(3)
        .initialDelayMs(1000)
        .maxDelayMs(30000)
        .backoffMultiplier(2.0)
        .useJitter(true)
        .maxJitter(0.1)
        .strategy(RetryStrategy.EXPONENTIAL)
        .retryOnAllExceptions(true)
        .build();
  }
  
  /**
   * Creates a retry policy for network operations.
   *
   * @return network retry policy
   */
  public static RetryPolicy networkPolicy() {
    return RetryPolicy.builder()
        .maxAttempts(5)
        .initialDelayMs(500)
        .maxDelayMs(10000)
        .backoffMultiplier(1.5)
        .useJitter(true)
        .maxJitter(0.2)
        .strategy(RetryStrategy.EXPONENTIAL)
        .retryOnAllExceptions(true)
        .build();
  }
  
  /**
   * Creates a retry policy for database operations.
   *
   * @return database retry policy
   */
  public static RetryPolicy databasePolicy() {
    return RetryPolicy.builder()
        .maxAttempts(3)
        .initialDelayMs(2000)
        .maxDelayMs(15000)
        .backoffMultiplier(2.0)
        .useJitter(false)
        .strategy(RetryStrategy.EXPONENTIAL)
        .retryOnAllExceptions(true)
        .build();
  }
  
  /**
   * Creates a retry policy for file operations.
   *
   * @return file retry policy
   */
  public static RetryPolicy filePolicy() {
    return RetryPolicy.builder()
        .maxAttempts(3)
        .initialDelayMs(1000)
        .maxDelayMs(5000)
        .backoffMultiplier(1.5)
        .useJitter(true)
        .maxJitter(0.1)
        .strategy(RetryStrategy.LINEAR)
        .retryOnAllExceptions(true)
        .build();
  }
  
  /**
   * Creates a retry policy for critical operations.
   *
   * @return critical retry policy
   */
  public static RetryPolicy criticalPolicy() {
    return RetryPolicy.builder()
        .maxAttempts(10)
        .initialDelayMs(500)
        .maxDelayMs(60000)
        .backoffMultiplier(1.2)
        .useJitter(true)
        .maxJitter(0.3)
        .strategy(RetryStrategy.EXPONENTIAL)
        .retryOnAllExceptions(true)
        .build();
  }
  
  /**
   * Calculates the delay for the specified attempt number.
   *
   * @param attemptNumber the attempt number (1-based)
   * @return delay in milliseconds
   */
  public long calculateDelay(int attemptNumber) {
    if (attemptNumber <= 1) {
      return 0;
    }
    
    long delay = initialDelayMs;
    
    switch (strategy) {
      case FIXED:
        delay = initialDelayMs;
        break;
        
      case LINEAR:
        delay = initialDelayMs * attemptNumber;
        break;
        
      case EXPONENTIAL:
        delay = (long) (initialDelayMs * Math.pow(backoffMultiplier, attemptNumber - 1));
        break;
        
      case CUSTOM:
        // Custom strategy would be implemented here
        delay = initialDelayMs;
        break;
    }
    
    // Apply maximum delay limit
    delay = Math.min(delay, maxDelayMs);
    
    // Apply jitter if enabled
    if (useJitter && maxJitter > 0) {
      double jitter = (Math.random() - 0.5) * 2 * maxJitter;
      delay = (long) (delay * (1 + jitter));
    }
    
    return Math.max(0, delay);
  }
  
  /**
   * Determines if an exception should be retried.
   *
   * @param exception the exception to check
   * @return true if should retry, false otherwise
   */
  public boolean shouldRetry(Throwable exception) {
    if (retryOnAllExceptions) {
      return true;
    }
    
    if (retryCondition != null) {
      return retryCondition.test(exception);
    }
    
    // Check specific exception types
    if (retryOnExceptions != null) {
      for (Class<? extends Throwable> exceptionType : retryOnExceptions) {
        if (exceptionType.isInstance(exception)) {
          return true;
        }
      }
    }
    
    // Check if exception should not be retried
    if (noRetryOnExceptions != null) {
      for (Class<? extends Throwable> exceptionType : noRetryOnExceptions) {
        if (exceptionType.isInstance(exception)) {
          return false;
        }
      }
    }
    
    return false;
  }
  
  /**
   * Gets the total retry duration for this policy.
   *
   * @return total retry duration
   */
  public Duration getTotalRetryDuration() {
    long totalDelay = 0;
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      totalDelay += calculateDelay(attempt);
    }
    return Duration.ofMillis(totalDelay);
  }
  
  /**
   * Checks if this policy allows retries.
   *
   * @return true if retries are allowed, false otherwise
   */
  public boolean allowsRetries() {
    return maxAttempts > 1;
  }
  
  /**
   * Gets a summary of this retry policy.
   *
   * @return policy summary
   */
  public String getSummary() {
    return String.format("RetryPolicy{maxAttempts=%d, strategy=%s, initialDelay=%dms, maxDelay=%dms, jitter=%s}", 
        maxAttempts, strategy, initialDelayMs, maxDelayMs, useJitter);
  }
  
  /**
   * Enumeration of retry strategies.
   */
  public enum RetryStrategy {
    /** Fixed delay between retries */
    FIXED,
    
    /** Linear increase in delay */
    LINEAR,
    
    /** Exponential increase in delay */
    EXPONENTIAL,
    
    /** Custom retry strategy */
    CUSTOM
  }
}
