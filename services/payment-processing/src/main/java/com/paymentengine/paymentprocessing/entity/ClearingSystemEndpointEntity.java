package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for storing clearing system endpoint configurations
 */
@Entity
@Table(name = "clearing_system_endpoints")
public class ClearingSystemEndpointEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "clearing_system_id", nullable = false)
    private String clearingSystemId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "endpoint_type", nullable = false, length = 50)
    private String endpointType; // SYNC, ASYNC, POLLING, WEBHOOK
    
    @Column(name = "message_type", nullable = false, length = 50)
    private String messageType; // pacs008, pacs002, pain001, pain002, pacs004, pacs007, pacs028, camt054, camt055, camt056, camt029
    
    @Column(name = "message_format", length = 10)
    private String messageFormat; // JSON, XML
    
    @Column(name = "response_mode", length = 20)
    private String responseMode; // IMMEDIATE, ASYNC, KAFKA, WEBHOOK
    
    @Column(name = "flow_direction", length = 30)
    private String flowDirection; // CLIENT_TO_CLEARING, CLEARING_TO_CLIENT, BIDIRECTIONAL
    
    @Column(name = "url", nullable = false, length = 500)
    private String url;
    
    @Column(name = "http_method", length = 10)
    private String httpMethod; // GET, POST, PUT, DELETE
    
    @Column(name = "timeout_ms")
    private Integer timeoutMs;
    
    @Column(name = "retry_attempts")
    private Integer retryAttempts;
    
    @Column(name = "authentication_type", length = 20)
    private String authenticationType; // NONE, API_KEY, JWT, OAUTH2, MTLS
    
    @ElementCollection
    @CollectionTable(name = "clearing_system_endpoint_auth_config", 
                    joinColumns = @JoinColumn(name = "endpoint_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value")
    private Map<String, String> authenticationConfig;
    
    @ElementCollection
    @CollectionTable(name = "clearing_system_endpoint_headers", 
                    joinColumns = @JoinColumn(name = "endpoint_id"))
    @MapKeyColumn(name = "header_name")
    @Column(name = "header_value")
    private Map<String, String> defaultHeaders;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 1;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public ClearingSystemEndpointEntity() {}
    
    public ClearingSystemEndpointEntity(String clearingSystemId, String name, String endpointType,
                                      String messageType, String url, String httpMethod,
                                      Integer timeoutMs, Integer retryAttempts, String authenticationType,
                                      Map<String, String> authenticationConfig, Map<String, String> defaultHeaders,
                                      Boolean isActive, Integer priority, String description) {
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
    
    public String getMessageFormat() { return messageFormat; }
    public void setMessageFormat(String messageFormat) { this.messageFormat = messageFormat; }
    
    public String getResponseMode() { return responseMode; }
    public void setResponseMode(String responseMode) { this.responseMode = responseMode; }
    
    public String getFlowDirection() { return flowDirection; }
    public void setFlowDirection(String flowDirection) { this.flowDirection = flowDirection; }
    
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