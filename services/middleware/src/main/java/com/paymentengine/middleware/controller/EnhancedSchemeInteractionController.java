package com.paymentengine.middleware.controller;

import com.paymentengine.middleware.dto.SchemeMessageRequest;
import com.paymentengine.middleware.dto.SchemeMessageResponse;
import com.paymentengine.middleware.service.SchemeProcessingService;
import com.paymentengine.middleware.service.ClearingSystemRoutingService;
import com.paymentengine.middleware.service.Pain001ToPacs008TransformationService;
import com.paymentengine.middleware.service.AdvancedPayloadTransformationService;
import com.paymentengine.middleware.entity.AdvancedPayloadMapping;
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
 * Enhanced controller for complete PAIN.001 → PACS.008 → PACS.002 → PAIN.002 flow
 */
@RestController
@RequestMapping("/api/v1/scheme/enhanced")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EnhancedSchemeInteractionController {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedSchemeInteractionController.class);
    
    private final SchemeProcessingService schemeProcessingService;
    private final ClearingSystemRoutingService clearingSystemRoutingService;
    private final Pain001ToPacs008TransformationService transformationService;
    private final AdvancedPayloadTransformationService advancedPayloadTransformationService;
    
    @Autowired
    public EnhancedSchemeInteractionController(
            SchemeProcessingService schemeProcessingService,
            ClearingSystemRoutingService clearingSystemRoutingService,
            Pain001ToPacs008TransformationService transformationService,
            AdvancedPayloadTransformationService advancedPayloadTransformationService) {
        this.schemeProcessingService = schemeProcessingService;
        this.clearingSystemRoutingService = clearingSystemRoutingService;
        this.transformationService = transformationService;
        this.advancedPayloadTransformationService = advancedPayloadTransformationService;
    }
    
    // ============================================================================
    // COMPLETE PAIN.001 PROCESSING FLOW
    // ============================================================================
    
    /**
     * Process PAIN.001 through complete clearing system flow (Synchronous)
     */
    @PostMapping(value = "/pain001/sync", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:send')")
    @Timed(value = "scheme.enhanced.pain001.sync", description = "Time taken to process PAIN.001 synchronously")
    public ResponseEntity<Map<String, Object>> processPain001Synchronous(
            @Valid @RequestBody Map<String, Object> pain001Message,
            @RequestParam String tenantId,
            @RequestParam String paymentType,
            @RequestParam String localInstrumentCode,
            @RequestParam(defaultValue = "IMMEDIATE") String responseMode) {
        
        logger.info("Processing PAIN.001 synchronously for tenant: {}, paymentType: {}, localInstrument: {}", 
                tenantId, paymentType, localInstrumentCode);
        
        try {
            // Validate PAIN.001 message
            Map<String, Object> validation = transformationService.validatePain001Message(pain001Message);
            if (!(Boolean) validation.get("valid")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid PAIN.001 message",
                        "errors", validation.get("errors"),
                        "timestamp", Instant.now().toString()
                ));
            }
            
            // Process through scheme
            CompletableFuture<SchemeProcessingService.SchemeProcessingResult> future = 
                    schemeProcessingService.processPain001ThroughScheme(
                            pain001Message, tenantId, paymentType, localInstrumentCode, responseMode);
            
            SchemeProcessingService.SchemeProcessingResult result = future.get();
            
            // Return PAIN.002 response
            Map<String, Object> response = Map.of(
                    "messageId", result.getMessageId(),
                    "correlationId", result.getCorrelationId(),
                    "status", result.getStatus(),
                    "clearingSystemCode", result.getClearingSystemCode(),
                    "transactionId", result.getTransactionId(),
                    "pain002Response", result.getPain002Response(),
                    "processingTimeMs", result.getProcessingTimeMs(),
                    "timestamp", Instant.now().toString()
            );
            
            if ("SUCCESS".equals(result.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing PAIN.001 synchronously: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process PAIN.001: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Process PAIN.001 through complete clearing system flow (Asynchronous)
     */
    @PostMapping(value = "/pain001/async", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:send')")
    @Timed(value = "scheme.enhanced.pain001.async", description = "Time taken to process PAIN.001 asynchronously")
    public ResponseEntity<Map<String, Object>> processPain001Asynchronous(
            @Valid @RequestBody Map<String, Object> pain001Message,
            @RequestParam String tenantId,
            @RequestParam String paymentType,
            @RequestParam String localInstrumentCode,
            @RequestParam(defaultValue = "WEBHOOK") String responseMode) {
        
        logger.info("Processing PAIN.001 asynchronously for tenant: {}, paymentType: {}, localInstrument: {}", 
                tenantId, paymentType, localInstrumentCode);
        
        try {
            // Validate PAIN.001 message
            Map<String, Object> validation = transformationService.validatePain001Message(pain001Message);
            if (!(Boolean) validation.get("valid")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid PAIN.001 message",
                        "errors", validation.get("errors"),
                        "timestamp", Instant.now().toString()
                ));
            }
            
            // Extract message information for correlation
            Pain001ToPacs008TransformationService.PaymentInfo paymentInfo = 
                    transformationService.extractPaymentInfo(pain001Message);
            String correlationId = "CORR-" + System.currentTimeMillis();
            
            // Start async processing
            CompletableFuture<SchemeProcessingService.SchemeProcessingResult> future = 
                    schemeProcessingService.processPain001ThroughScheme(
                            pain001Message, tenantId, paymentType, localInstrumentCode, responseMode);
            
            // Return immediate response
            Map<String, Object> immediateResponse = Map.of(
                    "messageId", paymentInfo.getMessageId(),
                    "correlationId", correlationId,
                    "status", "ACCEPTED",
                    "tenantId", tenantId,
                    "paymentType", paymentType,
                    "localInstrumentCode", localInstrumentCode,
                    "responseMode", responseMode,
                    "pollUrl", "/api/v1/scheme/enhanced/poll/" + correlationId,
                    "timestamp", Instant.now().toString()
            );
            
            logger.info("PAIN.001 accepted for async processing: {} - CorrelationId: {}", 
                    paymentInfo.getMessageId(), correlationId);
            
            return ResponseEntity.accepted().body(immediateResponse);
            
        } catch (Exception e) {
            logger.error("Error processing PAIN.001 asynchronously: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process PAIN.001: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // PACS.008 PROCESSING (FROM CLEARING SYSTEM)
    // ============================================================================
    
    /**
     * Process PACS.008 message from clearing system
     */
    @PostMapping(value = "/pacs008/process", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:receive')")
    @Timed(value = "scheme.enhanced.pacs008.process", description = "Time taken to process PACS.008 from scheme")
    public ResponseEntity<Map<String, Object>> processPacs008FromScheme(
            @Valid @RequestBody Map<String, Object> pacs008Message,
            @RequestParam String tenantId) {
        
        logger.info("Processing PACS.008 from scheme for tenant: {}", tenantId);
        
        try {
            // Process PACS.008 and generate PACS.002 response
            Map<String, Object> pacs002Response = schemeProcessingService.processPacs008FromScheme(pacs008Message, tenantId);
            
            Map<String, Object> response = Map.of(
                    "status", "PROCESSED",
                    "pacs002Response", pacs002Response,
                    "tenantId", tenantId,
                    "timestamp", Instant.now().toString()
            );
            
            logger.info("Successfully processed PACS.008 from scheme for tenant: {}", tenantId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing PACS.008 from scheme: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to process PACS.008: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // POLLING AND STATUS ENDPOINTS
    // ============================================================================
    
    /**
     * Poll for async processing result
     */
    @GetMapping(value = "/poll/{correlationId}", 
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.enhanced.poll", description = "Time taken to poll for processing result")
    public ResponseEntity<Map<String, Object>> pollProcessingResult(
            @PathVariable String correlationId,
            @RequestParam(defaultValue = "30000") long timeoutMs) {
        
        logger.debug("Polling for processing result: {}", correlationId);
        
        try {
            // In a real implementation, this would check a database or cache for the result
            // For now, return a mock response
            Map<String, Object> result = Map.of(
                    "correlationId", correlationId,
                    "status", "COMPLETED",
                    "message", "Processing completed successfully",
                    "timestamp", Instant.now().toString()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error polling for processing result: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to poll result: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // CLEARING SYSTEM ROUTING ENDPOINTS
    // ============================================================================
    
    /**
     * Get clearing system route for tenant and payment type
     */
    @GetMapping(value = "/route", 
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.enhanced.route", description = "Time taken to get clearing system route")
    public ResponseEntity<Map<String, Object>> getClearingSystemRoute(
            @RequestParam String tenantId,
            @RequestParam String paymentType,
            @RequestParam String localInstrumentCode) {
        
        logger.debug("Getting clearing system route for tenant: {}, paymentType: {}, localInstrument: {}", 
                tenantId, paymentType, localInstrumentCode);
        
        try {
            ClearingSystemRoutingService.ClearingSystemRoute route = 
                    clearingSystemRoutingService.determineClearingSystem(tenantId, paymentType, localInstrumentCode);
            
            Map<String, Object> response = Map.of(
                    "tenantId", tenantId,
                    "paymentType", paymentType,
                    "localInstrumentCode", localInstrumentCode,
                    "clearingSystemCode", route.getClearingSystemCode(),
                    "clearingSystemName", route.getClearingSystemName(),
                    "schemeConfigurationId", route.getSchemeConfigurationId(),
                    "endpointUrl", route.getEndpointUrl(),
                    "authenticationType", route.getAuthenticationType(),
                    "isActive", route.isActive(),
                    "routingPriority", route.getRoutingPriority(),
                    "timestamp", Instant.now().toString()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting clearing system route: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get clearing system route: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Get available clearing systems for tenant
     */
    @GetMapping(value = "/clearing-systems", 
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.enhanced.clearing_systems", description = "Time taken to get available clearing systems")
    public ResponseEntity<Map<String, Object>> getAvailableClearingSystems(
            @RequestParam String tenantId) {
        
        logger.debug("Getting available clearing systems for tenant: {}", tenantId);
        
        try {
            Map<String, ClearingSystemRoutingService.ClearingSystemConfig> systems = 
                    clearingSystemRoutingService.getAvailableClearingSystems(tenantId);
            
            Map<String, Object> response = Map.of(
                    "tenantId", tenantId,
                    "clearingSystems", systems,
                    "totalCount", systems.size(),
                    "timestamp", Instant.now().toString()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting available clearing systems: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get available clearing systems: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // TRANSFORMATION ENDPOINTS
    // ============================================================================
    
    /**
     * Transform PAIN.001 to PACS.008
     */
    @PostMapping(value = "/transform/pain001-to-pacs008", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:transform')")
    @Timed(value = "scheme.enhanced.transform", description = "Time taken to transform PAIN.001 to PACS.008")
    public ResponseEntity<Map<String, Object>> transformPain001ToPacs008(
            @Valid @RequestBody Map<String, Object> pain001Message,
            @RequestParam String tenantId,
            @RequestParam String paymentType,
            @RequestParam String localInstrumentCode) {
        
        logger.info("Transforming PAIN.001 to PACS.008 for tenant: {}, paymentType: {}, localInstrument: {}", 
                tenantId, paymentType, localInstrumentCode);
        
        try {
            // Validate PAIN.001 message
            Map<String, Object> validation = transformationService.validatePain001Message(pain001Message);
            if (!(Boolean) validation.get("valid")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid PAIN.001 message",
                        "errors", validation.get("errors"),
                        "timestamp", Instant.now().toString()
                ));
            }
            
            // Transform to PACS.008
            Map<String, Object> pacs008Message = transformationService.transformPain001ToPacs008(
                    pain001Message, tenantId, paymentType, localInstrumentCode);
            
            Map<String, Object> response = Map.of(
                    "originalMessage", pain001Message,
                    "transformedMessage", pacs008Message,
                    "tenantId", tenantId,
                    "paymentType", paymentType,
                    "localInstrumentCode", localInstrumentCode,
                    "timestamp", Instant.now().toString()
            );
            
            logger.info("Successfully transformed PAIN.001 to PACS.008 for tenant: {}", tenantId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error transforming PAIN.001 to PACS.008: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to transform PAIN.001 to PACS.008: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // ADVANCED PAYLOAD TRANSFORMATION ENDPOINTS
    // ============================================================================
    
    /**
     * Test advanced payload transformation
     */
    @PostMapping(value = "/transform/advanced", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('scheme:transform')")
    @Timed(value = "scheme.enhanced.transform.advanced", description = "Time taken to apply advanced payload transformation")
    public ResponseEntity<Map<String, Object>> testAdvancedPayloadTransformation(
            @Valid @RequestBody Map<String, Object> sourcePayload,
            @RequestParam String tenantId,
            @RequestParam String paymentType,
            @RequestParam String localInstrumentCode,
            @RequestParam String clearingSystemCode,
            @RequestParam(defaultValue = "REQUEST") String direction) {
        
        logger.info("Testing advanced payload transformation for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}, direction: {}", 
                   tenantId, paymentType, localInstrumentCode, clearingSystemCode, direction);
        
        try {
            // Convert direction string to enum
            AdvancedPayloadMapping.Direction directionEnum;
            switch (direction.toUpperCase()) {
                case "REQUEST":
                    directionEnum = AdvancedPayloadMapping.Direction.REQUEST;
                    break;
                case "RESPONSE":
                    directionEnum = AdvancedPayloadMapping.Direction.RESPONSE;
                    break;
                case "BIDIRECTIONAL":
                    directionEnum = AdvancedPayloadMapping.Direction.BIDIRECTIONAL;
                    break;
                default:
                    directionEnum = AdvancedPayloadMapping.Direction.REQUEST;
            }
            
            // Apply advanced payload transformation
            Optional<Map<String, Object>> transformedPayload = advancedPayloadTransformationService.transformPayload(
                    tenantId,
                    paymentType,
                    localInstrumentCode,
                    clearingSystemCode,
                    directionEnum,
                    sourcePayload
            );
            
            Map<String, Object> response = Map.of(
                    "sourcePayload", sourcePayload,
                    "transformedPayload", transformedPayload.orElse(null),
                    "transformationApplied", transformedPayload.isPresent(),
                    "tenantId", tenantId,
                    "paymentType", paymentType,
                    "localInstrumentCode", localInstrumentCode,
                    "clearingSystemCode", clearingSystemCode,
                    "direction", direction,
                    "timestamp", Instant.now().toString()
            );
            
            if (transformedPayload.isPresent()) {
                logger.info("Successfully applied advanced payload transformation for tenant: {}", tenantId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("No advanced payload mapping found for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}, direction: {}", 
                           tenantId, paymentType, localInstrumentCode, clearingSystemCode, direction);
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            logger.error("Error applying advanced payload transformation: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to apply advanced payload transformation: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // HEALTH CHECK
    // ============================================================================
    
    /**
     * Health check for enhanced scheme service
     */
    @GetMapping("/health")
    @Timed(value = "scheme.enhanced.health", description = "Time taken for enhanced scheme health check")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "enhanced-scheme-service",
                "timestamp", Instant.now().toString(),
                "version", "1.0.0",
                "features", Map.of(
                        "pain001Processing", true,
                        "pacs008Processing", true,
                        "clearingSystemRouting", true,
                        "asyncProcessing", true,
                        "transformation", true
                )
        ));
    }
}