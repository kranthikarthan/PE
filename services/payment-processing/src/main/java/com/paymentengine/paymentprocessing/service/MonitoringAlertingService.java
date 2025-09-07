package com.paymentengine.paymentprocessing.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for comprehensive monitoring and alerting
 */
public interface MonitoringAlertingService {
    
    /**
     * Record metrics for message processing
     */
    void recordMessageProcessingMetrics(
            String messageType,
            String tenantId,
            String clearingSystemCode,
            long processingTimeMs,
            boolean success,
            String correlationId);
    
    /**
     * Record metrics for clearing system interaction
     */
    void recordClearingSystemMetrics(
            String clearingSystemCode,
            String endpointType,
            long responseTimeMs,
            int statusCode,
            boolean success,
            String correlationId);
    
    /**
     * Record metrics for message transformation
     */
    void recordTransformationMetrics(
            String fromMessageType,
            String toMessageType,
            long transformationTimeMs,
            boolean success,
            String correlationId);
    
    /**
     * Record metrics for webhook delivery
     */
    void recordWebhookMetrics(
            String webhookUrl,
            long deliveryTimeMs,
            int statusCode,
            boolean success,
            String correlationId);
    
    /**
     * Record metrics for Kafka message production
     */
    void recordKafkaMetrics(
            String topic,
            long productionTimeMs,
            boolean success,
            String correlationId);
    
    /**
     * Check alert conditions and send alerts
     */
    CompletableFuture<List<Alert>> checkAlertConditions();
    
    /**
     * Send alert notification
     */
    CompletableFuture<Boolean> sendAlert(Alert alert);
    
    /**
     * Get system health status
     */
    SystemHealthStatus getSystemHealthStatus();
    
    /**
     * Get metrics summary
     */
    MetricsSummary getMetricsSummary(String tenantId, String timeRange);
    
    /**
     * Get performance metrics
     */
    PerformanceMetrics getPerformanceMetrics(String tenantId, String messageType, String timeRange);
    
    /**
     * Get error metrics
     */
    ErrorMetrics getErrorMetrics(String tenantId, String timeRange);
    
    /**
     * Get throughput metrics
     */
    ThroughputMetrics getThroughputMetrics(String tenantId, String timeRange);
    
    /**
     * Alert
     */
    class Alert {
        private String id;
        private String type;
        private String severity;
        private String title;
        private String description;
        private String tenantId;
        private String messageType;
        private String clearingSystemCode;
        private Map<String, Object> metadata;
        private long timestamp;
        private boolean acknowledged;
        private String acknowledgedBy;
        private long acknowledgedAt;
        
        // Constructors
        public Alert() {}
        
