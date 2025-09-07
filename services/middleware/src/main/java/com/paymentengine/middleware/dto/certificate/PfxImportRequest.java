package com.paymentengine.middleware.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * PFX Import Request DTO
 * 
 * Contains parameters for importing .pfx certificate files.
 */
public class PfxImportRequest {
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @Size(max = 100, message = "Tenant ID must not exceed 100 characters")
    private String tenantId;
    
    @Size(max = 50, message = "Certificate type must not exceed 50 characters")
    private String certificateType = "PFX_IMPORTED";
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private boolean validateCertificate = true;
    
    private boolean extractPrivateKey = true;
    
    private boolean extractCertificateChain = true;
    
    // Constructors
    public PfxImportRequest() {}
    
    public PfxImportRequest(String password, String tenantId) {
        this.password = password;
        this.tenantId = tenantId;
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
    
    @Override
    public String toString() {
        return "PfxImportRequest{" +
                "tenantId='" + tenantId + '\'' +
                ", certificateType='" + certificateType + '\'' +
                ", description='" + description + '\'' +
                ", validateCertificate=" + validateCertificate +
                ", extractPrivateKey=" + extractPrivateKey +
                ", extractCertificateChain=" + extractCertificateChain +
                '}';
    }
}