package com.paymentengine.paymentprocessing.service.impl;

import com.paymentengine.paymentprocessing.dto.SchemeMessageRequest;
import com.paymentengine.paymentprocessing.dto.SchemeMessageResponse;
import com.paymentengine.paymentprocessing.service.SchemeConfigService;
import com.paymentengine.paymentprocessing.service.SchemeMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of scheme message service
 */
@Service
public class SchemeMessageServiceImpl implements SchemeMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemeMessageServiceImpl.class);
    
    private final SchemeConfigService schemeConfigService;
    
    // In-memory storage for demo purposes - in production, use proper database/Redis
    private final Map<String, Map<String, SchemeMessageResponse>> messageResponses = new ConcurrentHashMap<>();
    private final Map<String, List<SchemeMessageResponse>> messageHistory = new ConcurrentHashMap<>();
    
    @Autowired
    public SchemeMessageServiceImpl(SchemeConfigService schemeConfigService) {
        this.schemeConfigService = schemeConfigService;
    }
    
    @Override
    public SchemeMessageResponse sendMessage(String configId, SchemeMessageRequest request) {
        logger.info("Sending scheme message via configuration: {} - MessageType: {}", 
                configId, request.getMessageType());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate configuration exists and is active
            var config = schemeConfigService.getConfiguration(configId);
            if (!config.getIsActive()) {
                throw new IllegalStateException("Configuration is not active: " + configId);
            }
            
            // Process message based on interaction mode
            SchemeMessageResponse response;
            if (request.getInteractionMode() == com.paymentengine.paymentprocessing.dto.SchemeConfigRequest.InteractionMode.SYNCHRONOUS) {
                response = processSynchronousMessage(configId, request);
            } else {
                // For async messages, we still return a response but mark it as pending
                response = new SchemeMessageResponse(
                        request.getMessageId(),
                        request.getCorrelationId(),
                        SchemeMessageResponse.MessageStatus.PENDING,
                        "202",
                        "Message accepted for asynchronous processing",
                        null,
                        null,
                        System.currentTimeMillis() - startTime,
                        Instant.now()
                );
                
                // Start async processing
                CompletableFuture.runAsync(() -> {
                    try {
                        SchemeMessageResponse asyncResponse = processAsynchronousMessage(configId, request);
                        storeMessageResponse(configId, request.getCorrelationId(), asyncResponse);
                    } catch (Exception e) {
                        logger.error("Async message processing failed: {}", e.getMessage());
                        SchemeMessageResponse errorResponse = createErrorResponse(request, e);
                        storeMessageResponse(configId, request.getCorrelationId(), errorResponse);
                    }
                });
            }
            
            // Store message in history
            storeMessageInHistory(configId, response);
            
            logger.info("Scheme message processed successfully: {} - Status: {}", 
                    request.getMessageId(), response.getStatus());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing scheme message: {}", e.getMessage());
            SchemeMessageResponse errorResponse = createErrorResponse(request, e);
            storeMessageInHistory(configId, errorResponse);
            return errorResponse;
        }
    }
    
    @Override
    public CompletableFuture<SchemeMessageResponse> sendAsyncMessage(String configId, SchemeMessageRequest request) {
        logger.info("Sending async scheme message via configuration: {} - MessageType: {}", 
                configId, request.getMessageType());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return processAsynchronousMessage(configId, request);
            } catch (Exception e) {
                logger.error("Async message processing failed: {}", e.getMessage());
                return createErrorResponse(request, e);
            }
        });
    }
    
    @Override
    public SchemeMessageResponse pollResponse(String configId, String correlationId, long timeoutMs) {
        logger.debug("Polling for message response: {} - CorrelationId: {}", configId, correlationId);
        
        long startTime = System.currentTimeMillis();
        long timeoutTime = startTime + timeoutMs;
        
        while (System.currentTimeMillis() < timeoutTime) {
            Map<String, SchemeMessageResponse> configResponses = messageResponses.get(configId);
            if (configResponses != null) {
                SchemeMessageResponse response = configResponses.get(correlationId);
                if (response != null) {
                    logger.debug("Found response for correlation ID: {}", correlationId);
                    return response;
                }
            }
            
            try {
                Thread.sleep(100); // Poll every 100ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        logger.debug("No response found for correlation ID: {} within timeout: {}ms", correlationId, timeoutMs);
        return null;
    }
    
    @Override
    public SchemeMessageResponse getMessageStatus(String configId, String correlationId) {
        logger.debug("Getting message status: {} - CorrelationId: {}", configId, correlationId);
        
        Map<String, SchemeMessageResponse> configResponses = messageResponses.get(configId);
        if (configResponses != null) {
            return configResponses.get(correlationId);
        }
        
        return null;
    }
    
    @Override
    public boolean cancelMessage(String configId, String correlationId) {
        logger.info("Cancelling message: {} - CorrelationId: {}", configId, correlationId);
        
        Map<String, SchemeMessageResponse> configResponses = messageResponses.get(configId);
        if (configResponses != null) {
            SchemeMessageResponse response = configResponses.get(correlationId);
            if (response != null && response.getStatus() == SchemeMessageResponse.MessageStatus.PENDING) {
                // Create cancellation response
                SchemeMessageResponse cancelResponse = new SchemeMessageResponse(
                        response.getMessageId(),
                        response.getCorrelationId(),
                        SchemeMessageResponse.MessageStatus.ERROR,
                        "CANCELLED",
                        "Message cancelled by user",
                        null,
                        new SchemeMessageResponse.ErrorDetails(
                                "CANCELLED",
                                "Message cancelled by user",
                                SchemeMessageResponse.ErrorDetails.ErrorCategory.PROCESSING,
                                false,
                                Map.of("cancelledAt", Instant.now().toString())
                        ),
                        response.getProcessingTimeMs(),
                        Instant.now()
                );
                
                configResponses.put(correlationId, cancelResponse);
                storeMessageInHistory(configId, cancelResponse);
                
                logger.info("Message cancelled successfully: {}", correlationId);
                return true;
            }
        }
        
        logger.warn("Message not found or cannot be cancelled: {}", correlationId);
        return false;
    }
    
    @Override
    public List<SchemeMessageResponse> getMessageHistory(String configId, int limit) {
        logger.debug("Getting message history for configuration: {} - Limit: {}", configId, limit);
        
        List<SchemeMessageResponse> history = messageHistory.getOrDefault(configId, Collections.emptyList());
        
        return history.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
    }
    
    private SchemeMessageResponse processSynchronousMessage(String configId, SchemeMessageRequest request) {
        logger.debug("Processing synchronous message: {}", request.getMessageId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Simulate message processing
            Thread.sleep(100 + new Random().nextInt(200)); // 100-300ms processing time
            
            // Mock successful response
            return new SchemeMessageResponse(
                    request.getMessageId(),
                    request.getCorrelationId(),
                    SchemeMessageResponse.MessageStatus.SUCCESS,
                    "200",
                    "Message processed successfully",
                    Map.of(
                            "processedAt", Instant.now().toString(),
                            "configId", configId,
                            "messageType", request.getMessageType(),
                            "format", request.getFormat().name()
                    ),
                    null,
                    System.currentTimeMillis() - startTime,
                    Instant.now()
            );
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Message processing interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Synchronous message processing failed", e);
        }
    }
    
    private SchemeMessageResponse processAsynchronousMessage(String configId, SchemeMessageRequest request) {
        logger.debug("Processing asynchronous message: {}", request.getMessageId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Simulate longer async processing
            Thread.sleep(500 + new Random().nextInt(1000)); // 500-1500ms processing time
            
            // Mock successful response
            return new SchemeMessageResponse(
                    request.getMessageId(),
                    request.getCorrelationId(),
                    SchemeMessageResponse.MessageStatus.SUCCESS,
                    "200",
                    "Asynchronous message processed successfully",
                    Map.of(
                            "processedAt", Instant.now().toString(),
                            "configId", configId,
                            "messageType", request.getMessageType(),
                            "format", request.getFormat().name(),
                            "processingMode", "ASYNCHRONOUS"
                    ),
                    null,
                    System.currentTimeMillis() - startTime,
                    Instant.now()
            );
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Async message processing interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Asynchronous message processing failed", e);
        }
    }
    
    private SchemeMessageResponse createErrorResponse(SchemeMessageRequest request, Exception e) {
        return new SchemeMessageResponse(
                request.getMessageId(),
                request.getCorrelationId(),
                SchemeMessageResponse.MessageStatus.ERROR,
                "500",
                "Message processing failed",
                null,
                new SchemeMessageResponse.ErrorDetails(
                        "PROCESSING_ERROR",
                        e.getMessage(),
                        SchemeMessageResponse.ErrorDetails.ErrorCategory.PROCESSING,
                        true,
                        Map.of(
                                "exception", e.getClass().getSimpleName(),
                                "timestamp", Instant.now().toString()
                        )
                ),
                0L,
                Instant.now()
        );
    }
    
    private void storeMessageResponse(String configId, String correlationId, SchemeMessageResponse response) {
        messageResponses.computeIfAbsent(configId, k -> new ConcurrentHashMap<>())
                .put(correlationId, response);
    }
    
    private void storeMessageInHistory(String configId, SchemeMessageResponse response) {
        messageHistory.computeIfAbsent(configId, k -> new ArrayList<>())
                .add(response);
    }
}