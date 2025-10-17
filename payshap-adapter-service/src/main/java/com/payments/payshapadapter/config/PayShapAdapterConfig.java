package com.payments.payshapadapter.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for PayShap Adapter */
@Configuration
public class PayShapAdapterConfig {

  /** Circuit breaker configuration for PayShap operations */
  @Bean
  public CircuitBreaker payShapCircuitBreaker() {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .build();

    return CircuitBreaker.of("payshap-adapter", config);
  }

  /** Retry configuration for PayShap operations */
  @Bean
  public Retry payShapRetry() {
    RetryConfig config =
        RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .retryExceptions(
                java.net.ConnectException.class,
                java.net.SocketTimeoutException.class,
                org.springframework.web.client.ResourceAccessException.class)
            .build();

    return Retry.of("payshap-adapter", config);
  }

  /** Time limiter configuration for PayShap operations */
  @Bean
  public TimeLimiter payShapTimeLimiter() {
    TimeLimiterConfig config =
        TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(5)).build();

    return TimeLimiter.of("payshap-adapter", config);
  }

  /** Circuit breaker configuration for Proxy Registry operations */
  @Bean
  public CircuitBreaker proxyRegistryCircuitBreaker() {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .failureRateThreshold(40)
            .waitDurationInOpenState(Duration.ofSeconds(20))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(3)
            .permittedNumberOfCallsInHalfOpenState(2)
            .build();

    return CircuitBreaker.of("proxy-registry", config);
  }

  /** Retry configuration for Proxy Registry operations */
  @Bean
  public Retry proxyRegistryRetry() {
    RetryConfig config =
        RetryConfig.custom()
            .maxAttempts(2)
            .waitDuration(Duration.ofMillis(500))
            .retryExceptions(
                java.net.ConnectException.class,
                java.net.SocketTimeoutException.class,
                org.springframework.web.client.ResourceAccessException.class)
            .build();

    return Retry.of("proxy-registry", config);
  }

  /** Time limiter configuration for Proxy Registry operations */
  @Bean
  public TimeLimiter proxyRegistryTimeLimiter() {
    TimeLimiterConfig config =
        TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(3)).build();

    return TimeLimiter.of("proxy-registry", config);
  }
}
