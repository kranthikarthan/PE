package com.paymentengine.shared.service;

import com.paymentengine.shared.dto.iso20022.Pain002Message;
import com.paymentengine.shared.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing pain.002 responses to Kafka topics
 * Supports configurable response routing per payment type and tenant
 */
@Service
public class KafkaResponseService {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaResponseService.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public KafkaResponseService(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publish pain.002 response to configured Kafka topic
     */
    public CompletableFuture<SendResult<String, Object>> publishPain002Response(
            Pain002Message pain002Message,
            String paymentType,
            String originalMessageId,
            Map<String, Object> responseConfig) {
        
        String tenantId = TenantContext.getCurrentTenant();
        
        logger.info("Publishing pain.002 response for tenant: {}, paymentType: {}, originalMessageId: {}", 
                   tenantId, paymentType, originalMessageId);
        
        try {
            // Determine target Kafka topic based on configuration
            String topicName = determineResponseTopic(tenantId, paymentType, responseConfig);
            
            // Enrich message with metadata
            Map<String, Object> enrichedMessage = enrichResponseMessage(pain002Message, tenantId, paymentType, originalMessageId, responseConfig);
            
            // Publish to Kafka
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, originalMessageId, enrichedMessage);
            
            // Add success/failure callbacks
            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    logger.error("Failed to publish pain.002 response to topic {}: {}", topicName, exception.getMessage(), exception);
                } else {
                    logger.info("Successfully published pain.002 response to topic {}, partition: {}, offset: {}", 
                               topicName, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });
            
            return future;
            
        } catch (Exception e) {
            logger.error("Error publishing pain.002 response: {}", e.getMessage(), e);
            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
    
    /**
     * Determine the appropriate Kafka topic for the response
     */
    private String determineResponseTopic(String tenantId, String paymentType, Map<String, Object> responseConfig) {
        // Priority order for topic determination:
        // 1. Explicit topic from response configuration
        // 2. Payment type specific topic
        // 3. Tenant-specific default topic
        // 4. Global default topic
        
        // Check for explicit topic configuration
        if (responseConfig.containsKey("kafkaTopicName")) {
            String explicitTopic = (String) responseConfig.get("kafkaTopicName");
            logger.debug("Using explicit Kafka topic: {}", explicitTopic);
            return explicitTopic;
        }
        
        // Check for payment type specific topic pattern
        if (responseConfig.containsKey("usePaymentTypeSpecificTopic") && 
            Boolean.TRUE.equals(responseConfig.get("usePaymentTypeSpecificTopic"))) {
            
            String paymentTypeTopicPattern = (String) responseConfig.getOrDefault("paymentTypeTopicPattern", 
                                                                                 "payment-engine.{tenantId}.responses.{paymentType}.pain002");
            String topicName = paymentTypeTopicPattern
                .replace("{tenantId}", tenantId)
                .replace("{paymentType}", paymentType.toLowerCase());
            
            logger.debug("Using payment type specific topic: {}", topicName);
            return topicName;
        }
        
        // Use tenant-specific default topic
        String defaultTopicPattern = (String) responseConfig.getOrDefault("defaultTopicPattern", 
                                                                          "payment-engine.{tenantId}.responses.pain002");
        String topicName = defaultTopicPattern.replace("{tenantId}", tenantId);
        
        logger.debug("Using tenant default topic: {}", topicName);
        return topicName;
    }
    
    /**
     * Enrich the pain.002 message with additional metadata for Kafka
     */
    private Map<String, Object> enrichResponseMessage(
            Pain002Message pain002Message, 
            String tenantId, 
            String paymentType, 
            String originalMessageId,
            Map<String, Object> responseConfig) {
        
        Map<String, Object> enrichedMessage = new HashMap<>();
        
        // Core message content
        enrichedMessage.put("messageType", "pain.002.001.03");
        enrichedMessage.put("pain002Message", pain002Message);
        
        // Metadata
        enrichedMessage.put("tenantId", tenantId);
        enrichedMessage.put("paymentType", paymentType);
        enrichedMessage.put("originalMessageId", originalMessageId);
        enrichedMessage.put("responseMessageId", pain002Message.getGroupHeader().getMessageId());
        enrichedMessage.put("publishedAt", LocalDateTime.now());
        
        // Response configuration metadata
        enrichedMessage.put("responseMode", "KAFKA_TOPIC");
        enrichedMessage.put("responseConfig", responseConfig);
        
        // Processing metadata
        enrichedMessage.put("processingInfo", Map.of(
            "processedBy", "payment-engine-core-banking",
            "processingVersion", "1.0.0",
            "processingTimestamp", LocalDateTime.now()
        ));
        
        // Routing metadata for downstream consumers
        enrichedMessage.put("routingInfo", Map.of(
            "targetSystems", responseConfig.getOrDefault("targetSystems", java.util.List.of()),
            "priority", responseConfig.getOrDefault("priority", "NORMAL"),
            "retryPolicy", responseConfig.getOrDefault("retryPolicy", Map.of("maxRetries", 3, "backoffMs", 1000))
        ));
        
        logger.debug("Enriched pain.002 message with metadata for tenant: {}, paymentType: {}", tenantId, paymentType);
        
        return enrichedMessage;
    }
    
    /**
     * Create pain.002 response topic name for a specific payment type
     */
    public String createPaymentTypeResponseTopic(String tenantId, String paymentType) {
        String topicName = String.format("payment-engine.%s.responses.%s.pain002", 
                                        tenantId, paymentType.toLowerCase());
        
        logger.info("Generated payment type response topic: {}", topicName);
        return topicName;
    }
    
    /**
     * Create default pain.002 response topic for a tenant
     */
    public String createDefaultResponseTopic(String tenantId) {
        String topicName = String.format("payment-engine.%s.responses.pain002", tenantId);
        
        logger.info("Generated default response topic: {}", topicName);
        return topicName;
    }
    
    /**
     * Validate Kafka response configuration
     */
    public boolean validateKafkaResponseConfig(Map<String, Object> responseConfig) {
        if (responseConfig == null || responseConfig.isEmpty()) {
            return false;
        }
        
        // Check required configuration elements
        if (responseConfig.containsKey("kafkaTopicName")) {
            String topicName = (String) responseConfig.get("kafkaTopicName");
            return topicName != null && !topicName.trim().isEmpty();
        }
        
        if (responseConfig.containsKey("usePaymentTypeSpecificTopic")) {
            return Boolean.TRUE.equals(responseConfig.get("usePaymentTypeSpecificTopic"));
        }
        
        // Default configuration is valid
        return true;
    }
    
    /**
     * Get Kafka response configuration for a payment type
     */
    public Map<String, Object> getKafkaResponseConfig(String tenantId, String paymentType) {
        // This would typically load from ConfigurationService
        // For now, return default configuration
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("usePaymentTypeSpecificTopic", true);
        defaultConfig.put("paymentTypeTopicPattern", "payment-engine.{tenantId}.responses.{paymentType}.pain002");
        defaultConfig.put("priority", "HIGH");
        defaultConfig.put("retryPolicy", Map.of("maxRetries", 3, "backoffMs", 1000));
        defaultConfig.put("targetSystems", java.util.List.of("core-banking", "notification-service"));
        
        logger.debug("Retrieved Kafka response config for tenant: {}, paymentType: {}", tenantId, paymentType);
        
        return defaultConfig;
    }
    
    /**
     * Publish generic status update to Kafka
     */
    public CompletableFuture<SendResult<String, Object>> publishStatusUpdate(
            String tenantId,
            String paymentType,
            String transactionId,
            String status,
            String statusReason,
            Map<String, Object> additionalData) {
        
        logger.info("Publishing status update for tenant: {}, paymentType: {}, transaction: {}, status: {}", 
                   tenantId, paymentType, transactionId, status);
        
        try {
            String topicName = String.format("payment-engine.%s.status-updates.%s", tenantId, paymentType.toLowerCase());
            
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("messageType", "PAYMENT_STATUS_UPDATE");
            statusUpdate.put("tenantId", tenantId);
            statusUpdate.put("paymentType", paymentType);
            statusUpdate.put("transactionId", transactionId);
            statusUpdate.put("status", status);
            statusUpdate.put("statusReason", statusReason);
            statusUpdate.put("timestamp", LocalDateTime.now());
            statusUpdate.put("additionalData", additionalData);
            
            return kafkaTemplate.send(topicName, transactionId, statusUpdate);
            
        } catch (Exception e) {
            logger.error("Error publishing status update: {}", e.getMessage(), e);
            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}