package com.paymentengine.paymentprocessing.dto;

import com.paymentengine.paymentprocessing.entity.TenantAuthConfiguration;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for tenant authentication configuration
 */
public class TenantAuthConfigurationResponse {
    
    private UUID id;
    private String tenantId;
    private TenantAuthConfiguration.AuthMethod authMethod;
    private String clientId;
    private String clientIdHeaderName;
    private String clientSecretHeaderName;
    private String authHeaderName;
    private String authHeaderPrefix;
    private String tokenEndpoint;
    private String publicKeyEndpoint;
    private String jwsPublicKey;
    private String jwsAlgorithm;
    private String jwsIssuer;
    private Boolean isActive;
    private Boolean includeClientHeaders;
    private String description;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public TenantAuthConfigurationResponse() {}
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public TenantAuthConfiguration.AuthMethod getAuthMethod() {
        return authMethod;
    }
    
    public void setAuthMethod(TenantAuthConfiguration.AuthMethod authMethod) {
        this.authMethod = authMethod;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
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
    
    public String getAuthHeaderName() {
        return authHeaderName;
    }
    
    public void setAuthHeaderName(String authHeaderName) {
        this.authHeaderName = authHeaderName;
    }
    
    public String getAuthHeaderPrefix() {
        return authHeaderPrefix;
    }
    
    public void setAuthHeaderPrefix(String authHeaderPrefix) {
        this.authHeaderPrefix = authHeaderPrefix;
    }
    
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }
    
    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }
    
    public String getPublicKeyEndpoint() {
        return publicKeyEndpoint;
    }
    
    public void setPublicKeyEndpoint(String publicKeyEndpoint) {
        this.publicKeyEndpoint = publicKeyEndpoint;
    }
    
    public String getJwsPublicKey() {
        return jwsPublicKey;
    }
    
    public void setJwsPublicKey(String jwsPublicKey) {
        this.jwsPublicKey = jwsPublicKey;
    }
    
    public String getJwsAlgorithm() {
        return jwsAlgorithm;
    }
    
    public void setJwsAlgorithm(String jwsAlgorithm) {
        this.jwsAlgorithm = jwsAlgorithm;
    }
    
    public String getJwsIssuer() {
        return jwsIssuer;
    }
    
    public void setJwsIssuer(String jwsIssuer) {
        this.jwsIssuer = jwsIssuer;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIncludeClientHeaders() {
        return includeClientHeaders;
    }
    
    public void setIncludeClientHeaders(Boolean includeClientHeaders) {
        this.includeClientHeaders = includeClientHeaders;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
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
}