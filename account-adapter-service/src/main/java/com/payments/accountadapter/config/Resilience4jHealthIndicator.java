package com.payments.accountadapter.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Resilience4j Health Indicator
 *
 * <p>Health indicator for Resilience4j components: - Circuit breaker health - Retry health - Time
 * limiter health - Overall resilience health
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Resilience4jHealthIndicator implements HealthIndicator {

  private final CircuitBreakerRegistry circuitBreakerRegistry;
  private final RetryRegistry retryRegistry;
  private final TimeLimiterRegistry timeLimiterRegistry;

  @Override
  public Health health() {
    try {
      Map<String, Object> details = new HashMap<>();
      boolean isHealthy = true;

      // Check circuit breaker health
      Map<String, Object> circuitBreakerHealth = checkCircuitBreakerHealth();
      details.put("circuitBreaker", circuitBreakerHealth);
      if (!(Boolean) circuitBreakerHealth.get("healthy")) {
        isHealthy = false;
      }

      // Check retry health
      Map<String, Object> retryHealth = checkRetryHealth();
      details.put("retry", retryHealth);
      if (!(Boolean) retryHealth.get("healthy")) {
        isHealthy = false;
      }

      // Check time limiter health
      Map<String, Object> timeLimiterHealth = checkTimeLimiterHealth();
      details.put("timeLimiter", timeLimiterHealth);
      if (!(Boolean) timeLimiterHealth.get("healthy")) {
        isHealthy = false;
      }

      // Overall health
      details.put("overall", Map.of("healthy", isHealthy, "timestamp", System.currentTimeMillis()));

      if (isHealthy) {
        return Health.up().withDetails(details).build();
      } else {
        return Health.down().withDetails(details).build();
      }

    } catch (Exception e) {
      log.error("Error checking Resilience4j health", e);
      return Health.down().withDetail("error", e.getMessage()).build();
    }
  }

  /** Check circuit breaker health */
  private Map<String, Object> checkCircuitBreakerHealth() {
    Map<String, Object> health = new HashMap<>();
    boolean isHealthy = true;

    try {
      CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("account-service");

      health.put("state", circuitBreaker.getState().name());
      health.put("failureRate", circuitBreaker.getMetrics().getFailureRate());
      health.put("slowCallRate", circuitBreaker.getMetrics().getSlowCallRate());
      health.put("numberOfBufferedCalls", circuitBreaker.getMetrics().getNumberOfBufferedCalls());
      health.put(
          "numberOfSuccessfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
      health.put("numberOfFailedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls());
      health.put("numberOfSlowCalls", circuitBreaker.getMetrics().getNumberOfSlowCalls());

      // Circuit breaker is healthy if it's not in OPEN state
      isHealthy = circuitBreaker.getState() != CircuitBreaker.State.OPEN;

    } catch (Exception e) {
      log.error("Error checking circuit breaker health", e);
      isHealthy = false;
      health.put("error", e.getMessage());
    }

    health.put("healthy", isHealthy);
    return health;
  }

  /** Check retry health */
  private Map<String, Object> checkRetryHealth() {
    Map<String, Object> health = new HashMap<>();
    boolean isHealthy = true;

    try {
      Retry retry = retryRegistry.retry("account-service");

      health.put("name", retry.getName());
      health.put("maxAttempts", retry.getRetryConfig().getMaxAttempts());
      // Expose interval function as string due to API differences
      health.put("intervalFunction", String.valueOf(retry.getRetryConfig().getIntervalFunction()));

      // Retry is always healthy (it's a passive component)
      isHealthy = true;

    } catch (Exception e) {
      log.error("Error checking retry health", e);
      isHealthy = false;
      health.put("error", e.getMessage());
    }

    health.put("healthy", isHealthy);
    return health;
  }

  /** Check time limiter health */
  private Map<String, Object> checkTimeLimiterHealth() {
    Map<String, Object> health = new HashMap<>();
    boolean isHealthy = true;

    try {
      TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("account-service");

      health.put("name", timeLimiter.getName());
      health.put("timeoutDuration", timeLimiter.getTimeLimiterConfig().getTimeoutDuration());
      health.put(
          "cancelRunningFuture", timeLimiter.getTimeLimiterConfig().shouldCancelRunningFuture());

      // Time limiter is always healthy (it's a passive component)
      isHealthy = true;

    } catch (Exception e) {
      log.error("Error checking time limiter health", e);
      isHealthy = false;
      health.put("error", e.getMessage());
    }

    health.put("healthy", isHealthy);
    return health;
  }
}
