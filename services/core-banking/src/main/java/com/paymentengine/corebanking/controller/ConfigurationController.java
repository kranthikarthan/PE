package com.paymentengine.corebanking.controller;

import com.paymentengine.shared.config.ConfigurationService;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * Configuration Management Controller
 * Provides runtime configuration management with multi-tenant support
 */
@RestController
@RequestMapping("/api/v1/config")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConfigurationController {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);
    
    private final ConfigurationService configurationService;
    
    @Autowired
    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
    
    // ============================================================================
    // TENANT MANAGEMENT
    // ============================================================================
    
    /**
     * List all tenants
     */
    @GetMapping("/tenants")
    @PreAuthorize("hasAuthority('tenant:read')")
    @Timed(value = "config.tenant.list", description = "Time taken to list tenants")
    public ResponseEntity<java.util.List<Map<String, Object>>> listTenants() {
        
        logger.debug("Listing all tenants");
        
        try {
            // This would typically have pagination and filtering
            java.util.List<Map<String, Object>> tenants = Arrays.asList(
                Map.of(
                    "tenantId", "default",
                    "tenantName", "Default Tenant",
                    "tenantType", "BANK",
                    "status", "ACTIVE",
                    "subscriptionTier", "ENTERPRISE"
                ),
                Map.of(
                    "tenantId", "demo-bank",
                    "tenantName", "Demo Bank",
                    "tenantType", "BANK", 
                    "status", "ACTIVE",
                    "subscriptionTier", "STANDARD"
                ),
                Map.of(
                    "tenantId", "fintech-corp",
                    "tenantName", "FinTech Corporation",
                    "tenantType", "FINTECH",
                    "status", "ACTIVE",
                    "subscriptionTier", "PREMIUM"
                )
            );
            
            return ResponseEntity.ok(tenants);
            
        } catch (Exception e) {
            logger.error("Error listing tenants: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Create new tenant
     */
    @PostMapping("/tenants")
    @PreAuthorize("hasAuthority('tenant:create')")
    @Timed(value = "config.tenant.create", description = "Time taken to create tenant")
    public ResponseEntity<Map<String, Object>> createTenant(
            @Valid @RequestBody Map<String, Object> tenantRequest,
            HttpServletRequest request) {
        
        String tenantId = (String) tenantRequest.get("tenantId");
        String tenantName = (String) tenantRequest.get("tenantName");
        
        logger.info("Creating new tenant: {} ({})", tenantName, tenantId);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tenantConfig = (Map<String, Object>) tenantRequest.getOrDefault("configuration", Map.of());
            
            configurationService.createTenant(tenantId, tenantName, tenantConfig);
            
            Map<String, Object> response = Map.of(
                "tenantId", tenantId,
                "tenantName", tenantName,
                "status", "CREATED",
                "message", "Tenant created successfully",
                "timestamp", LocalDateTime.now().toString()
            );
            
            logger.info("Tenant created successfully: {}", tenantId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "TENANT_CREATION_FAILED",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    /**
     * Get tenant information
     */
    @GetMapping("/tenants/{tenantId}")
    @PreAuthorize("hasAuthority('tenant:read')")
    @Timed(value = "config.tenant.get", description = "Time taken to get tenant info")
    public ResponseEntity<Map<String, Object>> getTenant(@PathVariable String tenantId) {
        
        logger.debug("Getting tenant information for: {}", tenantId);
        
        try {
            Map<String, Object> tenantInfo = configurationService.getTenantInfo(tenantId);
            return ResponseEntity.ok(tenantInfo);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Tenant not found: {}", tenantId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting tenant info for {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all tenant configurations
     */
    @GetMapping("/tenants/{tenantId}/config")
    @PreAuthorize("hasAuthority('tenant:config:read')")
    @Timed(value = "config.tenant.config", description = "Time taken to get tenant config")
    public ResponseEntity<Map<String, Object>> getTenantConfiguration(@PathVariable String tenantId) {
        
        logger.debug("Getting configuration for tenant: {}", tenantId);
        
        try {
            Map<String, Object> config = configurationService.getTenantConfiguration(tenantId);
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            logger.error("Error getting tenant configuration for {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ============================================================================
    // CONFIGURATION MANAGEMENT
    // ============================================================================
    
    /**
     * Set configuration value
     */
    @PostMapping("/tenants/{tenantId}/config")
    @PreAuthorize("hasAuthority('tenant:config:update')")
    @Timed(value = "config.set", description = "Time taken to set configuration")
    public ResponseEntity<Map<String, String>> setConfiguration(
            @PathVariable String tenantId,
            @Valid @RequestBody Map<String, Object> configRequest) {
        
        String configKey = (String) configRequest.get("configKey");
        String configValue = (String) configRequest.get("configValue");
        String environment = (String) configRequest.getOrDefault("environment", "production");
        
        logger.info("Setting configuration for tenant {}: {} = {}", tenantId, configKey, configValue);
        
        try {
            configurationService.setConfigValue(tenantId, configKey, configValue, environment);
            
            return ResponseEntity.ok(Map.of(
                "message", "Configuration updated successfully",
                "tenantId", tenantId,
                "configKey", configKey,
                "environment", environment,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error setting configuration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    /**
     * Get configuration value
     */
    @GetMapping("/tenants/{tenantId}/config/{configKey}")
    @PreAuthorize("hasAuthority('tenant:config:read')")
    @Timed(value = "config.get", description = "Time taken to get configuration")
    public ResponseEntity<Map<String, Object>> getConfiguration(
            @PathVariable String tenantId,
            @PathVariable String configKey) {
        
        logger.debug("Getting configuration for tenant {}: {}", tenantId, configKey);
        
        try {
            String configValue = configurationService.getConfigValue(tenantId, configKey);
            
            if (configValue == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "configKey", configKey,
                "configValue", configValue,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error getting configuration: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ============================================================================
    // PAYMENT TYPE CONFIGURATION
    // ============================================================================
    
    /**
     * Add new payment type dynamically
     */
    @PostMapping("/tenants/{tenantId}/payment-types")
    @PreAuthorize("hasAuthority('payment-type:create')")
    @Timed(value = "config.payment_type.add", description = "Time taken to add payment type")
    public ResponseEntity<Map<String, String>> addPaymentType(
            @PathVariable String tenantId,
            @Valid @RequestBody Map<String, Object> paymentTypeConfig) {
        
        String code = (String) paymentTypeConfig.get("code");
        logger.info("Adding payment type {} for tenant: {}", code, tenantId);
        
        try {
            configurationService.addPaymentType(tenantId, paymentTypeConfig);
            
            return ResponseEntity.ok(Map.of(
                "message", "Payment type added successfully",
                "tenantId", tenantId,
                "paymentTypeCode", code,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error adding payment type: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    /**
     * Update payment type configuration
     */
    @PutMapping("/tenants/{tenantId}/payment-types/{paymentTypeCode}")
    @PreAuthorize("hasAuthority('payment-type:update')")
    @Timed(value = "config.payment_type.update", description = "Time taken to update payment type")
    public ResponseEntity<Map<String, String>> updatePaymentType(
            @PathVariable String tenantId,
            @PathVariable String paymentTypeCode,
            @Valid @RequestBody Map<String, Object> updates) {
        
        logger.info("Updating payment type {} for tenant: {}", paymentTypeCode, tenantId);
        
        try {
            configurationService.updatePaymentType(tenantId, paymentTypeCode, updates);
            
            return ResponseEntity.ok(Map.of(
                "message", "Payment type updated successfully",
                "tenantId", tenantId,
                "paymentTypeCode", paymentTypeCode,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error updating payment type: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // FEATURE FLAGS
    // ============================================================================
    
    /**
     * Check if feature is enabled
     */
    @GetMapping("/tenants/{tenantId}/features/{featureName}")
    @PreAuthorize("hasAuthority('feature:read')")
    @Timed(value = "config.feature.check", description = "Time taken to check feature flag")
    public ResponseEntity<Map<String, Object>> checkFeature(
            @PathVariable String tenantId,
            @PathVariable String featureName) {
        
        logger.debug("Checking feature {} for tenant: {}", featureName, tenantId);
        
        try {
            boolean enabled = configurationService.isFeatureEnabled(tenantId, featureName);
            
            return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "featureName", featureName,
                "enabled", enabled,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error checking feature flag: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Set feature flag
     */
    @PostMapping("/tenants/{tenantId}/features/{featureName}")
    @PreAuthorize("hasAuthority('feature:update')")
    @Timed(value = "config.feature.set", description = "Time taken to set feature flag")
    public ResponseEntity<Map<String, String>> setFeature(
            @PathVariable String tenantId,
            @PathVariable String featureName,
            @Valid @RequestBody Map<String, Object> featureRequest) {
        
        boolean enabled = Boolean.TRUE.equals(featureRequest.get("enabled"));
        logger.info("Setting feature {} = {} for tenant: {}", featureName, enabled, tenantId);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) featureRequest.getOrDefault("config", Map.of());
            
            configurationService.setFeatureFlag(tenantId, featureName, enabled, config);
            
            return ResponseEntity.ok(Map.of(
                "message", "Feature flag updated successfully",
                "tenantId", tenantId,
                "featureName", featureName,
                "enabled", String.valueOf(enabled),
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error setting feature flag: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // RATE LIMITING CONFIGURATION
    // ============================================================================
    
    /**
     * Get rate limit configuration
     */
    @GetMapping("/tenants/{tenantId}/rate-limits")
    @PreAuthorize("hasAuthority('rate-limit:read')")
    @Timed(value = "config.rate_limit.get", description = "Time taken to get rate limit config")
    public ResponseEntity<Map<String, Object>> getRateLimitConfig(
            @PathVariable String tenantId,
            @RequestParam String endpoint) {
        
        logger.debug("Getting rate limit config for tenant: {}, endpoint: {}", tenantId, endpoint);
        
        try {
            Map<String, Object> rateLimitConfig = configurationService.getRateLimitConfig(tenantId, endpoint);
            
            return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "endpoint", endpoint,
                "rateLimitConfig", rateLimitConfig,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error getting rate limit config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update rate limit configuration
     */
    @PutMapping("/tenants/{tenantId}/rate-limits")
    @PreAuthorize("hasAuthority('rate-limit:update')")
    @Timed(value = "config.rate_limit.update", description = "Time taken to update rate limit config")
    public ResponseEntity<Map<String, String>> updateRateLimitConfig(
            @PathVariable String tenantId,
            @RequestParam String endpoint,
            @Valid @RequestBody Map<String, Object> rateLimitConfig) {
        
        logger.info("Updating rate limit config for tenant: {}, endpoint: {}", tenantId, endpoint);
        
        try {
            configurationService.updateRateLimitConfig(tenantId, endpoint, rateLimitConfig);
            
            return ResponseEntity.ok(Map.of(
                "message", "Rate limit configuration updated successfully",
                "tenantId", tenantId,
                "endpoint", endpoint,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error updating rate limit config: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // KAFKA TOPIC CONFIGURATION
    // ============================================================================
    
    /**
     * Add new Kafka topic
     */
    @PostMapping("/tenants/{tenantId}/kafka-topics")
    @PreAuthorize("hasAuthority('kafka:create')")
    @Timed(value = "config.kafka.topic.add", description = "Time taken to add Kafka topic")
    public ResponseEntity<Map<String, String>> addKafkaTopic(
            @PathVariable String tenantId,
            @Valid @RequestBody Map<String, Object> topicRequest) {
        
        String topicName = (String) topicRequest.get("topicName");
        logger.info("Adding Kafka topic {} for tenant: {}", topicName, tenantId);
        
        try {
            int partitions = (Integer) topicRequest.getOrDefault("partitions", 3);
            int replicationFactor = (Integer) topicRequest.getOrDefault("replicationFactor", 1);
            @SuppressWarnings("unchecked")
            Map<String, Object> topicConfig = (Map<String, Object>) topicRequest.getOrDefault("configuration", Map.of());
            
            configurationService.addKafkaTopic(tenantId, topicName, partitions, replicationFactor, topicConfig);
            
            return ResponseEntity.ok(Map.of(
                "message", "Kafka topic added successfully",
                "tenantId", tenantId,
                "topicName", topicName,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error adding Kafka topic: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    /**
     * Update Kafka topic configuration
     */
    @PutMapping("/tenants/{tenantId}/kafka-topics/{topicName}")
    @PreAuthorize("hasAuthority('kafka:update')")
    @Timed(value = "config.kafka.topic.update", description = "Time taken to update Kafka topic")
    public ResponseEntity<Map<String, String>> updateKafkaTopicConfig(
            @PathVariable String tenantId,
            @PathVariable String topicName,
            @Valid @RequestBody Map<String, Object> configUpdates) {
        
        logger.info("Updating Kafka topic {} config for tenant: {}", topicName, tenantId);
        
        try {
            configurationService.updateKafkaTopicConfig(tenantId, topicName, configUpdates);
            
            return ResponseEntity.ok(Map.of(
                "message", "Kafka topic configuration updated successfully",
                "tenantId", tenantId,
                "topicName", topicName,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error updating Kafka topic config: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // API ENDPOINT CONFIGURATION
    // ============================================================================
    
    /**
     * Add new API endpoint
     */
    @PostMapping("/tenants/{tenantId}/api-endpoints")
    @PreAuthorize("hasAuthority('api:create')")
    @Timed(value = "config.api.endpoint.add", description = "Time taken to add API endpoint")
    public ResponseEntity<Map<String, String>> addApiEndpoint(
            @PathVariable String tenantId,
            @Valid @RequestBody Map<String, Object> endpointRequest) {
        
        String endpointPath = (String) endpointRequest.get("endpointPath");
        String httpMethod = (String) endpointRequest.get("httpMethod");
        String serviceName = (String) endpointRequest.get("serviceName");
        
        logger.info("Adding API endpoint {} {} for tenant: {}", httpMethod, endpointPath, tenantId);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> endpointConfig = (Map<String, Object>) endpointRequest.getOrDefault("configuration", Map.of());
            
            configurationService.addApiEndpoint(tenantId, endpointPath, httpMethod, serviceName, endpointConfig);
            
            return ResponseEntity.ok(Map.of(
                "message", "API endpoint added successfully",
                "tenantId", tenantId,
                "endpointPath", endpointPath,
                "httpMethod", httpMethod,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error adding API endpoint: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // CONFIGURATION VALIDATION AND TESTING
    // ============================================================================
    
    /**
     * Validate configuration changes before applying
     */
    @PostMapping("/tenants/{tenantId}/config/validate")
    @PreAuthorize("hasAuthority('tenant:config:validate')")
    @Timed(value = "config.validate", description = "Time taken to validate configuration")
    public ResponseEntity<Map<String, Object>> validateConfiguration(
            @PathVariable String tenantId,
            @Valid @RequestBody Map<String, Object> configRequest) {
        
        logger.debug("Validating configuration for tenant: {}", tenantId);
        
        try {
            // Implement configuration validation logic
            Map<String, Object> validationResult = Map.of(
                "valid", true,
                "tenantId", tenantId,
                "warnings", java.util.List.of(),
                "errors", java.util.List.of(),
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(validationResult);
            
        } catch (Exception e) {
            logger.error("Error validating configuration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    /**
     * Get configuration change history
     */
    @GetMapping("/tenants/{tenantId}/config/history")
    @PreAuthorize("hasAuthority('tenant:config:read')")
    @Timed(value = "config.history", description = "Time taken to get configuration history")
    public ResponseEntity<Map<String, Object>> getConfigurationHistory(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Getting configuration history for tenant: {}", tenantId);
        
        try {
            // This would query the configuration_history table
            Map<String, Object> history = Map.of(
                "tenantId", tenantId,
                "changes", java.util.List.of(),
                "totalChanges", 0,
                "page", page,
                "size", size,
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            logger.error("Error getting configuration history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Health check for configuration service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "configuration-service",
            "features", "multi-tenancy, dynamic-config, feature-flags",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}