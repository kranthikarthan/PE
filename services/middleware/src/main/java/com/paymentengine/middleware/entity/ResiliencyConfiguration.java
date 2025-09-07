package com.paymentengine.middleware.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Resiliency Configuration Entity
 * 
 * Defines resiliency patterns and configurations for different services,
 * including circuit breaker, retry, bulkhead, timeout, and fallback settings.
 */
@Entity
@Table(name = "resiliency_configurations", schema = "payment_engine")
public class ResiliencyConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "service_name", nullable = false, length = 100)
    @NotBlank
    private String serviceName;
    
    @Column(name = "tenant_id", nullable = false, length = 50)
    @NotBlank
    private String tenantId;
    
    @Column(name = "endpoint_pattern", length = 500)
    private String endpointPattern;
    
    @Column(name = "circuit_breaker_config", columnDefinition = "jsonb")
    private CircuitBreakerConfig circuitBreakerConfig;
    
    @Column(name = "retry_config", columnDefinition = "jsonb")
    private RetryConfig retryConfig;
    
    @Column(name = "bulkhead_config", columnDefinition = "jsonb")
    private BulkheadConfig bulkheadConfig;
    
    @Column(name = "timeout_config", columnDefinition = "jsonb")
    private TimeoutConfig timeoutConfig;
    
    @Column(name = "fallback_config", columnDefinition = "jsonb")
    private FallbackConfig fallbackConfig;
    
    @Column(name = "health_check_config", columnDefinition = "jsonb")
    private HealthCheckConfig healthCheckConfig;
    
    @Column(name = "monitoring_config", columnDefinition = "jsonb")
    private MonitoringConfig monitoringConfig;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "priority")
    @Min(1)
    @Max(100)
    private Integer priority = 1;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public ResiliencyConfiguration() {}
    
    public ResiliencyConfiguration(String serviceName, String tenantId) {
        this.serviceName = serviceName;
        this.tenantId = tenantId;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getEndpointPattern() { return endpointPattern; }
    public void setEndpointPattern(String endpointPattern) { this.endpointPattern = endpointPattern; }
    
    public CircuitBreakerConfig getCircuitBreakerConfig() { return circuitBreakerConfig; }
    public void setCircuitBreakerConfig(CircuitBreakerConfig circuitBreakerConfig) { this.circuitBreakerConfig = circuitBreakerConfig; }
    
    public RetryConfig getRetryConfig() { return retryConfig; }
    public void setRetryConfig(RetryConfig retryConfig) { this.retryConfig = retryConfig; }
    
    public BulkheadConfig getBulkheadConfig() { return bulkheadConfig; }
    public void setBulkheadConfig(BulkheadConfig bulkheadConfig) { this.bulkheadConfig = bulkheadConfig; }
    
    public TimeoutConfig getTimeoutConfig() { return timeoutConfig; }
    public void setTimeoutConfig(TimeoutConfig timeoutConfig) { this.timeoutConfig = timeoutConfig; }
    
    public FallbackConfig getFallbackConfig() { return fallbackConfig; }
    public void setFallbackConfig(FallbackConfig fallbackConfig) { this.fallbackConfig = fallbackConfig; }
    
    public HealthCheckConfig getHealthCheckConfig() { return healthCheckConfig; }
    public void setHealthCheckConfig(HealthCheckConfig healthCheckConfig) { this.healthCheckConfig = healthCheckConfig; }
    
    public MonitoringConfig getMonitoringConfig() { return monitoringConfig; }
    public void setMonitoringConfig(MonitoringConfig monitoringConfig) { this.monitoringConfig = monitoringConfig; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    // Inner Classes for Configuration
    
    @Embeddable
    public static class CircuitBreakerConfig {
        @Column(name = "failure_threshold")
        private Integer failureThreshold = 5;
        
        @Column(name = "success_threshold")
        private Integer successThreshold = 3;
        
        @Column(name = "wait_duration_seconds")
        private Integer waitDurationSeconds = 60;
        
        @Column(name = "slow_call_threshold_seconds")
        private Integer slowCallThresholdSeconds = 5;
        
        @Column(name = "slow_call_rate_threshold")
        private Double slowCallRateThreshold = 0.5;
        
        @Column(name = "permitted_calls_in_half_open")
        private Integer permittedCallsInHalfOpen = 3;
        
        @Column(name = "automatic_transition_from_open_to_half_open")
        private Boolean automaticTransitionFromOpenToHalfOpen = true;
        
        // Getters and Setters
        public Integer getFailureThreshold() { return failureThreshold; }
        public void setFailureThreshold(Integer failureThreshold) { this.failureThreshold = failureThreshold; }
        
        public Integer getSuccessThreshold() { return successThreshold; }
        public void setSuccessThreshold(Integer successThreshold) { this.successThreshold = successThreshold; }
        
        public Integer getWaitDurationSeconds() { return waitDurationSeconds; }
        public void setWaitDurationSeconds(Integer waitDurationSeconds) { this.waitDurationSeconds = waitDurationSeconds; }
        
        public Integer getSlowCallThresholdSeconds() { return slowCallThresholdSeconds; }
        public void setSlowCallThresholdSeconds(Integer slowCallThresholdSeconds) { this.slowCallThresholdSeconds = slowCallThresholdSeconds; }
        
        public Double getSlowCallRateThreshold() { return slowCallRateThreshold; }
        public void setSlowCallRateThreshold(Double slowCallRateThreshold) { this.slowCallRateThreshold = slowCallRateThreshold; }
        
        public Integer getPermittedCallsInHalfOpen() { return permittedCallsInHalfOpen; }
        public void setPermittedCallsInHalfOpen(Integer permittedCallsInHalfOpen) { this.permittedCallsInHalfOpen = permittedCallsInHalfOpen; }
        
        public Boolean getAutomaticTransitionFromOpenToHalfOpen() { return automaticTransitionFromOpenToHalfOpen; }
        public void setAutomaticTransitionFromOpenToHalfOpen(Boolean automaticTransitionFromOpenToHalfOpen) { this.automaticTransitionFromOpenToHalfOpen = automaticTransitionFromOpenToHalfOpen; }
    }
    
    @Embeddable
    public static class RetryConfig {
        @Column(name = "max_attempts")
        private Integer maxAttempts = 3;
        
        @Column(name = "wait_duration_seconds")
        private Integer waitDurationSeconds = 1;
        
        @Column(name = "exponential_backoff_multiplier")
        private Double exponentialBackoffMultiplier = 2.0;
        
        @Column(name = "max_wait_duration_seconds")
        private Integer maxWaitDurationSeconds = 30;
        
        @Column(name = "retry_on_exceptions")
        private String retryOnExceptions = "java.net.ConnectException,java.net.SocketTimeoutException";
        
        @Column(name = "ignore_exceptions")
        private String ignoreExceptions = "java.lang.IllegalArgumentException";
        
        // Getters and Setters
        public Integer getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
        
        public Integer getWaitDurationSeconds() { return waitDurationSeconds; }
        public void setWaitDurationSeconds(Integer waitDurationSeconds) { this.waitDurationSeconds = waitDurationSeconds; }
        
        public Double getExponentialBackoffMultiplier() { return exponentialBackoffMultiplier; }
        public void setExponentialBackoffMultiplier(Double exponentialBackoffMultiplier) { this.exponentialBackoffMultiplier = exponentialBackoffMultiplier; }
        
        public Integer getMaxWaitDurationSeconds() { return maxWaitDurationSeconds; }
        public void setMaxWaitDurationSeconds(Integer maxWaitDurationSeconds) { this.maxWaitDurationSeconds = maxWaitDurationSeconds; }
        
        public String getRetryOnExceptions() { return retryOnExceptions; }
        public void setRetryOnExceptions(String retryOnExceptions) { this.retryOnExceptions = retryOnExceptions; }
        
        public String getIgnoreExceptions() { return ignoreExceptions; }
        public void setIgnoreExceptions(String ignoreExceptions) { this.ignoreExceptions = ignoreExceptions; }
    }
    
    @Embeddable
    public static class BulkheadConfig {
        @Column(name = "max_concurrent_calls")
        private Integer maxConcurrentCalls = 25;
        
        @Column(name = "max_wait_duration_seconds")
        private Integer maxWaitDurationSeconds = 5;
        
        @Column(name = "thread_pool_size")
        private Integer threadPoolSize = 10;
        
        @Column(name = "queue_capacity")
        private Integer queueCapacity = 100;
        
        @Column(name = "keep_alive_duration_seconds")
        private Integer keepAliveDurationSeconds = 60;
        
        // Getters and Setters
        public Integer getMaxConcurrentCalls() { return maxConcurrentCalls; }
        public void setMaxConcurrentCalls(Integer maxConcurrentCalls) { this.maxConcurrentCalls = maxConcurrentCalls; }
        
        public Integer getMaxWaitDurationSeconds() { return maxWaitDurationSeconds; }
        public void setMaxWaitDurationSeconds(Integer maxWaitDurationSeconds) { this.maxWaitDurationSeconds = maxWaitDurationSeconds; }
        
        public Integer getThreadPoolSize() { return threadPoolSize; }
        public void setThreadPoolSize(Integer threadPoolSize) { this.threadPoolSize = threadPoolSize; }
        
        public Integer getQueueCapacity() { return queueCapacity; }
        public void setQueueCapacity(Integer queueCapacity) { this.queueCapacity = queueCapacity; }
        
        public Integer getKeepAliveDurationSeconds() { return keepAliveDurationSeconds; }
        public void setKeepAliveDurationSeconds(Integer keepAliveDurationSeconds) { this.keepAliveDurationSeconds = keepAliveDurationSeconds; }
    }
    
    @Embeddable
    public static class TimeoutConfig {
        @Column(name = "timeout_duration_seconds")
        private Integer timeoutDurationSeconds = 30;
        
        @Column(name = "cancel_running_future")
        private Boolean cancelRunningFuture = true;
        
        @Column(name = "timeout_exception_message")
        private String timeoutExceptionMessage = "Service call timed out";
        
        // Getters and Setters
        public Integer getTimeoutDurationSeconds() { return timeoutDurationSeconds; }
        public void setTimeoutDurationSeconds(Integer timeoutDurationSeconds) { this.timeoutDurationSeconds = timeoutDurationSeconds; }
        
        public Boolean getCancelRunningFuture() { return cancelRunningFuture; }
        public void setCancelRunningFuture(Boolean cancelRunningFuture) { this.cancelRunningFuture = cancelRunningFuture; }
        
        public String getTimeoutExceptionMessage() { return timeoutExceptionMessage; }
        public void setTimeoutExceptionMessage(String timeoutExceptionMessage) { this.timeoutExceptionMessage = timeoutExceptionMessage; }
    }
    
    @Embeddable
    public static class FallbackConfig {
        @Column(name = "fallback_method")
        private String fallbackMethod;
        
        @Column(name = "fallback_data", columnDefinition = "jsonb")
        private Map<String, Object> fallbackData;
        
        @Column(name = "fallback_enabled")
        private Boolean fallbackEnabled = true;
        
        @Column(name = "fallback_timeout_seconds")
        private Integer fallbackTimeoutSeconds = 5;
        
        @Column(name = "fallback_retry_attempts")
        private Integer fallbackRetryAttempts = 1;
        
        // Getters and Setters
        public String getFallbackMethod() { return fallbackMethod; }
        public void setFallbackMethod(String fallbackMethod) { this.fallbackMethod = fallbackMethod; }
        
        public Map<String, Object> getFallbackData() { return fallbackData; }
        public void setFallbackData(Map<String, Object> fallbackData) { this.fallbackData = fallbackData; }
        
        public Boolean getFallbackEnabled() { return fallbackEnabled; }
        public void setFallbackEnabled(Boolean fallbackEnabled) { this.fallbackEnabled = fallbackEnabled; }
        
        public Integer getFallbackTimeoutSeconds() { return fallbackTimeoutSeconds; }
        public void setFallbackTimeoutSeconds(Integer fallbackTimeoutSeconds) { this.fallbackTimeoutSeconds = fallbackTimeoutSeconds; }
        
        public Integer getFallbackRetryAttempts() { return fallbackRetryAttempts; }
        public void setFallbackRetryAttempts(Integer fallbackRetryAttempts) { this.fallbackRetryAttempts = fallbackRetryAttempts; }
    }
    
    @Embeddable
    public static class HealthCheckConfig {
        @Column(name = "health_check_enabled")
        private Boolean healthCheckEnabled = true;
        
        @Column(name = "health_check_interval_seconds")
        private Integer healthCheckIntervalSeconds = 30;
        
        @Column(name = "health_check_timeout_seconds")
        private Integer healthCheckTimeoutSeconds = 5;
        
        @Column(name = "health_check_endpoint")
        private String healthCheckEndpoint = "/health";
        
        @Column(name = "health_check_method")
        private String healthCheckMethod = "GET";
        
        @Column(name = "expected_status_codes")
        private String expectedStatusCodes = "200,201,202";
        
        // Getters and Setters
        public Boolean getHealthCheckEnabled() { return healthCheckEnabled; }
        public void setHealthCheckEnabled(Boolean healthCheckEnabled) { this.healthCheckEnabled = healthCheckEnabled; }
        
        public Integer getHealthCheckIntervalSeconds() { return healthCheckIntervalSeconds; }
        public void setHealthCheckIntervalSeconds(Integer healthCheckIntervalSeconds) { this.healthCheckIntervalSeconds = healthCheckIntervalSeconds; }
        
        public Integer getHealthCheckTimeoutSeconds() { return healthCheckTimeoutSeconds; }
        public void setHealthCheckTimeoutSeconds(Integer healthCheckTimeoutSeconds) { this.healthCheckTimeoutSeconds = healthCheckTimeoutSeconds; }
        
        public String getHealthCheckEndpoint() { return healthCheckEndpoint; }
        public void setHealthCheckEndpoint(String healthCheckEndpoint) { this.healthCheckEndpoint = healthCheckEndpoint; }
        
        public String getHealthCheckMethod() { return healthCheckMethod; }
        public void setHealthCheckMethod(String healthCheckMethod) { this.healthCheckMethod = healthCheckMethod; }
        
        public String getExpectedStatusCodes() { return expectedStatusCodes; }
        public void setExpectedStatusCodes(String expectedStatusCodes) { this.expectedStatusCodes = expectedStatusCodes; }
    }
    
    @Embeddable
    public static class MonitoringConfig {
        @Column(name = "metrics_enabled")
        private Boolean metricsEnabled = true;
        
        @Column(name = "alerting_enabled")
        private Boolean alertingEnabled = true;
        
        @Column(name = "alert_threshold_failure_rate")
        private Double alertThresholdFailureRate = 0.5;
        
        @Column(name = "alert_threshold_response_time_ms")
        private Long alertThresholdResponseTimeMs = 5000L;
        
        @Column(name = "alert_threshold_circuit_breaker_open")
        private Boolean alertThresholdCircuitBreakerOpen = true;
        
        @Column(name = "notification_channels")
        private String notificationChannels = "email,slack";
        
        // Getters and Setters
        public Boolean getMetricsEnabled() { return metricsEnabled; }
        public void setMetricsEnabled(Boolean metricsEnabled) { this.metricsEnabled = metricsEnabled; }
        
        public Boolean getAlertingEnabled() { return alertingEnabled; }
        public void setAlertingEnabled(Boolean alertingEnabled) { this.alertingEnabled = alertingEnabled; }
        
        public Double getAlertThresholdFailureRate() { return alertThresholdFailureRate; }
        public void setAlertThresholdFailureRate(Double alertThresholdFailureRate) { this.alertThresholdFailureRate = alertThresholdFailureRate; }
        
        public Long getAlertThresholdResponseTimeMs() { return alertThresholdResponseTimeMs; }
        public void setAlertThresholdResponseTimeMs(Long alertThresholdResponseTimeMs) { this.alertThresholdResponseTimeMs = alertThresholdResponseTimeMs; }
        
        public Boolean getAlertThresholdCircuitBreakerOpen() { return alertThresholdCircuitBreakerOpen; }
        public void setAlertThresholdCircuitBreakerOpen(Boolean alertThresholdCircuitBreakerOpen) { this.alertThresholdCircuitBreakerOpen = alertThresholdCircuitBreakerOpen; }
        
        public String getNotificationChannels() { return notificationChannels; }
        public void setNotificationChannels(String notificationChannels) { this.notificationChannels = notificationChannels; }
    }
}