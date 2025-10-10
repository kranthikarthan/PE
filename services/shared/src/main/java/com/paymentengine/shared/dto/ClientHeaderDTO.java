package com.paymentengine.shared.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Shared DTO for client header configuration
 * Used across all services for consistent client header data transfer
 */
public class ClientHeaderDTO {
    
    private String tenantId;
    private String configurationLevel; // clearing-system, payment-type, downstream-call
    private String configurationId;
    private Boolean includeClientHeaders;
    private String clientId;
    private String clientSecret;
    private String clientIdHeaderName;
    private String clientSecretHeaderName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;
    
    // Constructors
    public ClientHeaderDTO() {}
    
    public ClientHeaderDTO(String tenantId, String configurationLevel, Boolean includeClientHeaders) {
        this.tenantId = tenantId;
        this.configurationLevel = configurationLevel;
        this.includeClientHeaders = includeClientHeaders;
    }
    
    // Getters and Setters
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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
    
    public Boolean getIncludeClientHeaders() {
        return includeClientHeaders;
    }
    
    public void setIncludeClientHeaders(Boolean includeClientHeaders) {
        this.includeClientHeaders = includeClientHeaders;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    public String getClientIdHeaderName() {
        return clientIdHeaderName;
    }
    
    public void setClientIdHeaderName(String clientIdHeaderName) {
        this.clientIdHeaderName = clientIdHeaderName;
    }
    
    public String getClientSecretHeaderName() {
        return clientSecretHeaderName;
    }
    
    public void setClientSecretHeaderName(String clientSecretHeaderName) {
        this.clientSecretHeaderName = clientSecretHeaderName;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}