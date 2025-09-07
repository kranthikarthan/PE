package com.paymentengine.paymentprocessing.dto;

import com.paymentengine.paymentprocessing.entity.MessageSecurityConfiguration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for message security configuration
 */
public class MessageSecurityConfigurationResponse {
    
    private UUID id;
    private String tenantId;
    private MessageSecurityConfiguration.ConfigurationLevel configurationLevel;
    private String paymentType;
    private String serviceType;
    private String endpoint;
    private MessageSecurityConfiguration.MessageDirection direction;
    
    // Encryption Configuration
    private Boolean encryptionEnabled;
    private MessageSecurityConfiguration.EncryptionAlgorithm encryptionAlgorithm;
    private String encryptionKeyId;
    private String encryptionKeyVersion;
    private MessageSecurityConfiguration.KeyProvider encryptionProvider;
    
    // Digital Signature Configuration
    private Boolean signatureEnabled;
    private MessageSecurityConfiguration.SignatureAlgorithm signatureAlgorithm;
    private String signatureKeyId;
    private String signatureKeyVersion;
    private MessageSecurityConfiguration.KeyProvider signatureProvider;
    
    // Message Format Configuration
    private MessageSecurityConfiguration.MessageFormat messageFormat;
    private String contentType;
    private String charset;
    
    // Security Headers Configuration
    private Boolean securityHeadersEnabled;
    private String securityHeadersConfig;
    
    // Status and Metadata
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String metadata;
    
    // Constructors
    public MessageSecurityConfigurationResponse() {}
    
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
    
    public MessageSecurityConfiguration.ConfigurationLevel getConfigurationLevel() {
        return configurationLevel;
    }
    
    public void setConfigurationLevel(MessageSecurityConfiguration.ConfigurationLevel configurationLevel) {
        this.configurationLevel = configurationLevel;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public MessageSecurityConfiguration.MessageDirection getDirection() {
        return direction;
    }
    
    public void setDirection(MessageSecurityConfiguration.MessageDirection direction) {
        this.direction = direction;
    }
    
    public Boolean getEncryptionEnabled() {
        return encryptionEnabled;
    }
    
    public void setEncryptionEnabled(Boolean encryptionEnabled) {
        this.encryptionEnabled = encryptionEnabled;
    }
    
    public MessageSecurityConfiguration.EncryptionAlgorithm getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }
    
    public void setEncryptionAlgorithm(MessageSecurityConfiguration.EncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }
    
    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }
    
    public void setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
    }
    
    public String getEncryptionKeyVersion() {
        return encryptionKeyVersion;
    }
    
    public void setEncryptionKeyVersion(String encryptionKeyVersion) {
        this.encryptionKeyVersion = encryptionKeyVersion;
    }
    
    public MessageSecurityConfiguration.KeyProvider getEncryptionProvider() {
        return encryptionProvider;
    }
    
    public void setEncryptionProvider(MessageSecurityConfiguration.KeyProvider encryptionProvider) {
        this.encryptionProvider = encryptionProvider;
    }
    
    public Boolean getSignatureEnabled() {
        return signatureEnabled;
    }
    
    public void setSignatureEnabled(Boolean signatureEnabled) {
        this.signatureEnabled = signatureEnabled;
    }
    
    public MessageSecurityConfiguration.SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }
    
    public void setSignatureAlgorithm(MessageSecurityConfiguration.SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }
    
    public String getSignatureKeyId() {
        return signatureKeyId;
    }
    
    public void setSignatureKeyId(String signatureKeyId) {
        this.signatureKeyId = signatureKeyId;
    }
    
    public String getSignatureKeyVersion() {
        return signatureKeyVersion;
    }
    
    public void setSignatureKeyVersion(String signatureKeyVersion) {
        this.signatureKeyVersion = signatureKeyVersion;
    }
    
    public MessageSecurityConfiguration.KeyProvider getSignatureProvider() {
        return signatureProvider;
    }
    
    public void setSignatureProvider(MessageSecurityConfiguration.KeyProvider signatureProvider) {
        this.signatureProvider = signatureProvider;
    }
    
    public MessageSecurityConfiguration.MessageFormat getMessageFormat() {
        return messageFormat;
    }
    
    public void setMessageFormat(MessageSecurityConfiguration.MessageFormat messageFormat) {
        this.messageFormat = messageFormat;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public String getCharset() {
        return charset;
    }
    
    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    public Boolean getSecurityHeadersEnabled() {
        return securityHeadersEnabled;
    }
    
    public void setSecurityHeadersEnabled(Boolean securityHeadersEnabled) {
        this.securityHeadersEnabled = securityHeadersEnabled;
    }
    
    public String getSecurityHeadersConfig() {
        return securityHeadersConfig;
    }
    
    public void setSecurityHeadersConfig(String securityHeadersConfig) {
        this.securityHeadersConfig = securityHeadersConfig;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}