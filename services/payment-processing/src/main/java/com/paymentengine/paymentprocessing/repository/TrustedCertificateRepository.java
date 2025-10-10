package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.TrustedCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Trusted Certificate Repository
 * 
 * Repository for trusted certificate operations including
 * CA certificate management and trust store operations.
 */
@Repository
public interface TrustedCertificateRepository extends JpaRepository<TrustedCertificate, UUID> {
    
    /**
     * Find trusted certificate by alias
     */
    Optional<TrustedCertificate> findByAlias(String alias);
    
    /**
     * Find trusted certificates by tenant ID
     */
    List<TrustedCertificate> findByTenantId(String tenantId);
    
    /**
     * Find trusted certificates by status
     */
    List<TrustedCertificate> findByStatus(TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by tenant ID and status
     */
    List<TrustedCertificate> findByTenantIdAndStatus(String tenantId, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by certificate type
     */
    List<TrustedCertificate> findByCertificateType(String certificateType);
    
    /**
     * Find trusted certificates by tenant ID and certificate type
     */
    List<TrustedCertificate> findByTenantIdAndCertificateType(String tenantId, String certificateType);
    
    /**
     * Find trusted certificates by subject DN
     */
    Optional<TrustedCertificate> findBySubjectDN(String subjectDN);
    
    /**
     * Find trusted certificates by issuer DN
     */
    List<TrustedCertificate> findByIssuerDN(String issuerDN);
    
    /**
     * Find trusted certificates by serial number
     */
    Optional<TrustedCertificate> findBySerialNumber(String serialNumber);
    
    /**
     * Find trusted certificates by source
     */
    List<TrustedCertificate> findBySource(String source);
    
    /**
     * Find trusted certificates by tenant ID and source
     */
    List<TrustedCertificate> findByTenantIdAndSource(String tenantId, String source);
    
    /**
     * Find trusted certificates expiring before specified date
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.validTo <= :expiryDate AND t.status = 'ACTIVE'")
    List<TrustedCertificate> findExpiringTrustedCertificates(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find trusted certificates expiring within specified days
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.validTo <= :expiryDate AND t.status = 'ACTIVE'")
    List<TrustedCertificate> findTrustedCertificatesExpiringWithinDays(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find expired trusted certificates
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.validTo < :currentDate AND t.status = 'ACTIVE'")
    List<TrustedCertificate> findExpiredTrustedCertificates(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find active trusted certificates
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.status = 'ACTIVE' AND t.validTo > :currentDate")
    List<TrustedCertificate> findActiveTrustedCertificates(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find active trusted certificates by tenant ID
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.tenantId = :tenantId AND t.status = 'ACTIVE' AND t.validTo > :currentDate")
    List<TrustedCertificate> findActiveTrustedCertificatesByTenant(@Param("tenantId") String tenantId, @Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find trusted certificates by subject DN pattern
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.subjectDN LIKE %:pattern%")
    List<TrustedCertificate> findBySubjectDNPattern(@Param("pattern") String pattern);
    
    /**
     * Find trusted certificates by issuer DN pattern
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.issuerDN LIKE %:pattern%")
    List<TrustedCertificate> findByIssuerDNPattern(@Param("pattern") String pattern);
    
    /**
     * Find trusted certificates by public key algorithm
     */
    List<TrustedCertificate> findByPublicKeyAlgorithm(String publicKeyAlgorithm);
    
    /**
     * Find trusted certificates by signature algorithm
     */
    List<TrustedCertificate> findBySignatureAlgorithm(String signatureAlgorithm);
    
    /**
     * Find trusted certificates by key size
     */
    List<TrustedCertificate> findByKeySize(Integer keySize);
    
    /**
     * Find trusted certificates by tenant ID and key size
     */
    List<TrustedCertificate> findByTenantIdAndKeySize(String tenantId, Integer keySize);
    
    /**
     * Find trusted certificates by tenant ID and public key algorithm
     */
    List<TrustedCertificate> findByTenantIdAndPublicKeyAlgorithm(String tenantId, String publicKeyAlgorithm);
    
    /**
     * Find trusted certificates by tenant ID and signature algorithm
     */
    List<TrustedCertificate> findByTenantIdAndSignatureAlgorithm(String tenantId, String signatureAlgorithm);
    
    /**
     * Find trusted certificates created within date range
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<TrustedCertificate> findTrustedCertificatesCreatedBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find trusted certificates by tenant ID created within date range
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.tenantId = :tenantId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<TrustedCertificate> findTrustedCertificatesByTenantCreatedBetween(
        @Param("tenantId") String tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Count trusted certificates by tenant ID
     */
    long countByTenantId(String tenantId);
    
    /**
     * Count trusted certificates by certificate type
     */
    long countByCertificateType(String certificateType);
    
    /**
     * Count trusted certificates by status
     */
    long countByStatus(TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Count trusted certificates by tenant ID and status
     */
    long countByTenantIdAndStatus(String tenantId, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Count expired trusted certificates
     */
    @Query("SELECT COUNT(t) FROM TrustedCertificate t WHERE t.validTo < :currentDate AND t.status = 'ACTIVE'")
    long countExpiredTrustedCertificates(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Count trusted certificates expiring within specified days
     */
    @Query("SELECT COUNT(t) FROM TrustedCertificate t WHERE t.validTo <= :expiryDate AND t.status = 'ACTIVE'")
    long countTrustedCertificatesExpiringWithinDays(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find trusted certificates by description pattern
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.description LIKE %:pattern%")
    List<TrustedCertificate> findByDescriptionPattern(@Param("pattern") String pattern);
    
    /**
     * Find trusted certificates by tenant ID and description pattern
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.tenantId = :tenantId AND t.description LIKE %:pattern%")
    List<TrustedCertificate> findByTenantIdAndDescriptionPattern(@Param("tenantId") String tenantId, @Param("pattern") String pattern);
    
    /**
     * Find trusted certificates by source and status
     */
    List<TrustedCertificate> findBySourceAndStatus(String source, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by tenant ID, source and status
     */
    List<TrustedCertificate> findByTenantIdAndSourceAndStatus(String tenantId, String source, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by certificate type and status
     */
    List<TrustedCertificate> findByCertificateTypeAndStatus(String certificateType, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by tenant ID, certificate type and status
     */
    List<TrustedCertificate> findByTenantIdAndCertificateTypeAndStatus(String tenantId, String certificateType, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by public key algorithm and status
     */
    List<TrustedCertificate> findByPublicKeyAlgorithmAndStatus(String publicKeyAlgorithm, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by signature algorithm and status
     */
    List<TrustedCertificate> findBySignatureAlgorithmAndStatus(String signatureAlgorithm, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by key size and status
     */
    List<TrustedCertificate> findByKeySizeAndStatus(Integer keySize, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by tenant ID and key size and status
     */
    List<TrustedCertificate> findByTenantIdAndKeySizeAndStatus(String tenantId, Integer keySize, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by tenant ID and public key algorithm and status
     */
    List<TrustedCertificate> findByTenantIdAndPublicKeyAlgorithmAndStatus(String tenantId, String publicKeyAlgorithm, TrustedCertificate.TrustedCertificateStatus status);
    
    /**
     * Find trusted certificates by tenant ID and signature algorithm and status
     */
    List<TrustedCertificate> findByTenantIdAndSignatureAlgorithmAndStatus(String tenantId, String signatureAlgorithm, TrustedCertificate.TrustedCertificateStatus status);
}