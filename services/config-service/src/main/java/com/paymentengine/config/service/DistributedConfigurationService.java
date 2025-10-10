package com.paymentengine.config.service;

import com.paymentengine.config.entity.TenantConfiguration;
import com.paymentengine.config.entity.FeatureFlag;
import com.paymentengine.config.entity.ConfigurationHistory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DistributedConfigurationService {
    
    // Configuration Management
    TenantConfiguration createConfiguration(TenantConfiguration configuration);
    
    TenantConfiguration updateConfiguration(TenantConfiguration configuration);
    
    void deleteConfiguration(String tenantId, String configurationKey);
    
    Optional<TenantConfiguration> getConfiguration(String tenantId, String configurationKey);
    
    List<TenantConfiguration> getAllConfigurations(String tenantId);
    
    List<TenantConfiguration> getConfigurationsByType(String tenantId, String configurationType);
    
    // Feature Flag Management
    FeatureFlag createFeatureFlag(FeatureFlag featureFlag);
    
    FeatureFlag updateFeatureFlag(FeatureFlag featureFlag);
    
    void deleteFeatureFlag(String name);
    
    Optional<FeatureFlag> getFeatureFlag(String name);
    
    List<FeatureFlag> getAllFeatureFlags();
    
    List<FeatureFlag> getFeatureFlagsByTenant(String tenantId);
    
    List<FeatureFlag> getFeatureFlagsByEnvironment(String environment);
    
    // Configuration Distribution
    void distributeConfiguration(String tenantId, String configurationKey);
    
    void distributeAllConfigurations(String tenantId);
    
    void distributeFeatureFlag(String featureFlagName);
    
    void distributeAllFeatureFlags();
    
    // Configuration Validation
    boolean validateConfiguration(TenantConfiguration configuration);
    
    boolean validateFeatureFlag(FeatureFlag featureFlag);
    
    List<String> validateAllConfigurations(String tenantId);
    
    // Configuration History
    List<ConfigurationHistory> getConfigurationHistory(String tenantId, String configurationKey);
    
    List<ConfigurationHistory> getConfigurationHistoryByTenant(String tenantId);
    
    void rollbackConfiguration(String tenantId, String configurationKey, String version);
    
    // Configuration Templates
    TenantConfiguration createConfigurationFromTemplate(String tenantId, String templateName, Map<String, String> parameters);
    
    List<String> getAvailableTemplates();
    
    // Configuration Dependencies
    List<TenantConfiguration> getConfigurationDependencies(String tenantId, String configurationKey);
    
    void updateConfigurationDependencies(String tenantId, String configurationKey, List<String> dependencies);
    
    // Configuration Schemas
    boolean validateConfigurationSchema(String configurationType, String configurationValue);
    
    String getConfigurationSchema(String configurationType);
    
    // Configuration Deployment
    void deployConfiguration(String tenantId, String configurationKey, String environment);
    
    void deployAllConfigurations(String tenantId, String environment);
    
    void approveConfiguration(String tenantId, String configurationKey, String approverId);
    
    // Configuration Monitoring
    Map<String, Object> getConfigurationMetrics(String tenantId);
    
    List<String> getConfigurationAlerts(String tenantId);
    
    void setConfigurationAlert(String tenantId, String configurationKey, String alertType, String threshold);
    
    // Configuration Backup and Restore
    void backupConfigurations(String tenantId);
    
    void restoreConfigurations(String tenantId, String backupId);
    
    List<String> getAvailableBackups(String tenantId);
    
    // Configuration Migration
    void migrateConfigurations(String fromTenantId, String toTenantId);
    
    void migrateFeatureFlags(String fromEnvironment, String toEnvironment);
    
    // Configuration Security
    void encryptConfiguration(String tenantId, String configurationKey);
    
    void decryptConfiguration(String tenantId, String configurationKey);
    
    boolean isConfigurationEncrypted(String tenantId, String configurationKey);
    
    // Configuration Audit
    List<ConfigurationHistory> getConfigurationAuditTrail(String tenantId, String configurationKey);
    
    void logConfigurationAccess(String tenantId, String configurationKey, String userId, String action);
    
    // Configuration Performance
    void cacheConfiguration(String tenantId, String configurationKey);
    
    void invalidateConfigurationCache(String tenantId, String configurationKey);
    
    void invalidateAllConfigurationCache(String tenantId);
    
    // Configuration Notifications
    void notifyConfigurationChange(String tenantId, String configurationKey, String changeType);
    
    void notifyFeatureFlagChange(String featureFlagName, String changeType);
    
    // Configuration Health Check
    boolean isConfigurationHealthy(String tenantId);
    
    Map<String, Object> getConfigurationHealthStatus(String tenantId);
    
    // Configuration Versioning
    String getConfigurationVersion(String tenantId, String configurationKey);
    
    List<String> getConfigurationVersions(String tenantId, String configurationKey);
    
    void tagConfigurationVersion(String tenantId, String configurationKey, String version, String tag);
    
    // Configuration Export/Import
    String exportConfigurations(String tenantId, String format);
    
    void importConfigurations(String tenantId, String configurationData, String format);
    
    // Configuration Analytics
    Map<String, Object> getConfigurationAnalytics(String tenantId);
    
    List<String> getConfigurationTrends(String tenantId, String configurationKey);
    
    // Configuration Compliance
    boolean isConfigurationCompliant(String tenantId, String configurationKey);
    
    List<String> getConfigurationComplianceIssues(String tenantId);
    
    void markConfigurationCompliant(String tenantId, String configurationKey);
}