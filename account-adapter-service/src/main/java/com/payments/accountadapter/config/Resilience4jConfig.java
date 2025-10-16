package com.payments.accountadapter.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.Duration;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j Configuration
 *
 * <p>Configures resilience patterns for account adapter: - Circuit breaker configuration - Retry
 * configuration - Timeout configuration - Event handling
 */
@Slf4j
@Configuration
public class Resilience4jConfig {

  /** Circuit Breaker Configuration */
  @Bean
  public CircuitBreakerConfig circuitBreakerConfig() {
    return CircuitBreakerConfig.custom()
        .slidingWindowSize(10)
        .minimumNumberOfCalls(5)
        .permittedNumberOfCallsInHalfOpenState(3)
        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
        .waitDurationInOpenState(Duration.ofSeconds(10))
        .failureRateThreshold(50)
        .slowCallRateThreshold(50)
        .slowCallDurationThreshold(Duration.ofSeconds(2))
        .recordExceptions(
            java.io.IOException.class,
            java.util.concurrent.TimeoutException.class,
            java.util.concurrent.CompletionException.class,
            feign.FeignException.class,
            com.payments.accountadapter.dto.AccountServiceException.class)
        .ignoreExceptions(com.payments.accountadapter.dto.AccountServiceException.class)
        .build();
  }

  /** Circuit Breaker Registry */
  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig circuitBreakerConfig) {
    CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(circuitBreakerConfig);

    // Register event consumers
    registry
        .getEventPublisher()
        .onEntryAdded(
            event -> {
              log.info("Circuit breaker entry added: {}", event.getAddedEntry().getName());
            });

    registry
        .getEventPublisher()
        .onEntryRemoved(
            event -> {
              log.info("Circuit breaker entry removed: {}", event.getRemovedEntry().getName());
            });

    return registry;
  }

  /** Account Service Circuit Breaker */
  @Bean
  public CircuitBreaker accountServiceCircuitBreaker(CircuitBreakerRegistry registry) {
    CircuitBreaker circuitBreaker = registry.circuitBreaker("account-service");

    // Register event consumers
    circuitBreaker
        .getEventPublisher()
        .onStateTransition(
            event -> {
              log.info(
                  "Circuit breaker state transition: {} -> {}",
                  event.getStateTransition().getFromState(),
                  event.getStateTransition().getToState());
            })
        .onFailureRateExceeded(
            event -> {
              log.warn("Circuit breaker failure rate exceeded: {}%", event.getFailureRate());
            })
        .onSlowCallRateExceeded(
            event -> {
              log.warn("Circuit breaker slow call rate exceeded: {}%", event.getSlowCallRate());
            })
        .onCallNotPermitted(
            event -> {
              log.warn("Circuit breaker call not permitted: {}", event.getEventType());
            });

    return circuitBreaker;
  }

  /** Retry Configuration */
  @Bean
  public RetryConfig retryConfig() {
    // Use IntervalFunction for exponential backoff with multiplier
    io.github.resilience4j.core.IntervalFunction intervalFunction =
        io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
            Duration.ofSeconds(1).toMillis(), 2.0);

    return RetryConfig.custom()
        .maxAttempts(3)
        .intervalFunction(intervalFunction)
        .retryExceptions(
            java.io.IOException.class,
            java.util.concurrent.TimeoutException.class,
            feign.FeignException.class)
        .ignoreExceptions(com.payments.accountadapter.dto.AccountServiceException.class)
        .build();
  }

  /** Retry Registry */
  @Bean
  public RetryRegistry retryRegistry(RetryConfig retryConfig) {
    RetryRegistry registry = RetryRegistry.of(retryConfig);

    // Register event consumers
    registry
        .getEventPublisher()
        .onEntryAdded(
            event -> {
              log.info("Retry entry added: {}", event.getAddedEntry().getName());
            });

    registry
        .getEventPublisher()
        .onEntryRemoved(
            event -> {
              log.info("Retry entry removed: {}", event.getRemovedEntry().getName());
            });

    return registry;
  }

  /** Account Service Retry */
  @Bean
  public Retry accountServiceRetry(RetryRegistry registry) {
    Retry retry = registry.retry("account-service");

    // Register event consumers
    retry
        .getEventPublisher()
        .onRetry(
            event -> {
              log.info(
                  "Retry attempt: {} for {}",
                  event.getNumberOfRetryAttempts(),
                  event.getEventType());
            })
        .onSuccess(
            event -> {
              log.info("Retry succeeded after {} attempts", event.getNumberOfRetryAttempts());
            })
        .onError(
            event -> {
              log.error(
                  "Retry error after {} attempts",
                  event.getNumberOfRetryAttempts(),
                  event.getLastThrowable());
            });

    return retry;
  }

  /** Time Limiter Configuration */
  @Bean
  public TimeLimiterConfig timeLimiterConfig() {
    return TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(30))
        .cancelRunningFuture(true)
        .build();
  }

  /** Time Limiter Registry */
  @Bean
  public TimeLimiterRegistry timeLimiterRegistry(TimeLimiterConfig timeLimiterConfig) {
    TimeLimiterRegistry registry = TimeLimiterRegistry.of(timeLimiterConfig);

    // Register event consumers
    registry
        .getEventPublisher()
        .onEntryAdded(
            event -> {
              log.info("Time limiter entry added: {}", event.getAddedEntry().getName());
            });

    registry
        .getEventPublisher()
        .onEntryRemoved(
            event -> {
              log.info("Time limiter entry removed: {}", event.getRemovedEntry().getName());
            });

    return registry;
  }

  /** Account Service Time Limiter */
  @Bean
  public TimeLimiter accountServiceTimeLimiter(TimeLimiterRegistry registry) {
    TimeLimiter timeLimiter = registry.timeLimiter("account-service");

    // Register event consumers
    timeLimiter
        .getEventPublisher()
        .onTimeout(
            event -> {
              log.warn("Time limiter timeout: {}", event.getEventType());
            })
        .onSuccess(
            event -> {
              log.debug("Time limiter success: {}", event.getEventType());
            });

    return timeLimiter;
  }

  /** Circuit Breaker State Transition Handler */
  @Bean
  public Function<CircuitBreaker.StateTransition, Void> circuitBreakerStateTransitionHandler() {
    return stateTransition -> {
      log.info(
          "Circuit breaker state transition: {} -> {}",
          stateTransition.getFromState(),
          stateTransition.getToState());
      return null;
    };
  }
}
