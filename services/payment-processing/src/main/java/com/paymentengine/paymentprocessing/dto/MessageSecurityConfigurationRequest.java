package com.paymentengine.paymentprocessing.dto;

import com.paymentengine.paymentprocessing.entity.MessageSecurityConfiguration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for message security configuration
 */
public class MessageSecurityConfigurationRequest {
    
    private UUID id;
    
    @NotBlank
    private String tenantId;
    
    @NotNull
    private MessageSecurityConfiguration.ConfigurationLevel configurationLevel;
    
    private String paymentType; // SEPA, SWIFT, ACH, CARD, CUSTOM
    
    private String serviceType; // fraud, clearing, banking, custom
    
    private String endpoint; // /fraud, /clearing, /banking, etc.
    
    @NotNull
    private MessageSecurityConfiguration.MessageDirection direction; // INCOMING, OUTGOING, BOTH
    
    // Encryption Configuration
    @NotNull
    private Boolean encryptionEnabled = false;
    
    private MessageSecurityConfiguration.EncryptionAlgorithm encryptionAlgorithm;
    
    private String encryptionKeyId;
    
    private String encryptionKeyVersion;
    
    private MessageSecurityConfiguration.KeyProvider encryptionProvider;
    
    // Digital Signature Configuration
    @NotNull
    private Boolean signatureEnabled = false;
    
    private MessageSecurityConfiguration.SignatureAlgorithm signatureAlgorithm;
    
    private String signatureKeyId;
    
    private String signatureKeyVersion;
    
    private MessageSecurityConfiguration.KeyProvider signatureProvider;
    
    // Message Format Configuration
    private MessageSecurityConfiguration.MessageFormat messageFormat;
    
    private String contentType; // application/json, application/xml, text/plain
    
    private String charset = "UTF-8";
    
    // Security Headers Configuration
    @NotNull
    private Boolean securityHeadersEnabled = false;
    
    private String securityHeadersConfig; // JSON configuration for security headers
    
    // Metadata
    @NotBlank
    private String createdBy;
    
    private String metadata; // JSON metadata
    
    // Constructors
    public MessageSecurityConfigurationRequest() {}
    
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
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}