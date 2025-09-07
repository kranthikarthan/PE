package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.entity.QueuedMessage;
import com.paymentengine.paymentprocessing.entity.QueuedMessage.MessageType;
import com.paymentengine.paymentprocessing.entity.QueuedMessage.MessageStatus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Message Queue Service
 * 
 * Handles queuing and processing of messages when downstream systems are unavailable.
 * Provides offline capability and message replay functionality.
 */
public interface MessageQueueService {
    
    /**
     * Queue a message for later processing
     */
    CompletableFuture<QueuedMessage> queueMessage(
            String messageType,
            String tenantId,
            String serviceName,
            Object payload,
            Map<String, Object> metadata);
    
    /**
     * Process queued messages for a specific service
     */
    CompletableFuture<List<QueuedMessage>> processQueuedMessages(String serviceName, String tenantId);
    
    /**
     * Process a specific queued message
     */
    CompletableFuture<QueuedMessage> processQueuedMessage(String messageId);
    
    /**
     * Get queued messages by status
     */
    List<QueuedMessage> getQueuedMessagesByStatus(MessageStatus status, String tenantId);
    
    /**
     * Get queued messages by service
     */
    List<QueuedMessage> getQueuedMessagesByService(String serviceName, String tenantId);
    
    /**
     * Retry failed messages
     */
    CompletableFuture<List<QueuedMessage>> retryFailedMessages(String serviceName, String tenantId);
    
    /**
     * Mark message as processed
     */
    QueuedMessage markMessageAsProcessed(String messageId, String result);
    
    /**
     * Mark message as failed
     */
    QueuedMessage markMessageAsFailed(String messageId, String errorMessage);
    
    /**
     * Get message by ID
     */
    Optional<QueuedMessage> getMessageById(String messageId);
    
    /**
     * Delete processed messages older than specified days
     */
    int cleanupOldMessages(int daysOld);
    
    /**
     * Get queue statistics
     */
    QueueStatistics getQueueStatistics(String tenantId);
    
    /**
     * Queue Statistics
     */
    class QueueStatistics {
        private long totalMessages;
        private long pendingMessages;
        private long processingMessages;
        private long processedMessages;
        private long failedMessages;
        private long retryMessages;
        private double averageProcessingTimeMs;
        private String oldestPendingMessage;
        private String newestMessage;
        
        // Constructors, getters, setters
        public QueueStatistics() {}
        
        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
        
        public long getPendingMessages() { return pendingMessages; }
        public void setPendingMessages(long pendingMessages) { this.pendingMessages = pendingMessages; }
        
        public long getProcessingMessages() { return processingMessages; }
        public void setProcessingMessages(long processingMessages) { this.processingMessages = processingMessages; }
        
        public long getProcessedMessages() { return processedMessages; }
        public void setProcessedMessages(long processedMessages) { this.processedMessages = processedMessages; }
        
        public long getFailedMessages() { return failedMessages; }
        public void setFailedMessages(long failedMessages) { this.failedMessages = failedMessages; }
        
        public long getRetryMessages() { return retryMessages; }
        public void setRetryMessages(long retryMessages) { this.retryMessages = retryMessages; }
        
        public double getAverageProcessingTimeMs() { return averageProcessingTimeMs; }
        public void setAverageProcessingTimeMs(double averageProcessingTimeMs) { this.averageProcessingTimeMs = averageProcessingTimeMs; }
        
        public String getOldestPendingMessage() { return oldestPendingMessage; }
        public void setOldestPendingMessage(String oldestPendingMessage) { this.oldestPendingMessage = oldestPendingMessage; }
        
        public String getNewestMessage() { return newestMessage; }
        public void setNewestMessage(String newestMessage) { this.newestMessage = newestMessage; }
    }
}