package com.paymentengine.middleware.service;

import com.paymentengine.middleware.dto.SchemeMessageRequest;
import com.paymentengine.middleware.dto.SchemeMessageResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for scheme message processing
 */
public interface SchemeMessageService {
    
    /**
     * Send a scheme message using a specific configuration
     */
    SchemeMessageResponse sendMessage(String configId, SchemeMessageRequest request);
    
    /**
     * Send an asynchronous scheme message
     */
    CompletableFuture<SchemeMessageResponse> sendAsyncMessage(String configId, SchemeMessageRequest request);
    
    /**
     * Poll for asynchronous message response
     */
    SchemeMessageResponse pollResponse(String configId, String correlationId, long timeoutMs);
    
    /**
     * Get message status by correlation ID
     */
    SchemeMessageResponse getMessageStatus(String configId, String correlationId);
    
    /**
     * Cancel a pending message
     */
    boolean cancelMessage(String configId, String correlationId);
    
    /**
     * Get message history for a configuration
     */
    java.util.List<SchemeMessageResponse> getMessageHistory(String configId, int limit);
}