package com.paymentengine.config.controller;

import com.paymentengine.config.dto.TenantRequest;
import com.paymentengine.config.dto.TenantConfigurationRequest;
import com.paymentengine.config.dto.FeatureFlagRequest;
import com.paymentengine.config.entity.Tenant;
import com.paymentengine.config.entity.TenantConfiguration;
import com.paymentengine.config.entity.FeatureFlag;
import com.paymentengine.config.entity.ConfigurationHistory;
import com.paymentengine.config.service.TenantService;
import com.paymentengine.config.service.TenantConfigurationService;
import com.paymentengine.config.service.FeatureFlagService;
import com.paymentengine.config.repository.ConfigurationHistoryRepository;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/config")
@CrossOrigin(origins = "*")
public class ConfigurationController {
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private TenantConfigurationService tenantConfigurationService;
    
    @Autowired
    private FeatureFlagService featureFlagService;
    
    @Autowired
    private ConfigurationHistoryRepository configurationHistoryRepository;
    
    // Tenant Management
    @GetMapping("/tenants")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.tenants.list", description = "Time taken to list tenants")
    public ResponseEntity<List<Tenant>> getTenants() {
        try {
            List<Tenant> tenants = tenantService.findAll();
            return ResponseEntity.ok(tenants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/tenants/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.tenants.get", description = "Time taken to get tenant")
    public ResponseEntity<Tenant> getTenant(@PathVariable UUID id) {
        try {
            return tenantService.findById(id)
                    .map(tenant -> ResponseEntity.ok(tenant))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/tenants")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.tenants.create", description = "Time taken to create tenant")
    public ResponseEntity<Tenant> createTenant(@Valid @RequestBody TenantRequest request) {
        try {
            Tenant tenant = tenantService.createTenant(
                    request.getName(), request.getCode(), request.getContactEmail(),
                    request.getContactPhone(), request.getAddress());
            return ResponseEntity.ok(tenant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/tenants/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.tenants.update", description = "Time taken to update tenant")
    public ResponseEntity<Tenant> updateTenant(@PathVariable UUID id, @Valid @RequestBody TenantRequest request) {
        try {
            Tenant tenant = tenantService.updateTenant(id, request.getName(), 
                    request.getContactEmail(), request.getContactPhone(), request.getAddress());
            return ResponseEntity.ok(tenant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/tenants/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.tenants.activate", description = "Time taken to activate tenant")
    public ResponseEntity<Void> activateTenant(@PathVariable UUID id) {
        try {
            tenantService.activateTenant(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/tenants/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.tenants.deactivate", description = "Time taken to deactivate tenant")
    public ResponseEntity<Void> deactivateTenant(@PathVariable UUID id) {
        try {
            tenantService.deactivateTenant(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Tenant Configuration Management
    @GetMapping("/tenants/{tenantId}/configurations")
    @PreAuthorize("hasRole('ADMIN') or @tenantService.isCurrentUserTenant(#tenantId)")
    @Timed(value = "config.tenant_configs.list", description = "Time taken to list tenant configurations")
    public ResponseEntity<List<TenantConfiguration>> getTenantConfigurations(@PathVariable UUID tenantId) {
        try {
            List<TenantConfiguration> configurations = tenantConfigurationService.findByTenantId(tenantId);
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/tenants/{tenantId}/configurations/{configKey}")
    @PreAuthorize("hasRole('ADMIN') or @tenantService.isCurrentUserTenant(#tenantId)")
    @Timed(value = "config.tenant_configs.get", description = "Time taken to get tenant configuration")
    public ResponseEntity<TenantConfiguration> getTenantConfiguration(@PathVariable UUID tenantId, 
                                                                     @PathVariable String configKey) {
        try {
            return tenantConfigurationService.findByTenantIdAndConfigKey(tenantId, configKey)
                    .map(config -> ResponseEntity.ok(config))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/tenants/{tenantId}/configurations")
    @PreAuthorize("hasRole('ADMIN') or @tenantService.isCurrentUserTenant(#tenantId)")
    @Timed(value = "config.tenant_configs.create", description = "Time taken to create tenant configuration")
    public ResponseEntity<TenantConfiguration> createTenantConfiguration(@PathVariable UUID tenantId, 
                                                                        @Valid @RequestBody TenantConfigurationRequest request) {
        try {
            TenantConfiguration configuration = tenantConfigurationService.setConfiguration(
                    tenantId, request.getConfigKey(), request.getConfigValue(), 
                    request.getConfigType(), request.getIsEncrypted());
            return ResponseEntity.ok(configuration);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/tenants/{tenantId}/configurations/{configKey}")
    @PreAuthorize("hasRole('ADMIN') or @tenantService.isCurrentUserTenant(#tenantId)")
    @Timed(value = "config.tenant_configs.update", description = "Time taken to update tenant configuration")
    public ResponseEntity<TenantConfiguration> updateTenantConfiguration(@PathVariable UUID tenantId, 
                                                                        @PathVariable String configKey, 
                                                                        @Valid @RequestBody TenantConfigurationRequest request) {
        try {
            TenantConfiguration configuration = tenantConfigurationService.setConfiguration(
                    tenantId, configKey, request.getConfigValue(), 
                    request.getConfigType(), request.getIsEncrypted());
            return ResponseEntity.ok(configuration);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/tenants/{tenantId}/configurations/{configKey}")
    @PreAuthorize("hasRole('ADMIN') or @tenantService.isCurrentUserTenant(#tenantId)")
    @Timed(value = "config.tenant_configs.delete", description = "Time taken to delete tenant configuration")
    public ResponseEntity<Void> deleteTenantConfiguration(@PathVariable UUID tenantId, 
                                                         @PathVariable String configKey) {
        try {
            tenantConfigurationService.deleteConfiguration(tenantId, configKey);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Feature Flag Management
    @GetMapping("/feature-flags")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.feature_flags.list", description = "Time taken to list feature flags")
    public ResponseEntity<List<FeatureFlag>> getFeatureFlags() {
        try {
            List<FeatureFlag> featureFlags = featureFlagService.findByEnvironment(FeatureFlag.Environment.PRODUCTION);
            return ResponseEntity.ok(featureFlags);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/feature-flags/{flagName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.feature_flags.get", description = "Time taken to get feature flag")
    public ResponseEntity<FeatureFlag> getFeatureFlag(@PathVariable String flagName) {
        try {
            return featureFlagService.findByFlagName(flagName)
                    .map(flag -> ResponseEntity.ok(flag))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/feature-flags")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.feature_flags.create", description = "Time taken to create feature flag")
    public ResponseEntity<FeatureFlag> createFeatureFlag(@Valid @RequestBody FeatureFlagRequest request) {
        try {
            FeatureFlag featureFlag = featureFlagService.createFeatureFlag(
                    request.getFlagName(), request.getFlagDescription(), request.getFlagValue(),
                    request.getTenantId(), request.getEnvironment(), request.getRolloutPercentage(),
                    request.getTargetUsers());
            return ResponseEntity.ok(featureFlag);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/feature-flags/{flagName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.feature_flags.update", description = "Time taken to update feature flag")
    public ResponseEntity<FeatureFlag> updateFeatureFlag(@PathVariable String flagName, 
                                                        @Valid @RequestBody FeatureFlagRequest request) {
        try {
            FeatureFlag featureFlag = featureFlagService.updateFeatureFlag(
                    flagName, request.getFlagDescription(), request.getFlagValue(),
                    request.getRolloutPercentage(), request.getTargetUsers());
            return ResponseEntity.ok(featureFlag);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/feature-flags/{flagName}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.feature_flags.toggle", description = "Time taken to toggle feature flag")
    public ResponseEntity<Void> toggleFeatureFlag(@PathVariable String flagName) {
        try {
            featureFlagService.toggleFeatureFlag(flagName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/feature-flags/{flagName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.feature_flags.delete", description = "Time taken to delete feature flag")
    public ResponseEntity<Void> deleteFeatureFlag(@PathVariable String flagName) {
        try {
            featureFlagService.deleteFeatureFlag(flagName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Configuration History
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.history.list", description = "Time taken to list configuration history")
    public ResponseEntity<List<ConfigurationHistory>> getConfigurationHistory() {
        try {
            List<ConfigurationHistory> history = configurationHistoryRepository.findAll();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/history/{configType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "config.history.get_by_type", description = "Time taken to get configuration history by type")
    public ResponseEntity<List<ConfigurationHistory>> getConfigurationHistoryByType(
            @PathVariable ConfigurationHistory.ConfigType configType) {
        try {
            List<ConfigurationHistory> history = configurationHistoryRepository.findByConfigType(configType);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/health")
    @Timed(value = "config.health", description = "Time taken for config service health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Configuration Service is healthy");
    }
}