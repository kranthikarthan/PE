package com.paymentengine.paymentprocessing.dto;

import com.paymentengine.paymentprocessing.entity.MessageSecurityConfiguration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for resolved message security configuration
 */
public class ResolvedMessageSecurityConfiguration {
    
    private String tenantId;
    private String serviceType;
    private String endpoint;
    private String paymentType;
    private MessageSecurityConfiguration.MessageDirection direction;
    private MessageSecurityConfiguration.ConfigurationLevel configurationLevel;
    private UUID configurationId;
    
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
    
    // Resolution Metadata
    private LocalDateTime resolvedAt;
    
    // Constructors
    public ResolvedMessageSecurityConfiguration() {}
    
    // Getters and Setters
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public MessageSecurityConfiguration.MessageDirection getDirection() {
        return direction;
    }
    
    public void setDirection(MessageSecurityConfiguration.MessageDirection direction) {
        this.direction = direction;
    }
    
    public MessageSecurityConfiguration.ConfigurationLevel getConfigurationLevel() {
        return configurationLevel;
    }
    
    public void setConfigurationLevel(MessageSecurityConfiguration.ConfigurationLevel configurationLevel) {
        this.configurationLevel = configurationLevel;
    }
    
    public UUID getConfigurationId() {
        return configurationId;
    }
    
    public void setConfigurationId(UUID configurationId) {
        this.configurationId = configurationId;
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
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}