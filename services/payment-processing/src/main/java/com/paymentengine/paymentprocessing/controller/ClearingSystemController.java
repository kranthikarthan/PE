package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.dto.ClearingSystemRequest;
import com.paymentengine.paymentprocessing.dto.ClearingSystemResponse;
import com.paymentengine.paymentprocessing.dto.ClearingSystemTestRequest;
import com.paymentengine.paymentprocessing.dto.ClearingSystemTestResponse;
import com.paymentengine.paymentprocessing.entity.ClearingSystemEntity;
import com.paymentengine.paymentprocessing.service.ClearingSystemService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for managing clearing system configurations
 */
@RestController
@RequestMapping("/api/v1/clearing-systems")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClearingSystemController {
    
    private static final Logger logger = LoggerFactory.getLogger(ClearingSystemController.class);
    
    private final ClearingSystemService clearingSystemService;
    
    @Autowired
    public ClearingSystemController(ClearingSystemService clearingSystemService) {
        this.clearingSystemService = clearingSystemService;
    }
    
    // ============================================================================
    // CLEARING SYSTEM CRUD OPERATIONS
    // ============================================================================
    
    /**
     * Get all clearing systems
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('clearing_system:read')")
    @Timed(value = "clearing_system.list", description = "Time taken to list clearing systems")
    public ResponseEntity<Map<String, Object>> getAllClearingSystems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String processingMode,
            @RequestParam(required = false) Boolean isActive) {
        
        logger.debug("Getting all clearing systems with filters: page={}, size={}, search={}, countryCode={}, currency={}, processingMode={}, isActive={}", 
                page, size, search, countryCode, currency, processingMode, isActive);
        
        try {
            Page<ClearingSystemResponse> clearingSystems = clearingSystemService.getAllClearingSystems(
                    page, size, sortBy, sortDir, search, countryCode, currency, processingMode, isActive);
            
            Map<String, Object> response = Map.of(
                    "success", true,
                    "data", clearingSystems.getContent(),
                    "pagination", Map.of(
                            "page", clearingSystems.getNumber(),
                            "size", clearingSystems.getSize(),
                            "totalElements", clearingSystems.getTotalElements(),
                            "totalPages", clearingSystems.getTotalPages(),
                            "first", clearingSystems.isFirst(),
                            "last", clearingSystems.isLast()
                    ),
                    "timestamp", Instant.now().toString()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting clearing systems: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to get clearing systems: " + e.getMessage(),
                            "timestamp", Instant.now().toString()
                    ));
        }
    }
    
    /**
     * Get clearing system by ID
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('clearing_system:read')")
    @Timed(value = "clearing_system.get_by_id", description = "Time taken to get clearing system by ID")
    public ResponseEntity<Map<String, Object>> getClearingSystemById(@PathVariable String id) {
        logger.debug("Getting clearing system by ID: {}", id);
        
        try {
            ClearingSystemResponse clearingSystem = clearingSystemService.getClearingSystemById(id);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", clearingSystem,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error getting clearing system by ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", "Clearing system not found: " + e.getMessage(),
                            "timestamp", Instant.now().toString()
                    ));
        }
    }
    
    /**
     * Get clearing system by code
     */
    @GetMapping(value = "/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('clearing_system:read')")
    @Timed(value = "clearing_system.get_by_code", description = "Time taken to get clearing system by code")
    public ResponseEntity<Map<String, Object>> getClearingSystemByCode(@PathVariable String code) {
        logger.debug("Getting clearing system by code: {}", code);
        
        try {
            ClearingSystemResponse clearingSystem = clearingSystemService.getClearingSystemByCode(code);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", clearingSystem,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error getting clearing system by code {}: {}", code, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", "Clearing system not found: " + e.getMessage(),
                            "timestamp", Instant.now().toString()
                    ));
        }
    }
    
    /**
     * Create new clearing system
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('clearing_system:write')")
    @Timed(value = "clearing_system.create", description = "Time taken to create clearing system")
    public ResponseEntity<Map<String, Object>> createClearingSystem(
            @Valid @RequestBody ClearingSystemRequest request) {
        
        logger.info("Creating new clearing system: {}", request.getCode());
        
        try {
            ClearingSystemResponse clearingSystem = clearingSystemService.createClearingSystem(request);
            
            logger.info("Successfully created clearing system: {}", clearingSystem.getCode());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "data", clearingSystem,
                            "message", "Clearing system created successfully",
                            "timestamp", Instant.now().toString()
                    ));
            
        } catch (Exception e) {
            logger.error("Error creating clearing system: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to create clearing system: " + e.getMessage(),
                            "timestamp", Instant.now().toString()
                    ));
        }
    }
    
    /**
     * Update clearing system
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('clearing_system:write')")
    @Timed(value = "clearing_system.update", description = "Time taken to update clearing system")
    public ResponseEntity<Map<String, Object>> updateClearingSystem(
            @PathVariable String id,
            @Valid @RequestBody ClearingSystemRequest request) {
        
        logger.info("Updating clearing system: {}", id);
        
        try {
            ClearingSystemResponse clearingSystem = clearingSystemService.updateClearingSystem(id, request);
            
            logger.info("Successfully updated clearing system: {}", clearingSystem.getCode());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", clearingSystem,
                    "message", "Clearing system updated successfully",
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error updating clearing system {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to update clearing system: " + e.getMessage(),
                            "timestamp", Instant.now().toString()
                    ));
        }
    }
    
    /**
     * Delete clearing system
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('clearing_system:delete')")
    @Timed(value = "clearing_system.delete", description = "Time taken to delete clearing system")
    public ResponseEntity<Map<String, Object>> deleteClearingSystem(@PathVariable String id) {
        logger.info("Deleting clearing system: {}", id);
        
        try {
            clearingSystemService.deleteClearingSystem(id);
            
            logger.info("Successfully deleted clearing system: {}", id);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Clearing system deleted successfully",
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error deleting clearing system {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to delete clearing system: " + e.getMessage(),
                            "timestamp", Instant.now().toString()
                    ));
        }
    }
    
    /**
     * Toggle clearing system status
     */
    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('clearing_system:write')")
    @Timed(value = "clearing_system.toggle_status", description = "Time taken to toggle clearing system status")
    public ResponseEntity<Map<String, Object>> toggleClearingSystemStatus(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request) {
        
        Boolean isActive = request.get("isActive");
        logger.info("Toggling clearing system status: {} to {}", id, isActive);
        
        try {
            ClearingSystemResponse clearingSystem = clearingSystemService.toggleClearingSystemStatus(id, isActive);
            
            logger.info("Successfully toggled clearing system status: {} to {}", id, isActive);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", clearingSystem,
                    "message", "Clearing system status updated successfully",
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error toggling clearing system status {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to update clearing system status: " + e.getMessage(),
                            "timestamp", Instant.now().toString()
                    ));
        }
    }
    
    // ============================================================================
    // STATISTICS AND ANALYTICS
    // ============================================================================
    
    /**
     * Get clearing system statistics
     */
    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('clearing_system:read')")
    @Timed(value = "clearing_system.stats", description = "Time taken to get clearing system statistics")
    public ResponseEntity<Map<String, Object>> getClearingSystemStats() {
        logger.debug("Getting clearing system statistics");
        
        try {
            Map<String, Object> stats = clearingSystemService.getClearingSystemStats();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", stats,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error getting clearing system statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to get clearing system statistics: " + e.getMessage(),
                            "timestamp", Instant.now().toString()
                    ));
        }
    }
    
    // ============================================================================
    // TESTING AND VALIDATION
    // ============================================================================
    
    /**
     * Test clearing system endpoint
     */
    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('clearing_system:test')")
    @Timed(value = "clearing_system.test", description = "Time taken to test clearing system endpoint")
    public ResponseEntity<Map<String, Object>> testClearingSystemEndpoint(
            @Valid @RequestBody ClearingSystemTestRequest request) {
        
        logger.info("Testing clearing system endpoint: {}", request.getClearingSystemId());
        
        try {
            ClearingSystemTestResponse testResponse = clearingSystemService.testClearingSystemEndpoint(request);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", testResponse,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error testing clearing system endpoint: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to test clearing system endpoint: " + e.getMessage(),
                            "timestamp", Instant.now().toString()
                    ));
        }
    }
    
    // ============================================================================
    // HEALTH AND STATUS
    // ============================================================================
    
    /**
     * Health check for clearing system service
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed(value = "clearing_system.health", description = "Time taken for clearing system health check")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "clearing-system-service",
                "timestamp", Instant.now().toString()
        ));
    }
    
    /**
     * Get clearing system service status
     */
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('clearing_system:read')")
    @Timed(value = "clearing_system.status", description = "Time taken to get clearing system service status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "version", "1.0.0",
                "features", Map.of(
                        "clearingSystemManagement", true,
                        "endpointManagement", true,
                        "tenantMapping", true,
                        "routing", true,
                        "testing", true,
                        "statistics", true
                ),
                "timestamp", Instant.now().toString()
        ));
    }
}