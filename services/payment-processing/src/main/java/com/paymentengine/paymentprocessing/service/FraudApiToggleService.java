package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.entity.FraudApiToggleConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing fraud API enable/disable configurations
 */
public interface FraudApiToggleService {

    /**
     * Check if fraud API is enabled for a specific context
     * @param tenantId Tenant identifier
     * @param paymentType Payment type (optional)
     * @param localInstrumentationCode Local instrumentation code (optional)
     * @param clearingSystemCode Clearing system code (optional)
     * @return true if fraud API is enabled, false otherwise
     */
    boolean isFraudApiEnabled(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode);

    /**
     * Check if fraud API is enabled for a specific context at a specific time
     * @param tenantId Tenant identifier
     * @param paymentType Payment type (optional)
     * @param localInstrumentationCode Local instrumentation code (optional)
     * @param clearingSystemCode Clearing system code (optional)
     * @param effectiveTime Time to check effectiveness
     * @return true if fraud API is enabled, false otherwise
     */
    boolean isFraudApiEnabled(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, LocalDateTime effectiveTime);

    /**
     * Get the most specific configuration for a given context
     * @param tenantId Tenant identifier
     * @param paymentType Payment type (optional)
     * @param localInstrumentationCode Local instrumentation code (optional)
     * @param clearingSystemCode Clearing system code (optional)
     * @return Optional containing the most specific configuration
     */
    Optional<FraudApiToggleConfiguration> getMostSpecificConfiguration(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode);

    /**
     * Create a new fraud API toggle configuration
     * @param configuration Configuration to create
     * @return Created configuration
     */
    FraudApiToggleConfiguration createConfiguration(FraudApiToggleConfiguration configuration);

    /**
     * Update an existing fraud API toggle configuration
     * @param configuration Configuration to update
     * @return Updated configuration
     */
    FraudApiToggleConfiguration updateConfiguration(FraudApiToggleConfiguration configuration);

    /**
     * Delete a fraud API toggle configuration
     * @param id Configuration ID to delete
     */
    void deleteConfiguration(String id);

    /**
     * Get all configurations for a tenant
     * @param tenantId Tenant identifier
     * @return List of configurations
     */
    List<FraudApiToggleConfiguration> getConfigurationsByTenant(String tenantId);

    /**
     * Get all active configurations
     * @return List of active configurations
     */
    List<FraudApiToggleConfiguration> getAllActiveConfigurations();

    /**
     * Get configurations that are currently effective
     * @return List of currently effective configurations
     */
    List<FraudApiToggleConfiguration> getCurrentlyEffectiveConfigurations();

    /**
     * Get configurations that will become effective in the future
     * @return List of future effective configurations
     */
    List<FraudApiToggleConfiguration> getFutureEffectiveConfigurations();

    /**
     * Get configurations that have expired
     * @return List of expired configurations
     */
    List<FraudApiToggleConfiguration> getExpiredConfigurations();

    /**
     * Enable fraud API for a specific context
     * @param tenantId Tenant identifier
     * @param paymentType Payment type (optional)
     * @param localInstrumentationCode Local instrumentation code (optional)
     * @param clearingSystemCode Clearing system code (optional)
     * @param reason Reason for enabling
     * @param createdBy User who created the configuration
     * @return Created configuration
     */
    FraudApiToggleConfiguration enableFraudApi(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, String reason, String createdBy);

    /**
     * Disable fraud API for a specific context
     * @param tenantId Tenant identifier
     * @param paymentType Payment type (optional)
     * @param localInstrumentationCode Local instrumentation code (optional)
     * @param clearingSystemCode Clearing system code (optional)
     * @param reason Reason for disabling
     * @param createdBy User who created the configuration
     * @return Created configuration
     */
    FraudApiToggleConfiguration disableFraudApi(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, String reason, String createdBy);

    /**
     * Toggle fraud API for a specific context
     * @param tenantId Tenant identifier
     * @param paymentType Payment type (optional)
     * @param localInstrumentationCode Local instrumentation code (optional)
     * @param clearingSystemCode Clearing system code (optional)
     * @param reason Reason for toggling
     * @param createdBy User who created the configuration
     * @return Created configuration
     */
    FraudApiToggleConfiguration toggleFraudApi(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, String reason, String createdBy);

