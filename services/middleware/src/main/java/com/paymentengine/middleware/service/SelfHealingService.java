package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.ResiliencyConfiguration;
import com.paymentengine.middleware.entity.QueuedMessage;
import com.paymentengine.middleware.service.ResiliencyConfigurationService.ServiceHealthStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Self-Healing Service
 * 
 * Monitors system health and automatically recovers from failures.
 * Provides proactive healing, automatic retry, and system restoration.
 */
public interface SelfHealingService {
    
    /**
     * Start self-healing monitoring
     */
    void startMonitoring();
    
    /**
     * Stop self-healing monitoring
     */
    void stopMonitoring();
    
    /**
     * Perform health check on all services
     */
    CompletableFuture<List<ServiceHealthStatus>> performHealthChecks(String tenantId);
    
    /**
     * Attempt to recover a failed service
     */
    CompletableFuture<RecoveryResult> recoverService(String serviceName, String tenantId);
    
    /**
     * Process queued messages for failed services
     */
    CompletableFuture<List<QueuedMessage>> processQueuedMessagesForService(String serviceName, String tenantId);
    
    /**
     * Auto-retry failed operations
     */
    CompletableFuture<List<RetryResult>> autoRetryFailedOperations(String tenantId);
    
    /**
     * Reset circuit breaker for a service
     */
    boolean resetCircuitBreaker(String serviceName, String tenantId);
    
    /**
     * Scale up resources for a service
     */
    CompletableFuture<ScalingResult> scaleUpService(String serviceName, String tenantId);
    
    /**
     * Scale down resources for a service
     */
    CompletableFuture<ScalingResult> scaleDownService(String serviceName, String tenantId);
    
    /**
     * Get self-healing statistics
     */
    SelfHealingStatistics getSelfHealingStatistics(String tenantId);
    
    /**
     * Configure auto-healing rules
     */
    void configureAutoHealingRules(String serviceName, String tenantId, AutoHealingRules rules);
    
    /**
     * Get auto-healing rules for a service
     */
    AutoHealingRules getAutoHealingRules(String serviceName, String tenantId);
    
    /**
     * Recovery Result
     */
    class RecoveryResult {
        private String serviceName;
        private boolean success;
        private String message;
        private long recoveryTimeMs;
        private List<String> actionsTaken;
        private Map<String, Object> metrics;
        
        // Constructors, getters, setters
        public RecoveryResult() {}
        
        public RecoveryResult(String serviceName, boolean success, String message) {
            this.serviceName = serviceName;
            this.success = success;
            this.message = message;
        }
        
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public long getRecoveryTimeMs() { return recoveryTimeMs; }
        public void setRecoveryTimeMs(long recoveryTimeMs) { this.recoveryTimeMs = recoveryTimeMs; }
        
        public List<String> getActionsTaken() { return actionsTaken; }
        public void setActionsTaken(List<String> actionsTaken) { this.actionsTaken = actionsTaken; }
        
        public Map<String, Object> getMetrics() { return metrics; }
        public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
    }
    
    /**
     * Retry Result
     */
    class RetryResult {
        private String operationId;
        private String serviceName;
        private boolean success;
        private String message;
        private int retryAttempt;
        private long retryTimeMs;
        
        // Constructors, getters, setters
        public RetryResult() {}
        
        public RetryResult(String operationId, String serviceName, boolean success, String message) {
            this.operationId = operationId;
            this.serviceName = serviceName;
            this.success = success;
            this.message = message;
        }
        
        public String getOperationId() { return operationId; }
        public void setOperationId(String operationId) { this.operationId = operationId; }
        
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getRetryAttempt() { return retryAttempt; }
        public void setRetryAttempt(int retryAttempt) { this.retryAttempt = retryAttempt; }
        
        public long getRetryTimeMs() { return retryTimeMs; }
        public void setRetryTimeMs(long retryTimeMs) { this.retryTimeMs = retryTimeMs; }
    }
    
    /**
     * Scaling Result
     */
    class ScalingResult {
        private String serviceName;
        private boolean success;
        private String message;
        private int currentInstances;
        private int targetInstances;
        private long scalingTimeMs;
        
        // Constructors, getters, setters
        public ScalingResult() {}
        
        public ScalingResult(String serviceName, boolean success, String message) {
            this.serviceName = serviceName;
            this.success = success;
            this.message = message;
        }
        
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getCurrentInstances() { return currentInstances; }
        public void setCurrentInstances(int currentInstances) { this.currentInstances = currentInstances; }
        
        public int getTargetInstances() { return targetInstances; }
        public void setTargetInstances(int targetInstances) { this.targetInstances = targetInstances; }
        
        public long getScalingTimeMs() { return scalingTimeMs; }
        public void setScalingTimeMs(long scalingTimeMs) { this.scalingTimeMs = scalingTimeMs; }
    }
    
    /**
     * Self-Healing Statistics
     */
    class SelfHealingStatistics {
        private long totalRecoveryAttempts;
        private long successfulRecoveries;
        private long failedRecoveries;
        private double averageRecoveryTimeMs;
        private long totalRetryAttempts;
        private long successfulRetries;
        private long failedRetries;
        private double averageRetryTimeMs;
        private long totalScalingOperations;
        private long successfulScalingOperations;
        private long failedScalingOperations;
        private String lastRecoveryTime;
        private String lastRetryTime;
        private String lastScalingTime;
        
        // Constructors, getters, setters
        public SelfHealingStatistics() {}
        
        public long getTotalRecoveryAttempts() { return totalRecoveryAttempts; }
        public void setTotalRecoveryAttempts(long totalRecoveryAttempts) { this.totalRecoveryAttempts = totalRecoveryAttempts; }
        
