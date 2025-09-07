package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.entity.FraudApiToggleConfiguration;
import com.paymentengine.middleware.repository.FraudApiToggleConfigurationRepository;
import com.paymentengine.middleware.service.FraudApiToggleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of FraudApiToggleService
 */
@Service
public class FraudApiToggleServiceImpl implements FraudApiToggleService {

    private static final Logger logger = LoggerFactory.getLogger(FraudApiToggleServiceImpl.class);

    @Autowired
    private FraudApiToggleConfigurationRepository repository;

    @Override
    @Cacheable(value = "fraud-api-toggle", key = "#tenantId + ':' + (#paymentType ?: 'null') + ':' + (#localInstrumentationCode ?: 'null') + ':' + (#clearingSystemCode ?: 'null')")
    public boolean isFraudApiEnabled(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode) {
        return isFraudApiEnabled(tenantId, paymentType, localInstrumentationCode, clearingSystemCode, LocalDateTime.now());
    }

    @Override
    public boolean isFraudApiEnabled(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, LocalDateTime effectiveTime) {
        logger.debug("Checking fraud API enabled status for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}, time: {}", 
                    tenantId, paymentType, localInstrumentationCode, clearingSystemCode, effectiveTime);

        try {
            Optional<Boolean> enabled = repository.isFraudApiEnabled(
                    tenantId, paymentType, localInstrumentationCode, clearingSystemCode, effectiveTime);

            boolean result = enabled.orElse(true); // Default to enabled if no configuration found
            logger.debug("Fraud API enabled status: {} for context: {}", result, 
                        tenantId + ":" + paymentType + ":" + localInstrumentationCode + ":" + clearingSystemCode);
            
            return result;

        } catch (Exception e) {
            logger.error("Error checking fraud API enabled status: {}", e.getMessage(), e);
            return true; // Default to enabled on error
        }
    }

