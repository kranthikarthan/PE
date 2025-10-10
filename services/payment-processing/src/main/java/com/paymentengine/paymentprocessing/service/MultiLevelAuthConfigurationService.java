package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.entity.*;
import com.paymentengine.paymentprocessing.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Service for managing multi-level authentication configuration
 * Handles configuration hierarchy and precedence:
 * 1. Downstream Call Level (highest priority)
 * 2. Payment Type Level
 * 3. Tenant Level
 * 4. Clearing System Level (lowest priority)
 */
@Service
@Transactional
public class MultiLevelAuthConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(MultiLevelAuthConfigurationService.class);

    @Autowired
    private ClearingSystemAuthConfigurationRepository clearingSystemAuthConfigRepository;

    @Autowired
    private TenantAuthConfigurationRepository tenantAuthConfigRepository;

    @Autowired
    private PaymentTypeAuthConfigurationRepository paymentTypeAuthConfigRepository;

    @Autowired
    private DownstreamCallAuthConfigurationRepository downstreamCallAuthConfigRepository;

    @Value("${app.environment:dev}")
    private String currentEnvironment;

    /**
     * Get resolved authentication configuration for a downstream call
     * Resolves configuration hierarchy with proper precedence
     */
    public ResolvedAuthConfiguration getResolvedConfiguration(String tenantId, String serviceType, 
                                                           String endpoint, String paymentType) {
        logger.debug("Resolving auth configuration for tenant: {}, service: {}, endpoint: {}, paymentType: {}", 
                    tenantId, serviceType, endpoint, paymentType);

        ResolvedAuthConfiguration resolved = new ResolvedAuthConfiguration();

        // 1. Start with clearing system level (lowest priority)
        Optional<ClearingSystemAuthConfiguration> clearingSystemConfig = 
            clearingSystemAuthConfigRepository.findByEnvironmentAndIsActive(currentEnvironment, true);
        if (clearingSystemConfig.isPresent()) {
            resolved.mergeFrom(clearingSystemConfig.get());
            logger.debug("Applied clearing system level configuration");
        }

        // 2. Apply tenant level configuration
        Optional<TenantAuthConfiguration> tenantConfig = 
            tenantAuthConfigRepository.findByTenantIdAndIsActive(tenantId, true);
        if (tenantConfig.isPresent()) {
            resolved.mergeFrom(tenantConfig.get());
            logger.debug("Applied tenant level configuration");
        }

        // 3. Apply payment type level configuration
        if (paymentType != null) {
            Optional<PaymentTypeAuthConfiguration> paymentTypeConfig = 
                paymentTypeAuthConfigRepository.findByTenantIdAndPaymentTypeAndIsActive(tenantId, paymentType, true);
            if (paymentTypeConfig.isPresent()) {
                resolved.mergeFrom(paymentTypeConfig.get());
                logger.debug("Applied payment type level configuration for: {}", paymentType);
            }
        }

        // 4. Apply downstream call level configuration (highest priority)
        Optional<DownstreamCallAuthConfiguration> downstreamConfig = 
            downstreamCallAuthConfigRepository.findByTenantIdAndServiceTypeAndEndpointAndIsActive(
                tenantId, serviceType, endpoint, true);
        if (downstreamConfig.isPresent()) {
            resolved.mergeFrom(downstreamConfig.get());
            logger.debug("Applied downstream call level configuration for: {}:{}", serviceType, endpoint);
        }

        logger.info("Resolved auth configuration for tenant: {} - Method: {}, Headers: {}", 
                   tenantId, resolved.getAuthMethod(), resolved.getIncludeClientHeaders());

        return resolved;
    }

    /**
     * Get configuration for a specific level
     */
    public Optional<ClearingSystemAuthConfiguration> getClearingSystemConfiguration(String environment) {
        return clearingSystemAuthConfigRepository.findByEnvironmentAndIsActive(environment, true);
    }

    public Optional<TenantAuthConfiguration> getTenantConfiguration(String tenantId) {
        return tenantAuthConfigRepository.findByTenantIdAndIsActive(tenantId, true);
    }

    public Optional<PaymentTypeAuthConfiguration> getPaymentTypeConfiguration(String tenantId, String paymentType) {
        return paymentTypeAuthConfigRepository.findByTenantIdAndPaymentTypeAndIsActive(tenantId, paymentType, true);
    }

    public Optional<DownstreamCallAuthConfiguration> getDownstreamCallConfiguration(String tenantId, String serviceType, String endpoint) {
        return downstreamCallAuthConfigRepository.findByTenantIdAndServiceTypeAndEndpointAndIsActive(
            tenantId, serviceType, endpoint, true);
    }

    /**
     * Resolved authentication configuration with merged values from all levels
     */
    public static class ResolvedAuthConfiguration {
        private TenantAuthConfiguration.AuthMethod authMethod;
        private String jwtSecret;
        private String jwtIssuer;
        private String jwtAudience;
        private Integer jwtExpirationSeconds;
        private String jwsSecret;
        private String jwsAlgorithm;
        private String jwsIssuer;
        private String jwsAudience;
        private Integer jwsExpirationSeconds;
        private String oauth2TokenEndpoint;
        private String oauth2ClientId;
        private String oauth2ClientSecret;
        private String oauth2Scope;
        private String apiKey;
        private String apiKeyHeaderName;
        private String basicAuthUsername;
        private String basicAuthPassword;
        private Boolean includeClientHeaders = false;
        private String clientId;
        private String clientSecret;
        private String clientIdHeaderName;
        private String clientSecretHeaderName;
        private String targetHost;
        private Integer targetPort;
        private String targetProtocol;
        private String targetPath;
        private Integer timeoutSeconds;
        private Integer retryAttempts;
        private Integer retryDelaySeconds;
        private Map<String, String> metadata;

        public void mergeFrom(ClearingSystemAuthConfiguration config) {
            if (config.getAuthMethod() != null) this.authMethod = config.getAuthMethod();
            if (config.getJwtSecret() != null) this.jwtSecret = config.getJwtSecret();
            if (config.getJwtIssuer() != null) this.jwtIssuer = config.getJwtIssuer();
            if (config.getJwtAudience() != null) this.jwtAudience = config.getJwtAudience();
            if (config.getJwtExpirationSeconds() != null) this.jwtExpirationSeconds = config.getJwtExpirationSeconds();
            if (config.getJwsSecret() != null) this.jwsSecret = config.getJwsSecret();
            if (config.getJwsAlgorithm() != null) this.jwsAlgorithm = config.getJwsAlgorithm();
            if (config.getJwsIssuer() != null) this.jwsIssuer = config.getJwsIssuer();
            if (config.getJwsAudience() != null) this.jwsAudience = config.getJwsAudience();
            if (config.getJwsExpirationSeconds() != null) this.jwsExpirationSeconds = config.getJwsExpirationSeconds();
            if (config.getOauth2TokenEndpoint() != null) this.oauth2TokenEndpoint = config.getOauth2TokenEndpoint();
            if (config.getOauth2ClientId() != null) this.oauth2ClientId = config.getOauth2ClientId();
            if (config.getOauth2ClientSecret() != null) this.oauth2ClientSecret = config.getOauth2ClientSecret();
            if (config.getOauth2Scope() != null) this.oauth2Scope = config.getOauth2Scope();
            if (config.getApiKey() != null) this.apiKey = config.getApiKey();
            if (config.getApiKeyHeaderName() != null) this.apiKeyHeaderName = config.getApiKeyHeaderName();
            if (config.getBasicAuthUsername() != null) this.basicAuthUsername = config.getBasicAuthUsername();
            if (config.getBasicAuthPassword() != null) this.basicAuthPassword = config.getBasicAuthPassword();
            if (config.getIncludeClientHeaders() != null) this.includeClientHeaders = config.getIncludeClientHeaders();
            if (config.getClientId() != null) this.clientId = config.getClientId();
            if (config.getClientSecret() != null) this.clientSecret = config.getClientSecret();
            if (config.getClientIdHeaderName() != null) this.clientIdHeaderName = config.getClientIdHeaderName();
            if (config.getClientSecretHeaderName() != null) this.clientSecretHeaderName = config.getClientSecretHeaderName();
            if (config.getMetadata() != null) this.metadata = config.getMetadata();
        }

        public void mergeFrom(TenantAuthConfiguration config) {
            if (config.getAuthMethod() != null) this.authMethod = config.getAuthMethod();
            if (config.getJwtSecret() != null) this.jwtSecret = config.getJwtSecret();
            if (config.getJwsSecret() != null) this.jwsSecret = config.getJwsSecret();
            if (config.getJwsAlgorithm() != null) this.jwsAlgorithm = config.getJwsAlgorithm();
            if (config.getJwsIssuer() != null) this.jwsIssuer = config.getJwsIssuer();
            if (config.getTokenEndpoint() != null) this.oauth2TokenEndpoint = config.getTokenEndpoint();
            if (config.getClientId() != null) this.oauth2ClientId = config.getClientId();
            if (config.getClientSecret() != null) this.oauth2ClientSecret = config.getClientSecret();
            if (config.getApiKey() != null) this.apiKey = config.getApiKey();
            if (config.getApiKeyHeaderName() != null) this.apiKeyHeaderName = config.getApiKeyHeaderName();
            if (config.getBasicAuthUsername() != null) this.basicAuthUsername = config.getBasicAuthUsername();
            if (config.getBasicAuthPassword() != null) this.basicAuthPassword = config.getBasicAuthPassword();
            if (config.getIncludeClientHeaders() != null) this.includeClientHeaders = config.getIncludeClientHeaders();
            if (config.getOutgoingClientId() != null) this.clientId = config.getOutgoingClientId();
            if (config.getOutgoingClientSecret() != null) this.clientSecret = config.getOutgoingClientSecret();
            if (config.getOutgoingClientIdHeaderName() != null) this.clientIdHeaderName = config.getOutgoingClientIdHeaderName();
            if (config.getOutgoingClientSecretHeaderName() != null) this.clientSecretHeaderName = config.getOutgoingClientSecretHeaderName();
            if (config.getMetadata() != null) this.metadata = config.getMetadata();
        }

        public void mergeFrom(PaymentTypeAuthConfiguration config) {
            if (config.getAuthMethod() != null) this.authMethod = config.getAuthMethod();
            if (config.getJwtSecret() != null) this.jwtSecret = config.getJwtSecret();
            if (config.getJwtIssuer() != null) this.jwtIssuer = config.getJwtIssuer();
            if (config.getJwtAudience() != null) this.jwtAudience = config.getJwtAudience();
            if (config.getJwtExpirationSeconds() != null) this.jwtExpirationSeconds = config.getJwtExpirationSeconds();
            if (config.getJwsSecret() != null) this.jwsSecret = config.getJwsSecret();
            if (config.getJwsAlgorithm() != null) this.jwsAlgorithm = config.getJwsAlgorithm();
            if (config.getJwsIssuer() != null) this.jwsIssuer = config.getJwsIssuer();
            if (config.getJwsAudience() != null) this.jwsAudience = config.getJwsAudience();
            if (config.getJwsExpirationSeconds() != null) this.jwsExpirationSeconds = config.getJwsExpirationSeconds();
            if (config.getOauth2TokenEndpoint() != null) this.oauth2TokenEndpoint = config.getOauth2TokenEndpoint();
            if (config.getOauth2ClientId() != null) this.oauth2ClientId = config.getOauth2ClientId();
            if (config.getOauth2ClientSecret() != null) this.oauth2ClientSecret = config.getOauth2ClientSecret();
            if (config.getOauth2Scope() != null) this.oauth2Scope = config.getOauth2Scope();
            if (config.getApiKey() != null) this.apiKey = config.getApiKey();
            if (config.getApiKeyHeaderName() != null) this.apiKeyHeaderName = config.getApiKeyHeaderName();
            if (config.getBasicAuthUsername() != null) this.basicAuthUsername = config.getBasicAuthUsername();
            if (config.getBasicAuthPassword() != null) this.basicAuthPassword = config.getBasicAuthPassword();
            if (config.getIncludeClientHeaders() != null) this.includeClientHeaders = config.getIncludeClientHeaders();
            if (config.getClientId() != null) this.clientId = config.getClientId();
            if (config.getClientSecret() != null) this.clientSecret = config.getClientSecret();
            if (config.getClientIdHeaderName() != null) this.clientIdHeaderName = config.getClientIdHeaderName();
            if (config.getClientSecretHeaderName() != null) this.clientSecretHeaderName = config.getClientSecretHeaderName();
            if (config.getMetadata() != null) this.metadata = config.getMetadata();
        }

        public void mergeFrom(DownstreamCallAuthConfiguration config) {
            if (config.getAuthMethod() != null) this.authMethod = config.getAuthMethod();
            if (config.getJwtSecret() != null) this.jwtSecret = config.getJwtSecret();
            if (config.getJwtIssuer() != null) this.jwtIssuer = config.getJwtIssuer();
            if (config.getJwtAudience() != null) this.jwtAudience = config.getJwtAudience();
            if (config.getJwtExpirationSeconds() != null) this.jwtExpirationSeconds = config.getJwtExpirationSeconds();
            if (config.getJwsSecret() != null) this.jwsSecret = config.getJwsSecret();
            if (config.getJwsAlgorithm() != null) this.jwsAlgorithm = config.getJwsAlgorithm();
            if (config.getJwsIssuer() != null) this.jwsIssuer = config.getJwsIssuer();
            if (config.getJwsAudience() != null) this.jwsAudience = config.getJwsAudience();
            if (config.getJwsExpirationSeconds() != null) this.jwsExpirationSeconds = config.getJwsExpirationSeconds();
            if (config.getOauth2TokenEndpoint() != null) this.oauth2TokenEndpoint = config.getOauth2TokenEndpoint();
            if (config.getOauth2ClientId() != null) this.oauth2ClientId = config.getOauth2ClientId();
            if (config.getOauth2ClientSecret() != null) this.oauth2ClientSecret = config.getOauth2ClientSecret();
            if (config.getOauth2Scope() != null) this.oauth2Scope = config.getOauth2Scope();
            if (config.getApiKey() != null) this.apiKey = config.getApiKey();
            if (config.getApiKeyHeaderName() != null) this.apiKeyHeaderName = config.getApiKeyHeaderName();
            if (config.getBasicAuthUsername() != null) this.basicAuthUsername = config.getBasicAuthUsername();
            if (config.getBasicAuthPassword() != null) this.basicAuthPassword = config.getBasicAuthPassword();
            if (config.getIncludeClientHeaders() != null) this.includeClientHeaders = config.getIncludeClientHeaders();
            if (config.getClientId() != null) this.clientId = config.getClientId();
            if (config.getClientSecret() != null) this.clientSecret = config.getClientSecret();
            if (config.getClientIdHeaderName() != null) this.clientIdHeaderName = config.getClientIdHeaderName();
            if (config.getClientSecretHeaderName() != null) this.clientSecretHeaderName = config.getClientSecretHeaderName();
            if (config.getTargetHost() != null) this.targetHost = config.getTargetHost();
            if (config.getTargetPort() != null) this.targetPort = config.getTargetPort();
            if (config.getTargetProtocol() != null) this.targetProtocol = config.getTargetProtocol();
            if (config.getTargetPath() != null) this.targetPath = config.getTargetPath();
            if (config.getTimeoutSeconds() != null) this.timeoutSeconds = config.getTimeoutSeconds();
            if (config.getRetryAttempts() != null) this.retryAttempts = config.getRetryAttempts();
            if (config.getRetryDelaySeconds() != null) this.retryDelaySeconds = config.getRetryDelaySeconds();
            if (config.getMetadata() != null) this.metadata = config.getMetadata();
        }

        // Getters
        public TenantAuthConfiguration.AuthMethod getAuthMethod() { return authMethod; }
        public String getJwtSecret() { return jwtSecret; }
        public String getJwtIssuer() { return jwtIssuer; }
        public String getJwtAudience() { return jwtAudience; }
        public Integer getJwtExpirationSeconds() { return jwtExpirationSeconds; }
        public String getJwsSecret() { return jwsSecret; }
        public String getJwsAlgorithm() { return jwsAlgorithm; }
        public String getJwsIssuer() { return jwsIssuer; }
        public String getJwsAudience() { return jwsAudience; }
        public Integer getJwsExpirationSeconds() { return jwsExpirationSeconds; }
        public String getOauth2TokenEndpoint() { return oauth2TokenEndpoint; }
        public String getOauth2ClientId() { return oauth2ClientId; }
        public String getOauth2ClientSecret() { return oauth2ClientSecret; }
        public String getOauth2Scope() { return oauth2Scope; }
        public String getApiKey() { return apiKey; }
        public String getApiKeyHeaderName() { return apiKeyHeaderName; }
        public String getBasicAuthUsername() { return basicAuthUsername; }
        public String getBasicAuthPassword() { return basicAuthPassword; }
        public Boolean getIncludeClientHeaders() { return includeClientHeaders; }
        public String getClientId() { return clientId; }
        public String getClientSecret() { return clientSecret; }
        public String getClientIdHeaderName() { return clientIdHeaderName; }
        public String getClientSecretHeaderName() { return clientSecretHeaderName; }
        public String getTargetHost() { return targetHost; }
        public Integer getTargetPort() { return targetPort; }
        public String getTargetProtocol() { return targetProtocol; }
        public String getTargetPath() { return targetPath; }
        public Integer getTimeoutSeconds() { return timeoutSeconds; }
        public Integer getRetryAttempts() { return retryAttempts; }
        public Integer getRetryDelaySeconds() { return retryDelaySeconds; }
        public Map<String, String> getMetadata() { return metadata; }
    }
}