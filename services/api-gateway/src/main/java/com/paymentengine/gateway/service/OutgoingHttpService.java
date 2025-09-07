package com.paymentengine.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling outgoing HTTP calls with client headers in API Gateway
 * Integrates with multi-level auth configuration for dynamic header management
 */
@Service
@Transactional
public class OutgoingHttpService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutgoingHttpService.class);
    
    @Autowired
    private MultiLevelAuthConfigurationService multiLevelAuthConfigurationService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Make outgoing HTTP call with client headers based on multi-level auth configuration
     */
    public <T> ResponseEntity<T> makeOutgoingCall(
            String tenantId,
            String serviceType,
            String endpoint,
            String paymentType,
            HttpMethod method,
            URI uri,
            Object requestBody,
            Class<T> responseType) {
        
        logger.info("Making outgoing HTTP call for tenant: {}, serviceType: {}, endpoint: {}, paymentType: {}", 
                tenantId, serviceType, endpoint, paymentType);
        
        try {
            // Resolve authentication configuration
            Optional<Map<String, Object>> resolvedConfig = multiLevelAuthConfigurationService
                    .getConfigurationHierarchy(tenantId, serviceType, endpoint, paymentType);
            
            HttpHeaders headers = new HttpHeaders();
            
            if (resolvedConfig.isPresent()) {
                Map<String, Object> config = resolvedConfig.get();
                Map<String, Object> clientHeaders = (Map<String, Object>) config.get("clientHeaders");
                
                if (clientHeaders != null && (Boolean) clientHeaders.get("includeClientHeaders")) {
                    // Add client ID header
                    String clientId = (String) clientHeaders.get("clientId");
                    String clientIdHeaderName = (String) clientHeaders.get("clientIdHeaderName");
                    if (clientId != null && clientIdHeaderName != null) {
                        headers.set(clientIdHeaderName, clientId);
                    }
                    
                    // Add client secret header
                    String clientSecret = (String) clientHeaders.get("clientSecret");
                    String clientSecretHeaderName = (String) clientHeaders.get("clientSecretHeaderName");
                    if (clientSecret != null && clientSecretHeaderName != null) {
                        headers.set(clientSecretHeaderName, clientSecret);
                    }
                }
            }
            
            // Create request entity
            RequestEntity<Object> requestEntity = new RequestEntity<>(requestBody, headers, method, uri);
            
            // Make the call
            ResponseEntity<T> response = restTemplate.exchange(requestEntity, responseType);
            
            logger.info("Successfully made outgoing HTTP call for tenant: {}", tenantId);
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to make outgoing HTTP call for tenant: {}", tenantId, e);
            throw new RuntimeException("Failed to make outgoing HTTP call", e);
        }
    }
    
    /**
     * Make GET request with client headers
     */
    public <T> ResponseEntity<T> get(
            String tenantId,
            String serviceType,
            String endpoint,
            String paymentType,
            URI uri,
            Class<T> responseType) {
        
        return makeOutgoingCall(tenantId, serviceType, endpoint, paymentType, 
                HttpMethod.GET, uri, null, responseType);
    }
    
    /**
     * Make POST request with client headers
     */
    public <T> ResponseEntity<T> post(
            String tenantId,
            String serviceType,
            String endpoint,
            String paymentType,
            URI uri,
            Object requestBody,
            Class<T> responseType) {
        
        return makeOutgoingCall(tenantId, serviceType, endpoint, paymentType, 
                HttpMethod.POST, uri, requestBody, responseType);
    }
    
    /**
     * Make PUT request with client headers
     */
    public <T> ResponseEntity<T> put(
            String tenantId,
            String serviceType,
            String endpoint,
            String paymentType,
            URI uri,
            Object requestBody,
            Class<T> responseType) {
        
        return makeOutgoingCall(tenantId, serviceType, endpoint, paymentType, 
                HttpMethod.PUT, uri, requestBody, responseType);
    }
    
    /**
     * Make DELETE request with client headers
     */
    public <T> ResponseEntity<T> delete(
            String tenantId,
            String serviceType,
            String endpoint,
            String paymentType,
            URI uri,
            Class<T> responseType) {
        
        return makeOutgoingCall(tenantId, serviceType, endpoint, paymentType, 
                HttpMethod.DELETE, uri, null, responseType);
    }
    
    /**
     * Get client headers for a specific context
     */
    public Optional<Map<String, String>> getClientHeaders(
            String tenantId,
            String serviceType,
            String endpoint,
            String paymentType) {
        
        logger.info("Getting client headers for tenant: {}, serviceType: {}, endpoint: {}, paymentType: {}", 
                tenantId, serviceType, endpoint, paymentType);
        
        try {
            // Resolve authentication configuration
            Optional<Map<String, Object>> resolvedConfig = multiLevelAuthConfigurationService
                    .getConfigurationHierarchy(tenantId, serviceType, endpoint, paymentType);
            
            if (resolvedConfig.isPresent()) {
                Map<String, Object> config = resolvedConfig.get();
                Map<String, Object> clientHeaders = (Map<String, Object>) config.get("clientHeaders");
                
                if (clientHeaders != null && (Boolean) clientHeaders.get("includeClientHeaders")) {
                    Map<String, String> headers = new HashMap<>();
                    
                    // Add client ID header
                    String clientId = (String) clientHeaders.get("clientId");
                    String clientIdHeaderName = (String) clientHeaders.get("clientIdHeaderName");
                    if (clientId != null && clientIdHeaderName != null) {
                        headers.put(clientIdHeaderName, clientId);
                    }
                    
                    // Add client secret header
                    String clientSecret = (String) clientHeaders.get("clientSecret");
                    String clientSecretHeaderName = (String) clientHeaders.get("clientSecretHeaderName");
                    if (clientSecret != null && clientSecretHeaderName != null) {
                        headers.put(clientSecretHeaderName, clientSecret);
                    }
                    
                    return Optional.of(headers);
                }
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Failed to get client headers for tenant: {}", tenantId, e);
            return Optional.empty();
        }
    }
}