    @Override
    @Cacheable(value = "fraud-api-config", key = "#tenantId + ':' + (#paymentType ?: 'null') + ':' + (#localInstrumentationCode ?: 'null') + ':' + (#clearingSystemCode ?: 'null')")
    public Optional<FraudApiToggleConfiguration> getMostSpecificConfiguration(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode) {
        logger.debug("Getting most specific configuration for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}", 
                    tenantId, paymentType, localInstrumentationCode, clearingSystemCode);

        try {
            return repository.findMostSpecificConfiguration(
                    tenantId, paymentType, localInstrumentationCode, clearingSystemCode, LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error getting most specific configuration: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"fraud-api-toggle", "fraud-api-config"}, allEntries = true)
    public FraudApiToggleConfiguration createConfiguration(FraudApiToggleConfiguration configuration) {
        logger.info("Creating fraud API toggle configuration: {}", configuration);

        try {
            // Set default values
            if (configuration.getPriority() == null) {
                configuration.setPriority(100);
            }
            if (configuration.getIsActive() == null) {
                configuration.setIsActive(true);
            }
            if (configuration.getIsEnabled() == null) {
                configuration.setIsEnabled(true);
            }

            FraudApiToggleConfiguration saved = repository.save(configuration);
            logger.info("Created fraud API toggle configuration with ID: {}", saved.getId());
            
            return saved;

        } catch (Exception e) {
            logger.error("Error creating fraud API toggle configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create fraud API toggle configuration", e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"fraud-api-toggle", "fraud-api-config"}, allEntries = true)
    public FraudApiToggleConfiguration updateConfiguration(FraudApiToggleConfiguration configuration) {
        logger.info("Updating fraud API toggle configuration: {}", configuration.getId());

        try {
            if (!repository.existsById(configuration.getId())) {
                throw new RuntimeException("Configuration not found: " + configuration.getId());
            }

            FraudApiToggleConfiguration saved = repository.save(configuration);
            logger.info("Updated fraud API toggle configuration with ID: {}", saved.getId());
            
            return saved;

        } catch (Exception e) {
            logger.error("Error updating fraud API toggle configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update fraud API toggle configuration", e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"fraud-api-toggle", "fraud-api-config"}, allEntries = true)
    public void deleteConfiguration(String id) {
        logger.info("Deleting fraud API toggle configuration: {}", id);

        try {
            if (!repository.existsById(id)) {
                throw new RuntimeException("Configuration not found: " + id);
            }

            repository.deleteById(id);
            logger.info("Deleted fraud API toggle configuration with ID: {}", id);

        } catch (Exception e) {
            logger.error("Error deleting fraud API toggle configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete fraud API toggle configuration", e);
        }
    }

    @Override
    public List<FraudApiToggleConfiguration> getConfigurationsByTenant(String tenantId) {
        logger.debug("Getting configurations for tenant: {}", tenantId);

        try {
            return repository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(tenantId);
        } catch (Exception e) {
            logger.error("Error getting configurations by tenant: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<FraudApiToggleConfiguration> getAllActiveConfigurations() {
        logger.debug("Getting all active configurations");

        try {
            return repository.findAllActiveConfigurations();
        } catch (Exception e) {
            logger.error("Error getting all active configurations: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<FraudApiToggleConfiguration> getCurrentlyEffectiveConfigurations() {
        logger.debug("Getting currently effective configurations");

        try {
            return repository.findCurrentlyEffectiveConfigurations(LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error getting currently effective configurations: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<FraudApiToggleConfiguration> getFutureEffectiveConfigurations() {
        logger.debug("Getting future effective configurations");

        try {
            return repository.findFutureEffectiveConfigurations(LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error getting future effective configurations: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<FraudApiToggleConfiguration> getExpiredConfigurations() {
        logger.debug("Getting expired configurations");

        try {
            return repository.findExpiredConfigurations(LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error getting expired configurations: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"fraud-api-toggle", "fraud-api-config"}, allEntries = true)
    public FraudApiToggleConfiguration enableFraudApi(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, String reason, String createdBy) {
        logger.info("Enabling fraud API for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}, reason: {}", 
                   tenantId, paymentType, localInstrumentationCode, clearingSystemCode, reason);

        FraudApiToggleConfiguration configuration = new FraudApiToggleConfiguration(
                tenantId, paymentType, localInstrumentationCode, clearingSystemCode, true);
        configuration.setEnabledReason(reason);
        configuration.setCreatedBy(createdBy);
        configuration.setUpdatedBy(createdBy);

        return createConfiguration(configuration);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"fraud-api-toggle", "fraud-api-config"}, allEntries = true)
    public FraudApiToggleConfiguration disableFraudApi(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, String reason, String createdBy) {
        logger.info("Disabling fraud API for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}, reason: {}", 
                   tenantId, paymentType, localInstrumentationCode, clearingSystemCode, reason);

        FraudApiToggleConfiguration configuration = new FraudApiToggleConfiguration(
                tenantId, paymentType, localInstrumentationCode, clearingSystemCode, false);
        configuration.setDisabledReason(reason);
        configuration.setCreatedBy(createdBy);
        configuration.setUpdatedBy(createdBy);

        return createConfiguration(configuration);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"fraud-api-toggle", "fraud-api-config"}, allEntries = true)
    public FraudApiToggleConfiguration toggleFraudApi(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, String reason, String createdBy) {
        logger.info("Toggling fraud API for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}", 
                   tenantId, paymentType, localInstrumentationCode, clearingSystemCode);

        // Get current status
        boolean currentlyEnabled = isFraudApiEnabled(tenantId, paymentType, localInstrumentationCode, clearingSystemCode);
        boolean newStatus = !currentlyEnabled;

        if (newStatus) {
            return enableFraudApi(tenantId, paymentType, localInstrumentationCode, clearingSystemCode, reason, createdBy);
        } else {
            return disableFraudApi(tenantId, paymentType, localInstrumentationCode, clearingSystemCode, reason, createdBy);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"fraud-api-toggle", "fraud-api-config"}, allEntries = true)
    public FraudApiToggleConfiguration scheduleConfiguration(FraudApiToggleConfiguration configuration) {
        logger.info("Scheduling fraud API toggle configuration: {}", configuration);

        try {
            // Validate scheduled times
            if (configuration.getEffectiveFrom() != null && configuration.getEffectiveUntil() != null) {
                if (configuration.getEffectiveFrom().isAfter(configuration.getEffectiveUntil())) {
                    throw new IllegalArgumentException("Effective from time cannot be after effective until time");
                }
            }

            return createConfiguration(configuration);

        } catch (Exception e) {
            logger.error("Error scheduling configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to schedule configuration", e);
        }
    }

    @Override
    public FraudApiToggleStatistics getStatistics() {
        logger.debug("Getting fraud API toggle statistics");

        try {
            List<FraudApiToggleConfiguration> allConfigs = repository.findAll();
            List<FraudApiToggleConfiguration> activeConfigs = repository.findAllActiveConfigurations();
            List<FraudApiToggleConfiguration> effectiveConfigs = repository.findCurrentlyEffectiveConfigurations(LocalDateTime.now());
            List<FraudApiToggleConfiguration> futureConfigs = repository.findFutureEffectiveConfigurations(LocalDateTime.now());
            List<FraudApiToggleConfiguration> expiredConfigs = repository.findExpiredConfigurations(LocalDateTime.now());

            FraudApiToggleStatistics stats = new FraudApiToggleStatistics();
            stats.setTotalConfigurations(allConfigs.size());
            stats.setActiveConfigurations(activeConfigs.size());
            stats.setCurrentlyEffectiveConfigurations(effectiveConfigs.size());
            stats.setFutureEffectiveConfigurations(futureConfigs.size());
            stats.setExpiredConfigurations(expiredConfigs.size());

            // Count by enabled/disabled status
            long enabledCount = activeConfigs.stream().mapToLong(config -> config.getIsEnabled() ? 1 : 0).sum();
            long disabledCount = activeConfigs.size() - enabledCount;
            stats.setEnabledConfigurations(enabledCount);
            stats.setDisabledConfigurations(disabledCount);

            // Count by configuration level
            long tenantLevel = activeConfigs.stream().mapToLong(config -> 
                config.getPaymentType() == null && config.getLocalInstrumentationCode() == null && config.getClearingSystemCode() == null ? 1 : 0).sum();
            long paymentTypeLevel = activeConfigs.stream().mapToLong(config -> 
                config.getPaymentType() != null && config.getLocalInstrumentationCode() == null && config.getClearingSystemCode() == null ? 1 : 0).sum();
            long localInstrumentLevel = activeConfigs.stream().mapToLong(config -> 
                config.getLocalInstrumentationCode() != null && config.getClearingSystemCode() == null ? 1 : 0).sum();
            long clearingSystemLevel = activeConfigs.stream().mapToLong(config -> 
                config.getClearingSystemCode() != null ? 1 : 0).sum();

            stats.setTenantLevelConfigurations(tenantLevel);
            stats.setPaymentTypeLevelConfigurations(paymentTypeLevel);
            stats.setLocalInstrumentLevelConfigurations(localInstrumentLevel);
            stats.setClearingSystemLevelConfigurations(clearingSystemLevel);

            return stats;

        } catch (Exception e) {
            logger.error("Error getting statistics: {}", e.getMessage(), e);
            return new FraudApiToggleStatistics();
        }
    }

    @Override
    @CacheEvict(value = {"fraud-api-toggle", "fraud-api-config"}, allEntries = true)
    public void refreshCache() {
        logger.info("Refreshing fraud API toggle cache");
        // Cache eviction is handled by the @CacheEvict annotation
    }
}