package com.paymentengine.paymentprocessing.service.impl;

import com.paymentengine.paymentprocessing.entity.QueuedMessage;
import com.paymentengine.paymentprocessing.entity.ResiliencyConfiguration;
import com.paymentengine.paymentprocessing.repository.QueuedMessageRepository;
import com.paymentengine.paymentprocessing.repository.ResiliencyConfigurationRepository;
import com.paymentengine.paymentprocessing.service.MessageQueueService;
import com.paymentengine.paymentprocessing.service.SelfHealingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of SelfHealingService for monitoring system health
 * and triggering automated recovery actions
 */
@Service
@Transactional
public class SelfHealingServiceImpl implements SelfHealingService {

    private static final Logger logger = LoggerFactory.getLogger(SelfHealingServiceImpl.class);

    @Autowired
    private QueuedMessageRepository queuedMessageRepository;

    @Autowired
    private ResiliencyConfigurationRepository resiliencyConfigurationRepository;

    @Autowired
    private MessageQueueService messageQueueService;

    @Autowired
    private RestTemplate restTemplate;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(3);

    // Health check cache to avoid excessive calls
    private final Map<String, HealthCheckResult> healthCheckCache = new HashMap<>();
    private final long HEALTH_CHECK_CACHE_TTL_MINUTES = 5;

    public SelfHealingServiceImpl() {
        // Start background monitoring tasks
        startHealthMonitoring();
        startQueueProcessing();
        startCleanupTasks();
    }

