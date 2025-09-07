package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.AdvancedPayloadMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Advanced Payload Mapping entities
 */
@Repository
public interface AdvancedPayloadMappingRepository extends JpaRepository<AdvancedPayloadMapping, UUID> {
    
    /**
     * Find payload mappings by tenant ID
     */
    List<AdvancedPayloadMapping> findByTenantId(String tenantId);
    
    /**
     * Find active payload mappings by tenant ID
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find payload mappings by tenant ID and payment type
     */
    List<AdvancedPayloadMapping> findByTenantIdAndPaymentType(String tenantId, String paymentType);
    
    /**
     * Find active payload mappings by tenant ID and payment type
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.paymentType = :paymentType AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByTenantIdAndPaymentType(@Param("tenantId") String tenantId, @Param("paymentType") String paymentType);
    
    /**
     * Find payload mappings by tenant ID and local instrumentation code
     */
    List<AdvancedPayloadMapping> findByTenantIdAndLocalInstrumentationCode(String tenantId, String localInstrumentationCode);
    
    /**
     * Find active payload mappings by tenant ID and local instrumentation code
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.localInstrumentationCode = :localInstrumentationCode AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByTenantIdAndLocalInstrumentationCode(@Param("tenantId") String tenantId, @Param("localInstrumentationCode") String localInstrumentationCode);
    
    /**
     * Find payload mappings by tenant ID and clearing system code
     */
    List<AdvancedPayloadMapping> findByTenantIdAndClearingSystemCode(String tenantId, String clearingSystemCode);
    
    /**
     * Find active payload mappings by tenant ID and clearing system code
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.clearingSystemCode = :clearingSystemCode AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByTenantIdAndClearingSystemCode(@Param("tenantId") String tenantId, @Param("clearingSystemCode") String clearingSystemCode);
    
    /**
     * Find payload mappings by mapping type
     */
    List<AdvancedPayloadMapping> findByMappingType(AdvancedPayloadMapping.MappingType mappingType);
    
    /**
     * Find active payload mappings by mapping type
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.mappingType = :mappingType AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByMappingType(@Param("mappingType") AdvancedPayloadMapping.MappingType mappingType);
    
    /**
     * Find payload mappings by direction
     */
    List<AdvancedPayloadMapping> findByDirection(AdvancedPayloadMapping.Direction direction);
    
    /**
     * Find active payload mappings by direction
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.direction = :direction AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByDirection(@Param("direction") AdvancedPayloadMapping.Direction direction);
    
    /**
     * Find payload mappings by tenant ID and direction
     */
    List<AdvancedPayloadMapping> findByTenantIdAndDirection(String tenantId, AdvancedPayloadMapping.Direction direction);
    
    /**
     * Find active payload mappings by tenant ID and direction
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.direction = :direction AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByTenantIdAndDirection(@Param("tenantId") String tenantId, @Param("direction") AdvancedPayloadMapping.Direction direction);
    
    /**
     * Find payload mappings by tenant ID, payment type, and local instrumentation code
     */
    List<AdvancedPayloadMapping> findByTenantIdAndPaymentTypeAndLocalInstrumentationCode(
        String tenantId, String paymentType, String localInstrumentationCode);
    
    /**
     * Find active payload mappings by tenant ID, payment type, and local instrumentation code
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.paymentType = :paymentType AND apm.localInstrumentationCode = :localInstrumentationCode AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByTenantIdAndPaymentTypeAndLocalInstrumentationCode(
        @Param("tenantId") String tenantId, 
        @Param("paymentType") String paymentType, 
        @Param("localInstrumentationCode") String localInstrumentationCode);
    
    /**
     * Find payload mappings by tenant ID, payment type, local instrumentation code, and clearing system code
     */
    List<AdvancedPayloadMapping> findByTenantIdAndPaymentTypeAndLocalInstrumentationCodeAndClearingSystemCode(
        String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode);
    
    /**
     * Find active payload mappings by tenant ID, payment type, local instrumentation code, and clearing system code
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.paymentType = :paymentType AND apm.localInstrumentationCode = :localInstrumentationCode AND apm.clearingSystemCode = :clearingSystemCode AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByTenantIdAndPaymentTypeAndLocalInstrumentationCodeAndClearingSystemCode(
        @Param("tenantId") String tenantId, 
        @Param("paymentType") String paymentType, 
        @Param("localInstrumentationCode") String localInstrumentationCode, 
        @Param("clearingSystemCode") String clearingSystemCode);
    
    /**
     * Find payload mappings by mapping name
     */
    List<AdvancedPayloadMapping> findByMappingName(String mappingName);
    
    /**
     * Find active payload mappings by mapping name
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.mappingName = :mappingName AND apm.isActive = true")
    List<AdvancedPayloadMapping> findActiveByMappingName(@Param("mappingName") String mappingName);
    
    /**
     * Find payload mapping by tenant ID and mapping name
     */
    Optional<AdvancedPayloadMapping> findByTenantIdAndMappingName(String tenantId, String mappingName);
    
    /**
     * Find active payload mapping by tenant ID and mapping name
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.mappingName = :mappingName AND apm.isActive = true")
    Optional<AdvancedPayloadMapping> findActiveByTenantIdAndMappingName(@Param("tenantId") String tenantId, @Param("mappingName") String mappingName);
    
    /**
     * Find payload mappings by version
     */
    List<AdvancedPayloadMapping> findByVersion(String version);
    
