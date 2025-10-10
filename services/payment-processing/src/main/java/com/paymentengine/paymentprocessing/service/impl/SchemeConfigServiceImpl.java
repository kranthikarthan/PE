package com.paymentengine.paymentprocessing.service.impl;

import com.paymentengine.paymentprocessing.dto.SchemeConfigRequest;
import com.paymentengine.paymentprocessing.dto.SchemeConfigResponse;
import com.paymentengine.paymentprocessing.dto.SchemeTestRequest;
import com.paymentengine.paymentprocessing.dto.SchemeTestResponse;
import com.paymentengine.paymentprocessing.service.SchemeConfigService;
import com.paymentengine.paymentprocessing.service.SchemeMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of scheme configuration service
 */
@Service
public class SchemeConfigServiceImpl implements SchemeConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemeConfigServiceImpl.class);
    
    private final SchemeMessageService schemeMessageService;
    
    // In-memory storage for demo purposes - in production, use proper database
    private final Map<String, SchemeConfigResponse> configurations = new HashMap<>();
    private final Map<String, List<SchemeTestResponse>> testHistory = new HashMap<>();
    
    @Autowired
    public SchemeConfigServiceImpl(SchemeMessageService schemeMessageService) {
        this.schemeMessageService = schemeMessageService;
        initializeDefaultConfigurations();
    }
    
    @Override
    public Page<SchemeConfigResponse> getConfigurations(
            String name, Boolean isActive, String interactionMode, 
            String messageFormat, String responseMode, Pageable pageable) {
        
        logger.debug("Getting configurations with filters - name: {}, isActive: {}, mode: {}, format: {}, response: {}", 
                name, isActive, interactionMode, messageFormat, responseMode);
        
        List<SchemeConfigResponse> filteredConfigs = configurations.values().stream()
                .filter(config -> name == null || config.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(config -> isActive == null || config.getIsActive().equals(isActive))
                .filter(config -> interactionMode == null || config.getInteractionMode().name().equals(interactionMode))
                .filter(config -> messageFormat == null || config.getMessageFormat().name().equals(messageFormat))
                .filter(config -> responseMode == null || config.getResponseMode().name().equals(responseMode))
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .toList();
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredConfigs.size());
        List<SchemeConfigResponse> pageContent = start < filteredConfigs.size() 
                ? filteredConfigs.subList(start, end) 
                : Collections.emptyList();
        
        return new PageImpl<>(pageContent, pageable, filteredConfigs.size());
    }
    
    @Override
    public SchemeConfigResponse getConfiguration(String configId) {
        logger.debug("Getting configuration: {}", configId);
        
        SchemeConfigResponse config = configurations.get(configId);
        if (config == null) {
            throw new IllegalArgumentException("Configuration not found: " + configId);
        }
        
        return config;
    }
    
    @Override
    public SchemeConfigResponse createConfiguration(SchemeConfigRequest request) {
        logger.info("Creating configuration: {}", request.getName());
        
        String configId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        SchemeConfigResponse config = new SchemeConfigResponse(
                configId,
                request.getName(),
                request.getDescription(),
                request.getIsActive(),
                request.getInteractionMode(),
                request.getMessageFormat(),
                request.getResponseMode(),
                request.getTimeoutMs(),
                request.getRetryPolicy(),
                request.getAuthentication(),
                request.getEndpoints(),
                now,
                now,
                "system", // In real implementation, get from security context
                "system"
        );
        
        configurations.put(configId, config);
        testHistory.put(configId, new ArrayList<>());
        
        logger.info("Configuration created successfully: {}", configId);
        return config;
    }
    
    @Override
    public SchemeConfigResponse updateConfiguration(String configId, SchemeConfigRequest request) {
        logger.info("Updating configuration: {}", configId);
        
        SchemeConfigResponse existingConfig = getConfiguration(configId);
        
        SchemeConfigResponse updatedConfig = new SchemeConfigResponse(
                configId,
                request.getName(),
                request.getDescription(),
                request.getIsActive(),
                request.getInteractionMode(),
                request.getMessageFormat(),
                request.getResponseMode(),
                request.getTimeoutMs(),
                request.getRetryPolicy(),
                request.getAuthentication(),
                request.getEndpoints(),
                existingConfig.getCreatedAt(),
                Instant.now(),
                existingConfig.getCreatedBy(),
                "system" // In real implementation, get from security context
        );
        
        configurations.put(configId, updatedConfig);
        
        logger.info("Configuration updated successfully: {}", configId);
        return updatedConfig;
    }
    
    @Override
    public void deleteConfiguration(String configId) {
        logger.info("Deleting configuration: {}", configId);
        
        if (!configurations.containsKey(configId)) {
            throw new IllegalArgumentException("Configuration not found: " + configId);
        }
        
        configurations.remove(configId);
        testHistory.remove(configId);
        
        logger.info("Configuration deleted successfully: {}", configId);
    }
    
    @Override
    public SchemeConfigResponse cloneConfiguration(String configId, String newName) {
        logger.info("Cloning configuration: {} with new name: {}", configId, newName);
        
        SchemeConfigResponse originalConfig = getConfiguration(configId);
        String newConfigId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        SchemeConfigResponse clonedConfig = new SchemeConfigResponse(
                newConfigId,
                newName,
                originalConfig.getDescription() + " (Cloned from " + originalConfig.getName() + ")",
                false, // Cloned configurations start as inactive
                originalConfig.getInteractionMode(),
                originalConfig.getMessageFormat(),
                originalConfig.getResponseMode(),
                originalConfig.getTimeoutMs(),
                originalConfig.getRetryPolicy(),
                originalConfig.getAuthentication(),
                originalConfig.getEndpoints(),
                now,
                now,
                "system",
                "system"
        );
        
        configurations.put(newConfigId, clonedConfig);
        testHistory.put(newConfigId, new ArrayList<>());
        
        logger.info("Configuration cloned successfully: {} -> {}", configId, newConfigId);
        return clonedConfig;
    }
    
    @Override
    public SchemeConfigResponse toggleConfigurationStatus(String configId, Boolean isActive) {
        logger.info("Toggling configuration status: {} -> {}", configId, isActive);
        
        SchemeConfigResponse config = getConfiguration(configId);
        
        SchemeConfigResponse updatedConfig = new SchemeConfigResponse(
                config.getId(),
                config.getName(),
                config.getDescription(),
                isActive,
                config.getInteractionMode(),
                config.getMessageFormat(),
                config.getResponseMode(),
                config.getTimeoutMs(),
                config.getRetryPolicy(),
                config.getAuthentication(),
                config.getEndpoints(),
                config.getCreatedAt(),
                Instant.now(),
                config.getCreatedBy(),
                "system"
        );
        
        configurations.put(configId, updatedConfig);
        
        logger.info("Configuration status toggled successfully: {}", configId);
        return updatedConfig;
    }
    
    @Override
    public SchemeTestResponse testConfiguration(SchemeTestRequest request) {
        logger.info("Testing configuration: {}", request.getConfigId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate configuration exists
            SchemeConfigResponse config = getConfiguration(request.getConfigId());
            
            if (Boolean.TRUE.equals(request.getValidateOnly())) {
                // Only validate, don't send message
                Map<String, Object> validation = validateConfiguration(request.getConfigId());
                long responseTime = System.currentTimeMillis() - startTime;
                
                return new SchemeTestResponse(
                        true,
                        responseTime,
                        null,
                        null,
                        Collections.emptyList(),
                        Instant.now()
                );
            } else {
                // Send actual test message
                CompletableFuture<com.paymentengine.paymentprocessing.dto.SchemeMessageResponse> future = 
                        schemeMessageService.sendAsyncMessage(request.getConfigId(), request.getTestMessage());
                
                com.paymentengine.paymentprocessing.dto.SchemeMessageResponse response = future.get();
                long responseTime = System.currentTimeMillis() - startTime;
                
                SchemeTestResponse testResponse = new SchemeTestResponse(
                        response.getStatus() == com.paymentengine.paymentprocessing.dto.SchemeMessageResponse.MessageStatus.SUCCESS,
                        responseTime,
                        response,
                        response.getErrorDetails(),
                        Collections.emptyList(),
                        Instant.now()
                );
                
                // Store test result in history
                testHistory.computeIfAbsent(request.getConfigId(), k -> new ArrayList<>()).add(testResponse);
                
                return testResponse;
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            logger.error("Configuration test failed: {}", e.getMessage());
            
            com.paymentengine.paymentprocessing.dto.SchemeMessageResponse.ErrorDetails errorDetails = 
                    new com.paymentengine.paymentprocessing.dto.SchemeMessageResponse.ErrorDetails(
                            "TEST_ERROR",
                            e.getMessage(),
                            com.paymentengine.paymentprocessing.dto.SchemeMessageResponse.ErrorDetails.ErrorCategory.PROCESSING,
                            false,
                            Map.of("exception", e.getClass().getSimpleName())
                    );
            
            SchemeTestResponse testResponse = new SchemeTestResponse(
                    false,
                    responseTime,
                    null,
                    errorDetails,
                    Collections.emptyList(),
                    Instant.now()
            );
            
            // Store test result in history
            testHistory.computeIfAbsent(request.getConfigId(), k -> new ArrayList<>()).add(testResponse);
            
            return testResponse;
        }
    }
    
    @Override
    public Map<String, Object> validateConfiguration(String configId) {
        logger.debug("Validating configuration: {}", configId);
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            SchemeConfigResponse config = getConfiguration(configId);
            
            // Validate endpoints
            if (config.getEndpoints().isEmpty()) {
                errors.add("At least one endpoint is required");
            }
            
            for (int i = 0; i < config.getEndpoints().size(); i++) {
                var endpoint = config.getEndpoints().get(i);
                if (endpoint.getUrl() == null || endpoint.getUrl().trim().isEmpty()) {
                    errors.add("Endpoint " + (i + 1) + " URL is required");
                }
                if (endpoint.getSupportedMessageTypes().isEmpty()) {
                    warnings.add("Endpoint " + (i + 1) + " has no supported message types");
                }
            }
            
            // Validate timeout
            if (config.getTimeoutMs() <= 0) {
                errors.add("Timeout must be positive");
            }
            
            // Validate retry policy
            if (config.getRetryPolicy().getMaxRetries() < 0) {
                errors.add("Max retries cannot be negative");
            }
            
        } catch (Exception e) {
            errors.add("Configuration not found: " + e.getMessage());
        }
        
        return Map.of(
                "valid", errors.isEmpty(),
                "errors", errors,
                "warnings", warnings,
                "timestamp", Instant.now().toString()
        );
    }
    
    @Override
    public Page<SchemeTestResponse> getTestHistory(String configId, Pageable pageable) {
        logger.debug("Getting test history for configuration: {}", configId);
        
        List<SchemeTestResponse> history = testHistory.getOrDefault(configId, Collections.emptyList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), history.size());
        List<SchemeTestResponse> pageContent = start < history.size() 
                ? history.subList(start, end) 
                : Collections.emptyList();
        
        return new PageImpl<>(pageContent, pageable, history.size());
    }
    
    @Override
    public Map<String, Object> getStatistics(String configId, String fromDate, String toDate) {
        logger.debug("Getting statistics for configuration: {}", configId);
        
        // Mock statistics - in production, calculate from actual data
        return Map.of(
                "totalRequests", 1250,
                "successfulRequests", 1180,
                "failedRequests", 70,
                "averageResponseTime", 245.5,
                "successRate", 94.4,
                "formatBreakdown", Map.of(
                        "JSON", 800,
                        "XML", 450
                ),
                "modeBreakdown", Map.of(
                        "SYNCHRONOUS", 900,
                        "ASYNCHRONOUS", 350
                ),
                "lastUpdated", Instant.now().toString()
        );
    }
    
    @Override
    public Map<String, Object> getConfigurationHealth(String configId) {
        logger.debug("Getting health status for configuration: {}", configId);
        
        try {
            SchemeConfigResponse config = getConfiguration(configId);
            
            // Mock health check - in production, perform actual health checks
            return Map.of(
                    "status", config.getIsActive() ? "HEALTHY" : "UNHEALTHY",
                    "lastChecked", Instant.now().toString(),
                    "responseTime", 150,
                    "errorRate", 2.5,
                    "details", Map.of(
                            "endpoints", config.getEndpoints().size(),
                            "activeEndpoints", config.getEndpoints().stream()
                                    .mapToInt(ep -> ep.getIsActive() ? 1 : 0)
                                    .sum()
                    )
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "UNHEALTHY",
                    "lastChecked", Instant.now().toString(),
                    "responseTime", 0,
                    "errorRate", 100.0,
                    "details", Map.of("error", e.getMessage())
            );
        }
    }
    
    @Override
    public Map<String, Object> getAllConfigurationHealth() {
        logger.debug("Getting health status for all configurations");
        
        Map<String, Object> healthStatuses = new HashMap<>();
        for (String configId : configurations.keySet()) {
            healthStatuses.put(configId, getConfigurationHealth(configId));
        }
        
        return healthStatuses;
    }
    
    @Override
    public Map<String, Object> getMessageTemplates() {
        logger.debug("Getting message templates");
        
        return Map.of(
                "templates", Arrays.asList(
                        Map.of(
                                "id", "pain001-template",
                                "name", "PAIN.001 Credit Transfer",
                                "messageType", "pain001",
                                "format", "JSON",
                                "description", "Customer Credit Transfer Initiation"
                        ),
                        Map.of(
                                "id", "pain002-template",
                                "name", "PAIN.002 Status Report",
                                "messageType", "pain002",
                                "format", "JSON",
                                "description", "Customer Payment Status Report"
                        ),
                        Map.of(
                                "id", "camt055-template",
                                "name", "CAMT.055 Cancellation Request",
                                "messageType", "camt055",
                                "format", "XML",
                                "description", "Customer Payment Cancellation Request"
                        )
                ),
                "totalTemplates", 3
        );
    }
    
    @Override
    public com.paymentengine.paymentprocessing.dto.SchemeMessageRequest createMessageFromTemplate(
            String templateId, Map<String, Object> variables) {
        
        logger.info("Creating message from template: {}", templateId);
        
        // Mock template processing - in production, use actual template engine
        return new com.paymentengine.paymentprocessing.dto.SchemeMessageRequest(
                "pain001",
                "MSG-" + System.currentTimeMillis(),
                "CORR-" + System.currentTimeMillis(),
                com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.MessageFormat.JSON,
                com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.InteractionMode.SYNCHRONOUS,
                Map.of("templateId", templateId, "variables", variables),
                Map.of("createdFromTemplate", true, "templateId", templateId)
        );
    }
    
    private void initializeDefaultConfigurations() {
        logger.info("Initializing default scheme configurations");
        
        // Create default configurations for demo
        String configId1 = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        SchemeConfigResponse defaultConfig1 = new SchemeConfigResponse(
                configId1,
                "Real-Time Payment Scheme",
                "Synchronous JSON-based real-time payment processing",
                true,
                com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.InteractionMode.SYNCHRONOUS,
                com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.MessageFormat.JSON,
                com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.ResponseMode.IMMEDIATE,
                10000L,
                new com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.RetryPolicy(3, 1000L, true, Arrays.asList(500, 502, 503, 504)),
                new com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.AuthenticationConfig(
                        com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.AuthenticationConfig.AuthType.API_KEY
                ),
                Arrays.asList(
                        new com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.EndpointConfig(
                                "Primary Endpoint",
                                "https://api.scheme.com/v1/payments",
                                com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.EndpointConfig.HttpMethod.POST,
                                true,
                                10000L,
                                Map.of("Content-Type", "application/json"),
                                Arrays.asList("pain001", "pain002"),
                                1
                        )
                ),
                now,
                now,
                "system",
                "system"
        );
        
        configurations.put(configId1, defaultConfig1);
        testHistory.put(configId1, new ArrayList<>());
        
        logger.info("Default configurations initialized");
    }
}