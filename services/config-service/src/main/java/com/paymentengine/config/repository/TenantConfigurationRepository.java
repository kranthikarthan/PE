package com.paymentengine.config.repository;

import com.paymentengine.config.entity.TenantConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantConfigurationRepository extends JpaRepository<TenantConfiguration, UUID> {
    
    List<TenantConfiguration> findByTenantId(UUID tenantId);
    
    List<TenantConfiguration> findByTenantIdAndIsActive(UUID tenantId, Boolean isActive);
    
    Optional<TenantConfiguration> findByTenantIdAndConfigKey(UUID tenantId, String configKey);
    
    Optional<TenantConfiguration> findByTenantIdAndConfigKeyAndIsActive(UUID tenantId, String configKey, Boolean isActive);
    
    List<TenantConfiguration> findByConfigKey(String configKey);
    
    List<TenantConfiguration> findByConfigType(TenantConfiguration.ConfigType configType);
    
    List<TenantConfiguration> findByIsEncrypted(Boolean isEncrypted);
    
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId AND tc.configKey LIKE %:keyPattern%")
    List<TenantConfiguration> findByTenantIdAndConfigKeyContaining(@Param("tenantId") UUID tenantId, 
                                                                   @Param("keyPattern") String keyPattern);
    
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId AND tc.configType = :configType AND tc.isActive = true")
    List<TenantConfiguration> findActiveByTenantIdAndConfigType(@Param("tenantId") UUID tenantId, 
                                                               @Param("configType") TenantConfiguration.ConfigType configType);
    
    @Query("SELECT COUNT(tc) FROM TenantConfiguration tc WHERE tc.tenantId = :tenantId AND tc.isActive = :isActive")
    Long countByTenantIdAndIsActive(@Param("tenantId") UUID tenantId, @Param("isActive") Boolean isActive);
    
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.configKey = :configKey AND tc.isActive = true")
    List<TenantConfiguration> findActiveByConfigKey(@Param("configKey") String configKey);
    
    boolean existsByTenantIdAndConfigKey(UUID tenantId, String configKey);
}