    /**
     * Find active payload mappings by version
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.version = :version AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByVersion(@Param("version") String version);
    
    /**
     * Find payload mappings by priority range
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.priority BETWEEN :minPriority AND :maxPriority ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find active payload mappings by priority range
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.priority BETWEEN :minPriority AND :maxPriority AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findActiveByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find all active payload mappings
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.isActive = true ORDER BY apm.priority ASC, apm.createdAt ASC")
    List<AdvancedPayloadMapping> findAllActive();
    
    /**
     * Find payload mappings with value assignments
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.valueAssignments IS NOT NULL AND apm.isActive = true")
    List<AdvancedPayloadMapping> findWithValueAssignments();
    
    /**
     * Find payload mappings with derived value rules
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.derivedValueRules IS NOT NULL AND apm.isActive = true")
    List<AdvancedPayloadMapping> findWithDerivedValueRules();
    
    /**
     * Find payload mappings with auto generation rules
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.autoGenerationRules IS NOT NULL AND apm.isActive = true")
    List<AdvancedPayloadMapping> findWithAutoGenerationRules();
    
    /**
     * Find payload mappings with conditional mappings
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.conditionalMappings IS NOT NULL AND apm.isActive = true")
    List<AdvancedPayloadMapping> findWithConditionalMappings();
    
    /**
     * Find payload mappings by tenant ID with value assignments
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.valueAssignments IS NOT NULL AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findByTenantIdWithValueAssignments(@Param("tenantId") String tenantId);
    
    /**
     * Find payload mappings by tenant ID with derived value rules
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.derivedValueRules IS NOT NULL AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findByTenantIdWithDerivedValueRules(@Param("tenantId") String tenantId);
    
    /**
     * Find payload mappings by tenant ID with auto generation rules
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.autoGenerationRules IS NOT NULL AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findByTenantIdWithAutoGenerationRules(@Param("tenantId") String tenantId);
    
    /**
     * Find payload mappings by tenant ID with conditional mappings
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.conditionalMappings IS NOT NULL AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findByTenantIdWithConditionalMappings(@Param("tenantId") String tenantId);
    
    /**
     * Check if payload mapping exists for tenant and mapping name
     */
    boolean existsByTenantIdAndMappingName(String tenantId, String mappingName);
    
    /**
     * Check if active payload mapping exists for tenant and mapping name
     */
    @Query("SELECT COUNT(apm) > 0 FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.mappingName = :mappingName AND apm.isActive = true")
    boolean existsActiveByTenantIdAndMappingName(@Param("tenantId") String tenantId, @Param("mappingName") String mappingName);
    
    /**
     * Count payload mappings by tenant ID
     */
    long countByTenantId(String tenantId);
    
    /**
     * Count active payload mappings by tenant ID
     */
    @Query("SELECT COUNT(apm) FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.isActive = true")
    long countActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count payload mappings by mapping type
     */
    long countByMappingType(AdvancedPayloadMapping.MappingType mappingType);
    
    /**
     * Count active payload mappings by mapping type
     */
    @Query("SELECT COUNT(apm) FROM AdvancedPayloadMapping apm WHERE apm.mappingType = :mappingType AND apm.isActive = true")
    long countActiveByMappingType(@Param("mappingType") AdvancedPayloadMapping.MappingType mappingType);
    
    /**
     * Count payload mappings by direction
     */
    long countByDirection(AdvancedPayloadMapping.Direction direction);
    
    /**
     * Count active payload mappings by direction
     */
    @Query("SELECT COUNT(apm) FROM AdvancedPayloadMapping apm WHERE apm.direction = :direction AND apm.isActive = true")
    long countActiveByDirection(@Param("direction") AdvancedPayloadMapping.Direction direction);
    
    /**
     * Find payload mappings created by specific user
     */
    List<AdvancedPayloadMapping> findByCreatedBy(String createdBy);
    
    /**
     * Find payload mappings updated by specific user
     */
    List<AdvancedPayloadMapping> findByUpdatedBy(String updatedBy);
    
    /**
     * Find payload mappings by description containing text
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE LOWER(apm.description) LIKE LOWER(CONCAT('%', :description, '%')) AND apm.isActive = true")
    List<AdvancedPayloadMapping> findByDescriptionContaining(@Param("description") String description);
    
    /**
     * Find latest version of payload mappings by tenant ID and mapping name
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.mappingName = :mappingName AND apm.isActive = true ORDER BY apm.version DESC")
    List<AdvancedPayloadMapping> findLatestVersionByTenantIdAndMappingName(
        @Param("tenantId") String tenantId,
        @Param("mappingName") String mappingName);
    
    /**
     * Find payload mappings with custom mapping type
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.mappingType = 'CUSTOM_MAPPING' AND apm.isActive = true")
    List<AdvancedPayloadMapping> findCustomMappings();
    
    /**
     * Find payload mappings with bidirectional direction
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.direction = 'BIDIRECTIONAL' AND apm.isActive = true")
    List<AdvancedPayloadMapping> findBidirectionalMappings();
    
    /**
     * Find payload mappings by tenant ID and payment type with specific mapping type
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.paymentType = :paymentType AND apm.mappingType = :mappingType AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findByTenantIdAndPaymentTypeAndMappingType(
        @Param("tenantId") String tenantId, 
        @Param("paymentType") String paymentType, 
        @Param("mappingType") AdvancedPayloadMapping.MappingType mappingType);
    
    /**
     * Find payload mappings by tenant ID and clearing system code with specific mapping type
     */
    @Query("SELECT apm FROM AdvancedPayloadMapping apm WHERE apm.tenantId = :tenantId AND apm.clearingSystemCode = :clearingSystemCode AND apm.mappingType = :mappingType AND apm.isActive = true ORDER BY apm.priority ASC")
    List<AdvancedPayloadMapping> findByTenantIdAndClearingSystemCodeAndMappingType(
        @Param("tenantId") String tenantId, 
        @Param("clearingSystemCode") String clearingSystemCode, 
        @Param("mappingType") AdvancedPayloadMapping.MappingType mappingType);
}