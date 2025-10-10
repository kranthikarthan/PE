package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.service.MultiLevelAuthConfigurationService.ResolvedAuthConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced downstream routing service with multi-level authentication configuration
 * Supports configuration at clearing system, tenant, payment type, and downstream call levels
 */
@Service
public class EnhancedDownstreamRoutingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedDownstreamRoutingService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private MultiLevelAuthConfigurationService multiLevelAuthConfigService;
    
    /**
     * Call external service with multi-level configuration resolution
     */
    public <T> ResponseEntity<T> callExternalService(String tenantId, String serviceType, String endpoint, 
                                                   String paymentType, Object requestBody, 
                                                   Class<T> responseType, Map<String, String> additionalHeaders) {
        logger.info("Calling external service with multi-level config - tenant: {}, service: {}, endpoint: {}, paymentType: {}", 
                   tenantId, serviceType, endpoint, paymentType);
        
        // Resolve configuration from all levels
        ResolvedAuthConfiguration config = multiLevelAuthConfigService.getResolvedConfiguration(
            tenantId, serviceType, endpoint, paymentType);
        
        // Build URL from resolved configuration
        String url = buildUrl(config);
        
        // Create headers with resolved configuration
        HttpHeaders headers = createHeaders(tenantId, serviceType, endpoint, paymentType, config, additionalHeaders);
        
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
            logger.info("External service call successful for tenant: {} to service: {}:{}", tenantId, serviceType, endpoint);
            return response;
        } catch (Exception e) {
            logger.error("External service call failed for tenant: {} to service: {}:{}", tenantId, serviceType, endpoint, e);
            throw new RuntimeException("External service call failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Call fraud system with multi-level configuration
     */
    public <T> ResponseEntity<T> callFraudSystem(String tenantId, String paymentType, Object requestBody, 
                                               Class<T> responseType, Map<String, String> additionalHeaders) {
        return callExternalService(tenantId, "fraud", "/fraud", paymentType, requestBody, responseType, additionalHeaders);
    }
    
    /**
     * Call clearing system with multi-level configuration
     */
    public <T> ResponseEntity<T> callClearingSystem(String tenantId, String paymentType, Object requestBody, 
                                                  Class<T> responseType, Map<String, String> additionalHeaders) {
        return callExternalService(tenantId, "clearing", "/clearing", paymentType, requestBody, responseType, additionalHeaders);
    }
    
    /**
     * Call banking system with multi-level configuration
     */
    public <T> ResponseEntity<T> callBankingSystem(String tenantId, String paymentType, Object requestBody, 
                                                 Class<T> responseType, Map<String, String> additionalHeaders) {
        return callExternalService(tenantId, "banking", "/banking", paymentType, requestBody, responseType, additionalHeaders);
    }
    
    /**
     * Call external service with automatic service type detection
     */
    public <T> ResponseEntity<T> callExternalServiceAuto(String tenantId, String paymentType, Object requestBody, 
                                                       Class<T> responseType, Map<String, String> additionalHeaders) {
        String serviceType = determineServiceType(requestBody);
        String endpoint = determineEndpoint(serviceType, requestBody);
        
        logger.info("Auto-detected service type: {} and endpoint: {} for tenant: {}", serviceType, endpoint, tenantId);
        
        return callExternalService(tenantId, serviceType, endpoint, paymentType, requestBody, responseType, additionalHeaders);
    }
    
    /**
     * Build URL from resolved configuration
     */
    private String buildUrl(ResolvedAuthConfiguration config) {
        String protocol = config.getTargetProtocol() != null ? config.getTargetProtocol() : "https";
        String host = config.getTargetHost() != null ? config.getTargetHost() : "bank-nginx.example.com";
        Integer port = config.getTargetPort() != null ? config.getTargetPort() : 443;
        String path = config.getTargetPath() != null ? config.getTargetPath() : "";
        
        return String.format("%s://%s:%d%s", protocol, host, port, path);
    }
    
    /**
     * Create headers with resolved configuration
     */
    private HttpHeaders createHeaders(String tenantId, String serviceType, String endpoint, String paymentType,
                                    ResolvedAuthConfiguration config, Map<String, String> additionalHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Add tenant context headers
        headers.set("X-Tenant-ID", tenantId);
        headers.set("X-Service-Type", serviceType);
        headers.set("X-Endpoint", endpoint);
        if (paymentType != null) {
            headers.set("X-Payment-Type", paymentType);
        }
        headers.set("X-Route-Context", tenantId + "-" + serviceType + "-" + endpoint);
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        headers.set("X-Request-Time", String.valueOf(System.currentTimeMillis()));
        
        // Add authentication headers based on resolved configuration
        addAuthenticationHeaders(headers, config);
        
        // Add client headers if configured
        if (config.getIncludeClientHeaders() && config.getClientId() != null) {
            String clientIdHeaderName = config.getClientIdHeaderName() != null ? 
                config.getClientIdHeaderName() : "X-Client-ID";
            headers.set(clientIdHeaderName, config.getClientId());
        }
        
        if (config.getIncludeClientHeaders() && config.getClientSecret() != null) {
            String clientSecretHeaderName = config.getClientSecretHeaderName() != null ? 
                config.getClientSecretHeaderName() : "X-Client-Secret";
            headers.set(clientSecretHeaderName, config.getClientSecret());
        }
        
        // Add additional headers
        if (additionalHeaders != null) {
            additionalHeaders.forEach(headers::set);
        }
        
        // Add configuration metadata
        if (config.getMetadata() != null) {
            config.getMetadata().forEach((key, value) -> 
                headers.set("X-Config-" + key, value));
        }
        
        return headers;
    }
    
    /**
     * Add authentication headers based on resolved configuration
     */
    private void addAuthenticationHeaders(HttpHeaders headers, ResolvedAuthConfiguration config) {
        if (config.getAuthMethod() == null) {
            return;
        }
        
        switch (config.getAuthMethod()) {
            case JWT:
                addJwtHeaders(headers, config);
                break;
            case JWS:
                addJwsHeaders(headers, config);
                break;
            case OAUTH2:
                addOAuth2Headers(headers, config);
                break;
            case API_KEY:
                addApiKeyHeaders(headers, config);
                break;
            case BASIC:
                addBasicAuthHeaders(headers, config);
                break;
        }
    }
    
    /**
     * Add JWT authentication headers
     */
    private void addJwtHeaders(HttpHeaders headers, ResolvedAuthConfiguration config) {
        headers.set("X-Auth-Method", "JWT");
        if (config.getJwtIssuer() != null) {
            headers.set("X-JWT-Issuer", config.getJwtIssuer());
        }
        if (config.getJwtAudience() != null) {
            headers.set("X-JWT-Audience", config.getJwtAudience());
        }
        if (config.getJwtExpirationSeconds() != null) {
            headers.set("X-JWT-Expiration", config.getJwtExpirationSeconds().toString());
        }
        // Note: JWT secret is not sent in headers for security
    }
    
    /**
     * Add JWS authentication headers
     */
    private void addJwsHeaders(HttpHeaders headers, ResolvedAuthConfiguration config) {
        headers.set("X-Auth-Method", "JWS");
        if (config.getJwsAlgorithm() != null) {
            headers.set("X-JWS-Algorithm", config.getJwsAlgorithm());
        }
        if (config.getJwsIssuer() != null) {
            headers.set("X-JWS-Issuer", config.getJwsIssuer());
        }
        if (config.getJwsAudience() != null) {
            headers.set("X-JWS-Audience", config.getJwsAudience());
        }
        if (config.getJwsExpirationSeconds() != null) {
            headers.set("X-JWS-Expiration", config.getJwsExpirationSeconds().toString());
        }
        // Note: JWS secret is not sent in headers for security
    }
    
    /**
     * Add OAuth2 authentication headers
     */
    private void addOAuth2Headers(HttpHeaders headers, ResolvedAuthConfiguration config) {
        headers.set("X-Auth-Method", "OAUTH2");
        if (config.getOauth2TokenEndpoint() != null) {
            headers.set("X-OAuth2-Token-Endpoint", config.getOauth2TokenEndpoint());
        }
        if (config.getOauth2ClientId() != null) {
            headers.set("X-OAuth2-Client-ID", config.getOauth2ClientId());
        }
        if (config.getOauth2Scope() != null) {
            headers.set("X-OAuth2-Scope", config.getOauth2Scope());
        }
        // Note: OAuth2 client secret is not sent in headers for security
    }
    
    /**
     * Add API Key authentication headers
     */
    private void addApiKeyHeaders(HttpHeaders headers, ResolvedAuthConfiguration config) {
        headers.set("X-Auth-Method", "API_KEY");
        if (config.getApiKey() != null && config.getApiKeyHeaderName() != null) {
            headers.set(config.getApiKeyHeaderName(), config.getApiKey());
        }
    }
    
    /**
     * Add Basic Auth headers
     */
    private void addBasicAuthHeaders(HttpHeaders headers, ResolvedAuthConfiguration config) {
        headers.set("X-Auth-Method", "BASIC");
        if (config.getBasicAuthUsername() != null) {
            headers.set("X-Basic-Auth-Username", config.getBasicAuthUsername());
        }
        // Note: Basic auth password is not sent in headers for security
    }
    
    /**
     * Determine service type based on request content
     */
    private String determineServiceType(Object requestBody) {
        if (requestBody == null) {
            return "default";
        }
        
        String requestStr = requestBody.toString().toLowerCase();
        
        // Check for fraud-related keywords
        if (requestStr.contains("fraud") || requestStr.contains("risk") || 
            requestStr.contains("suspicious") || requestStr.contains("assessment")) {
            return "fraud";
        }
        
        // Check for clearing-related keywords
        if (requestStr.contains("clearing") || requestStr.contains("settlement") || 
            requestStr.contains("payment") || requestStr.contains("instruction")) {
            return "clearing";
        }
        
        // Check for banking-related keywords
        if (requestStr.contains("banking") || requestStr.contains("account") || 
            requestStr.contains("balance") || requestStr.contains("transfer")) {
            return "banking";
        }
        
        // Default to clearing for unknown content
        return "clearing";
    }
    
    /**
     * Determine endpoint based on service type and request content
     */
    private String determineEndpoint(String serviceType, Object requestBody) {
        if (requestBody == null) {
            return "/" + serviceType;
        }
        
        String requestStr = requestBody.toString().toLowerCase();
        
        // Determine specific endpoint based on content
        if (serviceType.equals("fraud")) {
            if (requestStr.contains("check")) {
                return "/fraud/check";
            } else if (requestStr.contains("assessment")) {
                return "/fraud/assessment";
            } else {
                return "/fraud";
            }
        } else if (serviceType.equals("clearing")) {
            if (requestStr.contains("settlement")) {
                return "/clearing/settlement";
            } else if (requestStr.contains("instruction")) {
                return "/clearing/instruction";
            } else {
                return "/clearing";
            }
        } else if (serviceType.equals("banking")) {
            if (requestStr.contains("balance")) {
                return "/banking/balance";
            } else if (requestStr.contains("transfer")) {
                return "/banking/transfer";
            } else {
                return "/banking";
            }
        }
        
        return "/" + serviceType;
    }
    
    /**
     * Get resolved configuration for debugging
     */
    public ResolvedAuthConfiguration getResolvedConfiguration(String tenantId, String serviceType, 
                                                           String endpoint, String paymentType) {
        return multiLevelAuthConfigService.getResolvedConfiguration(tenantId, serviceType, endpoint, paymentType);
    }
    
    /**
     * Validate tenant access to external service
     */
    public boolean validateTenantAccess(String tenantId, String serviceType, String endpoint, String paymentType) {
        logger.debug("Validating tenant access for tenant: {} to service: {}:{} with paymentType: {}", 
                    tenantId, serviceType, endpoint, paymentType);
        
        try {
            ResolvedAuthConfiguration config = multiLevelAuthConfigService.getResolvedConfiguration(
                tenantId, serviceType, endpoint, paymentType);
            
            // Check if we have a valid configuration
            if (config.getAuthMethod() == null) {
                logger.warn("No authentication method configured for tenant: {} to service: {}:{}", 
                           tenantId, serviceType, endpoint);
                return false;
            }
            
            // Additional validation logic can be added here
            return true;
        } catch (Exception e) {
            logger.error("Error validating tenant access for tenant: {} to service: {}:{}", 
                        tenantId, serviceType, endpoint, e);
            return false;
        }
    }
    
    /**
     * Get downstream service statistics
     */
    public Map<String, Object> getDownstreamStats(String tenantId, String serviceType, String endpoint, String paymentType) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("tenantId", tenantId);
        stats.put("serviceType", serviceType);
        stats.put("endpoint", endpoint);
        stats.put("paymentType", paymentType);
        stats.put("timestamp", System.currentTimeMillis());
        
        try {
            ResolvedAuthConfiguration config = multiLevelAuthConfigService.getResolvedConfiguration(
                tenantId, serviceType, endpoint, paymentType);
            
            stats.put("authMethod", config.getAuthMethod());
            stats.put("includeClientHeaders", config.getIncludeClientHeaders());
            stats.put("targetHost", config.getTargetHost());
            stats.put("targetPort", config.getTargetPort());
            stats.put("targetProtocol", config.getTargetProtocol());
            stats.put("timeoutSeconds", config.getTimeoutSeconds());
            stats.put("retryAttempts", config.getRetryAttempts());
        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
}