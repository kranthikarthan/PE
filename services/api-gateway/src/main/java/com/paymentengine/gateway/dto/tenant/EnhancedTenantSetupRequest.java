package com.paymentengine.gateway.dto.tenant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for enhanced tenant setup with multi-level authentication configuration
 */
public class EnhancedTenantSetupRequest {
    
    @Valid
    @NotNull
    private BasicInfo basicInfo;
    
    @Valid
    private ClearingSystemConfig clearingSystemConfig;
    
    @Valid
    private List<PaymentTypeConfig> paymentTypeConfigs;
    
    @Valid
    private List<DownstreamCallConfig> downstreamCallConfigs;
    
    private Map<String, Object> metadata;
    
    // Constructors
    public EnhancedTenantSetupRequest() {}
    
    // Getters and Setters
    public BasicInfo getBasicInfo() {
        return basicInfo;
    }
    
    public void setBasicInfo(BasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }
    
    public ClearingSystemConfig getClearingSystemConfig() {
        return clearingSystemConfig;
    }
    
    public void setClearingSystemConfig(ClearingSystemConfig clearingSystemConfig) {
        this.clearingSystemConfig = clearingSystemConfig;
    }
    
    public List<PaymentTypeConfig> getPaymentTypeConfigs() {
        return paymentTypeConfigs;
    }
    
    public void setPaymentTypeConfigs(List<PaymentTypeConfig> paymentTypeConfigs) {
        this.paymentTypeConfigs = paymentTypeConfigs;
    }
    
    public List<DownstreamCallConfig> getDownstreamCallConfigs() {
        return downstreamCallConfigs;
    }
    
