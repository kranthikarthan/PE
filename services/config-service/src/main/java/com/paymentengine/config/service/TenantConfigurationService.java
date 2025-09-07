package com.paymentengine.config.service;

import com.paymentengine.config.entity.TenantConfiguration;
import com.paymentengine.config.entity.ConfigurationHistory;
import com.paymentengine.config.repository.TenantConfigurationRepository;
import com.paymentengine.config.repository.ConfigurationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TenantConfigurationService {
    
    @Autowired
    private TenantConfigurationRepository tenantConfigurationRepository;
    
    @Autowired
    private ConfigurationHistoryRepository configurationHistoryRepository;
    
    @Cacheable(value = "tenant-configurations", key = "#tenantId + ':' + #configKey")
    public Optional<TenantConfiguration> findByTenantIdAndConfigKey(UUID tenantId, String configKey) {
        return tenantConfigurationRepository.findByTenantIdAndConfigKeyAndIsActive(tenantId, configKey, true);
    }
    
    @Cacheable(value = "tenant-configurations", key = "#tenantId")
    public List<TenantConfiguration> findByTenantId(UUID tenantId) {
        return tenantConfigurationRepository.findByTenantIdAndIsActive(tenantId, true);
    }
    
    public List<TenantConfiguration> findByConfigKey(String configKey) {
        return tenantConfigurationRepository.findActiveByConfigKey(configKey);
    }
    
    public List<TenantConfiguration> findByConfigType(TenantConfiguration.ConfigType configType) {
        return tenantConfigurationRepository.findByConfigType(configType);
    }
    
    public List<TenantConfiguration> findByTenantIdAndConfigType(UUID tenantId, TenantConfiguration.ConfigType configType) {
        return tenantConfigurationRepository.findActiveByTenantIdAndConfigType(tenantId, configType);
    }
    
    @CacheEvict(value = "tenant-configurations", key = "#tenantId + ':' + #configKey")
    public TenantConfiguration setConfiguration(UUID tenantId, String configKey, String configValue, 
                                               TenantConfiguration.ConfigType configType, Boolean isEncrypted) {
        
        Optional<TenantConfiguration> existingConfig = tenantConfigurationRepository
                .findByTenantIdAndConfigKey(tenantId, configKey);
        
        String oldValue = null;
        TenantConfiguration configuration;
        
        if (existingConfig.isPresent()) {
            configuration = existingConfig.get();
            oldValue = configuration.getConfigValue();
            configuration.setConfigValue(configValue);
            configuration.setConfigType(configType);
            configuration.setIsEncrypted(isEncrypted);
            configuration.setUpdatedAt(LocalDateTime.now());
        } else {
            configuration = new TenantConfiguration(tenantId, configKey, configValue, configType);
            configuration.setIsEncrypted(isEncrypted);
            configuration.setCreatedAt(LocalDateTime.now());
        }
        
        TenantConfiguration savedConfig = tenantConfigurationRepository.save(configuration);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.TENANT_CONFIG,
                savedConfig.getId(),
                configKey,
                oldValue,
                configValue,
                "Configuration updated",
                "system"
        );
        configurationHistoryRepository.save(history);
        
        return savedConfig;
    }
    
    @CacheEvict(value = "tenant-configurations", key = "#tenantId + ':' + #configKey")
    public void deleteConfiguration(UUID tenantId, String configKey) {
        Optional<TenantConfiguration> config = tenantConfigurationRepository
                .findByTenantIdAndConfigKey(tenantId, configKey);
        
        if (config.isPresent()) {
            TenantConfiguration configuration = config.get();
            String oldValue = configuration.getConfigValue();
            
            configuration.setIsActive(false);
            configuration.setUpdatedAt(LocalDateTime.now());
            tenantConfigurationRepository.save(configuration);
            
            // Record in history
            ConfigurationHistory history = new ConfigurationHistory(
                    ConfigurationHistory.ConfigType.TENANT_CONFIG,
                    configuration.getId(),
                    configKey,
                    oldValue,
                    null,
                    "Configuration deleted",
                    "system"
            );
            configurationHistoryRepository.save(history);
        }
    }
    
    @CacheEvict(value = "tenant-configurations", key = "#tenantId + ':' + #configKey")
    public void activateConfiguration(UUID tenantId, String configKey) {
        Optional<TenantConfiguration> config = tenantConfigurationRepository
                .findByTenantIdAndConfigKey(tenantId, configKey);
        
        if (config.isPresent()) {
            TenantConfiguration configuration = config.get();
            configuration.setIsActive(true);
            configuration.setUpdatedAt(LocalDateTime.now());
            tenantConfigurationRepository.save(configuration);
        }
    }
    
    @CacheEvict(value = "tenant-configurations", key = "#tenantId + ':' + #configKey")
    public void deactivateConfiguration(UUID tenantId, String configKey) {
        Optional<TenantConfiguration> config = tenantConfigurationRepository
                .findByTenantIdAndConfigKey(tenantId, configKey);
        
        if (config.isPresent()) {
            TenantConfiguration configuration = config.get();
            configuration.setIsActive(false);
            configuration.setUpdatedAt(LocalDateTime.now());
            tenantConfigurationRepository.save(configuration);
        }
    }
    
    public String getStringValue(UUID tenantId, String configKey, String defaultValue) {
        return findByTenantIdAndConfigKey(tenantId, configKey)
                .map(TenantConfiguration::getConfigValue)
                .orElse(defaultValue);
    }
    
    public Integer getIntegerValue(UUID tenantId, String configKey, Integer defaultValue) {
        return findByTenantIdAndConfigKey(tenantId, configKey)
                .map(config -> {
                    try {
                        return Integer.parseInt(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }
    
    public Boolean getBooleanValue(UUID tenantId, String configKey, Boolean defaultValue) {
        return findByTenantIdAndConfigKey(tenantId, configKey)
                .map(config -> Boolean.parseBoolean(config.getConfigValue()))
                .orElse(defaultValue);
    }
    
    public Long countByTenantIdAndIsActive(UUID tenantId, Boolean isActive) {
        return tenantConfigurationRepository.countByTenantIdAndIsActive(tenantId, isActive);
    }
    
    public List<TenantConfiguration> findByTenantIdAndConfigKeyContaining(UUID tenantId, String keyPattern) {
        return tenantConfigurationRepository.findByTenantIdAndConfigKeyContaining(tenantId, keyPattern);
    }
    
    public boolean existsByTenantIdAndConfigKey(UUID tenantId, String configKey) {
        return tenantConfigurationRepository.existsByTenantIdAndConfigKey(tenantId, configKey);
    }
}