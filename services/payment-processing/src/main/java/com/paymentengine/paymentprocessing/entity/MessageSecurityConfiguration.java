package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for message security configuration (encryption and digital signatures)
 * Supports configuration at tenant, payment type, and endpoint levels
 */
@Entity
@Table(name = "message_security_configuration")
public class MessageSecurityConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Column(name = "configuration_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConfigurationLevel configurationLevel;
    
    @Column(name = "payment_type")
    private String paymentType; // SEPA, SWIFT, ACH, CARD, CUSTOM
    
    @Column(name = "service_type")
    private String serviceType; // fraud, clearing, banking, custom
    
    @Column(name = "endpoint")
    private String endpoint; // /fraud, /clearing, /banking, etc.
    
    @Column(name = "direction", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageDirection direction; // INCOMING, OUTGOING, BOTH
    
    // Encryption Configuration
    @Column(name = "encryption_enabled", nullable = false)
    private Boolean encryptionEnabled = false;
    
    @Column(name = "encryption_algorithm")
    @Enumerated(EnumType.STRING)
    private EncryptionAlgorithm encryptionAlgorithm;
    
    @Column(name = "encryption_key_id")
    private String encryptionKeyId;
    
    @Column(name = "encryption_key_version")
    private String encryptionKeyVersion;
    
    @Column(name = "encryption_provider")
    @Enumerated(EnumType.STRING)
    private KeyProvider encryptionProvider;
    
    // Digital Signature Configuration
    @Column(name = "signature_enabled", nullable = false)
    private Boolean signatureEnabled = false;
    
    @Column(name = "signature_algorithm")
    @Enumerated(EnumType.STRING)
    private SignatureAlgorithm signatureAlgorithm;
    
    @Column(name = "signature_key_id")
    private String signatureKeyId;
    
    @Column(name = "signature_key_version")
    private String signatureKeyVersion;
    
    @Column(name = "signature_provider")
    @Enumerated(EnumType.STRING)
    private KeyProvider signatureProvider;
    
    // Message Format Configuration
    @Column(name = "message_format")
    @Enumerated(EnumType.STRING)
    private MessageFormat messageFormat;
    
    @Column(name = "content_type")
    private String contentType; // application/json, application/xml, text/plain
    
    @Column(name = "charset")
    private String charset = "UTF-8";
    
    // Security Headers Configuration
    @Column(name = "security_headers_enabled", nullable = false)
    private Boolean securityHeadersEnabled = false;
    
    @Column(name = "security_headers_config", columnDefinition = "TEXT")
    private String securityHeadersConfig; // JSON configuration for security headers
    
    // Status and Metadata
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata
    
    // Constructors
    public MessageSecurityConfiguration() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Enums
    public enum ConfigurationLevel {
        CLEARING_SYSTEM,    // Global clearing system level
        TENANT,             // Tenant-specific level
        PAYMENT_TYPE,       // Payment type specific level
        ENDPOINT            // Endpoint specific level
    }
    
    public enum MessageDirection {
        INCOMING,           // Incoming messages
        OUTGOING,           // Outgoing messages
        BOTH                // Both incoming and outgoing
    }
    
    public enum EncryptionAlgorithm {
        AES_128_GCM,        // AES-128-GCM
        AES_256_GCM,        // AES-256-GCM
        AES_128_CBC,        // AES-128-CBC
        AES_256_CBC,        // AES-256-CBC
        RSA_OAEP_2048,      // RSA-OAEP with 2048-bit keys
        RSA_OAEP_4096,      // RSA-OAEP with 4096-bit keys
        CHACHA20_POLY1305,  // ChaCha20-Poly1305
        SM4_GCM,            // SM4-GCM (Chinese standard)
        NONE                // No encryption
    }
    
    public enum SignatureAlgorithm {
        RSA_SHA256,         // RSA with SHA-256
        RSA_SHA384,         // RSA with SHA-384
        RSA_SHA512,         // RSA with SHA-512
        ECDSA_SHA256,       // ECDSA with SHA-256
        ECDSA_SHA384,       // ECDSA with SHA-384
        ECDSA_SHA512,       // ECDSA with SHA-512
        ED25519,            // Ed25519
        SM2_SM3,            // SM2 with SM3 (Chinese standard)
        HMAC_SHA256,        // HMAC with SHA-256
        HMAC_SHA384,        // HMAC with SHA-384
        HMAC_SHA512,        // HMAC with SHA-512
        NONE                // No signature
    }
    
    public enum KeyProvider {
        AZURE_KEY_VAULT,    // Azure Key Vault
        AWS_KMS,            // AWS Key Management Service
        HASHICORP_VAULT,    // HashiCorp Vault
        LOCAL_STORAGE,      // Local storage
        CUSTOM              // Custom key provider
    }
    
    public enum MessageFormat {
        JSON,               // JSON format
        XML,                // XML format
        ISO20022,           // ISO 20022 format
        CUSTOM,             // Custom format
        BINARY              // Binary format
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
    
    public ConfigurationLevel getConfigurationLevel() {
        return configurationLevel;
    }
    
    public void setConfigurationLevel(ConfigurationLevel configurationLevel) {
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
    
    public MessageDirection getDirection() {
        return direction;
    }
    
    public void setDirection(MessageDirection direction) {
        this.direction = direction;
    }
    
    public Boolean getEncryptionEnabled() {
        return encryptionEnabled;
    }
    
    public void setEncryptionEnabled(Boolean encryptionEnabled) {
        this.encryptionEnabled = encryptionEnabled;
    }
    
    public EncryptionAlgorithm getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }
    
    public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
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
    
    public KeyProvider getEncryptionProvider() {
        return encryptionProvider;
    }
    
    public void setEncryptionProvider(KeyProvider encryptionProvider) {
        this.encryptionProvider = encryptionProvider;
    }
    
    public Boolean getSignatureEnabled() {
        return signatureEnabled;
    }
    
    public void setSignatureEnabled(Boolean signatureEnabled) {
        this.signatureEnabled = signatureEnabled;
    }
    
    public SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }
    
    public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
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
    
    public KeyProvider getSignatureProvider() {
        return signatureProvider;
    }
    
    public void setSignatureProvider(KeyProvider signatureProvider) {
        this.signatureProvider = signatureProvider;
    }
    
    public MessageFormat getMessageFormat() {
        return messageFormat;
    }
    
    public void setMessageFormat(MessageFormat messageFormat) {
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