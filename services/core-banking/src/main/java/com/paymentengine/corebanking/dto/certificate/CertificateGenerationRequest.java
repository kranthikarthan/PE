package com.paymentengine.corebanking.dto.certificate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.util.List;

/**
 * Request DTO for certificate generation
 */
public class CertificateGenerationRequest {
    
    @NotBlank(message = "Subject DN is required")
    private String subjectDN;
    
    private String tenantId;
    
    private String certificateType;
    
    @Min(value = 1, message = "Validity days must be at least 1")
    @Max(value = 3650, message = "Validity days cannot exceed 3650")
    private Integer validityDays;
    
    private List<String> keyUsage;
    
    private List<String> extendedKeyUsage;
    
    private String description;
    
    private Boolean includePrivateKey = true;
    
    private Boolean includePublicKey = true;
    
    // Constructors
    public CertificateGenerationRequest() {}
    
    public CertificateGenerationRequest(String subjectDN) {
        this.subjectDN = subjectDN;
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
    
    public Boolean getIncludePrivateKey() {
        return includePrivateKey;
    }
    
    public void setIncludePrivateKey(Boolean includePrivateKey) {
        this.includePrivateKey = includePrivateKey;
    }
    
    public Boolean getIncludePublicKey() {
        return includePublicKey;
    }
    
    public void setIncludePublicKey(Boolean includePublicKey) {
        this.includePublicKey = includePublicKey;
    }
}