        public Alert(String id, String type, String severity, String title, String description,
                    String tenantId, String messageType, String clearingSystemCode,
                    Map<String, Object> metadata, long timestamp) {
            this.id = id;
            this.type = type;
            this.severity = severity;
            this.title = title;
            this.description = description;
            this.tenantId = tenantId;
            this.messageType = messageType;
            this.clearingSystemCode = clearingSystemCode;
            this.metadata = metadata;
            this.timestamp = timestamp;
            this.acknowledged = false;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        
        public String getClearingSystemCode() { return clearingSystemCode; }
        public void setClearingSystemCode(String clearingSystemCode) { this.clearingSystemCode = clearingSystemCode; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public boolean isAcknowledged() { return acknowledged; }
        public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
        
        public String getAcknowledgedBy() { return acknowledgedBy; }
        public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
        
        public long getAcknowledgedAt() { return acknowledgedAt; }
        public void setAcknowledgedAt(long acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    }
    
    /**
     * System health status
     */
    class SystemHealthStatus {
        private String status;
        private Map<String, String> components;
        private Map<String, Object> metrics;
        private long timestamp;
        
        // Constructors
        public SystemHealthStatus() {}
        
        public SystemHealthStatus(String status, Map<String, String> components, 
                                Map<String, Object> metrics, long timestamp) {
            this.status = status;
            this.components = components;
            this.metrics = metrics;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Map<String, String> getComponents() { return components; }
        public void setComponents(Map<String, String> components) { this.components = components; }
        
        public Map<String, Object> getMetrics() { return metrics; }
        public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * Metrics summary
     */
    class MetricsSummary {
        private String tenantId;
        private String timeRange;
        private long totalMessages;
        private long successfulMessages;
        private long failedMessages;
        private double successRate;
        private double averageProcessingTime;
        private Map<String, Long> messageTypeCounts;
        private Map<String, Long> clearingSystemCounts;
        private long timestamp;
        
        // Constructors
        public MetricsSummary() {}
        
        public MetricsSummary(String tenantId, String timeRange, long totalMessages,
                            long successfulMessages, long failedMessages, double successRate,
                            double averageProcessingTime, Map<String, Long> messageTypeCounts,
                            Map<String, Long> clearingSystemCounts, long timestamp) {
            this.tenantId = tenantId;
            this.timeRange = timeRange;
            this.totalMessages = totalMessages;
            this.successfulMessages = successfulMessages;
            this.failedMessages = failedMessages;
            this.successRate = successRate;
            this.averageProcessingTime = averageProcessingTime;
            this.messageTypeCounts = messageTypeCounts;
            this.clearingSystemCounts = clearingSystemCounts;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getTimeRange() { return timeRange; }
        public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
        
        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
        
        public long getSuccessfulMessages() { return successfulMessages; }
        public void setSuccessfulMessages(long successfulMessages) { this.successfulMessages = successfulMessages; }
        
        public long getFailedMessages() { return failedMessages; }
        public void setFailedMessages(long failedMessages) { this.failedMessages = failedMessages; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public double getAverageProcessingTime() { return averageProcessingTime; }
        public void setAverageProcessingTime(double averageProcessingTime) { this.averageProcessingTime = averageProcessingTime; }
        
        public Map<String, Long> getMessageTypeCounts() { return messageTypeCounts; }
        public void setMessageTypeCounts(Map<String, Long> messageTypeCounts) { this.messageTypeCounts = messageTypeCounts; }
        
        public Map<String, Long> getClearingSystemCounts() { return clearingSystemCounts; }
        public void setClearingSystemCounts(Map<String, Long> clearingSystemCounts) { this.clearingSystemCounts = clearingSystemCounts; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * Performance metrics
     */
    class PerformanceMetrics {
        private String tenantId;
        private String messageType;
        private String timeRange;
        private double averageProcessingTime;
        private double p50ProcessingTime;
        private double p95ProcessingTime;
        private double p99ProcessingTime;
        private double maxProcessingTime;
        private double minProcessingTime;
        private long timestamp;
        
        // Constructors
        public PerformanceMetrics() {}
        
        public PerformanceMetrics(String tenantId, String messageType, String timeRange,
                                double averageProcessingTime, double p50ProcessingTime,
                                double p95ProcessingTime, double p99ProcessingTime,
                                double maxProcessingTime, double minProcessingTime, long timestamp) {
            this.tenantId = tenantId;
            this.messageType = messageType;
            this.timeRange = timeRange;
            this.averageProcessingTime = averageProcessingTime;
            this.p50ProcessingTime = p50ProcessingTime;
            this.p95ProcessingTime = p95ProcessingTime;
            this.p99ProcessingTime = p99ProcessingTime;
            this.maxProcessingTime = maxProcessingTime;
            this.minProcessingTime = minProcessingTime;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        
        public String getTimeRange() { return timeRange; }
        public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
        
        public double getAverageProcessingTime() { return averageProcessingTime; }
        public void setAverageProcessingTime(double averageProcessingTime) { this.averageProcessingTime = averageProcessingTime; }
        
        public double getP50ProcessingTime() { return p50ProcessingTime; }
        public void setP50ProcessingTime(double p50ProcessingTime) { this.p50ProcessingTime = p50ProcessingTime; }
        
        public double getP95ProcessingTime() { return p95ProcessingTime; }
        public void setP95ProcessingTime(double p95ProcessingTime) { this.p95ProcessingTime = p95ProcessingTime; }
        
        public double getP99ProcessingTime() { return p99ProcessingTime; }
        public void setP99ProcessingTime(double p99ProcessingTime) { this.p99ProcessingTime = p99ProcessingTime; }
        
        public double getMaxProcessingTime() { return maxProcessingTime; }
        public void setMaxProcessingTime(double maxProcessingTime) { this.maxProcessingTime = maxProcessingTime; }
        
        public double getMinProcessingTime() { return minProcessingTime; }
        public void setMinProcessingTime(double minProcessingTime) { this.minProcessingTime = minProcessingTime; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * Error metrics
     */
    class ErrorMetrics {
        private String tenantId;
        private String timeRange;
        private long totalErrors;
        private Map<String, Long> errorTypeCounts;
        private Map<String, Long> errorMessageCounts;
        private Map<String, Long> clearingSystemErrorCounts;
        private double errorRate;
        private long timestamp;
        
        // Constructors
        public ErrorMetrics() {}
        
        public ErrorMetrics(String tenantId, String timeRange, long totalErrors,
                          Map<String, Long> errorTypeCounts, Map<String, Long> errorMessageCounts,
                          Map<String, Long> clearingSystemErrorCounts, double errorRate, long timestamp) {
            this.tenantId = tenantId;
            this.timeRange = timeRange;
            this.totalErrors = totalErrors;
            this.errorTypeCounts = errorTypeCounts;
            this.errorMessageCounts = errorMessageCounts;
            this.clearingSystemErrorCounts = clearingSystemErrorCounts;
            this.errorRate = errorRate;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getTimeRange() { return timeRange; }
        public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
        
        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }
        
        public Map<String, Long> getErrorTypeCounts() { return errorTypeCounts; }
        public void setErrorTypeCounts(Map<String, Long> errorTypeCounts) { this.errorTypeCounts = errorTypeCounts; }
        
        public Map<String, Long> getErrorMessageCounts() { return errorMessageCounts; }
        public void setErrorMessageCounts(Map<String, Long> errorMessageCounts) { this.errorMessageCounts = errorMessageCounts; }
        
        public Map<String, Long> getClearingSystemErrorCounts() { return clearingSystemErrorCounts; }
        public void setClearingSystemErrorCounts(Map<String, Long> clearingSystemErrorCounts) { this.clearingSystemErrorCounts = clearingSystemErrorCounts; }
        
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * Throughput metrics
     */
    class ThroughputMetrics {
        private String tenantId;
        private String timeRange;
        private double messagesPerSecond;
        private double messagesPerMinute;
        private double messagesPerHour;
        private Map<String, Double> messageTypeThroughput;
        private Map<String, Double> clearingSystemThroughput;
        private long timestamp;
        
        // Constructors
        public ThroughputMetrics() {}
        
        public ThroughputMetrics(String tenantId, String timeRange, double messagesPerSecond,
                               double messagesPerMinute, double messagesPerHour,
                               Map<String, Double> messageTypeThroughput,
                               Map<String, Double> clearingSystemThroughput, long timestamp) {
            this.tenantId = tenantId;
            this.timeRange = timeRange;
            this.messagesPerSecond = messagesPerSecond;
            this.messagesPerMinute = messagesPerMinute;
            this.messagesPerHour = messagesPerHour;
            this.messageTypeThroughput = messageTypeThroughput;
            this.clearingSystemThroughput = clearingSystemThroughput;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getTimeRange() { return timeRange; }
        public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
        
        public double getMessagesPerSecond() { return messagesPerSecond; }
        public void setMessagesPerSecond(double messagesPerSecond) { this.messagesPerSecond = messagesPerSecond; }
        
        public double getMessagesPerMinute() { return messagesPerMinute; }
        public void setMessagesPerMinute(double messagesPerMinute) { this.messagesPerMinute = messagesPerMinute; }
        
        public double getMessagesPerHour() { return messagesPerHour; }
        public void setMessagesPerHour(double messagesPerHour) { this.messagesPerHour = messagesPerHour; }
        
        public Map<String, Double> getMessageTypeThroughput() { return messageTypeThroughput; }
        public void setMessageTypeThroughput(Map<String, Double> messageTypeThroughput) { this.messageTypeThroughput = messageTypeThroughput; }
        
        public Map<String, Double> getClearingSystemThroughput() { return clearingSystemThroughput; }
        public void setClearingSystemThroughput(Map<String, Double> clearingSystemThroughput) { this.clearingSystemThroughput = clearingSystemThroughput; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}