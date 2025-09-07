package com.paymentengine.middleware.service;

import com.paymentengine.middleware.dto.SchemeConfigRequest;
import com.paymentengine.middleware.dto.SchemeConfigResponse;
import com.paymentengine.middleware.dto.SchemeTestRequest;
import com.paymentengine.middleware.dto.SchemeTestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Service interface for scheme configuration management
 */
public interface SchemeConfigService {
    
    /**
     * Get all scheme configurations with pagination and filtering
     */
    Page<SchemeConfigResponse> getConfigurations(
            String name, Boolean isActive, String interactionMode, 
            String messageFormat, String responseMode, Pageable pageable);
    
    /**
     * Get a specific scheme configuration by ID
     */
    SchemeConfigResponse getConfiguration(String configId);
    
    /**
     * Create a new scheme configuration
     */
    SchemeConfigResponse createConfiguration(SchemeConfigRequest request);
    
    /**
     * Update an existing scheme configuration
     */
    SchemeConfigResponse updateConfiguration(String configId, SchemeConfigRequest request);
    
    /**
     * Delete a scheme configuration
     */
    void deleteConfiguration(String configId);
    
    /**
     * Clone an existing configuration
     */
    SchemeConfigResponse cloneConfiguration(String configId, String newName);
    
    /**
     * Toggle configuration status (activate/deactivate)
     */
    SchemeConfigResponse toggleConfigurationStatus(String configId, Boolean isActive);
    
    /**
     * Test a scheme configuration
     */
    SchemeTestResponse testConfiguration(SchemeTestRequest request);
    
    /**
     * Validate a configuration without testing
     */
    Map<String, Object> validateConfiguration(String configId);
    
    /**
     * Get test history for a configuration
     */
    Page<SchemeTestResponse> getTestHistory(String configId, Pageable pageable);
    
    /**
     * Get scheme interaction statistics
     */
    Map<String, Object> getStatistics(String configId, String fromDate, String toDate);
    
    /**
     * Get configuration health status
     */
    Map<String, Object> getConfigurationHealth(String configId);
    
    /**
     * Get all configuration health statuses
     */
    Map<String, Object> getAllConfigurationHealth();
    
    /**
     * Get available message templates
     */
    Map<String, Object> getMessageTemplates();
    
    /**
     * Create a message from template
     */
    com.paymentengine.middleware.dto.SchemeMessageRequest createMessageFromTemplate(
            String templateId, Map<String, Object> variables);
}