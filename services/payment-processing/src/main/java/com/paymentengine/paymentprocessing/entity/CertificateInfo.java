package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Certificate Information Entity
 * 
 * Stores metadata about certificates including generated certificates
 * and imported PFX certificates.
 */
@Entity
@Table(name = "certificate_info", schema = "payment_engine")
public class CertificateInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "subject_dn", nullable = false, length = 500)
    @NotNull
    @Size(max = 500)
    private String subjectDN;
    
    @Column(name = "issuer_dn", nullable = false, length = 500)
    @NotNull
    @Size(max = 500)
    private String issuerDN;
    
    @Column(name = "serial_number", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String serialNumber;
    
    @Column(name = "valid_from", nullable = false)
    @NotNull
    private LocalDateTime validFrom;
    
    @Column(name = "valid_to", nullable = false)
    @NotNull
    private LocalDateTime validTo;
    
    @Column(name = "public_key_algorithm", length = 50)
    @Size(max = 50)
    private String publicKeyAlgorithm;
    
    @Column(name = "key_size")
    private Integer keySize;
    
    @Column(name = "signature_algorithm", length = 50)
    @Size(max = 50)
    private String signatureAlgorithm;
    
    @Column(name = "certificate_type", length = 50)
    @Size(max = 50)
    private String certificateType;
    
    @Column(name = "tenant_id", length = 100)
    @Size(max = 100)
    private String tenantId;
    
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private CertificateStatus status = CertificateStatus.ACTIVE;
    
    @Column(name = "alias", length = 100)
    @Size(max = 100)
    private String alias;
    
    @Column(name = "validation_status", length = 20)
    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus;
    
    @Column(name = "validation_message", length = 500)
    @Size(max = 500)
    private String validationMessage;
    
    @Column(name = "last_validated")
    private LocalDateTime lastValidated;
    
    @Column(name = "rotated_to")
    private UUID rotatedTo;
    
    @Column(name = "rotated_at")
    private LocalDateTime rotatedAt;
    
    @Column(name = "rolled_back_at")
    private LocalDateTime rolledBackAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
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
    public CertificateInfo() {}
    
    public CertificateInfo(String subjectDN, String issuerDN, String serialNumber) {
        this.subjectDN = subjectDN;
        this.issuerDN = issuerDN;
        this.serialNumber = serialNumber;
    }
    
    // Business methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(validTo);
    }
    
    public boolean isExpiringSoon(int daysAhead) {
        return LocalDateTime.now().plusDays(daysAhead).isAfter(validTo);
    }
    
    public boolean isActive() {
        return status == CertificateStatus.ACTIVE && !isExpired();
    }
    
    public boolean isRotated() {
        return status == CertificateStatus.ROTATED;
    }
    
    public boolean isValidated() {
        return validationStatus == ValidationStatus.VALID;
    }
    
    public long getDaysUntilExpiry() {
        return java.time.Duration.between(LocalDateTime.now(), validTo).toDays();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
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
    
    public CertificateStatus getStatus() {
        return status;
    }
    
    public void setStatus(CertificateStatus status) {
        this.status = status;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }
    
    public void setValidationStatus(ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }
    
    public String getValidationMessage() {
        return validationMessage;
    }
    
    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
    
    public LocalDateTime getLastValidated() {
        return lastValidated;
    }
    
    public void setLastValidated(LocalDateTime lastValidated) {
        this.lastValidated = lastValidated;
    }
    
    public UUID getRotatedTo() {
        return rotatedTo;
    }
    
    public void setRotatedTo(UUID rotatedTo) {
        this.rotatedTo = rotatedTo;
    }
    
    public LocalDateTime getRotatedAt() {
        return rotatedAt;
    }
    
    public void setRotatedAt(LocalDateTime rotatedAt) {
        this.rotatedAt = rotatedAt;
    }
    
    public LocalDateTime getRolledBackAt() {
        return rolledBackAt;
    }
    
    public void setRolledBackAt(LocalDateTime rolledBackAt) {
        this.rolledBackAt = rolledBackAt;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CertificateInfo that = (CertificateInfo) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "CertificateInfo{" +
                "id=" + id +
                ", subjectDN='" + subjectDN + '\'' +
                ", issuerDN='" + issuerDN + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", certificateType='" + certificateType + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", status=" + status +
                '}';
    }
    
    /**
     * Certificate Status Enumeration
     */
    public enum CertificateStatus {
        ACTIVE,
        INACTIVE,
        EXPIRED,
        ROTATED,
        REVOKED
    }
    
    /**
     * Validation Status Enumeration
     */
    public enum ValidationStatus {
        VALID,
        INVALID,
        PENDING,
        EXPIRED,
        REVOKED
    }
}