package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.PayloadSchemaMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Payload Schema Mapping entities
 */
@Repository
public interface PayloadSchemaMappingRepository extends JpaRepository<PayloadSchemaMapping, UUID> {
    
    /**
     * Find payload schema mappings by endpoint configuration ID
     */
    List<PayloadSchemaMapping> findByEndpointConfigId(UUID endpointConfigId);
    
    /**
     * Find active payload schema mappings by endpoint configuration ID
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.endpointConfigId = :endpointConfigId AND p.isActive = true ORDER BY p.priority ASC")
    List<PayloadSchemaMapping> findActiveByEndpointConfigId(@Param("endpointConfigId") UUID endpointConfigId);
    
    /**
     * Find payload schema mapping by endpoint configuration ID and mapping name
     */
    Optional<PayloadSchemaMapping> findByEndpointConfigIdAndMappingName(
        UUID endpointConfigId, 
        String mappingName
    );
    
    /**
     * Find active payload schema mapping by endpoint configuration ID and mapping name
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.endpointConfigId = :endpointConfigId AND p.mappingName = :mappingName AND p.isActive = true")
    Optional<PayloadSchemaMapping> findActiveByEndpointConfigIdAndMappingName(
        @Param("endpointConfigId") UUID endpointConfigId,
        @Param("mappingName") String mappingName
    );
    
    /**
     * Find payload schema mappings by mapping type
     */
    List<PayloadSchemaMapping> findByMappingType(PayloadSchemaMapping.MappingType mappingType);
    
    /**
     * Find active payload schema mappings by mapping type
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.mappingType = :mappingType AND p.isActive = true ORDER BY p.priority ASC")
    List<PayloadSchemaMapping> findActiveByMappingType(@Param("mappingType") PayloadSchemaMapping.MappingType mappingType);
    
    /**
     * Find payload schema mappings by direction
     */
    List<PayloadSchemaMapping> findByDirection(PayloadSchemaMapping.Direction direction);
    
    /**
     * Find active payload schema mappings by direction
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.direction = :direction AND p.isActive = true ORDER BY p.priority ASC")
    List<PayloadSchemaMapping> findActiveByDirection(@Param("direction") PayloadSchemaMapping.Direction direction);
    
    /**
     * Find payload schema mappings by endpoint configuration ID and direction
     */
    List<PayloadSchemaMapping> findByEndpointConfigIdAndDirection(
        UUID endpointConfigId, 
        PayloadSchemaMapping.Direction direction
    );
    
    /**
     * Find active payload schema mappings by endpoint configuration ID and direction
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.endpointConfigId = :endpointConfigId AND p.direction = :direction AND p.isActive = true ORDER BY p.priority ASC")
    List<PayloadSchemaMapping> findActiveByEndpointConfigIdAndDirection(
        @Param("endpointConfigId") UUID endpointConfigId,
        @Param("direction") PayloadSchemaMapping.Direction direction
    );
    
    /**
     * Find payload schema mappings by endpoint configuration ID and mapping type
     */
    List<PayloadSchemaMapping> findByEndpointConfigIdAndMappingType(
        UUID endpointConfigId, 
        PayloadSchemaMapping.MappingType mappingType
    );
    
    /**
     * Find active payload schema mappings by endpoint configuration ID and mapping type
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.endpointConfigId = :endpointConfigId AND p.mappingType = :mappingType AND p.isActive = true ORDER BY p.priority ASC")
    List<PayloadSchemaMapping> findActiveByEndpointConfigIdAndMappingType(
        @Param("endpointConfigId") UUID endpointConfigId,
        @Param("mappingType") PayloadSchemaMapping.MappingType mappingType
    );
    
    /**
     * Find payload schema mappings by version
     */
    List<PayloadSchemaMapping> findByVersion(String version);
    
