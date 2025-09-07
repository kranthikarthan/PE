package com.paymentengine.middleware.dto.certificate;

import java.time.LocalDateTime;

/**
 * Certificate Generation Result DTO
 * 
 * Contains the result of certificate generation including
 * certificate details and PEM-formatted strings.
 */
public class CertificateGenerationResult {
    
    private String certificateId;
    private String subjectDN;
    private String issuerDN;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String serialNumber;
    private String publicKeyAlgorithm;
    private Integer keySize;
    private String signatureAlgorithm;
    private String certificatePem;
    private String privateKeyPem;
    private String publicKeyPem;
    
    // Constructors
    public CertificateGenerationResult() {}
    
    public CertificateGenerationResult(String certificateId, String subjectDN, String issuerDN) {
        this.certificateId = certificateId;
        this.subjectDN = subjectDN;
        this.issuerDN = issuerDN;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private CertificateGenerationResult result = new CertificateGenerationResult();
        
        public Builder certificateId(String certificateId) {
            result.certificateId = certificateId;
            return this;
        }
        
        public Builder subjectDN(String subjectDN) {
            result.subjectDN = subjectDN;
            return this;
        }
        
        public Builder issuerDN(String issuerDN) {
            result.issuerDN = issuerDN;
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
        
        public Builder serialNumber(String serialNumber) {
            result.serialNumber = serialNumber;
            return this;
        }
        
        public Builder publicKeyAlgorithm(String publicKeyAlgorithm) {
            result.publicKeyAlgorithm = publicKeyAlgorithm;
            return this;
        }
        
        public Builder keySize(Integer keySize) {
            result.keySize = keySize;
            return this;
        }
        
        public Builder signatureAlgorithm(String signatureAlgorithm) {
            result.signatureAlgorithm = signatureAlgorithm;
            return this;
        }
        
        public Builder certificatePem(String certificatePem) {
            result.certificatePem = certificatePem;
            return this;
        }
        
        public Builder privateKeyPem(String privateKeyPem) {
            result.privateKeyPem = privateKeyPem;
            return this;
        }
        
        public Builder publicKeyPem(String publicKeyPem) {
            result.publicKeyPem = publicKeyPem;
            return this;
        }
        
        public CertificateGenerationResult build() {
            return result;
        }
    }
    
    // Getters and Setters
    public String getCertificateId() {
        return certificateId;
    }
    
    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
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
    
    public String getCertificatePem() {
        return certificatePem;
    }
    
    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }
    
    public String getPrivateKeyPem() {
        return privateKeyPem;
    }
    
    public void setPrivateKeyPem(String privateKeyPem) {
        this.privateKeyPem = privateKeyPem;
    }
    
    public String getPublicKeyPem() {
        return publicKeyPem;
    }
    
    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }
    
    @Override
    public String toString() {
        return "CertificateGenerationResult{" +
                "certificateId='" + certificateId + '\'' +
                ", subjectDN='" + subjectDN + '\'' +
                ", issuerDN='" + issuerDN + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", serialNumber='" + serialNumber + '\'' +
                ", publicKeyAlgorithm='" + publicKeyAlgorithm + '\'' +
                ", keySize=" + keySize +
                ", signatureAlgorithm='" + signatureAlgorithm + '\'' +
                '}';
    }
}