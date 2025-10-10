package com.paymentengine.paymentprocessing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling downstream routing to external services
 * Resolves conflicts when multiple tenants use the same host:port
 * but need different routing based on context
 */
@Service
public class DownstreamRoutingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DownstreamRoutingService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private TenantAuthConfigurationService tenantAuthConfigurationService;
    
    @Value("${downstream.bank-nginx.host:bank-nginx.example.com}")
    private String bankNginxHost;
    
    @Value("${downstream.bank-nginx.port:443}")
    private int bankNginxPort;
    
    @Value("${downstream.bank-nginx.protocol:https}")
    private String bankNginxProtocol;
    
    /**
     * Call fraud system via bank's NGINX
     */
    public <T> ResponseEntity<T> callFraudSystem(String tenantId, Object requestBody, 
                                               Class<T> responseType, Map<String, String> additionalHeaders) {
        logger.info("Calling fraud system for tenant: {}", tenantId);
        
        String url = buildUrl("/fraud");
        HttpHeaders headers = createHeaders(tenantId, "fraud", additionalHeaders);
        
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
            logger.info("Fraud system call successful for tenant: {}", tenantId);
            return response;
        } catch (Exception e) {
            logger.error("Fraud system call failed for tenant: {}", tenantId, e);
            throw new RuntimeException("Fraud system call failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Call clearing system via bank's NGINX
     */
    public <T> ResponseEntity<T> callClearingSystem(String tenantId, Object requestBody, 
                                                  Class<T> responseType, Map<String, String> additionalHeaders) {
        logger.info("Calling clearing system for tenant: {}", tenantId);
        
        String url = buildUrl("/clearing");
        HttpHeaders headers = createHeaders(tenantId, "clearing", additionalHeaders);
        
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
            logger.info("Clearing system call successful for tenant: {}", tenantId);
            return response;
        } catch (Exception e) {
            logger.error("Clearing system call failed for tenant: {}", tenantId, e);
            throw new RuntimeException("Clearing system call failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Call external service with automatic routing based on request content
     */
    public <T> ResponseEntity<T> callExternalService(String tenantId, Object requestBody, 
                                                   Class<T> responseType, Map<String, String> additionalHeaders) {
        logger.info("Calling external service for tenant: {}", tenantId);
        
        // Determine service type based on request content
        String serviceType = determineServiceType(requestBody);
        logger.info("Determined service type: {} for tenant: {}", serviceType, tenantId);
        
        String url = buildUrl("/" + serviceType);
        HttpHeaders headers = createHeaders(tenantId, serviceType, additionalHeaders);
        
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
            logger.info("External service call successful for tenant: {} to service: {}", tenantId, serviceType);
            return response;
        } catch (Exception e) {
            logger.error("External service call failed for tenant: {} to service: {}", tenantId, serviceType, e);
            throw new RuntimeException("External service call failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Call external service with specific service type
     */
    public <T> ResponseEntity<T> callExternalService(String tenantId, String serviceType, Object requestBody, 
                                                   Class<T> responseType, Map<String, String> additionalHeaders) {
        logger.info("Calling external service for tenant: {} with service type: {}", tenantId, serviceType);
        
        String url = buildUrl("/" + serviceType);
        HttpHeaders headers = createHeaders(tenantId, serviceType, additionalHeaders);
        
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
            logger.info("External service call successful for tenant: {} to service: {}", tenantId, serviceType);
            return response;
        } catch (Exception e) {
            logger.error("External service call failed for tenant: {} to service: {}", tenantId, serviceType, e);
            throw new RuntimeException("External service call failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build URL for external service calls
     */
    private String buildUrl(String path) {
        return String.format("%s://%s:%d%s", bankNginxProtocol, bankNginxHost, bankNginxPort, path);
    }
    
    /**
     * Create headers for downstream calls with tenant context
     */
    private HttpHeaders createHeaders(String tenantId, String serviceType, Map<String, String> additionalHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Add tenant context headers
        headers.set("X-Tenant-ID", tenantId);
        headers.set("X-Service-Type", serviceType);
        headers.set("X-Route-Context", tenantId + "-" + serviceType);
        headers.set("X-Downstream-Route", serviceType + "-system");
        headers.set("X-Bank-Route", "/" + serviceType + "/" + tenantId);
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        headers.set("X-Request-Time", String.valueOf(System.currentTimeMillis()));
        
        // Add tenant-specific authentication headers
        tenantAuthConfigurationService.getConfigurationForOutgoingCalls(tenantId)
            .ifPresent(config -> {
                if (config.getIncludeClientHeaders() && config.getClientId() != null) {
                    headers.set(config.getClientIdHeaderName(), config.getClientId());
                }
                if (config.getIncludeClientHeaders() && config.getClientSecret() != null) {
                    headers.set(config.getClientSecretHeaderName(), config.getClientSecret());
                }
            });
        
        // Add additional headers
        if (additionalHeaders != null) {
            additionalHeaders.forEach(headers::set);
        }
        
        return headers;
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
        
        // Default to fraud for unknown content
        return "fraud";
    }
    
    /**
     * Get tenant-specific configuration for downstream calls
     */
    public Map<String, String> getTenantDownstreamConfig(String tenantId) {
        Map<String, String> config = new HashMap<>();
        
        tenantAuthConfigurationService.getConfigurationForOutgoingCalls(tenantId)
            .ifPresent(authConfig -> {
                config.put("tenantId", tenantId);
                config.put("authMethod", authConfig.getAuthMethod().toString());
                config.put("includeClientHeaders", authConfig.getIncludeClientHeaders().toString());
                
                if (authConfig.getClientId() != null) {
                    config.put("clientId", authConfig.getClientId());
                }
                
                if (authConfig.getClientIdHeaderName() != null) {
                    config.put("clientIdHeaderName", authConfig.getClientIdHeaderName());
                }
                
                if (authConfig.getClientSecretHeaderName() != null) {
                    config.put("clientSecretHeaderName", authConfig.getClientSecretHeaderName());
                }
            });
        
        return config;
    }
    
    /**
     * Validate tenant access to external service
     */
    public boolean validateTenantAccess(String tenantId, String serviceType) {
        logger.debug("Validating tenant access for tenant: {} to service: {}", tenantId, serviceType);
        
        // Check if tenant has active configuration
        boolean hasConfig = tenantAuthConfigurationService.hasActiveConfiguration(tenantId);
        
        if (!hasConfig) {
            logger.warn("Tenant {} does not have active configuration", tenantId);
            return false;
        }
        
        // Additional validation logic can be added here
        // For example, check if tenant is allowed to access specific service types
        
        return true;
    }
    
    /**
     * Get downstream service statistics
     */
    public Map<String, Object> getDownstreamStats(String tenantId) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("tenantId", tenantId);
        stats.put("bankNginxHost", bankNginxHost);
        stats.put("bankNginxPort", bankNginxPort);
        stats.put("bankNginxProtocol", bankNginxProtocol);
        stats.put("timestamp", System.currentTimeMillis());
        
        // Add tenant configuration info
        tenantAuthConfigurationService.getConfigurationForOutgoingCalls(tenantId)
            .ifPresent(config -> {
                stats.put("hasActiveConfig", true);
                stats.put("authMethod", config.getAuthMethod().toString());
                stats.put("includeClientHeaders", config.getIncludeClientHeaders());
            });
        
        return stats;
    }
}