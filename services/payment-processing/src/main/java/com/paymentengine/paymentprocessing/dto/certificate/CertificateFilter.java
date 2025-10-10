package com.paymentengine.paymentprocessing.dto.certificate;

import java.time.LocalDateTime;

/**
 * Certificate Filter DTO
 * 
 * Contains filtering criteria for certificate queries.
 */
public class CertificateFilter {
    
    private String tenantId;
    private String certificateType;
    private String status;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String subjectDN;
    private String issuerDN;
    private String serialNumber;
    private boolean includeExpired = false;
    private boolean includeRotated = false;
    
    // Constructors
    public CertificateFilter() {}
    
    public CertificateFilter(String tenantId) {
        this.tenantId = tenantId;
    }
    
    // Getters and Setters
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public boolean isIncludeExpired() {
        return includeExpired;
    }
    
    public void setIncludeExpired(boolean includeExpired) {
        this.includeExpired = includeExpired;
    }
    
    public boolean isIncludeRotated() {
        return includeRotated;
    }
    
    public void setIncludeRotated(boolean includeRotated) {
        this.includeRotated = includeRotated;
    }
    
    @Override
    public String toString() {
        return "CertificateFilter{" +
                "tenantId='" + tenantId + '\'' +
                ", certificateType='" + certificateType + '\'' +
                ", status='" + status + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", subjectDN='" + subjectDN + '\'' +
                ", issuerDN='" +issuerDN + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", includeExpired=" + includeExpired +
                ", includeRotated=" + includeRotated +
                '}';
    }
}