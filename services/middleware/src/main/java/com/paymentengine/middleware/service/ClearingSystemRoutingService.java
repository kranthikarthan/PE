package com.paymentengine.middleware.service;

import java.util.Map;

/**
 * Service interface for clearing system routing based on tenant and payment type
 */
public interface ClearingSystemRoutingService {
    
    /**
     * Determine the clearing system for a given tenant and payment type
     */
    ClearingSystemRoute determineClearingSystem(String tenantId, String paymentType, String localInstrumentCode);
    
    /**
     * Get clearing system configuration for a specific system
     */
    ClearingSystemConfig getClearingSystemConfig(String clearingSystemCode);
    
    /**
     * Get all available clearing systems for a tenant
     */
    Map<String, ClearingSystemConfig> getAvailableClearingSystems(String tenantId);
    
    /**
     * Validate if a tenant has access to a specific clearing system
     */
    boolean validateClearingSystemAccess(String tenantId, String clearingSystemCode);
    
    /**
     * Get the scheme configuration for a clearing system
     */
    String getSchemeConfigurationId(String clearingSystemCode, String messageType);
    
    /**
     * Route message to appropriate clearing system
     */
    ClearingSystemRoute routeMessage(String tenantId, String paymentType, String localInstrumentCode, String messageType);
    
    /**
     * Clearing system route information
     */
    class ClearingSystemRoute {
        private String clearingSystemCode;
        private String clearingSystemName;
        private String schemeConfigurationId;
        private String endpointUrl;
        private String authenticationType;
        private Map<String, String> authenticationConfig;
        private boolean isActive;
        private String routingPriority;
        
        // Constructors
        public ClearingSystemRoute() {}
        
        public ClearingSystemRoute(String clearingSystemCode, String clearingSystemName, 
                                 String schemeConfigurationId, String endpointUrl,
                                 String authenticationType, Map<String, String> authenticationConfig,
                                 boolean isActive, String routingPriority) {
            this.clearingSystemCode = clearingSystemCode;
            this.clearingSystemName = clearingSystemName;
            this.schemeConfigurationId = schemeConfigurationId;
            this.endpointUrl = endpointUrl;
            this.authenticationType = authenticationType;
            this.authenticationConfig = authenticationConfig;
            this.isActive = isActive;
            this.routingPriority = routingPriority;
        }
        
        // Getters and Setters
        public String getClearingSystemCode() { return clearingSystemCode; }
        public void setClearingSystemCode(String clearingSystemCode) { this.clearingSystemCode = clearingSystemCode; }
        
        public String getClearingSystemName() { return clearingSystemName; }
        public void setClearingSystemName(String clearingSystemName) { this.clearingSystemName = clearingSystemName; }
        
        public String getSchemeConfigurationId() { return schemeConfigurationId; }
        public void setSchemeConfigurationId(String schemeConfigurationId) { this.schemeConfigurationId = schemeConfigurationId; }
        
        public String getEndpointUrl() { return endpointUrl; }
        public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
        
        public String getAuthenticationType() { return authenticationType; }
        public void setAuthenticationType(String authenticationType) { this.authenticationType = authenticationType; }
        
        public Map<String, String> getAuthenticationConfig() { return authenticationConfig; }
        public void setAuthenticationConfig(Map<String, String> authenticationConfig) { this.authenticationConfig = authenticationConfig; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public String getRoutingPriority() { return routingPriority; }
        public void setRoutingPriority(String routingPriority) { this.routingPriority = routingPriority; }
    }
    
    /**
     * Clearing system configuration
     */
    class ClearingSystemConfig {
        private String code;
        private String name;
        private String description;
        private String countryCode;
        private String currency;
        private boolean isActive;
        private Map<String, String> supportedMessageTypes;
        private Map<String, String> supportedPaymentTypes;
        private Map<String, String> supportedLocalInstruments;
        private String processingMode; // SYNCHRONOUS, ASYNCHRONOUS, BATCH
        private int timeoutSeconds;
        private String endpointUrl;
        private Map<String, String> authenticationConfig;
        
        // Constructors
        public ClearingSystemConfig() {}
        
        public ClearingSystemConfig(String code, String name, String description, String countryCode,
                                  String currency, boolean isActive, Map<String, String> supportedMessageTypes,
                                  Map<String, String> supportedPaymentTypes, Map<String, String> supportedLocalInstruments,
                                  String processingMode, int timeoutSeconds, String endpointUrl,
                                  Map<String, String> authenticationConfig) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.countryCode = countryCode;
            this.currency = currency;
            this.isActive = isActive;
            this.supportedMessageTypes = supportedMessageTypes;
            this.supportedPaymentTypes = supportedPaymentTypes;
            this.supportedLocalInstruments = supportedLocalInstruments;
            this.processingMode = processingMode;
            this.timeoutSeconds = timeoutSeconds;
            this.endpointUrl = endpointUrl;
            this.authenticationConfig = authenticationConfig;
        }
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public Map<String, String> getSupportedMessageTypes() { return supportedMessageTypes; }
        public void setSupportedMessageTypes(Map<String, String> supportedMessageTypes) { this.supportedMessageTypes = supportedMessageTypes; }
        
        public Map<String, String> getSupportedPaymentTypes() { return supportedPaymentTypes; }
        public void setSupportedPaymentTypes(Map<String, String> supportedPaymentTypes) { this.supportedPaymentTypes = supportedPaymentTypes; }
        
        public Map<String, String> getSupportedLocalInstruments() { return supportedLocalInstruments; }
        public void setSupportedLocalInstruments(Map<String, String> supportedLocalInstruments) { this.supportedLocalInstruments = supportedLocalInstruments; }
        
        public String getProcessingMode() { return processingMode; }
        public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
        
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
        
        public String getEndpointUrl() { return endpointUrl; }
        public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
        
        public Map<String, String> getAuthenticationConfig() { return authenticationConfig; }
        public void setAuthenticationConfig(Map<String, String> authenticationConfig) { this.authenticationConfig = authenticationConfig; }
    }
}