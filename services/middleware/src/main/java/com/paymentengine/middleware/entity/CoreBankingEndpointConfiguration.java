package com.paymentengine.middleware.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Core Banking Endpoint Configuration Entity
 * 
 * Stores configuration for specific endpoints within a core banking system,
 * including URL patterns, HTTP methods, headers, and authentication details.
 */
@Entity
@Table(name = "core_banking_endpoint_configurations", schema = "payment_engine")
public class CoreBankingEndpointConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "core_banking_config_id", nullable = false)
    @NotNull
    private UUID coreBankingConfigId;
    
    @Column(name = "endpoint_name", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String endpointName;
    
    @Column(name = "endpoint_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private EndpointType endpointType;
    
    @Column(name = "http_method", length = 10)
    @Size(max = 10)
    private String httpMethod = "POST";
    
    @Column(name = "endpoint_path", nullable = false, length = 500)
    @NotBlank
    @Size(max = 500)
    private String endpointPath;
    
    @Column(name = "base_url_override", length = 500)
    @Size(max = 500)
    private String baseUrlOverride;
    
    @Column(name = "request_headers", columnDefinition = "jsonb")
    private Map<String, String> requestHeaders;
    
    @Column(name = "query_parameters", columnDefinition = "jsonb")
    private Map<String, String> queryParameters;
    
    @Column(name = "authentication_config", columnDefinition = "jsonb")
    private Map<String, Object> authenticationConfig;
    
    @Column(name = "timeout_ms")
    private Integer timeoutMs;
    
    @Column(name = "retry_attempts")
    private Integer retryAttempts;
    
    @Column(name = "circuit_breaker_config", columnDefinition = "jsonb")
    private Map<String, Object> circuitBreakerConfig;
    
    @Column(name = "rate_limiting_config", columnDefinition = "jsonb")
    private Map<String, Object> rateLimitingConfig;
    
    @Column(name = "request_transformation_config", columnDefinition = "jsonb")
    private Map<String, Object> requestTransformationConfig;
    
    @Column(name = "response_transformation_config", columnDefinition = "jsonb")
    private Map<String, Object> responseTransformationConfig;
    
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;
    
    @Column(name = "error_handling_config", columnDefinition = "jsonb")
    private Map<String, Object> errorHandlingConfig;
    
    @Column(name = "priority")
    private Integer priority = 1;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "description", length = 1000)
    @Size(max = 1000)
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    @Size(max = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    @Size(max = 100)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public CoreBankingEndpointConfiguration() {}
    
    public CoreBankingEndpointConfiguration(UUID coreBankingConfigId, String endpointName, 
                                          EndpointType endpointType, String endpointPath) {
        this.coreBankingConfigId = coreBankingConfigId;
        this.endpointName = endpointName;
        this.endpointType = endpointType;
        this.endpointPath = endpointPath;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getCoreBankingConfigId() {
        return coreBankingConfigId;
    }
    
    public void setCoreBankingConfigId(UUID coreBankingConfigId) {
        this.coreBankingConfigId = coreBankingConfigId;
    }
    
    public String getEndpointName() {
        return endpointName;
    }
    
    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }
    
    public EndpointType getEndpointType() {
        return endpointType;
    }
    
    public void setEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public String getEndpointPath() {
        return endpointPath;
    }
    
    public void setEndpointPath(String endpointPath) {
        this.endpointPath = endpointPath;
    }
    
    public String getBaseUrlOverride() {
        return baseUrlOverride;
    }
    
    public void setBaseUrlOverride(String baseUrlOverride) {
        this.baseUrlOverride = baseUrlOverride;
    }
    
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }
    
    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
    
    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }
    
    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }
    
    public Map<String, Object> getAuthenticationConfig() {
        return authenticationConfig;
    }
    
    public void setAuthenticationConfig(Map<String, Object> authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }
    
    public Integer getTimeoutMs() {
        return timeoutMs;
    }
    
    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
    
    public Integer getRetryAttempts() {
        return retryAttempts;
    }
    
    public void setRetryAttempts(Integer retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
    
    public Map<String, Object> getCircuitBreakerConfig() {
        return circuitBreakerConfig;
    }
    
    public void setCircuitBreakerConfig(Map<String, Object> circuitBreakerConfig) {
        this.circuitBreakerConfig = circuitBreakerConfig;
    }
    
    public Map<String, Object> getRateLimitingConfig() {
        return rateLimitingConfig;
    }
    
    public void setRateLimitingConfig(Map<String, Object> rateLimitingConfig) {
        this.rateLimitingConfig = rateLimitingConfig;
    }
    
    public Map<String, Object> getRequestTransformationConfig() {
        return requestTransformationConfig;
    }
    
    public void setRequestTransformationConfig(Map<String, Object> requestTransformationConfig) {
        this.requestTransformationConfig = requestTransformationConfig;
    }
    
    public Map<String, Object> getResponseTransformationConfig() {
        return responseTransformationConfig;
    }
    
    public void setResponseTransformationConfig(Map<String, Object> responseTransformationConfig) {
        this.responseTransformationConfig = responseTransformationConfig;
    }
    
    public Map<String, Object> getValidationRules() {
        return validationRules;
    }
    
    public void setValidationRules(Map<String, Object> validationRules) {
        this.validationRules = validationRules;
    }
    
    public Map<String, Object> getErrorHandlingConfig() {
        return errorHandlingConfig;
    }
    
    public void setErrorHandlingConfig(Map<String, Object> errorHandlingConfig) {
        this.errorHandlingConfig = errorHandlingConfig;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
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
    
    @Override
    public String toString() {
        return "CoreBankingEndpointConfiguration{" +
                "id=" + id +
                ", endpointName='" + endpointName + '\'' +
                ", endpointType=" + endpointType +
                ", httpMethod='" + httpMethod + '\'' +
                ", endpointPath='" + endpointPath + '\'' +
                ", isActive=" + isActive +
                '}';
    }
    
    /**
     * Endpoint Type Enumeration
     */
    public enum EndpointType {
        ACCOUNT_INFO,
        ACCOUNT_BALANCE,
        ACCOUNT_HOLDER,
        DEBIT_TRANSACTION,
        CREDIT_TRANSACTION,
        TRANSFER_TRANSACTION,
        TRANSACTION_STATUS,
        HOLD_FUNDS,
        RELEASE_FUNDS,
        ISO20022_PAYMENT,
        ISO20022_RESPONSE,
        ISO20022_VALIDATION,
        BATCH_TRANSACTIONS,
        RECONCILIATION,
        HEALTH_CHECK,
        CUSTOM
    }
}