    /**
     * Find active payload schema mappings by version
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.version = :version AND p.isActive = true ORDER BY p.priority ASC")
    List<PayloadSchemaMapping> findActiveByVersion(@Param("version") String version);
    
    /**
     * Find payload schema mappings by priority range
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.priority BETWEEN :minPriority AND :maxPriority ORDER BY p.priority ASC")
    List<PayloadSchemaMapping> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find active payload schema mappings by priority range
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.priority BETWEEN :minPriority AND :maxPriority AND p.isActive = true ORDER BY p.priority ASC")
    List<PayloadSchemaMapping> findActiveByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find all active payload schema mappings
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.isActive = true ORDER BY p.priority ASC, p.createdAt ASC")
    List<PayloadSchemaMapping> findAllActive();
    
    /**
     * Find payload schema mappings by mapping name
     */
    List<PayloadSchemaMapping> findByMappingName(String mappingName);
    
    /**
     * Find active payload schema mappings by mapping name
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.mappingName = :mappingName AND p.isActive = true")
    List<PayloadSchemaMapping> findActiveByMappingName(@Param("mappingName") String mappingName);
    
    /**
     * Check if payload schema mapping exists for endpoint configuration and mapping name
     */
    boolean existsByEndpointConfigIdAndMappingName(UUID endpointConfigId, String mappingName);
    
    /**
     * Check if active payload schema mapping exists for endpoint configuration and mapping name
     */
    @Query("SELECT COUNT(p) > 0 FROM PayloadSchemaMapping p WHERE p.endpointConfigId = :endpointConfigId AND p.mappingName = :mappingName AND p.isActive = true")
    boolean existsActiveByEndpointConfigIdAndMappingName(@Param("endpointConfigId") UUID endpointConfigId, @Param("mappingName") String mappingName);
    
    /**
     * Count payload schema mappings by endpoint configuration ID
     */
    long countByEndpointConfigId(UUID endpointConfigId);
    
    /**
     * Count active payload schema mappings by endpoint configuration ID
     */
    @Query("SELECT COUNT(p) FROM PayloadSchemaMapping p WHERE p.endpointConfigId = :endpointConfigId AND p.isActive = true")
    long countActiveByEndpointConfigId(@Param("endpointConfigId") UUID endpointConfigId);
    
    /**
     * Count payload schema mappings by mapping type
     */
    long countByMappingType(PayloadSchemaMapping.MappingType mappingType);
    
    /**
     * Count active payload schema mappings by mapping type
     */
    @Query("SELECT COUNT(p) FROM PayloadSchemaMapping p WHERE p.mappingType = :mappingType AND p.isActive = true")
    long countActiveByMappingType(@Param("mappingType") PayloadSchemaMapping.MappingType mappingType);
    
    /**
     * Count payload schema mappings by direction
     */
    long countByDirection(PayloadSchemaMapping.Direction direction);
    
    /**
     * Count active payload schema mappings by direction
     */
    @Query("SELECT COUNT(p) FROM PayloadSchemaMapping p WHERE p.direction = :direction AND p.isActive = true")
    long countActiveByDirection(@Param("direction") PayloadSchemaMapping.Direction direction);
    
    /**
     * Find payload schema mappings created by specific user
     */
    List<PayloadSchemaMapping> findByCreatedBy(String createdBy);
    
    /**
     * Find payload schema mappings updated by specific user
     */
    List<PayloadSchemaMapping> findByUpdatedBy(String updatedBy);
    
    /**
     * Find payload schema mappings by description containing text
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%')) AND p.isActive = true")
    List<PayloadSchemaMapping> findByDescriptionContaining(@Param("description") String description);
    
    /**
     * Find latest version of payload schema mappings by endpoint configuration ID and mapping name
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.endpointConfigId = :endpointConfigId AND p.mappingName = :mappingName AND p.isActive = true ORDER BY p.version DESC")
    List<PayloadSchemaMapping> findLatestVersionByEndpointConfigIdAndMappingName(
        @Param("endpointConfigId") UUID endpointConfigId,
        @Param("mappingName") String mappingName
    );
    
    /**
     * Find payload schema mappings with custom mapping type
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.mappingType = 'CUSTOM_MAPPING' AND p.isActive = true")
    List<PayloadSchemaMapping> findCustomMappings();
    
    /**
     * Find payload schema mappings with bidirectional direction
     */
    @Query("SELECT p FROM PayloadSchemaMapping p WHERE p.direction = 'BIDIRECTIONAL' AND p.isActive = true")
    List<PayloadSchemaMapping> findBidirectionalMappings();
}