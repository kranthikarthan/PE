package com.paymentengine.gateway.dto.multilevel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResolvedAuthConfiguration {

    private String authMethod;
    private String apiKey;
    private String apiKeyHeaderName;
    private Boolean includeClientHeaders;
    private String clientId;
    private String clientSecret;
    private Integer timeoutSeconds;
    private Integer retryAttempts;
    private Integer retryDelaySeconds;

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKeyHeaderName() {
        return apiKeyHeaderName;
    }

    public void setApiKeyHeaderName(String apiKeyHeaderName) {
        this.apiKeyHeaderName = apiKeyHeaderName;
    }

    public Boolean getIncludeClientHeaders() {
        return includeClientHeaders;
    }

    public void setIncludeClientHeaders(Boolean includeClientHeaders) {
        this.includeClientHeaders = includeClientHeaders;
    }

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

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Integer getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(Integer retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public Integer getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public void setRetryDelaySeconds(Integer retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }
}
