package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.TenantAuthConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TenantAuthConfiguration entity
 */
@Repository
public interface TenantAuthConfigurationRepository extends JpaRepository<TenantAuthConfiguration, UUID> {
    
    /**
     * Find active auth configuration by tenant ID
     */
    @Query("SELECT t FROM TenantAuthConfiguration t WHERE t.tenantId = :tenantId AND t.isActive = true")
    Optional<TenantAuthConfiguration> findActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find auth configuration by tenant ID
     */
    List<TenantAuthConfiguration> findByTenantId(String tenantId);
    
    /**
     * Find auth configurations by authentication method
     */
    List<TenantAuthConfiguration> findByAuthMethod(TenantAuthConfiguration.AuthMethod authMethod);
    
    /**
     * Find active auth configurations by authentication method
     */
    @Query("SELECT t FROM TenantAuthConfiguration t WHERE t.authMethod = :authMethod AND t.isActive = true")
    List<TenantAuthConfiguration> findActiveByAuthMethod(@Param("authMethod") TenantAuthConfiguration.AuthMethod authMethod);
    
    /**
     * Find auth configurations that include client headers
     */
    @Query("SELECT t FROM TenantAuthConfiguration t WHERE t.includeClientHeaders = true AND t.isActive = true")
    List<TenantAuthConfiguration> findWithClientHeaders();
    
    /**
     * Find auth configurations by tenant ID and authentication method
     */
    Optional<TenantAuthConfiguration> findByTenantIdAndAuthMethod(String tenantId, TenantAuthConfiguration.AuthMethod authMethod);
    
    /**
     * Find active auth configurations by tenant ID and authentication method
     */
    @Query("SELECT t FROM TenantAuthConfiguration t WHERE t.tenantId = :tenantId AND t.authMethod = :authMethod AND t.isActive = true")
    Optional<TenantAuthConfiguration> findActiveByTenantIdAndAuthMethod(
        @Param("tenantId") String tenantId, 
        @Param("authMethod") TenantAuthConfiguration.AuthMethod authMethod
    );
    
    /**
     * Check if tenant has active auth configuration
     */
    @Query("SELECT COUNT(t) > 0 FROM TenantAuthConfiguration t WHERE t.tenantId = :tenantId AND t.isActive = true")
    boolean existsActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find all active auth configurations
     */
    @Query("SELECT t FROM TenantAuthConfiguration t WHERE t.isActive = true")
    List<TenantAuthConfiguration> findAllActive();
    
    /**
     * Find auth configurations by client ID
     */
    List<TenantAuthConfiguration> findByClientId(String clientId);
    
    /**
     * Find active auth configurations by client ID
     */
    @Query("SELECT t FROM TenantAuthConfiguration t WHERE t.clientId = :clientId AND t.isActive = true")
    List<TenantAuthConfiguration> findActiveByClientId(@Param("clientId") String clientId);
    
    /**
     * Count auth configurations by tenant ID
     */
    long countByTenantId(String tenantId);
    
    /**
     * Count active auth configurations by tenant ID
     */
    @Query("SELECT COUNT(t) FROM TenantAuthConfiguration t WHERE t.tenantId = :tenantId AND t.isActive = true")
    long countActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count auth configurations by authentication method
     */
    long countByAuthMethod(TenantAuthConfiguration.AuthMethod authMethod);
    
    /**
     * Count active auth configurations by authentication method
     */
    @Query("SELECT COUNT(t) FROM TenantAuthConfiguration t WHERE t.authMethod = :authMethod AND t.isActive = true")
    long countActiveByAuthMethod(@Param("authMethod") TenantAuthConfiguration.AuthMethod authMethod);
}