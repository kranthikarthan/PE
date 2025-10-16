package com.payments.accountadapter.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate Configuration
 *
 * <p>Configuration for RestTemplate with resilience patterns: - Circuit breaker integration - Retry
 * integration - Timeout configuration - Error handling
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

  private final CircuitBreakerRegistry circuitBreakerRegistry;
  private final RetryRegistry retryRegistry;
  private final TimeLimiterRegistry timeLimiterRegistry;

  /** RestTemplate with resilience patterns */
  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();

    // Configure request factory with timeouts
    ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    ((SimpleClientHttpRequestFactory) requestFactory).setConnectTimeout(10000);
    ((SimpleClientHttpRequestFactory) requestFactory).setReadTimeout(30000);
    restTemplate.setRequestFactory(requestFactory);

    log.info("RestTemplate configured with resilience patterns");
    return restTemplate;
  }

  /** Circuit breaker for RestTemplate */
  @Bean
  public CircuitBreaker restTemplateCircuitBreaker() {
    return circuitBreakerRegistry.circuitBreaker("rest-template");
  }

  /** Retry for RestTemplate */
  @Bean
  public Retry restTemplateRetry() {
    return retryRegistry.retry("rest-template");
  }

  /** Time limiter for RestTemplate */
  @Bean
  public TimeLimiter restTemplateTimeLimiter() {
    return timeLimiterRegistry.timeLimiter("rest-template");
  }
}
