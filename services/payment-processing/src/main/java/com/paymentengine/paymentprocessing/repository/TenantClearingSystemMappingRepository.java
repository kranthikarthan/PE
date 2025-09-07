package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.TenantClearingSystemMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for tenant clearing system mapping entities
 */
@Repository
public interface TenantClearingSystemMappingRepository extends JpaRepository<TenantClearingSystemMappingEntity, String> {
    
    /**
     * Find mapping by tenant ID, payment type, and local instrument code
     */
    Optional<TenantClearingSystemMappingEntity> findByTenantIdAndPaymentTypeAndLocalInstrumentCodeAndIsActiveTrue(
            String tenantId, String paymentType, String localInstrumentCode);
    
    /**
     * Find mapping by tenant ID and payment type (without local instrument)
     */
    Optional<TenantClearingSystemMappingEntity> findByTenantIdAndPaymentTypeAndLocalInstrumentCodeIsNullAndIsActiveTrue(
            String tenantId, String paymentType);
    
    /**
     * Find mapping by tenant ID and local instrument code (without payment type)
     */
    Optional<TenantClearingSystemMappingEntity> findByTenantIdAndPaymentTypeIsNullAndLocalInstrumentCodeAndIsActiveTrue(
            String tenantId, String localInstrumentCode);
    
    /**
     * Find all mappings for a tenant
     */
    List<TenantClearingSystemMappingEntity> findByTenantIdAndIsActiveTrueOrderByPriorityAsc(String tenantId);
    
    /**
     * Find all mappings for a clearing system
     */
    List<TenantClearingSystemMappingEntity> findByClearingSystemCodeAndIsActiveTrue(String clearingSystemCode);
    
    /**
     * Find mappings by tenant ID and payment type
     */
    List<TenantClearingSystemMappingEntity> findByTenantIdAndPaymentTypeAndIsActiveTrueOrderByPriorityAsc(
            String tenantId, String paymentType);
    
    /**
     * Find mappings by tenant ID and local instrument code
     */
    List<TenantClearingSystemMappingEntity> findByTenantIdAndLocalInstrumentCodeAndIsActiveTrueOrderByPriorityAsc(
            String tenantId, String localInstrumentCode);
    
    /**
     * Check if mapping exists for tenant, payment type, and local instrument
     */
    boolean existsByTenantIdAndPaymentTypeAndLocalInstrumentCodeAndIsActiveTrue(
            String tenantId, String paymentType, String localInstrumentCode);
    
    /**
     * Find the best matching mapping for a tenant, payment type, and local instrument
     * Priority: exact match > payment type only > local instrument only
     */
    @Query("SELECT t FROM TenantClearingSystemMappingEntity t WHERE t.tenantId = :tenantId " +
           "AND t.isActive = true " +
           "AND ((t.paymentType = :paymentType AND t.localInstrumentCode = :localInstrumentCode) " +
           "OR (t.paymentType = :paymentType AND t.localInstrumentCode IS NULL) " +
           "OR (t.paymentType IS NULL AND t.localInstrumentCode = :localInstrumentCode)) " +
           "ORDER BY " +
           "CASE WHEN t.paymentType = :paymentType AND t.localInstrumentCode = :localInstrumentCode THEN 1 " +
           "WHEN t.paymentType = :paymentType AND t.localInstrumentCode IS NULL THEN 2 " +
           "WHEN t.paymentType IS NULL AND t.localInstrumentCode = :localInstrumentCode THEN 3 " +
           "ELSE 4 END, " +
           "t.priority ASC")
    List<TenantClearingSystemMappingEntity> findBestMatchingMapping(
            @Param("tenantId") String tenantId,
            @Param("paymentType") String paymentType,
            @Param("localInstrumentCode") String localInstrumentCode);
}