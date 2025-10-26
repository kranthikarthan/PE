package com.payments.paymentinitiation.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j Configuration
 *
 * <p>Configures circuit breakers and time limiters for the payment initiation service. This ensures
 * that Resilience4j annotations work properly and the fallback methods are correctly wired.
 */
@Configuration
public class Resilience4jConfig {

  /** Circuit breaker registry bean */
  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry() {
    return CircuitBreakerRegistry.ofDefaults();
  }

  /** Time limiter registry bean */
  @Bean
  public TimeLimiterRegistry timeLimiterRegistry() {
    return TimeLimiterRegistry.ofDefaults();
  }
}
