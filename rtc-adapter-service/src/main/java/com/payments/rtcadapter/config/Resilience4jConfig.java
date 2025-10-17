package com.payments.rtcadapter.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j configuration for RTC adapter
 */
@Configuration
public class Resilience4jConfig {

  @Bean
  public CircuitBreaker rtcAdapterCircuitBreaker() {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofSeconds(30))
        .slidingWindowSize(10)
        .minimumNumberOfCalls(5)
        .permittedNumberOfCallsInHalfOpenState(3)
        .build();

    return CircuitBreaker.of("rtc-adapter", config);
  }

  @Bean
  public Retry rtcAdapterRetry() {
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofSeconds(1))
        .retryOnException(throwable -> true)
        .build();

    return Retry.of("rtc-adapter", config);
  }

  @Bean
  public TimeLimiterConfig rtcAdapterTimeLimiter() {
    return TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(30))
        .build();
  }
}
