package com.paymentengine.middleware.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for delivering webhook notifications for async responses
 */
public interface WebhookDeliveryService {
    
    /**
     * Deliver webhook notification for async response
     */
    CompletableFuture<WebhookDeliveryResult> deliverWebhook(
            String webhookUrl,
            Map<String, Object> payload,
            Map<String, String> headers,
            String tenantId,
            String messageType,
            String correlationId);
    
    /**
     * Deliver webhook notification with retry logic
     */
    CompletableFuture<WebhookDeliveryResult> deliverWebhookWithRetry(
            String webhookUrl,
            Map<String, Object> payload,
            Map<String, String> headers,
            String tenantId,
            String messageType,
            String correlationId,
            int maxRetries,
            long retryDelayMs);
    
    /**
     * Validate webhook URL
     */
    boolean validateWebhookUrl(String webhookUrl);
    
    /**
     * Get webhook delivery status
     */
    WebhookDeliveryStatus getDeliveryStatus(String correlationId);
    
    /**
     * Get webhook delivery history
     */
    Map<String, Object> getDeliveryHistory(String tenantId, String messageType, int limit);
    
    /**
     * Webhook delivery result
     */
    class WebhookDeliveryResult {
        private String correlationId;
        private String webhookUrl;
        private int statusCode;
        private String responseBody;
        private Map<String, String> responseHeaders;
        private long deliveryTimeMs;
        private boolean success;
        private String errorMessage;
        private int retryAttempt;
        private long timestamp;
        
        // Constructors
        public WebhookDeliveryResult() {}
        
        public WebhookDeliveryResult(String correlationId, String webhookUrl, int statusCode,
                                   String responseBody, Map<String, String> responseHeaders,
                                   long deliveryTimeMs, boolean success, String errorMessage,
                                   int retryAttempt, long timestamp) {
            this.correlationId = correlationId;
            this.webhookUrl = webhookUrl;
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.responseHeaders = responseHeaders;
            this.deliveryTimeMs = deliveryTimeMs;
            this.success = success;
            this.errorMessage = errorMessage;
            this.retryAttempt = retryAttempt;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
        
        public int getStatusCode() { return statusCode; }
        public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
        
        public String getResponseBody() { return responseBody; }
        public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
        
        public Map<String, String> getResponseHeaders() { return responseHeaders; }
        public void setResponseHeaders(Map<String, String> responseHeaders) { this.responseHeaders = responseHeaders; }
        
        public long getDeliveryTimeMs() { return deliveryTimeMs; }
        public void setDeliveryTimeMs(long deliveryTimeMs) { this.deliveryTimeMs = deliveryTimeMs; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public int getRetryAttempt() { return retryAttempt; }
        public void setRetryAttempt(int retryAttempt) { this.retryAttempt = retryAttempt; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * Webhook delivery status
     */
    enum WebhookDeliveryStatus {
        PENDING,
        DELIVERING,
        DELIVERED,
        FAILED,
        RETRYING,
        EXPIRED
    }
}