package com.paymentengine.middleware.repository;

import com.paymentengine.middleware.entity.ResiliencyConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Resiliency Configuration entities
 */
@Repository
public interface ResiliencyConfigurationRepository extends JpaRepository<ResiliencyConfiguration, UUID> {
    
    /**
     * Find active configurations by service name and tenant ID
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.serviceName = :serviceName AND rc.tenantId = :tenantId AND rc.isActive = true ORDER BY rc.priority DESC")
    List<ResiliencyConfiguration> findActiveByServiceNameAndTenantId(@Param("serviceName") String serviceName, @Param("tenantId") String tenantId);
    
    /**
     * Find the highest priority active configuration for a service and tenant
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.serviceName = :serviceName AND rc.tenantId = :tenantId AND rc.isActive = true ORDER BY rc.priority DESC LIMIT 1")
    Optional<ResiliencyConfiguration> findTopActiveByServiceNameAndTenantId(@Param("serviceName") String serviceName, @Param("tenantId") String tenantId);
    
    /**
     * Find all active configurations by tenant ID
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.tenantId = :tenantId AND rc.isActive = true ORDER BY rc.serviceName, rc.priority DESC")
    List<ResiliencyConfiguration> findActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find configurations by service name
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.serviceName = :serviceName AND rc.isActive = true ORDER BY rc.tenantId, rc.priority DESC")
    List<ResiliencyConfiguration> findActiveByServiceName(@Param("serviceName") String serviceName);
    
    /**
     * Find configurations matching endpoint pattern
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.endpointPattern IS NOT NULL AND rc.isActive = true")
    List<ResiliencyConfiguration> findActiveWithEndpointPattern();
    
    /**
     * Count configurations by service name and tenant ID
     */
    @Query("SELECT COUNT(rc) FROM ResiliencyConfiguration rc WHERE rc.serviceName = :serviceName AND rc.tenantId = :tenantId AND rc.isActive = true")
    long countActiveByServiceNameAndTenantId(@Param("serviceName") String serviceName, @Param("tenantId") String tenantId);
    
    /**
     * Find configurations with circuit breaker enabled
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.circuitBreakerConfig IS NOT NULL AND rc.isActive = true")
    List<ResiliencyConfiguration> findActiveWithCircuitBreaker();
    
    /**
     * Find configurations with retry enabled
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.retryConfig IS NOT NULL AND rc.isActive = true")
    List<ResiliencyConfiguration> findActiveWithRetry();
    
    /**
     * Find configurations with bulkhead enabled
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.bulkheadConfig IS NOT NULL AND rc.isActive = true")
    List<ResiliencyConfiguration> findActiveWithBulkhead();
    
    /**
     * Find configurations with health check enabled
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.healthCheckConfig.healthCheckEnabled = true AND rc.isActive = true")
    List<ResiliencyConfiguration> findActiveWithHealthCheck();
    
    /**
     * Find configurations with monitoring enabled
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.monitoringConfig.metricsEnabled = true AND rc.isActive = true")
    List<ResiliencyConfiguration> findActiveWithMonitoring();
    
    /**
     * Find configurations by priority range
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.priority BETWEEN :minPriority AND :maxPriority AND rc.isActive = true ORDER BY rc.priority DESC")
    List<ResiliencyConfiguration> findActiveByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find configurations created after a specific date
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.createdAt >= :createdAfter AND rc.isActive = true ORDER BY rc.createdAt DESC")
    List<ResiliencyConfiguration> findActiveCreatedAfter(@Param("createdAfter") java.time.LocalDateTime createdAfter);
    
    /**
     * Find configurations updated after a specific date
     */
    @Query("SELECT rc FROM ResiliencyConfiguration rc WHERE rc.updatedAt >= :updatedAfter AND rc.isActive = true ORDER BY rc.updatedAt DESC")
    List<ResiliencyConfiguration> findActiveUpdatedAfter(@Param("updatedAfter") java.time.LocalDateTime updatedAfter);
    
    /**
     * Find resiliency configuration by service name and tenant ID
     */
    Optional<ResiliencyConfiguration> findByServiceNameAndTenantIdAndIsActiveTrue(String serviceName, String tenantId);

    /**
     * Find all resiliency configurations by tenant ID
     */
    List<ResiliencyConfiguration> findByTenantIdAndIsActiveTrue(String tenantId);

    /**
     * Find distinct tenant IDs
     */
    @Query("SELECT DISTINCT rc.tenantId FROM ResiliencyConfiguration rc WHERE rc.isActive = true")
    List<String> findDistinctTenantIds();
}