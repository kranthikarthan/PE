package com.paymentengine.middleware.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Response DTO for scheme messages
 */
public class SchemeMessageResponse {
    
    private String messageId;
    private String correlationId;
    private MessageStatus status;
    private String responseCode;
    private String responseMessage;
    private Object payload;
    private ErrorDetails errorDetails;
    private Long processingTimeMs;
    private Instant timestamp;
    
    // Constructors
    public SchemeMessageResponse() {}
    
    public SchemeMessageResponse(String messageId, String correlationId, MessageStatus status,
                                String responseCode, String responseMessage, Object payload,
                                ErrorDetails errorDetails, Long processingTimeMs, Instant timestamp) {
        this.messageId = messageId;
        this.correlationId = correlationId;
        this.status = status;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.payload = payload;
        this.errorDetails = errorDetails;
        this.processingTimeMs = processingTimeMs;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    public MessageStatus getStatus() {
        return status;
    }
    
    public void setStatus(MessageStatus status) {
        this.status = status;
    }
    
    public String getResponseCode() {
        return responseCode;
    }
    
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    
    public String getResponseMessage() {
        return responseMessage;
    }
    
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    public ErrorDetails getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(ErrorDetails errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    // Enums
    public enum MessageStatus {
        SUCCESS,
        ERROR,
        PENDING
    }
    
    // Nested classes
    public static class ErrorDetails {
        private String errorCode;
        private String errorMessage;
        private ErrorCategory errorCategory;
        private Boolean retryable;
        private java.util.Map<String, Object> details;
        
        // Constructors
        public ErrorDetails() {}
        
        public ErrorDetails(String errorCode, String errorMessage, ErrorCategory errorCategory,
                           Boolean retryable, java.util.Map<String, Object> details) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.errorCategory = errorCategory;
            this.retryable = retryable;
            this.details = details;
        }
        
        // Getters and Setters
        public String getErrorCode() {
            return errorCode;
        }
        
        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public ErrorCategory getErrorCategory() {
            return errorCategory;
        }
        
        public void setErrorCategory(ErrorCategory errorCategory) {
            this.errorCategory = errorCategory;
        }
        
        public Boolean getRetryable() {
            return retryable;
        }
        
        public void setRetryable(Boolean retryable) {
            this.retryable = retryable;
        }
        
        public java.util.Map<String, Object> getDetails() {
            return details;
        }
        
        public void setDetails(java.util.Map<String, Object> details) {
            this.details = details;
        }
        
        public enum ErrorCategory {
            VALIDATION,
            PROCESSING,
            NETWORK,
            AUTHENTICATION,
            TIMEOUT
        }
    }
}