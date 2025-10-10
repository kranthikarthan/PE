package com.paymentengine.paymentprocessing.dto;

import com.paymentengine.paymentprocessing.entity.TenantAuthConfiguration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for tenant authentication configuration
 */
public class TenantAuthConfigurationRequest {
    
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
    
    @NotNull(message = "Authentication method is required")
    private TenantAuthConfiguration.AuthMethod authMethod;
    
    private String clientId;
    
    private String clientSecret;
    
    private String clientIdHeaderName = "X-Client-ID";
    
    private String clientSecretHeaderName = "X-Client-Secret";
    
    private String authHeaderName = "Authorization";
    
    private String authHeaderPrefix = "Bearer";
    
    private String tokenEndpoint;
    
    private String publicKeyEndpoint;
    
    private String jwsPublicKey;
    
    private String jwsAlgorithm = "HS256";
    
    private String jwsIssuer;
    
    private Boolean includeClientHeaders = false;
    
    private String description;
    
    private String createdBy;
    
    private String updatedBy;
    
    // Constructors
    public TenantAuthConfigurationRequest() {}
    
    public TenantAuthConfigurationRequest(String tenantId, TenantAuthConfiguration.AuthMethod authMethod) {
        this.tenantId = tenantId;
        this.authMethod = authMethod;
    }
    
    // Getters and Setters
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
}