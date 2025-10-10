package com.paymentengine.shared.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

/**
 * Shared service client for communicating with Payment Processing Service
 * Used by other services to access multi-level auth, certificate management, and outgoing HTTP functionality
 */
@Service
public class PaymentProcessingServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingServiceClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String PAYMENT_PROCESSING_SERVICE_URL = "http://payment-processing-service";
    
    // Multi-Level Auth Configuration Methods
    
    /**
     * Get resolved authentication configuration
     */
    public Optional<Map<String, Object>> getResolvedAuthConfiguration(
            String tenantId, String serviceType, String endpoint, String paymentType) {
        
        logger.info("Getting resolved auth configuration for tenant: {}, serviceType: {}, endpoint: {}, paymentType: {}", 
                tenantId, serviceType, endpoint, paymentType);
        
        try {
            String url = String.format("%s/api/v1/multi-level-auth/resolve/%s?serviceType=%s&endpoint=%s&paymentType=%s", 
                    PAYMENT_PROCESSING_SERVICE_URL, tenantId, serviceType, endpoint, paymentType);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return Optional.of(response.getBody());
            
        } catch (Exception e) {
            logger.error("Failed to get resolved auth configuration for tenant: {}", tenantId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Validate multi-level auth configuration
     */
    public boolean validateMultiLevelAuthConfiguration(String tenantId) {
        logger.info("Validating multi-level auth configuration for tenant: {}", tenantId);
        
        try {
            String url = String.format("%s/api/v1/multi-level-auth/validate/%s", 
                    PAYMENT_PROCESSING_SERVICE_URL, tenantId);
            
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Failed to validate multi-level auth configuration for tenant: {}", tenantId, e);
            return false;
        }
    }
    
    // Certificate Management Methods
    
    /**
     * Generate certificate
     */
    public Optional<Map<String, Object>> generateCertificate(Map<String, Object> request) {
        logger.info("Generating certificate via Payment Processing Service");
        
        try {
            String url = PAYMENT_PROCESSING_SERVICE_URL + "/api/v1/certificates/generate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return Optional.of(response.getBody());
            
        } catch (Exception e) {
            logger.error("Failed to generate certificate via Payment Processing Service", e);
            return Optional.empty();
        }
    }
    
    /**
     * Import PFX certificate
     */
    public Optional<Map<String, Object>> importPfxCertificate(Map<String, Object> request) {
        logger.info("Importing PFX certificate via Payment Processing Service");
        
        try {
            String url = PAYMENT_PROCESSING_SERVICE_URL + "/api/v1/certificates/import-pfx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return Optional.of(response.getBody());
            
        } catch (Exception e) {
            logger.error("Failed to import PFX certificate via Payment Processing Service", e);
            return Optional.empty();
        }
    }
    
    /**
     * Validate certificate
     */
    public Optional<Map<String, Object>> validateCertificate(String certificateId) {
        logger.info("Validating certificate: {} via Payment Processing Service", certificateId);
        
        try {
            String url = String.format("%s/api/v1/certificates/%s/validate", 
                    PAYMENT_PROCESSING_SERVICE_URL, certificateId);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return Optional.of(response.getBody());
            
        } catch (Exception e) {
            logger.error("Failed to validate certificate: {} via Payment Processing Service", certificateId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Rotate certificate
     */
    public Optional<Map<String, Object>> rotateCertificate(String certificateId) {
        logger.info("Rotating certificate: {} via Payment Processing Service", certificateId);
        
        try {
            String url = String.format("%s/api/v1/certificates/%s/rotate", 
                    PAYMENT_PROCESSING_SERVICE_URL, certificateId);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            return Optional.of(response.getBody());
            
        } catch (Exception e) {
            logger.error("Failed to rotate certificate: {} via Payment Processing Service", certificateId, e);
            return Optional.empty();
        }
    }
    
    // Outgoing HTTP Methods
    
    /**
     * Make outgoing HTTP call with multi-level auth configuration
     */
    public Optional<Map<String, Object>> makeOutgoingHttpCall(Map<String, Object> request) {
        logger.info("Making outgoing HTTP call via Payment Processing Service");
        
        try {
            String url = PAYMENT_PROCESSING_SERVICE_URL + "/api/v1/outgoing-http/call";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return Optional.of(response.getBody());
            
        } catch (Exception e) {
            logger.error("Failed to make outgoing HTTP call via Payment Processing Service", e);
            return Optional.empty();
        }
    }
    
    /**
     * Get client headers for outgoing calls
     */
    public Optional<Map<String, Object>> getClientHeaders(
            String tenantId, String serviceType, String endpoint, String paymentType) {
        
        logger.info("Getting client headers for tenant: {}, serviceType: {}, endpoint: {}, paymentType: {}", 
                tenantId, serviceType, endpoint, paymentType);
        
        try {
            String url = String.format("%s/api/v1/outgoing-http/headers?tenantId=%s&serviceType=%s&endpoint=%s&paymentType=%s", 
                    PAYMENT_PROCESSING_SERVICE_URL, tenantId, serviceType, endpoint, paymentType);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return Optional.of(response.getBody());
            
        } catch (Exception e) {
            logger.error("Failed to get client headers for tenant: {}", tenantId, e);
            return Optional.empty();
        }
    }
    
    // Health Check Methods
    
    /**
     * Check Payment Processing Service health
     */
    public boolean isPaymentProcessingServiceHealthy() {
        logger.info("Checking Payment Processing Service health");
        
        try {
            String url = PAYMENT_PROCESSING_SERVICE_URL + "/actuator/health";
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> health = response.getBody();
            
            return health != null && "UP".equals(health.get("status"));
            
        } catch (Exception e) {
            logger.error("Payment Processing Service health check failed", e);
            return false;
        }
    }
}