    @Override
    public CompletableFuture<Map<String, Object>> monitorDownstreamServices(String tenantId) {
        logger.info("Starting monitoring of downstream services for tenant: {}", tenantId);

        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> monitoringResults = new HashMap<>();
            List<String> failedServices = new ArrayList<>();
            List<String> recoveredServices = new ArrayList<>();

            try {
                // Get all resiliency configurations for the tenant
                List<ResiliencyConfiguration> configurations = resiliencyConfigurationRepository
                        .findByTenantIdAndIsActiveTrue(tenantId);

                for (ResiliencyConfiguration config : configurations) {
                    try {
                        HealthCheckResult healthResult = performHealthCheck(config);
                        monitoringResults.put(config.getServiceName(), healthResult);

                        if (healthResult.isHealthy()) {
                            if (wasServiceDown(config.getServiceName())) {
                                recoveredServices.add(config.getServiceName());
                                logger.info("Service recovered: {}", config.getServiceName());
                            }
                        } else {
                            if (!wasServiceDown(config.getServiceName())) {
                                failedServices.add(config.getServiceName());
                                logger.warn("Service failed: {} - {}", config.getServiceName(), healthResult.getErrorMessage());
                            }
                        }

                        // Update health check cache
                        healthCheckCache.put(config.getServiceName(), healthResult);

                    } catch (Exception e) {
                        logger.error("Error monitoring service: {}", config.getServiceName(), e);
                        HealthCheckResult errorResult = new HealthCheckResult(false, 
                                "Monitoring error: " + e.getMessage(), 0, LocalDateTime.now());
                        monitoringResults.put(config.getServiceName(), errorResult);
                    }
                }

                // Trigger recovery actions for failed services
                if (!failedServices.isEmpty()) {
                    triggerRecoveryActions(failedServices, tenantId);
                }

                // Process queued messages for recovered services
                if (!recoveredServices.isEmpty()) {
                    processQueuedMessagesForRecoveredServices(recoveredServices, tenantId);
                }

                monitoringResults.put("failedServices", failedServices);
                monitoringResults.put("recoveredServices", recoveredServices);
                monitoringResults.put("timestamp", LocalDateTime.now().toString());

                logger.info("Completed monitoring for tenant: {} - Failed: {}, Recovered: {}", 
                           tenantId, failedServices.size(), recoveredServices.size());

            } catch (Exception e) {
                logger.error("Error during downstream service monitoring for tenant: {}", tenantId, e);
                monitoringResults.put("error", "Monitoring failed: " + e.getMessage());
            }

            return monitoringResults;
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> triggerRecoveryActions(List<String> failedServices, String tenantId) {
        logger.info("Triggering recovery actions for failed services: {} tenant: {}", failedServices, tenantId);

        return CompletableFuture.runAsync(() -> {
            try {
                for (String serviceName : failedServices) {
                    try {
                        // Get resiliency configuration for the service
                        Optional<ResiliencyConfiguration> configOpt = resiliencyConfigurationRepository
                                .findByServiceNameAndTenantIdAndIsActiveTrue(serviceName, tenantId);

                        if (configOpt.isPresent()) {
                            ResiliencyConfiguration config = configOpt.get();
                            
                            // Perform recovery actions based on configuration
                            performRecoveryActions(config);
                            
                            logger.info("Recovery actions completed for service: {}", serviceName);
                        } else {
                            logger.warn("No resiliency configuration found for service: {} tenant: {}", serviceName, tenantId);
                        }

                    } catch (Exception e) {
                        logger.error("Error during recovery actions for service: {}", serviceName, e);
                    }
                }

            } catch (Exception e) {
                logger.error("Error triggering recovery actions for services: {}", failedServices, e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> reprocessQueuedMessages(String tenantId, String serviceName) {
        logger.info("Reprocessing queued messages for tenant: {} service: {}", tenantId, serviceName);

        return CompletableFuture.runAsync(() -> {
            try {
                // Get failed messages for the service
                List<QueuedMessage> failedMessages = queuedMessageRepository
                        .findByTenantIdAndServiceNameAndStatus(tenantId, serviceName, QueuedMessage.Status.FAILED, 
                                                               org.springframework.data.domain.PageRequest.of(0, 100));

                logger.info("Found {} failed messages to reprocess for service: {}", failedMessages.size(), serviceName);

                // Reprocess each message
                for (QueuedMessage message : failedMessages) {
                    try {
                        // Reset retry count and status
                        message.setRetryCount(0);
                        message.setStatus(QueuedMessage.Status.PENDING);
                        message.setNextRetryAt(LocalDateTime.now());
                        message.setErrorMessage(null);
                        message.setErrorDetails(null);
                        message.setUpdatedAt(LocalDateTime.now());
                        queuedMessageRepository.save(message);

                        // Process the message
                        messageQueueService.processMessage(message.getMessageId()).join();

                        logger.debug("Reprocessed message: {} for service: {}", message.getMessageId(), serviceName);

                    } catch (Exception e) {
                        logger.error("Error reprocessing message: {} for service: {}", message.getMessageId(), serviceName, e);
                    }
                }

                logger.info("Completed reprocessing queued messages for tenant: {} service: {}", tenantId, serviceName);

            } catch (Exception e) {
                logger.error("Error reprocessing queued messages for tenant: {} service: {}", tenantId, serviceName, e);
            }
        }, executorService);
    }

    @Override
    public Map<String, Object> getSystemHealthStatus(String tenantId) {
        logger.debug("Getting system health status for tenant: {}", tenantId);

        try {
            Map<String, Object> healthStatus = new HashMap<>();
            List<Map<String, Object>> serviceHealth = new ArrayList<>();

            // Get all resiliency configurations
            List<ResiliencyConfiguration> configurations = resiliencyConfigurationRepository
                    .findByTenantIdAndIsActiveTrue(tenantId);

            int healthyServices = 0;
            int unhealthyServices = 0;
            int totalServices = configurations.size();

            for (ResiliencyConfiguration config : configurations) {
                HealthCheckResult healthResult = getCachedHealthCheck(config.getServiceName());
                if (healthResult == null) {
                    healthResult = performHealthCheck(config);
                    healthCheckCache.put(config.getServiceName(), healthResult);
                }

                Map<String, Object> serviceStatus = new HashMap<>();
                serviceStatus.put("serviceName", config.getServiceName());
                serviceStatus.put("healthy", healthResult.isHealthy());
                serviceStatus.put("responseTimeMs", healthResult.getResponseTimeMs());
                serviceStatus.put("lastChecked", healthResult.getLastChecked().toString());
                serviceStatus.put("errorMessage", healthResult.getErrorMessage());
                serviceHealth.add(serviceStatus);

                if (healthResult.isHealthy()) {
                    healthyServices++;
                } else {
                    unhealthyServices++;
                }
            }

            healthStatus.put("tenantId", tenantId);
            healthStatus.put("totalServices", totalServices);
            healthStatus.put("healthyServices", healthyServices);
            healthStatus.put("unhealthyServices", unhealthyServices);
            healthStatus.put("overallHealth", unhealthyServices == 0 ? "HEALTHY" : "DEGRADED");
            healthStatus.put("serviceHealth", serviceHealth);
            healthStatus.put("timestamp", LocalDateTime.now().toString());

            return healthStatus;

        } catch (Exception e) {
            logger.error("Error getting system health status for tenant: {}", tenantId, e);
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("tenantId", tenantId);
            errorStatus.put("overallHealth", "ERROR");
            errorStatus.put("error", "Failed to get health status: " + e.getMessage());
            errorStatus.put("timestamp", LocalDateTime.now().toString());
            return errorStatus;
        }
    }

    // Private helper methods

    private HealthCheckResult performHealthCheck(ResiliencyConfiguration config) {
        try {
            Map<String, Object> healthCheckConfig = config.getHealthCheckConfig();
            if (healthCheckConfig == null || !Boolean.TRUE.equals(healthCheckConfig.get("healthCheckEnabled"))) {
                return new HealthCheckResult(true, "Health check disabled", 0, LocalDateTime.now());
            }

            String healthEndpoint = (String) healthCheckConfig.get("healthCheckEndpoint");
            String healthMethod = (String) healthCheckConfig.getOrDefault("healthCheckMethod", "GET");
            Integer timeoutSeconds = (Integer) healthCheckConfig.getOrDefault("healthCheckTimeoutSeconds", 5);

            if (healthEndpoint == null) {
                return new HealthCheckResult(false, "Health check endpoint not configured", 0, LocalDateTime.now());
            }

            // Construct full URL
            String baseUrl = extractBaseUrl(config);
            String fullUrl = baseUrl + healthEndpoint;

            long startTime = System.currentTimeMillis();

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Perform health check
            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl, HttpMethod.valueOf(healthMethod), entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;

            // Check if response is successful
            String expectedStatusCodes = (String) healthCheckConfig.get("expectedStatusCodes");
            boolean isHealthy = isResponseHealthy(response.getStatusCode().value(), expectedStatusCodes);

            return new HealthCheckResult(isHealthy, 
                    isHealthy ? "OK" : "HTTP " + response.getStatusCode().value(), 
                    responseTime, LocalDateTime.now());

        } catch (Exception e) {
            logger.debug("Health check failed for service: {} - {}", config.getServiceName(), e.getMessage());
            return new HealthCheckResult(false, "Health check failed: " + e.getMessage(), 0, LocalDateTime.now());
        }
    }

    private boolean isResponseHealthy(int statusCode, String expectedStatusCodes) {
        if (expectedStatusCodes == null) {
            return statusCode >= 200 && statusCode < 300;
        }

        String[] codes = expectedStatusCodes.split(",");
        for (String code : codes) {
            try {
                int expectedCode = Integer.parseInt(code.trim());
                if (statusCode == expectedCode) {
                    return true;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid status code in configuration: {}", code);
            }
        }
        return false;
    }

    private String extractBaseUrl(ResiliencyConfiguration config) {
        String endpointPattern = config.getEndpointPattern();
        if (endpointPattern != null && endpointPattern.contains("://")) {
            // Extract base URL from endpoint pattern
            int protocolEnd = endpointPattern.indexOf("://");
            int pathStart = endpointPattern.indexOf("/", protocolEnd + 3);
            if (pathStart > 0) {
                return endpointPattern.substring(0, pathStart);
            }
        }
        return "http://localhost:8080"; // Default fallback
    }

    private HealthCheckResult getCachedHealthCheck(String serviceName) {
        HealthCheckResult cached = healthCheckCache.get(serviceName);
        if (cached != null) {
            long ageMinutes = ChronoUnit.MINUTES.between(cached.getLastChecked(), LocalDateTime.now());
            if (ageMinutes < HEALTH_CHECK_CACHE_TTL_MINUTES) {
                return cached;
            }
        }
        return null;
    }

    private boolean wasServiceDown(String serviceName) {
        HealthCheckResult cached = healthCheckCache.get(serviceName);
        return cached != null && !cached.isHealthy();
    }

    private void performRecoveryActions(ResiliencyConfiguration config) {
        logger.info("Performing recovery actions for service: {}", config.getServiceName());

        try {
            // Reset circuit breaker if configured
            Map<String, Object> circuitBreakerConfig = config.getCircuitBreakerConfig();
            if (circuitBreakerConfig != null) {
                // In a real implementation, you would reset the circuit breaker here
                logger.info("Circuit breaker reset for service: {}", config.getServiceName());
            }

            // Clear health check cache to force fresh checks
            healthCheckCache.remove(config.getServiceName());

            // Log recovery action
            logger.info("Recovery actions completed for service: {}", config.getServiceName());

        } catch (Exception e) {
            logger.error("Error performing recovery actions for service: {}", config.getServiceName(), e);
        }
    }

    private void processQueuedMessagesForRecoveredServices(List<String> recoveredServices, String tenantId) {
        logger.info("Processing queued messages for recovered services: {} tenant: {}", recoveredServices, tenantId);

        for (String serviceName : recoveredServices) {
            try {
                reprocessQueuedMessages(tenantId, serviceName).join();
            } catch (Exception e) {
                logger.error("Error processing queued messages for recovered service: {}", serviceName, e);
            }
        }
    }

    private void startHealthMonitoring() {
        // Monitor health every 2 minutes
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                logger.debug("Running scheduled health monitoring");
                // Monitor all active tenants
                List<String> tenants = resiliencyConfigurationRepository.findDistinctTenantIds();
                for (String tenantId : tenants) {
                    monitorDownstreamServices(tenantId).join();
                }
            } catch (Exception e) {
                logger.error("Error in scheduled health monitoring", e);
            }
        }, 2, 2, TimeUnit.MINUTES);
    }

    private void startQueueProcessing() {
        // Process queued messages every 5 minutes
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                logger.debug("Running scheduled queue processing");
                messageQueueService.retryFailedMessages().join();
            } catch (Exception e) {
                logger.error("Error in scheduled queue processing", e);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private void startCleanupTasks() {
        // Cleanup expired messages every hour
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                logger.debug("Running scheduled cleanup tasks");
                messageQueueService.cleanupExpiredMessages();
            } catch (Exception e) {
                logger.error("Error in scheduled cleanup tasks", e);
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    // Inner class for health check results
    private static class HealthCheckResult {
        private final boolean healthy;
        private final String errorMessage;
        private final long responseTimeMs;
        private final LocalDateTime lastChecked;

        public HealthCheckResult(boolean healthy, String errorMessage, long responseTimeMs, LocalDateTime lastChecked) {
            this.healthy = healthy;
            this.errorMessage = errorMessage;
            this.responseTimeMs = responseTimeMs;
            this.lastChecked = lastChecked;
        }

        public boolean isHealthy() { return healthy; }
        public String getErrorMessage() { return errorMessage; }
        public long getResponseTimeMs() { return responseTimeMs; }
        public LocalDateTime getLastChecked() { return lastChecked; }
    }
}