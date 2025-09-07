package com.paymentengine.middleware.controller;

import com.paymentengine.middleware.dto.SchemeMessageRequest;
import com.paymentengine.middleware.dto.SchemeMessageResponse;
import com.paymentengine.middleware.service.SchemeConfigService;
import com.paymentengine.middleware.service.SchemeMessageService;
import com.paymentengine.middleware.service.Iso20022FormatService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for scheme interaction with configurable sync/async endpoints
 */
@RestController
@RequestMapping("/api/v1/scheme/interaction")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SchemeInteractionController {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemeInteractionController.class);
    
    private final SchemeConfigService schemeConfigService;
    private final SchemeMessageService schemeMessageService;
    private final Iso20022FormatService iso20022FormatService;
    
    @Autowired
    public SchemeInteractionController(
            SchemeConfigService schemeConfigService,
            SchemeMessageService schemeMessageService,
            Iso20022FormatService iso20022FormatService) {
        this.schemeConfigService = schemeConfigService;
        this.schemeMessageService = schemeMessageService;
        this.iso20022FormatService = iso20022FormatService;
    }
    
    // ============================================================================
    // SYNCHRONOUS ENDPOINTS
    // ============================================================================
    
    /**
     * Send synchronous ISO 20022 message (JSON format)
     */
    @PostMapping(value = "/sync/json/{configId}", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:send')")
    @Timed(value = "scheme.interaction.sync.json", description = "Time taken to send sync JSON message")
    public ResponseEntity<SchemeMessageResponse> sendSynchronousJsonMessage(
            @PathVariable String configId,
            @Valid @RequestBody Map<String, Object> messagePayload,
            @RequestParam(defaultValue = "pain001") String messageType) {
        
        logger.info("Sending synchronous JSON message via configuration: {} - MessageType: {}", 
                configId, messageType);
        
        try {
            // Validate configuration supports synchronous mode
            var config = schemeConfigService.getConfiguration(configId);
            if (!config.getIsActive()) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "Configuration is not active", "CONFIG_INACTIVE"));
            }
            
            if (config.getInteractionMode() != com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.SYNCHRONOUS &&
                config.getInteractionMode() != com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.HYBRID) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "Configuration does not support synchronous mode", "MODE_NOT_SUPPORTED"));
            }
            
            // Create message request
            SchemeMessageRequest request = createMessageRequest(
                    messageType, messagePayload, 
                    com.paymentengine.middleware.dto.SchemeConfigRequest.MessageFormat.JSON,
                    com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.SYNCHRONOUS
            );
            
            // Send message
            SchemeMessageResponse response = schemeMessageService.sendMessage(configId, request);
            
            logger.info("Synchronous JSON message sent successfully: {} - Status: {}", 
                    request.getMessageId(), response.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error sending synchronous JSON message: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(
                    "Failed to send message: " + e.getMessage(), "SEND_ERROR"));
        }
    }
    
    /**
     * Send synchronous ISO 20022 message (XML format)
     */
    @PostMapping(value = "/sync/xml/{configId}", 
                 consumes = MediaType.APPLICATION_XML_VALUE,
                 produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAuthority('scheme:send')")
    @Timed(value = "scheme.interaction.sync.xml", description = "Time taken to send sync XML message")
    public ResponseEntity<String> sendSynchronousXmlMessage(
            @PathVariable String configId,
            @RequestBody String xmlMessage,
            @RequestParam(defaultValue = "pain001") String messageType) {
        
        logger.info("Sending synchronous XML message via configuration: {} - MessageType: {}", 
                configId, messageType);
        
        try {
            // Validate configuration supports synchronous mode
            var config = schemeConfigService.getConfiguration(configId);
            if (!config.getIsActive()) {
                return ResponseEntity.badRequest().body(createXmlErrorResponse(
                        "Configuration is not active", "CONFIG_INACTIVE"));
            }
            
            if (config.getInteractionMode() != com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.SYNCHRONOUS &&
                config.getInteractionMode() != com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.HYBRID) {
                return ResponseEntity.badRequest().body(createXmlErrorResponse(
                        "Configuration does not support synchronous mode", "MODE_NOT_SUPPORTED"));
            }
            
            // Parse XML message
            Object messagePayload = iso20022FormatService.deserializeFromXml(xmlMessage, messageType);
            
            // Create message request
            SchemeMessageRequest request = createMessageRequest(
                    messageType, messagePayload, 
                    com.paymentengine.middleware.dto.SchemeConfigRequest.MessageFormat.XML,
                    com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.SYNCHRONOUS
            );
            
            // Send message
            SchemeMessageResponse response = schemeMessageService.sendMessage(configId, request);
            
            // Convert response to XML
            String xmlResponse = iso20022FormatService.serializeToXml(response, messageType);
            
            logger.info("Synchronous XML message sent successfully: {} - Status: {}", 
                    request.getMessageId(), response.getStatus());
            
            return ResponseEntity.ok(xmlResponse);
            
        } catch (Exception e) {
            logger.error("Error sending synchronous XML message: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createXmlErrorResponse(
                    "Failed to send message: " + e.getMessage(), "SEND_ERROR"));
        }
    }
    
    // ============================================================================
    // ASYNCHRONOUS ENDPOINTS
    // ============================================================================
    
    /**
     * Send asynchronous ISO 20022 message (JSON format)
     */
    @PostMapping(value = "/async/json/{configId}", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:send')")
    @Timed(value = "scheme.interaction.async.json", description = "Time taken to send async JSON message")
    public ResponseEntity<Map<String, Object>> sendAsynchronousJsonMessage(
            @PathVariable String configId,
            @Valid @RequestBody Map<String, Object> messagePayload,
            @RequestParam(defaultValue = "pain001") String messageType) {
        
        logger.info("Sending asynchronous JSON message via configuration: {} - MessageType: {}", 
                configId, messageType);
        
        try {
            // Validate configuration supports asynchronous mode
            var config = schemeConfigService.getConfiguration(configId);
            if (!config.getIsActive()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Configuration is not active",
                        "errorCode", "CONFIG_INACTIVE"
                ));
            }
            
            if (config.getInteractionMode() != com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.ASYNCHRONOUS &&
                config.getInteractionMode() != com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.HYBRID) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Configuration does not support asynchronous mode",
                        "errorCode", "MODE_NOT_SUPPORTED"
                ));
            }
            
            // Create message request
            SchemeMessageRequest request = createMessageRequest(
                    messageType, messagePayload, 
                    com.paymentengine.middleware.dto.SchemeConfigRequest.MessageFormat.JSON,
                    com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.ASYNCHRONOUS
            );
            
            // Send async message
            CompletableFuture<SchemeMessageResponse> future = schemeMessageService.sendAsyncMessage(configId, request);
            
            // Return immediate response
            Map<String, Object> immediateResponse = Map.of(
                    "messageId", request.getMessageId(),
                    "correlationId", request.getCorrelationId(),
                    "status", "ACCEPTED",
                    "messageType", messageType,
                    "format", "JSON",
                    "timestamp", Instant.now().toString(),
                    "pollUrl", "/api/v1/scheme/interaction/poll/" + configId + "/" + request.getCorrelationId()
            );
            
            logger.info("Asynchronous JSON message accepted: {} - CorrelationId: {}", 
                    request.getMessageId(), request.getCorrelationId());
            
            return ResponseEntity.accepted().body(immediateResponse);
            
        } catch (Exception e) {
            logger.error("Error sending asynchronous JSON message: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to send message: " + e.getMessage(),
                    "errorCode", "SEND_ERROR"
            ));
        }
    }
    
    /**
     * Send asynchronous ISO 20022 message (XML format)
     */
    @PostMapping(value = "/async/xml/{configId}", 
                 consumes = MediaType.APPLICATION_XML_VALUE,
                 produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAuthority('scheme:send')")
    @Timed(value = "scheme.interaction.async.xml", description = "Time taken to send async XML message")
    public ResponseEntity<String> sendAsynchronousXmlMessage(
            @PathVariable String configId,
            @RequestBody String xmlMessage,
            @RequestParam(defaultValue = "pain001") String messageType) {
        
        logger.info("Sending asynchronous XML message via configuration: {} - MessageType: {}", 
                configId, messageType);
        
        try {
            // Validate configuration supports asynchronous mode
            var config = schemeConfigService.getConfiguration(configId);
            if (!config.getIsActive()) {
                return ResponseEntity.badRequest().body(createXmlErrorResponse(
                        "Configuration is not active", "CONFIG_INACTIVE"));
            }
            
            if (config.getInteractionMode() != com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.ASYNCHRONOUS &&
                config.getInteractionMode() != com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.HYBRID) {
                return ResponseEntity.badRequest().body(createXmlErrorResponse(
                        "Configuration does not support asynchronous mode", "MODE_NOT_SUPPORTED"));
            }
            
            // Parse XML message
            Object messagePayload = iso20022FormatService.deserializeFromXml(xmlMessage, messageType);
            
            // Create message request
            SchemeMessageRequest request = createMessageRequest(
                    messageType, messagePayload, 
                    com.paymentengine.middleware.dto.SchemeConfigRequest.MessageFormat.XML,
                    com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.ASYNCHRONOUS
            );
            
            // Send async message
            CompletableFuture<SchemeMessageResponse> future = schemeMessageService.sendAsyncMessage(configId, request);
            
            // Return immediate XML response
            String xmlResponse = String.format("""
                    <?xml version="1.0" encoding="UTF-8"?>
                    <AsyncResponse>
                        <messageId>%s</messageId>
                        <correlationId>%s</correlationId>
                        <status>ACCEPTED</status>
                        <messageType>%s</messageType>
                        <format>XML</format>
                        <timestamp>%s</timestamp>
                        <pollUrl>/api/v1/scheme/interaction/poll/%s/%s</pollUrl>
                    </AsyncResponse>
                    """, request.getMessageId(), request.getCorrelationId(), messageType, 
                    Instant.now().toString(), configId, request.getCorrelationId());
            
            logger.info("Asynchronous XML message accepted: {} - CorrelationId: {}", 
                    request.getMessageId(), request.getCorrelationId());
            
            return ResponseEntity.accepted().body(xmlResponse);
            
        } catch (Exception e) {
            logger.error("Error sending asynchronous XML message: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createXmlErrorResponse(
                    "Failed to send message: " + e.getMessage(), "SEND_ERROR"));
        }
    }
    
    // ============================================================================
    // POLLING ENDPOINTS
    // ============================================================================
    
    /**
     * Poll for asynchronous message response (JSON)
     */
    @GetMapping(value = "/poll/{configId}/{correlationId}", 
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.interaction.poll", description = "Time taken to poll for message response")
    public ResponseEntity<SchemeMessageResponse> pollMessageResponse(
            @PathVariable String configId,
            @PathVariable String correlationId,
            @RequestParam(defaultValue = "30000") long timeoutMs) {
        
        logger.debug("Polling for message response: {} - CorrelationId: {}", configId, correlationId);
        
        try {
            SchemeMessageResponse response = schemeMessageService.pollResponse(configId, correlationId, timeoutMs);
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            logger.error("Error polling for message response: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(createErrorResponse(
                    "Failed to poll response: " + e.getMessage(), "POLL_ERROR"));
        }
    }
    
    /**
     * Poll for asynchronous message response (XML)
     */
    @GetMapping(value = "/poll/{configId}/{correlationId}/xml", 
                produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.interaction.poll.xml", description = "Time taken to poll for XML message response")
    public ResponseEntity<String> pollMessageResponseXml(
            @PathVariable String configId,
            @PathVariable String correlationId,
            @RequestParam(defaultValue = "30000") long timeoutMs) {
        
        logger.debug("Polling for XML message response: {} - CorrelationId: {}", configId, correlationId);
        
        try {
            SchemeMessageResponse response = schemeMessageService.pollResponse(configId, correlationId, timeoutMs);
            if (response != null) {
                String xmlResponse = iso20022FormatService.serializeToXml(response, "response");
                return ResponseEntity.ok(xmlResponse);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            logger.error("Error polling for XML message response: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(createXmlErrorResponse(
                    "Failed to poll response: " + e.getMessage(), "POLL_ERROR"));
        }
    }
    
    // ============================================================================
    // MESSAGE STATUS ENDPOINTS
    // ============================================================================
    
    /**
     * Get message status by correlation ID
     */
    @GetMapping(value = "/status/{configId}/{correlationId}", 
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.interaction.status", description = "Time taken to get message status")
    public ResponseEntity<SchemeMessageResponse> getMessageStatus(
            @PathVariable String configId,
            @PathVariable String correlationId) {
        
        logger.debug("Getting message status: {} - CorrelationId: {}", configId, correlationId);
        
        try {
            SchemeMessageResponse response = schemeMessageService.getMessageStatus(configId, correlationId);
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting message status: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(createErrorResponse(
                    "Failed to get message status: " + e.getMessage(), "STATUS_ERROR"));
        }
    }
    
    /**
     * Cancel a pending message
     */
    @PostMapping(value = "/cancel/{configId}/{correlationId}", 
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:cancel')")
    @Timed(value = "scheme.interaction.cancel", description = "Time taken to cancel message")
    public ResponseEntity<Map<String, Object>> cancelMessage(
            @PathVariable String configId,
            @PathVariable String correlationId) {
        
        logger.info("Cancelling message: {} - CorrelationId: {}", configId, correlationId);
        
        try {
            boolean cancelled = schemeMessageService.cancelMessage(configId, correlationId);
            if (cancelled) {
                return ResponseEntity.ok(Map.of(
                        "message", "Message cancelled successfully",
                        "correlationId", correlationId,
                        "timestamp", Instant.now().toString()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Message not found or cannot be cancelled",
                        "correlationId", correlationId
                ));
            }
        } catch (Exception e) {
            logger.error("Error cancelling message: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to cancel message: " + e.getMessage(),
                    "correlationId", correlationId
            ));
        }
    }
    
    // ============================================================================
    // FORMAT CONVERSION ENDPOINTS
    // ============================================================================
    
    /**
     * Convert message between JSON and XML formats
     */
    @PostMapping(value = "/convert/{fromFormat}/{toFormat}", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:convert')")
    @Timed(value = "scheme.interaction.convert", description = "Time taken to convert message format")
    public ResponseEntity<Object> convertMessageFormat(
            @PathVariable String fromFormat,
            @PathVariable String toFormat,
            @RequestBody Map<String, Object> request,
            @RequestParam(defaultValue = "pain001") String messageType) {
        
        logger.info("Converting message from {} to {} for message type: {}", fromFormat, toFormat, messageType);
        
        try {
            com.paymentengine.middleware.dto.SchemeConfigRequest.MessageFormat from = 
                    com.paymentengine.middleware.dto.SchemeConfigRequest.MessageFormat.valueOf(fromFormat.toUpperCase());
            com.paymentengine.middleware.dto.SchemeConfigRequest.MessageFormat to = 
                    com.paymentengine.middleware.dto.SchemeConfigRequest.MessageFormat.valueOf(toFormat.toUpperCase());
            
            Object convertedMessage = iso20022FormatService.convertMessage(request, from, to, messageType);
            
            return ResponseEntity.ok(convertedMessage);
            
        } catch (Exception e) {
            logger.error("Error converting message format: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to convert message format: " + e.getMessage()
            ));
        }
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private SchemeMessageRequest createMessageRequest(
            String messageType, Object payload, 
            com.paymentengine.middleware.dto.SchemeConfigRequest.MessageFormat format,
            com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode interactionMode) {
        
        String messageId = "MSG-" + System.currentTimeMillis();
        String correlationId = "CORR-" + System.currentTimeMillis();
        
        return new SchemeMessageRequest(
                messageType,
                messageId,
                correlationId,
                format,
                interactionMode,
                payload,
                Map.of(
                        "timestamp", Instant.now().toString(),
                        "source", "scheme-interaction-controller"
                )
        );
    }
    
    private SchemeMessageResponse createErrorResponse(String message, String errorCode) {
        return new SchemeMessageResponse(
                null,
                null,
                SchemeMessageResponse.MessageStatus.ERROR,
                "400",
                message,
                null,
                new SchemeMessageResponse.ErrorDetails(
                        errorCode,
                        message,
                        SchemeMessageResponse.ErrorDetails.ErrorCategory.PROCESSING,
                        false,
                        Map.of("timestamp", Instant.now().toString())
                ),
                0L,
                Instant.now()
        );
    }
    
    private String createXmlErrorResponse(String message, String errorCode) {
        return String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <ErrorResponse>
                    <errorCode>%s</errorCode>
                    <errorMessage>%s</errorMessage>
                    <timestamp>%s</timestamp>
                </ErrorResponse>
                """, errorCode, message, Instant.now().toString());
    }
}