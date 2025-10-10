package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.service.EnhancedDownstreamRoutingService;
import com.paymentengine.paymentprocessing.service.MultiLevelAuthConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced controller for downstream routing operations with multi-level authentication configuration
 * Supports configuration at clearing system, tenant, payment type, and downstream call levels
 */
@RestController
@RequestMapping("/api/v1/enhanced-downstream")
@CrossOrigin(origins = "*")
public class EnhancedDownstreamRoutingController {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedDownstreamRoutingController.class);
    
    @Autowired
    private EnhancedDownstreamRoutingService enhancedDownstreamRoutingService;
    
    @Autowired
    private MultiLevelAuthConfigurationService multiLevelAuthConfigService;
    
    /**
     * Call external service with multi-level configuration
     */
    @PostMapping("/call/{tenantId}/{serviceType}/{endpoint}")
    @PreAuthorize("hasAuthority('enhanced:downstream:call')")
    public ResponseEntity<Map<String, Object>> callExternalService(
            @PathVariable String tenantId,
            @PathVariable String serviceType,
            @PathVariable String endpoint,
            @RequestParam(required = false) String paymentType,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader Map<String, String> headers) {
        
        logger.info("Received enhanced downstream call request - tenant: {}, service: {}, endpoint: {}, paymentType: {}", 
                   tenantId, serviceType, endpoint, paymentType);
        
        try {
            // Validate tenant access
            if (!enhancedDownstreamRoutingService.validateTenantAccess(tenantId, serviceType, endpoint, paymentType)) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("error", "Access denied");
                resp.put("message", "Tenant does not have access to this service");
                resp.put("tenantId", tenantId);
                resp.put("serviceType", serviceType);
                resp.put("endpoint", endpoint);
                resp.put("paymentType", paymentType);
                return ResponseEntity.status(403).body(resp);
            }
            
            // Call external service
            ResponseEntity<Map<String, Object>> response = enhancedDownstreamRoutingService.callExternalService(
                tenantId, serviceType, endpoint, paymentType, requestBody, Map.class, headers);
            
            logger.info("Enhanced downstream call successful - tenant: {}, service: {}, endpoint: {}", 
                       tenantId, serviceType, endpoint);
            return response;
            
        } catch (Exception e) {
            logger.error("Enhanced downstream call failed - tenant: {}, service: {}, endpoint: {}", 
                        tenantId, serviceType, endpoint, e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("error", "Enhanced downstream call failed");
            resp.put("message", e.getMessage());
            resp.put("tenantId", tenantId);
            resp.put("serviceType", serviceType);
            resp.put("endpoint", endpoint);
            resp.put("paymentType", paymentType);
            return ResponseEntity.status(500).body(resp);
        }
    }
    
    /**
     * Call fraud system with multi-level configuration
     */
    @PostMapping("/fraud/{tenantId}")
    @PreAuthorize("hasAuthority('enhanced:downstream:fraud:call')")
    public ResponseEntity<Map<String, Object>> callFraudSystem(
            @PathVariable String tenantId,
            @RequestParam(required = false) String paymentType,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader Map<String, String> headers) {
        
        logger.info("Received enhanced fraud system call request - tenant: {}, paymentType: {}", tenantId, paymentType);
        
        try {
            // Validate tenant access
            if (!enhancedDownstreamRoutingService.validateTenantAccess(tenantId, "fraud", "/fraud", paymentType)) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("error", "Access denied");
                resp.put("message", "Tenant does not have access to fraud system");
                resp.put("tenantId", tenantId);
                resp.put("paymentType", paymentType);
                return ResponseEntity.status(403).body(resp);
            }
            
            // Call fraud system
            ResponseEntity<Map<String, Object>> response = enhancedDownstreamRoutingService.callFraudSystem(
                tenantId, paymentType, requestBody, Map.class, headers);
            
            logger.info("Enhanced fraud system call successful - tenant: {}, paymentType: {}", tenantId, paymentType);
            return response;
            
        } catch (Exception e) {
            logger.error("Enhanced fraud system call failed - tenant: {}, paymentType: {}", tenantId, paymentType, e);
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("error", "Enhanced fraud system call failed");
            resp.put("message", e.getMessage());
            resp.put("tenantId", tenantId);
            resp.put("paymentType", paymentType);
            return ResponseEntity.status(500).body(resp);
        }
    }
    
    /**
     * Call clearing system with multi-level configuration
     */
    @PostMapping("/clearing/{tenantId}")
    @PreAuthorize("hasAuthority('enhanced:downstream:clearing:call')")
    public ResponseEntity<Map<String, Object>> callClearingSystem(
            @PathVariable String tenantId,
            @RequestParam(required = false) String paymentType,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader Map<String, String> headers) {
        
        logger.info("Received enhanced clearing system call request - tenant: {}, paymentType: {}", tenantId, paymentType);
        
        try {
            // Validate tenant access
            if (!enhancedDownstreamRoutingService.validateTenantAccess(tenantId, "clearing", "/clearing", paymentType)) {
                java.util.Map<String, Object> resp = new java.util.HashMap<>();
                resp.put("error", "Access denied");
                resp.put("message", "Tenant does not have access to clearing system");
                resp.put("tenantId", tenantId);
                resp.put("paymentType", paymentType);
                return ResponseEntity.status(403).body(resp);
            }
            
            // Call clearing system
            ResponseEntity<Map<String, Object>> response = enhancedDownstreamRoutingService.callClearingSystem(
                tenantId, paymentType, requestBody, Map.class, headers);
            
            logger.info("Enhanced clearing system call successful - tenant: {}, paymentType: {}", tenantId, paymentType);
            return response;
            
        } catch (Exception e) {
            logger.error("Enhanced clearing system call failed - tenant: {}, paymentType: {}", tenantId, paymentType, e);
            java.util.Map<String, Object> resp2 = new java.util.HashMap<>();
            resp2.put("error", "Enhanced clearing system call failed");
            resp2.put("message", e.getMessage());
            resp2.put("tenantId", tenantId);
            resp2.put("paymentType", paymentType);
            return ResponseEntity.status(500).body(resp2);
        }
    }
    
    /**
     * Call banking system with multi-level configuration
     */
    @PostMapping("/banking/{tenantId}")
    @PreAuthorize("hasAuthority('enhanced:downstream:banking:call')")
    public ResponseEntity<Map<String, Object>> callBankingSystem(
            @PathVariable String tenantId,
            @RequestParam(required = false) String paymentType,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader Map<String, String> headers) {
        
        logger.info("Received enhanced banking system call request - tenant: {}, paymentType: {}", tenantId, paymentType);
        
        try {
            // Validate tenant access
            if (!enhancedDownstreamRoutingService.validateTenantAccess(tenantId, "banking", "/banking", paymentType)) {
                java.util.Map<String, Object> resp3 = new java.util.HashMap<>();
                resp3.put("error", "Access denied");
                resp3.put("message", "Tenant does not have access to banking system");
                resp3.put("tenantId", tenantId);
                resp3.put("paymentType", paymentType);
                return ResponseEntity.status(403).body(resp3);
            }
            
            // Call banking system
            ResponseEntity<Map<String, Object>> response = enhancedDownstreamRoutingService.callBankingSystem(
                tenantId, paymentType, requestBody, Map.class, headers);
            
            logger.info("Enhanced banking system call successful - tenant: {}, paymentType: {}", tenantId, paymentType);
            return response;
            
        } catch (Exception e) {
            logger.error("Enhanced banking system call failed - tenant: {}, paymentType: {}", tenantId, paymentType, e);
            java.util.Map<String, Object> resp4 = new java.util.HashMap<>();
            resp4.put("error", "Enhanced banking system call failed");
            resp4.put("message", e.getMessage());
            resp4.put("tenantId", tenantId);
            resp4.put("paymentType", paymentType);
            return ResponseEntity.status(500).body(resp4);
        }
    }
    
    /**
     * Call external service with automatic service type detection
     */
    @PostMapping("/auto/{tenantId}")
    @PreAuthorize("hasAuthority('enhanced:downstream:auto:call')")
    public ResponseEntity<Map<String, Object>> callExternalServiceAuto(
            @PathVariable String tenantId,
            @RequestParam(required = false) String paymentType,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader Map<String, String> headers) {
        
        logger.info("Received enhanced auto-routing call request - tenant: {}, paymentType: {}", tenantId, paymentType);
        
        try {
            // Call external service with auto-routing
            ResponseEntity<Map<String, Object>> response = enhancedDownstreamRoutingService.callExternalServiceAuto(
                tenantId, paymentType, requestBody, Map.class, headers);
            
            logger.info("Enhanced auto-routing call successful - tenant: {}, paymentType: {}", tenantId, paymentType);
            return response;
            
        } catch (Exception e) {
            logger.error("Enhanced auto-routing call failed - tenant: {}, paymentType: {}", tenantId, paymentType, e);
            java.util.Map<String, Object> resp5 = new java.util.HashMap<>();
            resp5.put("error", "Enhanced auto-routing call failed");
            resp5.put("message", e.getMessage());
            resp5.put("tenantId", tenantId);
            resp5.put("paymentType", paymentType);
            return ResponseEntity.status(500).body(resp5);
        }
    }
    
    /**
     * Get resolved configuration for debugging
     */
    @GetMapping("/config/{tenantId}/{serviceType}/{endpoint}")
    @PreAuthorize("hasAuthority('enhanced:downstream:config:read')")
    public ResponseEntity<MultiLevelAuthConfigurationService.ResolvedAuthConfiguration> getResolvedConfiguration(
            @PathVariable String tenantId,
            @PathVariable String serviceType,
            @PathVariable String endpoint,
            @RequestParam(required = false) String paymentType) {
        
        logger.info("Getting resolved configuration - tenant: {}, service: {}, endpoint: {}, paymentType: {}", 
                   tenantId, serviceType, endpoint, paymentType);
        
        try {
            MultiLevelAuthConfigurationService.ResolvedAuthConfiguration config = 
                multiLevelAuthConfigService.getResolvedConfiguration(tenantId, serviceType, endpoint, paymentType);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Failed to get resolved configuration - tenant: {}, service: {}, endpoint: {}", 
                        tenantId, serviceType, endpoint, e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get downstream service statistics
     */
    @GetMapping("/stats/{tenantId}/{serviceType}/{endpoint}")
    @PreAuthorize("hasAuthority('enhanced:downstream:stats:read')")
    public ResponseEntity<Map<String, Object>> getDownstreamStats(
            @PathVariable String tenantId,
            @PathVariable String serviceType,
            @PathVariable String endpoint,
            @RequestParam(required = false) String paymentType) {
        
        logger.info("Getting downstream statistics - tenant: {}, service: {}, endpoint: {}, paymentType: {}", 
                   tenantId, serviceType, endpoint, paymentType);
        
        try {
            Map<String, Object> stats = enhancedDownstreamRoutingService.getDownstreamStats(
                tenantId, serviceType, endpoint, paymentType);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Failed to get downstream statistics - tenant: {}, service: {}, endpoint: {}", 
                        tenantId, serviceType, endpoint, e);
            java.util.Map<String, Object> resp6 = new java.util.HashMap<>();
            resp6.put("error", "Failed to get statistics");
            resp6.put("message", e.getMessage());
            resp6.put("tenantId", tenantId);
            resp6.put("serviceType", serviceType);
            resp6.put("endpoint", endpoint);
            resp6.put("paymentType", paymentType);
            return ResponseEntity.status(500).body(resp6);
        }
    }
    
    /**
     * Validate tenant access to external service
     */
    @GetMapping("/validate/{tenantId}/{serviceType}/{endpoint}")
    @PreAuthorize("hasAuthority('enhanced:downstream:validate:read')")
    public ResponseEntity<Map<String, Object>> validateTenantAccess(
            @PathVariable String tenantId,
            @PathVariable String serviceType,
            @PathVariable String endpoint,
            @RequestParam(required = false) String paymentType) {
        
        logger.info("Validating tenant access - tenant: {}, service: {}, endpoint: {}, paymentType: {}", 
                   tenantId, serviceType, endpoint, paymentType);
        
        try {
            boolean hasAccess = enhancedDownstreamRoutingService.validateTenantAccess(
                tenantId, serviceType, endpoint, paymentType);
            
            Map<String, Object> result = new HashMap<>();
            result.put("tenantId", tenantId);
            result.put("serviceType", serviceType);
            result.put("endpoint", endpoint);
            result.put("paymentType", paymentType);
            result.put("hasAccess", hasAccess);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to validate tenant access - tenant: {}, service: {}, endpoint: {}", 
                        tenantId, serviceType, endpoint, e);
            java.util.Map<String, Object> resp7 = new java.util.HashMap<>();
            resp7.put("error", "Failed to validate access");
            resp7.put("message", e.getMessage());
            resp7.put("tenantId", tenantId);
            resp7.put("serviceType", serviceType);
            resp7.put("endpoint", endpoint);
            resp7.put("paymentType", paymentType);
            return ResponseEntity.status(500).body(resp7);
        }
    }
    
    /**
     * Health check for enhanced downstream routing
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "enhanced-downstream-routing");
        health.put("timestamp", System.currentTimeMillis());
        health.put("features", Map.of(
            "multiLevelConfig", true,
            "clearingSystemLevel", true,
            "tenantLevel", true,
            "paymentTypeLevel", true,
            "downstreamCallLevel", true
        ));
        
        return ResponseEntity.ok(health);
    }
}