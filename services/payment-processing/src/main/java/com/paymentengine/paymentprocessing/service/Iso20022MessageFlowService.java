package com.paymentengine.paymentprocessing.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling complete ISO 20022 message flows between clients and clearing systems
 */
public interface Iso20022MessageFlowService {
    
    // ============================================================================
    // CLIENT TO CLEARING SYSTEM MESSAGES
    // ============================================================================
    
    /**
     * Process PAIN.001 (Customer Credit Transfer Initiation) from client to clearing system
     */
    CompletableFuture<MessageFlowResult> processPain001ToClearingSystem(
            Map<String, Object> pain001Message,
            String tenantId,
            String paymentType,
            String localInstrumentCode,
            String responseMode);
    
    /**
     * Process CAMT.055 (Financial Institution to Financial Institution Payment Cancellation Request)
     */
    CompletableFuture<MessageFlowResult> processCamt055ToClearingSystem(
            Map<String, Object> camt055Message,
            String tenantId,
            String originalMessageId,
            String responseMode);
    
    /**
     * Process CAMT.056 (Financial Institution to Financial Institution Payment Status Request)
     */
    CompletableFuture<MessageFlowResult> processCamt056ToClearingSystem(
            Map<String, Object> camt056Message,
            String tenantId,
            String originalMessageId,
            String responseMode);
    
    /**
     * Process PACS.028 (Financial Institution to Financial Institution Payment Status Request)
     */
    CompletableFuture<MessageFlowResult> processPacs028ToClearingSystem(
            Map<String, Object> pacs028Message,
            String tenantId,
            String originalMessageId,
            String responseMode);
    
    // ============================================================================
    // CLEARING SYSTEM TO CLIENT MESSAGES
    // ============================================================================
    
    /**
     * Process PACS.008 (FI to FI Customer Credit Transfer) from clearing system to client
     */
    Map<String, Object> processPacs008FromClearingSystem(
            Map<String, Object> pacs008Message,
            String tenantId);
    
    /**
     * Process PACS.002 (FI to FI Payment Status Report) from clearing system to client
     */
    Map<String, Object> processPacs002FromClearingSystem(
            Map<String, Object> pacs002Message,
            String tenantId);
    
    /**
     * Process PACS.004 (Payment Return) from clearing system to client
     */
    Map<String, Object> processPacs004FromClearingSystem(
            Map<String, Object> pacs004Message,
            String tenantId);
    
    /**
     * Process CAMT.054 (Bank to Customer Debit Credit Notification) from clearing system to client
     */
    Map<String, Object> processCamt054FromClearingSystem(
            Map<String, Object> camt054Message,
            String tenantId);
    
    /**
     * Process CAMT.029 (Resolution of Investigation) from clearing system to client
     */
    Map<String, Object> processCamt029FromClearingSystem(
            Map<String, Object> camt029Message,
            String tenantId);
    
    // ============================================================================
    // RESPONSE GENERATION
    // ============================================================================
    
    /**
     * Generate PAIN.002 (Customer Payment Status Report) response to client
     */
    Map<String, Object> generatePain002Response(
            String originalMessageId,
            String transactionId,
            String status,
            String reasonCode,
            String responseMode);
    
    /**
     * Generate CAMT.029 (Resolution of Investigation) response to client
     */
    Map<String, Object> generateCamt029Response(
            String originalMessageId,
            String transactionId,
            String status,
            String reasonCode,
            String responseMode);
    
    /**
     * Generate CAMT.056 (Financial Institution to Financial Institution Payment Status Response)
     */
    Map<String, Object> generateCamt056Response(
            String originalMessageId,
            String transactionId,
            String status,
            String reasonCode,
            String responseMode);
    
    // ============================================================================
    // MESSAGE TRANSFORMATION
    // ============================================================================
    
    /**
     * Transform PAIN.001 to PACS.008 for clearing system
     */
    Map<String, Object> transformPain001ToPacs008(
            Map<String, Object> pain001Message,
            String tenantId,
            String paymentType,
            String localInstrumentCode);
    
