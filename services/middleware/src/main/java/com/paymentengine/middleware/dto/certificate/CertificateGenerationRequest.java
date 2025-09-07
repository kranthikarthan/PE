package com.paymentengine.middleware.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Certificate Generation Request DTO
 * 
 * Contains parameters for generating new certificates and key pairs.
 */
public class CertificateGenerationRequest {
    
    @NotBlank(message = "Subject DN is required")
    @Size(max = 500, message = "Subject DN must not exceed 500 characters")
    private String subjectDN;
    
    @Size(max = 100, message = "Tenant ID must not exceed 100 characters")
    private String tenantId;
    
    @Size(max = 50, message = "Certificate type must not exceed 50 characters")
    private String certificateType = "GENERATED";
    
    @Positive(message = "Validity days must be positive")
    private Integer validityDays;
    
    private List<String> keyUsage;
    
    private List<String> extendedKeyUsage;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private boolean includePrivateKey = true;
    
    private boolean includePublicKey = true;
    
    // Constructors
    public CertificateGenerationRequest() {}
    
    public CertificateGenerationRequest(String subjectDN, String tenantId) {
        this.subjectDN = subjectDN;
        this.tenantId = tenantId;
    }
    
    // Getters and Setters
    public String getSubjectDN() {
        return subjectDN;
    }
    
    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getCertificateType() {
        return certificateType;
    }
    
    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }
    
    public Integer getValidityDays() {
        return validityDays;
    }
    
    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }
    
    public List<String> getKeyUsage() {
        return keyUsage;
    }
    
    public void setKeyUsage(List<String> keyUsage) {
        this.keyUsage = keyUsage;
    }
    
    public List<String> getExtendedKeyUsage() {
        return extendedKeyUsage;
    }
    
    public void setExtendedKeyUsage(List<String> extendedKeyUsage) {
        this.extendedKeyUsage = extendedKeyUsage;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isIncludePrivateKey() {
        return includePrivateKey;
    }
    
    public void setIncludePrivateKey(boolean includePrivateKey) {
        this.includePrivateKey = includePrivateKey;
    }
    
    public boolean isIncludePublicKey() {
        return includePublicKey;
    }
    
    public void setIncludePublicKey(boolean includePublicKey) {
        this.includePublicKey = includePublicKey;
    }
    
    @Override
    public String toString() {
        return "CertificateGenerationRequest{" +
                "subjectDN='" + subjectDN + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", certificateType='" + certificateType + '\'' +
                ", validityDays=" + validityDays +
                ", keyUsage=" + keyUsage +
                ", extendedKeyUsage=" + extendedKeyUsage +
                ", description='" + description + '\'' +
                ", includePrivateKey=" + includePrivateKey +
                ", includePublicKey=" + includePublicKey +
                '}';
    }
}