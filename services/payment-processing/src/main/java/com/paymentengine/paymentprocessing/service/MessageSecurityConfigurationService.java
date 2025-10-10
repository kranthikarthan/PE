package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.entity.MessageSecurityConfiguration;
import com.paymentengine.paymentprocessing.repository.MessageSecurityConfigurationRepository;
import com.paymentengine.paymentprocessing.dto.MessageSecurityConfigurationRequest;
import com.paymentengine.paymentprocessing.dto.MessageSecurityConfigurationResponse;
import com.paymentengine.paymentprocessing.dto.ResolvedMessageSecurityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing message security configuration (encryption and digital signatures)
 */
@Service
@Transactional
public class MessageSecurityConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageSecurityConfigurationService.class);
    
    @Autowired
    private MessageSecurityConfigurationRepository repository;
    
    /**
     * Create or update message security configuration
     */
    public MessageSecurityConfigurationResponse createOrUpdateConfiguration(
            MessageSecurityConfigurationRequest request) {
        
        logger.info("Creating or updating message security configuration for tenant: {}, level: {}", 
                request.getTenantId(), request.getConfigurationLevel());
        
        try {
            MessageSecurityConfiguration config;
            
            if (request.getId() != null) {
                // Update existing configuration
                config = repository.findById(request.getId())
                        .orElseThrow(() -> new RuntimeException("Configuration not found: " + request.getId()));
                
                updateConfigurationFromRequest(config, request);
                config.setUpdatedAt(LocalDateTime.now());
                config.setUpdatedBy(request.getCreatedBy());
                
            } else {
                // Create new configuration
                config = createConfigurationFromRequest(request);
            }
            
            MessageSecurityConfiguration savedConfig = repository.save(config);
            
            logger.info("Successfully created/updated message security configuration: {}", savedConfig.getId());
            return mapToResponse(savedConfig);
            
        } catch (Exception e) {
            logger.error("Failed to create/update message security configuration", e);
            throw new RuntimeException("Failed to create/update configuration: " + e.getMessage());
        }
    }
    
    /**
     * Get message security configuration by ID
     */
    public Optional<MessageSecurityConfigurationResponse> getConfigurationById(UUID id) {
        logger.info("Getting message security configuration by ID: {}", id);
        
        try {
            return repository.findById(id)
                    .map(this::mapToResponse);
        } catch (Exception e) {
            logger.error("Failed to get message security configuration by ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Get all configurations for a tenant
     */
    public List<MessageSecurityConfigurationResponse> getConfigurationsByTenant(String tenantId) {
        logger.info("Getting message security configurations for tenant: {}", tenantId);
        
        try {
            return repository.findByTenantIdAndIsActiveTrue(tenantId)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get message security configurations for tenant: {}", tenantId, e);
            return List.of();
        }
    }
    
    /**
     * Get configurations by tenant and configuration level
     */
    public List<MessageSecurityConfigurationResponse> getConfigurationsByTenantAndLevel(
            String tenantId, MessageSecurityConfiguration.ConfigurationLevel level) {
        
        logger.info("Getting message security configurations for tenant: {}, level: {}", tenantId, level);
        
        try {
            return repository.findByTenantIdAndConfigurationLevelAndIsActiveTrue(tenantId, level)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get message security configurations for tenant: {}, level: {}", tenantId, level, e);
            return List.of();
        }
    }
    
    /**
     * Resolve message security configuration for a specific context
     * Returns the most specific configuration based on hierarchy
     */
    public Optional<ResolvedMessageSecurityConfiguration> resolveConfiguration(
            String tenantId,
            String serviceType,
            String endpoint,
            String paymentType,
            MessageSecurityConfiguration.MessageDirection direction) {
        
        logger.info("Resolving message security configuration for tenant: {}, serviceType: {}, endpoint: {}, paymentType: {}, direction: {}", 
                tenantId, serviceType, endpoint, paymentType, direction);
        
        try {
            List<MessageSecurityConfiguration> configurations = repository.findMostSpecificConfiguration(
                    tenantId, serviceType, endpoint, paymentType, direction);
            
            if (configurations.isEmpty()) {
                logger.warn("No message security configuration found for tenant: {}", tenantId);
                return Optional.empty();
            }
            
            // Get the most specific configuration (first in the list due to ordering)
            MessageSecurityConfiguration config = configurations.get(0);
            
            ResolvedMessageSecurityConfiguration resolved = new ResolvedMessageSecurityConfiguration();
            resolved.setTenantId(tenantId);
            resolved.setServiceType(serviceType);
            resolved.setEndpoint(endpoint);
            resolved.setPaymentType(paymentType);
            resolved.setDirection(direction);
            resolved.setConfigurationLevel(config.getConfigurationLevel());
            resolved.setConfigurationId(config.getId());
            
            // Encryption configuration
            resolved.setEncryptionEnabled(config.getEncryptionEnabled());
            resolved.setEncryptionAlgorithm(config.getEncryptionAlgorithm());
            resolved.setEncryptionKeyId(config.getEncryptionKeyId());
            resolved.setEncryptionKeyVersion(config.getEncryptionKeyVersion());
            resolved.setEncryptionProvider(config.getEncryptionProvider());
            
            // Signature configuration
            resolved.setSignatureEnabled(config.getSignatureEnabled());
            resolved.setSignatureAlgorithm(config.getSignatureAlgorithm());
            resolved.setSignatureKeyId(config.getSignatureKeyId());
            resolved.setSignatureKeyVersion(config.getSignatureKeyVersion());
            resolved.setSignatureProvider(config.getSignatureProvider());
            
            // Message format configuration
            resolved.setMessageFormat(config.getMessageFormat());
            resolved.setContentType(config.getContentType());
            resolved.setCharset(config.getCharset());
            
            // Security headers configuration
            resolved.setSecurityHeadersEnabled(config.getSecurityHeadersEnabled());
            resolved.setSecurityHeadersConfig(config.getSecurityHeadersConfig());
            
            resolved.setResolvedAt(LocalDateTime.now());
            
            logger.info("Successfully resolved message security configuration for tenant: {}", tenantId);
            return Optional.of(resolved);
            
        } catch (Exception e) {
            logger.error("Failed to resolve message security configuration for tenant: {}", tenantId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Activate configuration
     */
    public boolean activateConfiguration(UUID id, String updatedBy) {
        logger.info("Activating message security configuration: {}", id);
        
        try {
            MessageSecurityConfiguration config = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Configuration not found: " + id));
            
            config.setIsActive(true);
            config.setUpdatedAt(LocalDateTime.now());
            config.setUpdatedBy(updatedBy);
            
            repository.save(config);
            
            logger.info("Successfully activated message security configuration: {}", id);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to activate message security configuration: {}", id, e);
            return false;
        }
    }
    
    /**
     * Deactivate configuration
     */
    public boolean deactivateConfiguration(UUID id, String updatedBy) {
        logger.info("Deactivating message security configuration: {}", id);
        
        try {
            MessageSecurityConfiguration config = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Configuration not found: " + id));
            
            config.setIsActive(false);
            config.setUpdatedAt(LocalDateTime.now());
            config.setUpdatedBy(updatedBy);
            
            repository.save(config);
            
            logger.info("Successfully deactivated message security configuration: {}", id);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to deactivate message security configuration: {}", id, e);
            return false;
        }
    }
    
    /**
     * Delete configuration
     */
    public boolean deleteConfiguration(UUID id) {
        logger.info("Deleting message security configuration: {}", id);
        
        try {
            repository.deleteById(id);
            
            logger.info("Successfully deleted message security configuration: {}", id);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to delete message security configuration: {}", id, e);
            return false;
        }
    }
    
    /**
     * Get configurations with encryption enabled
     */
    public List<MessageSecurityConfigurationResponse> getConfigurationsWithEncryptionEnabled() {
        logger.info("Getting configurations with encryption enabled");
        
        try {
            return repository.findConfigurationsWithEncryptionEnabled()
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get configurations with encryption enabled", e);
            return List.of();
        }
    }
    
    /**
     * Get configurations with signature enabled
     */
    public List<MessageSecurityConfigurationResponse> getConfigurationsWithSignatureEnabled() {
        logger.info("Getting configurations with signature enabled");
        
        try {
            return repository.findConfigurationsWithSignatureEnabled()
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get configurations with signature enabled", e);
            return List.of();
        }
    }
    
    /**
     * Validate configuration
     */
    public boolean validateConfiguration(MessageSecurityConfigurationRequest request) {
        logger.info("Validating message security configuration for tenant: {}", request.getTenantId());
        
        try {
            // Validate encryption configuration
            if (request.getEncryptionEnabled() && request.getEncryptionAlgorithm() == null) {
                logger.warn("Encryption enabled but no algorithm specified");
                return false;
            }
            
            if (request.getEncryptionEnabled() && request.getEncryptionKeyId() == null) {
                logger.warn("Encryption enabled but no key ID specified");
                return false;
            }
            
            // Validate signature configuration
            if (request.getSignatureEnabled() && request.getSignatureAlgorithm() == null) {
                logger.warn("Signature enabled but no algorithm specified");
                return false;
            }
            
            if (request.getSignatureEnabled() && request.getSignatureKeyId() == null) {
                logger.warn("Signature enabled but no key ID specified");
                return false;
            }
            
            // Validate message format
            if (request.getMessageFormat() == null) {
                logger.warn("Message format not specified");
                return false;
            }
            
            logger.info("Message security configuration validation passed for tenant: {}", request.getTenantId());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to validate message security configuration", e);
            return false;
        }
    }
    
    /**
     * Get configuration statistics
     */
    public Map<String, Object> getConfigurationStatistics() {
        logger.info("Getting message security configuration statistics");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalConfigurations", repository.count());
            stats.put("activeConfigurations", repository.countByIsActiveTrue());
            stats.put("configurationsWithEncryption", repository.countByEncryptionEnabledTrueAndIsActiveTrue());
            stats.put("configurationsWithSignature", repository.countBySignatureEnabledTrueAndIsActiveTrue());
            
            // Count by configuration level
            Map<String, Long> levelStats = new HashMap<>();
            for (MessageSecurityConfiguration.ConfigurationLevel level : 
                    MessageSecurityConfiguration.ConfigurationLevel.values()) {
                levelStats.put(level.name(), repository.countByConfigurationLevelAndIsActiveTrue(level));
            }
            stats.put("configurationsByLevel", levelStats);
            
            // Count by encryption algorithm
            Map<String, Long> encryptionStats = new HashMap<>();
            for (MessageSecurityConfiguration.EncryptionAlgorithm algorithm : 
                    MessageSecurityConfiguration.EncryptionAlgorithm.values()) {
                encryptionStats.put(algorithm.name(), repository.countByEncryptionAlgorithmAndIsActiveTrue(algorithm));
            }
            stats.put("configurationsByEncryptionAlgorithm", encryptionStats);
            
            // Count by signature algorithm
            Map<String, Long> signatureStats = new HashMap<>();
            for (MessageSecurityConfiguration.SignatureAlgorithm algorithm : 
                    MessageSecurityConfiguration.SignatureAlgorithm.values()) {
                signatureStats.put(algorithm.name(), repository.countBySignatureAlgorithmAndIsActiveTrue(algorithm));
            }
            stats.put("configurationsBySignatureAlgorithm", signatureStats);
            
            logger.info("Successfully retrieved message security configuration statistics");
            return stats;
            
        } catch (Exception e) {
            logger.error("Failed to get message security configuration statistics", e);
            return Map.of("error", "Failed to get statistics: " + e.getMessage());
        }
    }
    
    // Helper methods
    private MessageSecurityConfiguration createConfigurationFromRequest(MessageSecurityConfigurationRequest request) {
        MessageSecurityConfiguration config = new MessageSecurityConfiguration();
        config.setTenantId(request.getTenantId());
        config.setConfigurationLevel(request.getConfigurationLevel());
        config.setPaymentType(request.getPaymentType());
        config.setServiceType(request.getServiceType());
        config.setEndpoint(request.getEndpoint());
        config.setDirection(request.getDirection());
        config.setEncryptionEnabled(request.getEncryptionEnabled());
        config.setEncryptionAlgorithm(request.getEncryptionAlgorithm());
        config.setEncryptionKeyId(request.getEncryptionKeyId());
        config.setEncryptionKeyVersion(request.getEncryptionKeyVersion());
        config.setEncryptionProvider(request.getEncryptionProvider());
        config.setSignatureEnabled(request.getSignatureEnabled());
        config.setSignatureAlgorithm(request.getSignatureAlgorithm());
        config.setSignatureKeyId(request.getSignatureKeyId());
        config.setSignatureKeyVersion(request.getSignatureKeyVersion());
        config.setSignatureProvider(request.getSignatureProvider());
        config.setMessageFormat(request.getMessageFormat());
        config.setContentType(request.getContentType());
        config.setCharset(request.getCharset());
        config.setSecurityHeadersEnabled(request.getSecurityHeadersEnabled());
        config.setSecurityHeadersConfig(request.getSecurityHeadersConfig());
        config.setCreatedBy(request.getCreatedBy());
        config.setMetadata(request.getMetadata());
        
        return config;
    }
    
    private void updateConfigurationFromRequest(MessageSecurityConfiguration config, MessageSecurityConfigurationRequest request) {
        config.setTenantId(request.getTenantId());
        config.setConfigurationLevel(request.getConfigurationLevel());
        config.setPaymentType(request.getPaymentType());
        config.setServiceType(request.getServiceType());
        config.setEndpoint(request.getEndpoint());
        config.setDirection(request.getDirection());
        config.setEncryptionEnabled(request.getEncryptionEnabled());
        config.setEncryptionAlgorithm(request.getEncryptionAlgorithm());
        config.setEncryptionKeyId(request.getEncryptionKeyId());
        config.setEncryptionKeyVersion(request.getEncryptionKeyVersion());
        config.setEncryptionProvider(request.getEncryptionProvider());
        config.setSignatureEnabled(request.getSignatureEnabled());
        config.setSignatureAlgorithm(request.getSignatureAlgorithm());
        config.setSignatureKeyId(request.getSignatureKeyId());
        config.setSignatureKeyVersion(request.getSignatureKeyVersion());
        config.setSignatureProvider(request.getSignatureProvider());
        config.setMessageFormat(request.getMessageFormat());
        config.setContentType(request.getContentType());
        config.setCharset(request.getCharset());
        config.setSecurityHeadersEnabled(request.getSecurityHeadersEnabled());
        config.setSecurityHeadersConfig(request.getSecurityHeadersConfig());
        config.setMetadata(request.getMetadata());
    }
    
    private MessageSecurityConfigurationResponse mapToResponse(MessageSecurityConfiguration config) {
        MessageSecurityConfigurationResponse response = new MessageSecurityConfigurationResponse();
        response.setId(config.getId());
        response.setTenantId(config.getTenantId());
        response.setConfigurationLevel(config.getConfigurationLevel());
        response.setPaymentType(config.getPaymentType());
        response.setServiceType(config.getServiceType());
        response.setEndpoint(config.getEndpoint());
        response.setDirection(config.getDirection());
        response.setEncryptionEnabled(config.getEncryptionEnabled());
        response.setEncryptionAlgorithm(config.getEncryptionAlgorithm());
        response.setEncryptionKeyId(config.getEncryptionKeyId());
        response.setEncryptionKeyVersion(config.getEncryptionKeyVersion());
        response.setEncryptionProvider(config.getEncryptionProvider());
        response.setSignatureEnabled(config.getSignatureEnabled());
        response.setSignatureAlgorithm(config.getSignatureAlgorithm());
        response.setSignatureKeyId(config.getSignatureKeyId());
        response.setSignatureKeyVersion(config.getSignatureKeyVersion());
        response.setSignatureProvider(config.getSignatureProvider());
        response.setMessageFormat(config.getMessageFormat());
        response.setContentType(config.getContentType());
        response.setCharset(config.getCharset());
        response.setSecurityHeadersEnabled(config.getSecurityHeadersEnabled());
        response.setSecurityHeadersConfig(config.getSecurityHeadersConfig());
        response.setIsActive(config.getIsActive());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        response.setCreatedBy(config.getCreatedBy());
        response.setUpdatedBy(config.getUpdatedBy());
        response.setMetadata(config.getMetadata());
        
        return response;
    }
}