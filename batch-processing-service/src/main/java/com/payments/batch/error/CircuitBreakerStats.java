package com.payments.batch.error;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Statistics for circuit breaker operations and state.
 *
 * <p>This class provides comprehensive statistics about circuit breaker
 * performance including call counts, success/failure rates, and timing
 * information for monitoring and analysis.
 *
 * @since PE-404
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBreakerStats {
  
  /** Circuit breaker name */
  private String name;
  
  /** Current circuit breaker state */
  private CircuitBreaker.CircuitState state;
  
  /** Total number of calls */
  private long totalCalls;
  
  /** Number of successful calls */
  private long successfulCalls;
  
  /** Number of failed calls */
  private long failedCalls;
  
  /** Current success rate (0.0 to 1.0) */
  private double successRate;
  
  /** Current failure rate (0.0 to 1.0) */
  private double failureRate;
  
  /** Time when circuit was opened */
  private LocalDateTime openedAt;
  
  /** Time when circuit was last reset */
  private LocalDateTime lastResetAt;
  
  /**
   * Gets the success rate as a percentage.
   *
   * @return success rate percentage
   */
  public double getSuccessRatePercentage() {
    return successRate * 100.0;
  }
  
  /**
   * Gets the failure rate as a percentage.
   *
   * @return failure rate percentage
   */
  public double getFailureRatePercentage() {
    return failureRate * 100.0;
  }
  
  /**
   * Gets the duration the circuit has been in its current state.
   *
   * @return duration in the current state
   */
  public java.time.Duration getCurrentStateDuration() {
    LocalDateTime referenceTime = switch (state) {
      case OPEN -> openedAt;
      case CLOSED, HALF_OPEN -> lastResetAt;
    };
    
    if (referenceTime == null) {
      return java.time.Duration.ZERO;
    }
    
    return java.time.Duration.between(referenceTime, LocalDateTime.now());
  }
  
  /**
   * Gets the duration the circuit has been open.
   *
   * @return duration in open state, or zero if not open
   */
  public java.time.Duration getOpenDuration() {
    if (state != CircuitBreaker.CircuitState.OPEN || openedAt == null) {
      return java.time.Duration.ZERO;
    }
    
    return java.time.Duration.between(openedAt, LocalDateTime.now());
  }
  
  /**
   * Gets the duration since the last reset.
   *
   * @return duration since last reset
   */
  public java.time.Duration getTimeSinceLastReset() {
    if (lastResetAt == null) {
      return java.time.Duration.ZERO;
    }
    
    return java.time.Duration.between(lastResetAt, LocalDateTime.now());
  }
  
  /**
   * Checks if the circuit breaker is healthy.
   *
   * @return true if healthy, false otherwise
   */
  public boolean isHealthy() {
    return state == CircuitBreaker.CircuitState.CLOSED && 
           successRate >= 0.8 && 
           failureRate <= 0.2;
  }
  
  /**
   * Checks if the circuit breaker is in a warning state.
   *
   * @return true if in warning state, false otherwise
   */
  public boolean isWarning() {
    return (state == CircuitBreaker.CircuitState.HALF_OPEN) ||
           (state == CircuitBreaker.CircuitState.CLOSED && 
            (successRate < 0.9 || failureRate > 0.1));
  }
  
  /**
   * Checks if the circuit breaker is in a critical state.
   *
   * @return true if in critical state, false otherwise
   */
  public boolean isCritical() {
    return state == CircuitBreaker.CircuitState.OPEN ||
           (state == CircuitBreaker.CircuitState.CLOSED && failureRate > 0.3);
  }
  
  /**
   * Gets the health status of the circuit breaker.
   *
   * @return health status
   */
  public HealthStatus getHealthStatus() {
    if (isCritical()) {
      return HealthStatus.CRITICAL;
    } else if (isWarning()) {
      return HealthStatus.WARNING;
    } else if (isHealthy()) {
      return HealthStatus.HEALTHY;
    } else {
      return HealthStatus.UNKNOWN;
    }
  }
  
  /**
   * Gets a formatted summary of the circuit breaker statistics.
   *
   * @return formatted summary
   */
  public String getSummary() {
    return String.format("CircuitBreakerStats{name='%s', state=%s, calls=%d, " +
        "successRate=%.1f%%, failureRate=%.1f%%, health=%s}",
        name, state, totalCalls, getSuccessRatePercentage(), 
        getFailureRatePercentage(), getHealthStatus());
  }
  
  /**
   * Gets detailed statistics as a formatted string.
   *
   * @return detailed statistics
   */
  public String getDetailedStats() {
    return String.format(
        "Circuit Breaker: %s\n" +
        "State: %s\n" +
        "Total Calls: %d\n" +
        "Successful Calls: %d\n" +
        "Failed Calls: %d\n" +
        "Success Rate: %.2f%%\n" +
        "Failure Rate: %.2f%%\n" +
        "Current State Duration: %s\n" +
        "Health Status: %s",
        name, state, totalCalls, successfulCalls, failedCalls,
        getSuccessRatePercentage(), getFailureRatePercentage(),
        getCurrentStateDuration(), getHealthStatus()
    );
  }
  
  /**
   * Enumeration of health status levels.
   */
  public enum HealthStatus {
    /** Circuit breaker is healthy */
    HEALTHY,
    
    /** Circuit breaker is in warning state */
    WARNING,
    
    /** Circuit breaker is in critical state */
    CRITICAL,
    
    /** Circuit breaker health is unknown */
    UNKNOWN
  }
  
  @Override
  public String toString() {
    return getSummary();
  }
}
