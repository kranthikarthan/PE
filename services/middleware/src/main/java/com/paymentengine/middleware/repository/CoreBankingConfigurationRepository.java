package com.paymentengine.middleware.repository;

import com.paymentengine.middleware.entity.CoreBankingConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Core Banking Configuration entities
 */
@Repository
public interface CoreBankingConfigurationRepository extends JpaRepository<CoreBankingConfiguration, UUID> {
    
    /**
     * Find core banking configuration by tenant ID
     */
    Optional<CoreBankingConfiguration> findByTenantId(String tenantId);
    
    /**
     * Find active core banking configuration by tenant ID
     */
    @Query("SELECT c FROM CoreBankingConfiguration c WHERE c.tenantId = :tenantId AND c.isActive = true")
    Optional<CoreBankingConfiguration> findActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find core banking configurations by adapter type
     */
    List<CoreBankingConfiguration> findByAdapterType(CoreBankingConfiguration.AdapterType adapterType);
    
    /**
     * Find active core banking configurations by adapter type
     */
    @Query("SELECT c FROM CoreBankingConfiguration c WHERE c.adapterType = :adapterType AND c.isActive = true")
    List<CoreBankingConfiguration> findActiveByAdapterType(@Param("adapterType") CoreBankingConfiguration.AdapterType adapterType);
    
    /**
     * Find core banking configurations by bank code
     */
    List<CoreBankingConfiguration> findByBankCode(String bankCode);
    
    /**
     * Find active core banking configurations by bank code
     */
    @Query("SELECT c FROM CoreBankingConfiguration c WHERE c.bankCode = :bankCode AND c.isActive = true")
    List<CoreBankingConfiguration> findActiveByBankCode(@Param("bankCode") String bankCode);
    
    /**
     * Find core banking configurations by processing mode
     */
    List<CoreBankingConfiguration> findByProcessingMode(String processingMode);
    
    /**
     * Find core banking configurations by message format
     */
    List<CoreBankingConfiguration> findByMessageFormat(String messageFormat);
    
    /**
     * Find all active core banking configurations
     */
    @Query("SELECT c FROM CoreBankingConfiguration c WHERE c.isActive = true ORDER BY c.priority ASC, c.createdAt ASC")
    List<CoreBankingConfiguration> findAllActive();
    
    /**
     * Check if core banking configuration exists for tenant
     */
    boolean existsByTenantId(String tenantId);
    
    /**
     * Check if active core banking configuration exists for tenant
     */
    @Query("SELECT COUNT(c) > 0 FROM CoreBankingConfiguration c WHERE c.tenantId = :tenantId AND c.isActive = true")
    boolean existsActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find core banking configurations by tenant ID and adapter type
     */
    Optional<CoreBankingConfiguration> findByTenantIdAndAdapterType(String tenantId, CoreBankingConfiguration.AdapterType adapterType);
    
    /**
     * Find active core banking configurations by tenant ID and adapter type
     */
    @Query("SELECT c FROM CoreBankingConfiguration c WHERE c.tenantId = :tenantId AND c.adapterType = :adapterType AND c.isActive = true")
    Optional<CoreBankingConfiguration> findActiveByTenantIdAndAdapterType(
        @Param("tenantId") String tenantId, 
        @Param("adapterType") CoreBankingConfiguration.AdapterType adapterType
    );
    
    /**
     * Find core banking configurations by priority range
     */
    @Query("SELECT c FROM CoreBankingConfiguration c WHERE c.priority BETWEEN :minPriority AND :maxPriority ORDER BY c.priority ASC")
    List<CoreBankingConfiguration> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find core banking configurations by authentication method
     */
    List<CoreBankingConfiguration> findByAuthenticationMethod(String authenticationMethod);
    
    /**
     * Find core banking configurations created by specific user
     */
    List<CoreBankingConfiguration> findByCreatedBy(String createdBy);
    
    /**
     * Find core banking configurations updated by specific user
     */
    List<CoreBankingConfiguration> findByUpdatedBy(String updatedBy);
    
    /**
     * Count core banking configurations by tenant ID
     */
    long countByTenantId(String tenantId);
    
    /**
     * Count active core banking configurations by tenant ID
     */
    @Query("SELECT COUNT(c) FROM CoreBankingConfiguration c WHERE c.tenantId = :tenantId AND c.isActive = true")
    long countActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count core banking configurations by adapter type
     */
    long countByAdapterType(CoreBankingConfiguration.AdapterType adapterType);
    
    /**
     * Count active core banking configurations by adapter type
     */
    @Query("SELECT COUNT(c) FROM CoreBankingConfiguration c WHERE c.adapterType = :adapterType AND c.isActive = true")
    long countActiveByAdapterType(@Param("adapterType") CoreBankingConfiguration.AdapterType adapterType);
}