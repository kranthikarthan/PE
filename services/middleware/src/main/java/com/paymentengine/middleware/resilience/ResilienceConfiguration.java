package com.paymentengine.middleware.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Configuration for Resilience4j patterns
 */
@Configuration
public class ResilienceConfiguration {

    /**
     * Circuit Breaker for clearing system interactions
     */
    @Bean
    public CircuitBreaker clearingSystemCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return CircuitBreaker.of("clearingSystem", config);
    }

    /**
     * Circuit Breaker for webhook deliveries
     */
    @Bean
    public CircuitBreaker webhookCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return CircuitBreaker.of("webhook", config);
    }

    /**
     * Circuit Breaker for Kafka operations
     */
    @Bean
    public CircuitBreaker kafkaCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(15)
                .minimumNumberOfCalls(8)
                .failureRateThreshold(40)
                .waitDurationInOpenState(Duration.ofSeconds(45))
                .permittedNumberOfCallsInHalfOpenState(4)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return CircuitBreaker.of("kafka", config);
    }

    /**
     * Retry configuration for clearing system calls
     */
    @Bean
    public Retry clearingSystemRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryOnException(throwable -> !(throwable instanceof IllegalArgumentException))
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return Retry.of("clearingSystem", config);
    }

    /**
     * Retry configuration for webhook deliveries
     */
    @Bean
    public Retry webhookRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofMillis(2000))
                .retryOnException(throwable -> !(throwable instanceof IllegalArgumentException))
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return Retry.of("webhook", config);
    }

    /**
     * Time limiter for clearing system calls
     */
    @Bean
    public TimeLimiter clearingSystemTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiter.of("clearingSystem", config);
    }

    /**
     * Time limiter for webhook deliveries
     */
    @Bean
    public TimeLimiter webhookTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiter.of("webhook", config);
    }

    /**
     * Bulkhead for clearing system calls
     */
    @Bean
    public Bulkhead clearingSystemBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .maxWaitDuration(Duration.ofMillis(1000))
                .build();

        return Bulkhead.of("clearingSystem", config);
    }

    /**
     * Bulkhead for webhook deliveries
     */
    @Bean
    public Bulkhead webhookBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(20)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();

        return Bulkhead.of("webhook", config);
    }

    /**
     * Rate limiter for API calls
     */
    @Bean
    public RateLimiter apiRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(1000))
                .build();

        return RateLimiter.of("api", config);
    }

    /**
     * Rate limiter for clearing system calls
     */
    @Bean
    public RateLimiter clearingSystemRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(50)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(2000))
                .build();

        return RateLimiter.of("clearingSystem", config);
    }

    // ============================================================================
    // FRAUD API RESILIENCY PATTERNS (MISSING GAP)
    // ============================================================================

    /**
     * Circuit Breaker for fraud API calls
     */
    @Bean
    public CircuitBreaker fraudApiCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return CircuitBreaker.of("fraudApi", config);
    }

    /**
     * Retry configuration for fraud API calls
     */
    @Bean
    public Retry fraudApiRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .exponentialBackoffMultiplier(2.0)
                .maxWaitDuration(Duration.ofSeconds(30))
                .retryOnException(throwable -> !(throwable instanceof IllegalArgumentException))
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return Retry.of("fraudApi", config);
    }

    /**
     * Time limiter for fraud API calls
     */
    @Bean
    public TimeLimiter fraudApiTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiter.of("fraudApi", config);
    }

    /**
     * Bulkhead for fraud API calls
     */
    @Bean
    public Bulkhead fraudApiBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(25)
                .maxWaitDuration(Duration.ofMillis(1000))
                .build();

        return Bulkhead.of("fraudApi", config);
    }

    // ============================================================================
    // CORE BANKING RESILIENCY PATTERNS (MISSING GAP)
    // ============================================================================

    /**
     * Circuit Breaker for core banking debit operations
     */
    @Bean
    public CircuitBreaker coreBankingDebitCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(30)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return CircuitBreaker.of("coreBankingDebit", config);
    }

    /**
     * Circuit Breaker for core banking credit operations
     */
    @Bean
    public CircuitBreaker coreBankingCreditCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(30)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return CircuitBreaker.of("coreBankingCredit", config);
    }

    /**
     * Retry configuration for core banking operations
     */
    @Bean
    public Retry coreBankingRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofMillis(2000))
                .exponentialBackoffMultiplier(1.5)
                .maxWaitDuration(Duration.ofSeconds(60))
                .retryOnException(throwable -> !(throwable instanceof IllegalArgumentException))
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return Retry.of("coreBanking", config);
    }

    /**
     * Time limiter for core banking operations
     */
    @Bean
    public TimeLimiter coreBankingTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(60))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiter.of("coreBanking", config);
    }

    /**
     * Bulkhead for core banking operations
     */
    @Bean
    public Bulkhead coreBankingBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(50)
                .maxWaitDuration(Duration.ofMillis(2000))
                .build();

        return Bulkhead.of("coreBanking", config);
    }
}