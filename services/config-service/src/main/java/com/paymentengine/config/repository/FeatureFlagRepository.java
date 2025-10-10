package com.paymentengine.config.repository;

import com.paymentengine.config.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {
    
    Optional<FeatureFlag> findByFlagName(String flagName);
    
    List<FeatureFlag> findByTenantId(UUID tenantId);
    
    List<FeatureFlag> findByTenantIdAndIsActive(UUID tenantId, Boolean isActive);
    
    List<FeatureFlag> findByEnvironment(FeatureFlag.Environment environment);
    
    List<FeatureFlag> findByIsActive(Boolean isActive);
    
    Optional<FeatureFlag> findByFlagNameAndTenantId(String flagName, UUID tenantId);
    
    Optional<FeatureFlag> findByFlagNameAndTenantIdAndIsActive(String flagName, UUID tenantId, Boolean isActive);
    
    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.flagName = :flagName AND ff.environment = :environment AND ff.isActive = true")
    List<FeatureFlag> findActiveByFlagNameAndEnvironment(@Param("flagName") String flagName, 
                                                       @Param("environment") FeatureFlag.Environment environment);
    
    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.tenantId = :tenantId AND ff.environment = :environment AND ff.isActive = true")
    List<FeatureFlag> findActiveByTenantIdAndEnvironment(@Param("tenantId") UUID tenantId, 
                                                       @Param("environment") FeatureFlag.Environment environment);
    
    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.flagValue = :flagValue AND ff.isActive = true")
    List<FeatureFlag> findActiveByFlagValue(@Param("flagValue") Boolean flagValue);
    
    @Query("SELECT COUNT(ff) FROM FeatureFlag ff WHERE ff.tenantId = :tenantId AND ff.isActive = :isActive")
    Long countByTenantIdAndIsActive(@Param("tenantId") UUID tenantId, @Param("isActive") Boolean isActive);
    
    @Query("SELECT COUNT(ff) FROM FeatureFlag ff WHERE ff.flagValue = :flagValue AND ff.isActive = true")
    Long countActiveByFlagValue(@Param("flagValue") Boolean flagValue);
    
    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.flagName LIKE %:name%")
    List<FeatureFlag> findByFlagNameContaining(@Param("name") String name);
    
    boolean existsByFlagName(String flagName);
    
    boolean existsByFlagNameAndTenantId(String flagName, UUID tenantId);
}