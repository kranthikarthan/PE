package com.paymentengine.middleware.repository;

import com.paymentengine.middleware.entity.CertificateInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Certificate Info Repository
 * 
 * Repository for certificate information operations including
 * filtering, expiration checking, and status management.
 */
@Repository
public interface CertificateInfoRepository extends JpaRepository<CertificateInfo, UUID> {
    
    /**
     * Find certificates by tenant ID
     */
    List<CertificateInfo> findByTenantId(String tenantId);
    
    /**
     * Find certificates by certificate type
     */
    List<CertificateInfo> findByCertificateType(String certificateType);
    
    /**
     * Find certificates by status
     */
    List<CertificateInfo> findByStatus(CertificateInfo.CertificateStatus status);
    
    /**
     * Find certificates by tenant ID and status
     */
    List<CertificateInfo> findByTenantIdAndStatus(String tenantId, CertificateInfo.CertificateStatus status);
    
    /**
     * Find certificates by subject DN
     */
    Optional<CertificateInfo> findBySubjectDN(String subjectDN);
    
    /**
     * Find certificates by serial number
     */
    Optional<CertificateInfo> findBySerialNumber(String serialNumber);
    
    /**
     * Find certificates by alias
     */
    Optional<CertificateInfo> findByAlias(String alias);
    
    /**
     * Find certificates expiring before specified date
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.validTo <= :expiryDate AND c.status = 'ACTIVE'")
    List<CertificateInfo> findExpiringCertificates(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find certificates expiring within specified days
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.validTo <= :expiryDate AND c.status = 'ACTIVE'")
    List<CertificateInfo> findCertificatesExpiringWithinDays(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find expired certificates
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.validTo < :currentDate AND c.status = 'ACTIVE'")
    List<CertificateInfo> findExpiredCertificates(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find certificates by filter criteria
     */
    @Query("SELECT c FROM CertificateInfo c WHERE " +
           "(:tenantId IS NULL OR c.tenantId = :tenantId) AND " +
           "(:certificateType IS NULL OR c.certificateType = :certificateType) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:validFrom IS NULL OR c.validFrom >= :validFrom) AND " +
           "(:validTo IS NULL OR c.validTo <= :validTo)")
    List<CertificateInfo> findByFilter(
        @Param("tenantId") String tenantId,
        @Param("certificateType") String certificateType,
        @Param("status") CertificateInfo.CertificateStatus status,
        @Param("validFrom") LocalDateTime validFrom,
        @Param("validTo") LocalDateTime validTo
    );
    
    /**
     * Find certificates by filter criteria with pagination
     */
    @Query("SELECT c FROM CertificateInfo c WHERE " +
           "(:tenantId IS NULL OR c.tenantId = :tenantId) AND " +
           "(:certificateType IS NULL OR c.certificateType = :certificateType) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:validFrom IS NULL OR c.validFrom >= :validFrom) AND " +
           "(:validTo IS NULL OR c.validTo <= :validTo)")
    Page<CertificateInfo> findByFilterWithPagination(
        @Param("tenantId") String tenantId,
        @Param("certificateType") String certificateType,
        @Param("status") CertificateInfo.CertificateStatus status,
        @Param("validFrom") LocalDateTime validFrom,
        @Param("validTo") LocalDateTime validTo,
        Pageable pageable
    );
    
    /**
     * Find certificates by tenant ID with pagination
     */
    Page<CertificateInfo> findByTenantId(String tenantId, Pageable pageable);
    
    /**
     * Find certificates by certificate type with pagination
     */
    Page<CertificateInfo> findByCertificateType(String certificateType, Pageable pageable);
    
    /**
     * Find certificates by status with pagination
     */
    Page<CertificateInfo> findByStatus(CertificateInfo.CertificateStatus status, Pageable pageable);
    
    /**
     * Find certificates by tenant ID and certificate type
     */
    List<CertificateInfo> findByTenantIdAndCertificateType(String tenantId, String certificateType);
    
    /**
     * Find certificates by tenant ID and status with pagination
     */
    Page<CertificateInfo> findByTenantIdAndStatus(String tenantId, CertificateInfo.CertificateStatus status, Pageable pageable);
    
