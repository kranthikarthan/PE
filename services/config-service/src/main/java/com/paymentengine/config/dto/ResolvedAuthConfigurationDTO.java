package com.paymentengine.config.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for resolved authentication configuration
 */
public class ResolvedAuthConfigurationDTO {
    
    private String tenantId;
    private String serviceType;
    private String endpoint;
    private String paymentType;
    private String authMethod;
    private String configurationLevel; // downstream-call, payment-type, tenant, clearing-system
    private String configurationId;
    private Boolean isActive;
    private LocalDateTime resolvedAt;
    private Map<String, Object> authConfiguration;
    private Map<String, Object> clientHeaders;
    private Map<String, Object> metadata;
    
    // Constructors
    public ResolvedAuthConfigurationDTO() {}
    
    public ResolvedAuthConfigurationDTO(String tenantId, String serviceType, String endpoint, String paymentType) {
        this.tenantId = tenantId;
        this.serviceType = serviceType;
        this.endpoint = endpoint;
        this.paymentType = paymentType;
    }
    
    // Getters and Setters
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public String getAuthMethod() {
        return authMethod;
    }
    
    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }
    
    public String getConfigurationLevel() {
        return configurationLevel;
    }
    
    public void setConfigurationLevel(String configurationLevel) {
        this.configurationLevel = configurationLevel;
    }
    
    public String getConfigurationId() {
        return configurationId;
    }
    
    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public Map<String, Object> getAuthConfiguration() {
        return authConfiguration;
    }
    
    public void setAuthConfiguration(Map<String, Object> authConfiguration) {
        this.authConfiguration = authConfiguration;
    }
    
    public Map<String, Object> getClientHeaders() {
        return clientHeaders;
    }
    
    public void setClientHeaders(Map<String, Object> clientHeaders) {
        this.clientHeaders = clientHeaders;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}