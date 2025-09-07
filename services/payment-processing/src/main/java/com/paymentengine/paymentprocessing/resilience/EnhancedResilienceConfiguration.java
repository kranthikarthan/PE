package com.paymentengine.paymentprocessing.resilience;

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

@Configuration
public class EnhancedResilienceConfiguration {
    
    // Circuit Breaker Configurations
    @Bean
    public CircuitBreaker clearingSystemCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        
        return CircuitBreaker.of("clearing-system", config);
    }
    
    @Bean
    public CircuitBreaker authServiceCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(40)
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .slidingWindowSize(8)
                .minimumNumberOfCalls(3)
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        
        return CircuitBreaker.of("auth-service", config);
    }
    
    @Bean
    public CircuitBreaker configServiceCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(45)
                .waitDurationInOpenState(Duration.ofSeconds(25))
                .slidingWindowSize(8)
                .minimumNumberOfCalls(3)
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        
        return CircuitBreaker.of("config-service", config);
    }
    
    @Bean
    public CircuitBreaker webhookCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofSeconds(45))
                .slidingWindowSize(12)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        
        return CircuitBreaker.of("webhook", config);
    }
    
    @Bean
    public CircuitBreaker kafkaCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(55)
                .waitDurationInOpenState(Duration.ofSeconds(35))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(4)
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        
        return CircuitBreaker.of("kafka", config);
    }
    
    // Retry Configurations
    @Bean
    public Retry clearingSystemRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .exponentialBackoffMultiplier(2)
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        
        return Retry.of("clearing-system", config);
    }
    
    @Bean
    public Retry authServiceRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(500))
                .exponentialBackoffMultiplier(1.5)
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        
        return Retry.of("auth-service", config);
    }
    
    @Bean
    public Retry configServiceRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(750))
                .exponentialBackoffMultiplier(1.5)
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        
        return Retry.of("config-service", config);
    }
    
    @Bean
    public Retry webhookRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(4)
                .waitDuration(Duration.ofSeconds(2))
                .exponentialBackoffMultiplier(2)
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        
        return Retry.of("webhook", config);
    }
    
    @Bean
    public Retry kafkaRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .exponentialBackoffMultiplier(2)
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
        
        return Retry.of("kafka", config);
    }
    
    // Time Limiter Configurations
    @Bean
    public TimeLimiter clearingSystemTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .cancelRunningFuture(true)
                .build();
        
        return TimeLimiter.of("clearing-system", config);
    }
    
    @Bean
    public TimeLimiter authServiceTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(15))
                .cancelRunningFuture(true)
                .build();
        
        return TimeLimiter.of("auth-service", config);
    }
    
    @Bean
    public TimeLimiter configServiceTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(20))
                .cancelRunningFuture(true)
                .build();
        
        return TimeLimiter.of("config-service", config);
    }
    
    @Bean
    public TimeLimiter webhookTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(45))
                .cancelRunningFuture(true)
                .build();
        
        return TimeLimiter.of("webhook", config);
    }
    
    @Bean
    public TimeLimiter kafkaTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(25))
                .cancelRunningFuture(true)
                .build();
        
        return TimeLimiter.of("kafka", config);
    }
    
    // Bulkhead Configurations
    @Bean
    public Bulkhead clearingSystemBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(20)
                .maxWaitDuration(Duration.ofSeconds(2))
                .build();
        
        return Bulkhead.of("clearing-system", config);
    }
    
    @Bean
    public Bulkhead authServiceBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(15)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();
        
        return Bulkhead.of("auth-service", config);
    }
    
    @Bean
    public Bulkhead configServiceBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(15)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();
        
        return Bulkhead.of("config-service", config);
    }
    
    @Bean
    public Bulkhead webhookBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .maxWaitDuration(Duration.ofSeconds(3))
                .build();
        
        return Bulkhead.of("webhook", config);
    }
    
    @Bean
    public Bulkhead kafkaBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(25)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();
        
        return Bulkhead.of("kafka", config);
    }
    
    // Rate Limiter Configurations
    @Bean
    public RateLimiter apiRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofSeconds(1))
                .build();
        
        return RateLimiter.of("api", config);
    }
    
    @Bean
    public RateLimiter clearingSystemRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(50)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofSeconds(2))
                .build();
        
        return RateLimiter.of("clearing-system", config);
    }
    
    @Bean
    public RateLimiter authServiceRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(200)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofSeconds(1))
                .build();
        
        return RateLimiter.of("auth-service", config);
    }
    
    @Bean
    public RateLimiter configServiceRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(150)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofSeconds(1))
                .build();
        
        return RateLimiter.of("config-service", config);
    }
}