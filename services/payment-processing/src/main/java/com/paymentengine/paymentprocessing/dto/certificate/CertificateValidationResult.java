package com.paymentengine.paymentprocessing.dto.certificate;

import java.time.LocalDateTime;

/**
 * Certificate Validation Result DTO
 * 
 * Contains the result of certificate validation including
 * validation status and certificate details.
 */
public class CertificateValidationResult {
    
    private String status;
    private String message;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String issuerDN;
    private String subjectDN;
    private String serialNumber;
    private String publicKeyAlgorithm;
    private String signatureAlgorithm;
    private boolean isValid;
    private String validationDetails;
    
    // Constructors
    public CertificateValidationResult() {}
    
    public CertificateValidationResult(String status, String message) {
        this.status = status;
        this.message = message;
        this.isValid = "VALID".equals(status);
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private CertificateValidationResult result = new CertificateValidationResult();
        
        public Builder status(String status) {
            result.status = status;
            result.isValid = "VALID".equals(status);
            return this;
        }
        
        public Builder message(String message) {
            result.message = message;
            return this;
        }
        
        public Builder validFrom(LocalDateTime validFrom) {
            result.validFrom = validFrom;
            return this;
        }
        
        public Builder validTo(LocalDateTime validTo) {
            result.validTo = validTo;
            return this;
        }
        
        public Builder issuerDN(String issuerDN) {
            result.issuerDN = issuerDN;
            return this;
        }
        
        public Builder subjectDN(String subjectDN) {
            result.subjectDN = subjectDN;
            return this;
        }
        
        public Builder serialNumber(String serialNumber) {
            result.serialNumber = serialNumber;
            return this;
        }
        
        public Builder publicKeyAlgorithm(String publicKeyAlgorithm) {
            result.publicKeyAlgorithm = publicKeyAlgorithm;
            return this;
        }
        
        public Builder signatureAlgorithm(String signatureAlgorithm) {
            result.signatureAlgorithm = signatureAlgorithm;
            return this;
        }
        
        public Builder validationDetails(String validationDetails) {
            result.validationDetails = validationDetails;
            return this;
        }
        
        public CertificateValidationResult build() {
            return result;
        }
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.isValid = "VALID".equals(status);
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
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
    
    public String getIssuerDN() {
        return issuerDN;
    }
    
    public void setIssuerDN(String issuerDN) {
        this.issuerDN = issuerDN;
    }
    
    public String getSubjectDN() {
        return subjectDN;
    }
    
    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public String getPublicKeyAlgorithm() {
        return publicKeyAlgorithm;
    }
    
    public void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
        this.publicKeyAlgorithm = publicKeyAlgorithm;
    }
    
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }
    
    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public void setValid(boolean valid) {
        this.isValid = valid;
    }
    
    public String getValidationDetails() {
        return validationDetails;
    }
    
    public void setValidationDetails(String validationDetails) {
        this.validationDetails = validationDetails;
    }
    
    @Override
    public String toString() {
        return "CertificateValidationResult{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", issuerDN='" + issuerDN + '\'' +
                ", subjectDN='" + subjectDN + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", publicKeyAlgorithm='" + publicKeyAlgorithm + '\'' +
                ", signatureAlgorithm='" + signatureAlgorithm + '\'' +
                ", isValid=" + isValid +
                '}';
    }
}