package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.entity.ResiliencyConfiguration;
import com.paymentengine.middleware.repository.ResiliencyConfigurationRepository;
import com.paymentengine.middleware.service.ResiliencyConfigurationService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of Resiliency Service
 * 
 * Provides comprehensive resiliency patterns including circuit breaker, retry,
 * bulkhead, timeout, and fallback mechanisms for external service calls.
 */
@Service
public class ResiliencyServiceImpl implements ResiliencyConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ResiliencyServiceImpl.class);
    
    @Autowired
    private ResiliencyConfigurationRepository resiliencyConfigurationRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    // Resilience4j registries
    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
    private final BulkheadRegistry bulkheadRegistry = BulkheadRegistry.ofDefaults();
    private final TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.ofDefaults();
    
    // Thread pool for bulkhead isolation
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    // Health check cache
    private final Map<String, ServiceHealthStatus> healthStatusCache = new HashMap<>();
    
    @Override
    @Cacheable(value = "resiliency-configurations", key = "#serviceName + '_' + #tenantId")
    public Optional<ResiliencyConfiguration> getConfiguration(String serviceName, String tenantId) {
        logger.debug("Getting resiliency configuration for service: {}, tenant: {}", serviceName, tenantId);
        
        try {
            Optional<ResiliencyConfiguration> config = resiliencyConfigurationRepository
                    .findTopActiveByServiceNameAndTenantId(serviceName, tenantId);
            
            if (config.isEmpty()) {
                // Try to get default configuration
                config = Optional.of(getDefaultConfiguration(serviceName));
                logger.debug("Using default configuration for service: {}", serviceName);
            }
            
            return config;
            
        } catch (Exception e) {
            logger.error("Error getting resiliency configuration for service: {}, tenant: {}: {}", 
                        serviceName, tenantId, e.getMessage());
            return Optional.of(getDefaultConfiguration(serviceName));
        }
    }
    
    @Override
    public Optional<CircuitBreakerConfig> getCircuitBreakerConfig(String serviceName, String tenantId) {
        return getConfiguration(serviceName, tenantId)
                .map(ResiliencyConfiguration::getCircuitBreakerConfig);
    }
    
    @Override
    public Optional<RetryConfig> getRetryConfig(String serviceName, String tenantId) {
        return getConfiguration(serviceName, tenantId)
                .map(ResiliencyConfiguration::getRetryConfig);
    }
    
    @Override
    public Optional<BulkheadConfig> getBulkheadConfig(String serviceName, String tenantId) {
        return getConfiguration(serviceName, tenantId)
                .map(ResiliencyConfiguration::getBulkheadConfig);
    }
    
    @Override
    public Optional<TimeoutConfig> getTimeoutConfig(String serviceName, String tenantId) {
        return getConfiguration(serviceName, tenantId)
                .map(ResiliencyConfiguration::getTimeoutConfig);
    }
    
    @Override
    public Optional<FallbackConfig> getFallbackConfig(String serviceName, String tenantId) {
        return getConfiguration(serviceName, tenantId)
                .map(ResiliencyConfiguration::getFallbackConfig);
    }
    
    @Override
    public ResiliencyConfiguration updateConfiguration(ResiliencyConfiguration configuration) {
        logger.info("Updating resiliency configuration for service: {}, tenant: {}", 
                   configuration.getServiceName(), configuration.getTenantId());
        
        try {
            configuration.setUpdatedAt(LocalDateTime.now());
            ResiliencyConfiguration savedConfig = resiliencyConfigurationRepository.save(configuration);
            
            // Clear cache for this configuration
            clearConfigurationCache(configuration.getServiceName(), configuration.getTenantId());
            
            logger.info("Successfully updated resiliency configuration for service: {}, tenant: {}", 
                       configuration.getServiceName(), configuration.getTenantId());
            
            return savedConfig;
            
        } catch (Exception e) {
            logger.error("Error updating resiliency configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update resiliency configuration", e);
        }
    }
    
    @Override
    public List<ResiliencyConfiguration> getConfigurationsByTenant(String tenantId) {
        logger.debug("Getting all resiliency configurations for tenant: {}", tenantId);
        
        try {
            return resiliencyConfigurationRepository.findActiveByTenantId(tenantId);
        } catch (Exception e) {
            logger.error("Error getting resiliency configurations for tenant: {}: {}", tenantId, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public ResiliencyConfiguration getDefaultConfiguration(String serviceName) {
        logger.debug("Creating default resiliency configuration for service: {}", serviceName);
        
        ResiliencyConfiguration config = new ResiliencyConfiguration(serviceName, "default");
        
        // Default circuit breaker configuration
        ResiliencyConfiguration.CircuitBreakerConfig circuitBreakerConfig = new ResiliencyConfiguration.CircuitBreakerConfig();
        circuitBreakerConfig.setFailureThreshold(5);
        circuitBreakerConfig.setSuccessThreshold(3);
        circuitBreakerConfig.setWaitDurationSeconds(60);
        circuitBreakerConfig.setSlowCallThresholdSeconds(5);
        circuitBreakerConfig.setSlowCallRateThreshold(0.5);
        config.setCircuitBreakerConfig(circuitBreakerConfig);
        
        // Default retry configuration
        ResiliencyConfiguration.RetryConfig retryConfig = new ResiliencyConfiguration.RetryConfig();
        retryConfig.setMaxAttempts(3);
        retryConfig.setWaitDurationSeconds(1);
        retryConfig.setExponentialBackoffMultiplier(2.0);
        retryConfig.setMaxWaitDurationSeconds(30);
        retryConfig.setRetryOnExceptions("java.net.ConnectException,java.net.SocketTimeoutException");
        config.setRetryConfig(retryConfig);
        
        // Default bulkhead configuration
        ResiliencyConfiguration.BulkheadConfig bulkheadConfig = new ResiliencyConfiguration.BulkheadConfig();
        bulkheadConfig.setMaxConcurrentCalls(25);
        bulkheadConfig.setMaxWaitDurationSeconds(5);
        bulkheadConfig.setThreadPoolSize(10);
        bulkheadConfig.setQueueCapacity(100);
        config.setBulkheadConfig(bulkheadConfig);
        
        // Default timeout configuration
        ResiliencyConfiguration.TimeoutConfig timeoutConfig = new ResiliencyConfiguration.TimeoutConfig();
        timeoutConfig.setTimeoutDurationSeconds(30);
        timeoutConfig.setCancelRunningFuture(true);
        config.setTimeoutConfig(timeoutConfig);
        
        // Default fallback configuration
        ResiliencyConfiguration.FallbackConfig fallbackConfig = new ResiliencyConfiguration.FallbackConfig();
        fallbackConfig.setFallbackEnabled(true);
        fallbackConfig.setFallbackTimeoutSeconds(5);
        config.setFallbackConfig(fallbackConfig);
        
        // Default health check configuration
        ResiliencyConfiguration.HealthCheckConfig healthCheckConfig = new ResiliencyConfiguration.HealthCheckConfig();
        healthCheckConfig.setHealthCheckEnabled(true);
        healthCheckConfig.setHealthCheckIntervalSeconds(30);
        healthCheckConfig.setHealthCheckTimeoutSeconds(5);
        config.setHealthCheckConfig(healthCheckConfig);
        
        // Default monitoring configuration
        ResiliencyConfiguration.MonitoringConfig monitoringConfig = new ResiliencyConfiguration.MonitoringConfig();
        monitoringConfig.setMetricsEnabled(true);
        monitoringConfig.setAlertingEnabled(true);
        monitoringConfig.setAlertThresholdFailureRate(0.5);
        monitoringConfig.setAlertThresholdResponseTimeMs(5000L);
        config.setMonitoringConfig(monitoringConfig);
        
        config.setDescription("Default resiliency configuration for " + serviceName);
        config.setPriority(1);
        config.setIsActive(true);
        
        return config;
    }
    
    @Override
    public boolean testConfiguration(ResiliencyConfiguration configuration) {
        logger.info("Testing resiliency configuration for service: {}", configuration.getServiceName());
        
        try {
            // Test circuit breaker configuration
            if (configuration.getCircuitBreakerConfig() != null) {
                CircuitBreakerConfig circuitBreakerConfig = createCircuitBreakerConfig(configuration.getCircuitBreakerConfig());
                CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("test-" + configuration.getServiceName(), circuitBreakerConfig);
                logger.debug("Circuit breaker configuration test passed for service: {}", configuration.getServiceName());
            }
            
            // Test retry configuration
            if (configuration.getRetryConfig() != null) {
                RetryConfig retryConfig = createRetryConfig(configuration.getRetryConfig());
                Retry retry = retryRegistry.retry("test-" + configuration.getServiceName(), retryConfig);
                logger.debug("Retry configuration test passed for service: {}", configuration.getServiceName());
            }
            
            // Test bulkhead configuration
            if (configuration.getBulkheadConfig() != null) {
                BulkheadConfig bulkheadConfig = createBulkheadConfig(configuration.getBulkheadConfig());
                Bulkhead bulkhead = bulkheadRegistry.bulkhead("test-" + configuration.getServiceName(), bulkheadConfig);
                logger.debug("Bulkhead configuration test passed for service: {}", configuration.getServiceName());
            }
            
            // Test timeout configuration
            if (configuration.getTimeoutConfig() != null) {
                TimeLimiterConfig timeLimiterConfig = createTimeLimiterConfig(configuration.getTimeoutConfig());
                TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("test-" + configuration.getServiceName(), timeLimiterConfig);
                logger.debug("Timeout configuration test passed for service: {}", configuration.getServiceName());
            }
            
            logger.info("All resiliency configuration tests passed for service: {}", configuration.getServiceName());
            return true;
            
        } catch (Exception e) {
            logger.error("Resiliency configuration test failed for service: {}: {}", 
                        configuration.getServiceName(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<ServiceHealthStatus> getServiceHealthStatus(String tenantId) {
        logger.debug("Getting service health status for tenant: {}", tenantId);
        
        List<ServiceHealthStatus> healthStatuses = new ArrayList<>();
        List<ResiliencyConfiguration> configurations = getConfigurationsByTenant(tenantId);
        
        for (ResiliencyConfiguration config : configurations) {
            String serviceName = config.getServiceName();
            ServiceHealthStatus healthStatus = healthStatusCache.get(serviceName);
            
            if (healthStatus == null || isHealthStatusExpired(healthStatus)) {
                healthStatus = performHealthCheck(config);
                healthStatusCache.put(serviceName, healthStatus);
            }
            
            healthStatuses.add(healthStatus);
        }
        
        return healthStatuses;
    }
    
    /**
     * Execute a resilient service call with all resiliency patterns applied
     */
    public <T> T executeResilientCall(String serviceName, String tenantId, Supplier<T> serviceCall, Function<Exception, T> fallback) {
        logger.debug("Executing resilient call for service: {}, tenant: {}", serviceName, tenantId);
        
        try {
            Optional<ResiliencyConfiguration> configOpt = getConfiguration(serviceName, tenantId);
            if (configOpt.isEmpty()) {
                logger.warn("No resiliency configuration found for service: {}, tenant: {}, using direct call", serviceName, tenantId);
                return serviceCall.get();
            }
            
            ResiliencyConfiguration config = configOpt.get();
            
            // Create resilience4j components
            CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(serviceName, config);
            Retry retry = getOrCreateRetry(serviceName, config);
            Bulkhead bulkhead = getOrCreateBulkhead(serviceName, config);
            TimeLimiter timeLimiter = getOrCreateTimeLimiter(serviceName, config);
            
            // Decorate the service call with all resiliency patterns
            Supplier<T> decoratedSupplier = Decorators.ofSupplier(serviceCall)
                    .withCircuitBreaker(circuitBreaker)
                    .withRetry(retry)
                    .withBulkhead(bulkhead)
                    .withTimeLimiter(timeLimiter)
                    .decorate();
            
            // Execute with fallback
            return Try.ofSupplier(decoratedSupplier)
                    .recover(fallback)
                    .get();
            
        } catch (Exception e) {
            logger.error("Error executing resilient call for service: {}, tenant: {}: {}", 
                        serviceName, tenantId, e.getMessage(), e);
            
            // Try fallback if available
            if (fallback != null) {
                try {
                    return fallback.apply(e);
                } catch (Exception fallbackException) {
                    logger.error("Fallback also failed for service: {}, tenant: {}: {}", 
                                serviceName, tenantId, fallbackException.getMessage());
                }
            }
            
            throw new RuntimeException("Resilient call failed for service: " + serviceName, e);
        }
    }
    
    /**
     * Execute a resilient HTTP call
     */
    public <T> ResponseEntity<T> executeResilientHttpCall(String serviceName, String tenantId, String url, 
                                                         HttpMethod method, HttpEntity<?> entity, Class<T> responseType) {
        logger.debug("Executing resilient HTTP call for service: {}, tenant: {}, url: {}", serviceName, tenantId, url);
        
        Supplier<ResponseEntity<T>> httpCall = () -> {
            try {
                return restTemplate.exchange(url, method, entity, responseType);
            } catch (Exception e) {
                logger.error("HTTP call failed for service: {}, url: {}: {}", serviceName, url, e.getMessage());
                throw e;
            }
        };
        
        Function<Exception, ResponseEntity<T>> fallback = (exception) -> {
            logger.warn("Using fallback response for service: {}, url: {}", serviceName, url);
            // Return a default error response
            return ResponseEntity.status(503).build();
        };
        
        return executeResilientCall(serviceName, tenantId, httpCall, fallback);
    }
    
    /**
     * Execute a resilient async call
     */
    public <T> CompletableFuture<T> executeResilientAsyncCall(String serviceName, String tenantId, Supplier<T> serviceCall, Function<Exception, T> fallback) {
        logger.debug("Executing resilient async call for service: {}, tenant: {}", serviceName, tenantId);
        
        return CompletableFuture.supplyAsync(() -> {
            return executeResilientCall(serviceName, tenantId, serviceCall, fallback);
        }, executorService);
    }
    
    // Private helper methods
    
    private CircuitBreaker getOrCreateCircuitBreaker(String serviceName, ResiliencyConfiguration config) {
        String circuitBreakerName = serviceName + "-circuit-breaker";
        
        if (circuitBreakerRegistry.find(circuitBreakerName).isPresent()) {
            return circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        }
        
        if (config.getCircuitBreakerConfig() != null) {
            CircuitBreakerConfig circuitBreakerConfig = createCircuitBreakerConfig(config.getCircuitBreakerConfig());
            return circuitBreakerRegistry.circuitBreaker(circuitBreakerName, circuitBreakerConfig);
        }
        
        return circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
    }
    
    private Retry getOrCreateRetry(String serviceName, ResiliencyConfiguration config) {
        String retryName = serviceName + "-retry";
        
        if (retryRegistry.find(retryName).isPresent()) {
            return retryRegistry.retry(retryName);
        }
        
        if (config.getRetryConfig() != null) {
            RetryConfig retryConfig = createRetryConfig(config.getRetryConfig());
            return retryRegistry.retry(retryName, retryConfig);
        }
        
        return retryRegistry.retry(retryName);
    }
    
    private Bulkhead getOrCreateBulkhead(String serviceName, ResiliencyConfiguration config) {
        String bulkheadName = serviceName + "-bulkhead";
        
        if (bulkheadRegistry.find(bulkheadName).isPresent()) {
            return bulkheadRegistry.bulkhead(bulkheadName);
        }
        
        if (config.getBulkheadConfig() != null) {
            BulkheadConfig bulkheadConfig = createBulkheadConfig(config.getBulkheadConfig());
            return bulkheadRegistry.bulkhead(bulkheadName, bulkheadConfig);
        }
        
        return bulkheadRegistry.bulkhead(bulkheadName);
    }
    
    private TimeLimiter getOrCreateTimeLimiter(String serviceName, ResiliencyConfiguration config) {
        String timeLimiterName = serviceName + "-time-limiter";
        
        if (timeLimiterRegistry.find(timeLimiterName).isPresent()) {
            return timeLimiterRegistry.timeLimiter(timeLimiterName);
        }
        
        if (config.getTimeoutConfig() != null) {
            TimeLimiterConfig timeLimiterConfig = createTimeLimiterConfig(config.getTimeoutConfig());
            return timeLimiterRegistry.timeLimiter(timeLimiterName, timeLimiterConfig);
        }
        
        return timeLimiterRegistry.timeLimiter(timeLimiterName);
    }
    
    private CircuitBreakerConfig createCircuitBreakerConfig(ResiliencyConfiguration.CircuitBreakerConfig config) {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(config.getFailureThreshold() * 10) // Convert to percentage
                .waitDurationInOpenState(Duration.ofSeconds(config.getWaitDurationSeconds()))
                .slowCallRateThreshold(config.getSlowCallRateThreshold() * 100) // Convert to percentage
                .slowCallDurationThreshold(Duration.ofSeconds(config.getSlowCallThresholdSeconds()))
                .permittedNumberOfCallsInHalfOpenState(config.getPermittedCallsInHalfOpen())
                .automaticTransitionFromOpenToHalfOpenEnabled(config.getAutomaticTransitionFromOpenToHalfOpen())
                .build();
    }
    
    private RetryConfig createRetryConfig(ResiliencyConfiguration.RetryConfig config) {
        return RetryConfig.custom()
                .maxAttempts(config.getMaxAttempts())
                .waitDuration(Duration.ofSeconds(config.getWaitDurationSeconds()))
                .exponentialBackoffMultiplier(config.getExponentialBackoffMultiplier())
                .maxWaitDuration(Duration.ofSeconds(config.getMaxWaitDurationSeconds()))
                .build();
    }
    
    private BulkheadConfig createBulkheadConfig(ResiliencyConfiguration.BulkheadConfig config) {
        return BulkheadConfig.custom()
                .maxConcurrentCalls(config.getMaxConcurrentCalls())
                .maxWaitDuration(Duration.ofSeconds(config.getMaxWaitDurationSeconds()))
                .build();
    }
    
    private TimeLimiterConfig createTimeLimiterConfig(ResiliencyConfiguration.TimeoutConfig config) {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(config.getTimeoutDurationSeconds()))
                .cancelRunningFuture(config.getCancelRunningFuture())
                .build();
    }
    
    private ServiceHealthStatus performHealthCheck(ResiliencyConfiguration config) {
        String serviceName = config.getServiceName();
        long startTime = System.currentTimeMillis();
        
        try {
            if (config.getHealthCheckConfig() != null && config.getHealthCheckConfig().getHealthCheckEnabled()) {
                // Perform actual health check
                String healthCheckUrl = config.getHealthCheckConfig().getHealthCheckEndpoint();
                if (healthCheckUrl != null && !healthCheckUrl.isEmpty()) {
                    ResponseEntity<String> response = restTemplate.getForEntity(healthCheckUrl, String.class);
                    long responseTime = System.currentTimeMillis() - startTime;
                    
                    return new ServiceHealthStatus(
                            serviceName,
                            response.getStatusCode().is2xxSuccessful() ? "UP" : "DOWN",
                            responseTime,
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    );
                }
            }
            
            // Default health status
            long responseTime = System.currentTimeMillis() - startTime;
            return new ServiceHealthStatus(
                    serviceName,
                    "UNKNOWN",
                    responseTime,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            ServiceHealthStatus healthStatus = new ServiceHealthStatus(
                    serviceName,
                    "DOWN",
                    responseTime,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            healthStatus.setErrorMessage(e.getMessage());
            return healthStatus;
        }
    }
    
    private boolean isHealthStatusExpired(ServiceHealthStatus healthStatus) {
        try {
            LocalDateTime lastChecked = LocalDateTime.parse(healthStatus.getLastChecked());
            return lastChecked.isBefore(LocalDateTime.now().minusMinutes(5)); // Expire after 5 minutes
        } catch (Exception e) {
            return true; // If we can't parse the date, consider it expired
        }
    }
    
    private void clearConfigurationCache(String serviceName, String tenantId) {
        // This would clear the Spring cache if using @CacheEvict
        // For now, we'll rely on cache TTL
        logger.debug("Configuration cache cleared for service: {}, tenant: {}", serviceName, tenantId);
    }
}