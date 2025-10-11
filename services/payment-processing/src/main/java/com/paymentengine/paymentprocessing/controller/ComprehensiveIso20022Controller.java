package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.service.Iso20022MessageFlowService;
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
 * Comprehensive controller for all ISO 20022 message flows between clients and clearing systems
 */
@RestController
@RequestMapping("/api/v1/iso20022/comprehensive")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ComprehensiveIso20022Controller {
    
    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveIso20022Controller.class);
    
    private final Iso20022MessageFlowService messageFlowService;
    
    @Autowired
    public ComprehensiveIso20022Controller(Iso20022MessageFlowService messageFlowService) {
        this.messageFlowService = messageFlowService;
    }
    
    // ============================================================================
    // CLIENT TO CLEARING SYSTEM MESSAGES
    // ============================================================================
    
    /**
     * Process PAIN.001 (Customer Credit Transfer Initiation) to clearing system
     */
    @PostMapping(value = "/pain001-to-clearing-system", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:send')")
    @Timed(value = "iso20022.comprehensive.pain001_to_clearing", description = "Time taken to process PAIN.001 to clearing system")
    public ResponseEntity<Map<String, Object>> processPain001ToClearingSystem(
            @Valid @RequestBody Map<String, Object> pain001Message,
            @RequestParam String tenantId,
            @RequestParam String paymentType,
            @RequestParam String localInstrumentCode,
            @RequestParam(defaultValue = "IMMEDIATE") String responseMode) {
        
        logger.info("Processing PAIN.001 to clearing system for tenant: {}, paymentType: {}, localInstrument: {}", 
                tenantId, paymentType, localInstrumentCode);
        
        try {
            CompletableFuture<Iso20022MessageFlowService.MessageFlowResult> future = 
                    messageFlowService.processPain001ToClearingSystem(
                            pain001Message, tenantId, paymentType, localInstrumentCode, responseMode);
            
            Iso20022MessageFlowService.MessageFlowResult result = future.get();
            
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("messageId", result.getMessageId());
        response.put("correlationId", result.getCorrelationId());
        response.put("status", result.getStatus());
        response.put("clearingSystemCode", result.getClearingSystemCode());
        response.put("transactionId", result.getTransactionId());
        response.put("transformedMessage", result.getTransformedMessage());
        response.put("clearingSystemResponse", result.getClearingSystemResponse());
        response.put("clientResponse", result.getClientResponse());
        response.put("processingTimeMs", result.getProcessingTimeMs());
        response.put("metadata", result.getMetadata());
        response.put("timestamp", Instant.now().toString());
            
            if ("SUCCESS".equals(result.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing PAIN.001 to clearing system: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process PAIN.001 to clearing system: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Process CAMT.055 (Payment Cancellation Request) to clearing system
     */
    @PostMapping(value = "/camt055-to-clearing-system", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:send')")
    @Timed(value = "iso20022.comprehensive.camt055_to_clearing", description = "Time taken to process CAMT.055 to clearing system")
    public ResponseEntity<Map<String, Object>> processCamt055ToClearingSystem(
            @Valid @RequestBody Map<String, Object> camt055Message,
            @RequestParam String tenantId,
            @RequestParam String originalMessageId,
            @RequestParam(defaultValue = "IMMEDIATE") String responseMode) {
        
        logger.info("Processing CAMT.055 to clearing system for tenant: {}, originalMessageId: {}", 
                tenantId, originalMessageId);
        
        try {
            CompletableFuture<Iso20022MessageFlowService.MessageFlowResult> future = 
                    messageFlowService.processCamt055ToClearingSystem(
                            camt055Message, tenantId, originalMessageId, responseMode);
            
            Iso20022MessageFlowService.MessageFlowResult result = future.get();
            
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("messageId", result.getMessageId());
        response.put("correlationId", result.getCorrelationId());
        response.put("status", result.getStatus());
        response.put("clearingSystemCode", result.getClearingSystemCode());
        response.put("transactionId", result.getTransactionId());
        response.put("transformedMessage", result.getTransformedMessage());
        response.put("clearingSystemResponse", result.getClearingSystemResponse());
        response.put("clientResponse", result.getClientResponse());
        response.put("processingTimeMs", result.getProcessingTimeMs());
        response.put("metadata", result.getMetadata());
        response.put("timestamp", Instant.now().toString());
            
            if ("SUCCESS".equals(result.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing CAMT.055 to clearing system: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process CAMT.055 to clearing system: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Process CAMT.056 (Payment Status Request) to clearing system
     */
    @PostMapping(value = "/camt056-to-clearing-system", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:send')")
    @Timed(value = "iso20022.comprehensive.camt056_to_clearing", description = "Time taken to process CAMT.056 to clearing system")
    public ResponseEntity<Map<String, Object>> processCamt056ToClearingSystem(
            @Valid @RequestBody Map<String, Object> camt056Message,
            @RequestParam String tenantId,
            @RequestParam String originalMessageId,
            @RequestParam(defaultValue = "IMMEDIATE") String responseMode) {
        
        logger.info("Processing CAMT.056 to clearing system for tenant: {}, originalMessageId: {}", 
                tenantId, originalMessageId);
        
        try {
            CompletableFuture<Iso20022MessageFlowService.MessageFlowResult> future = 
                    messageFlowService.processCamt056ToClearingSystem(
                            camt056Message, tenantId, originalMessageId, responseMode);
            
            Iso20022MessageFlowService.MessageFlowResult result = future.get();
            
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("messageId", result.getMessageId());
        response.put("correlationId", result.getCorrelationId());
        response.put("status", result.getStatus());
        response.put("clearingSystemCode", result.getClearingSystemCode());
        response.put("transactionId", result.getTransactionId());
        response.put("transformedMessage", result.getTransformedMessage());
        response.put("clearingSystemResponse", result.getClearingSystemResponse());
        response.put("clientResponse", result.getClientResponse());
        response.put("processingTimeMs", result.getProcessingTimeMs());
        response.put("metadata", result.getMetadata());
        response.put("timestamp", Instant.now().toString());
            
            if ("SUCCESS".equals(result.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing CAMT.056 to clearing system: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process CAMT.056 to clearing system: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Process PACS.028 (Payment Status Request) to clearing system
     */
    @PostMapping(value = "/pacs028-to-clearing-system", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:send')")
    @Timed(value = "iso20022.comprehensive.pacs028_to_clearing", description = "Time taken to process PACS.028 to clearing system")
    public ResponseEntity<Map<String, Object>> processPacs028ToClearingSystem(
            @Valid @RequestBody Map<String, Object> pacs028Message,
            @RequestParam String tenantId,
            @RequestParam String originalMessageId,
            @RequestParam(defaultValue = "IMMEDIATE") String responseMode) {
        
        logger.info("Processing PACS.028 to clearing system for tenant: {}, originalMessageId: {}", 
                tenantId, originalMessageId);
        
        try {
            CompletableFuture<Iso20022MessageFlowService.MessageFlowResult> future = 
                    messageFlowService.processPacs028ToClearingSystem(
                            pacs028Message, tenantId, originalMessageId, responseMode);
            
            Iso20022MessageFlowService.MessageFlowResult result = future.get();
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("messageId", result.getMessageId());
            response.put("correlationId", result.getCorrelationId());
            response.put("status", result.getStatus());
            response.put("clearingSystemCode", result.getClearingSystemCode());
            response.put("transactionId", result.getTransactionId());
            response.put("transformedMessage", result.getTransformedMessage());
            response.put("clearingSystemResponse", result.getClearingSystemResponse());
            response.put("clientResponse", result.getClientResponse());
            response.put("processingTimeMs", result.getProcessingTimeMs());
            response.put("metadata", result.getMetadata());
            response.put("timestamp", Instant.now().toString());
            
            if ("SUCCESS".equals(result.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing PACS.028 to clearing system: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process PACS.028 to clearing system: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // CLEARING SYSTEM TO CLIENT MESSAGES
    // ============================================================================
    
    /**
     * Process PACS.008 (FI to FI Customer Credit Transfer) from clearing system
     */
    @PostMapping(value = "/pacs008-from-clearing-system", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:receive')")
    @Timed(value = "iso20022.comprehensive.pacs008_from_clearing", description = "Time taken to process PACS.008 from clearing system")
    public ResponseEntity<Map<String, Object>> processPacs008FromClearingSystem(
            @Valid @RequestBody Map<String, Object> pacs008Message,
            @RequestParam String tenantId) {
        
        logger.info("Processing PACS.008 from clearing system for tenant: {}", tenantId);
        
        try {
            Map<String, Object> response = messageFlowService.processPacs008FromClearingSystem(pacs008Message, tenantId);
            
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("success", true);
        resp.put("data", response);
        resp.put("tenantId", tenantId);
        resp.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            logger.error("Error processing PACS.008 from clearing system: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process PACS.008 from clearing system: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Process PACS.002 (FI to FI Payment Status Report) from clearing system
     */
    @PostMapping(value = "/pacs002-from-clearing-system", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:receive')")
    @Timed(value = "iso20022.comprehensive.pacs002_from_clearing", description = "Time taken to process PACS.002 from clearing system")
    public ResponseEntity<Map<String, Object>> processPacs002FromClearingSystem(
            @Valid @RequestBody Map<String, Object> pacs002Message,
            @RequestParam String tenantId) {
        
        logger.info("Processing PACS.002 from clearing system for tenant: {}", tenantId);
        
        try {
            Map<String, Object> response = messageFlowService.processPacs002FromClearingSystem(pacs002Message, tenantId);
            
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("success", true);
        resp.put("data", response);
        resp.put("tenantId", tenantId);
        resp.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            logger.error("Error processing PACS.002 from clearing system: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process PACS.002 from clearing system: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Process PACS.004 (Payment Return) from clearing system
     */
    @PostMapping(value = "/pacs004-from-clearing-system", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:receive')")
    @Timed(value = "iso20022.comprehensive.pacs004_from_clearing", description = "Time taken to process PACS.004 from clearing system")
    public ResponseEntity<Map<String, Object>> processPacs004FromClearingSystem(
            @Valid @RequestBody Map<String, Object> pacs004Message,
            @RequestParam String tenantId) {
        
        logger.info("Processing PACS.004 from clearing system for tenant: {}", tenantId);
        
        try {
            Map<String, Object> response = messageFlowService.processPacs004FromClearingSystem(pacs004Message, tenantId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response,
                    "tenantId", tenantId,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error processing PACS.004 from clearing system: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process PACS.004 from clearing system: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Process CAMT.054 (Bank to Customer Debit Credit Notification) from clearing system
     */
    @PostMapping(value = "/camt054-from-clearing-system", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:receive')")
    @Timed(value = "iso20022.comprehensive.camt054_from_clearing", description = "Time taken to process CAMT.054 from clearing system")
    public ResponseEntity<Map<String, Object>> processCamt054FromClearingSystem(
            @Valid @RequestBody Map<String, Object> camt054Message,
            @RequestParam String tenantId) {
        
        logger.info("Processing CAMT.054 from clearing system for tenant: {}", tenantId);
        
        try {
            Map<String, Object> response = messageFlowService.processCamt054FromClearingSystem(camt054Message, tenantId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response,
                    "tenantId", tenantId,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error processing CAMT.054 from clearing system: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process CAMT.054 from clearing system: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Process CAMT.029 (Resolution of Investigation) from clearing system
     */
    @PostMapping(value = "/camt029-from-clearing-system", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:receive')")
    @Timed(value = "iso20022.comprehensive.camt029_from_clearing", description = "Time taken to process CAMT.029 from clearing system")
    public ResponseEntity<Map<String, Object>> processCamt029FromClearingSystem(
            @Valid @RequestBody Map<String, Object> camt029Message,
            @RequestParam String tenantId) {
        
        logger.info("Processing CAMT.029 from clearing system for tenant: {}", tenantId);
        
        try {
            Map<String, Object> response = messageFlowService.processCamt029FromClearingSystem(camt029Message, tenantId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response,
                    "tenantId", tenantId,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error processing CAMT.029 from clearing system: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process CAMT.029 from clearing system: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // MESSAGE TRANSFORMATION
    // ============================================================================
    
    /**
     * Transform PAIN.001 to PACS.008
     */
    @PostMapping(value = "/transform/pain001-to-pacs008", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:transform')")
    @Timed(value = "iso20022.comprehensive.transform_pain001_to_pacs008", description = "Time taken to transform PAIN.001 to PACS.008")
    public ResponseEntity<Map<String, Object>> transformPain001ToPacs008(
            @Valid @RequestBody Map<String, Object> pain001Message,
            @RequestParam String tenantId,
            @RequestParam String paymentType,
            @RequestParam String localInstrumentCode) {
        
        logger.info("Transforming PAIN.001 to PACS.008 for tenant: {}, paymentType: {}, localInstrument: {}", 
                tenantId, paymentType, localInstrumentCode);
        
        try {
            Map<String, Object> pacs008Message = messageFlowService.transformPain001ToPacs008(
                    pain001Message, tenantId, paymentType, localInstrumentCode);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "originalMessage", pain001Message,
                    "transformedMessage", pacs008Message,
                    "tenantId", tenantId,
                    "paymentType", paymentType,
                    "localInstrumentCode", localInstrumentCode,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error transforming PAIN.001 to PACS.008: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to transform PAIN.001 to PACS.008: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Transform CAMT.055 to PACS.007
     */
    @PostMapping(value = "/transform/camt055-to-pacs007", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:transform')")
    @Timed(value = "iso20022.comprehensive.transform_camt055_to_pacs007", description = "Time taken to transform CAMT.055 to PACS.007")
    public ResponseEntity<Map<String, Object>> transformCamt055ToPacs007(
            @Valid @RequestBody Map<String, Object> camt055Message,
            @RequestParam String tenantId) {
        
        logger.info("Transforming CAMT.055 to PACS.007 for tenant: {}", tenantId);
        
        try {
            Map<String, Object> pacs007Message = messageFlowService.transformCamt055ToPacs007(camt055Message, tenantId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "originalMessage", camt055Message,
                    "transformedMessage", pacs007Message,
                    "tenantId", tenantId,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error transforming CAMT.055 to PACS.007: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to transform CAMT.055 to PACS.007: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Transform CAMT.056 to PACS.028
     */
    @PostMapping(value = "/transform/camt056-to-pacs028", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:transform')")
    @Timed(value = "iso20022.comprehensive.transform_camt056_to_pacs028", description = "Time taken to transform CAMT.056 to PACS.028")
    public ResponseEntity<Map<String, Object>> transformCamt056ToPacs028(
            @Valid @RequestBody Map<String, Object> camt056Message,
            @RequestParam String tenantId) {
        
        logger.info("Transforming CAMT.056 to PACS.028 for tenant: {}", tenantId);
        
        try {
            Map<String, Object> pacs028Message = messageFlowService.transformCamt056ToPacs028(camt056Message, tenantId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "originalMessage", camt056Message,
                    "transformedMessage", pacs028Message,
                    "tenantId", tenantId,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error transforming CAMT.056 to PACS.028: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to transform CAMT.056 to PACS.028: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // MESSAGE VALIDATION
    // ============================================================================
    
    /**
     * Validate ISO 20022 message
     */
    @PostMapping(value = "/validate", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:validate')")
    @Timed(value = "iso20022.comprehensive.validate", description = "Time taken to validate ISO 20022 message")
    public ResponseEntity<Map<String, Object>> validateIso20022Message(
            @Valid @RequestBody Map<String, Object> message,
            @RequestParam String messageType) {
        
        logger.info("Validating ISO 20022 message of type: {}", messageType);
        
        try {
            Map<String, Object> validation = messageFlowService.validateIso20022Message(message, messageType);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "validation", validation,
                    "messageType", messageType,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error validating ISO 20022 message: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to validate ISO 20022 message: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Validate message flow
     */
    @GetMapping(value = "/validate-flow", 
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:validate')")
    @Timed(value = "iso20022.comprehensive.validate_flow", description = "Time taken to validate message flow")
    public ResponseEntity<Map<String, Object>> validateMessageFlow(
            @RequestParam String fromMessageType,
            @RequestParam String toMessageType,
            @RequestParam String flowDirection) {
        
        logger.info("Validating message flow: {} -> {} ({})", fromMessageType, toMessageType, flowDirection);
        
        try {
            Map<String, Object> validation = messageFlowService.validateMessageFlow(fromMessageType, toMessageType, flowDirection);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "validation", validation,
                    "fromMessageType", fromMessageType,
                    "toMessageType", toMessageType,
                    "flowDirection", flowDirection,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error validating message flow: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to validate message flow: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // MESSAGE FLOW TRACKING
    // ============================================================================
    
    /**
     * Get message flow history
     */
    @GetMapping(value = "/flow-history/{correlationId}", 
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('iso20022:read')")
    @Timed(value = "iso20022.comprehensive.flow_history", description = "Time taken to get message flow history")
    public ResponseEntity<Map<String, Object>> getMessageFlowHistory(@PathVariable String correlationId) {
        logger.info("Getting message flow history for correlationId: {}", correlationId);
        
        try {
            Map<String, Object> history = messageFlowService.getMessageFlowHistory(correlationId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", history,
                    "correlationId", correlationId,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error getting message flow history: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get message flow history: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // HEALTH AND STATUS
    // ============================================================================
    
    /**
     * Health check for comprehensive ISO 20022 service
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed(value = "iso20022.comprehensive.health", description = "Time taken for comprehensive ISO 20022 health check")
    public ResponseEntity<Map<String, Object>> health() {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("status", "UP");
        body.put("service", "comprehensive-iso20022-service");
        body.put("timestamp", Instant.now().toString());
        body.put("version", "1.0.0");
        java.util.Map<String, Object> features = new java.util.HashMap<>();
        features.put("pain001Processing", true);
        features.put("camt055Processing", true);
        features.put("camt056Processing", true);
        features.put("pacs028Processing", true);
        features.put("pacs008Processing", true);
        features.put("pacs002Processing", true);
        features.put("pacs004Processing", true);
        features.put("camt054Processing", true);
        features.put("camt029Processing", true);
        features.put("messageTransformation", true);
        features.put("messageValidation", true);
        features.put("flowTracking", true);
        body.put("features", features);
        return ResponseEntity.ok(body);
    }
}