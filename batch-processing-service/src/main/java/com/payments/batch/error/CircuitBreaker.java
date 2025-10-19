package com.payments.batch.error;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

/**
 * Circuit breaker implementation for batch processing operations.
 *
 * <p>This circuit breaker provides protection against cascading failures by
 * monitoring operation success rates and automatically opening the circuit
 * when failure thresholds are exceeded. It supports multiple states and
 * automatic recovery mechanisms.
 *
 * <p><b>Circuit Breaker States:</b>
 * <ul>
 *   <li>CLOSED - Normal operation, requests pass through
 *   <li>OPEN - Circuit is open, requests are rejected immediately
   * <li>HALF_OPEN - Testing state, limited requests allowed
   * </ul>
 *
 * <p><b>Configuration Options:</b>
 * <ul>
 *   <li>Failure threshold percentage
 *   <li>Minimum number of calls before opening
 *   <li>Timeout duration for open state
 *   <li>Success threshold for closing
 * </ul>
 *
 * @since PE-404
 */
@Slf4j
public class CircuitBreaker {
  
  /** Circuit breaker state */
  private final AtomicReference<CircuitState> state = new AtomicReference<>(CircuitState.CLOSED);
  
  /** Total number of calls */
  private final AtomicLong totalCalls = new AtomicLong(0);
  
  /** Number of successful calls */
  private final AtomicLong successfulCalls = new AtomicLong(0);
  
  /** Number of failed calls */
  private final AtomicLong failedCalls = new AtomicLong(0);
  
  /** Number of calls in half-open state */
  private final AtomicInteger halfOpenCalls = new AtomicInteger(0);
  
  /** Time when circuit was opened */
  private volatile LocalDateTime openedAt;
  
  /** Time when circuit was last reset */
  private volatile LocalDateTime lastResetAt;
  
  /** Circuit breaker configuration */
  private final CircuitBreakerConfig config;
  
  /** Circuit breaker name for logging */
  private final String name;
  
  /**
   * Creates a circuit breaker with the specified configuration.
   *
   * @param name the circuit breaker name
   * @param config the circuit breaker configuration
   */
  public CircuitBreaker(String name, CircuitBreakerConfig config) {
    this.name = name;
    this.config = config;
    this.lastResetAt = LocalDateTime.now();
  }
  
  /**
   * Creates a circuit breaker with default configuration.
   *
   * @param name the circuit breaker name
   * @return circuit breaker with default config
   */
  public static CircuitBreaker withDefaultConfig(String name) {
    return new CircuitBreaker(name, CircuitBreakerConfig.defaultConfig());
  }
  
  /**
   * Creates a circuit breaker for batch processing operations.
   *
   * @param name the circuit breaker name
   * @return circuit breaker for batch processing
   */
  public static CircuitBreaker forBatchProcessing(String name) {
    return new CircuitBreaker(name, CircuitBreakerConfig.batchProcessingConfig());
  }
  
  /**
   * Creates a circuit breaker for external service calls.
   *
   * @param name the circuit breaker name
   * @return circuit breaker for external services
   */
  public static CircuitBreaker forExternalService(String name) {
    return new CircuitBreaker(name, CircuitBreakerConfig.externalServiceConfig());
  }
  
  /**
   * Executes an operation through the circuit breaker.
   *
   * @param operation the operation to execute
   * @return operation result
   * @throws CircuitBreakerOpenException if circuit is open
   * @throws Exception if operation fails
   */
  public <T> T execute(CircuitBreakerOperation<T> operation) throws Exception {
    if (!allowRequest()) {
      throw new CircuitBreakerOpenException("Circuit breaker is open: " + name);
    }
    
    try {
      T result = operation.execute();
      onSuccess();
      return result;
    } catch (Exception e) {
      onFailure();
      throw e;
    }
  }
  
  /**
   * Checks if a request should be allowed through the circuit breaker.
   *
   * @return true if request should be allowed, false otherwise
   */
  public boolean allowRequest() {
    CircuitState currentState = state.get();
    
    switch (currentState) {
      case CLOSED:
        return true;
        
      case OPEN:
        if (shouldAttemptReset()) {
          if (state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
            log.info("Circuit breaker {} transitioning to HALF_OPEN", name);
            halfOpenCalls.set(0);
          }
          return true;
        }
        return false;
        
      case HALF_OPEN:
        return halfOpenCalls.get() < config.getHalfOpenMaxCalls();
        
      default:
        return false;
    }
  }
  