    public void setDownstreamCallConfigs(List<DownstreamCallConfig> downstreamCallConfigs) {
        this.downstreamCallConfigs = downstreamCallConfigs;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    // Inner classes
    public static class BasicInfo {
        @NotBlank
        private String tenantId;
        
        @NotBlank
        private String tenantName;
        
        private String description;
        
        @NotBlank
        private String environment; // dev, staging, prod
        
        // Constructors
        public BasicInfo() {}
        
        // Getters and Setters
        public String getTenantId() {
            return tenantId;
        }
        
        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
        
        public String getTenantName() {
            return tenantName;
        }
        
        public void setTenantName(String tenantName) {
            this.tenantName = tenantName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getEnvironment() {
            return environment;
        }
        
        public void setEnvironment(String environment) {
            this.environment = environment;
        }
    }
    
    public static class ClearingSystemConfig {
        @NotBlank
        private String authMethod; // JWT, JWS, OAUTH2, API_KEY, BASIC
        
        private JwtConfig jwtConfig;
        private JwsConfig jwsConfig;
        private OAuth2Config oauth2Config;
        private ApiKeyConfig apiKeyConfig;
        private BasicAuthConfig basicAuthConfig;
        
        @Valid
        private ClientHeadersConfig clientHeaders;
        
        // Constructors
        public ClearingSystemConfig() {}
        
        // Getters and Setters
        public String getAuthMethod() {
            return authMethod;
        }
        
        public void setAuthMethod(String authMethod) {
            this.authMethod = authMethod;
        }
        
        public JwtConfig getJwtConfig() {
            return jwtConfig;
        }
        
        public void setJwtConfig(JwtConfig jwtConfig) {
            this.jwtConfig = jwtConfig;
        }
        
        public JwsConfig getJwsConfig() {
            return jwsConfig;
        }
        
        public void setJwsConfig(JwsConfig jwsConfig) {
            this.jwsConfig = jwsConfig;
        }
        
        public OAuth2Config getOauth2Config() {
            return oauth2Config;
        }
        
        public void setOauth2Config(OAuth2Config oauth2Config) {
            this.oauth2Config = oauth2Config;
        }
        
        public ApiKeyConfig getApiKeyConfig() {
            return apiKeyConfig;
        }
        
        public void setApiKeyConfig(ApiKeyConfig apiKeyConfig) {
            this.apiKeyConfig = apiKeyConfig;
        }
        
        public BasicAuthConfig getBasicAuthConfig() {
            return basicAuthConfig;
        }
        
        public void setBasicAuthConfig(BasicAuthConfig basicAuthConfig) {
            this.basicAuthConfig = basicAuthConfig;
        }
        
        public ClientHeadersConfig getClientHeaders() {
            return clientHeaders;
        }
        
        public void setClientHeaders(ClientHeadersConfig clientHeaders) {
            this.clientHeaders = clientHeaders;
        }
    }
    
    public static class PaymentTypeConfig {
        @NotBlank
        private String paymentType; // SEPA, SWIFT, ACH, CARD, CUSTOM
        
        @NotBlank
        private String authMethod;
        
        private String clearingSystem;
        private String currency;
        private Boolean isHighValue = false;
        
        @Valid
        private ClientHeadersConfig clientHeaders;
        
        // Constructors
        public PaymentTypeConfig() {}
        
        // Getters and Setters
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
        
        public String getClearingSystem() {
            return clearingSystem;
        }
        
        public void setClearingSystem(String clearingSystem) {
            this.clearingSystem = clearingSystem;
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
        
        public void setIsHighValue(Boolean isHighValue) {
            this.isHighValue = isHighValue;
        }
        
        public ClientHeadersConfig getClientHeaders() {
            return clientHeaders;
        }
        
        public void setClientHeaders(ClientHeadersConfig clientHeaders) {
            this.clientHeaders = clientHeaders;
        }
    }
    
    public static class DownstreamCallConfig {
        @NotBlank
        private String serviceType; // fraud, clearing, banking, custom
        
        @NotBlank
        private String endpoint;
        
        private String paymentType;
        
        @NotBlank
        private String authMethod;
        
        private String targetHost;
        private Integer targetPort;
        private String targetProtocol; // HTTP, HTTPS
        private String targetPath;
        private Integer timeoutSeconds;
        private Integer retryAttempts;
        private Integer retryDelaySeconds;
        
        @Valid
        private ClientHeadersConfig clientHeaders;
        
        // Constructors
        public DownstreamCallConfig() {}
        
        // Getters and Setters
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
        
        public ClientHeadersConfig getClientHeaders() {
            return clientHeaders;
        }
        
        public void setClientHeaders(ClientHeadersConfig clientHeaders) {
            this.clientHeaders = clientHeaders;
        }
    }
    
    // Authentication configuration classes
    public static class JwtConfig {
        private String secret;
        private String issuer;
        private String audience;
        private Integer expirationSeconds;
        
        // Getters and Setters
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getAudience() { return audience; }
        public void setAudience(String audience) { this.audience = audience; }
        public Integer getExpirationSeconds() { return expirationSeconds; }
        public void setExpirationSeconds(Integer expirationSeconds) { this.expirationSeconds = expirationSeconds; }
    }
    
    public static class JwsConfig {
        private String secret;
        private String algorithm; // HS256, HS384, HS512, RS256, RS384, RS512
        private String issuer;
        private String audience;
        private Integer expirationSeconds;
        
        // Getters and Setters
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getAudience() { return audience; }
        public void setAudience(String audience) { this.audience = audience; }
        public Integer getExpirationSeconds() { return expirationSeconds; }
        public void setExpirationSeconds(Integer expirationSeconds) { this.expirationSeconds = expirationSeconds; }
    }
    
    public static class OAuth2Config {
        private String tokenEndpoint;
        private String clientId;
        private String clientSecret;
        private String scope;
        
        // Getters and Setters
        public String getTokenEndpoint() { return tokenEndpoint; }
        public void setTokenEndpoint(String tokenEndpoint) { this.tokenEndpoint = tokenEndpoint; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
    }
    
    public static class ApiKeyConfig {
        private String apiKey;
        private String headerName;
        
        // Getters and Setters
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getHeaderName() { return headerName; }
        public void setHeaderName(String headerName) { this.headerName = headerName; }
    }
    
    public static class BasicAuthConfig {
        private String username;
        private String password;
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class ClientHeadersConfig {
        private Boolean includeClientHeaders = false;
        private String clientId;
        private String clientSecret;
        private String clientIdHeaderName;
        private String clientSecretHeaderName;
        
        // Getters and Setters
        public Boolean getIncludeClientHeaders() { return includeClientHeaders; }
        public void setIncludeClientHeaders(Boolean includeClientHeaders) { this.includeClientHeaders = includeClientHeaders; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
        public String getClientIdHeaderName() { return clientIdHeaderName; }
        public void setClientIdHeaderName(String clientIdHeaderName) { this.clientIdHeaderName = clientIdHeaderName; }
        public String getClientSecretHeaderName() { return clientSecretHeaderName; }
        public void setClientSecretHeaderName(String clientSecretHeaderName) { this.clientSecretHeaderName = clientSecretHeaderName; }
    }
}