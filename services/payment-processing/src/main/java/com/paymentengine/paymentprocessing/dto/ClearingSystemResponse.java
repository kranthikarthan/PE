package com.paymentengine.paymentprocessing.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for returning clearing system information
 */
public class ClearingSystemResponse {
    
    private String id;
    private String code;
    private String name;
    private String description;
    private String countryCode;
    private String currency;
    private Boolean isActive;
    private String processingMode;
    private Integer timeoutSeconds;
    private String endpointUrl;
    private String authenticationType;
    private Map<String, String> authenticationConfig;
    private Map<String, String> supportedMessageTypes;
    private Map<String, String> supportedPaymentTypes;
    private Map<String, String> supportedLocalInstruments;
    private List<ClearingSystemEndpointResponse> endpoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public ClearingSystemResponse() {}
    
    public ClearingSystemResponse(String id, String code, String name, String description,
                                String countryCode, String currency, Boolean isActive,
                                String processingMode, Integer timeoutSeconds, String endpointUrl,
                                String authenticationType, Map<String, String> authenticationConfig,
                                Map<String, String> supportedMessageTypes, Map<String, String> supportedPaymentTypes,
                                Map<String, String> supportedLocalInstruments, List<ClearingSystemEndpointResponse> endpoints,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.countryCode = countryCode;
        this.currency = currency;
        this.isActive = isActive;
        this.processingMode = processingMode;
        this.timeoutSeconds = timeoutSeconds;
        this.endpointUrl = endpointUrl;
        this.authenticationType = authenticationType;
        this.authenticationConfig = authenticationConfig;
        this.supportedMessageTypes = supportedMessageTypes;
        this.supportedPaymentTypes = supportedPaymentTypes;
        this.supportedLocalInstruments = supportedLocalInstruments;
        this.endpoints = endpoints;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
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
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getProcessingMode() { return processingMode; }
    public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
    
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    
    public String getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(String authenticationType) { this.authenticationType = authenticationType; }
    
    public Map<String, String> getAuthenticationConfig() { return authenticationConfig; }
    public void setAuthenticationConfig(Map<String, String> authenticationConfig) { this.authenticationConfig = authenticationConfig; }
    
    public Map<String, String> getSupportedMessageTypes() { return supportedMessageTypes; }
    public void setSupportedMessageTypes(Map<String, String> supportedMessageTypes) { this.supportedMessageTypes = supportedMessageTypes; }
    
    public Map<String, String> getSupportedPaymentTypes() { return supportedPaymentTypes; }
    public void setSupportedPaymentTypes(Map<String, String> supportedPaymentTypes) { this.supportedPaymentTypes = supportedPaymentTypes; }
    
    public Map<String, String> getSupportedLocalInstruments() { return supportedLocalInstruments; }
    public void setSupportedLocalInstruments(Map<String, String> supportedLocalInstruments) { this.supportedLocalInstruments = supportedLocalInstruments; }
    
    public List<ClearingSystemEndpointResponse> getEndpoints() { return endpoints; }
    public void setEndpoints(List<ClearingSystemEndpointResponse> endpoints) { this.endpoints = endpoints; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    /**
     * DTO for clearing system endpoint response
     */
    public static class ClearingSystemEndpointResponse {
        
        private String id;
        private String clearingSystemId;
        private String name;
        private String endpointType;
        private String messageType;
        private String url;
        private String httpMethod;
        private Integer timeoutMs;
        private Integer retryAttempts;
        private String authenticationType;
        private Map<String, String> authenticationConfig;
        private Map<String, String> defaultHeaders;
        private Boolean isActive;
        private Integer priority;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Constructors
        public ClearingSystemEndpointResponse() {}
        
        public ClearingSystemEndpointResponse(String id, String clearingSystemId, String name,
                                            String endpointType, String messageType, String url,
                                            String httpMethod, Integer timeoutMs, Integer retryAttempts,
                                            String authenticationType, Map<String, String> authenticationConfig,
                                            Map<String, String> defaultHeaders, Boolean isActive,
                                            Integer priority, String description, LocalDateTime createdAt,
                                            LocalDateTime updatedAt) {
            this.id = id;
            this.clearingSystemId = clearingSystemId;
            this.name = name;
            this.endpointType = endpointType;
            this.messageType = messageType;
            this.url = url;
            this.httpMethod = httpMethod;
            this.timeoutMs = timeoutMs;
            this.retryAttempts = retryAttempts;
            this.authenticationType = authenticationType;
            this.authenticationConfig = authenticationConfig;
            this.defaultHeaders = defaultHeaders;
            this.isActive = isActive;
            this.priority = priority;
            this.description = description;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getClearingSystemId() { return clearingSystemId; }
        public void setClearingSystemId(String clearingSystemId) { this.clearingSystemId = clearingSystemId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEndpointType() { return endpointType; }
        public void setEndpointType(String endpointType) { this.endpointType = endpointType; }
        
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getHttpMethod() { return httpMethod; }
        public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
        
        public Integer getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }
        
        public Integer getRetryAttempts() { return retryAttempts; }
        public void setRetryAttempts(Integer retryAttempts) { this.retryAttempts = retryAttempts; }
        
        public String getAuthenticationType() { return authenticationType; }
        public void setAuthenticationType(String authenticationType) { this.authenticationType = authenticationType; }
        
        public Map<String, String> getAuthenticationConfig() { return authenticationConfig; }
        public void setAuthenticationConfig(Map<String, String> authenticationConfig) { this.authenticationConfig = authenticationConfig; }
        
        public Map<String, String> getDefaultHeaders() { return defaultHeaders; }
        public void setDefaultHeaders(Map<String, String> defaultHeaders) { this.defaultHeaders = defaultHeaders; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
}