    /**
     * Schedule fraud API enable/disable for a specific time
     * @param configuration Configuration with scheduled times
     * @return Created configuration
     */
    FraudApiToggleConfiguration scheduleConfiguration(FraudApiToggleConfiguration configuration);

    /**
     * Get configuration statistics
     * @return Statistics about configurations
     */
    FraudApiToggleStatistics getStatistics();

    /**
     * Refresh cache (for dynamic configuration updates)
     */
    void refreshCache();

    /**
     * Statistics class for fraud API toggle configurations
     */
    class FraudApiToggleStatistics {
        private long totalConfigurations;
        private long activeConfigurations;
        private long enabledConfigurations;
        private long disabledConfigurations;
        private long tenantLevelConfigurations;
        private long paymentTypeLevelConfigurations;
        private long localInstrumentLevelConfigurations;
        private long clearingSystemLevelConfigurations;
        private long currentlyEffectiveConfigurations;
        private long futureEffectiveConfigurations;
        private long expiredConfigurations;

        // Constructors
        public FraudApiToggleStatistics() {}

        public FraudApiToggleStatistics(long totalConfigurations, long activeConfigurations, long enabledConfigurations, long disabledConfigurations) {
            this.totalConfigurations = totalConfigurations;
            this.activeConfigurations = activeConfigurations;
            this.enabledConfigurations = enabledConfigurations;
            this.disabledConfigurations = disabledConfigurations;
        }

        // Getters and Setters
        public long getTotalConfigurations() { return totalConfigurations; }
        public void setTotalConfigurations(long totalConfigurations) { this.totalConfigurations = totalConfigurations; }

        public long getActiveConfigurations() { return activeConfigurations; }
        public void setActiveConfigurations(long activeConfigurations) { this.activeConfigurations = activeConfigurations; }

        public long getEnabledConfigurations() { return enabledConfigurations; }
        public void setEnabledConfigurations(long enabledConfigurations) { this.enabledConfigurations = enabledConfigurations; }

        public long getDisabledConfigurations() { return disabledConfigurations; }
        public void setDisabledConfigurations(long disabledConfigurations) { this.disabledConfigurations = disabledConfigurations; }

        public long getTenantLevelConfigurations() { return tenantLevelConfigurations; }
        public void setTenantLevelConfigurations(long tenantLevelConfigurations) { this.tenantLevelConfigurations = tenantLevelConfigurations; }

        public long getPaymentTypeLevelConfigurations() { return paymentTypeLevelConfigurations; }
        public void setPaymentTypeLevelConfigurations(long paymentTypeLevelConfigurations) { this.paymentTypeLevelConfigurations = paymentTypeLevelConfigurations; }

        public long getLocalInstrumentLevelConfigurations() { return localInstrumentLevelConfigurations; }
        public void setLocalInstrumentLevelConfigurations(long localInstrumentLevelConfigurations) { this.localInstrumentLevelConfigurations = localInstrumentLevelConfigurations; }

        public long getClearingSystemLevelConfigurations() { return clearingSystemLevelConfigurations; }
        public void setClearingSystemLevelConfigurations(long clearingSystemLevelConfigurations) { this.clearingSystemLevelConfigurations = clearingSystemLevelConfigurations; }

        public long getCurrentlyEffectiveConfigurations() { return currentlyEffectiveConfigurations; }
        public void setCurrentlyEffectiveConfigurations(long currentlyEffectiveConfigurations) { this.currentlyEffectiveConfigurations = currentlyEffectiveConfigurations; }

        public long getFutureEffectiveConfigurations() { return futureEffectiveConfigurations; }
        public void setFutureEffectiveConfigurations(long futureEffectiveConfigurations) { this.futureEffectiveConfigurations = futureEffectiveConfigurations; }

        public long getExpiredConfigurations() { return expiredConfigurations; }
        public void setExpiredConfigurations(long expiredConfigurations) { this.expiredConfigurations = expiredConfigurations; }
    }
}