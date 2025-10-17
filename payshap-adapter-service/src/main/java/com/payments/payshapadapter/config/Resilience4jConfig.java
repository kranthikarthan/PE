package com.payments.payshapadapter.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j configuration for PayShap adapter
 */
@Configuration
public class Resilience4jConfig {

  @Bean
  public CircuitBreaker payshapAdapterCircuitBreaker() {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofSeconds(30))
        .slidingWindowSize(10)
        .minimumNumberOfCalls(5)
        .permittedNumberOfCallsInHalfOpenState(3)
        .build();

    return CircuitBreaker.of("payshap-adapter", config);
  }

  @Bean
  public Retry payshapAdapterRetry() {
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofSeconds(1))
        .retryOnException(throwable -> true)
        .build();

    return Retry.of("payshap-adapter", config);
  }

  @Bean
  public TimeLimiterConfig payshapAdapterTimeLimiter() {
    return TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(30))
        .build();
  }
}
