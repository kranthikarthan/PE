package com.paymentengine.paymentprocessing.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for producing messages to Kafka for async responses
 */
public interface KafkaMessageProducer {
    
    /**
     * Send message to Kafka topic
     */
    CompletableFuture<KafkaProducerResult> sendMessage(
            String topic,
            String key,
            Map<String, Object> message,
            String tenantId,
            String messageType,
            String correlationId);
    
    /**
     * Send message to Kafka topic with custom headers
     */
    CompletableFuture<KafkaProducerResult> sendMessageWithHeaders(
            String topic,
            String key,
            Map<String, Object> message,
            Map<String, String> headers,
            String tenantId,
            String messageType,
            String correlationId);
    
    /**
     * Send message to tenant-specific topic
     */
    CompletableFuture<KafkaProducerResult> sendMessageToTenantTopic(
            String tenantId,
            String messageType,
            Map<String, Object> message,
            String correlationId);
    
    /**
     * Send message to message-type-specific topic
     */
    CompletableFuture<KafkaProducerResult> sendMessageToMessageTypeTopic(
            String messageType,
            Map<String, Object> message,
            String tenantId,
            String correlationId);
    
    /**
     * Get Kafka producer status
     */
    KafkaProducerStatus getProducerStatus();
    
    /**
     * Get topic information
     */
    Map<String, Object> getTopicInfo(String topic);
    
    /**
     * Kafka producer result
     */
    class KafkaProducerResult {
        private String topic;
        private String key;
        private long offset;
        private int partition;
        private long timestamp;
        private boolean success;
        private String errorMessage;
        private String correlationId;
        private String tenantId;
        private String messageType;
        
        // Constructors
        public KafkaProducerResult() {}
        
        public KafkaProducerResult(String topic, String key, long offset, int partition,
                                 long timestamp, boolean success, String errorMessage,
                                 String correlationId, String tenantId, String messageType) {
            this.topic = topic;
            this.key = key;
            this.offset = offset;
            this.partition = partition;
            this.timestamp = timestamp;
            this.success = success;
            this.errorMessage = errorMessage;
            this.correlationId = correlationId;
            this.tenantId = tenantId;
            this.messageType = messageType;
        }
        
        // Getters and Setters
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        
        public long getOffset() { return offset; }
        public void setOffset(long offset) { this.offset = offset; }
        
        public int getPartition() { return partition; }
        public void setPartition(int partition) { this.partition = partition; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
    }
    
    /**
     * Kafka producer status
     */
    enum KafkaProducerStatus {
        CONNECTED,
        DISCONNECTED,
        ERROR,
        UNKNOWN
    }
}