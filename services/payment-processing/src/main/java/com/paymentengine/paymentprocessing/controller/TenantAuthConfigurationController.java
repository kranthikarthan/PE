package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.dto.TenantAuthConfigurationRequest;
import com.paymentengine.paymentprocessing.dto.TenantAuthConfigurationResponse;
import com.paymentengine.paymentprocessing.entity.TenantAuthConfiguration;
import com.paymentengine.paymentprocessing.service.TenantAuthConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Controller for managing tenant authentication configuration
 */
@RestController
@RequestMapping("/api/v1/tenant-auth-config")
@CrossOrigin(origins = "*")
public class TenantAuthConfigurationController {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantAuthConfigurationController.class);
    
    @Autowired
    private TenantAuthConfigurationService tenantAuthConfigurationService;
    
    /**
     * Create or update tenant authentication configuration
     */
    @PostMapping
    @PreAuthorize("hasAuthority('tenant:auth:write')")
    public ResponseEntity<TenantAuthConfigurationResponse> createOrUpdateConfiguration(
            @Valid @RequestBody TenantAuthConfigurationRequest request) {
        try {
            logger.info("Creating or updating auth configuration for tenant: {}", request.getTenantId());
            TenantAuthConfigurationResponse response = tenantAuthConfigurationService.createOrUpdateConfiguration(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating or updating auth configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get active authentication configuration for tenant
     */
    @GetMapping("/tenant/{tenantId}/active")
    @PreAuthorize("hasAuthority('tenant:auth:read')")
    public ResponseEntity<TenantAuthConfigurationResponse> getActiveConfiguration(
            @PathVariable String tenantId) {
        try {
            logger.debug("Getting active auth configuration for tenant: {}", tenantId);
            return tenantAuthConfigurationService.getActiveConfiguration(tenantId)
                .map(config -> ResponseEntity.ok(config))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting active auth configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all authentication configurations for tenant
     */
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAuthority('tenant:auth:read')")
    public ResponseEntity<List<TenantAuthConfigurationResponse>> getConfigurations(
            @PathVariable String tenantId) {
        try {
            logger.debug("Getting all auth configurations for tenant: {}", tenantId);
            List<TenantAuthConfigurationResponse> configurations = tenantAuthConfigurationService.getConfigurations(tenantId);
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            logger.error("Error getting auth configurations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get authentication configuration by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:auth:read')")
    public ResponseEntity<TenantAuthConfigurationResponse> getConfigurationById(
            @PathVariable UUID id) {
        try {
            logger.debug("Getting auth configuration by ID: {}", id);
            return tenantAuthConfigurationService.getConfigurationById(id)
                .map(config -> ResponseEntity.ok(config))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting auth configuration by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Activate authentication configuration
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('tenant:auth:write')")
    public ResponseEntity<TenantAuthConfigurationResponse> activateConfiguration(
            @PathVariable UUID id,
            @RequestParam String updatedBy) {
        try {
            logger.info("Activating auth configuration: {}", id);
            TenantAuthConfigurationResponse response = tenantAuthConfigurationService.activateConfiguration(id, updatedBy);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error activating auth configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Deactivate authentication configuration
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('tenant:auth:write')")
    public ResponseEntity<TenantAuthConfigurationResponse> deactivateConfiguration(
            @PathVariable UUID id,
            @RequestParam String updatedBy) {
        try {
            logger.info("Deactivating auth configuration: {}", id);
            TenantAuthConfigurationResponse response = tenantAuthConfigurationService.deactivateConfiguration(id, updatedBy);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deactivating auth configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete authentication configuration
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:auth:delete')")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable UUID id) {
        try {
            logger.info("Deleting auth configuration: {}", id);
            tenantAuthConfigurationService.deleteConfiguration(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting auth configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all active authentication configurations
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('tenant:auth:read')")
    public ResponseEntity<List<TenantAuthConfigurationResponse>> getAllActiveConfigurations() {
        try {
            logger.debug("Getting all active auth configurations");
            List<TenantAuthConfigurationResponse> configurations = tenantAuthConfigurationService.getAllActiveConfigurations();
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            logger.error("Error getting all active auth configurations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get configurations by authentication method
     */
    @GetMapping("/method/{authMethod}")
    @PreAuthorize("hasAuthority('tenant:auth:read')")
    public ResponseEntity<List<TenantAuthConfigurationResponse>> getConfigurationsByAuthMethod(
            @PathVariable TenantAuthConfiguration.AuthMethod authMethod) {
        try {
            logger.debug("Getting auth configurations by method: {}", authMethod);
            List<TenantAuthConfigurationResponse> configurations = tenantAuthConfigurationService.getConfigurationsByAuthMethod(authMethod);
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            logger.error("Error getting auth configurations by method: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get active configurations by authentication method
     */
    @GetMapping("/method/{authMethod}/active")
    @PreAuthorize("hasAuthority('tenant:auth:read')")
    public ResponseEntity<List<TenantAuthConfigurationResponse>> getActiveConfigurationsByAuthMethod(
            @PathVariable TenantAuthConfiguration.AuthMethod authMethod) {
        try {
            logger.debug("Getting active auth configurations by method: {}", authMethod);
            List<TenantAuthConfigurationResponse> configurations = tenantAuthConfigurationService.getActiveConfigurationsByAuthMethod(authMethod);
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            logger.error("Error getting active auth configurations by method: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get configurations that include client headers
     */
    @GetMapping("/with-client-headers")
    @PreAuthorize("hasAuthority('tenant:auth:read')")
    public ResponseEntity<List<TenantAuthConfigurationResponse>> getConfigurationsWithClientHeaders() {
        try {
            logger.debug("Getting auth configurations with client headers");
            List<TenantAuthConfigurationResponse> configurations = tenantAuthConfigurationService.getConfigurationsWithClientHeaders();
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            logger.error("Error getting auth configurations with client headers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Check if tenant has active authentication configuration
     */
    @GetMapping("/tenant/{tenantId}/has-active")
    @PreAuthorize("hasAuthority('tenant:auth:read')")
    public ResponseEntity<Boolean> hasActiveConfiguration(@PathVariable String tenantId) {
        try {
            logger.debug("Checking if tenant has active auth configuration: {}", tenantId);
            boolean hasActive = tenantAuthConfigurationService.hasActiveConfiguration(tenantId);
            return ResponseEntity.ok(hasActive);
        } catch (Exception e) {
            logger.error("Error checking active auth configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get available authentication methods
     */
    @GetMapping("/auth-methods")
    @PreAuthorize("hasAuthority('tenant:auth:read')")
    public ResponseEntity<TenantAuthConfiguration.AuthMethod[]> getAvailableAuthMethods() {
        try {
            logger.debug("Getting available authentication methods");
            TenantAuthConfiguration.AuthMethod[] methods = TenantAuthConfiguration.AuthMethod.values();
            return ResponseEntity.ok(methods);
        } catch (Exception e) {
            logger.error("Error getting available authentication methods: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}