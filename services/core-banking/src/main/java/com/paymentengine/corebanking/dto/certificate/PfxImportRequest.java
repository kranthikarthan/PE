package com.paymentengine.corebanking.dto.certificate;

import javax.validation.constraints.NotBlank;

/**
 * Request DTO for PFX certificate import
 */
public class PfxImportRequest {
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private String tenantId;
    
    private String certificateType;
    
    private String description;
    
    private boolean validateCertificate = true;
    
    private boolean extractPrivateKey = true;
    
    private boolean extractCertificateChain = true;
    
    // Constructors
    public PfxImportRequest() {}
    
    public PfxImportRequest(String password) {
        this.password = password;
    }
    
    // Getters and Setters
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isValidateCertificate() {
        return validateCertificate;
    }
    
    public void setValidateCertificate(boolean validateCertificate) {
        this.validateCertificate = validateCertificate;
    }
    
    public boolean isExtractPrivateKey() {
        return extractPrivateKey;
    }
    
    public void setExtractPrivateKey(boolean extractPrivateKey) {
        this.extractPrivateKey = extractPrivateKey;
    }
    
    public boolean isExtractCertificateChain() {
        return extractCertificateChain;
    }
    
    public void setExtractCertificateChain(boolean extractCertificateChain) {
        this.extractCertificateChain = extractCertificateChain;
    }
}