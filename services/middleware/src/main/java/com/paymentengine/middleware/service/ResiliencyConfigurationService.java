package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.ResiliencyConfiguration;
import com.paymentengine.middleware.entity.ResiliencyConfiguration.CircuitBreakerConfig;
import com.paymentengine.middleware.entity.ResiliencyConfiguration.RetryConfig;
import com.paymentengine.middleware.entity.ResiliencyConfiguration.BulkheadConfig;
import com.paymentengine.middleware.entity.ResiliencyConfiguration.TimeoutConfig;
import com.paymentengine.middleware.entity.ResiliencyConfiguration.FallbackConfig;

import java.util.List;
import java.util.Optional;

/**
 * Resiliency Configuration Service
 * 
 * Manages resiliency configurations for different services and endpoints,
 * including circuit breaker, retry, bulkhead, timeout, and fallback settings.
 */
public interface ResiliencyConfigurationService {
    
    /**
     * Get resiliency configuration for a specific service
     */
    Optional<ResiliencyConfiguration> getConfiguration(String serviceName, String tenantId);
    
    /**
     * Get circuit breaker configuration
     */
    Optional<CircuitBreakerConfig> getCircuitBreakerConfig(String serviceName, String tenantId);
    
    /**
     * Get retry configuration
     */
    Optional<RetryConfig> getRetryConfig(String serviceName, String tenantId);
    
    /**
     * Get bulkhead configuration
     */
    Optional<BulkheadConfig> getBulkheadConfig(String serviceName, String tenantId);
    
    /**
     * Get timeout configuration
     */
    Optional<TimeoutConfig> getTimeoutConfig(String serviceName, String tenantId);
    
    /**
     * Get fallback configuration
     */
    Optional<FallbackConfig> getFallbackConfig(String serviceName, String tenantId);
    
    /**
     * Update resiliency configuration
     */
    ResiliencyConfiguration updateConfiguration(ResiliencyConfiguration configuration);
    
    /**
     * Get all configurations for a tenant
     */
    List<ResiliencyConfiguration> getConfigurationsByTenant(String tenantId);
    
    /**
     * Get default configuration for a service
     */
    ResiliencyConfiguration getDefaultConfiguration(String serviceName);
    
    /**
     * Test resiliency configuration
     */
    boolean testConfiguration(ResiliencyConfiguration configuration);
    
    /**
     * Get health status of all configured services
     */
    List<ServiceHealthStatus> getServiceHealthStatus(String tenantId);
    
    /**
     * Service Health Status
     */
    class ServiceHealthStatus {
        private String serviceName;
        private String status;
        private long responseTime;
        private String lastChecked;
        private String errorMessage;
        private boolean circuitBreakerOpen;
        private int activeConnections;
        private int queuedRequests;
        
        // Constructors, getters, setters
        public ServiceHealthStatus() {}
        
        public ServiceHealthStatus(String serviceName, String status, long responseTime, String lastChecked) {
            this.serviceName = serviceName;
            this.status = status;
            this.responseTime = responseTime;
            this.lastChecked = lastChecked;
        }
        
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getResponseTime() { return responseTime; }
        public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
        
        public String getLastChecked() { return lastChecked; }
        public void setLastChecked(String lastChecked) { this.lastChecked = lastChecked; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public boolean isCircuitBreakerOpen() { return circuitBreakerOpen; }
        public void setCircuitBreakerOpen(boolean circuitBreakerOpen) { this.circuitBreakerOpen = circuitBreakerOpen; }
        
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        
        public int getQueuedRequests() { return queuedRequests; }
        public void setQueuedRequests(int queuedRequests) { this.queuedRequests = queuedRequests; }
    }
}