  /**
   * Records a successful operation.
   */
  public void onSuccess() {
    totalCalls.incrementAndGet();
    successfulCalls.incrementAndGet();
    
    if (state.get() == CircuitState.HALF_OPEN) {
      int calls = halfOpenCalls.incrementAndGet();
      if (calls >= config.getHalfOpenMaxCalls()) {
        if (state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.CLOSED)) {
          log.info("Circuit breaker {} transitioning to CLOSED", name);
          lastResetAt = LocalDateTime.now();
        }
      }
    } else {
      checkAndCloseCircuit();
    }
  }
  
  /**
   * Records a failed operation.
   */
  public void onFailure() {
    totalCalls.incrementAndGet();
    failedCalls.incrementAndGet();
    
    if (state.get() == CircuitState.HALF_OPEN) {
      if (state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.OPEN)) {
        log.warn("Circuit breaker {} transitioning to OPEN from HALF_OPEN", name);
        openedAt = LocalDateTime.now();
      }
    } else {
      checkAndOpenCircuit();
    }
  }
  
  /**
   * Checks if the circuit should be opened based on failure rate.
   */
  private void checkAndOpenCircuit() {
    if (totalCalls.get() >= config.getMinimumNumberOfCalls()) {
      double failureRate = calculateFailureRate();
      if (failureRate >= config.getFailureThreshold()) {
        if (state.compareAndSet(CircuitState.CLOSED, CircuitState.OPEN)) {
          log.warn("Circuit breaker {} opening due to failure rate: {:.2f}%", name, failureRate * 100);
          openedAt = LocalDateTime.now();
        }
      }
    }
  }
  
  /**
   * Checks if the circuit should be closed based on success rate.
   */
  private void checkAndCloseCircuit() {
    if (totalCalls.get() >= config.getMinimumNumberOfCalls()) {
      double successRate = calculateSuccessRate();
      if (successRate >= config.getSuccessThreshold()) {
        if (state.compareAndSet(CircuitState.OPEN, CircuitState.CLOSED)) {
          log.info("Circuit breaker {} closing due to success rate: {:.2f}%", name, successRate * 100);
          lastResetAt = LocalDateTime.now();
        }
      }
    }
  }
  
  /**
   * Checks if the circuit should attempt to reset from open state.
   *
   * @return true if should attempt reset, false otherwise
   */
  private boolean shouldAttemptReset() {
    if (openedAt == null) {
      return false;
    }
    
    Duration timeSinceOpened = Duration.between(openedAt, LocalDateTime.now());
    return timeSinceOpened.compareTo(config.getWaitDurationInOpenState()) >= 0;
  }
  
  /**
   * Calculates the current failure rate.
   *
   * @return failure rate as a decimal (0.0 to 1.0)
   */
  public double calculateFailureRate() {
    long total = totalCalls.get();
    if (total == 0) {
      return 0.0;
    }
    return (double) failedCalls.get() / total;
  }
  
  /**
   * Calculates the current success rate.
   *
   * @return success rate as a decimal (0.0 to 1.0)
   */
  public double calculateSuccessRate() {
    long total = totalCalls.get();
    if (total == 0) {
      return 1.0;
    }
    return (double) successfulCalls.get() / total;
  }
  
  /**
   * Gets the current circuit breaker state.
   *
   * @return current state
   */
  public CircuitState getState() {
    return state.get();
  }
  
  /**
   * Gets the circuit breaker statistics.
   *
   * @return circuit breaker statistics
   */
  public CircuitBreakerStats getStats() {
    return new CircuitBreakerStats(
        name,
        getState(),
        totalCalls.get(),
        successfulCalls.get(),
        failedCalls.get(),
        calculateSuccessRate(),
        calculateFailureRate(),
        openedAt,
        lastResetAt
    );
  }
  
  /**
   * Resets the circuit breaker to initial state.
   */
  public void reset() {
    state.set(CircuitState.CLOSED);
    totalCalls.set(0);
    successfulCalls.set(0);
    failedCalls.set(0);
    halfOpenCalls.set(0);
    openedAt = null;
    lastResetAt = LocalDateTime.now();
    log.info("Circuit breaker {} reset to initial state", name);
  }
  
  /**
   * Forces the circuit breaker to open state.
   */
  public void forceOpen() {
    state.set(CircuitState.OPEN);
    openedAt = LocalDateTime.now();
    log.warn("Circuit breaker {} forced to OPEN state", name);
  }
  
  /**
   * Forces the circuit breaker to closed state.
   */
  public void forceClosed() {
    state.set(CircuitState.CLOSED);
    lastResetAt = LocalDateTime.now();
    log.info("Circuit breaker {} forced to CLOSED state", name);
  }
  
  /**
   * Gets the circuit breaker name.
   *
   * @return circuit breaker name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Checks if the circuit breaker is open.
   *
   * @return true if open, false otherwise
   */
  public boolean isOpen() {
    return state.get() == CircuitState.OPEN;
  }
  
  /**
   * Checks if the circuit breaker is closed.
   *
   * @return true if closed, false otherwise
   */
  public boolean isClosed() {
    return state.get() == CircuitState.CLOSED;
  }
  
  /**
   * Checks if the circuit breaker is half-open.
   *
   * @return true if half-open, false otherwise
   */
  public boolean isHalfOpen() {
    return state.get() == CircuitState.HALF_OPEN;
  }
  
  /**
   * Enumeration of circuit breaker states.
   */
  public enum CircuitState {
    /** Circuit is closed, requests pass through */
    CLOSED,
    
    /** Circuit is open, requests are rejected */
    OPEN,
    
    /** Circuit is half-open, limited requests allowed */
    HALF_OPEN
  }
  
  /**
   * Functional interface for circuit breaker operations.
   */
  @FunctionalInterface
  public interface CircuitBreakerOperation<T> {
    T execute() throws Exception;
  }
}
