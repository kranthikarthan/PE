package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.service.DownstreamRoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for downstream routing operations
 * Handles routing to external services via bank's NGINX
 */
@RestController
@RequestMapping("/api/v1/downstream")
@CrossOrigin(origins = "*")
public class DownstreamRoutingController {
    
    private static final Logger logger = LoggerFactory.getLogger(DownstreamRoutingController.class);
    
    @Autowired
    private DownstreamRoutingService downstreamRoutingService;
    
    /**
     * Call fraud system for tenant
     */
    @PostMapping("/fraud/{tenantId}")
    @PreAuthorize("hasAuthority('downstream:fraud:call')")
    public ResponseEntity<Map<String, Object>> callFraudSystem(
            @PathVariable String tenantId,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader Map<String, String> headers) {
        
        logger.info("Received fraud system call request for tenant: {}", tenantId);
        
        try {
            // Validate tenant access
            if (!downstreamRoutingService.validateTenantAccess(tenantId, "fraud")) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("error", "Access denied");
                resp.put("message", "Tenant does not have access to fraud system");
                resp.put("tenantId", tenantId);
                return ResponseEntity.status(403).body(resp);
            }
            
            // Call fraud system
            ResponseEntity<Map<String, Object>> response = downstreamRoutingService.callFraudSystem(
                tenantId, requestBody, Map.class, headers);
            
            logger.info("Fraud system call successful for tenant: {}", tenantId);
            return response;
            
        } catch (Exception e) {
            logger.error("Fraud system call failed for tenant: {}", tenantId, e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("error", "Fraud system call failed");
            resp.put("message", e.getMessage());
            resp.put("tenantId", tenantId);
            return ResponseEntity.status(500).body(resp);
        }
    }
    
    /**
     * Call clearing system for tenant
     */
    @PostMapping("/clearing/{tenantId}")
    @PreAuthorize("hasAuthority('downstream:clearing:call')")
    public ResponseEntity<Map<String, Object>> callClearingSystem(
            @PathVariable String tenantId,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader Map<String, String> headers) {
        
        logger.info("Received clearing system call request for tenant: {}", tenantId);
        
        try {
            // Validate tenant access
            if (!downstreamRoutingService.validateTenantAccess(tenantId, "clearing")) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("error", "Access denied");
                resp.put("message", "Tenant does not have access to clearing system");
                resp.put("tenantId", tenantId);
                return ResponseEntity.status(403).body(resp);
            }
            
            // Call clearing system
            ResponseEntity<Map<String, Object>> response = downstreamRoutingService.callClearingSystem(
                tenantId, requestBody, Map.class, headers);
            
            logger.info("Clearing system call successful for tenant: {}", tenantId);
            return response;
            
        } catch (Exception e) {
            logger.error("Clearing system call failed for tenant: {}", tenantId, e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("error", "Clearing system call failed");
            resp.put("message", e.getMessage());
            resp.put("tenantId", tenantId);
            return ResponseEntity.status(500).body(resp);
        }
    }
    
    /**
     * Call external service with automatic routing
     */
    @PostMapping("/auto/{tenantId}")
    @PreAuthorize("hasAuthority('downstream:auto:call')")
    public ResponseEntity<Map<String, Object>> callExternalServiceAuto(
            @PathVariable String tenantId,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader Map<String, String> headers) {
        
        logger.info("Received auto-routing external service call request for tenant: {}", tenantId);
        
        try {
            // Validate tenant access
            if (!downstreamRoutingService.validateTenantAccess(tenantId, "auto")) {
                java.util.Map<String, Object> resp = new java.util.HashMap<>();
                resp.put("error", "Access denied");
                resp.put("message", "Tenant does not have access to external services");
                resp.put("tenantId", tenantId);
                return ResponseEntity.status(403).body(resp);
            }
            
            // Call external service with auto-routing
            ResponseEntity<Map<String, Object>> response = downstreamRoutingService.callExternalService(
                tenantId, requestBody, Map.class, headers);
            
            logger.info("Auto-routing external service call successful for tenant: {}", tenantId);
            return response;
            
        } catch (Exception e) {
            logger.error("Auto-routing external service call failed for tenant: {}", tenantId, e);
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("error", "External service call failed");
            resp.put("message", e.getMessage());
            resp.put("tenantId", tenantId);
            return ResponseEntity.status(500).body(resp);
        }
    }
    
    /**
     * Call external service with specific service type
     */
    @PostMapping("/service/{tenantId}/{serviceType}")
    @PreAuthorize("hasAuthority('downstream:service:call')")
    public ResponseEntity<Map<String, Object>> callExternalService(
            @PathVariable String tenantId,
            @PathVariable String serviceType,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader Map<String, String> headers) {
        
        logger.info("Received external service call request for tenant: {} to service: {}", tenantId, serviceType);
        
        try {
            // Validate tenant access
            if (!downstreamRoutingService.validateTenantAccess(tenantId, serviceType)) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Access denied",
                    "message", "Tenant does not have access to service: " + serviceType,
                    "tenantId", tenantId,
                    "serviceType", serviceType
                ));
            }
            
            // Call external service
            ResponseEntity<Map<String, Object>> response = downstreamRoutingService.callExternalService(
                tenantId, serviceType, requestBody, Map.class, headers);
            
            logger.info("External service call successful for tenant: {} to service: {}", tenantId, serviceType);
            return response;
            
        } catch (Exception e) {
            logger.error("External service call failed for tenant: {} to service: {}", tenantId, serviceType, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "External service call failed",
                "message", e.getMessage(),
                "tenantId", tenantId,
                "serviceType", serviceType
            ));
        }
    }
    
    /**
     * Get tenant downstream configuration
     */
    @GetMapping("/config/{tenantId}")
    @PreAuthorize("hasAuthority('downstream:config:read')")
    public ResponseEntity<Map<String, String>> getTenantDownstreamConfig(@PathVariable String tenantId) {
        logger.info("Getting downstream configuration for tenant: {}", tenantId);
        
        try {
            Map<String, String> config = downstreamRoutingService.getTenantDownstreamConfig(tenantId);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Failed to get downstream configuration for tenant: {}", tenantId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get configuration",
                "message", e.getMessage(),
                "tenantId", tenantId
            ));
        }
    }
    
    /**
     * Get downstream service statistics
     */
    @GetMapping("/stats/{tenantId}")
    @PreAuthorize("hasAuthority('downstream:stats:read')")
    public ResponseEntity<Map<String, Object>> getDownstreamStats(@PathVariable String tenantId) {
        logger.info("Getting downstream statistics for tenant: {}", tenantId);
        
        try {
            Map<String, Object> stats = downstreamRoutingService.getDownstreamStats(tenantId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Failed to get downstream statistics for tenant: {}", tenantId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get statistics",
                "message", e.getMessage(),
                "tenantId", tenantId
            ));
        }
    }
    
    /**
     * Health check for downstream routing
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "downstream-routing");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
}