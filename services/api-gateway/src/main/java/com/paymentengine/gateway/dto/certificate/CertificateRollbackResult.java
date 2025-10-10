package com.paymentengine.gateway.dto.certificate;

import java.time.LocalDateTime;

/**
 * Result DTO for certificate rollback operation
 */
public class CertificateRollbackResult {
    
    private boolean success;
    private String message;
    private String previousCertificateId;
    private String currentCertificateId;
    private LocalDateTime rollbackTimestamp;
    
    // Constructors
    public CertificateRollbackResult() {}
    
    public CertificateRollbackResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.rollbackTimestamp = LocalDateTime.now();
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private CertificateRollbackResult result = new CertificateRollbackResult();
        
        public Builder success(boolean success) {
            result.success = success;
            return this;
        }
        
        public Builder message(String message) {
            result.message = message;
            return this;
        }
        
        public Builder previousCertificateId(String previousCertificateId) {
            result.previousCertificateId = previousCertificateId;
            return this;
        }
        
        public Builder currentCertificateId(String currentCertificateId) {
            result.currentCertificateId = currentCertificateId;
            return this;
        }
        
        public Builder rollbackTimestamp(LocalDateTime rollbackTimestamp) {
            result.rollbackTimestamp = rollbackTimestamp;
            return this;
        }
        
        public CertificateRollbackResult build() {
            return result;
        }
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPreviousCertificateId() {
        return previousCertificateId;
    }
    
    public void setPreviousCertificateId(String previousCertificateId) {
        this.previousCertificateId = previousCertificateId;
    }
    
    public String getCurrentCertificateId() {
        return currentCertificateId;
    }
    
    public void setCurrentCertificateId(String currentCertificateId) {
        this.currentCertificateId = currentCertificateId;
    }
    
    public LocalDateTime getRollbackTimestamp() {
        return rollbackTimestamp;
    }
    
    public void setRollbackTimestamp(LocalDateTime rollbackTimestamp) {
        this.rollbackTimestamp = rollbackTimestamp;
    }
}