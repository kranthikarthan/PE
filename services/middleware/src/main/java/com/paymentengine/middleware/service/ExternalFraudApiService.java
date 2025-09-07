package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.FraudRiskAssessment;

import java.util.Map;

/**
 * Service interface for bank's fraud/risk monitoring engine integration
 */
public interface ExternalFraudApiService {
    
    /**
     * Build API request for bank's fraud/risk monitoring engine
     */
    Map<String, Object> buildBankFraudApiRequest(
            Map<String, Object> bankFraudApiConfig,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment);
    
    /**
     * Call bank's fraud/risk monitoring engine
     */
    Map<String, Object> callBankFraudApi(
            Map<String, Object> bankFraudApiConfig,
            Map<String, Object> apiRequest);
    
    /**
     * Validate bank's fraud API configuration
     */
    boolean validateBankFraudApiConfig(Map<String, Object> bankFraudApiConfig);
    
    /**
     * Test bank's fraud API connectivity
     */
    boolean testBankFraudApiConnectivity(Map<String, Object> bankFraudApiConfig);
    
    /**
     * Get bank's fraud API health status
     */
    Map<String, Object> getBankFraudApiHealthStatus();
}