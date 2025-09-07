package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.CoreBankingEndpointConfiguration;
import com.paymentengine.middleware.entity.CoreBankingConfiguration;
import com.paymentengine.middleware.repository.CoreBankingEndpointConfigurationRepository;
import com.paymentengine.middleware.repository.CoreBankingConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Dynamic Endpoint Resolution Service
 * 
 * This service provides dynamic endpoint resolution for core banking operations,
 * allowing runtime configuration of endpoints, headers, authentication, and other
 * endpoint-specific settings without code changes.
 */
@Service
public class DynamicEndpointResolutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamicEndpointResolutionService.class);
    
    @Autowired
    private CoreBankingEndpointConfigurationRepository endpointConfigRepository;
    
    @Autowired
    private CoreBankingConfigurationRepository coreBankingConfigRepository;
    
    /**
     * Resolve endpoint configuration for a specific operation
     */
    @Cacheable(value = "endpoint-configurations", key = "#tenantId + '_' + #endpointType")
    public Optional<EndpointResolution> resolveEndpoint(String tenantId, 
                                                       CoreBankingEndpointConfiguration.EndpointType endpointType) {
        logger.debug("Resolving endpoint for tenant: {} and endpoint type: {}", tenantId, endpointType);
        
        try {
            // Get core banking configuration for tenant
            Optional<CoreBankingConfiguration> coreBankingConfig = coreBankingConfigRepository.findByTenantId(tenantId);
            if (coreBankingConfig.isEmpty()) {
                logger.warn("No core banking configuration found for tenant: {}", tenantId);
                return Optional.empty();
            }
            
            // Get endpoint configuration
            Optional<CoreBankingEndpointConfiguration> endpointConfig = endpointConfigRepository
                .findActiveByCoreBankingConfigIdAndEndpointType(
                    coreBankingConfig.get().getId(), 
                    endpointType
                );
            
            if (endpointConfig.isEmpty()) {
                logger.warn("No endpoint configuration found for tenant: {} and endpoint type: {}", tenantId, endpointType);
                return Optional.empty();
            }
            
            // Build endpoint resolution
            EndpointResolution resolution = buildEndpointResolution(coreBankingConfig.get(), endpointConfig.get());
            
            logger.debug("Successfully resolved endpoint for tenant: {} and endpoint type: {}", tenantId, endpointType);
            return Optional.of(resolution);
            
        } catch (Exception e) {
            logger.error("Error resolving endpoint for tenant: {} and endpoint type: {}: {}", 
                        tenantId, endpointType, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Resolve endpoint configuration by name
     */
    @Cacheable(value = "endpoint-configurations-by-name", key = "#tenantId + '_' + #endpointName")
    public Optional<EndpointResolution> resolveEndpointByName(String tenantId, String endpointName) {
        logger.debug("Resolving endpoint by name for tenant: {} and endpoint name: {}", tenantId, endpointName);
        
        try {
            // Get core banking configuration for tenant
            Optional<CoreBankingConfiguration> coreBankingConfig = coreBankingConfigRepository.findByTenantId(tenantId);
            if (coreBankingConfig.isEmpty()) {
                logger.warn("No core banking configuration found for tenant: {}", tenantId);
                return Optional.empty();
            }
            
            // Get endpoint configuration by name
            Optional<CoreBankingEndpointConfiguration> endpointConfig = endpointConfigRepository
                .findActiveByCoreBankingConfigIdAndEndpointName(
                    coreBankingConfig.get().getId(), 
                    endpointName
                );
            
            if (endpointConfig.isEmpty()) {
                logger.warn("No endpoint configuration found for tenant: {} and endpoint name: {}", tenantId, endpointName);
                return Optional.empty();
            }
            
            // Build endpoint resolution
            EndpointResolution resolution = buildEndpointResolution(coreBankingConfig.get(), endpointConfig.get());
            
            logger.debug("Successfully resolved endpoint by name for tenant: {} and endpoint name: {}", tenantId, endpointName);
            return Optional.of(resolution);
            
        } catch (Exception e) {
            logger.error("Error resolving endpoint by name for tenant: {} and endpoint name: {}: {}", 
                        tenantId, endpointName, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get all available endpoints for a tenant
     */
    public java.util.List<EndpointResolution> getAllEndpoints(String tenantId) {
        logger.debug("Getting all endpoints for tenant: {}", tenantId);
        
        try {
            // Get core banking configuration for tenant
            Optional<CoreBankingConfiguration> coreBankingConfig = coreBankingConfigRepository.findByTenantId(tenantId);
            if (coreBankingConfig.isEmpty()) {
                logger.warn("No core banking configuration found for tenant: {}", tenantId);
                return java.util.Collections.emptyList();
            }
            
            // Get all active endpoint configurations
            java.util.List<CoreBankingEndpointConfiguration> endpointConfigs = endpointConfigRepository
                .findActiveByCoreBankingConfigId(coreBankingConfig.get().getId());
            
            // Build endpoint resolutions
            return endpointConfigs.stream()
                .map(config -> buildEndpointResolution(coreBankingConfig.get(), config))
                .collect(java.util.stream.Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Error getting all endpoints for tenant: {}: {}", tenantId, e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    /**
     * Check if endpoint is available for a tenant
     */
    public boolean isEndpointAvailable(String tenantId, CoreBankingEndpointConfiguration.EndpointType endpointType) {
        return resolveEndpoint(tenantId, endpointType).isPresent();
    }
    
    /**
     * Get endpoint URL for a specific operation
     */
    public Optional<String> getEndpointUrl(String tenantId, CoreBankingEndpointConfiguration.EndpointType endpointType) {
        return resolveEndpoint(tenantId, endpointType)
            .map(EndpointResolution::getFullUrl);
    }
    
    /**
     * Get HTTP method for a specific operation
     */
    public Optional<String> getHttpMethod(String tenantId, CoreBankingEndpointConfiguration.EndpointType endpointType) {
        return resolveEndpoint(tenantId, endpointType)
            .map(EndpointResolution::getHttpMethod);
    }
    
    /**
     * Get request headers for a specific operation
     */
    public Map<String, String> getRequestHeaders(String tenantId, CoreBankingEndpointConfiguration.EndpointType endpointType) {
        return resolveEndpoint(tenantId, endpointType)
            .map(EndpointResolution::getRequestHeaders)
            .orElse(java.util.Collections.emptyMap());
    }
    
    /**
     * Get query parameters for a specific operation
     */
    public Map<String, String> getQueryParameters(String tenantId, CoreBankingEndpointConfiguration.EndpointType endpointType) {
        return resolveEndpoint(tenantId, endpointType)
            .map(EndpointResolution::getQueryParameters)
            .orElse(java.util.Collections.emptyMap());
    }
    
    /**
     * Get authentication configuration for a specific operation
     */
    public Map<String, Object> getAuthenticationConfig(String tenantId, CoreBankingEndpointConfiguration.EndpointType endpointType) {
        return resolveEndpoint(tenantId, endpointType)
            .map(EndpointResolution::getAuthenticationConfig)
            .orElse(java.util.Collections.emptyMap());
    }
    
    /**
     * Get timeout configuration for a specific operation
     */
    public int getTimeout(String tenantId, CoreBankingEndpointConfiguration.EndpointType endpointType) {
        return resolveEndpoint(tenantId, endpointType)
            .map(EndpointResolution::getTimeoutMs)
            .orElse(30000); // Default timeout
    }
    
    /**
     * Get retry attempts for a specific operation
     */
    public int getRetryAttempts(String tenantId, CoreBankingEndpointConfiguration.EndpointType endpointType) {
        return resolveEndpoint(tenantId, endpointType)
            .map(EndpointResolution::getRetryAttempts)
            .orElse(3); // Default retry attempts
    }
    
    /**
     * Build endpoint resolution from configurations
     */
    private EndpointResolution buildEndpointResolution(CoreBankingConfiguration coreBankingConfig, 
                                                      CoreBankingEndpointConfiguration endpointConfig) {
        EndpointResolution resolution = new EndpointResolution();
        
        // Set basic endpoint information
        resolution.setEndpointName(endpointConfig.getEndpointName());
        resolution.setEndpointType(endpointConfig.getEndpointType());
        resolution.setHttpMethod(endpointConfig.getHttpMethod());
        resolution.setEndpointPath(endpointConfig.getEndpointPath());
        
        // Build full URL
        String baseUrl = endpointConfig.getBaseUrlOverride() != null ? 
            endpointConfig.getBaseUrlOverride() : 
            coreBankingConfig.getBaseUrl();
        
        if (baseUrl != null) {
            String fullUrl = baseUrl.endsWith("/") ? 
                baseUrl + endpointConfig.getEndpointPath() : 
                baseUrl + "/" + endpointConfig.getEndpointPath();
            resolution.setFullUrl(fullUrl);
        } else {
            resolution.setFullUrl(endpointConfig.getEndpointPath());
        }
        
        // Set configuration details
        resolution.setRequestHeaders(endpointConfig.getRequestHeaders() != null ? 
            endpointConfig.getRequestHeaders() : java.util.Collections.emptyMap());
        resolution.setQueryParameters(endpointConfig.getQueryParameters() != null ? 
            endpointConfig.getQueryParameters() : java.util.Collections.emptyMap());
        resolution.setAuthenticationConfig(endpointConfig.getAuthenticationConfig() != null ? 
            endpointConfig.getAuthenticationConfig() : java.util.Collections.emptyMap());
        resolution.setTimeoutMs(endpointConfig.getTimeoutMs() != null ? 
            endpointConfig.getTimeoutMs() : coreBankingConfig.getTimeoutMs());
        resolution.setRetryAttempts(endpointConfig.getRetryAttempts() != null ? 
            endpointConfig.getRetryAttempts() : coreBankingConfig.getRetryAttempts());
        
        // Set additional configurations
        resolution.setCircuitBreakerConfig(endpointConfig.getCircuitBreakerConfig());
        resolution.setRateLimitingConfig(endpointConfig.getRateLimitingConfig());
        resolution.setRequestTransformationConfig(endpointConfig.getRequestTransformationConfig());
        resolution.setResponseTransformationConfig(endpointConfig.getResponseTransformationConfig());
        resolution.setValidationRules(endpointConfig.getValidationRules());
        resolution.setErrorHandlingConfig(endpointConfig.getErrorHandlingConfig());
        
        // Set metadata
        resolution.setPriority(endpointConfig.getPriority());
        resolution.setDescription(endpointConfig.getDescription());
        
        return resolution;
    }
    
    /**
     * Endpoint Resolution DTO
     */
    public static class EndpointResolution {
        private String endpointName;
        private CoreBankingEndpointConfiguration.EndpointType endpointType;
        private String httpMethod;
        private String endpointPath;
        private String fullUrl;
        private Map<String, String> requestHeaders;
        private Map<String, String> queryParameters;
        private Map<String, Object> authenticationConfig;
        private Integer timeoutMs;
        private Integer retryAttempts;
        private Map<String, Object> circuitBreakerConfig;
        private Map<String, Object> rateLimitingConfig;
        private Map<String, Object> requestTransformationConfig;
        private Map<String, Object> responseTransformationConfig;
        private Map<String, Object> validationRules;
        private Map<String, Object> errorHandlingConfig;
        private Integer priority;
        private String description;
        
        // Getters and Setters
        public String getEndpointName() {
            return endpointName;
        }
        
        public void setEndpointName(String endpointName) {
            this.endpointName = endpointName;
        }
        
        public CoreBankingEndpointConfiguration.EndpointType getEndpointType() {
            return endpointType;
        }
        
        public void setEndpointType(CoreBankingEndpointConfiguration.EndpointType endpointType) {
            this.endpointType = endpointType;
        }
        
        public String getHttpMethod() {
            return httpMethod;
        }
        
        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }
        
        public String getEndpointPath() {
            return endpointPath;
        }
        
        public void setEndpointPath(String endpointPath) {
            this.endpointPath = endpointPath;
        }
        
        public String getFullUrl() {
            return fullUrl;
        }
        
        public void setFullUrl(String fullUrl) {
            this.fullUrl = fullUrl;
        }
        
        public Map<String, String> getRequestHeaders() {
            return requestHeaders;
        }
        
        public void setRequestHeaders(Map<String, String> requestHeaders) {
            this.requestHeaders = requestHeaders;
        }
        
        public Map<String, String> getQueryParameters() {
            return queryParameters;
        }
        
        public void setQueryParameters(Map<String, String> queryParameters) {
            this.queryParameters = queryParameters;
        }
        
        public Map<String, Object> getAuthenticationConfig() {
            return authenticationConfig;
        }
        
        public void setAuthenticationConfig(Map<String, Object> authenticationConfig) {
            this.authenticationConfig = authenticationConfig;
        }
        
        public Integer getTimeoutMs() {
            return timeoutMs;
        }
        
        public void setTimeoutMs(Integer timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
        
        public Integer getRetryAttempts() {
            return retryAttempts;
        }
        
        public void setRetryAttempts(Integer retryAttempts) {
            this.retryAttempts = retryAttempts;
        }
        
        public Map<String, Object> getCircuitBreakerConfig() {
            return circuitBreakerConfig;
        }
        
        public void setCircuitBreakerConfig(Map<String, Object> circuitBreakerConfig) {
            this.circuitBreakerConfig = circuitBreakerConfig;
        }
        
        public Map<String, Object> getRateLimitingConfig() {
            return rateLimitingConfig;
        }
        
        public void setRateLimitingConfig(Map<String, Object> rateLimitingConfig) {
            this.rateLimitingConfig = rateLimitingConfig;
        }
        
        public Map<String, Object> getRequestTransformationConfig() {
            return requestTransformationConfig;
        }
        
        public void setRequestTransformationConfig(Map<String, Object> requestTransformationConfig) {
            this.requestTransformationConfig = requestTransformationConfig;
        }
        
        public Map<String, Object> getResponseTransformationConfig() {
            return responseTransformationConfig;
        }
        
        public void setResponseTransformationConfig(Map<String, Object> responseTransformationConfig) {
            this.responseTransformationConfig = responseTransformationConfig;
        }
        
        public Map<String, Object> getValidationRules() {
            return validationRules;
        }
        
        public void setValidationRules(Map<String, Object> validationRules) {
            this.validationRules = validationRules;
        }
        
        public Map<String, Object> getErrorHandlingConfig() {
            return errorHandlingConfig;
        }
        
        public void setErrorHandlingConfig(Map<String, Object> errorHandlingConfig) {
            this.errorHandlingConfig = errorHandlingConfig;
        }
        
        public Integer getPriority() {
            return priority;
        }
        
        public void setPriority(Integer priority) {
            this.priority = priority;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return "EndpointResolution{" +
                    "endpointName='" + endpointName + '\'' +
                    ", endpointType=" + endpointType +
                    ", httpMethod='" + httpMethod + '\'' +
                    ", fullUrl='" + fullUrl + '\'' +
                    ", timeoutMs=" + timeoutMs +
                    ", retryAttempts=" + retryAttempts +
                    '}';
        }
    }
}