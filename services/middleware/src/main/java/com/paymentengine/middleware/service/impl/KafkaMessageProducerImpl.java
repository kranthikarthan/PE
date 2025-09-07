package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.service.KafkaMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of Kafka message producer for async responses
 */
@Service
public class KafkaMessageProducerImpl implements KafkaMessageProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageProducerImpl.class);
    
    @Value("${kafka.bootstrap.servers:localhost:9092}")
    private String bootstrapServers;
    
    @Value("${kafka.default.topic:iso20022-messages}")
    private String defaultTopic;
    
    @Value("${kafka.tenant.topic.prefix:tenant-}")
    private String tenantTopicPrefix;
    
    @Value("${kafka.message.type.topic.prefix:message-type-}")
    private String messageTypeTopicPrefix;
    
    private final Map<String, KafkaProducerStatus> producerStatuses;
    private final Map<String, Object> topicInfo;
    
    public KafkaMessageProducerImpl() {
        this.producerStatuses = new ConcurrentHashMap<>();
        this.topicInfo = new ConcurrentHashMap<>();
        
        // Initialize producer status
        producerStatuses.put("default", KafkaProducerStatus.CONNECTED);
        
        // Initialize topic info
        topicInfo.put(defaultTopic, Map.of(
                "name", defaultTopic,
                "partitions", 3,
                "replicationFactor", 1,
                "created", Instant.now().toString()
        ));
    }
    
    @Override
    public CompletableFuture<KafkaProducerResult> sendMessage(
            String topic,
            String key,
            Map<String, Object> message,
            String tenantId,
            String messageType,
            String correlationId) {
        
        return sendMessageWithHeaders(topic, key, message, Map.of(), tenantId, messageType, correlationId);
    }
    
    @Override
    public CompletableFuture<KafkaProducerResult> sendMessageWithHeaders(
            String topic,
            String key,
            Map<String, Object> message,
            Map<String, String> headers,
            String tenantId,
            String messageType,
            String correlationId) {
        
        logger.info("Sending message to Kafka topic: {}, key: {}, correlationId: {}, messageType: {}", 
                topic, key, correlationId, messageType);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate Kafka producer call
                // In a real implementation, this would use KafkaTemplate or KafkaProducer
                
                // Add metadata to message
                Map<String, Object> enrichedMessage = Map.of(
                        "payload", message,
                        "metadata", Map.of(
                                "correlationId", correlationId,
                                "tenantId", tenantId,
                                "messageType", messageType,
                                "timestamp", Instant.now().toString(),
                                "headers", headers
                        )
                );
                
                // Simulate partition assignment and offset
                int partition = Math.abs(key.hashCode()) % 3;
                long offset = System.currentTimeMillis();
                
                // Simulate success
                KafkaProducerResult result = new KafkaProducerResult(
                        topic, key, offset, partition, Instant.now().toEpochMilli(),
                        true, null, correlationId, tenantId, messageType
                );
                
                logger.info("Message sent successfully to Kafka topic: {}, partition: {}, offset: {}, correlationId: {}", 
                        topic, partition, offset, correlationId);
                
                return result;
                
            } catch (Exception e) {
                logger.error("Failed to send message to Kafka topic: {}, correlationId: {}, error: {}", 
                        topic, correlationId, e.getMessage());
                
                return new KafkaProducerResult(
                        topic, key, -1, -1, Instant.now().toEpochMilli(),
                        false, e.getMessage(), correlationId, tenantId, messageType
                );
            }
        });
    }
    
    @Override
    public CompletableFuture<KafkaProducerResult> sendMessageToTenantTopic(
            String tenantId,
            String messageType,
            Map<String, Object> message,
            String correlationId) {
        
        String topic = tenantTopicPrefix + tenantId;
        String key = correlationId;
        
        logger.info("Sending message to tenant-specific topic: {}, tenantId: {}, correlationId: {}", 
                topic, tenantId, correlationId);
        
        return sendMessage(topic, key, message, tenantId, messageType, correlationId);
    }
    
    @Override
    public CompletableFuture<KafkaProducerResult> sendMessageToMessageTypeTopic(
            String messageType,
            Map<String, Object> message,
            String tenantId,
            String correlationId) {
        
        String topic = messageTypeTopicPrefix + messageType;
        String key = correlationId;
        
        logger.info("Sending message to message-type-specific topic: {}, messageType: {}, correlationId: {}", 
                topic, messageType, correlationId);
        
        return sendMessage(topic, key, message, tenantId, messageType, correlationId);
    }
    
    @Override
    public KafkaProducerStatus getProducerStatus() {
        return producerStatuses.getOrDefault("default", KafkaProducerStatus.UNKNOWN);
    }
    
    @Override
    public Map<String, Object> getTopicInfo(String topic) {
        return topicInfo.getOrDefault(topic, Map.of(
                "name", topic,
                "partitions", 1,
                "replicationFactor", 1,
                "created", Instant.now().toString(),
                "status", "UNKNOWN"
        ));
    }
    
    /**
     * Create tenant-specific topic
     */
    public void createTenantTopic(String tenantId) {
        String topic = tenantTopicPrefix + tenantId;
        
        if (!topicInfo.containsKey(topic)) {
            topicInfo.put(topic, Map.of(
                    "name", topic,
                    "partitions", 3,
                    "replicationFactor", 1,
                    "created", Instant.now().toString(),
                    "type", "tenant-specific"
            ));
            
            logger.info("Created tenant-specific topic: {}", topic);
        }
    }
    
    /**
     * Create message-type-specific topic
     */
    public void createMessageTypeTopic(String messageType) {
        String topic = messageTypeTopicPrefix + messageType;
        
        if (!topicInfo.containsKey(topic)) {
            topicInfo.put(topic, Map.of(
                    "name", topic,
                    "partitions", 3,
                    "replicationFactor", 1,
                    "created", Instant.now().toString(),
                    "type", "message-type-specific"
            ));
            
            logger.info("Created message-type-specific topic: {}", topic);
        }
    }
    
    /**
     * Get all topics
     */
    public Map<String, Object> getAllTopics() {
        return Map.of(
                "topics", topicInfo,
                "totalCount", topicInfo.size(),
                "timestamp", Instant.now().toString()
        );
    }
    
    /**
     * Get producer metrics
     */
    public Map<String, Object> getProducerMetrics() {
        return Map.of(
                "status", getProducerStatus(),
                "bootstrapServers", bootstrapServers,
                "defaultTopic", defaultTopic,
                "tenantTopicPrefix", tenantTopicPrefix,
                "messageTypeTopicPrefix", messageTypeTopicPrefix,
                "totalTopics", topicInfo.size(),
                "timestamp", Instant.now().toString()
        );
    }
}