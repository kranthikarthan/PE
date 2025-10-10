package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.entity.TenantAuthConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling outgoing HTTP calls with configurable authentication and client headers
 */
@Service
public class OutgoingHttpService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutgoingHttpService.class);
    
    @Autowired
    private TenantAuthConfigurationService tenantAuthConfigurationService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Make HTTP GET request with tenant-specific authentication and client headers
     */
    public <T> ResponseEntity<T> get(String tenantId, String url, Class<T> responseType) {
        return makeRequest(tenantId, HttpMethod.GET, url, null, responseType);
    }
    
    /**
     * Make HTTP POST request with tenant-specific authentication and client headers
     */
    public <T> ResponseEntity<T> post(String tenantId, String url, Object requestBody, Class<T> responseType) {
        return makeRequest(tenantId, HttpMethod.POST, url, requestBody, responseType);
    }
    
    /**
     * Make HTTP PUT request with tenant-specific authentication and client headers
     */
    public <T> ResponseEntity<T> put(String tenantId, String url, Object requestBody, Class<T> responseType) {
        return makeRequest(tenantId, HttpMethod.PUT, url, requestBody, responseType);
    }
    
    /**
     * Make HTTP DELETE request with tenant-specific authentication and client headers
     */
    public <T> ResponseEntity<T> delete(String tenantId, String url, Class<T> responseType) {
        return makeRequest(tenantId, HttpMethod.DELETE, url, null, responseType);
    }
    
    /**
     * Make HTTP request with tenant-specific authentication and client headers
     */
    public <T> ResponseEntity<T> makeRequest(String tenantId, HttpMethod method, String url, Object requestBody, Class<T> responseType) {
        logger.debug("Making {} request to {} for tenant: {}", method, url, tenantId);
        
        // Get tenant authentication configuration
        Optional<TenantAuthConfiguration> authConfig = tenantAuthConfigurationService.getConfigurationForOutgoingCalls(tenantId);
        
        // Create headers
        HttpHeaders headers = createHeaders(authConfig);
        
        // Create request entity
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
        
        // Make the request
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
            logger.debug("Request successful for tenant: {} to URL: {}", tenantId, url);
            return response;
        } catch (Exception e) {
            logger.error("Request failed for tenant: {} to URL: {}", tenantId, url, e);
            throw new RuntimeException("HTTP request failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Make HTTP request with custom headers
     */
    public <T> ResponseEntity<T> makeRequestWithCustomHeaders(String tenantId, HttpMethod method, String url, 
                                                             Object requestBody, Map<String, String> customHeaders, 
                                                             Class<T> responseType) {
        logger.debug("Making {} request to {} for tenant: {} with custom headers", method, url, tenantId);
        
        // Get tenant authentication configuration
        Optional<TenantAuthConfiguration> authConfig = tenantAuthConfigurationService.getConfigurationForOutgoingCalls(tenantId);
        
        // Create headers
        HttpHeaders headers = createHeaders(authConfig);
        
        // Add custom headers
        if (customHeaders != null) {
            customHeaders.forEach(headers::set);
        }
        
        // Create request entity
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
        
        // Make the request
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
            logger.debug("Request with custom headers successful for tenant: {} to URL: {}", tenantId, url);
            return response;
        } catch (Exception e) {
            logger.error("Request with custom headers failed for tenant: {} to URL: {}", tenantId, url, e);
            throw new RuntimeException("HTTP request with custom headers failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create HTTP headers based on tenant authentication configuration
     */
    private HttpHeaders createHeaders(Optional<TenantAuthConfiguration> authConfig) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (authConfig.isPresent()) {
            TenantAuthConfiguration config = authConfig.get();
            
            // Add client headers if configured
            if (config.getIncludeClientHeaders() && config.getClientId() != null) {
                headers.set(config.getClientIdHeaderName(), config.getClientId());
                logger.debug("Added client ID header: {} = {}", config.getClientIdHeaderName(), config.getClientId());
            }
            
            if (config.getIncludeClientHeaders() && config.getClientSecret() != null) {
                headers.set(config.getClientSecretHeaderName(), config.getClientSecret());
                logger.debug("Added client secret header: {}", config.getClientSecretHeaderName());
            }
            
            // Add authentication header based on method
            switch (config.getAuthMethod()) {
                case JWT:
                case JWS:
                    // For JWT/JWS, we would typically get a token from the token endpoint
                    // and add it to the Authorization header
                    String token = getTokenForAuth(config);
                    if (token != null) {
                        String authHeader = config.getAuthHeaderPrefix() + " " + token;
                        headers.set(config.getAuthHeaderName(), authHeader);
                        logger.debug("Added authentication header: {}", config.getAuthHeaderName());
                    }
                    break;
                case API_KEY:
                    // For API key, add it as a custom header
                    if (config.getClientId() != null) {
                        headers.set("X-API-Key", config.getClientId());
                        logger.debug("Added API key header");
                    }
                    break;
                case BASIC:
                    // For basic auth, encode client ID and secret
                    if (config.getClientId() != null && config.getClientSecret() != null) {
                        String credentials = config.getClientId() + ":" + config.getClientSecret();
                        String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
                        headers.set("Authorization", "Basic " + encodedCredentials);
                        logger.debug("Added basic authentication header");
                    }
                    break;
                case OAUTH2:
                    // For OAuth2, get access token from token endpoint
                    String oauthToken = getOAuth2Token(config);
                    if (oauthToken != null) {
                        headers.set("Authorization", "Bearer " + oauthToken);
                        logger.debug("Added OAuth2 authentication header");
                    }
                    break;
            }
        }
        
        return headers;
    }
    
    /**
     * Get token for JWT/JWS authentication
     */
    private String getTokenForAuth(TenantAuthConfiguration config) {
        // This would typically call the token endpoint to get a fresh token
        // For now, we'll return null and let the calling code handle token management
        logger.debug("Token retrieval for JWT/JWS authentication not implemented yet");
        return null;
    }
    
    /**
     * Get OAuth2 access token
     */
    private String getOAuth2Token(TenantAuthConfiguration config) {
        // This would typically call the OAuth2 token endpoint
        // For now, we'll return null and let the calling code handle token management
        logger.debug("OAuth2 token retrieval not implemented yet");
        return null;
    }
    
    /**
     * Create a RestTemplate with custom configuration
     */
    public RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
    
    /**
     * Get tenant authentication configuration for outgoing calls
     */
    public Optional<TenantAuthConfiguration> getTenantAuthConfiguration(String tenantId) {
        return tenantAuthConfigurationService.getConfigurationForOutgoingCalls(tenantId);
    }
    
    /**
     * Check if tenant has client headers configured
     */
    public boolean hasClientHeaders(String tenantId) {
        Optional<TenantAuthConfiguration> config = tenantAuthConfigurationService.getConfigurationForOutgoingCalls(tenantId);
        return config.isPresent() && config.get().getIncludeClientHeaders();
    }
    
    /**
     * Get client headers for tenant
     */
    public Map<String, String> getClientHeaders(String tenantId) {
        Map<String, String> headers = new HashMap<>();
        
        Optional<TenantAuthConfiguration> config = tenantAuthConfigurationService.getConfigurationForOutgoingCalls(tenantId);
        if (config.isPresent() && config.get().getIncludeClientHeaders()) {
            TenantAuthConfiguration authConfig = config.get();
            
            if (authConfig.getClientId() != null) {
                headers.put(authConfig.getClientIdHeaderName(), authConfig.getClientId());
            }
            
            if (authConfig.getClientSecret() != null) {
                headers.put(authConfig.getClientSecretHeaderName(), authConfig.getClientSecret());
            }
        }
        
        return headers;
    }
}