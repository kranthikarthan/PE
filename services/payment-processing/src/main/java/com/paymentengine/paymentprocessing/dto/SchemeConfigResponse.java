package com.paymentengine.paymentprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for scheme configurations
 */
public class SchemeConfigResponse {
    
    private String id;
    private String name;
    private String description;
    private Boolean isActive;
    private SchemeConfigRequest.InteractionMode interactionMode;
    private SchemeConfigRequest.MessageFormat messageFormat;
    private SchemeConfigRequest.ResponseMode responseMode;
    private Long timeoutMs;
    private SchemeConfigRequest.RetryPolicy retryPolicy;
    private SchemeConfigRequest.AuthenticationConfig authentication;
    private List<SchemeConfigRequest.EndpointConfig> endpoints;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Map<String, Object> metadata;
    
    // Constructors
    public SchemeConfigResponse() {}
    
    public SchemeConfigResponse(String id, String name, String description, Boolean isActive,
                               SchemeConfigRequest.InteractionMode interactionMode,
                               SchemeConfigRequest.MessageFormat messageFormat,
                               SchemeConfigRequest.ResponseMode responseMode,
                               Long timeoutMs, SchemeConfigRequest.RetryPolicy retryPolicy,
                               SchemeConfigRequest.AuthenticationConfig authentication,
                               List<SchemeConfigRequest.EndpointConfig> endpoints,
                               Instant createdAt, Instant updatedAt,
                               String createdBy, String updatedBy) {
        this.id = id;
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public SchemeConfigRequest.InteractionMode getInteractionMode() {
        return interactionMode;
    }
    
    public void setInteractionMode(SchemeConfigRequest.InteractionMode interactionMode) {
        this.interactionMode = interactionMode;
    }
    
    public SchemeConfigRequest.MessageFormat getMessageFormat() {
        return messageFormat;
    }
    
    public void setMessageFormat(SchemeConfigRequest.MessageFormat messageFormat) {
        this.messageFormat = messageFormat;
    }
    
    public SchemeConfigRequest.ResponseMode getResponseMode() {
        return responseMode;
    }
    
    public void setResponseMode(SchemeConfigRequest.ResponseMode responseMode) {
        this.responseMode = responseMode;
    }
    
    public Long getTimeoutMs() {
        return timeoutMs;
    }
    
    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
    
    public SchemeConfigRequest.RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }
    
    public void setRetryPolicy(SchemeConfigRequest.RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }
    
    public SchemeConfigRequest.AuthenticationConfig getAuthentication() {
        return authentication;
    }
    
    public void setAuthentication(SchemeConfigRequest.AuthenticationConfig authentication) {
        this.authentication = authentication;
    }
    
    public List<SchemeConfigRequest.EndpointConfig> getEndpoints() {
        return endpoints;
    }
    
    public void setEndpoints(List<SchemeConfigRequest.EndpointConfig> endpoints) {
        this.endpoints = endpoints;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}