    /**
     * Find certificates by validation status
     */
    List<CertificateInfo> findByValidationStatus(CertificateInfo.ValidationStatus validationStatus);
    
    /**
     * Find certificates by tenant ID and validation status
     */
    List<CertificateInfo> findByTenantIdAndValidationStatus(String tenantId, CertificateInfo.ValidationStatus validationStatus);
    
    /**
     * Find certificates that need validation
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.validationStatus IS NULL OR c.validationStatus = 'PENDING'")
    List<CertificateInfo> findCertificatesNeedingValidation();
    
    /**
     * Find certificates by tenant ID that need validation
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.tenantId = :tenantId AND (c.validationStatus IS NULL OR c.validationStatus = 'PENDING')")
    List<CertificateInfo> findCertificatesNeedingValidationByTenant(@Param("tenantId") String tenantId);
    
    /**
     * Find certificates created within date range
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<CertificateInfo> findCertificatesCreatedBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find certificates by tenant ID created within date range
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.tenantId = :tenantId AND c.createdAt BETWEEN :startDate AND :endDate")
    List<CertificateInfo> findCertificatesByTenantCreatedBetween(
        @Param("tenantId") String tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Count certificates by tenant ID
     */
    long countByTenantId(String tenantId);
    
    /**
     * Count certificates by certificate type
     */
    long countByCertificateType(String certificateType);
    
    /**
     * Count certificates by status
     */
    long countByStatus(CertificateInfo.CertificateStatus status);
    
    /**
     * Count certificates by tenant ID and status
     */
    long countByTenantIdAndStatus(String tenantId, CertificateInfo.CertificateStatus status);
    
    /**
     * Count expired certificates
     */
    @Query("SELECT COUNT(c) FROM CertificateInfo c WHERE c.validTo < :currentDate AND c.status = 'ACTIVE'")
    long countExpiredCertificates(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Count certificates expiring within specified days
     */
    @Query("SELECT COUNT(c) FROM CertificateInfo c WHERE c.validTo <= :expiryDate AND c.status = 'ACTIVE'")
    long countCertificatesExpiringWithinDays(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find certificates by rotated certificate ID
     */
    List<CertificateInfo> findByRotatedTo(UUID rotatedTo);
    
    /**
     * Find certificate by rotated certificate ID (single result)
     */
    Optional<CertificateInfo> findFirstByRotatedTo(UUID rotatedTo);
    
    /**
     * Find certificates that have been rotated
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.status = 'ROTATED'")
    List<CertificateInfo> findRotatedCertificates();
    
    /**
     * Find certificates by tenant ID that have been rotated
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.tenantId = :tenantId AND c.status = 'ROTATED'")
    List<CertificateInfo> findRotatedCertificatesByTenant(@Param("tenantId") String tenantId);
    
    /**
     * Find certificates by subject DN pattern
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.subjectDN LIKE %:pattern%")
    List<CertificateInfo> findBySubjectDNPattern(@Param("pattern") String pattern);
    
    /**
     * Find certificates by issuer DN pattern
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.issuerDN LIKE %:pattern%")
    List<CertificateInfo> findByIssuerDNPattern(@Param("pattern") String pattern);
    
    /**
     * Find certificates by public key algorithm
     */
    List<CertificateInfo> findByPublicKeyAlgorithm(String publicKeyAlgorithm);
    
    /**
     * Find certificates by signature algorithm
     */
    List<CertificateInfo> findBySignatureAlgorithm(String signatureAlgorithm);
    
    /**
     * Find certificates by key size
     */
    List<CertificateInfo> findByKeySize(Integer keySize);
    
    /**
     * Find certificates by tenant ID and key size
     */
    List<CertificateInfo> findByTenantIdAndKeySize(String tenantId, Integer keySize);
    
    /**
     * Find certificates by tenant ID and public key algorithm
     */
    List<CertificateInfo> findByTenantIdAndPublicKeyAlgorithm(String tenantId, String publicKeyAlgorithm);
    
    /**
     * Find certificates by tenant ID and signature algorithm
     */
    List<CertificateInfo> findByTenantIdAndSignatureAlgorithm(String tenantId, String signatureAlgorithm);
}