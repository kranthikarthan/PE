package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity for payment type level authentication configuration
 * This configuration applies to specific payment types (e.g., SEPA, SWIFT, ACH)
 */
@Entity
@Table(name = "payment_type_auth_configuration", indexes = {
        @Index(name = "idx_payment_type_auth_payment_type", columnList = "paymentType"),
        @Index(name = "idx_payment_type_auth_tenant", columnList = "tenantId"),
        @Index(name = "idx_payment_type_auth_active", columnList = "isActive")
})
public class PaymentTypeAuthConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String tenantId;

    @Column(nullable = false, length = 50)
    private String paymentType; // SEPA, SWIFT, ACH, CARD, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthMethod authMethod;

    // JWT Configuration
    @Column(length = 500)
    private String jwtSecret;
    @Column(length = 100)
    private String jwtIssuer;
    @Column(length = 100)
    private String jwtAudience;
    @Column
    private Integer jwtExpirationSeconds;

    // JWS Configuration
    @Column(length = 500)
    private String jwsSecret;
    @Column(length = 20)
    private String jwsAlgorithm; // HS256, HS384, HS512, RS256, RS384, RS512
    @Column(length = 100)
    private String jwsIssuer;
    @Column(length = 100)
    private String jwsAudience;
    @Column
    private Integer jwsExpirationSeconds;

    // OAuth2 Configuration
    @Column(length = 500)
    private String oauth2TokenEndpoint;
    @Column(length = 500)
    private String oauth2ClientId;
    @Column(length = 500)
    private String oauth2ClientSecret;
    @Column(length = 100)
    private String oauth2Scope;

    // API Key Configuration
    @Column(length = 500)
    private String apiKey;
    @Column(length = 100)
    private String apiKeyHeaderName;

    // Basic Auth Configuration
    @Column(length = 100)
    private String basicAuthUsername;
    @Column(length = 500)
    private String basicAuthPassword;

    // Client Headers Configuration
    @Column(nullable = false)
    private Boolean includeClientHeaders = false;
    @Column(length = 100)
    private String clientId;
    @Column(length = 500)
    private String clientSecret;
    @Column(length = 100)
    private String clientIdHeaderName;
    @Column(length = 100)
    private String clientSecretHeaderName;

    // Payment Type Specific Configuration
    @Column(length = 100)
    private String clearingSystem; // Target clearing system
    @Column(length = 100)
    private String routingCode; // Payment routing code
    @Column(length = 100)
    private String currency; // Payment currency
    @Column
    private Boolean isHighValue = false; // High value payment flag

    // Additional Configuration
    @Column(nullable = false)
    private Boolean isActive = false;
    @Column(length = 500)
    private String description;

    @ElementCollection
    @CollectionTable(name = "payment_type_auth_configuration_metadata", joinColumns = @JoinColumn(name = "payment_type_auth_configuration_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value", columnDefinition = "TEXT")
    private Map<String, String> metadata;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public PaymentTypeAuthConfiguration() {}

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

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String getJwtIssuer() {
        return jwtIssuer;
    }

    public void setJwtIssuer(String jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
    }

    public String getJwtAudience() {
        return jwtAudience;
    }

    public void setJwtAudience(String jwtAudience) {
        this.jwtAudience = jwtAudience;
    }

    public Integer getJwtExpirationSeconds() {
        return jwtExpirationSeconds;
    }

    public void setJwtExpirationSeconds(Integer jwtExpirationSeconds) {
        this.jwtExpirationSeconds = jwtExpirationSeconds;
    }

    public String getJwsSecret() {
        return jwsSecret;
    }

    public void setJwsSecret(String jwsSecret) {
        this.jwsSecret = jwsSecret;
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

    public String getJwsAudience() {
        return jwsAudience;
    }

    public void setJwsAudience(String jwsAudience) {
        this.jwsAudience = jwsAudience;
    }

    public Integer getJwsExpirationSeconds() {
        return jwsExpirationSeconds;
    }

    public void setJwsExpirationSeconds(Integer jwsExpirationSeconds) {
        this.jwsExpirationSeconds = jwsExpirationSeconds;
    }

    public String getOauth2TokenEndpoint() {
        return oauth2TokenEndpoint;
    }

    public void setOauth2TokenEndpoint(String oauth2TokenEndpoint) {
        this.oauth2TokenEndpoint = oauth2TokenEndpoint;
    }

    public String getOauth2ClientId() {
        return oauth2ClientId;
    }

    public void setOauth2ClientId(String oauth2ClientId) {
        this.oauth2ClientId = oauth2ClientId;
    }

    public String getOauth2ClientSecret() {
        return oauth2ClientSecret;
    }

    public void setOauth2ClientSecret(String oauth2ClientSecret) {
        this.oauth2ClientSecret = oauth2ClientSecret;
    }

    public String getOauth2Scope() {
        return oauth2Scope;
    }

    public void setOauth2Scope(String oauth2Scope) {
        this.oauth2Scope = oauth2Scope;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKeyHeaderName() {
        return apiKeyHeaderName;
    }

    public void setApiKeyHeaderName(String apiKeyHeaderName) {
        this.apiKeyHeaderName = apiKeyHeaderName;
    }

    public String getBasicAuthUsername() {
        return basicAuthUsername;
    }

    public void setBasicAuthUsername(String basicAuthUsername) {
        this.basicAuthUsername = basicAuthUsername;
    }

    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    public void setBasicAuthPassword(String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
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

    public String getClearingSystem() {
        return clearingSystem;
    }

    public void setClearingSystem(String clearingSystem) {
        this.clearingSystem = clearingSystem;
    }

    public String getRoutingCode() {
        return routingCode;
    }

    public void setRoutingCode(String routingCode) {
        this.routingCode = routingCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getIsHighValue() {
        return isHighValue;
    }

    public void setIsHighValue(Boolean highValue) {
        isHighValue = highValue;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
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

    public enum AuthMethod {
        JWT, JWS, OAUTH2, API_KEY, BASIC
    }
}