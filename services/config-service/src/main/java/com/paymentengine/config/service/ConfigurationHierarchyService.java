package com.paymentengine.config.service;

import com.paymentengine.config.dto.ConfigurationHierarchyDTO;
import com.paymentengine.config.dto.ResolvedAuthConfigurationDTO;
import com.paymentengine.config.entity.Tenant;
import com.paymentengine.config.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing configuration hierarchy in Config Service
 * Integrates with Payment Processing Service for configuration precedence management
 */
@Service
@Transactional
public class ConfigurationHierarchyService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationHierarchyService.class);
    
    @Autowired
    private TenantRepository tenantRepository;
    
    // TODO: Add RestTemplate or WebClient to communicate with Payment Processing Service
    // @Autowired
    // private RestTemplate restTemplate;
    
    /**
     * Get configuration hierarchy for a tenant
     */
    public Optional<ConfigurationHierarchyDTO> getConfigurationHierarchy(String tenantId) {
        logger.info("Getting configuration hierarchy for tenant: {}", tenantId);
        
        try {
            // Validate tenant exists
            Optional<Tenant> tenant = tenantRepository.findByCode(tenantId);
            if (tenant.isEmpty()) {
                logger.warn("Tenant not found: {}", tenantId);
                return Optional.empty();
            }
            
            // TODO: Call Payment Processing Service to get configuration hierarchy
            // String url = "http://payment-processing-service/api/v1/multi-level-auth/hierarchy/" + tenantId;
            // ConfigurationHierarchyDTO hierarchy = restTemplate.getForObject(url, ConfigurationHierarchyDTO.class);
            
            // For now, create a placeholder hierarchy
            ConfigurationHierarchyDTO hierarchy = createPlaceholderHierarchy(tenantId, tenant.get().getName());
            
            logger.info("Successfully retrieved configuration hierarchy for tenant: {}", tenantId);
            return Optional.of(hierarchy);
            
        } catch (Exception e) {
            logger.error("Failed to get configuration hierarchy for tenant: {}", tenantId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Resolve configuration precedence for a specific context
     */
    public Optional<ResolvedAuthConfigurationDTO> resolveConfigurationPrecedence(
            String tenantId, 
            String serviceType, 
            String endpoint, 
            String paymentType) {
        
        logger.info("Resolving configuration precedence for tenant: {}, serviceType: {}, endpoint: {}, paymentType: {}", 
                tenantId, serviceType, endpoint, paymentType);
        
        try {
            // Validate tenant exists
            Optional<Tenant> tenant = tenantRepository.findByCode(tenantId);
            if (tenant.isEmpty()) {
                logger.warn("Tenant not found: {}", tenantId);
                return Optional.empty();
            }
            
            // TODO: Call Payment Processing Service to resolve configuration precedence
            // String url = String.format("http://payment-processing-service/api/v1/multi-level-auth/resolve/%s?serviceType=%s&endpoint=%s&paymentType=%s", 
            //         tenantId, serviceType, endpoint, paymentType);
            // ResolvedAuthConfigurationDTO resolved = restTemplate.getForObject(url, ResolvedAuthConfigurationDTO.class);
            
            // For now, create a placeholder resolved configuration
            ResolvedAuthConfigurationDTO resolved = createPlaceholderResolvedConfiguration(
                    tenantId, serviceType, endpoint, paymentType);
            
            logger.info("Successfully resolved configuration precedence for tenant: {}", tenantId);
            return Optional.of(resolved);
            
        } catch (Exception e) {
            logger.error("Failed to resolve configuration precedence for tenant: {}", tenantId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Validate configuration hierarchy
     */
    public boolean validateConfigurationHierarchy(String tenantId) {
        logger.info("Validating configuration hierarchy for tenant: {}", tenantId);
        
        try {
            // Validate tenant exists
            Optional<Tenant> tenant = tenantRepository.findByCode(tenantId);
            if (tenant.isEmpty()) {
                logger.warn("Tenant not found: {}", tenantId);
                return false;
            }
            
            // TODO: Call Payment Processing Service to validate configuration hierarchy
            // String url = "http://payment-processing-service/api/v1/multi-level-auth/validate-hierarchy/" + tenantId;
            // ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            // return response.getBody();
            
            // For now, return true
            logger.info("Successfully validated configuration hierarchy for tenant: {}", tenantId);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to validate configuration hierarchy for tenant: {}", tenantId, e);
            return false;
        }
    }
    
    /**
     * Get configuration precedence rules
     */
    public List<String> getConfigurationPrecedenceRules() {
        logger.info("Getting configuration precedence rules");
        
        List<String> rules = new ArrayList<>();
        rules.add("1. Downstream Call Level (Highest Priority) - Most specific configuration for individual service calls");
        rules.add("2. Payment Type Level - Configuration for specific payment types (SEPA, SWIFT, etc.)");
        rules.add("3. Tenant Level - Tenant-specific configuration");
        rules.add("4. Clearing System Level (Lowest Priority) - Global configuration for the entire clearing system");
        
        logger.info("Successfully retrieved {} configuration precedence rules", rules.size());
        return rules;
    }
    
    /**
     * Create placeholder hierarchy for testing
     */
    private ConfigurationHierarchyDTO createPlaceholderHierarchy(String tenantId, String tenantName) {
        ConfigurationHierarchyDTO hierarchy = new ConfigurationHierarchyDTO(tenantId, tenantName);
        hierarchy.setEnvironment("dev");
        hierarchy.setLastUpdated(LocalDateTime.now());
        
        List<ConfigurationHierarchyDTO.HierarchyLevelDTO> levels = new ArrayList<>();
        
        // Downstream Call Level (Priority 1)
        ConfigurationHierarchyDTO.HierarchyLevelDTO downstreamLevel = 
                new ConfigurationHierarchyDTO.HierarchyLevelDTO("downstream-call", 1, "Fraud Service", "JWT");
        downstreamLevel.setIsConfigured(false);
        downstreamLevel.setIsActive(false);
        levels.add(downstreamLevel);
        
        // Payment Type Level (Priority 2)
        ConfigurationHierarchyDTO.HierarchyLevelDTO paymentTypeLevel = 
                new ConfigurationHierarchyDTO.HierarchyLevelDTO("payment-type", 2, "SEPA Payments", "JWT");
        paymentTypeLevel.setIsConfigured(false);
        paymentTypeLevel.setIsActive(false);
        levels.add(paymentTypeLevel);
        
        // Tenant Level (Priority 3)
        ConfigurationHierarchyDTO.HierarchyLevelDTO tenantLevel = 
                new ConfigurationHierarchyDTO.HierarchyLevelDTO("tenant", 3, "Tenant Configuration", "JWT");
        tenantLevel.setIsConfigured(false);
        tenantLevel.setIsActive(false);
        levels.add(tenantLevel);
        
        // Clearing System Level (Priority 4)
        ConfigurationHierarchyDTO.HierarchyLevelDTO clearingSystemLevel = 
                new ConfigurationHierarchyDTO.HierarchyLevelDTO("clearing-system", 4, "Dev Environment", "JWT");
        clearingSystemLevel.setIsConfigured(true);
        clearingSystemLevel.setIsActive(true);
        levels.add(clearingSystemLevel);
        
        hierarchy.setHierarchyLevels(levels);
        return hierarchy;
    }
    
    /**
     * Create placeholder resolved configuration for testing
     */
    private ResolvedAuthConfigurationDTO createPlaceholderResolvedConfiguration(
            String tenantId, String serviceType, String endpoint, String paymentType) {
        
        ResolvedAuthConfigurationDTO resolved = new ResolvedAuthConfigurationDTO(tenantId, serviceType, endpoint, paymentType);
        resolved.setAuthMethod("JWT");
        resolved.setConfigurationLevel("clearing-system");
        resolved.setConfigurationId("clearing-system-dev");
        resolved.setIsActive(true);
        resolved.setResolvedAt(LocalDateTime.now());
        
        return resolved;
    }
}