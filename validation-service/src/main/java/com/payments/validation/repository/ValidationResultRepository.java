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

/**
 * Validation Result Repository
 * 
 * JPA repository for validation results:
 * - CRUD operations
 * - Custom queries for analytics
 * - Multi-tenant data access
 * - Performance optimized queries
 */
@Repository
public interface ValidationResultRepository extends JpaRepository<ValidationResultEntity, Long> {

    /**
     * Find by validation ID
     */
    Optional<ValidationResultEntity> findByValidationId(String validationId);

    /**
     * Find by payment ID ordered by validation date
     */
    List<ValidationResultEntity> findByPaymentIdOrderByValidatedAtDesc(String paymentId);

    /**
     * Find by tenant ID ordered by validation date
     */
    Page<ValidationResultEntity> findByTenantIdOrderByValidatedAtDesc(String tenantId, Pageable pageable);

    /**
     * Find by tenant ID and business unit ID ordered by validation date
     */
    Page<ValidationResultEntity> findByTenantIdAndBusinessUnitIdOrderByValidatedAtDesc(
            String tenantId, String businessUnitId, Pageable pageable);

    /**
     * Find by correlation ID ordered by validation date
     */
    List<ValidationResultEntity> findByCorrelationIdOrderByValidatedAtDesc(String correlationId);

    /**
     * Find by status ordered by validation date
     */
    Page<ValidationResultEntity> findByStatusOrderByValidatedAtDesc(
            ValidationResultEntity.ValidationStatus status, Pageable pageable);

    /**
     * Find by risk level ordered by validation date
     */
    Page<ValidationResultEntity> findByRiskLevelOrderByValidatedAtDesc(
            ValidationResultEntity.RiskLevel riskLevel, Pageable pageable);

    /**
     * Find by validation date range
     */
    Page<ValidationResultEntity> findByValidatedAtBetween(
            Instant startDate, Instant endDate, Pageable pageable);

    /**
     * Find by tenant ID and validation date range
     */
    Page<ValidationResultEntity> findByTenantIdAndValidatedAtBetween(
            String tenantId, Instant startDate, Instant endDate, Pageable pageable);

    /**
     * Find by tenant ID and status
     */
    Page<ValidationResultEntity> findByTenantIdAndStatusOrderByValidatedAtDesc(
            String tenantId, ValidationResultEntity.ValidationStatus status, Pageable pageable);

    /**
     * Find by tenant ID and risk level
     */
    Page<ValidationResultEntity> findByTenantIdAndRiskLevelOrderByValidatedAtDesc(
            String tenantId, ValidationResultEntity.RiskLevel riskLevel, Pageable pageable);

    /**
     * Find by tenant ID, business unit ID and status
     */
    Page<ValidationResultEntity> findByTenantIdAndBusinessUnitIdAndStatusOrderByValidatedAtDesc(
            String tenantId, String businessUnitId, ValidationResultEntity.ValidationStatus status, Pageable pageable);

    /**
     * Find by tenant ID, business unit ID and risk level
     */
    Page<ValidationResultEntity> findByTenantIdAndBusinessUnitIdAndRiskLevelOrderByValidatedAtDesc(
            String tenantId, String businessUnitId, ValidationResultEntity.RiskLevel riskLevel, Pageable pageable);

    /**
     * Delete by validation date before
     */
    int deleteByValidatedAtBefore(Instant cutoffDate);

    /**
     * Delete by tenant ID and validation date before
     */
    int deleteByTenantIdAndValidatedAtBefore(String tenantId, Instant cutoffDate);

    /**
     * Count by tenant ID
     */
    long countByTenantId(String tenantId);

    /**
     * Count by tenant ID and status
     */
    long countByTenantIdAndStatus(String tenantId, ValidationResultEntity.ValidationStatus status);

    /**
     * Count by tenant ID and risk level
     */
    long countByTenantIdAndRiskLevel(String tenantId, ValidationResultEntity.RiskLevel riskLevel);

    /**
     * Count by tenant ID and business unit ID
     */
    long countByTenantIdAndBusinessUnitId(String tenantId, String businessUnitId);

    /**
     * Count by tenant ID, business unit ID and status
     */
    long countByTenantIdAndBusinessUnitIdAndStatus(
            String tenantId, String businessUnitId, ValidationResultEntity.ValidationStatus status);

    /**
     * Count by tenant ID, business unit ID and risk level
     */
    long countByTenantIdAndBusinessUnitIdAndRiskLevel(
            String tenantId, String businessUnitId, ValidationResultEntity.RiskLevel riskLevel);

    /**
     * Get validation statistics for tenant
     */
    @Query("SELECT " +
           "COUNT(v), " +
           "COUNT(CASE WHEN v.status = 'PASSED' THEN 1 END), " +
           "COUNT(CASE WHEN v.status = 'FAILED' THEN 1 END), " +
           "AVG(v.fraudScore), " +
           "AVG(v.riskScore) " +
           "FROM ValidationResultEntity v " +
           "WHERE v.tenantId = :tenantId")
    Object[] getValidationStatistics(@Param("tenantId") String tenantId);

    /**
     * Get validation statistics for tenant and business unit
     */
    @Query("SELECT " +
           "COUNT(v), " +
           "COUNT(CASE WHEN v.status = 'PASSED' THEN 1 END), " +
           "COUNT(CASE WHEN v.status = 'FAILED' THEN 1 END), " +
           "AVG(v.fraudScore), " +
           "AVG(v.riskScore) " +
           "FROM ValidationResultEntity v " +
           "WHERE v.tenantId = :tenantId AND v.businessUnitId = :businessUnitId")
    Object[] getValidationStatisticsByTenantAndBusinessUnit(
            @Param("tenantId") String tenantId, 
            @Param("businessUnitId") String businessUnitId);

    /**
     * Get validation statistics for date range
     */
    @Query("SELECT " +
           "COUNT(v), " +
           "COUNT(CASE WHEN v.status = 'PASSED' THEN 1 END), " +
           "COUNT(CASE WHEN v.status = 'FAILED' THEN 1 END), " +
           "AVG(v.fraudScore), " +
           "AVG(v.riskScore) " +
           "FROM ValidationResultEntity v " +
           "WHERE v.validatedAt BETWEEN :startDate AND :endDate")
    Object[] getValidationStatisticsByDateRange(
            @Param("startDate") Instant startDate, 
            @Param("endDate") Instant endDate);

    /**
     * Get validation statistics for tenant and date range
     */
    @Query("SELECT " +
           "COUNT(v), " +
           "COUNT(CASE WHEN v.status = 'PASSED' THEN 1 END), " +
           "COUNT(CASE WHEN v.status = 'FAILED' THEN 1 END), " +
           "AVG(v.fraudScore), " +
           "AVG(v.riskScore) " +
           "FROM ValidationResultEntity v " +
           "WHERE v.tenantId = :tenantId AND v.validatedAt BETWEEN :startDate AND :endDate")
    Object[] getValidationStatisticsByTenantAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") Instant startDate, 
            @Param("endDate") Instant endDate);
}