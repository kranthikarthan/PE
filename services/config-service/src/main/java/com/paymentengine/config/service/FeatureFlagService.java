package com.paymentengine.config.service;

import com.paymentengine.config.entity.FeatureFlag;
import com.paymentengine.config.entity.ConfigurationHistory;
import com.paymentengine.config.repository.FeatureFlagRepository;
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
public class FeatureFlagService {
    
    @Autowired
    private FeatureFlagRepository featureFlagRepository;
    
    @Autowired
    private ConfigurationHistoryRepository configurationHistoryRepository;
    
    @Cacheable(value = "feature-flags", key = "#flagName")
    public Optional<FeatureFlag> findByFlagName(String flagName) {
        return featureFlagRepository.findByFlagName(flagName);
    }
    
    @Cacheable(value = "feature-flags", key = "#tenantId + ':' + #flagName")
    public Optional<FeatureFlag> findByFlagNameAndTenantId(String flagName, UUID tenantId) {
        return featureFlagRepository.findByFlagNameAndTenantIdAndIsActive(flagName, tenantId, true);
    }
    
    @Cacheable(value = "feature-flags", key = "#tenantId")
    public List<FeatureFlag> findByTenantId(UUID tenantId) {
        return featureFlagRepository.findByTenantIdAndIsActive(tenantId, true);
    }
    
    @Cacheable(value = "feature-flags", key = "#environment")
    public List<FeatureFlag> findByEnvironment(FeatureFlag.Environment environment) {
        return featureFlagRepository.findByEnvironment(environment);
    }
    
    public List<FeatureFlag> findActiveByFlagNameAndEnvironment(String flagName, FeatureFlag.Environment environment) {
        return featureFlagRepository.findActiveByFlagNameAndEnvironment(flagName, environment);
    }
    
    public List<FeatureFlag> findActiveByTenantIdAndEnvironment(UUID tenantId, FeatureFlag.Environment environment) {
        return featureFlagRepository.findActiveByTenantIdAndEnvironment(tenantId, environment);
    }
    
    @CacheEvict(value = "feature-flags", key = "#flagName")
    public FeatureFlag createFeatureFlag(String flagName, String flagDescription, Boolean flagValue, 
                                        UUID tenantId, FeatureFlag.Environment environment, 
                                        Integer rolloutPercentage, String targetUsers) {
        
        if (featureFlagRepository.existsByFlagName(flagName)) {
            throw new RuntimeException("Feature flag with name " + flagName + " already exists");
        }
        
        FeatureFlag featureFlag = new FeatureFlag();
        featureFlag.setFlagName(flagName);
        featureFlag.setFlagDescription(flagDescription);
        featureFlag.setFlagValue(flagValue);
        featureFlag.setTenantId(tenantId);
        featureFlag.setEnvironment(environment);
        featureFlag.setRolloutPercentage(rolloutPercentage);
        featureFlag.setTargetUsers(targetUsers);
        featureFlag.setIsActive(true);
        featureFlag.setCreatedAt(LocalDateTime.now());
        
        FeatureFlag savedFlag = featureFlagRepository.save(featureFlag);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.FEATURE_FLAG,
                savedFlag.getId(),
                flagName,
                null,
                flagValue.toString(),
                "Feature flag created",
                "system"
        );
        configurationHistoryRepository.save(history);
        
