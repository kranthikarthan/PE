package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.FraudRiskAssessment;

import java.util.Map;

/**
 * Service interface for external fraud API integration
 */
public interface ExternalFraudApiService {
    
    /**
     * Build API request for external fraud service
     */
    Map<String, Object> buildApiRequest(
            Map<String, Object> apiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment);
    
    /**
     * Call external fraud API
     */
    Map<String, Object> callExternalApi(
            Map<String, Object> apiConfig,
            Map<String, Object> apiRequest);
    
    /**
     * Validate external fraud API configuration
     */
    boolean validateApiConfig(Map<String, Object> apiConfig);
    
    /**
     * Test external fraud API connectivity
     */
    boolean testApiConnectivity(Map<String, Object> apiConfig);
    
    /**
     * Get supported external fraud APIs
     */
    Map<String, Object> getSupportedApis();
    
    /**
     * Get API health status
     */
    Map<String, Object> getApiHealthStatus(String apiName);
}