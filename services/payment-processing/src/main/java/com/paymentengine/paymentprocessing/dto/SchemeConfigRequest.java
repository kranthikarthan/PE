package com.paymentengine.paymentprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating/updating scheme configurations
 */
public class SchemeConfigRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Active status is required")
    private Boolean isActive;
    
    @NotNull(message = "Interaction mode is required")
    private InteractionMode interactionMode;
    
    @NotNull(message = "Message format is required")
    private MessageFormat messageFormat;
    
    @NotNull(message = "Response mode is required")
    private ResponseMode responseMode;
    
    @Positive(message = "Timeout must be positive")
    private Long timeoutMs;
    
    @Valid
    @NotNull(message = "Retry policy is required")
    private RetryPolicy retryPolicy;
    
    @Valid
    @NotNull(message = "Authentication config is required")
    private AuthenticationConfig authentication;
    
    @Valid
    @NotNull(message = "Endpoints are required")
    @Size(min = 1, message = "At least one endpoint is required")
    private List<EndpointConfig> endpoints;
    
    // Constructors
    public SchemeConfigRequest() {}
    
    public SchemeConfigRequest(String name, String description, Boolean isActive, 
                              InteractionMode interactionMode, MessageFormat messageFormat,
                              ResponseMode responseMode, Long timeoutMs, 
                              RetryPolicy retryPolicy, AuthenticationConfig authentication,
                              List<EndpointConfig> endpoints) {
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.interactionMode = interactionMode;
        this.messageFormat = messageFormat;
        this.responseMode = responseMode;
        this.timeoutMs = timeoutMs;
        this.retryPolicy = retryPolicy;
        this.authentication = authentication;
        this.endpoints = endpoints;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public InteractionMode getInteractionMode() {
        return interactionMode;
    }
    
    public void setInteractionMode(InteractionMode interactionMode) {
        this.interactionMode = interactionMode;
    }
    
    public MessageFormat getMessageFormat() {
        return messageFormat;
    }
    
    public void setMessageFormat(MessageFormat messageFormat) {
        this.messageFormat = messageFormat;
    }
    
    public ResponseMode getResponseMode() {
        return responseMode;
    }
    
    public void setResponseMode(ResponseMode responseMode) {
        this.responseMode = responseMode;
    }
    
    public Long getTimeoutMs() {
        return timeoutMs;
    }
    
    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
    
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }
    
    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }
    
    public AuthenticationConfig getAuthentication() {
        return authentication;
    }
    
    public void setAuthentication(AuthenticationConfig authentication) {
        this.authentication = authentication;
    }
    
    public List<EndpointConfig> getEndpoints() {
        return endpoints;
    }
    
    public void setEndpoints(List<EndpointConfig> endpoints) {
        this.endpoints = endpoints;
    }
    
    // Enums
    public enum InteractionMode {
        SYNCHRONOUS,
        ASYNCHRONOUS,
        HYBRID
    }
    
    public enum MessageFormat {
        JSON,
        XML,
        BOTH
    }
    
    public enum ResponseMode {
        IMMEDIATE,
        WEBHOOK,
        KAFKA_TOPIC,
        POLLING
    }
    
    // Nested classes
    public static class RetryPolicy {
        @Positive(message = "Max retries must be positive")
        private Integer maxRetries;
        
        @Positive(message = "Backoff must be positive")
        private Long backoffMs;
        
        @NotNull(message = "Exponential backoff flag is required")
        private Boolean exponentialBackoff;
        
        @NotNull(message = "Retryable status codes are required")
        private List<Integer> retryableStatusCodes;
        
        // Constructors
        public RetryPolicy() {}
        
        public RetryPolicy(Integer maxRetries, Long backoffMs, Boolean exponentialBackoff, 
                          List<Integer> retryableStatusCodes) {
            this.maxRetries = maxRetries;
            this.backoffMs = backoffMs;
            this.exponentialBackoff = exponentialBackoff;
            this.retryableStatusCodes = retryableStatusCodes;
        }
        
        // Getters and Setters
        public Integer getMaxRetries() {
            return maxRetries;
        }
        
        public void setMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
        }
        
        public Long getBackoffMs() {
            return backoffMs;
        }
        
        public void setBackoffMs(Long backoffMs) {
            this.backoffMs = backoffMs;
        }
        
        public Boolean getExponentialBackoff() {
            return exponentialBackoff;
        }
        
        public void setExponentialBackoff(Boolean exponentialBackoff) {
            this.exponentialBackoff = exponentialBackoff;
        }
        
        public List<Integer> getRetryableStatusCodes() {
            return retryableStatusCodes;
        }
        
        public void setRetryableStatusCodes(List<Integer> retryableStatusCodes) {
            this.retryableStatusCodes = retryableStatusCodes;
        }
    }
    
    public static class AuthenticationConfig {
        @NotNull(message = "Authentication type is required")
        private AuthType type;
        
        private String apiKey;
        private String jwtSecret;
        private OAuth2Config oauth2Config;
        private String certificatePath;
        private String keyPath;
        
        // Constructors
        public AuthenticationConfig() {}
        
        public AuthenticationConfig(AuthType type) {
            this.type = type;
        }
        
        // Getters and Setters
        public AuthType getType() {
            return type;
        }
        
        public void setType(AuthType type) {
            this.type = type;
        }
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getJwtSecret() {
            return jwtSecret;
        }
        
        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }
        
        public OAuth2Config getOauth2Config() {
            return oauth2Config;
        }
        
        public void setOauth2Config(OAuth2Config oauth2Config) {
            this.oauth2Config = oauth2Config;
        }
        
        public String getCertificatePath() {
            return certificatePath;
        }
        
        public void setCertificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
        }
        
        public String getKeyPath() {
            return keyPath;
        }
        
        public void setKeyPath(String keyPath) {
            this.keyPath = keyPath;
        }
        
        public enum AuthType {
            NONE,
            API_KEY,
            JWT,
            OAUTH2,
            MUTUAL_TLS
        }
        
        public static class OAuth2Config {
            @NotBlank(message = "Client ID is required")
            private String clientId;
            
            @NotBlank(message = "Client secret is required")
            private String clientSecret;
            
            @NotBlank(message = "Token URL is required")
            private String tokenUrl;
            
            @NotNull(message = "Scope is required")
            private List<String> scope;
            
            // Constructors
            public OAuth2Config() {}
            
            public OAuth2Config(String clientId, String clientSecret, String tokenUrl, List<String> scope) {
                this.clientId = clientId;
                this.clientSecret = clientSecret;
                this.tokenUrl = tokenUrl;
                this.scope = scope;
            }
            
            // Getters and Setters
            public String getClientId() {
                return clientId;
            }
            
            public void setClientId(String clientId) {
                this.clientId = clientId;
            }
            
            public String getClientSecret() {
                return clientSecret;
            }
            
            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }
            
            public String getTokenUrl() {
                return tokenUrl;
            }
            
            public void setTokenUrl(String tokenUrl) {
                this.tokenUrl = tokenUrl;
            }
            
            public List<String> getScope() {
                return scope;
            }
            
            public void setScope(List<String> scope) {
                this.scope = scope;
            }
        }
    }
    
    public static class EndpointConfig {
        @NotBlank(message = "Endpoint name is required")
        @Size(max = 255, message = "Endpoint name must not exceed 255 characters")
        private String name;
        
        @NotBlank(message = "URL is required")
        private String url;
        
        @NotNull(message = "HTTP method is required")
        private HttpMethod method;
        
        @NotNull(message = "Active status is required")
        private Boolean isActive;
        
        @Positive(message = "Timeout must be positive")
        private Long timeoutMs;
        
        private Map<String, String> headers;
        
        @NotNull(message = "Supported message types are required")
        private List<String> supportedMessageTypes;
        
        @Positive(message = "Priority must be positive")
        private Integer priority;
        
        // Constructors
        public EndpointConfig() {}
        
        public EndpointConfig(String name, String url, HttpMethod method, Boolean isActive,
                             Long timeoutMs, Map<String, String> headers,
                             List<String> supportedMessageTypes, Integer priority) {
            this.name = name;
            this.url = url;
            this.method = method;
            this.isActive = isActive;
            this.timeoutMs = timeoutMs;
            this.headers = headers;
            this.supportedMessageTypes = supportedMessageTypes;
            this.priority = priority;
        }
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public HttpMethod getMethod() {
            return method;
        }
        
        public void setMethod(HttpMethod method) {
            this.method = method;
        }
        
        public Boolean getIsActive() {
            return isActive;
        }
        
        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
        
        public Long getTimeoutMs() {
            return timeoutMs;
        }
        
        public void setTimeoutMs(Long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
        
        public Map<String, String> getHeaders() {
            return headers;
        }
        
        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
        
        public List<String> getSupportedMessageTypes() {
            return supportedMessageTypes;
        }
        
        public void setSupportedMessageTypes(List<String> supportedMessageTypes) {
            this.supportedMessageTypes = supportedMessageTypes;
        }
        
        public Integer getPriority() {
            return priority;
        }
        
        public void setPriority(Integer priority) {
            this.priority = priority;
        }
        
        public enum HttpMethod {
            GET,
            POST,
            PUT,
            DELETE
        }
    }
}