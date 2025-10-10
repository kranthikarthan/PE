package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.dto.TenantAuthConfigurationRequest;
import com.paymentengine.paymentprocessing.dto.TenantAuthConfigurationResponse;
import com.paymentengine.paymentprocessing.entity.TenantAuthConfiguration;
import com.paymentengine.paymentprocessing.repository.TenantAuthConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing tenant authentication configuration
 */
@Service
@Transactional
public class TenantAuthConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantAuthConfigurationService.class);
    
    @Autowired
    private TenantAuthConfigurationRepository repository;
    
    /**
     * Create or update tenant authentication configuration
     */
    public TenantAuthConfigurationResponse createOrUpdateConfiguration(TenantAuthConfigurationRequest request) {
        logger.info("Creating or updating auth configuration for tenant: {}", request.getTenantId());
        
        // Check if configuration already exists
        Optional<TenantAuthConfiguration> existingConfig = repository.findByTenantIdAndAuthMethod(
            request.getTenantId(), 
            request.getAuthMethod()
        );
        
        TenantAuthConfiguration config;
        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            updateConfiguration(config, request);
            logger.info("Updated existing auth configuration for tenant: {}", request.getTenantId());
        } else {
            config = createNewConfiguration(request);
            logger.info("Created new auth configuration for tenant: {}", request.getTenantId());
        }
        
        config = repository.save(config);
        return mapToResponse(config);
    }
    
    /**
     * Get active authentication configuration for tenant
     */
    @Transactional(readOnly = true)
    public Optional<TenantAuthConfigurationResponse> getActiveConfiguration(String tenantId) {
        logger.debug("Getting active auth configuration for tenant: {}", tenantId);
        
        return repository.findActiveByTenantId(tenantId)
            .map(this::mapToResponse);
    }
    
    /**
     * Get all authentication configurations for tenant
     */
    @Transactional(readOnly = true)
    public List<TenantAuthConfigurationResponse> getConfigurations(String tenantId) {
        logger.debug("Getting all auth configurations for tenant: {}", tenantId);
        
        return repository.findByTenantId(tenantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get authentication configuration by ID
     */
    @Transactional(readOnly = true)
    public Optional<TenantAuthConfigurationResponse> getConfigurationById(UUID id) {
        logger.debug("Getting auth configuration by ID: {}", id);
        
        return repository.findById(id)
            .map(this::mapToResponse);
    }
    
    /**
     * Activate authentication configuration
     */
    public TenantAuthConfigurationResponse activateConfiguration(UUID id, String updatedBy) {
        logger.info("Activating auth configuration: {}", id);
        
        TenantAuthConfiguration config = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Auth configuration not found: " + id));
        
        // Deactivate other configurations for the same tenant
        repository.findByTenantId(config.getTenantId())
            .forEach(c -> {
                if (!c.getId().equals(id)) {
                    c.setIsActive(false);
                    c.setUpdatedBy(updatedBy);
                    c.setUpdatedAt(LocalDateTime.now());
                    repository.save(c);
                }
            });
        
        // Activate the specified configuration
        config.setIsActive(true);
        config.setUpdatedBy(updatedBy);
        config.setUpdatedAt(LocalDateTime.now());
        
        config = repository.save(config);
        return mapToResponse(config);
    }
    
    /**
     * Deactivate authentication configuration
     */
    public TenantAuthConfigurationResponse deactivateConfiguration(UUID id, String updatedBy) {
        logger.info("Deactivating auth configuration: {}", id);
        
        TenantAuthConfiguration config = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Auth configuration not found: " + id));
        
        config.setIsActive(false);
        config.setUpdatedBy(updatedBy);
        config.setUpdatedAt(LocalDateTime.now());
        
        config = repository.save(config);
        return mapToResponse(config);
    }
    
    /**
     * Delete authentication configuration
     */
    public void deleteConfiguration(UUID id) {
        logger.info("Deleting auth configuration: {}", id);
        
        if (!repository.existsById(id)) {
            throw new RuntimeException("Auth configuration not found: " + id);
        }
        
        repository.deleteById(id);
    }
    
    /**
     * Get all active authentication configurations
     */
    @Transactional(readOnly = true)
    public List<TenantAuthConfigurationResponse> getAllActiveConfigurations() {
        logger.debug("Getting all active auth configurations");
        
        return repository.findAllActive()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get configurations by authentication method
     */
    @Transactional(readOnly = true)
    public List<TenantAuthConfigurationResponse> getConfigurationsByAuthMethod(TenantAuthConfiguration.AuthMethod authMethod) {
        logger.debug("Getting auth configurations by method: {}", authMethod);
        
        return repository.findByAuthMethod(authMethod)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get active configurations by authentication method
     */
    @Transactional(readOnly = true)
    public List<TenantAuthConfigurationResponse> getActiveConfigurationsByAuthMethod(TenantAuthConfiguration.AuthMethod authMethod) {
        logger.debug("Getting active auth configurations by method: {}", authMethod);
        
        return repository.findActiveByAuthMethod(authMethod)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get configurations that include client headers
     */
    @Transactional(readOnly = true)
    public List<TenantAuthConfigurationResponse> getConfigurationsWithClientHeaders() {
        logger.debug("Getting auth configurations with client headers");
        
        return repository.findWithClientHeaders()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if tenant has active authentication configuration
     */
    @Transactional(readOnly = true)
    public boolean hasActiveConfiguration(String tenantId) {
        return repository.existsActiveByTenantId(tenantId);
    }
    
    /**
     * Get authentication configuration for outgoing calls
     */
    @Transactional(readOnly = true)
    public Optional<TenantAuthConfiguration> getConfigurationForOutgoingCalls(String tenantId) {
        return repository.findActiveByTenantId(tenantId);
    }
    
    // Private helper methods
    
    private TenantAuthConfiguration createNewConfiguration(TenantAuthConfigurationRequest request) {
        TenantAuthConfiguration config = new TenantAuthConfiguration();
        config.setTenantId(request.getTenantId());
        config.setAuthMethod(request.getAuthMethod());
        config.setClientId(request.getClientId());
        config.setClientSecret(request.getClientSecret());
        config.setClientIdHeaderName(request.getClientIdHeaderName());
        config.setClientSecretHeaderName(request.getClientSecretHeaderName());
        config.setAuthHeaderName(request.getAuthHeaderName());
        config.setAuthHeaderPrefix(request.getAuthHeaderPrefix());
        config.setTokenEndpoint(request.getTokenEndpoint());
        config.setPublicKeyEndpoint(request.getPublicKeyEndpoint());
        config.setJwsPublicKey(request.getJwsPublicKey());
        config.setJwsAlgorithm(request.getJwsAlgorithm());
        config.setJwsIssuer(request.getJwsIssuer());
        config.setIncludeClientHeaders(request.getIncludeClientHeaders());
        config.setDescription(request.getDescription());
        config.setCreatedBy(request.getCreatedBy());
        config.setIsActive(true);
        
        return config;
    }
    
    private void updateConfiguration(TenantAuthConfiguration config, TenantAuthConfigurationRequest request) {
        config.setAuthMethod(request.getAuthMethod());
        config.setClientId(request.getClientId());
        config.setClientSecret(request.getClientSecret());
        config.setClientIdHeaderName(request.getClientIdHeaderName());
        config.setClientSecretHeaderName(request.getClientSecretHeaderName());
        config.setAuthHeaderName(request.getAuthHeaderName());
        config.setAuthHeaderPrefix(request.getAuthHeaderPrefix());
        config.setTokenEndpoint(request.getTokenEndpoint());
        config.setPublicKeyEndpoint(request.getPublicKeyEndpoint());
        config.setJwsPublicKey(request.getJwsPublicKey());
        config.setJwsAlgorithm(request.getJwsAlgorithm());
        config.setJwsIssuer(request.getJwsIssuer());
        config.setIncludeClientHeaders(request.getIncludeClientHeaders());
        config.setDescription(request.getDescription());
        config.setUpdatedBy(request.getUpdatedBy());
        config.setUpdatedAt(LocalDateTime.now());
    }
    
    private TenantAuthConfigurationResponse mapToResponse(TenantAuthConfiguration config) {
        TenantAuthConfigurationResponse response = new TenantAuthConfigurationResponse();
        response.setId(config.getId());
        response.setTenantId(config.getTenantId());
        response.setAuthMethod(config.getAuthMethod());
        response.setClientId(config.getClientId());
        response.setClientIdHeaderName(config.getClientIdHeaderName());
        response.setClientSecretHeaderName(config.getClientSecretHeaderName());
        response.setAuthHeaderName(config.getAuthHeaderName());
        response.setAuthHeaderPrefix(config.getAuthHeaderPrefix());
        response.setTokenEndpoint(config.getTokenEndpoint());
        response.setPublicKeyEndpoint(config.getPublicKeyEndpoint());
        response.setJwsPublicKey(config.getJwsPublicKey());
        response.setJwsAlgorithm(config.getJwsAlgorithm());
        response.setJwsIssuer(config.getJwsIssuer());
        response.setIsActive(config.getIsActive());
        response.setIncludeClientHeaders(config.getIncludeClientHeaders());
        response.setDescription(config.getDescription());
        response.setCreatedBy(config.getCreatedBy());
        response.setUpdatedBy(config.getUpdatedBy());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        
        return response;
    }
}