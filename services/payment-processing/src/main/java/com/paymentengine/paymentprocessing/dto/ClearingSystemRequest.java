package com.paymentengine.paymentprocessing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * DTO for creating or updating a clearing system
 */
public class ClearingSystemRequest {
    
    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    private String code;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 3, message = "Country code must not exceed 3 characters")
    private String countryCode;
    
    @Size(max = 3, message = "Currency must not exceed 3 characters")
    private String currency;
    
    @NotNull(message = "Active status is required")
    private Boolean isActive;
    
    @NotBlank(message = "Processing mode is required")
    private String processingMode; // SYNCHRONOUS, ASYNCHRONOUS, BATCH
    
    @Positive(message = "Timeout must be positive")
    private Integer timeoutSeconds;
    
    @NotBlank(message = "Endpoint URL is required")
    @Size(max = 500, message = "Endpoint URL must not exceed 500 characters")
    private String endpointUrl;
    
    @NotBlank(message = "Authentication type is required")
    private String authenticationType; // NONE, API_KEY, JWT, OAUTH2, MTLS
    
    private Map<String, String> authenticationConfig;
    
    private Map<String, String> supportedMessageTypes;
    
    private Map<String, String> supportedPaymentTypes;
    
    private Map<String, String> supportedLocalInstruments;
    
    @Valid
    private List<ClearingSystemEndpointRequest> endpoints;
    
    // Constructors
    public ClearingSystemRequest() {}
    
    public ClearingSystemRequest(String code, String name, String description, String countryCode,
                               String currency, Boolean isActive, String processingMode,
                               Integer timeoutSeconds, String endpointUrl, String authenticationType,
                               Map<String, String> authenticationConfig, Map<String, String> supportedMessageTypes,
                               Map<String, String> supportedPaymentTypes, Map<String, String> supportedLocalInstruments,
                               List<ClearingSystemEndpointRequest> endpoints) {
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
    
    public List<ClearingSystemEndpointRequest> getEndpoints() { return endpoints; }
    public void setEndpoints(List<ClearingSystemEndpointRequest> endpoints) { this.endpoints = endpoints; }
    
    /**
     * DTO for clearing system endpoint
     */
    public static class ClearingSystemEndpointRequest {
        
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        private String name;
        
        @NotBlank(message = "Endpoint type is required")
        private String endpointType; // SYNC, ASYNC, POLLING, WEBHOOK
        
        @NotBlank(message = "Message type is required")
        private String messageType; // pacs008, pacs002, pain001, pain002
        
        @NotBlank(message = "URL is required")
        @Size(max = 500, message = "URL must not exceed 500 characters")
        private String url;
        
        private String httpMethod; // GET, POST, PUT, DELETE
        
        @Positive(message = "Timeout must be positive")
        private Integer timeoutMs;
        
        @Positive(message = "Retry attempts must be positive")
        private Integer retryAttempts;
        
        @NotBlank(message = "Authentication type is required")
        private String authenticationType; // NONE, API_KEY, JWT, OAUTH2, MTLS
        
        private Map<String, String> authenticationConfig;
        
        private Map<String, String> defaultHeaders;
        
        @NotNull(message = "Active status is required")
        private Boolean isActive;
        
        @Positive(message = "Priority must be positive")
        private Integer priority;
        
        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;
        
        // Constructors
        public ClearingSystemEndpointRequest() {}
        
        public ClearingSystemEndpointRequest(String name, String endpointType, String messageType,
                                           String url, String httpMethod, Integer timeoutMs,
                                           Integer retryAttempts, String authenticationType,
                                           Map<String, String> authenticationConfig, Map<String, String> defaultHeaders,
                                           Boolean isActive, Integer priority, String description) {
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
        }
        
        // Getters and Setters
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
    }
}