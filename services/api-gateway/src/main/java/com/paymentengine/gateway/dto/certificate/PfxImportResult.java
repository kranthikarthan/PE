package com.paymentengine.gateway.dto.certificate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Result DTO for PFX certificate import
 */
public class PfxImportResult {
    
    private UUID certificateId;
    private String subjectDN;
    private String issuerDN;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String serialNumber;
    private String publicKeyAlgorithm;
    private String signatureAlgorithm;
    private String certificatePem;
    private String privateKeyPem;
    private String alias;
    private List<String> certificateChain;
    
    // Constructors
    public PfxImportResult() {}
    
    // Getters and Setters
    public UUID getCertificateId() {
        return certificateId;
    }
    
    public void setCertificateId(UUID certificateId) {
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
    
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public List<String> getCertificateChain() {
        return certificateChain;
    }
    
    public void setCertificateChain(List<String> certificateChain) {
        this.certificateChain = certificateChain;
    }
}