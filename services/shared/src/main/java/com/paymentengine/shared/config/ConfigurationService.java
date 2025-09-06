package com.paymentengine.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Dynamic Configuration Service
 * Provides runtime configuration management with multi-tenant support
 */
@Service
public class ConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    private static final String CONFIG_CACHE_PREFIX = "config:";
    private static final String TENANT_CONFIG_PREFIX = "tenant_config:";
    private static final String GLOBAL_CONFIG_PREFIX = "global_config:";
    
    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    public ConfigurationService(JdbcTemplate jdbcTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }
    
    // ============================================================================
    // GLOBAL CONFIGURATION MANAGEMENT
    // ============================================================================
    
    /**
     * Get configuration value with tenant support
     */
    @Cacheable(value = "configuration", key = "#tenantId + ':' + #configKey")
    public String getConfigValue(String tenantId, String configKey) {
        logger.debug("Getting configuration value for tenant: {}, key: {}", tenantId, configKey);
        
        try {
            // Try tenant-specific configuration first
            if (tenantId != null && !tenantId.isEmpty()) {
                String tenantValue = getTenantSpecificConfig(tenantId, configKey);
                if (tenantValue != null) {
                    return tenantValue;
                }
            }
            
            // Fall back to global configuration
            return getGlobalConfig(configKey);
            
        } catch (Exception e) {
            logger.error("Error getting configuration value for {}: {}", configKey, e.getMessage());
            return null;
        }
    }
    
    /**
     * Set configuration value with tenant support
     */
    @CacheEvict(value = "configuration", key = "#tenantId + ':' + #configKey")
    public void setConfigValue(String tenantId, String configKey, String configValue, String environment) {
        logger.info("Setting configuration value for tenant: {}, key: {}, environment: {}", tenantId, configKey, environment);
        
        try {
            if (tenantId != null && !tenantId.isEmpty()) {
                setTenantSpecificConfig(tenantId, configKey, configValue, environment);
            } else {
                setGlobalConfig(configKey, configValue, environment);
            }
            
            // Also cache in Redis for fast access
            String cacheKey = (tenantId != null ? TENANT_CONFIG_PREFIX + tenantId + ":" : GLOBAL_CONFIG_PREFIX) + configKey;
            redisTemplate.opsForValue().set(cacheKey, configValue, 1, TimeUnit.HOURS);
            
        } catch (Exception e) {
            logger.error("Error setting configuration value for {}: {}", configKey, e.getMessage(), e);
            throw new RuntimeException("Failed to set configuration", e);
        }
    }
    
    /**
     * Get all configuration for a tenant
     */
    @Cacheable(value = "tenant-config", key = "#tenantId")
    public Map<String, Object> getTenantConfiguration(String tenantId) {
        logger.debug("Getting all configuration for tenant: {}", tenantId);
        
        try {
            String sql = """
                SELECT config_key, config_value, environment
                FROM config.system_config 
                WHERE (tenant_id = ? OR tenant_id IS NULL)
                ORDER BY tenant_id NULLS LAST
                """;
            
            List<Map<String, Object>> configs = jdbcTemplate.queryForList(sql, tenantId);
            Map<String, Object> result = new HashMap<>();
            
            configs.forEach(config -> {
                String key = (String) config.get("config_key");
                Object value = config.get("config_value");
                result.put(key, value);
            });
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting tenant configuration for {}: {}", tenantId, e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    // ============================================================================
    // PAYMENT TYPE CONFIGURATION
    // ============================================================================
    
    /**
     * Add new payment type dynamically
     */
    public void addPaymentType(String tenantId, Map<String, Object> paymentTypeConfig) {
        logger.info("Adding new payment type for tenant: {}", tenantId);
        
        try {
            String sql = """
                INSERT INTO payment_engine.payment_types 
                (code, name, description, is_synchronous, max_amount, min_amount, processing_fee, configuration, tenant_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?)
                """;
            
            jdbcTemplate.update(sql,
                paymentTypeConfig.get("code"),
                paymentTypeConfig.get("name"),
                paymentTypeConfig.get("description"),
                paymentTypeConfig.get("isSynchronous"),
                paymentTypeConfig.get("maxAmount"),
                paymentTypeConfig.get("minAmount"),
                paymentTypeConfig.get("processingFee"),
                objectMapper.writeValueAsString(paymentTypeConfig.get("configuration")),
                tenantId
            );
            
            // Clear cache
            evictPaymentTypeCache(tenantId);
            
            logger.info("Payment type added successfully: {}", paymentTypeConfig.get("code"));
            
        } catch (Exception e) {
            logger.error("Error adding payment type: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add payment type", e);
        }
    }
    
    /**
     * Update payment type configuration dynamically
     */
    @CacheEvict(value = "payment-types", key = "#tenantId")
    public void updatePaymentType(String tenantId, String paymentTypeCode, Map<String, Object> updates) {
        logger.info("Updating payment type {} for tenant: {}", paymentTypeCode, tenantId);
        
        try {
            StringBuilder sql = new StringBuilder("UPDATE payment_engine.payment_types SET ");
            List<Object> params = new ArrayList<>();
            
            if (updates.containsKey("isActive")) {
                sql.append("is_active = ?, ");
                params.add(updates.get("isActive"));
            }
            
            if (updates.containsKey("maxAmount")) {
                sql.append("max_amount = ?, ");
                params.add(updates.get("maxAmount"));
            }
            
            if (updates.containsKey("minAmount")) {
                sql.append("min_amount = ?, ");
                params.add(updates.get("minAmount"));
            }
            
            if (updates.containsKey("processingFee")) {
                sql.append("processing_fee = ?, ");
                params.add(updates.get("processingFee"));
            }
            
            if (updates.containsKey("configuration")) {
                sql.append("configuration = ?::jsonb, ");
                params.add(objectMapper.writeValueAsString(updates.get("configuration")));
            }
            
            sql.append("updated_at = CURRENT_TIMESTAMP ");
            sql.append("WHERE code = ? AND (tenant_id = ? OR tenant_id IS NULL)");
            
            params.add(paymentTypeCode);
            params.add(tenantId);
            
            int updated = jdbcTemplate.update(sql.toString(), params.toArray());
            
            if (updated == 0) {
                throw new RuntimeException("Payment type not found or not updated");
            }
            
            // Clear cache
            evictPaymentTypeCache(tenantId);
            
            logger.info("Payment type updated successfully: {}", paymentTypeCode);
            
        } catch (Exception e) {
            logger.error("Error updating payment type {}: {}", paymentTypeCode, e.getMessage(), e);
            throw new RuntimeException("Failed to update payment type", e);
        }
    }
    
    // ============================================================================
    // KAFKA TOPIC CONFIGURATION
    // ============================================================================
    
    /**
     * Add new Kafka topic dynamically
     */
    public void addKafkaTopic(String tenantId, String topicName, int partitions, int replicationFactor, Map<String, Object> topicConfig) {
        logger.info("Adding Kafka topic {} for tenant: {}", topicName, tenantId);
        
        try {
            String sql = """
                INSERT INTO config.kafka_topics 
                (topic_name, partitions, replication_factor, configuration, tenant_id)
                VALUES (?, ?, ?, ?::jsonb, ?)
                """;
            
            jdbcTemplate.update(sql,
                topicName,
                partitions,
                replicationFactor,
                objectMapper.writeValueAsString(topicConfig),
                tenantId
            );
            
            // Create the topic in Kafka (this would integrate with Kafka Admin API)
            createKafkaTopicInCluster(topicName, partitions, replicationFactor, topicConfig);
            
            logger.info("Kafka topic added successfully: {}", topicName);
            
        } catch (Exception e) {
            logger.error("Error adding Kafka topic {}: {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to add Kafka topic", e);
        }
    }
    
    /**
     * Update Kafka topic configuration
     */
    public void updateKafkaTopicConfig(String tenantId, String topicName, Map<String, Object> configUpdates) {
        logger.info("Updating Kafka topic {} configuration for tenant: {}", topicName, tenantId);
        
        try {
            // Update in database
            String sql = """
                UPDATE config.kafka_topics 
                SET configuration = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                WHERE topic_name = ? AND (tenant_id = ? OR tenant_id IS NULL)
                """;
            
            jdbcTemplate.update(sql,
                objectMapper.writeValueAsString(configUpdates),
                topicName,
                tenantId
            );
            
            // Update in Kafka cluster
            updateKafkaTopicInCluster(topicName, configUpdates);
            
        } catch (Exception e) {
            logger.error("Error updating Kafka topic config: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update Kafka topic configuration", e);
        }
    }
    
    // ============================================================================
    // API ENDPOINT CONFIGURATION
    // ============================================================================
    
    /**
     * Add new API endpoint dynamically
     */
    public void addApiEndpoint(String tenantId, String endpointPath, String httpMethod, String serviceName, Map<String, Object> endpointConfig) {
        logger.info("Adding API endpoint {} for tenant: {}", endpointPath, tenantId);
        
        try {
            String sql = """
                INSERT INTO config.api_endpoints 
                (endpoint_path, http_method, service_name, tenant_id, rate_limit_per_minute, requires_auth, configuration)
                VALUES (?, ?, ?, ?, ?, ?, ?::jsonb)
                """;
            
            jdbcTemplate.update(sql,
                endpointPath,
                httpMethod,
                serviceName,
                tenantId,
                endpointConfig.getOrDefault("rateLimitPerMinute", 1000),
                endpointConfig.getOrDefault("requiresAuth", true),
                objectMapper.writeValueAsString(endpointConfig)
            );
            
            // Refresh API Gateway routes (this would integrate with Spring Cloud Gateway)
            refreshApiGatewayRoutes();
            
            logger.info("API endpoint added successfully: {} {}", httpMethod, endpointPath);
            
        } catch (Exception e) {
            logger.error("Error adding API endpoint: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add API endpoint", e);
        }
    }
    
    // ============================================================================
    // TENANT MANAGEMENT
    // ============================================================================
    
    /**
     * Create new tenant with default configuration
     */
    public void createTenant(String tenantId, String tenantName, Map<String, Object> tenantConfig) {
        logger.info("Creating new tenant: {} ({})", tenantName, tenantId);
        
        try {
            // Create tenant record
            String sql = """
                INSERT INTO config.tenants 
                (tenant_id, tenant_name, status, configuration, created_at)
                VALUES (?, ?, 'ACTIVE', ?::jsonb, CURRENT_TIMESTAMP)
                """;
            
            jdbcTemplate.update(sql, tenantId, tenantName, objectMapper.writeValueAsString(tenantConfig));
            
            // Initialize default configurations for tenant
            initializeTenantDefaults(tenantId, tenantConfig);
            
            // Create tenant-specific Kafka topics
            createTenantKafkaTopics(tenantId);
            
            // Set up tenant-specific database schemas if needed
            createTenantSchema(tenantId);
            
            logger.info("Tenant created successfully: {}", tenantId);
            
        } catch (Exception e) {
            logger.error("Error creating tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Failed to create tenant", e);
        }
    }
    
    /**
     * Get tenant configuration
     */
    @Cacheable(value = "tenant-info", key = "#tenantId")
    public Map<String, Object> getTenantInfo(String tenantId) {
        logger.debug("Getting tenant info for: {}", tenantId);
        
        try {
            String sql = """
                SELECT tenant_id, tenant_name, status, configuration, created_at, updated_at
                FROM config.tenants 
                WHERE tenant_id = ?
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, tenantId);
            
            if (results.isEmpty()) {
                throw new IllegalArgumentException("Tenant not found: " + tenantId);
            }
            
            return results.get(0);
            
        } catch (Exception e) {
            logger.error("Error getting tenant info for {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Failed to get tenant information", e);
        }
    }
    
    // ============================================================================
    // FEATURE FLAGS & A/B TESTING
    // ============================================================================
    
    /**
     * Check if feature is enabled for tenant
     */
    @Cacheable(value = "feature-flags", key = "#tenantId + ':' + #featureName")
    public boolean isFeatureEnabled(String tenantId, String featureName) {
        logger.debug("Checking feature flag {} for tenant: {}", featureName, tenantId);
        
        try {
            String sql = """
                SELECT COALESCE(
                    (SELECT (config_value->>'enabled')::boolean 
                     FROM config.feature_flags 
                     WHERE feature_name = ? AND tenant_id = ?),
                    (SELECT (config_value->>'enabled')::boolean 
                     FROM config.feature_flags 
                     WHERE feature_name = ? AND tenant_id IS NULL),
                    false
                ) as enabled
                """;
            
            List<Boolean> results = jdbcTemplate.queryForList(sql, Boolean.class, featureName, tenantId, featureName);
            return !results.isEmpty() && Boolean.TRUE.equals(results.get(0));
            
        } catch (Exception e) {
            logger.error("Error checking feature flag {}: {}", featureName, e.getMessage());
            return false;
        }
    }
    
    /**
     * Set feature flag for tenant
     */
    @CacheEvict(value = "feature-flags", key = "#tenantId + ':' + #featureName")
    public void setFeatureFlag(String tenantId, String featureName, boolean enabled, Map<String, Object> config) {
        logger.info("Setting feature flag {} = {} for tenant: {}", featureName, enabled, tenantId);
        
        try {
            String sql = """
                INSERT INTO config.feature_flags (tenant_id, feature_name, config_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (tenant_id, feature_name) 
                DO UPDATE SET config_value = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                """;
            
            Map<String, Object> featureConfig = new HashMap<>(config);
            featureConfig.put("enabled", enabled);
            String configJson = objectMapper.writeValueAsString(featureConfig);
            
            jdbcTemplate.update(sql, tenantId, featureName, configJson, configJson);
            
            // Clear cache
            String cacheKey = TENANT_CONFIG_PREFIX + tenantId + ":" + featureName;
            redisTemplate.delete(cacheKey);
            
        } catch (Exception e) {
            logger.error("Error setting feature flag: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to set feature flag", e);
        }
    }
    
    // ============================================================================
    // RATE LIMITING CONFIGURATION
    // ============================================================================
    
    /**
     * Get rate limit configuration for tenant and endpoint
     */
    @Cacheable(value = "rate-limits", key = "#tenantId + ':' + #endpoint")
    public Map<String, Object> getRateLimitConfig(String tenantId, String endpoint) {
        logger.debug("Getting rate limit config for tenant: {}, endpoint: {}", tenantId, endpoint);
        
        try {
            String sql = """
                SELECT rate_limit_per_minute, burst_capacity, window_size_seconds, configuration
                FROM config.rate_limits 
                WHERE (tenant_id = ? OR tenant_id IS NULL) 
                AND (endpoint_pattern = ? OR endpoint_pattern = '*')
                ORDER BY tenant_id NULLS LAST, endpoint_pattern DESC
                LIMIT 1
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, tenantId, endpoint);
            
            if (results.isEmpty()) {
                // Return default rate limits
                return Map.of(
                    "rateLimitPerMinute", 1000,
                    "burstCapacity", 1500,
                    "windowSizeSeconds", 60
                );
            }
            
            return results.get(0);
            
        } catch (Exception e) {
            logger.error("Error getting rate limit config: {}", e.getMessage());
            return Map.of("rateLimitPerMinute", 1000, "burstCapacity", 1500, "windowSizeSeconds", 60);
        }
    }
    
    /**
     * Update rate limit configuration
     */
    @CacheEvict(value = "rate-limits", key = "#tenantId + ':' + #endpoint")
    public void updateRateLimitConfig(String tenantId, String endpoint, Map<String, Object> rateLimitConfig) {
        logger.info("Updating rate limit config for tenant: {}, endpoint: {}", tenantId, endpoint);
        
        try {
            String sql = """
                INSERT INTO config.rate_limits 
                (tenant_id, endpoint_pattern, rate_limit_per_minute, burst_capacity, window_size_seconds, configuration)
                VALUES (?, ?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (tenant_id, endpoint_pattern)
                DO UPDATE SET 
                    rate_limit_per_minute = ?,
                    burst_capacity = ?,
                    window_size_seconds = ?,
                    configuration = ?::jsonb,
                    updated_at = CURRENT_TIMESTAMP
                """;
            
            Object rateLimitPerMinute = rateLimitConfig.get("rateLimitPerMinute");
            Object burstCapacity = rateLimitConfig.get("burstCapacity");
            Object windowSizeSeconds = rateLimitConfig.get("windowSizeSeconds");
            String configJson = objectMapper.writeValueAsString(rateLimitConfig);
            
            jdbcTemplate.update(sql,
                tenantId, endpoint, rateLimitPerMinute, burstCapacity, windowSizeSeconds, configJson,
                rateLimitPerMinute, burstCapacity, windowSizeSeconds, configJson
            );
            
        } catch (Exception e) {
            logger.error("Error updating rate limit config: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update rate limit configuration", e);
        }
    }
    
    // ============================================================================
    // CIRCUIT BREAKER CONFIGURATION
    // ============================================================================
    
    /**
     * Get circuit breaker configuration
     */
    @Cacheable(value = "circuit-breaker-config", key = "#tenantId + ':' + #serviceName")
    public Map<String, Object> getCircuitBreakerConfig(String tenantId, String serviceName) {
        logger.debug("Getting circuit breaker config for tenant: {}, service: {}", tenantId, serviceName);
        
        try {
            String sql = """
                SELECT failure_threshold, timeout_duration, half_open_max_calls, configuration
                FROM config.circuit_breaker_config 
                WHERE (tenant_id = ? OR tenant_id IS NULL) 
                AND service_name = ?
                ORDER BY tenant_id NULLS LAST
                LIMIT 1
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, tenantId, serviceName);
            
            if (results.isEmpty()) {
                // Return default circuit breaker config
                return Map.of(
                    "failureThreshold", 50,
                    "timeoutDuration", 10000,
                    "halfOpenMaxCalls", 3,
                    "slidingWindowSize", 10
                );
            }
            
            return results.get(0);
            
        } catch (Exception e) {
            logger.error("Error getting circuit breaker config: {}", e.getMessage());
            return Map.of("failureThreshold", 50, "timeoutDuration", 10000);
        }
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private String getTenantSpecificConfig(String tenantId, String configKey) {
        try {
            String sql = """
                SELECT config_value 
                FROM config.system_config 
                WHERE config_key = ? AND tenant_id = ?
                """;
            
            List<String> results = jdbcTemplate.queryForList(sql, String.class, configKey, tenantId);
            return results.isEmpty() ? null : results.get(0);
            
        } catch (Exception e) {
            logger.debug("No tenant-specific config found for {}: {}", configKey, e.getMessage());
            return null;
        }
    }
    
    private String getGlobalConfig(String configKey) {
        try {
            String sql = """
                SELECT config_value 
                FROM config.system_config 
                WHERE config_key = ? AND tenant_id IS NULL
                """;
            
            List<String> results = jdbcTemplate.queryForList(sql, String.class, configKey);
            return results.isEmpty() ? null : results.get(0);
            
        } catch (Exception e) {
            logger.debug("No global config found for {}: {}", configKey, e.getMessage());
            return null;
        }
    }
    
    private void setTenantSpecificConfig(String tenantId, String configKey, String configValue, String environment) {
        String sql = """
            INSERT INTO config.system_config (tenant_id, config_key, config_value, environment)
            VALUES (?, ?, ?::jsonb, ?)
            ON CONFLICT (tenant_id, config_key, environment)
            DO UPDATE SET config_value = ?::jsonb, updated_at = CURRENT_TIMESTAMP
            """;
        
        jdbcTemplate.update(sql, tenantId, configKey, configValue, environment, configValue);
    }
    
    private void setGlobalConfig(String configKey, String configValue, String environment) {
        String sql = """
            INSERT INTO config.system_config (config_key, config_value, environment)
            VALUES (?, ?::jsonb, ?)
            ON CONFLICT (config_key, environment) WHERE tenant_id IS NULL
            DO UPDATE SET config_value = ?::jsonb, updated_at = CURRENT_TIMESTAMP
            """;
        
        jdbcTemplate.update(sql, configKey, configValue, environment, configValue);
    }
    
    private void evictPaymentTypeCache(String tenantId) {
        String cacheKey = TENANT_CONFIG_PREFIX + tenantId + ":payment-types";
        redisTemplate.delete(cacheKey);
    }
    
    private void initializeTenantDefaults(String tenantId, Map<String, Object> tenantConfig) {
        // Initialize default payment types for tenant
        // Initialize default rate limits for tenant  
        // Initialize default feature flags for tenant
        logger.debug("Initializing tenant defaults for: {}", tenantId);
    }
    
    private void createTenantKafkaTopics(String tenantId) {
        // Create tenant-specific Kafka topics
        logger.debug("Creating tenant Kafka topics for: {}", tenantId);
    }
    
    private void createTenantSchema(String tenantId) {
        // Create tenant-specific database schema if needed
        logger.debug("Creating tenant schema for: {}", tenantId);
    }
    
    private void createKafkaTopicInCluster(String topicName, int partitions, int replicationFactor, Map<String, Object> config) {
        // Integration with Kafka Admin API to create topics
        logger.debug("Creating Kafka topic in cluster: {}", topicName);
    }
    
    private void updateKafkaTopicInCluster(String topicName, Map<String, Object> config) {
        // Integration with Kafka Admin API to update topic configuration
        logger.debug("Updating Kafka topic in cluster: {}", topicName);
    }
    
    private void refreshApiGatewayRoutes() {
        // Integration with Spring Cloud Gateway to refresh routes
        logger.debug("Refreshing API Gateway routes");
    }
    
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
}