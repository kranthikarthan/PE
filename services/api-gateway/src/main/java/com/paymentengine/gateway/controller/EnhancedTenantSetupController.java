package com.paymentengine.gateway.controller;

import com.paymentengine.gateway.dto.tenant.EnhancedTenantSetupRequest;
import com.paymentengine.gateway.dto.tenant.EnhancedTenantSetupResponse;
import com.paymentengine.gateway.dto.tenant.ConfigurationDeploymentResult;
import com.paymentengine.gateway.service.EnhancedTenantSetupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Tenant Setup Controller for multi-level authentication configuration
 * Provides guided setup flow for tenant configuration with multi-level auth
 */
@RestController
@RequestMapping("/api/v1/tenant-setup")
@CrossOrigin(origins = "*")
public class EnhancedTenantSetupController {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedTenantSetupController.class);
    
    @Autowired
    private EnhancedTenantSetupService enhancedTenantSetupService;
    
    /**
     * Create tenant with multi-level authentication configuration
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('tenant:create')")
    public ResponseEntity<EnhancedTenantSetupResponse> createTenantWithMultiLevelAuth(
            @Valid @RequestBody EnhancedTenantSetupRequest request) {
        
        logger.info("Creating tenant with multi-level authentication configuration: {}", request.getBasicInfo().getTenantId());
        
        try {
            EnhancedTenantSetupResponse response = enhancedTenantSetupService.createTenantWithMultiLevelAuth(request);
            logger.info("Successfully created tenant with multi-level auth: {}", request.getBasicInfo().getTenantId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to create tenant with multi-level auth: {}", request.getBasicInfo().getTenantId(), e);
            return ResponseEntity.status(500).body(EnhancedTenantSetupResponse.builder()
                .success(false)
                .message("Failed to create tenant: " + e.getMessage())
                .build());
        }
    }
    
    /**
     * Validate tenant configuration before deployment
     */
    @PostMapping("/validate")
    @PreAuthorize("hasAuthority('tenant:validate')")
    public ResponseEntity<Map<String, Object>> validateTenantConfiguration(
            @Valid @RequestBody EnhancedTenantSetupRequest request) {
        
        logger.info("Validating tenant configuration: {}", request.getBasicInfo().getTenantId());
        
        try {
            Map<String, Object> validationResult = enhancedTenantSetupService.validateTenantConfiguration(request);
            return ResponseEntity.ok(validationResult);
        } catch (Exception e) {
            logger.error("Failed to validate tenant configuration: {}", request.getBasicInfo().getTenantId(), e);
            return ResponseEntity.status(500).body(Map.of(
                "valid", false,
                "errors", List.of("Validation failed: " + e.getMessage())
            ));
        }
    }
    
    /**
     * Deploy tenant configurations
     */
    @PostMapping("/deploy")
    @PreAuthorize("hasAuthority('tenant:deploy')")
    public ResponseEntity<ConfigurationDeploymentResult> deployTenantConfigurations(
            @Valid @RequestBody EnhancedTenantSetupRequest request) {
        
        logger.info("Deploying tenant configurations: {}", request.getBasicInfo().getTenantId());
        
        try {
            ConfigurationDeploymentResult result = enhancedTenantSetupService.deployTenantConfigurations(request);
            logger.info("Successfully deployed tenant configurations: {}", request.getBasicInfo().getTenantId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to deploy tenant configurations: {}", request.getBasicInfo().getTenantId(), e);
            return ResponseEntity.status(500).body(ConfigurationDeploymentResult.builder()
                .success(false)
                .message("Deployment failed: " + e.getMessage())
                .build());
        }
    }
    
    /**
     * Test tenant configuration
     */
    @PostMapping("/test")
    @PreAuthorize("hasAuthority('tenant:test')")
    public ResponseEntity<Map<String, Object>> testTenantConfiguration(
            @RequestParam String tenantId,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String endpoint,
            @RequestParam(required = false) String paymentType) {
        
        logger.info("Testing tenant configuration: {} - {}:{}:{}", tenantId, serviceType, endpoint, paymentType);
        
        try {
            Map<String, Object> testResult = enhancedTenantSetupService.testTenantConfiguration(
                tenantId, serviceType, endpoint, paymentType);
            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            logger.error("Failed to test tenant configuration: {}", tenantId, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Test failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get configuration hierarchy for tenant
     */
    @GetMapping("/hierarchy/{tenantId}")
    @PreAuthorize("hasAuthority('tenant:read')")
    public ResponseEntity<Map<String, Object>> getConfigurationHierarchy(
            @PathVariable String tenantId,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String endpoint,
            @RequestParam(required = false) String paymentType) {
        
        logger.info("Getting configuration hierarchy for tenant: {} - {}:{}:{}", tenantId, serviceType, endpoint, paymentType);
        
        try {
            Map<String, Object> hierarchy = enhancedTenantSetupService.getConfigurationHierarchy(
                tenantId, serviceType, endpoint, paymentType);
            return ResponseEntity.ok(hierarchy);
        } catch (Exception e) {
            logger.error("Failed to get configuration hierarchy for tenant: {}", tenantId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get hierarchy: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get available configuration templates
     */
    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('tenant:read')")
    public ResponseEntity<List<Map<String, Object>>> getConfigurationTemplates() {
        logger.info("Getting configuration templates");
        
        try {
            List<Map<String, Object>> templates = enhancedTenantSetupService.getConfigurationTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            logger.error("Failed to get configuration templates", e);
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    /**
     * Clone tenant configuration
     */
    @PostMapping("/clone")
    @PreAuthorize("hasAuthority('tenant:clone')")
    public ResponseEntity<EnhancedTenantSetupResponse> cloneTenantConfiguration(
            @RequestParam String sourceTenantId,
            @RequestParam String targetTenantId,
            @RequestParam(required = false) String targetTenantName) {
        
        logger.info("Cloning tenant configuration from {} to {}", sourceTenantId, targetTenantId);
        
        try {
            EnhancedTenantSetupResponse response = enhancedTenantSetupService.cloneTenantConfiguration(
                sourceTenantId, targetTenantId, targetTenantName);
            logger.info("Successfully cloned tenant configuration from {} to {}", sourceTenantId, targetTenantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to clone tenant configuration from {} to {}", sourceTenantId, targetTenantId, e);
            return ResponseEntity.status(500).body(EnhancedTenantSetupResponse.builder()
                .success(false)
                .message("Clone failed: " + e.getMessage())
                .build());
        }
    }
    
    /**
     * Export tenant configuration
     */
    @GetMapping("/export/{tenantId}")
    @PreAuthorize("hasAuthority('tenant:export')")
    public ResponseEntity<Map<String, Object>> exportTenantConfiguration(@PathVariable String tenantId) {
        logger.info("Exporting tenant configuration: {}", tenantId);
        
        try {
            Map<String, Object> configuration = enhancedTenantSetupService.exportTenantConfiguration(tenantId);
            return ResponseEntity.ok(configuration);
        } catch (Exception e) {
            logger.error("Failed to export tenant configuration: {}", tenantId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Export failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Import tenant configuration
     */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('tenant:import')")
    public ResponseEntity<EnhancedTenantSetupResponse> importTenantConfiguration(
            @RequestBody Map<String, Object> configuration) {
        
        logger.info("Importing tenant configuration");
        
        try {
            EnhancedTenantSetupResponse response = enhancedTenantSetupService.importTenantConfiguration(configuration);
            logger.info("Successfully imported tenant configuration");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to import tenant configuration", e);
            return ResponseEntity.status(500).body(EnhancedTenantSetupResponse.builder()
                .success(false)
                .message("Import failed: " + e.getMessage())
                .build());
        }
    }
    
    /**
     * Get setup wizard progress
     */
    @GetMapping("/progress/{tenantId}")
    @PreAuthorize("hasAuthority('tenant:read')")
    public ResponseEntity<Map<String, Object>> getSetupProgress(@PathVariable String tenantId) {
        logger.info("Getting setup progress for tenant: {}", tenantId);
        
        try {
            Map<String, Object> progress = enhancedTenantSetupService.getSetupProgress(tenantId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            logger.error("Failed to get setup progress for tenant: {}", tenantId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get progress: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Update setup wizard progress
     */
    @PutMapping("/progress/{tenantId}")
    @PreAuthorize("hasAuthority('tenant:update')")
    public ResponseEntity<Map<String, Object>> updateSetupProgress(
            @PathVariable String tenantId,
            @RequestBody Map<String, Object> progressData) {
        
        logger.info("Updating setup progress for tenant: {}", tenantId);
        
        try {
            Map<String, Object> result = enhancedTenantSetupService.updateSetupProgress(tenantId, progressData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to update setup progress for tenant: {}", tenantId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update progress: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Health check for enhanced tenant setup
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "enhanced-tenant-setup",
            "timestamp", System.currentTimeMillis(),
            "features", Map.of(
                "multiLevelAuth", true,
                "guidedSetup", true,
                "configurationHierarchy", true,
                "templateSupport", true,
                "cloneSupport", true,
                "importExport", true
            )
        );
        
        return ResponseEntity.ok(health);
    }
}