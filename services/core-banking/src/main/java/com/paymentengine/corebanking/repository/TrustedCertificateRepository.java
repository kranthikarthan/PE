package com.paymentengine.corebanking.repository;

import com.paymentengine.corebanking.entity.TrustedCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TrustedCertificate entity in API Gateway service
 */
@Repository
public interface TrustedCertificateRepository extends JpaRepository<TrustedCertificate, UUID> {
    
    /**
     * Find trusted certificates by alias
     */
    Optional<TrustedCertificate> findByAlias(String alias);
    
    /**
     * Find trusted certificates by tenant ID
     */
    List<TrustedCertificate> findByTenantId(String tenantId);
    
    /**
     * Find trusted certificates by status
     */
    List<TrustedCertificate> findByStatus(String status);
    
    /**
     * Find trusted certificates by certificate type
     */
    List<TrustedCertificate> findByCertificateType(String certificateType);
    
    /**
     * Find trusted certificates by subject DN
     */
    List<TrustedCertificate> findBySubjectDN(String subjectDN);
    
    /**
     * Find trusted certificates by serial number
     */
    Optional<TrustedCertificate> findBySerialNumber(String serialNumber);
    
    /**
     * Find trusted certificates expiring soon
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.validTo <= :expiryDate AND t.status = 'ACTIVE'")
    List<TrustedCertificate> findExpiringCertificates(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find active trusted certificates
     */
    @Query("SELECT t FROM TrustedCertificate t WHERE t.status = 'ACTIVE'")
    List<TrustedCertificate> findActiveCertificates();
}