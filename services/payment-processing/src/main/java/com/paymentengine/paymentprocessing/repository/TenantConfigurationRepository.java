package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.TenantConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TenantConfiguration entity
 * Provides methods for tenant configuration versioning and management
 */
@Repository
public interface TenantConfigurationRepository extends JpaRepository<TenantConfiguration, UUID> {

    /**
     * Find all configurations for a specific tenant
     */
    List<TenantConfiguration> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    /**
     * Find active configuration for a tenant
     */
    Optional<TenantConfiguration> findByTenantIdAndIsActiveTrue(String tenantId);

    /**
     * Find configuration by tenant ID and version
     */
    Optional<TenantConfiguration> findByTenantIdAndVersion(String tenantId, String version);

    /**
     * Find all configurations for a specific environment
     */
    List<TenantConfiguration> findByEnvironmentOrderByCreatedAtDesc(TenantConfiguration.Environment environment);

    /**
     * Find all active configurations for a specific environment
     */
    List<TenantConfiguration> findByEnvironmentAndIsActiveTrueOrderByCreatedAtDesc(TenantConfiguration.Environment environment);

    /**
     * Find configurations cloned from a specific tenant
     */
    List<TenantConfiguration> findBySourceTenantIdOrderByCreatedAtDesc(String sourceTenantId);

    /**
     * Find configurations cloned from a specific tenant and version
     */
    List<TenantConfiguration> findBySourceTenantIdAndSourceVersionOrderByCreatedAtDesc(String sourceTenantId, String sourceVersion);

    /**
     * Find all tenant IDs
     */
    @Query("SELECT DISTINCT tc.tenantId FROM TenantConfiguration tc ORDER BY tc.tenantId")
    List<String> findDistinctTenantIds();

    /**
     * Find all versions for a specific tenant
     */
    @Query("SELECT tc.version FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId ORDER BY tc.createdAt DESC")
    List<String> findVersionsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find latest version for a tenant
     */
    @Query("SELECT tc.version FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId ORDER BY tc.createdAt DESC LIMIT 1")
    Optional<String> findLatestVersionByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find configurations by environment and tenant ID
     */
    List<TenantConfiguration> findByEnvironmentAndTenantIdOrderByCreatedAtDesc(
            TenantConfiguration.Environment environment, String tenantId);

    /**
     * Find configurations created by a specific user
     */
    List<TenantConfiguration> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find configurations updated by a specific user
     */
    List<TenantConfiguration> findByUpdatedByOrderByUpdatedAtDesc(String updatedBy);

    /**
     * Find configurations with specific metadata key-value pair
     */
    @Query("SELECT tc FROM TenantConfiguration tc JOIN tc.metadata m WHERE KEY(m) = :key AND VALUE(m) = :value")
    List<TenantConfiguration> findByMetadataKeyAndValue(@Param("key") String key, @Param("value") String value);

    /**
     * Find configurations with specific configuration data key-value pair
     */
    @Query("SELECT tc FROM TenantConfiguration tc JOIN tc.configurationData cd WHERE KEY(cd) = :key AND VALUE(cd) = :value")
    List<TenantConfiguration> findByConfigurationDataKeyAndValue(@Param("key") String key, @Param("value") String value);

    /**
     * Count configurations by tenant ID
     */
    long countByTenantId(String tenantId);

    /**
     * Count configurations by environment
     */
    long countByEnvironment(TenantConfiguration.Environment environment);

    /**
     * Count active configurations by environment
     */
    long countByEnvironmentAndIsActiveTrue(TenantConfiguration.Environment environment);

    /**
     * Find configurations created within a date range
     */
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.createdAt BETWEEN :startDate AND :endDate ORDER BY tc.createdAt DESC")
    List<TenantConfiguration> findByCreatedAtBetween(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Find configurations updated within a date range
     */
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.updatedAt BETWEEN :startDate AND :endDate ORDER BY tc.updatedAt DESC")
    List<TenantConfiguration> findByUpdatedAtBetween(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Find default configuration for an environment
     */
    Optional<TenantConfiguration> findByEnvironmentAndIsDefaultTrue(TenantConfiguration.Environment environment);

    /**
     * Find configurations that can be used as templates (have been cloned)
     */
    @Query("SELECT DISTINCT tc.tenantId FROM TenantConfiguration tc WHERE tc.id IN (SELECT DISTINCT tc2.sourceTenantId FROM TenantConfiguration tc2 WHERE tc2.sourceTenantId IS NOT NULL)")
    List<String> findTemplateTenantIds();

    /**
     * Find all environments that have configurations
     */
    @Query("SELECT DISTINCT tc.environment FROM TenantConfiguration tc ORDER BY tc.environment")
    List<TenantConfiguration.Environment> findDistinctEnvironments();

    /**
     * Find configurations with specific change log content
     */
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.changeLog LIKE %:changeLog% ORDER BY tc.createdAt DESC")
    List<TenantConfiguration> findByChangeLogContaining(@Param("changeLog") String changeLog);

    /**
     * Find configurations by name pattern
     */
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.name LIKE %:name% ORDER BY tc.createdAt DESC")
    List<TenantConfiguration> findByNameContaining(@Param("name") String name);

    /**
     * Find configurations by description pattern
     */
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.description LIKE %:description% ORDER BY tc.createdAt DESC")
    List<TenantConfiguration> findByDescriptionContaining(@Param("description") String description);
}