        return savedFlag;
    }
    
    @CacheEvict(value = "feature-flags", key = "#flagName")
    public FeatureFlag updateFeatureFlag(String flagName, String flagDescription, Boolean flagValue, 
                                        Integer rolloutPercentage, String targetUsers) {
        
        FeatureFlag featureFlag = featureFlagRepository.findByFlagName(flagName)
                .orElseThrow(() -> new RuntimeException("Feature flag not found"));
        
        String oldValue = featureFlag.getFlagValue().toString();
        
        featureFlag.setFlagDescription(flagDescription);
        featureFlag.setFlagValue(flagValue);
        featureFlag.setRolloutPercentage(rolloutPercentage);
        featureFlag.setTargetUsers(targetUsers);
        featureFlag.setUpdatedAt(LocalDateTime.now());
        
        FeatureFlag savedFlag = featureFlagRepository.save(featureFlag);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.FEATURE_FLAG,
                savedFlag.getId(),
                flagName,
                oldValue,
                flagValue.toString(),
                "Feature flag updated",
                "system"
        );
        configurationHistoryRepository.save(history);
        
        return savedFlag;
    }
    
    @CacheEvict(value = "feature-flags", key = "#flagName")
    public void toggleFeatureFlag(String flagName) {
        FeatureFlag featureFlag = featureFlagRepository.findByFlagName(flagName)
                .orElseThrow(() -> new RuntimeException("Feature flag not found"));
        
        String oldValue = featureFlag.getFlagValue().toString();
        Boolean newValue = !featureFlag.getFlagValue();
        featureFlag.setFlagValue(newValue);
        featureFlag.setUpdatedAt(LocalDateTime.now());
        
        featureFlagRepository.save(featureFlag);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.FEATURE_FLAG,
                featureFlag.getId(),
                flagName,
                oldValue,
                newValue.toString(),
                "Feature flag toggled",
                "system"
        );
        configurationHistoryRepository.save(history);
    }
    
    @CacheEvict(value = "feature-flags", key = "#flagName")
    public void activateFeatureFlag(String flagName) {
        FeatureFlag featureFlag = featureFlagRepository.findByFlagName(flagName)
                .orElseThrow(() -> new RuntimeException("Feature flag not found"));
        
        featureFlag.setIsActive(true);
        featureFlag.setUpdatedAt(LocalDateTime.now());
        featureFlagRepository.save(featureFlag);
    }
    
    @CacheEvict(value = "feature-flags", key = "#flagName")
    public void deactivateFeatureFlag(String flagName) {
        FeatureFlag featureFlag = featureFlagRepository.findByFlagName(flagName)
                .orElseThrow(() -> new RuntimeException("Feature flag not found"));
        
        featureFlag.setIsActive(false);
        featureFlag.setUpdatedAt(LocalDateTime.now());
        featureFlagRepository.save(featureFlag);
    }
    
    @CacheEvict(value = "feature-flags", key = "#flagName")
    public void deleteFeatureFlag(String flagName) {
        FeatureFlag featureFlag = featureFlagRepository.findByFlagName(flagName)
                .orElseThrow(() -> new RuntimeException("Feature flag not found"));
        
        String oldValue = featureFlag.getFlagValue().toString();
        featureFlagRepository.delete(featureFlag);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.FEATURE_FLAG,
                featureFlag.getId(),
                flagName,
                oldValue,
                null,
                "Feature flag deleted",
                "system"
        );
        configurationHistoryRepository.save(history);
    }
    
    public boolean isFeatureEnabled(String flagName, UUID tenantId) {
        Optional<FeatureFlag> flag = findByFlagNameAndTenantId(flagName, tenantId);
        return flag.map(FeatureFlag::getFlagValue).orElse(false);
    }
    
    public boolean isFeatureEnabled(String flagName, UUID tenantId, FeatureFlag.Environment environment) {
        List<FeatureFlag> flags = findActiveByTenantIdAndEnvironment(tenantId, environment);
        return flags.stream()
                .filter(flag -> flag.getFlagName().equals(flagName))
                .findFirst()
                .map(FeatureFlag::getFlagValue)
                .orElse(false);
    }
    
    public Long countByTenantIdAndIsActive(UUID tenantId, Boolean isActive) {
        return featureFlagRepository.countByTenantIdAndIsActive(tenantId, isActive);
    }
    
    public Long countActiveByFlagValue(Boolean flagValue) {
        return featureFlagRepository.countActiveByFlagValue(flagValue);
    }
    
    public List<FeatureFlag> findByFlagNameContaining(String name) {
        return featureFlagRepository.findByFlagNameContaining(name);
    }
    
    public List<FeatureFlag> findActiveByFlagValue(Boolean flagValue) {
        return featureFlagRepository.findActiveByFlagValue(flagValue);
    }
    
    public boolean existsByFlagName(String flagName) {
        return featureFlagRepository.existsByFlagName(flagName);
    }
    
    public boolean existsByFlagNameAndTenantId(String flagName, UUID tenantId) {
        return featureFlagRepository.existsByFlagNameAndTenantId(flagName, tenantId);
    }
}