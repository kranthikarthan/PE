package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Queued Message Entity
 * 
 * Represents a message that has been queued for later processing when
 * downstream systems are unavailable.
 */
@Entity
@Table(name = "queued_messages", schema = "payment_engine")
public class QueuedMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "message_id", nullable = false, length = 100, unique = true)
    @NotBlank
    private String messageId;
    
    @Column(name = "message_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private MessageType messageType;
    
    @Column(name = "tenant_id", nullable = false, length = 50)
    @NotBlank
    private String tenantId;
    
    @Column(name = "service_name", nullable = false, length = 100)
    @NotBlank
    private String serviceName;
    
    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;
    
    @Column(name = "http_method", length = 10)
    private String httpMethod = "POST";
    
    @Column(name = "payload", columnDefinition = "jsonb")
    private Map<String, Object> payload;
    
    @Column(name = "headers", columnDefinition = "jsonb")
    private Map<String, String> headers;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private MessageStatus status = MessageStatus.PENDING;
    
    @Column(name = "priority")
    private Integer priority = 1;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;
    
    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Column(name = "result", columnDefinition = "jsonb")
    private Map<String, Object> result;
    
    @Column(name = "error_message", length = 2000)
    private String errorMessage;
    
    @Column(name = "error_details", columnDefinition = "jsonb")
    private Map<String, Object> errorDetails;
    
    @Column(name = "correlation_id", length = 100)
    private String correlationId;
    
    @Column(name = "parent_message_id", length = 100)
    private String parentMessageId;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (messageId == null) {
            messageId = "MSG-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public QueuedMessage() {}
    
    public QueuedMessage(MessageType messageType, String tenantId, String serviceName, Map<String, Object> payload) {
        this.messageType = messageType;
        this.tenantId = tenantId;
        this.serviceName = serviceName;
        this.payload = payload;
    }
    
    // Business methods
    public boolean canRetry() {
        return retryCount < maxRetries && 
               (nextRetryAt == null || LocalDateTime.now().isAfter(nextRetryAt)) &&
               status == MessageStatus.FAILED;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
        this.nextRetryAt = LocalDateTime.now().plusMinutes(calculateRetryDelay());
    }
    
    private int calculateRetryDelay() {
        // Exponential backoff: 1, 2, 4, 8, 16 minutes
        return (int) Math.pow(2, retryCount - 1);
    }
    
    public void markAsProcessing() {
        this.status = MessageStatus.PROCESSING;
        this.processingStartedAt = LocalDateTime.now();
    }
    
    public void markAsProcessed(Map<String, Object> result) {
        this.status = MessageStatus.PROCESSED;
        this.processingCompletedAt = LocalDateTime.now();
        this.result = result;
        if (processingStartedAt != null) {
            this.processingTimeMs = java.time.Duration.between(processingStartedAt, processingCompletedAt).toMillis();
        }
    }
    
    public void markAsFailed(String errorMessage, Map<String, Object> errorDetails) {
        this.status = MessageStatus.FAILED;
        this.processingCompletedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
        if (processingStartedAt != null) {
            this.processingTimeMs = java.time.Duration.between(processingStartedAt, processingCompletedAt).toMillis();
        }
        incrementRetryCount();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    
    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
    
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    
    public LocalDateTime getProcessingStartedAt() { return processingStartedAt; }
    public void setProcessingStartedAt(LocalDateTime processingStartedAt) { this.processingStartedAt = processingStartedAt; }
    
    public LocalDateTime getProcessingCompletedAt() { return processingCompletedAt; }
    public void setProcessingCompletedAt(LocalDateTime processingCompletedAt) { this.processingCompletedAt = processingCompletedAt; }
    
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    
    public Map<String, Object> getResult() { return result; }
    public void setResult(Map<String, Object> result) { this.result = result; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Map<String, Object> getErrorDetails() { return errorDetails; }
    public void setErrorDetails(Map<String, Object> errorDetails) { this.errorDetails = errorDetails; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public String getParentMessageId() { return parentMessageId; }
    public void setParentMessageId(String parentMessageId) { this.parentMessageId = parentMessageId; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    // Enums
    public enum MessageType {
        FRAUD_API_REQUEST,
        FRAUD_API_RESPONSE,
        CORE_BANKING_DEBIT_REQUEST,
        CORE_BANKING_DEBIT_RESPONSE,
        CORE_BANKING_CREDIT_REQUEST,
        CORE_BANKING_CREDIT_RESPONSE,
        SCHEME_REQUEST,
        SCHEME_RESPONSE,
        KAFKA_MESSAGE,
        WEBHOOK_CALLBACK,
        NOTIFICATION,
        AUDIT_LOG,
        METRICS_UPDATE
    }
    
    public enum MessageStatus {
        PENDING,
        PROCESSING,
        PROCESSED,
        FAILED,
        RETRY,
        EXPIRED,
        CANCELLED
    }
}