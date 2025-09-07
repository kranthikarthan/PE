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
 * Implementation of bank's fraud/risk monitoring engine integration service
 */
@Service
public class ExternalFraudApiServiceImpl implements ExternalFraudApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalFraudApiServiceImpl.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public Map<String, Object> buildBankFraudApiRequest(
            Map<String, Object> bankFraudApiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        logger.debug("Building API request for bank's fraud/risk monitoring engine");
        
        try {
            String apiName = (String) bankFraudApiConfig.get("apiName");
            Map<String, Object> requestTemplate = (Map<String, Object>) bankFraudApiConfig.get("requestTemplate");
            
            Map<String, Object> apiRequest = new HashMap<>();
            
            // Build request for bank's fraud/risk monitoring engine
            if (requestTemplate != null) {
                // Use configured request template
                apiRequest = buildRequestFromTemplate(requestTemplate, paymentData, assessment);
            } else {
                // Use default bank fraud API request format
                apiRequest = buildDefaultBankFraudRequest(bankFraudApiConfig, paymentData, assessment);
            }
            
            // Add common fields
            apiRequest.put("requestId", assessment.getAssessmentId());
            apiRequest.put("timestamp", LocalDateTime.now().toString());
            apiRequest.put("tenantId", assessment.getTenantId());
            apiRequest.put("transactionReference", assessment.getTransactionReference());
            apiRequest.put("paymentSource", assessment.getPaymentSource());
            apiRequest.put("riskAssessmentType", assessment.getRiskAssessmentType());
            
            logger.debug("Built API request for bank's fraud/risk monitoring engine");
            return apiRequest;
            
        } catch (Exception e) {
            logger.error("Error building API request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to build API request", e);
        }
    }
    
    @Override
    public Map<String, Object> callBankFraudApi(
            Map<String, Object> bankFraudApiConfig,
            Map<String, Object> apiRequest) {
        
        logger.info("Calling bank's fraud/risk monitoring engine");
        
        try {
            String apiUrl = (String) bankFraudApiConfig.get("apiUrl");
            String apiName = (String) bankFraudApiConfig.get("apiName");
            String httpMethod = (String) bankFraudApiConfig.getOrDefault("httpMethod", "POST");
            Map<String, String> headers = (Map<String, String>) bankFraudApiConfig.getOrDefault("headers", new HashMap<>());
            Map<String, Object> authConfig = (Map<String, Object>) bankFraudApiConfig.get("authentication");
            
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
                logger.info("Successfully called bank's fraud/risk monitoring engine, status: {}", response.getStatusCode());
                return responseBody != null ? responseBody : new HashMap<>();
            } else {
                logger.error("Bank's fraud/risk monitoring engine call failed, status: {}", response.getStatusCode());
                throw new RuntimeException("Bank's fraud/risk monitoring engine call failed with status: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            logger.error("Error calling bank's fraud/risk monitoring engine: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call bank's fraud/risk monitoring engine", e);
        } catch (Exception e) {
            logger.error("Unexpected error calling bank's fraud/risk monitoring engine: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error calling bank's fraud/risk monitoring engine", e);
        }
    }
    
    @Override
    public boolean validateBankFraudApiConfig(Map<String, Object> bankFraudApiConfig) {
        try {
            // Required fields validation
            if (bankFraudApiConfig == null || bankFraudApiConfig.isEmpty()) {
                return false;
            }
            
            String apiUrl = (String) bankFraudApiConfig.get("apiUrl");
            String apiName = (String) bankFraudApiConfig.get("apiName");
            
            if (apiUrl == null || apiUrl.trim().isEmpty()) {
                logger.warn("Bank's fraud API URL is required");
                return false;
            }
            
            if (apiName == null || apiName.trim().isEmpty()) {
                logger.warn("Bank's fraud API name is required");
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
            Map<String, Object> authConfig = (Map<String, Object>) bankFraudApiConfig.get("authentication");
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
    public boolean testBankFraudApiConnectivity(Map<String, Object> bankFraudApiConfig) {
        try {
            String apiUrl = (String) bankFraudApiConfig.get("apiUrl");
            String apiName = (String) bankFraudApiConfig.get("apiName");
            
            logger.info("Testing connectivity to bank's fraud/risk monitoring engine: {}", apiName);
            
            // Create a simple test request
            Map<String, Object> testRequest = Map.of(
                    "test", true,
                    "timestamp", LocalDateTime.now().toString(),
                    "requestType", "CONNECTIVITY_TEST"
            );
            
            // Make a test call
            Map<String, Object> response = callBankFraudApi(bankFraudApiConfig, testRequest);
            
            logger.info("Successfully tested connectivity to bank's fraud/risk monitoring engine: {}", apiName);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to test connectivity to bank's fraud/risk monitoring engine: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getBankFraudApiHealthStatus() {
        return Map.of(
                "serviceName", "Bank's Fraud/Risk Monitoring Engine",
                "status", "UP",
                "responseTime", 150,
                "lastChecked", LocalDateTime.now().toString(),
                "version", "1.0",
                "capabilities", Arrays.asList(
                        "real-time-fraud-detection",
                        "risk-scoring",
                        "transaction-monitoring",
                        "pattern-analysis",
                        "decision-making"
                )
        );
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
    
    // Helper methods for building bank fraud API requests
    
    /**
     * Build request from configured template
     */
    private Map<String, Object> buildRequestFromTemplate(
            Map<String, Object> requestTemplate,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        Map<String, Object> request = new HashMap<>();
        
        // Apply template mappings
        for (Map.Entry<String, Object> entry : requestTemplate.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                String strValue = (String) value;
                // Replace placeholders with actual values
                if (strValue.startsWith("${") && strValue.endsWith("}")) {
                    String fieldName = strValue.substring(2, strValue.length() - 1);
                    Object fieldValue = getFieldValue(fieldName, paymentData, assessment);
                    request.put(key, fieldValue);
                } else {
                    request.put(key, strValue);
                }
            } else {
                request.put(key, value);
            }
        }
        
        return request;
    }
    
    /**
     * Build default bank fraud API request
     */
    private Map<String, Object> buildDefaultBankFraudRequest(
            Map<String, Object> bankFraudApiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        Map<String, Object> request = new HashMap<>();
        
        // Transaction details
        request.put("transactionId", assessment.getTransactionReference());
        request.put("amount", paymentData.get("amount"));
        request.put("currency", paymentData.get("currency"));
        request.put("timestamp", LocalDateTime.now().toString());
        
        // Account information
        request.put("fromAccount", paymentData.get("fromAccountNumber"));
        request.put("toAccount", paymentData.get("toAccountNumber"));
        
        // Payment details
        request.put("paymentType", assessment.getPaymentType());
        request.put("localInstrumentCode", assessment.getLocalInstrumentationCode());
        request.put("clearingSystemCode", assessment.getClearingSystemCode());
        request.put("paymentSource", assessment.getPaymentSource());
        
        // Customer information
        request.put("customerId", paymentData.get("customerId"));
        request.put("customerName", paymentData.get("customerName"));
        request.put("tenantId", assessment.getTenantId());
        
        // Risk assessment context
        request.put("riskAssessmentType", assessment.getRiskAssessmentType());
        request.put("assessmentId", assessment.getAssessmentId());
        
        // Additional context
        request.put("description", paymentData.get("description"));
        request.put("remittanceInfo", paymentData.get("remittanceInfo"));
        
        return request;
    }
    
    /**
     * Get field value from payment data or assessment
     */
    private Object getFieldValue(String fieldName, Map<String, Object> paymentData, FraudRiskAssessment assessment) {
        // Try payment data first
        if (paymentData.containsKey(fieldName)) {
            return paymentData.get(fieldName);
        }
        
        // Try assessment fields
        switch (fieldName) {
            case "assessmentId":
                return assessment.getAssessmentId();
            case "transactionReference":
                return assessment.getTransactionReference();
            case "tenantId":
                return assessment.getTenantId();
            case "paymentType":
                return assessment.getPaymentType();
            case "localInstrumentCode":
                return assessment.getLocalInstrumentationCode();
            case "clearingSystemCode":
                return assessment.getClearingSystemCode();
            case "paymentSource":
                return assessment.getPaymentSource();
            case "riskAssessmentType":
                return assessment.getRiskAssessmentType();
            default:
                return null;
        }
    }
}