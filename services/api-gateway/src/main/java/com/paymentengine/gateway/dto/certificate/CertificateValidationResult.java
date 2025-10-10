package com.paymentengine.gateway.dto.certificate;

import java.time.LocalDateTime;

/**
 * Result DTO for certificate validation
 */
public class CertificateValidationResult {
    
    private String status;
    private String message;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String issuerDN;
    private String subjectDN;
    
    // Constructors
    public CertificateValidationResult() {}
    
    public CertificateValidationResult(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private CertificateValidationResult result = new CertificateValidationResult();
        
        public Builder status(String status) {
            result.status = status;
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
    
    public boolean isValid() {
        return "VALID".equals(status);
    }
}