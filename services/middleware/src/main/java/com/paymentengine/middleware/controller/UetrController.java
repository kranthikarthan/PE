package com.paymentengine.middleware.controller;

import com.paymentengine.shared.service.UetrGenerationService;
import com.paymentengine.shared.service.UetrTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for UETR (Unique End-to-End Transaction Reference) management
 * 
 * Provides endpoints for UETR generation, tracking, and search functionality
 * to support end-to-end transaction monitoring and reconciliation.
 */
@RestController
@RequestMapping("/api/v1/uetr")
@CrossOrigin(origins = "*")
public class UetrController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UetrController.class);

    @Autowired
    private UetrGenerationService uetrGenerationService;

    @Autowired
    private UetrTrackingService uetrTrackingService;

    /**
     * Generate a new UETR
     * 
     * @param messageType The ISO 20022 message type
     * @param tenantId The tenant identifier
     * @return Generated UETR
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<Map<String, Object>> generateUetr(
            @RequestParam String messageType,
            @RequestParam String tenantId) {
        
        try {
            logger.info("Generating UETR for messageType: {}, tenantId: {}", messageType, tenantId);
            
            String uetr = uetrGenerationService.generateUetr(messageType, tenantId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("uetr", uetr);
            response.put("messageType", messageType);
            response.put("tenantId", tenantId);
            response.put("generatedAt", LocalDateTime.now());
            response.put("timestamp", uetrGenerationService.extractTimestamp(uetr));
            response.put("systemId", uetrGenerationService.extractSystemId(uetr));
            response.put("messageTypeId", uetrGenerationService.extractMessageType(uetr));
            
            logger.info("Successfully generated UETR: {} for messageType: {}, tenantId: {}", 
                       uetr, messageType, tenantId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating UETR for messageType: {}, tenantId: {}", messageType, tenantId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate UETR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Validate UETR format
     * 
     * @param uetr The UETR to validate
     * @return Validation result
     */
    @GetMapping("/validate/{uetr}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> validateUetr(@PathVariable String uetr) {
        
        try {
            logger.debug("Validating UETR: {}", uetr);
            
            boolean isValid = uetrGenerationService.isValidUetr(uetr);
            
            Map<String, Object> response = new HashMap<>();
            response.put("uetr", uetr);
            response.put("isValid", isValid);
            
            if (isValid) {
                response.put("timestamp", uetrGenerationService.extractTimestamp(uetr));
                response.put("systemId", uetrGenerationService.extractSystemId(uetr));
                response.put("messageTypeId", uetrGenerationService.extractMessageType(uetr));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error validating UETR: {}", uetr, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to validate UETR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get UETR tracking information
     * 
     * @param uetr The UETR to lookup
     * @return UETR tracking information
     */
    @GetMapping("/track/{uetr}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getUetrTracking(@PathVariable String uetr) {
        
        try {
            logger.info("Retrieving UETR tracking for: {}", uetr);
            
            if (!uetrGenerationService.isValidUetr(uetr)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid UETR format");
                errorResponse.put("uetr", uetr);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Optional<UetrTrackingService.UetrTrackingRecord> trackingRecord = 
                    uetrTrackingService.getUetrTracking(uetr);
            
            Map<String, Object> response = new HashMap<>();
            response.put("uetr", uetr);
            response.put("found", trackingRecord.isPresent());
            
            if (trackingRecord.isPresent()) {
                UetrTrackingService.UetrTrackingRecord record = trackingRecord.get();
                response.put("trackingRecord", record);
                response.put("timestamp", uetrGenerationService.extractTimestamp(uetr));
                response.put("systemId", uetrGenerationService.extractSystemId(uetr));
                response.put("messageTypeId", uetrGenerationService.extractMessageType(uetr));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving UETR tracking for: {}", uetr, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve UETR tracking");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get UETR journey (all related UETRs and their statuses)
     * 
     * @param uetr The UETR to trace
     * @return UETR journey information
     */
    @GetMapping("/journey/{uetr}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getUetrJourney(@PathVariable String uetr) {
        
        try {
            logger.info("Retrieving UETR journey for: {}", uetr);
            
            if (!uetrGenerationService.isValidUetr(uetr)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid UETR format");
                errorResponse.put("uetr", uetr);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            UetrTrackingService.UetrJourney journey = uetrTrackingService.getUetrJourney(uetr);
            
            Map<String, Object> response = new HashMap<>();
            response.put("uetr", uetr);
            response.put("journey", journey);
            response.put("timestamp", uetrGenerationService.extractTimestamp(uetr));
            response.put("systemId", uetrGenerationService.extractSystemId(uetr));
            response.put("messageTypeId", uetrGenerationService.extractMessageType(uetr));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving UETR journey for: {}", uetr, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve UETR journey");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Search UETRs by various criteria
     * 
     * @param tenantId Optional tenant ID filter
     * @param messageType Optional message type filter
     * @param status Optional status filter
     * @param dateFrom Optional date from filter (ISO format)
     * @param dateTo Optional date to filter (ISO format)
     * @return List of matching UETR tracking records
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> searchUetrs(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        
        try {
            logger.info("Searching UETRs with filters - tenantId: {}, messageType: {}, status: {}, dateFrom: {}, dateTo: {}", 
                       tenantId, messageType, status, dateFrom, dateTo);
            
            LocalDateTime fromDate = null;
            LocalDateTime toDate = null;
            
            if (dateFrom != null) {
                fromDate = LocalDateTime.parse(dateFrom);
            }
            if (dateTo != null) {
                toDate = LocalDateTime.parse(dateTo);
            }
            
            List<UetrTrackingService.UetrTrackingRecord> results = 
                    uetrTrackingService.searchUetrs(tenantId, messageType, status, fromDate, toDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            response.put("count", results.size());
            response.put("filters", Map.of(
                "tenantId", tenantId != null ? tenantId : "all",
                "messageType", messageType != null ? messageType : "all",
                "status", status != null ? status : "all",
                "dateFrom", dateFrom != null ? dateFrom : "all",
                "dateTo", dateTo != null ? dateTo : "all"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching UETRs with filters", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to search UETRs");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get UETR statistics for monitoring
     * 
     * @param tenantId Optional tenant ID filter
     * @param dateFrom Optional date from filter (ISO format)
     * @param dateTo Optional date to filter (ISO format)
     * @return UETR statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<Map<String, Object>> getUetrStatistics(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        
        try {
            logger.info("Retrieving UETR statistics for tenantId: {}, dateFrom: {}, dateTo: {}", 
                       tenantId, dateFrom, dateTo);
            
            LocalDateTime fromDate = null;
            LocalDateTime toDate = null;
            
            if (dateFrom != null) {
                fromDate = LocalDateTime.parse(dateFrom);
            }
            if (dateTo != null) {
                toDate = LocalDateTime.parse(dateTo);
            }
            
            UetrTrackingService.UetrStatistics statistics = 
                    uetrTrackingService.getUetrStatistics(tenantId, fromDate, toDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("statistics", statistics);
            response.put("generatedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving UETR statistics for tenantId: {}", tenantId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve UETR statistics");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Check if two UETRs are related
     * 
     * @param uetr1 First UETR
     * @param uetr2 Second UETR
     * @return Relationship information
     */
    @GetMapping("/related/{uetr1}/{uetr2}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> checkRelatedUetrs(
            @PathVariable String uetr1, 
            @PathVariable String uetr2) {
        
        try {
            logger.debug("Checking if UETRs are related: {} and {}", uetr1, uetr2);
            
            boolean areRelated = uetrGenerationService.areRelatedUetrs(uetr1, uetr2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("uetr1", uetr1);
            response.put("uetr2", uetr2);
            response.put("areRelated", areRelated);
            
            if (areRelated) {
                response.put("commonTimestamp", uetrGenerationService.extractTimestamp(uetr1));
                response.put("commonSystemId", uetrGenerationService.extractSystemId(uetr1));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking related UETRs: {} and {}", uetr1, uetr2, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to check UETR relationship");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}