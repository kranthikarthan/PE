package com.paymentengine.middleware.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Core Banking Configuration Entity
 * 
 * Stores configuration for core banking integration per tenant,
 * including adapter type (REST/gRPC), endpoints, authentication, etc.
 */
@Entity
@Table(name = "core_banking_configurations", schema = "payment_engine")
public class CoreBankingConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String tenantId;
    
    @Column(name = "adapter_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private AdapterType adapterType;
    
    @Column(name = "base_url", length = 500)
    @Size(max = 500)
    private String baseUrl;
    
    @Column(name = "grpc_host", length = 255)
    @Size(max = 255)
    private String grpcHost;
    
    @Column(name = "grpc_port")
    private Integer grpcPort;
    
    @Column(name = "authentication_method", length = 50)
    @Size(max = 50)
    private String authenticationMethod;
    
    @Column(name = "api_key", length = 500)
    @Size(max = 500)
    private String apiKey;
    
    @Column(name = "username", length = 100)
    @Size(max = 100)
    private String username;
    
    @Column(name = "password", length = 500)
    @Size(max = 500)
    private String password;
    
    @Column(name = "certificate_path", length = 500)
    @Size(max = 500)
    private String certificatePath;
    
    @Column(name = "processing_mode", length = 20)
    @Size(max = 20)
    private String processingMode = "SYNC";
    
    @Column(name = "message_format", length = 20)
    @Size(max = 20)
    private String messageFormat = "JSON";
    
    @Column(name = "timeout_ms")
    private Integer timeoutMs = 30000;
    
    @Column(name = "retry_attempts")
    private Integer retryAttempts = 3;
    
    @Column(name = "priority")
    private Integer priority = 1;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "bank_code", length = 20)
    @Size(max = 20)
    private String bankCode;
    
    @Column(name = "bank_name", length = 255)
    @Size(max = 255)
    private String bankName;
    
    @Column(name = "additional_config", columnDefinition = "jsonb")
    private Map<String, Object> additionalConfig;
    
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
    public CoreBankingConfiguration() {}
    
    public CoreBankingConfiguration(String tenantId, AdapterType adapterType) {
        this.tenantId = tenantId;
        this.adapterType = adapterType;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public AdapterType getAdapterType() {
        return adapterType;
    }
    
    public void setAdapterType(AdapterType adapterType) {
        this.adapterType = adapterType;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getGrpcHost() {
        return grpcHost;
    }
    
    public void setGrpcHost(String grpcHost) {
        this.grpcHost = grpcHost;
    }
    
    public Integer getGrpcPort() {
        return grpcPort;
    }
    
    public void setGrpcPort(Integer grpcPort) {
        this.grpcPort = grpcPort;
    }
    
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }
    
    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getCertificatePath() {
        return certificatePath;
    }
    
    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }
    
    public String getProcessingMode() {
        return processingMode;
    }
    
    public void setProcessingMode(String processingMode) {
        this.processingMode = processingMode;
    }
    
    public String getMessageFormat() {
        return messageFormat;
    }
    
    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
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
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public Map<String, Object> getAdditionalConfig() {
        return additionalConfig;
    }
    
    public void setAdditionalConfig(Map<String, Object> additionalConfig) {
        this.additionalConfig = additionalConfig;
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
        return "CoreBankingConfiguration{" +
                "id=" + id +
                ", tenantId='" + tenantId + '\'' +
                ", adapterType=" + adapterType +
                ", baseUrl='" + baseUrl + '\'' +
                ", grpcHost='" + grpcHost + '\'' +
                ", grpcPort=" + grpcPort +
                ", authenticationMethod='" + authenticationMethod + '\'' +
                ", processingMode='" + processingMode + '\'' +
                ", messageFormat='" + messageFormat + '\'' +
                ", isActive=" + isActive +
                ", bankCode='" + bankCode + '\'' +
                '}';
    }
    
    /**
     * Adapter Type Enumeration
     */
    public enum AdapterType {
        REST,
        GRPC,
        INTERNAL
    }
}