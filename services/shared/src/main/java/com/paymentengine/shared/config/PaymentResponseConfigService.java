package com.paymentengine.shared.config;

import com.paymentengine.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing payment response configurations
 * Supports configurable response modes: SYNCHRONOUS, ASYNCHRONOUS, KAFKA_TOPIC
 */
@Service
public class PaymentResponseConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentResponseConfigService.class);
    
    public enum ResponseMode {
        SYNCHRONOUS,    // Immediate API response
        ASYNCHRONOUS,   // Async API response with callback
        KAFKA_TOPIC     // Response published to Kafka topic
    }
    
    private final JdbcTemplate jdbcTemplate;
    private final ConfigurationService configurationService;
    
    @Autowired
    public PaymentResponseConfigService(JdbcTemplate jdbcTemplate, ConfigurationService configurationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.configurationService = configurationService;
    }
    
    /**
     * Get response configuration for a payment type
     */
    @Cacheable(value = "payment-response-config", key = "#tenantId + ':' + #paymentType")
    public PaymentResponseConfig getResponseConfig(String tenantId, String paymentType) {
        logger.debug("Getting response configuration for tenant: {}, paymentType: {}", tenantId, paymentType);
        
        try {
            // Get payment type configuration from database
            String sql = """
                SELECT configuration
                FROM payment_engine.payment_types 
                WHERE code = ? AND (tenant_id = ? OR tenant_id IS NULL)
                ORDER BY tenant_id NULLS LAST
                LIMIT 1
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, paymentType, tenantId);
            
            if (results.isEmpty()) {
                logger.warn("Payment type not found: {} for tenant: {}", paymentType, tenantId);
                return getDefaultResponseConfig();
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> configuration = (Map<String, Object>) results.get(0).get("configuration");
            
            return parseResponseConfig(configuration);
            
        } catch (Exception e) {
            logger.error("Error getting response configuration for {}: {}", paymentType, e.getMessage(), e);
            return getDefaultResponseConfig();
        }
    }
    
    /**
     * Update response configuration for a payment type
     */
    public void updateResponseConfig(String tenantId, String paymentType, PaymentResponseConfig responseConfig) {
        logger.info("Updating response configuration for tenant: {}, paymentType: {}", tenantId, paymentType);
        
        try {
            // Get current configuration
            String selectSql = """
                SELECT configuration
                FROM payment_engine.payment_types 
                WHERE code = ? AND (tenant_id = ? OR tenant_id IS NULL)
                ORDER BY tenant_id NULLS LAST
                LIMIT 1
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(selectSql, paymentType, tenantId);
            
            if (results.isEmpty()) {
                throw new IllegalArgumentException("Payment type not found: " + paymentType);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> currentConfig = (Map<String, Object>) results.get(0).get("configuration");
            
            if (currentConfig == null) {
                currentConfig = new HashMap<>();
            }
            
            // Update response configuration
            currentConfig.put("response_mode", responseConfig.getResponseMode().name());
            currentConfig.put("kafka_response_config", responseConfig.getKafkaConfig());
            
            // Update in database
            String updateSql = """
                UPDATE payment_engine.payment_types 
                SET configuration = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                WHERE code = ? AND (tenant_id = ? OR tenant_id IS NULL)
                """;
            
            jdbcTemplate.update(updateSql, 
                objectMapper.writeValueAsString(currentConfig),
                paymentType, 
                tenantId);
            
            // Clear cache
            String cacheKey = tenantId + ":" + paymentType;
            // Cache eviction would be handled by @CacheEvict annotation
            
            logger.info("Response configuration updated successfully for {}: {}", paymentType, responseConfig.getResponseMode());
            
        } catch (Exception e) {
            logger.error("Error updating response configuration for {}: {}", paymentType, e.getMessage(), e);
            throw new RuntimeException("Failed to update response configuration", e);
        }
    }
    
    /**
     * Check if Kafka response is enabled for a payment type
     */
    public boolean isKafkaResponseEnabled(String tenantId, String paymentType) {
        PaymentResponseConfig config = getResponseConfig(tenantId, paymentType);
        return config.getResponseMode() == ResponseMode.KAFKA_TOPIC && 
               config.getKafkaConfig().isEnabled();
    }
    
    /**
     * Get Kafka topic name for pain.002 responses
     */
    public String getKafkaResponseTopic(String tenantId, String paymentType) {
        PaymentResponseConfig config = getResponseConfig(tenantId, paymentType);
        
        if (config.getResponseMode() != ResponseMode.KAFKA_TOPIC) {
            return null;
        }
        
        KafkaResponseConfig kafkaConfig = config.getKafkaConfig();
        
        if (kafkaConfig.getExplicitTopicName() != null) {
            return kafkaConfig.getExplicitTopicName();
        }
        
        if (kafkaConfig.isUsePaymentTypeSpecificTopic()) {
            return kafkaConfig.getTopicPattern()
                .replace("{tenantId}", tenantId)
                .replace("{paymentType}", paymentType.toLowerCase());
        }
        
        return String.format("payment-engine.%s.responses.pain002", tenantId);
    }
    
    /**
     * Parse response configuration from payment type configuration
     */
    private PaymentResponseConfig parseResponseConfig(Map<String, Object> configuration) {
        if (configuration == null) {
            return getDefaultResponseConfig();
        }
        
        // Parse response mode
        String responseModeStr = (String) configuration.getOrDefault("response_mode", "SYNCHRONOUS");
        ResponseMode responseMode;
        try {
            responseMode = ResponseMode.valueOf(responseModeStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid response mode: {}, using SYNCHRONOUS", responseModeStr);
            responseMode = ResponseMode.SYNCHRONOUS;
        }
        
        // Parse Kafka configuration
        @SuppressWarnings("unchecked")
        Map<String, Object> kafkaConfigMap = (Map<String, Object>) configuration.get("kafka_response_config");
        KafkaResponseConfig kafkaConfig = parseKafkaConfig(kafkaConfigMap);
        
        return new PaymentResponseConfig(responseMode, kafkaConfig);
    }
    
    /**
     * Parse Kafka response configuration
     */
    private KafkaResponseConfig parseKafkaConfig(Map<String, Object> kafkaConfigMap) {
        if (kafkaConfigMap == null) {
            return new KafkaResponseConfig();
        }
        
        KafkaResponseConfig kafkaConfig = new KafkaResponseConfig();
        kafkaConfig.setEnabled(Boolean.TRUE.equals(kafkaConfigMap.get("enabled")));
        kafkaConfig.setUsePaymentTypeSpecificTopic(Boolean.TRUE.equals(kafkaConfigMap.get("use_payment_type_specific_topic")));
        kafkaConfig.setTopicPattern((String) kafkaConfigMap.getOrDefault("topic_pattern", "payment-engine.{tenantId}.responses.{paymentType}.pain002"));
        kafkaConfig.setExplicitTopicName((String) kafkaConfigMap.get("explicit_topic_name"));
        kafkaConfig.setIncludeOriginalMessage(Boolean.TRUE.equals(kafkaConfigMap.get("include_original_message")));
        kafkaConfig.setPriority((String) kafkaConfigMap.getOrDefault("priority", "NORMAL"));
        
        @SuppressWarnings("unchecked")
        List<String> targetSystems = (List<String>) kafkaConfigMap.get("target_systems");
        if (targetSystems != null) {
            kafkaConfig.setTargetSystems(targetSystems);
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> retryPolicy = (Map<String, Object>) kafkaConfigMap.get("retry_policy");
        if (retryPolicy != null) {
            kafkaConfig.setMaxRetries((Integer) retryPolicy.getOrDefault("max_retries", 3));
            kafkaConfig.setBackoffMs((Integer) retryPolicy.getOrDefault("backoff_ms", 1000));
        }
        
        return kafkaConfig;
    }
    
    /**
     * Get default response configuration
     */
    private PaymentResponseConfig getDefaultResponseConfig() {
        return new PaymentResponseConfig(ResponseMode.SYNCHRONOUS, new KafkaResponseConfig());
    }
    
    // Configuration Classes
    public static class PaymentResponseConfig {
        private ResponseMode responseMode;
        private KafkaResponseConfig kafkaConfig;
        
        public PaymentResponseConfig() {
            this.responseMode = ResponseMode.SYNCHRONOUS;
            this.kafkaConfig = new KafkaResponseConfig();
        }
        
        public PaymentResponseConfig(ResponseMode responseMode, KafkaResponseConfig kafkaConfig) {
            this.responseMode = responseMode;
            this.kafkaConfig = kafkaConfig;
        }
        
        // Getters and Setters
        public ResponseMode getResponseMode() { return responseMode; }
        public void setResponseMode(ResponseMode responseMode) { this.responseMode = responseMode; }
        
        public KafkaResponseConfig getKafkaConfig() { return kafkaConfig; }
        public void setKafkaConfig(KafkaResponseConfig kafkaConfig) { this.kafkaConfig = kafkaConfig; }
    }
    
    public static class KafkaResponseConfig {
        private boolean enabled = false;
        private boolean usePaymentTypeSpecificTopic = true;
        private String topicPattern = "payment-engine.{tenantId}.responses.{paymentType}.pain002";
        private String explicitTopicName;
        private boolean includeOriginalMessage = true;
        private String priority = "NORMAL";
        private List<String> targetSystems = java.util.List.of();
        private int maxRetries = 3;
        private int backoffMs = 1000;
        
        // Constructors
        public KafkaResponseConfig() {}
        
        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public boolean isUsePaymentTypeSpecificTopic() { return usePaymentTypeSpecificTopic; }
        public void setUsePaymentTypeSpecificTopic(boolean usePaymentTypeSpecificTopic) { this.usePaymentTypeSpecificTopic = usePaymentTypeSpecificTopic; }
        
        public String getTopicPattern() { return topicPattern; }
        public void setTopicPattern(String topicPattern) { this.topicPattern = topicPattern; }
        
        public String getExplicitTopicName() { return explicitTopicName; }
        public void setExplicitTopicName(String explicitTopicName) { this.explicitTopicName = explicitTopicName; }
        
        public boolean isIncludeOriginalMessage() { return includeOriginalMessage; }
        public void setIncludeOriginalMessage(boolean includeOriginalMessage) { this.includeOriginalMessage = includeOriginalMessage; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public List<String> getTargetSystems() { return targetSystems; }
        public void setTargetSystems(List<String> targetSystems) { this.targetSystems = targetSystems; }
        
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        
        public int getBackoffMs() { return backoffMs; }
        public void setBackoffMs(int backoffMs) { this.backoffMs = backoffMs; }
    }
    
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
}