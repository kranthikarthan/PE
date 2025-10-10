package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for storing tenant authentication configuration
 * Supports configurable authentication methods and client credentials
 */
@Entity
@Table(name = "tenant_auth_configuration", indexes = {
    @Index(name = "idx_tenant_auth_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_tenant_auth_active", columnList = "isActive"),
    @Index(name = "idx_tenant_auth_created_at", columnList = "createdAt")
})
public class TenantAuthConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthMethod authMethod = AuthMethod.JWT;

    @Column(length = 100)
    private String clientId;

    @Column(length = 500)
    private String clientSecret;

    @Column(length = 100)
    private String clientIdHeaderName = "X-Client-ID";

    @Column(length = 100)
    private String clientSecretHeaderName = "X-Client-Secret";

    @Column(length = 100)
    private String authHeaderName = "Authorization";

    @Column(length = 20)
    private String authHeaderPrefix = "Bearer";

    @Column(length = 100)
    private String tokenEndpoint;

    @Column(length = 100)
    private String publicKeyEndpoint;

    @Column(length = 100)
    private String jwsPublicKey;

    @Column(length = 50)
    private String jwsAlgorithm = "HS256";

    @Column(length = 100)
    private String jwsIssuer;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean includeClientHeaders = false;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public TenantAuthConfiguration() {}

    public TenantAuthConfiguration(String tenantId, AuthMethod authMethod) {
        this.tenantId = tenantId;
        this.authMethod = authMethod;
    }

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

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthMethod authMethod) {
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

    // Authentication Method enum
    public enum AuthMethod {
        JWT("JSON Web Token"),
        JWS("JSON Web Signature"),
        OAUTH2("OAuth 2.0"),
        API_KEY("API Key"),
        BASIC("Basic Authentication");

        private final String displayName;

        AuthMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}