package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.CoreBankingEndpointConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Core Banking Endpoint Configuration entities
 */
@Repository
public interface CoreBankingEndpointConfigurationRepository extends JpaRepository<CoreBankingEndpointConfiguration, UUID> {
    
    /**
     * Find endpoint configurations by core banking configuration ID
     */
    List<CoreBankingEndpointConfiguration> findByCoreBankingConfigId(UUID coreBankingConfigId);
    
    /**
     * Find active endpoint configurations by core banking configuration ID
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE e.coreBankingConfigId = :coreBankingConfigId AND e.isActive = true ORDER BY e.priority ASC")
    List<CoreBankingEndpointConfiguration> findActiveByCoreBankingConfigId(@Param("coreBankingConfigId") UUID coreBankingConfigId);
    
    /**
     * Find endpoint configuration by core banking configuration ID and endpoint type
     */
    Optional<CoreBankingEndpointConfiguration> findByCoreBankingConfigIdAndEndpointType(
        UUID coreBankingConfigId, 
        CoreBankingEndpointConfiguration.EndpointType endpointType
    );
    
    /**
     * Find active endpoint configuration by core banking configuration ID and endpoint type
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE e.coreBankingConfigId = :coreBankingConfigId AND e.endpointType = :endpointType AND e.isActive = true")
    Optional<CoreBankingEndpointConfiguration> findActiveByCoreBankingConfigIdAndEndpointType(
        @Param("coreBankingConfigId") UUID coreBankingConfigId,
        @Param("endpointType") CoreBankingEndpointConfiguration.EndpointType endpointType
    );
    
    /**
     * Find endpoint configurations by endpoint name
     */
    List<CoreBankingEndpointConfiguration> findByEndpointName(String endpointName);
    
    /**
     * Find active endpoint configurations by endpoint name
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE e.endpointName = :endpointName AND e.isActive = true")
    List<CoreBankingEndpointConfiguration> findActiveByEndpointName(@Param("endpointName") String endpointName);
    
    /**
     * Find endpoint configurations by endpoint type
     */
    List<CoreBankingEndpointConfiguration> findByEndpointType(CoreBankingEndpointConfiguration.EndpointType endpointType);
    
    /**
     * Find active endpoint configurations by endpoint type
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE e.endpointType = :endpointType AND e.isActive = true ORDER BY e.priority ASC")
    List<CoreBankingEndpointConfiguration> findActiveByEndpointType(@Param("endpointType") CoreBankingEndpointConfiguration.EndpointType endpointType);
    
    /**
     * Find endpoint configurations by HTTP method
     */
    List<CoreBankingEndpointConfiguration> findByHttpMethod(String httpMethod);
    
    /**
     * Find active endpoint configurations by HTTP method
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE e.httpMethod = :httpMethod AND e.isActive = true")
    List<CoreBankingEndpointConfiguration> findActiveByHttpMethod(@Param("httpMethod") String httpMethod);
    
    /**
     * Find endpoint configurations by priority range
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE e.priority BETWEEN :minPriority AND :maxPriority ORDER BY e.priority ASC")
    List<CoreBankingEndpointConfiguration> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find active endpoint configurations by priority range
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE e.priority BETWEEN :minPriority AND :maxPriority AND e.isActive = true ORDER BY e.priority ASC")
    List<CoreBankingEndpointConfiguration> findActiveByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find all active endpoint configurations
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE e.isActive = true ORDER BY e.priority ASC, e.createdAt ASC")
    List<CoreBankingEndpointConfiguration> findAllActive();
    
    /**
     * Find endpoint configurations by core banking configuration ID and endpoint name
     */
    Optional<CoreBankingEndpointConfiguration> findByCoreBankingConfigIdAndEndpointName(
        UUID coreBankingConfigId, 
        String endpointName
    );
    
    /**
     * Find active endpoint configuration by core banking configuration ID and endpoint name
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE e.coreBankingConfigId = :coreBankingConfigId AND e.endpointName = :endpointName AND e.isActive = true")
    Optional<CoreBankingEndpointConfiguration> findActiveByCoreBankingConfigIdAndEndpointName(
        @Param("coreBankingConfigId") UUID coreBankingConfigId,
        @Param("endpointName") String endpointName
    );
    
    /**
     * Check if endpoint configuration exists for core banking configuration and endpoint type
     */
    boolean existsByCoreBankingConfigIdAndEndpointType(
        UUID coreBankingConfigId, 
        CoreBankingEndpointConfiguration.EndpointType endpointType
    );
    
    /**
     * Check if active endpoint configuration exists for core banking configuration and endpoint type
     */
    @Query("SELECT COUNT(e) > 0 FROM CoreBankingEndpointConfiguration e WHERE e.coreBankingConfigId = :coreBankingConfigId AND e.endpointType = :endpointType AND e.isActive = true")
    boolean existsActiveByCoreBankingConfigIdAndEndpointType(
        @Param("coreBankingConfigId") UUID coreBankingConfigId,
        @Param("endpointType") CoreBankingEndpointConfiguration.EndpointType endpointType
    );
    
    /**
     * Count endpoint configurations by core banking configuration ID
     */
    long countByCoreBankingConfigId(UUID coreBankingConfigId);
    
    /**
     * Count active endpoint configurations by core banking configuration ID
     */
    @Query("SELECT COUNT(e) FROM CoreBankingEndpointConfiguration e WHERE e.coreBankingConfigId = :coreBankingConfigId AND e.isActive = true")
    long countActiveByCoreBankingConfigId(@Param("coreBankingConfigId") UUID coreBankingConfigId);
    
    /**
     * Count endpoint configurations by endpoint type
     */
    long countByEndpointType(CoreBankingEndpointConfiguration.EndpointType endpointType);
    
    /**
     * Count active endpoint configurations by endpoint type
     */
    @Query("SELECT COUNT(e) FROM CoreBankingEndpointConfiguration e WHERE e.endpointType = :endpointType AND e.isActive = true")
    long countActiveByEndpointType(@Param("endpointType") CoreBankingEndpointConfiguration.EndpointType endpointType);
    
    /**
     * Find endpoint configurations created by specific user
     */
    List<CoreBankingEndpointConfiguration> findByCreatedBy(String createdBy);
    
    /**
     * Find endpoint configurations updated by specific user
     */
    List<CoreBankingEndpointConfiguration> findByUpdatedBy(String updatedBy);
    
    /**
     * Find endpoint configurations with custom endpoint type
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE e.endpointType = 'CUSTOM' AND e.isActive = true")
    List<CoreBankingEndpointConfiguration> findCustomEndpoints();
    
    /**
     * Find endpoint configurations by description containing text
     */
    @Query("SELECT e FROM CoreBankingEndpointConfiguration e WHERE LOWER(e.description) LIKE LOWER(CONCAT('%', :description, '%')) AND e.isActive = true")
    List<CoreBankingEndpointConfiguration> findByDescriptionContaining(@Param("description") String description);
}