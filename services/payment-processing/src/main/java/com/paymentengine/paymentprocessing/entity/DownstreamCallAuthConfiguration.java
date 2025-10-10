package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity for downstream call per tenant level authentication configuration
 * This is the most granular level configuration for specific downstream calls
 */
@Entity
@Table(name = "downstream_call_auth_configuration", indexes = {
        @Index(name = "idx_downstream_call_auth_tenant", columnList = "tenantId"),
        @Index(name = "idx_downstream_call_auth_service", columnList = "serviceType"),
        @Index(name = "idx_downstream_call_auth_endpoint", columnList = "endpoint"),
        @Index(name = "idx_downstream_call_auth_active", columnList = "isActive")
})
public class DownstreamCallAuthConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String tenantId;

    @Column(nullable = false, length = 50)
    private String serviceType; // fraud, clearing, banking, etc.

    @Column(nullable = false, length = 200)
    private String endpoint; // Specific endpoint path

    @Column(length = 50)
    private String paymentType; // Optional: specific payment type

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

    // Downstream Call Specific Configuration
    @Column(length = 500)
    private String targetHost; // Target host for the call
    @Column
    private Integer targetPort; // Target port for the call
    @Column(length = 20)
    private String targetProtocol; // HTTP, HTTPS
    @Column(length = 100)
    private String targetPath; // Target path
    @Column
    private Integer timeoutSeconds; // Call timeout
    @Column
    private Integer retryAttempts; // Number of retry attempts
    @Column
    private Integer retryDelaySeconds; // Delay between retries

    // Additional Configuration
    @Column(nullable = false)
    private Boolean isActive = false;
    @Column(length = 500)
    private String description;

    @ElementCollection
    @CollectionTable(name = "downstream_call_auth_configuration_metadata", joinColumns = @JoinColumn(name = "downstream_call_auth_configuration_id"))
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
    public DownstreamCallAuthConfiguration() {}

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

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public Integer getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(Integer targetPort) {
        this.targetPort = targetPort;
    }

    public String getTargetProtocol() {
        return targetProtocol;
    }

    public void setTargetProtocol(String targetProtocol) {
        this.targetProtocol = targetProtocol;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Integer getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(Integer retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public Integer getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public void setRetryDelaySeconds(Integer retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
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