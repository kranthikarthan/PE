package com.paymentengine.middleware.repository;

import com.paymentengine.middleware.entity.FraudApiToggleConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FraudApiToggleConfiguration entity
 */
@Repository
public interface FraudApiToggleConfigurationRepository extends JpaRepository<FraudApiToggleConfiguration, String> {

    /**
     * Find the most specific configuration for a given context
     * Priority: clearing_system > local_instrument > payment_type > tenant
     */
    @Query("""
        SELECT f FROM FraudApiToggleConfiguration f 
        WHERE f.tenantId = :tenantId 
        AND f.isActive = true 
        AND (f.effectiveFrom IS NULL OR f.effectiveFrom <= :now)
        AND (f.effectiveUntil IS NULL OR f.effectiveUntil >= :now)
        AND (
            (f.clearingSystemCode = :clearingSystemCode AND f.localInstrumentationCode = :localInstrumentationCode AND f.paymentType = :paymentType) OR
            (f.clearingSystemCode IS NULL AND f.localInstrumentationCode = :localInstrumentationCode AND f.paymentType = :paymentType) OR
            (f.clearingSystemCode IS NULL AND f.localInstrumentationCode IS NULL AND f.paymentType = :paymentType) OR
            (f.clearingSystemCode IS NULL AND f.localInstrumentationCode IS NULL AND f.paymentType IS NULL)
        )
        ORDER BY 
            CASE WHEN f.clearingSystemCode IS NOT NULL THEN 1 ELSE 2 END,
            CASE WHEN f.localInstrumentationCode IS NOT NULL THEN 1 ELSE 2 END,
            CASE WHEN f.paymentType IS NOT NULL THEN 1 ELSE 2 END,
            f.priority ASC
        """)
    Optional<FraudApiToggleConfiguration> findMostSpecificConfiguration(
            @Param("tenantId") String tenantId,
            @Param("paymentType") String paymentType,
            @Param("localInstrumentationCode") String localInstrumentationCode,
            @Param("clearingSystemCode") String clearingSystemCode,
            @Param("now") LocalDateTime now
    );

    /**
     * Find all configurations for a tenant
     */
    List<FraudApiToggleConfiguration> findByTenantIdAndIsActiveTrueOrderByPriorityAsc(String tenantId);

    /**
     * Find configurations by tenant and payment type
     */
    List<FraudApiToggleConfiguration> findByTenantIdAndPaymentTypeAndIsActiveTrueOrderByPriorityAsc(
            String tenantId, String paymentType);

    /**
     * Find configurations by tenant, payment type, and local instrumentation code
     */
    List<FraudApiToggleConfiguration> findByTenantIdAndPaymentTypeAndLocalInstrumentationCodeAndIsActiveTrueOrderByPriorityAsc(
            String tenantId, String paymentType, String localInstrumentationCode);

    /**
     * Find configurations by tenant, payment type, local instrumentation code, and clearing system
     */
    List<FraudApiToggleConfiguration> findByTenantIdAndPaymentTypeAndLocalInstrumentationCodeAndClearingSystemCodeAndIsActiveTrueOrderByPriorityAsc(
            String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode);

    /**
     * Find all active configurations
     */
    @Query("SELECT f FROM FraudApiToggleConfiguration f WHERE f.isActive = true ORDER BY f.tenantId, f.priority ASC")
    List<FraudApiToggleConfiguration> findAllActiveConfigurations();

    /**
     * Find configurations that are currently effective
     */
    @Query("""
        SELECT f FROM FraudApiToggleConfiguration f 
        WHERE f.isActive = true 
        AND (f.effectiveFrom IS NULL OR f.effectiveFrom <= :now)
        AND (f.effectiveUntil IS NULL OR f.effectiveUntil >= :now)
        ORDER BY f.tenantId, f.priority ASC
        """)
    List<FraudApiToggleConfiguration> findCurrentlyEffectiveConfigurations(@Param("now") LocalDateTime now);

    /**
     * Find configurations that will become effective in the future
     */
    @Query("""
        SELECT f FROM FraudApiToggleConfiguration f 
        WHERE f.isActive = true 
        AND f.effectiveFrom > :now
        ORDER BY f.effectiveFrom ASC
        """)
    List<FraudApiToggleConfiguration> findFutureEffectiveConfigurations(@Param("now") LocalDateTime now);

    /**
     * Find configurations that have expired
     */
    @Query("""
        SELECT f FROM FraudApiToggleConfiguration f 
        WHERE f.isActive = true 
        AND f.effectiveUntil < :now
        ORDER BY f.effectiveUntil DESC
        """)
    List<FraudApiToggleConfiguration> findExpiredConfigurations(@Param("now") LocalDateTime now);

    /**
     * Check if fraud API is enabled for a specific context
     */
    @Query("""
        SELECT COALESCE(f.isEnabled, true) FROM FraudApiToggleConfiguration f 
        WHERE f.tenantId = :tenantId 
        AND f.isActive = true 
        AND (f.effectiveFrom IS NULL OR f.effectiveFrom <= :now)
        AND (f.effectiveUntil IS NULL OR f.effectiveUntil >= :now)
        AND (
            (f.clearingSystemCode = :clearingSystemCode AND f.localInstrumentationCode = :localInstrumentationCode AND f.paymentType = :paymentType) OR
            (f.clearingSystemCode IS NULL AND f.localInstrumentationCode = :localInstrumentationCode AND f.paymentType = :paymentType) OR
            (f.clearingSystemCode IS NULL AND f.localInstrumentationCode IS NULL AND f.paymentType = :paymentType) OR
            (f.clearingSystemCode IS NULL AND f.localInstrumentationCode IS NULL AND f.paymentType IS NULL)
        )
        ORDER BY 
            CASE WHEN f.clearingSystemCode IS NOT NULL THEN 1 ELSE 2 END,
            CASE WHEN f.localInstrumentationCode IS NOT NULL THEN 1 ELSE 2 END,
            CASE WHEN f.paymentType IS NOT NULL THEN 1 ELSE 2 END,
            f.priority ASC
        """)
    Optional<Boolean> isFraudApiEnabled(
            @Param("tenantId") String tenantId,
            @Param("paymentType") String paymentType,
            @Param("localInstrumentationCode") String localInstrumentationCode,
            @Param("clearingSystemCode") String clearingSystemCode,
            @Param("now") LocalDateTime now
    );

    /**
     * Count configurations by tenant
     */
    long countByTenantIdAndIsActiveTrue(String tenantId);

    /**
     * Count configurations by tenant and payment type
     */
    long countByTenantIdAndPaymentTypeAndIsActiveTrue(String tenantId, String paymentType);

    /**
     * Find configurations by configuration level
     */
    @Query("""
        SELECT f FROM FraudApiToggleConfiguration f 
        WHERE f.isActive = true 
        AND (
            (:level = 'TENANT' AND f.paymentType IS NULL AND f.localInstrumentationCode IS NULL AND f.clearingSystemCode IS NULL) OR
            (:level = 'PAYMENT_TYPE' AND f.paymentType IS NOT NULL AND f.localInstrumentationCode IS NULL AND f.clearingSystemCode IS NULL) OR
            (:level = 'LOCAL_INSTRUMENT' AND f.localInstrumentationCode IS NOT NULL AND f.clearingSystemCode IS NULL) OR
            (:level = 'CLEARING_SYSTEM' AND f.clearingSystemCode IS NOT NULL)
        )
        ORDER BY f.tenantId, f.priority ASC
        """)
    List<FraudApiToggleConfiguration> findByConfigurationLevel(@Param("level") String level);
}