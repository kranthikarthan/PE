package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.entity.TenantConfiguration;
import com.paymentengine.paymentprocessing.service.TenantCloningService;
import com.paymentengine.paymentprocessing.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for tenant management operations
 * Provides endpoints for tenant cloning, versioning, and migration
 */
@RestController
@RequestMapping("/api/tenant-management")
@CrossOrigin(origins = "*")
public class TenantManagementController {

    private static final Logger logger = LoggerFactory.getLogger(TenantManagementController.class);

    @Autowired
    private TenantCloningService tenantCloningService;

    // ============================================================================
    // TENANT CLONING ENDPOINTS
    // ============================================================================

    /**
     * Clone a tenant configuration
     */
    @PostMapping("/clone")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('tenant:manage')")
    public ResponseEntity<TenantCloneResponse> cloneTenant(@Valid @RequestBody TenantCloneRequest request) {
        logger.info("Received tenant clone request: {} -> {}", request.getSourceTenantId(), request.getTargetTenantId());
        
        try {
            TenantCloneResponse response = tenantCloningService.cloneTenant(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error cloning tenant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TenantCloneResponse.error("Failed to clone tenant: " + e.getMessage()));
        }
    }

    /**
     * Clone a tenant to a different environment
     */
    @PostMapping("/clone-to-environment")
    public ResponseEntity<TenantCloneResponse> cloneTenantToEnvironment(@Valid @RequestBody TenantCloneRequest request) {
        logger.info("Received tenant clone to environment request: {} -> {} ({})", 
            request.getSourceTenantId(), request.getTargetTenantId(), request.getTargetEnvironment());
        
        try {
            TenantCloneResponse response = tenantCloningService.cloneTenantToEnvironment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error cloning tenant to environment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TenantCloneResponse.error("Failed to clone tenant to environment: " + e.getMessage()));
        }
    }

    /**
     * Clone a specific version of a tenant
     */
    @PostMapping("/clone-version")
    public ResponseEntity<TenantCloneResponse> cloneTenantVersion(@Valid @RequestBody TenantCloneRequest request) {
        logger.info("Received tenant version clone request: {}:{} -> {}", 
            request.getSourceTenantId(), request.getSourceVersion(), request.getTargetTenantId());
        
        try {
            TenantCloneResponse response = tenantCloningService.cloneTenantVersion(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error cloning tenant version", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TenantCloneResponse.error("Failed to clone tenant version: " + e.getMessage()));
        }
    }

    /**
     * Rollback tenant to a previous version
     */
    @PostMapping("/rollback/{tenantId}/{version}")
    public ResponseEntity<TenantCloneResponse> rollbackTenant(
            @PathVariable String tenantId, 
            @PathVariable String version) {
        logger.info("Received tenant rollback request: {} to version {}", tenantId, version);
        
        try {
            TenantCloneResponse response = tenantCloningService.rollbackTenant(tenantId, version);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error rolling back tenant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TenantCloneResponse.error("Failed to rollback tenant: " + e.getMessage()));
        }
    }

    // ============================================================================
    // TENANT EXPORT/IMPORT ENDPOINTS
    // ============================================================================

    /**
     * Export tenant configuration
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('tenant:export') or hasAuthority('tenant:manage')")
    public ResponseEntity<TenantExportResponse> exportTenant(@Valid @RequestBody TenantExportRequest request) {
        logger.info("Received tenant export request: {} (version: {})", request.getTenantId(), request.getVersion());
        
        try {
            TenantExportResponse response = tenantCloningService.exportTenant(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error exporting tenant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TenantExportResponse.error("Failed to export tenant: " + e.getMessage()));
        }
    }

    /**
     * Import tenant configuration
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('tenant:import') or hasAuthority('tenant:manage')")
    public ResponseEntity<TenantImportResponse> importTenant(@Valid @RequestBody TenantImportRequest request) {
        logger.info("Received tenant import request: {} -> {}", request.getImportData(), request.getTargetTenantId());
        
        try {
            TenantImportResponse response = tenantCloningService.importTenant(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error importing tenant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TenantImportResponse.error("Failed to import tenant: " + e.getMessage()));
        }
    }

    /**
     * Import tenant configuration from file
     */
    @PostMapping("/import-file")
    public ResponseEntity<TenantImportResponse> importTenantFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetTenantId") String targetTenantId,
            @RequestParam(value = "targetEnvironment", required = false) String targetEnvironment,
            @RequestParam(value = "importedBy", required = false) String importedBy) {
        
        logger.info("Received tenant import from file request: {} -> {}", file.getOriginalFilename(), targetTenantId);
        
        try {
            String importData = new String(file.getBytes());
            String format = getFileFormat(file.getOriginalFilename());
            
            TenantImportRequest request = new TenantImportRequest();
            request.setImportData(importData);
            request.setImportFormat(format);
            request.setTargetTenantId(targetTenantId);
            request.setImportedBy(importedBy);
            
            if (targetEnvironment != null) {
                request.setTargetEnvironment(TenantConfiguration.Environment.valueOf(targetEnvironment));
            }
            
            TenantImportResponse response = tenantCloningService.importTenant(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error importing tenant from file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TenantImportResponse.error("Failed to import tenant from file: " + e.getMessage()));
        }
    }

    // ============================================================================
    // TENANT INFORMATION ENDPOINTS
    // ============================================================================

    /**
     * Get all available tenants
     */
    @GetMapping("/tenants")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('tenant:read') or hasAuthority('tenant:manage')")
    public ResponseEntity<List<String>> getAvailableTenants() {
        logger.info("Received request for available tenants");
        
        try {
            List<String> tenants = tenantCloningService.getAvailableTenants();
            return ResponseEntity.ok(tenants);
        } catch (Exception e) {
            logger.error("Error getting available tenants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all versions of a tenant
     */
    @GetMapping("/tenants/{tenantId}/versions")
    public ResponseEntity<List<String>> getTenantVersions(@PathVariable String tenantId) {
        logger.info("Received request for tenant versions: {}", tenantId);
        
        try {
            List<String> versions = tenantCloningService.getTenantVersions(tenantId);
            return ResponseEntity.ok(versions);
        } catch (Exception e) {
            logger.error("Error getting tenant versions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get tenant configuration history
     */
    @GetMapping("/tenants/{tenantId}/history")
    public ResponseEntity<List<TenantConfiguration>> getTenantHistory(@PathVariable String tenantId) {
        logger.info("Received request for tenant history: {}", tenantId);
        
        try {
            List<TenantConfiguration> history = tenantCloningService.getTenantHistory(tenantId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error getting tenant history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Compare two tenant configurations
     */
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareTenantConfigurations(
            @RequestParam String tenantId1,
            @RequestParam String version1,
            @RequestParam String tenantId2,
            @RequestParam String version2) {
        
        logger.info("Received request to compare configurations: {}:{} vs {}:{}", tenantId1, version1, tenantId2, version2);
        
        try {
            Map<String, Object> comparison = tenantCloningService.compareTenantConfigurations(tenantId1, version1, tenantId2, version2);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            logger.error("Error comparing tenant configurations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Validate tenant configuration
     */
    @GetMapping("/validate/{tenantId}/{version}")
    public ResponseEntity<Map<String, Object>> validateTenantConfiguration(
            @PathVariable String tenantId,
            @PathVariable String version) {
        
        logger.info("Received request to validate configuration: {}:{}", tenantId, version);
        
        try {
            Map<String, Object> validation = tenantCloningService.validateTenantConfiguration(tenantId, version);
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            logger.error("Error validating tenant configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get cloning statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCloningStatistics() {
        logger.info("Received request for cloning statistics");
        
        try {
            Map<String, Object> statistics = tenantCloningService.getCloningStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting cloning statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============================================================================
    // TEMPLATE MANAGEMENT ENDPOINTS
    // ============================================================================

    /**
     * Create template from tenant configuration
     */
    @PostMapping("/templates")
    public ResponseEntity<TenantCloneResponse> createTemplate(
            @RequestParam String tenantId,
            @RequestParam String version,
            @RequestParam String templateName) {
        
        logger.info("Received request to create template: {}:{} -> {}", tenantId, version, templateName);
        
        try {
            TenantCloneResponse response = tenantCloningService.createTemplate(tenantId, version, templateName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TenantCloneResponse.error("Failed to create template: " + e.getMessage()));
        }
    }

    /**
     * Apply template to tenant
     */
    @PostMapping("/templates/{templateName}/apply")
    public ResponseEntity<TenantCloneResponse> applyTemplate(
            @PathVariable String templateName,
            @RequestParam String tenantId,
            @RequestBody(required = false) Map<String, String> overrides) {
        
        logger.info("Received request to apply template: {} -> {}", templateName, tenantId);
        
        try {
            TenantCloneResponse response = tenantCloningService.applyTemplate(tenantId, templateName, overrides);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error applying template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TenantCloneResponse.error("Failed to apply template: " + e.getMessage()));
        }
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private String getFileFormat(String fileName) {
        if (fileName == null) return "JSON";
        
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "json":
                return "JSON";
            case "yaml":
            case "yml":
                return "YAML";
            case "xml":
                return "XML";
            default:
                return "JSON";
        }
    }
}