    /**
     * Transform CAMT.055 to PACS.007 for clearing system
     */
    Map<String, Object> transformCamt055ToPacs007(
            Map<String, Object> camt055Message,
            String tenantId);
    
    /**
     * Transform CAMT.056 to PACS.028 for clearing system
     */
    Map<String, Object> transformCamt056ToPacs028(
            Map<String, Object> camt056Message,
            String tenantId);
    
    /**
     * Transform PACS.002 to PAIN.002 for client
     */
    Map<String, Object> transformPacs002ToPain002(
            Map<String, Object> pacs002Message,
            String tenantId);
    
    /**
     * Transform PACS.004 to PAIN.002 for client
     */
    Map<String, Object> transformPacs004ToPain002(
            Map<String, Object> pacs004Message,
            String tenantId);
    
    /**
     * Transform CAMT.054 to CAMT.053 for client
     */
    Map<String, Object> transformCamt054ToCamt053(
            Map<String, Object> camt054Message,
            String tenantId);
    
    // ============================================================================
    // MESSAGE VALIDATION
    // ============================================================================
    
    /**
     * Validate ISO 20022 message structure
     */
    Map<String, Object> validateIso20022Message(
            Map<String, Object> message,
            String messageType);
    
    /**
     * Validate message flow rules
     */
    Map<String, Object> validateMessageFlow(
            String fromMessageType,
            String toMessageType,
            String flowDirection);
    
    // ============================================================================
    // MESSAGE CORRELATION
    // ============================================================================
    
    /**
     * Correlate messages in a flow
     */
    String correlateMessage(
            String originalMessageId,
            String messageType,
            String flowDirection);
    
    /**
     * Track message flow
     */
    void trackMessageFlow(
            String correlationId,
            String messageType,
            String status,
            Map<String, Object> metadata);
    
    /**
     * Get message flow history
     */
    Map<String, Object> getMessageFlowHistory(String correlationId);
    
    /**
     * Message flow result
     */
    class MessageFlowResult {
        private String messageId;
        private String correlationId;
        private String status;
        private String clearingSystemCode;
        private String transactionId;
        private Map<String, Object> transformedMessage;
        private Map<String, Object> clearingSystemResponse;
        private Map<String, Object> clientResponse;
        private String errorMessage;
        private long processingTimeMs;
        private Map<String, Object> metadata;
        
        // Constructors
        public MessageFlowResult() {}
        
        public MessageFlowResult(String messageId, String correlationId, String status,
                               String clearingSystemCode, String transactionId,
                               Map<String, Object> transformedMessage, Map<String, Object> clearingSystemResponse,
                               Map<String, Object> clientResponse, String errorMessage,
                               long processingTimeMs, Map<String, Object> metadata) {
            this.messageId = messageId;
            this.correlationId = correlationId;
            this.status = status;
            this.clearingSystemCode = clearingSystemCode;
            this.transactionId = transactionId;
            this.transformedMessage = transformedMessage;
            this.clearingSystemResponse = clearingSystemResponse;
            this.clientResponse = clientResponse;
            this.errorMessage = errorMessage;
            this.processingTimeMs = processingTimeMs;
            this.metadata = metadata;
        }
        
        // Getters and Setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getClearingSystemCode() { return clearingSystemCode; }
        public void setClearingSystemCode(String clearingSystemCode) { this.clearingSystemCode = clearingSystemCode; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public Map<String, Object> getTransformedMessage() { return transformedMessage; }
        public void setTransformedMessage(Map<String, Object> transformedMessage) { this.transformedMessage = transformedMessage; }
        
        public Map<String, Object> getClearingSystemResponse() { return clearingSystemResponse; }
        public void setClearingSystemResponse(Map<String, Object> clearingSystemResponse) { this.clearingSystemResponse = clearingSystemResponse; }
        
        public Map<String, Object> getClientResponse() { return clientResponse; }
        public void setClientResponse(Map<String, Object> clientResponse) { this.clientResponse = clientResponse; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}