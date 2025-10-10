package com.paymentengine.gateway.repository;

import com.paymentengine.gateway.entity.CertificateInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CertificateInfo entity in API Gateway service
 */
@Repository
public interface CertificateInfoRepository extends JpaRepository<CertificateInfo, UUID> {
    
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
        @Param("status") String status,
        @Param("validFrom") LocalDateTime validFrom,
        @Param("validTo") LocalDateTime validTo
    );
    
    /**
     * Find certificates expiring soon
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.validTo <= :expiryDate AND c.status = 'ACTIVE'")
    List<CertificateInfo> findExpiringCertificates(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find certificate by rotated to ID
     */
    @Query("SELECT c FROM CertificateInfo c WHERE c.rotatedTo = :rotatedTo")
    Optional<CertificateInfo> findByRotatedTo(@Param("rotatedTo") UUID rotatedTo);
    
    /**
     * Find certificates by tenant ID
     */
    List<CertificateInfo> findByTenantId(String tenantId);
    
    /**
     * Find certificates by status
     */
    List<CertificateInfo> findByStatus(String status);
    
    /**
     * Find certificates by certificate type
     */
    List<CertificateInfo> findByCertificateType(String certificateType);
    
    /**
     * Find certificates by subject DN
     */
    List<CertificateInfo> findBySubjectDN(String subjectDN);
    
    /**
     * Find certificates by serial number
     */
    Optional<CertificateInfo> findBySerialNumber(String serialNumber);
}