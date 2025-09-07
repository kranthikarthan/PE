package com.paymentengine.gateway.dto.certificate;

/**
 * DTO for certificate statistics
 */
public class CertificateStats {
    
    private long totalCertificates;
    private long activeCertificates;
    private long expiredCertificates;
    private long expiringSoon;
    
    // Constructors
    public CertificateStats() {}
    
    public CertificateStats(long totalCertificates, long activeCertificates, long expiredCertificates, long expiringSoon) {
        this.totalCertificates = totalCertificates;
        this.activeCertificates = activeCertificates;
        this.expiredCertificates = expiredCertificates;
        this.expiringSoon = expiringSoon;
    }
    
    // Getters and Setters
    public long getTotalCertificates() {
        return totalCertificates;
    }
    
    public void setTotalCertificates(long totalCertificates) {
        this.totalCertificates = totalCertificates;
    }
    
    public long getActiveCertificates() {
        return activeCertificates;
    }
    
    public void setActiveCertificates(long activeCertificates) {
        this.activeCertificates = activeCertificates;
    }
    
    public long getExpiredCertificates() {
        return expiredCertificates;
    }
    
    public void setExpiredCertificates(long expiredCertificates) {
        this.expiredCertificates = expiredCertificates;
    }
    
    public long getExpiringSoon() {
        return expiringSoon;
    }
    
    public void setExpiringSoon(long expiringSoon) {
        this.expiringSoon = expiringSoon;
    }
}