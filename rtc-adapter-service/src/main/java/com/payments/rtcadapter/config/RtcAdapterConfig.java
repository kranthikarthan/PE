package com.payments.rtcadapter.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for RTC Adapter */
@Configuration
public class RtcAdapterConfig {

  /** Circuit breaker configuration for RTC operations */
  @Bean
  public CircuitBreaker rtcCircuitBreaker() {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .build();

    return CircuitBreaker.of("rtc-adapter", config);
  }

  /** Retry configuration for RTC operations */
  @Bean
  public Retry rtcRetry() {
    RetryConfig config =
        RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .retryOnException(throwable -> true)
            .build();

    return Retry.of("rtc-adapter", config);
  }

  /** Time limiter configuration for RTC operations */
  @Bean
  public TimeLimiter rtcTimeLimiter() {
    TimeLimiterConfig config =
        TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(10)).build();

    return TimeLimiter.of("rtc-adapter", config);
  }
}
