package com.paymentengine.paymentprocessing.service.impl;

import com.paymentengine.paymentprocessing.service.ResiliencyConfigurationService;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.concurrent.Callable;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class ResiliencyConfigurationServiceImpl implements ResiliencyConfigurationService {

    private final ApplicationContext applicationContext;

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    private final ConcurrentMap<String, ResiliencyPolicy> policyCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ServiceHealthStatus> healthByService = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    @Autowired
    public ResiliencyConfigurationServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        this.circuitBreakerRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(20)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build());

        this.retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build());

        this.timeLimiterRegistry = TimeLimiterRegistry.of(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .cancelRunningFuture(true)
                .build());

        this.bulkheadRegistry = BulkheadRegistry.of(BulkheadConfig.custom()
                .maxConcurrentCalls(20)
                .maxWaitDuration(Duration.ofSeconds(2))
                .build());

        this.rateLimiterRegistry = RateLimiterRegistry.of(RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(500))
                .build());
    }

    @Override
    public <T> T executeResilientCall(String serviceName,
                                      String tenantId,
                                      Supplier<T> primaryCall,
                                      Function<Throwable, T> fallback) {
        ResiliencyPolicy policy = resolvePolicy(serviceName);

        Supplier<T> decorated = primaryCall;
        if (policy.getRateLimiter() != null) {
            decorated = io.github.resilience4j.ratelimiter.RateLimiter.decorateSupplier(policy.getRateLimiter(), decorated);
        }
        if (policy.getBulkhead() != null) {
            decorated = io.github.resilience4j.bulkhead.Bulkhead.decorateSupplier(policy.getBulkhead(), decorated);
        }
        if (policy.getRetry() != null) {
            decorated = io.github.resilience4j.retry.Retry.decorateSupplier(policy.getRetry(), decorated);
        }
        if (policy.getCircuitBreaker() != null) {
            decorated = io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateSupplier(policy.getCircuitBreaker(), decorated);
        }
        // TimeLimiter supports decorating future suppliers; for sync, execute within timeout via scheduler if needed
        try {
            T result = decorated.get();
            recordSuccess(serviceName, tenantId, policy);
            return result;
        } catch (Throwable throwable) {
            recordFailure(serviceName, tenantId, throwable, policy);
            if (fallback != null) {
                return fallback.apply(throwable);
            }
            if (throwable instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new RuntimeException("Resilient call failed for service: " + serviceName, throwable);
        }
    }

    @Override
    public <T> CompletableFuture<T> executeResilientCallAsync(String serviceName,
                                                              String tenantId,
                                                              Supplier<CompletableFuture<T>> primaryCall,
                                                              Function<Throwable, T> fallback) {
        ResiliencyPolicy policy = resolvePolicy(serviceName);

        Supplier<CompletableFuture<T>> decoratedSupplier = primaryCall;
        if (policy.getRateLimiter() != null) {
            decoratedSupplier = io.github.resilience4j.ratelimiter.RateLimiter.decorateSupplier(policy.getRateLimiter(), decoratedSupplier);
        }
        if (policy.getBulkhead() != null) {
            decoratedSupplier = io.github.resilience4j.bulkhead.Bulkhead.decorateSupplier(policy.getBulkhead(), decoratedSupplier);
        }
        if (policy.getRetry() != null) {
            decoratedSupplier = io.github.resilience4j.retry.Retry.decorateSupplier(policy.getRetry(), decoratedSupplier);
        }
        if (policy.getCircuitBreaker() != null) {
            decoratedSupplier = io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateSupplier(policy.getCircuitBreaker(), decoratedSupplier);
        }
        // TimeLimiter for async: decorate future supplier
        if (policy.getTimeLimiter() != null) {
            decoratedSupplier = io.github.resilience4j.timelimiter.TimeLimiter.decorateFutureSupplier(policy.getTimeLimiter(), decoratedSupplier);
        }

        CompletableFuture<T> future;
        try {
            future = decoratedSupplier.get();
        } catch (Throwable throwable) {
            recordFailure(serviceName, tenantId, throwable, policy);
            if (fallback != null) {
                return CompletableFuture.completedFuture(fallback.apply(throwable));
            }
            CompletableFuture<T> failed = new CompletableFuture<>();
            failed.completeExceptionally(throwable);
            return failed;
        }

        return future.whenComplete((result, error) -> {
            if (error != null) {
                recordFailure(serviceName, tenantId, error, policy);
            } else {
                recordSuccess(serviceName, tenantId, policy);
            }
        }).handle((result, error) -> {
            if (error != null) {
                if (fallback != null) {
                    return fallback.apply(error);
                }
                if (error instanceof RuntimeException runtime) {
                    throw runtime;
                }
                throw new RuntimeException(error);
            }
            return result;
        });
    }

    @Override
    public Optional<ServiceHealthStatus> getServiceHealth(String serviceName) {
        updateMetrics(serviceName);
        return Optional.ofNullable(healthByService.get(serviceName));
    }

    @Override
    public List<ServiceHealthStatus> getAllServiceHealth() {
        policyCache.keySet().forEach(this::updateMetrics);
        return new ArrayList<>(healthByService.values());
    }

    private void recordSuccess(String serviceName, String tenantId, ResiliencyPolicy policy) {
        ServiceHealthStatus status = healthByService.computeIfAbsent(serviceName, ServiceHealthStatus::new);
        status.setLastTenantId(tenantId);
        status.setLastSuccessAt(Instant.now());
        status.setLastErrorMessage(null);
        status.setConsecutiveFailures(0);
        status.setStatus(ServiceHealthStatus.Status.HEALTHY);
        updateMetrics(status, policy);
    }

    private void recordFailure(String serviceName, String tenantId, Throwable throwable, ResiliencyPolicy policy) {
        ServiceHealthStatus status = healthByService.computeIfAbsent(serviceName, ServiceHealthStatus::new);
        status.setLastTenantId(tenantId);
        status.setLastFailureAt(Instant.now());
        status.setLastErrorMessage(throwable.getMessage());
        status.setConsecutiveFailures(status.getConsecutiveFailures() + 1);
        if (status.getConsecutiveFailures() >= 3) {
            status.setStatus(ServiceHealthStatus.Status.UNAVAILABLE);
        } else {
            status.setStatus(ServiceHealthStatus.Status.DEGRADED);
        }
        updateMetrics(status, policy);
    }

    private void updateMetrics(String serviceName) {
        ResiliencyPolicy policy = policyCache.get(serviceName);
        if (policy != null) {
            ServiceHealthStatus status = healthByService.computeIfAbsent(serviceName, ServiceHealthStatus::new);
            updateMetrics(status, policy);
        }
    }

    private void updateMetrics(ServiceHealthStatus status, ResiliencyPolicy policy) {
        if (policy == null) {
            return;
        }
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        if (policy.getCircuitBreaker() != null) {
            metrics.put("circuitBreakerState", policy.getCircuitBreaker().getState());
            metrics.put("circuitBreakerFailureRate", policy.getCircuitBreaker().getMetrics().getFailureRate());
        }
        if (policy.getRetry() != null) {
            metrics.put("retryMetrics", policy.getRetry().getMetrics());
        }
        if (policy.getBulkhead() != null) {
            metrics.put("bulkheadAvailableCalls", policy.getBulkhead().getMetrics().getAvailableConcurrentCalls());
        }
        if (policy.getRateLimiter() != null) {
            metrics.put("rateLimiterAvailablePermissions", policy.getRateLimiter().getMetrics().getAvailablePermissions());
        }
        status.setMetrics(Collections.unmodifiableMap(metrics));
    }

    private ResiliencyPolicy resolvePolicy(String serviceName) {
        return policyCache.computeIfAbsent(serviceName, this::createPolicy);
    }

    private ResiliencyPolicy createPolicy(String serviceName) {
        ResiliencyPolicy policy = new ResiliencyPolicy();

        policy.setCircuitBreaker(resolveBean(serviceName, CircuitBreaker.class)
                .orElseGet(() -> circuitBreakerRegistry.circuitBreaker(serviceName)));

        policy.setRetry(resolveBean(serviceName, Retry.class)
                .orElseGet(() -> retryRegistry.retry(serviceName)));

        policy.setTimeLimiter(resolveBean(serviceName, TimeLimiter.class)
                .orElseGet(() -> timeLimiterRegistry.timeLimiter(serviceName)));

        policy.setBulkhead(resolveBean(serviceName, Bulkhead.class)
                .orElseGet(() -> bulkheadRegistry.bulkhead(serviceName)));

        policy.setRateLimiter(resolveBean(serviceName, RateLimiter.class)
                .orElseGet(() -> rateLimiterRegistry.rateLimiter(serviceName)));

        return policy;
    }

    private <T> Optional<T> resolveBean(String serviceName, Class<T> type) {
        Map<String, T> beans = applicationContext.getBeansOfType(type);
        if (beans.isEmpty()) {
            return Optional.empty();
        }

        String normalizedName = serviceName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        return beans.entrySet().stream()
                .filter(entry -> entry.getKey().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().contains(normalizedName))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }

    private static final class ResiliencyPolicy {
        private CircuitBreaker circuitBreaker;
        private Retry retry;
        private TimeLimiter timeLimiter;
        private Bulkhead bulkhead;
        private RateLimiter rateLimiter;

        public CircuitBreaker getCircuitBreaker() {
            return circuitBreaker;
        }

        public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
        }

        public Retry getRetry() {
            return retry;
        }

        public void setRetry(Retry retry) {
            this.retry = retry;
        }

        public TimeLimiter getTimeLimiter() {
            return timeLimiter;
        }

        public void setTimeLimiter(TimeLimiter timeLimiter) {
            this.timeLimiter = timeLimiter;
        }

        public Bulkhead getBulkhead() {
            return bulkhead;
        }

        public void setBulkhead(Bulkhead bulkhead) {
            this.bulkhead = bulkhead;
        }

        public RateLimiter getRateLimiter() {
            return rateLimiter;
        }

        public void setRateLimiter(RateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
        }
    }
}
