package com.payments.batch.error;

import java.time.Duration;
import lombok.Builder;
import lombok.Data;

/**
 * Configuration for circuit breaker behavior.
 *
 * <p>This class defines the parameters that control circuit breaker behavior
 * including failure thresholds, timeouts, and recovery mechanisms for
 * resilient batch processing operations.
 *
 * @since PE-404
 */
@Data
@Builder
public class CircuitBreakerConfig {
  
  /** Failure threshold percentage (0.0 to 1.0) */
  private final double failureThreshold;
  
  /** Success threshold percentage (0.0 to 1.0) */
  private final double successThreshold;
  
  /** Minimum number of calls before opening circuit */
  private final int minimumNumberOfCalls;
  
  /** Wait duration in open state before attempting reset */
  private final Duration waitDurationInOpenState;
  
  /** Maximum number of calls allowed in half-open state */
  private final int halfOpenMaxCalls;
  
  /** Whether to record calls in half-open state */
  private final boolean recordCallsInHalfOpenState;
  
  /**
   * Creates a default circuit breaker configuration.
   *
   * @return default configuration
   */
  public static CircuitBreakerConfig defaultConfig() {
    return CircuitBreakerConfig.builder()
        .failureThreshold(0.5) // 50% failure rate
        .successThreshold(0.8) // 80% success rate
        .minimumNumberOfCalls(10)
        .waitDurationInOpenState(Duration.ofSeconds(60))
        .halfOpenMaxCalls(5)
        .recordCallsInHalfOpenState(true)
        .build();
  }
  
  /**
   * Creates a circuit breaker configuration for batch processing operations.
   *
   * @return batch processing configuration
   */
  public static CircuitBreakerConfig batchProcessingConfig() {
    return CircuitBreakerConfig.builder()
        .failureThreshold(0.3) // 30% failure rate
        .successThreshold(0.9) // 90% success rate
        .minimumNumberOfCalls(20)
        .waitDurationInOpenState(Duration.ofMinutes(5))
        .halfOpenMaxCalls(3)
        .recordCallsInHalfOpenState(true)
        .build();
  }
  
  /**
   * Creates a circuit breaker configuration for external service calls.
   *
   * @return external service configuration
   */
  public static CircuitBreakerConfig externalServiceConfig() {
    return CircuitBreakerConfig.builder()
        .failureThreshold(0.6) // 60% failure rate
        .successThreshold(0.7) // 70% success rate
        .minimumNumberOfCalls(5)
        .waitDurationInOpenState(Duration.ofSeconds(30))
        .halfOpenMaxCalls(2)
        .recordCallsInHalfOpenState(true)
        .build();
  }
  
  /**
   * Creates a circuit breaker configuration for critical operations.
   *
   * @return critical operations configuration
   */
  public static CircuitBreakerConfig criticalConfig() {
    return CircuitBreakerConfig.builder()
        .failureThreshold(0.2) // 20% failure rate
        .successThreshold(0.95) // 95% success rate
        .minimumNumberOfCalls(50)
        .waitDurationInOpenState(Duration.ofMinutes(10))
        .halfOpenMaxCalls(1)
        .recordCallsInHalfOpenState(true)
        .build();
  }
  
  /**
   * Creates a circuit breaker configuration for file operations.
   *
   * @return file operations configuration
   */
  public static CircuitBreakerConfig fileOperationsConfig() {
    return CircuitBreakerConfig.builder()
        .failureThreshold(0.4) // 40% failure rate
        .successThreshold(0.85) // 85% success rate
        .minimumNumberOfCalls(15)
        .waitDurationInOpenState(Duration.ofMinutes(2))
        .halfOpenMaxCalls(3)
        .recordCallsInHalfOpenState(true)
        .build();
  }
  
  /**
   * Validates the circuit breaker configuration.
   *
   * @return true if configuration is valid, false otherwise
   */
  public boolean isValid() {
    return failureThreshold >= 0.0 && failureThreshold <= 1.0 &&
           successThreshold >= 0.0 && successThreshold <= 1.0 &&
           minimumNumberOfCalls > 0 &&
           waitDurationInOpenState != null && !waitDurationInOpenState.isNegative() &&
           halfOpenMaxCalls > 0;
  }
  
  /**
   * Gets the failure threshold as a percentage.
   *
   * @return failure threshold percentage
   */
  public double getFailureThresholdPercentage() {
    return failureThreshold * 100.0;
  }
  
  /**
   * Gets the success threshold as a percentage.
   *
   * @return success threshold percentage
   */
  public double getSuccessThresholdPercentage() {
    return successThreshold * 100.0;
  }
  
  /**
   * Gets the wait duration in open state in seconds.
   *
   * @return wait duration in seconds
   */
  public long getWaitDurationInOpenStateSeconds() {
    return waitDurationInOpenState.getSeconds();
  }
  
  /**
   * Gets a summary of the circuit breaker configuration.
   *
   * @return configuration summary
   */
  public String getSummary() {
    return String.format("CircuitBreakerConfig{failureThreshold=%.1f%%, successThreshold=%.1f%%, " +
        "minCalls=%d, waitDuration=%ds, halfOpenMaxCalls=%d}",
        getFailureThresholdPercentage(), getSuccessThresholdPercentage(),
        minimumNumberOfCalls, getWaitDurationInOpenStateSeconds(), halfOpenMaxCalls);
  }
}
