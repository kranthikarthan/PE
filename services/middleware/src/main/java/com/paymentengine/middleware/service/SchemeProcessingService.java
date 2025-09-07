package com.paymentengine.middleware.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for processing messages through clearing systems (schemes)
 */
public interface SchemeProcessingService {
    
    /**
     * Process PAIN.001 message through clearing system
     */
    CompletableFuture<SchemeProcessingResult> processPain001ThroughScheme(
            Map<String, Object> pain001Message,
            String tenantId,
            String paymentType,
            String localInstrumentCode,
            String responseMode);
    
    /**
     * Process PACS.008 message from clearing system
     */
    Map<String, Object> processPacs008FromScheme(Map<String, Object> pacs008Message, String tenantId);
    
    /**
     * Generate PACS.002 response to clearing system
     */
    Map<String, Object> generatePacs002Response(String originalMessageId, 
                                              String transactionId, 
                                              String status, 
                                              String reasonCode);
    
    /**
     * Generate PAIN.002 response to client
     */
    Map<String, Object> generatePain002Response(String originalMessageId,
                                              String transactionId,
                                              String status,
                                              String reasonCode,
                                              String responseMode);
    
    /**
     * Send message to clearing system
     */
    CompletableFuture<Map<String, Object>> sendMessageToClearingSystem(
            Map<String, Object> message,
            String clearingSystemCode,
            String messageType,
            String schemeConfigurationId);
    
    /**
     * Process clearing system response
     */
    Map<String, Object> processClearingSystemResponse(Map<String, Object> response, String messageType);
    
    /**
     * Process incoming PACS.008 message from clearing system
     */
    CompletableFuture<Map<String, Object>> processIncomingPacs008(Map<String, Object> pacs008Message, String tenantId);
    
    /**
     * Scheme processing result
     */
    class SchemeProcessingResult {
        private String messageId;
        private String correlationId;
        private String status;
        private String uetr;
        private String clearingSystemCode;
        private String transactionId;
        private Map<String, Object> pacs008Message;
        private Map<String, Object> pacs002Response;
        private Map<String, Object> pain002Response;
        private String errorMessage;
        private long processingTimeMs;
        
        // Constructors
        public SchemeProcessingResult() {}
        
        public SchemeProcessingResult(String messageId, String correlationId, String status,
                                    String uetr, String clearingSystemCode, String transactionId,
                                    Map<String, Object> pacs008Message, Map<String, Object> pacs002Response,
                                    Map<String, Object> pain002Response, String errorMessage,
                                    long processingTimeMs) {
            this.messageId = messageId;
            this.correlationId = correlationId;
            this.status = status;
            this.uetr = uetr;
            this.clearingSystemCode = clearingSystemCode;
            this.transactionId = transactionId;
            this.pacs008Message = pacs008Message;
            this.pacs002Response = pacs002Response;
            this.pain002Response = pain002Response;
            this.errorMessage = errorMessage;
            this.processingTimeMs = processingTimeMs;
        }
        
        // Getters and Setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getUetr() { return uetr; }
        public void setUetr(String uetr) { this.uetr = uetr; }
        
        public String getClearingSystemCode() { return clearingSystemCode; }
        public void setClearingSystemCode(String clearingSystemCode) { this.clearingSystemCode = clearingSystemCode; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public Map<String, Object> getPacs008Message() { return pacs008Message; }
        public void setPacs008Message(Map<String, Object> pacs008Message) { this.pacs008Message = pacs008Message; }
        
        public Map<String, Object> getPacs002Response() { return pacs002Response; }
        public void setPacs002Response(Map<String, Object> pacs002Response) { this.pacs002Response = pacs002Response; }
        
        public Map<String, Object> getPain002Response() { return pain002Response; }
        public void setPain002Response(Map<String, Object> pain002Response) { this.pain002Response = pain002Response; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    }
}