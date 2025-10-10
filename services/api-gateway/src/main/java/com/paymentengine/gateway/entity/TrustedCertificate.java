package com.paymentengine.gateway.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing trusted certificate information in the API Gateway service
 */
@Entity
@Table(name = "trusted_certificates", schema = "payment_engine")
public class TrustedCertificate {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "alias", nullable = false, length = 100)
    private String alias;
    
    @Column(name = "subject_dn", nullable = false, length = 500)
    private String subjectDN;
    
    @Column(name = "issuer_dn", nullable = false, length = 500)
    private String issuerDN;
    
    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;
    
    @Column(name = "public_key_algorithm", length = 50)
    private String publicKeyAlgorithm;
    
    @Column(name = "key_size")
    private Integer keySize;
    
    @Column(name = "signature_algorithm", length = 50)
    private String signatureAlgorithm;
    
    @Column(name = "certificate_type", length = 50)
    private String certificateType;
    
    @Column(name = "tenant_id", length = 100)
    private String tenantId;
    
    @Column(name = "status", length = 20)
    private String status;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "source", length = 100)
    private String source;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TrustedCertificate() {}
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String getSubjectDN() {
        return subjectDN;
    }
    
    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }
    
    public String getIssuerDN() {
        return issuerDN;
    }
    
    public void setIssuerDN(String issuerDN) {
        this.issuerDN = issuerDN;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public LocalDateTime getValidFrom() {
        return validFrom;
    }
    
    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }
    
    public LocalDateTime getValidTo() {
        return validTo;
    }
    
    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }
    
    public String getPublicKeyAlgorithm() {
        return publicKeyAlgorithm;
    }
    
    public void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
        this.publicKeyAlgorithm = publicKeyAlgorithm;
    }
    
    public Integer getKeySize() {
        return keySize;
    }
    
    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }
    
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }
    
    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }
    
    public String getCertificateType() {
        return certificateType;
    }
    
    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
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
}