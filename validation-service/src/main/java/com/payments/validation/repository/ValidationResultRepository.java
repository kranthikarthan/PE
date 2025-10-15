package com.payments.validation.repository;

import com.payments.validation.entity.ValidationResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ValidationResultEntity
 * 
 * Provides data access methods for:
 * - Validation result queries
 * - Tenant and business unit filtering
 * - Time-based queries
 * - Performance analytics
 */
@Repository
public interface ValidationResultRepository extends JpaRepository<ValidationResultEntity, UUID> {

    /**
     * Find validation result by validation ID
     */
    Optional<ValidationResultEntity> findByValidationId(String validationId);

    /**
     * Find validation results by payment ID
     */
    List<ValidationResultEntity> findByPaymentIdOrderByValidatedAtDesc(String paymentId);

    /**
     * Find validation results by tenant ID
     */
    Page<ValidationResultEntity> findByTenantIdOrderByValidatedAtDesc(String tenantId, Pageable pageable);

    /**
     * Find validation results by tenant and business unit
     */
    Page<ValidationResultEntity> findByTenantIdAndBusinessUnitIdOrderByValidatedAtDesc(
            String tenantId, String businessUnitId, Pageable pageable);

    /**
     * Find validation results by correlation ID
     */
    List<ValidationResultEntity> findByCorrelationIdOrderByValidatedAtDesc(String correlationId);

    /**
     * Find validation results by status
     */
    Page<ValidationResultEntity> findByStatusOrderByValidatedAtDesc(
            ValidationResultEntity.ValidationStatus status, Pageable pageable);

    /**
     * Find validation results by risk level
     */
    Page<ValidationResultEntity> findByRiskLevelOrderByValidatedAtDesc(
            ValidationResultEntity.RiskLevel riskLevel, Pageable pageable);

    /**
     * Find validation results within date range
     */
    @Query("SELECT v FROM ValidationResultEntity v WHERE v.validatedAt BETWEEN :startDate AND :endDate ORDER BY v.validatedAt DESC")
    Page<ValidationResultEntity> findByValidatedAtBetween(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);

    /**
     * Find validation results by tenant within date range
     */
    @Query("SELECT v FROM ValidationResultEntity v WHERE v.tenantId = :tenantId AND v.validatedAt BETWEEN :startDate AND :endDate ORDER BY v.validatedAt DESC")
    Page<ValidationResultEntity> findByTenantIdAndValidatedAtBetween(
            @Param("tenantId") String tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);

    /**
     * Count validation results by status for tenant
     */
    @Query("SELECT COUNT(v) FROM ValidationResultEntity v WHERE v.tenantId = :tenantId AND v.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") ValidationResultEntity.ValidationStatus status);

    /**
     * Count validation results by risk level for tenant
     */
    @Query("SELECT COUNT(v) FROM ValidationResultEntity v WHERE v.tenantId = :tenantId AND v.riskLevel = :riskLevel")
    long countByTenantIdAndRiskLevel(@Param("tenantId") String tenantId, @Param("riskLevel") ValidationResultEntity.RiskLevel riskLevel);

    /**
     * Find validation results with fraud score above threshold
     */
    @Query("SELECT v FROM ValidationResultEntity v WHERE v.fraudScore >= :threshold ORDER BY v.fraudScore DESC")
    Page<ValidationResultEntity> findByFraudScoreGreaterThanEqual(
            @Param("threshold") Integer threshold, Pageable pageable);

    /**
     * Find validation results with risk score above threshold
     */
    @Query("SELECT v FROM ValidationResultEntity v WHERE v.riskScore >= :threshold ORDER BY v.riskScore DESC")
    Page<ValidationResultEntity> findByRiskScoreGreaterThanEqual(
            @Param("threshold") Integer threshold, Pageable pageable);

    /**
     * Get validation statistics for tenant
     */
    @Query("SELECT " +
           "COUNT(v) as totalValidations, " +
           "SUM(CASE WHEN v.status = 'PASSED' THEN 1 ELSE 0 END) as passedValidations, " +
           "SUM(CASE WHEN v.status = 'FAILED' THEN 1 ELSE 0 END) as failedValidations, " +
           "AVG(v.fraudScore) as avgFraudScore, " +
           "AVG(v.riskScore) as avgRiskScore " +
           "FROM ValidationResultEntity v WHERE v.tenantId = :tenantId")
    Object[] getValidationStatistics(@Param("tenantId") String tenantId);

    /**
     * Get validation statistics for tenant within date range
     */
    @Query("SELECT " +
           "COUNT(v) as totalValidations, " +
           "SUM(CASE WHEN v.status = 'PASSED' THEN 1 ELSE 0 END) as passedValidations, " +
           "SUM(CASE WHEN v.status = 'FAILED' THEN 1 ELSE 0 END) as failedValidations, " +
           "AVG(v.fraudScore) as avgFraudScore, " +
           "AVG(v.riskScore) as avgRiskScore " +
           "FROM ValidationResultEntity v WHERE v.tenantId = :tenantId AND v.validatedAt BETWEEN :startDate AND :endDate")
    Object[] getValidationStatisticsForDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Delete old validation results (for cleanup)
     */
    @Query("DELETE FROM ValidationResultEntity v WHERE v.validatedAt < :cutoffDate")
    int deleteByValidatedAtBefore(@Param("cutoffDate") Instant cutoffDate);
}