        public long getSuccessfulRecoveries() { return successfulRecoveries; }
        public void setSuccessfulRecoveries(long successfulRecoveries) { this.successfulRecoveries = successfulRecoveries; }
        
        public long getFailedRecoveries() { return failedRecoveries; }
        public void setFailedRecoveries(long failedRecoveries) { this.failedRecoveries = failedRecoveries; }
        
        public double getAverageRecoveryTimeMs() { return averageRecoveryTimeMs; }
        public void setAverageRecoveryTimeMs(double averageRecoveryTimeMs) { this.averageRecoveryTimeMs = averageRecoveryTimeMs; }
        
        public long getTotalRetryAttempts() { return totalRetryAttempts; }
        public void setTotalRetryAttempts(long totalRetryAttempts) { this.totalRetryAttempts = totalRetryAttempts; }
        
        public long getSuccessfulRetries() { return successfulRetries; }
        public void setSuccessfulRetries(long successfulRetries) { this.successfulRetries = successfulRetries; }
        
        public long getFailedRetries() { return failedRetries; }
        public void setFailedRetries(long failedRetries) { this.failedRetries = failedRetries; }
        
        public double getAverageRetryTimeMs() { return averageRetryTimeMs; }
        public void setAverageRetryTimeMs(double averageRetryTimeMs) { this.averageRetryTimeMs = averageRetryTimeMs; }
        
        public long getTotalScalingOperations() { return totalScalingOperations; }
        public void setTotalScalingOperations(long totalScalingOperations) { this.totalScalingOperations = totalScalingOperations; }
        
        public long getSuccessfulScalingOperations() { return successfulScalingOperations; }
        public void setSuccessfulScalingOperations(long successfulScalingOperations) { this.successfulScalingOperations = successfulScalingOperations; }
        
        public long getFailedScalingOperations() { return failedScalingOperations; }
        public void setFailedScalingOperations(long failedScalingOperations) { this.failedScalingOperations = failedScalingOperations; }
        
        public String getLastRecoveryTime() { return lastRecoveryTime; }
        public void setLastRecoveryTime(String lastRecoveryTime) { this.lastRecoveryTime = lastRecoveryTime; }
        
        public String getLastRetryTime() { return lastRetryTime; }
        public void setLastRetryTime(String lastRetryTime) { this.lastRetryTime = lastRetryTime; }
        
        public String getLastScalingTime() { return lastScalingTime; }
        public void setLastScalingTime(String lastScalingTime) { this.lastScalingTime = lastScalingTime; }
    }
    
    /**
     * Auto-Healing Rules
     */
    class AutoHealingRules {
        private boolean enabled;
        private int maxRecoveryAttempts;
        private int recoveryIntervalMinutes;
        private boolean autoRetryEnabled;
        private int maxRetryAttempts;
        private int retryIntervalMinutes;
        private boolean autoScalingEnabled;
        private int minInstances;
        private int maxInstances;
        private double cpuThreshold;
        private double memoryThreshold;
        private double errorRateThreshold;
        private List<String> recoveryActions;
        private Map<String, Object> customRules;
        
        // Constructors, getters, setters
        public AutoHealingRules() {}
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public int getMaxRecoveryAttempts() { return maxRecoveryAttempts; }
        public void setMaxRecoveryAttempts(int maxRecoveryAttempts) { this.maxRecoveryAttempts = maxRecoveryAttempts; }
        
        public int getRecoveryIntervalMinutes() { return recoveryIntervalMinutes; }
        public void setRecoveryIntervalMinutes(int recoveryIntervalMinutes) { this.recoveryIntervalMinutes = recoveryIntervalMinutes; }
        
        public boolean isAutoRetryEnabled() { return autoRetryEnabled; }
        public void setAutoRetryEnabled(boolean autoRetryEnabled) { this.autoRetryEnabled = autoRetryEnabled; }
        
        public int getMaxRetryAttempts() { return maxRetryAttempts; }
        public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
        
        public int getRetryIntervalMinutes() { return retryIntervalMinutes; }
        public void setRetryIntervalMinutes(int retryIntervalMinutes) { this.retryIntervalMinutes = retryIntervalMinutes; }
        
        public boolean isAutoScalingEnabled() { return autoScalingEnabled; }
        public void setAutoScalingEnabled(boolean autoScalingEnabled) { this.autoScalingEnabled = autoScalingEnabled; }
        
        public int getMinInstances() { return minInstances; }
        public void setMinInstances(int minInstances) { this.minInstances = minInstances; }
        
        public int getMaxInstances() { return maxInstances; }
        public void setMaxInstances(int maxInstances) { this.maxInstances = maxInstances; }
        
        public double getCpuThreshold() { return cpuThreshold; }
        public void setCpuThreshold(double cpuThreshold) { this.cpuThreshold = cpuThreshold; }
        
        public double getMemoryThreshold() { return memoryThreshold; }
        public void setMemoryThreshold(double memoryThreshold) { this.memoryThreshold = memoryThreshold; }
        
        public double getErrorRateThreshold() { return errorRateThreshold; }
        public void setErrorRateThreshold(double errorRateThreshold) { this.errorRateThreshold = errorRateThreshold; }
        
        public List<String> getRecoveryActions() { return recoveryActions; }
        public void setRecoveryActions(List<String> recoveryActions) { this.recoveryActions = recoveryActions; }
        
        public Map<String, Object> getCustomRules() { return customRules; }
        public void setCustomRules(Map<String, Object> customRules) { this.customRules = customRules; }
    }
}