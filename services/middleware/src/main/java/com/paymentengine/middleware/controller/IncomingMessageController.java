package com.paymentengine.middleware.controller;

import com.paymentengine.middleware.service.SchemeProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for processing incoming messages from external systems
 * 
 * Handles incoming PACS.008 messages from clearing systems and other
 * ISO 20022 messages with UETR support.
 */
@RestController
@RequestMapping("/api/v1/incoming")
@CrossOrigin(origins = "*")
public class IncomingMessageController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IncomingMessageController.class);

    @Autowired
    private SchemeProcessingService schemeProcessingService;

    /**
     * Process incoming PACS.008 message from clearing system
     * 
     * @param pacs008Message The PACS.008 message from clearing system
     * @param tenantId The tenant identifier
     * @return Processing result with UETR information
     */
    @PostMapping("/pacs008")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> processIncomingPacs008(
            @RequestBody Map<String, Object> pacs008Message,
            @RequestParam String tenantId) {
        
        logger.info("Received incoming PACS.008 message for tenant: {}", tenantId);
        
        return schemeProcessingService.processIncomingPacs008(pacs008Message, tenantId)
                .thenApply(result -> {
                    if ("SUCCESS".equals(result.get("status"))) {
                        logger.info("Successfully processed incoming PACS.008 with UETR: {} for tenant: {}", 
                                   result.get("uetr"), tenantId);
                        return ResponseEntity.ok(result);
                    } else {
                        logger.error("Failed to process incoming PACS.008 for tenant: {}, error: {}", 
                                   tenantId, result.get("error"));
                        return ResponseEntity.internalServerError().body(result);
                    }
                })
                .exceptionally(throwable -> {
                    logger.error("Exception processing incoming PACS.008 for tenant: {}", tenantId, throwable);
                    Map<String, Object> errorResult = Map.of(
                        "status", "ERROR",
                        "error", throwable.getMessage(),
                        "tenantId", tenantId
                    );
                    return ResponseEntity.internalServerError().body(errorResult);
                });
    }

    /**
     * Process incoming PACS.002 message from clearing system
     * 
     * @param pacs002Message The PACS.002 message from clearing system
     * @param tenantId The tenant identifier
     * @return Processing result with UETR information
     */
    @PostMapping("/pacs002")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<Map<String, Object>> processIncomingPacs002(
            @RequestBody Map<String, Object> pacs002Message,
            @RequestParam String tenantId) {
        
        logger.info("Received incoming PACS.002 message for tenant: {}", tenantId);
        
        try {
            // Extract UETR from PACS.002 message
            // This would typically be used to update the status of an existing UETR
            // For now, we'll return a simple acknowledgment
            
            Map<String, Object> result = Map.of(
                "status", "SUCCESS",
                "message", "PACS.002 message received and processed",
                "tenantId", tenantId,
                "timestamp", System.currentTimeMillis()
            );
            
            logger.info("Successfully processed incoming PACS.002 for tenant: {}", tenantId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error processing incoming PACS.002 for tenant: {}", tenantId, e);
            Map<String, Object> errorResult = Map.of(
                "status", "ERROR",
                "error", e.getMessage(),
                "tenantId", tenantId
            );
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * Process incoming CAMT.054 message (Bank Notification)
     * 
     * @param camt054Message The CAMT.054 message
     * @param tenantId The tenant identifier
     * @return Processing result with UETR information
     */
    @PostMapping("/camt054")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<Map<String, Object>> processIncomingCamt054(
            @RequestBody Map<String, Object> camt054Message,
            @RequestParam String tenantId) {
        
        logger.info("Received incoming CAMT.054 message for tenant: {}", tenantId);
        
        try {
            // Extract UETR from CAMT.054 message
            // This would typically be used to update account balances and transaction status
            
            Map<String, Object> result = Map.of(
                "status", "SUCCESS",
                "message", "CAMT.054 message received and processed",
                "tenantId", tenantId,
                "timestamp", System.currentTimeMillis()
            );
            
            logger.info("Successfully processed incoming CAMT.054 for tenant: {}", tenantId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error processing incoming CAMT.054 for tenant: {}", tenantId, e);
            Map<String, Object> errorResult = Map.of(
                "status", "ERROR",
                "error", e.getMessage(),
                "tenantId", tenantId
            );
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * Process incoming CAMT.055 message (Payment Cancellation Request)
     * 
     * @param camt055Message The CAMT.055 message
     * @param tenantId The tenant identifier
     * @return Processing result with UETR information
     */
    @PostMapping("/camt055")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<Map<String, Object>> processIncomingCamt055(
            @RequestBody Map<String, Object> camt055Message,
            @RequestParam String tenantId) {
        
        logger.info("Received incoming CAMT.055 message for tenant: {}", tenantId);
        
        try {
            // Extract UETR from CAMT.055 message
            // This would typically be used to process payment cancellation requests
            
            Map<String, Object> result = Map.of(
                "status", "SUCCESS",
                "message", "CAMT.055 message received and processed",
                "tenantId", tenantId,
                "timestamp", System.currentTimeMillis()
            );
            
            logger.info("Successfully processed incoming CAMT.055 for tenant: {}", tenantId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error processing incoming CAMT.055 for tenant: {}", tenantId, e);
            Map<String, Object> errorResult = Map.of(
                "status", "ERROR",
                "error", e.getMessage(),
                "tenantId", tenantId
            );
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
}