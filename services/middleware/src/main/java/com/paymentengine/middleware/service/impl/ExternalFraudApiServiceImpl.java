package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.entity.FraudRiskAssessment;
import com.paymentengine.middleware.service.ExternalFraudApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of external fraud API service
 */
@Service
public class ExternalFraudApiServiceImpl implements ExternalFraudApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalFraudApiServiceImpl.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public Map<String, Object> buildApiRequest(
            Map<String, Object> apiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        logger.debug("Building API request for external fraud service: {}", apiConfig.get("apiName"));
        
        try {
            String apiName = (String) apiConfig.get("apiName");
            Map<String, Object> requestTemplate = (Map<String, Object>) apiConfig.get("requestTemplate");
            
            Map<String, Object> apiRequest = new HashMap<>();
            
            // Build request based on API type
            switch (apiName != null ? apiName.toUpperCase() : "GENERIC") {
                case "FICO_FALCON":
                    apiRequest = buildFicoFalconRequest(apiConfig, paymentData, assessment);
                    break;
                case "SAS_FRAUD_MANAGEMENT":
                    apiRequest = buildSasFraudManagementRequest(apiConfig, paymentData, assessment);
                    break;
                case "EXPERIAN_FRAUD_DETECTION":
                    apiRequest = buildExperianFraudDetectionRequest(apiConfig, paymentData, assessment);
                    break;
                case "THREATMETRIX":
                    apiRequest = buildThreatMetrixRequest(apiConfig, paymentData, assessment);
                    break;
                case "FORTER":
                    apiRequest = buildForterRequest(apiConfig, paymentData, assessment);
                    break;
                case "SIGNIFYD":
                    apiRequest = buildSignifydRequest(apiConfig, paymentData, assessment);
                    break;
                case "GENERIC":
                default:
                    apiRequest = buildGenericRequest(apiConfig, paymentData, assessment);
                    break;
            }
            
            // Add common fields
            apiRequest.put("requestId", assessment.getAssessmentId());
            apiRequest.put("timestamp", LocalDateTime.now().toString());
            apiRequest.put("tenantId", assessment.getTenantId());
            apiRequest.put("transactionReference", assessment.getTransactionReference());
            
            logger.debug("Built API request for external fraud service: {}", apiName);
            return apiRequest;
            
        } catch (Exception e) {
            logger.error("Error building API request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to build API request", e);
        }
    }
    
    @Override
    public Map<String, Object> callExternalApi(
            Map<String, Object> apiConfig,
            Map<String, Object> apiRequest) {
        
        logger.info("Calling external fraud API: {}", apiConfig.get("apiName"));
        
        try {
            String apiUrl = (String) apiConfig.get("apiUrl");
            String apiName = (String) apiConfig.get("apiName");
            String httpMethod = (String) apiConfig.getOrDefault("httpMethod", "POST");
            Map<String, String> headers = (Map<String, String>) apiConfig.getOrDefault("headers", new HashMap<>());
            Map<String, Object> authConfig = (Map<String, Object>) apiConfig.get("authentication");
            
            // Build HTTP headers
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            // Add custom headers
            headers.forEach(httpHeaders::set);
            
            // Add authentication headers
            if (authConfig != null) {
                addAuthenticationHeaders(httpHeaders, authConfig);
            }
            
            // Create HTTP entity
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(apiRequest, httpHeaders);
            
            // Make API call
            ResponseEntity<Map> response;
            if ("GET".equalsIgnoreCase(httpMethod)) {
                response = restTemplate.exchange(apiUrl, HttpMethod.GET, httpEntity, Map.class);
            } else {
                response = restTemplate.exchange(apiUrl, HttpMethod.POST, httpEntity, Map.class);
            }
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                logger.info("Successfully called external fraud API: {}, status: {}", apiName, response.getStatusCode());
                return responseBody != null ? responseBody : new HashMap<>();
            } else {
                logger.error("External fraud API call failed: {}, status: {}", apiName, response.getStatusCode());
                throw new RuntimeException("External fraud API call failed with status: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            logger.error("Error calling external fraud API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call external fraud API", e);
        } catch (Exception e) {
            logger.error("Unexpected error calling external fraud API: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error calling external fraud API", e);
        }
    }
    
    @Override
    public boolean validateApiConfig(Map<String, Object> apiConfig) {
        try {
            // Required fields validation
            if (apiConfig == null || apiConfig.isEmpty()) {
                return false;
            }
            
            String apiUrl = (String) apiConfig.get("apiUrl");
            String apiName = (String) apiConfig.get("apiName");
            
            if (apiUrl == null || apiUrl.trim().isEmpty()) {
                logger.warn("API URL is required");
                return false;
            }
            
            if (apiName == null || apiName.trim().isEmpty()) {
                logger.warn("API name is required");
                return false;
            }
            
            // Validate URL format
            try {
                new java.net.URL(apiUrl);
            } catch (Exception e) {
                logger.warn("Invalid API URL format: {}", apiUrl);
                return false;
            }
            
            // Validate authentication configuration
            Map<String, Object> authConfig = (Map<String, Object>) apiConfig.get("authentication");
            if (authConfig != null) {
                String authType = (String) authConfig.get("type");
                if (authType == null || authType.trim().isEmpty()) {
                    logger.warn("Authentication type is required when authentication is configured");
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error validating API configuration: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean testApiConnectivity(Map<String, Object> apiConfig) {
        try {
            String apiUrl = (String) apiConfig.get("apiUrl");
            String apiName = (String) apiConfig.get("apiName");
            
            logger.info("Testing connectivity to external fraud API: {}", apiName);
            
            // Create a simple test request
            Map<String, Object> testRequest = Map.of(
                    "test", true,
                    "timestamp", LocalDateTime.now().toString()
            );
            
            // Make a test call
            Map<String, Object> response = callExternalApi(apiConfig, testRequest);
            
            logger.info("Successfully tested connectivity to external fraud API: {}", apiName);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to test connectivity to external fraud API: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getSupportedApis() {
        Map<String, Object> supportedApis = new HashMap<>();
        
        // FICO Falcon
        Map<String, Object> ficoFalcon = new HashMap<>();
        ficoFalcon.put("name", "FICO_FALCON");
        ficoFalcon.put("description", "FICO Falcon Fraud Manager");
        ficoFalcon.put("version", "6.0");
        ficoFalcon.put("capabilities", Arrays.asList("real-time", "batch", "machine-learning"));
        supportedApis.put("FICO_FALCON", ficoFalcon);
        
        // SAS Fraud Management
        Map<String, Object> sasFraud = new HashMap<>();
        sasFraud.put("name", "SAS_FRAUD_MANAGEMENT");
        sasFraud.put("description", "SAS Fraud Management");
        sasFraud.put("version", "8.5");
        sasFraud.put("capabilities", Arrays.asList("real-time", "batch", "analytics"));
        supportedApis.put("SAS_FRAUD_MANAGEMENT", sasFraud);
        
        // Experian Fraud Detection
        Map<String, Object> experian = new HashMap<>();
        experian.put("name", "EXPERIAN_FRAUD_DETECTION");
        experian.put("description", "Experian Fraud Detection");
        experian.put("version", "3.0");
        experian.put("capabilities", Arrays.asList("real-time", "identity-verification"));
        supportedApis.put("EXPERIAN_FRAUD_DETECTION", experian);
        
        // ThreatMetrix
        Map<String, Object> threatMetrix = new HashMap<>();
        threatMetrix.put("name", "THREATMETRIX");
        threatMetrix.put("description", "ThreatMetrix Digital Identity");
        threatMetrix.put("version", "2.0");
        threatMetrix.put("capabilities", Arrays.asList("real-time", "device-fingerprinting"));
        supportedApis.put("THREATMETRIX", threatMetrix);
        
        // Forter
        Map<String, Object> forter = new HashMap<>();
        forter.put("name", "FORTER");
        forter.put("description", "Forter Fraud Prevention");
        forter.put("version", "1.0");
        forter.put("capabilities", Arrays.asList("real-time", "e-commerce"));
        supportedApis.put("FORTER", forter);
        
        // Signifyd
        Map<String, Object> signifyd = new HashMap<>();
        signifyd.put("name", "SIGNIFYD");
        signifyd.put("description", "Signifyd Fraud Protection");
        signifyd.put("version", "1.0");
        signifyd.put("capabilities", Arrays.asList("real-time", "e-commerce", "chargeback-protection"));
        supportedApis.put("SIGNIFYD", signifyd);
        
        return supportedApis;
    }
    
    @Override
    public Map<String, Object> getApiHealthStatus(String apiName) {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // This would typically check the health of the specific API
            // For now, return a mock status
            health.put("apiName", apiName);
            health.put("status", "UP");
            health.put("responseTime", 150);
            health.put("lastChecked", LocalDateTime.now());
            health.put("version", "1.0");
            
        } catch (Exception e) {
            health.put("apiName", apiName);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("lastChecked", LocalDateTime.now());
        }
        
        return health;
    }
    
    /**
     * Build FICO Falcon API request
     */
    private Map<String, Object> buildFicoFalconRequest(
            Map<String, Object> apiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        Map<String, Object> request = new HashMap<>();
        
        // Transaction details
        request.put("transactionId", assessment.getTransactionReference());
        request.put("amount", paymentData.get("amount"));
        request.put("currency", paymentData.get("currency"));
        request.put("timestamp", LocalDateTime.now().toString());
        
        // Account information
        request.put("accountNumber", paymentData.get("fromAccountNumber"));
        request.put("beneficiaryAccount", paymentData.get("toAccountNumber"));
        
        // Payment type
        request.put("paymentType", assessment.getPaymentType());
        request.put("localInstrumentCode", assessment.getLocalInstrumentationCode());
        
        // Additional FICO-specific fields
        request.put("channel", "API");
        request.put("deviceId", paymentData.getOrDefault("deviceId", "unknown"));
        request.put("ipAddress", paymentData.getOrDefault("ipAddress", "unknown"));
        
        return request;
    }
    
    /**
     * Build SAS Fraud Management API request
     */
    private Map<String, Object> buildSasFraudManagementRequest(
            Map<String, Object> apiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        Map<String, Object> request = new HashMap<>();
        
        // Transaction details
        request.put("transactionId", assessment.getTransactionReference());
        request.put("amount", paymentData.get("amount"));
        request.put("currency", paymentData.get("currency"));
        request.put("transactionDate", LocalDateTime.now().toString());
        
        // Customer information
        request.put("customerId", paymentData.get("customerId"));
        request.put("accountNumber", paymentData.get("fromAccountNumber"));
        request.put("beneficiaryAccount", paymentData.get("toAccountNumber"));
        
        // Payment details
        request.put("paymentType", assessment.getPaymentType());
        request.put("clearingSystem", assessment.getClearingSystemCode());
        
        // Risk factors
        request.put("riskFactors", paymentData.getOrDefault("riskFactors", new HashMap<>()));
        
        return request;
    }
    
    /**
     * Build Experian Fraud Detection API request
     */
    private Map<String, Object> buildExperianFraudDetectionRequest(
            Map<String, Object> apiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        Map<String, Object> request = new HashMap<>();
        
        // Identity verification
        request.put("customerId", paymentData.get("customerId"));
        request.put("accountNumber", paymentData.get("fromAccountNumber"));
        request.put("beneficiaryAccount", paymentData.get("toAccountNumber"));
        
        // Transaction details
        request.put("transactionId", assessment.getTransactionReference());
        request.put("amount", paymentData.get("amount"));
        request.put("currency", paymentData.get("currency"));
        
        // Personal information for identity verification
        request.put("firstName", paymentData.get("firstName"));
        request.put("lastName", paymentData.get("lastName"));
        request.put("dateOfBirth", paymentData.get("dateOfBirth"));
        request.put("address", paymentData.get("address"));
        
        return request;
    }
    
    /**
     * Build ThreatMetrix API request
     */
    private Map<String, Object> buildThreatMetrixRequest(
            Map<String, Object> apiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        Map<String, Object> request = new HashMap<>();
        
        // Device fingerprinting
        request.put("deviceId", paymentData.getOrDefault("deviceId", "unknown"));
        request.put("ipAddress", paymentData.getOrDefault("ipAddress", "unknown"));
        request.put("userAgent", paymentData.getOrDefault("userAgent", "unknown"));
        request.put("sessionId", paymentData.getOrDefault("sessionId", "unknown"));
        
        // Transaction details
        request.put("transactionId", assessment.getTransactionReference());
        request.put("amount", paymentData.get("amount"));
        request.put("currency", paymentData.get("currency"));
        
        // Customer information
        request.put("customerId", paymentData.get("customerId"));
        request.put("accountNumber", paymentData.get("fromAccountNumber"));
        
        return request;
    }
    
    /**
     * Build Forter API request
     */
    private Map<String, Object> buildForterRequest(
            Map<String, Object> apiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        Map<String, Object> request = new HashMap<>();
        
        // Transaction details
        request.put("transactionId", assessment.getTransactionReference());
        request.put("amount", paymentData.get("amount"));
        request.put("currency", paymentData.get("currency"));
        request.put("timestamp", LocalDateTime.now().toString());
        
        // Customer information
        request.put("customerId", paymentData.get("customerId"));
        request.put("accountNumber", paymentData.get("fromAccountNumber"));
        request.put("beneficiaryAccount", paymentData.get("toAccountNumber"));
        
        // Device information
        request.put("deviceId", paymentData.getOrDefault("deviceId", "unknown"));
        request.put("ipAddress", paymentData.getOrDefault("ipAddress", "unknown"));
        
        // Payment method
        request.put("paymentMethod", paymentData.getOrDefault("paymentMethod", "bank_transfer"));
        
        return request;
    }
    
    /**
     * Build Signifyd API request
     */
    private Map<String, Object> buildSignifydRequest(
            Map<String, Object> apiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        Map<String, Object> request = new HashMap<>();
        
        // Transaction details
        request.put("transactionId", assessment.getTransactionReference());
        request.put("amount", paymentData.get("amount"));
        request.put("currency", paymentData.get("currency"));
        request.put("timestamp", LocalDateTime.now().toString());
        
        // Customer information
        request.put("customerId", paymentData.get("customerId"));
        request.put("accountNumber", paymentData.get("fromAccountNumber"));
        request.put("beneficiaryAccount", paymentData.get("toAccountNumber"));
        
        // Personal information
        request.put("firstName", paymentData.get("firstName"));
        request.put("lastName", paymentData.get("lastName"));
        request.put("email", paymentData.get("email"));
        request.put("phone", paymentData.get("phone"));
        
        // Address information
        request.put("billingAddress", paymentData.get("billingAddress"));
        request.put("shippingAddress", paymentData.get("shippingAddress"));
        
        return request;
    }
    
    /**
     * Build generic API request
     */
    private Map<String, Object> buildGenericRequest(
            Map<String, Object> apiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        Map<String, Object> request = new HashMap<>();
        
        // Basic transaction information
        request.put("transactionId", assessment.getTransactionReference());
        request.put("amount", paymentData.get("amount"));
        request.put("currency", paymentData.get("currency"));
        request.put("timestamp", LocalDateTime.now().toString());
        
        // Account information
        request.put("fromAccount", paymentData.get("fromAccountNumber"));
        request.put("toAccount", paymentData.get("toAccountNumber"));
        
        // Payment details
        request.put("paymentType", assessment.getPaymentType());
        request.put("clearingSystem", assessment.getClearingSystemCode());
        
        // Customer information
        request.put("customerId", paymentData.get("customerId"));
        request.put("tenantId", assessment.getTenantId());
        
        return request;
    }
    
    /**
     * Add authentication headers
     */
    private void addAuthenticationHeaders(HttpHeaders headers, Map<String, Object> authConfig) {
        String authType = (String) authConfig.get("type");
        
        switch (authType != null ? authType.toUpperCase() : "NONE") {
            case "API_KEY":
                String apiKey = (String) authConfig.get("apiKey");
                String apiKeyHeader = (String) authConfig.getOrDefault("apiKeyHeader", "X-API-Key");
                if (apiKey != null) {
                    headers.set(apiKeyHeader, apiKey);
                }
                break;
                
            case "BEARER_TOKEN":
                String bearerToken = (String) authConfig.get("bearerToken");
                if (bearerToken != null) {
                    headers.set("Authorization", "Bearer " + bearerToken);
                }
                break;
                
            case "BASIC_AUTH":
                String username = (String) authConfig.get("username");
                String password = (String) authConfig.get("password");
                if (username != null && password != null) {
                    String credentials = username + ":" + password;
                    String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                    headers.set("Authorization", "Basic " + encodedCredentials);
                }
                break;
                
            case "CUSTOM":
                Map<String, String> customHeaders = (Map<String, String>) authConfig.get("headers");
                if (customHeaders != null) {
                    customHeaders.forEach(headers::set);
                }
                break;
        }
    }
}