package com.paymentengine.middleware.controller;

import com.paymentengine.middleware.entity.FraudApiToggleConfiguration;
import com.paymentengine.middleware.service.FraudApiToggleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing fraud API toggle configurations
 */
@RestController
@RequestMapping("/api/v1/fraud-api-toggle")
@CrossOrigin(origins = "*")
public class FraudApiToggleController {

    private static final Logger logger = LoggerFactory.getLogger(FraudApiToggleController.class);

    @Autowired
    private FraudApiToggleService fraudApiToggleService;

    /**
     * Check if fraud API is enabled for a specific context
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkFraudApiEnabled(
            @RequestParam String tenantId,
            @RequestParam(required = false) String paymentType,
            @RequestParam(required = false) String localInstrumentationCode,
            @RequestParam(required = false) String clearingSystemCode) {

        logger.info("Checking fraud API enabled status for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}", 
                   tenantId, paymentType, localInstrumentationCode, clearingSystemCode);

        try {
            boolean isEnabled = fraudApiToggleService.isFraudApiEnabled(
                    tenantId, paymentType, localInstrumentationCode, clearingSystemCode);

            return ResponseEntity.ok(Map.of(
                    "tenantId", tenantId,
                    "paymentType", paymentType != null ? paymentType : "ALL",
                    "localInstrumentationCode", localInstrumentationCode != null ? localInstrumentationCode : "ALL",
                    "clearingSystemCode", clearingSystemCode != null ? clearingSystemCode : "ALL",
                    "isEnabled", isEnabled,
                    "checkedAt", LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Error checking fraud API enabled status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check fraud API enabled status: " + e.getMessage()));
        }
    }

    /**
     * Get all configurations for a tenant
     */
    @GetMapping("/configurations/tenant/{tenantId}")
    public ResponseEntity<List<FraudApiToggleConfiguration>> getConfigurationsByTenant(@PathVariable String tenantId) {
        logger.info("Getting fraud API toggle configurations for tenant: {}", tenantId);

        try {
            List<FraudApiToggleConfiguration> configurations = fraudApiToggleService.getConfigurationsByTenant(tenantId);
            return ResponseEntity.ok(configurations);

        } catch (Exception e) {
            logger.error("Error getting configurations by tenant: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all active configurations
     */
    @GetMapping("/configurations")
    public ResponseEntity<List<FraudApiToggleConfiguration>> getAllConfigurations() {
        logger.info("Getting all fraud API toggle configurations");

        try {
            List<FraudApiToggleConfiguration> configurations = fraudApiToggleService.getAllActiveConfigurations();
            return ResponseEntity.ok(configurations);

        } catch (Exception e) {
            logger.error("Error getting all configurations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get currently effective configurations
     */
    @GetMapping("/configurations/effective")
    public ResponseEntity<List<FraudApiToggleConfiguration>> getCurrentlyEffectiveConfigurations() {
        logger.info("Getting currently effective fraud API toggle configurations");

        try {
            List<FraudApiToggleConfiguration> configurations = fraudApiToggleService.getCurrentlyEffectiveConfigurations();
            return ResponseEntity.ok(configurations);

        } catch (Exception e) {
            logger.error("Error getting currently effective configurations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get future effective configurations
     */
    @GetMapping("/configurations/future")
    public ResponseEntity<List<FraudApiToggleConfiguration>> getFutureEffectiveConfigurations() {
        logger.info("Getting future effective fraud API toggle configurations");

        try {
            List<FraudApiToggleConfiguration> configurations = fraudApiToggleService.getFutureEffectiveConfigurations();
            return ResponseEntity.ok(configurations);

        } catch (Exception e) {
            logger.error("Error getting future effective configurations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get expired configurations
     */
    @GetMapping("/configurations/expired")
    public ResponseEntity<List<FraudApiToggleConfiguration>> getExpiredConfigurations() {
        logger.info("Getting expired fraud API toggle configurations");

        try {
            List<FraudApiToggleConfiguration> configurations = fraudApiToggleService.getExpiredConfigurations();
            return ResponseEntity.ok(configurations);

        } catch (Exception e) {
            logger.error("Error getting expired configurations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new configuration
     */
    @PostMapping("/configurations")
    public ResponseEntity<FraudApiToggleConfiguration> createConfiguration(@Valid @RequestBody FraudApiToggleConfiguration configuration) {
        logger.info("Creating fraud API toggle configuration: {}", configuration);

        try {
            FraudApiToggleConfiguration created = fraudApiToggleService.createConfiguration(configuration);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (Exception e) {
            logger.error("Error creating configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing configuration
     */
    @PutMapping("/configurations/{id}")
    public ResponseEntity<FraudApiToggleConfiguration> updateConfiguration(
            @PathVariable String id, 
            @Valid @RequestBody FraudApiToggleConfiguration configuration) {
        logger.info("Updating fraud API toggle configuration: {}", id);

        try {
            configuration.setId(id);
            FraudApiToggleConfiguration updated = fraudApiToggleService.updateConfiguration(configuration);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            logger.error("Error updating configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a configuration
     */
    @DeleteMapping("/configurations/{id}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable String id) {
        logger.info("Deleting fraud API toggle configuration: {}", id);

        try {
            fraudApiToggleService.deleteConfiguration(id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Error deleting configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Enable fraud API for a specific context
     */
    @PostMapping("/enable")
    public ResponseEntity<FraudApiToggleConfiguration> enableFraudApi(@RequestBody Map<String, Object> request) {
        logger.info("Enabling fraud API: {}", request);

        try {
            String tenantId = (String) request.get("tenantId");
            String paymentType = (String) request.get("paymentType");
            String localInstrumentationCode = (String) request.get("localInstrumentationCode");
            String clearingSystemCode = (String) request.get("clearingSystemCode");
            String reason = (String) request.get("reason");
            String createdBy = (String) request.getOrDefault("createdBy", "system");

            FraudApiToggleConfiguration configuration = fraudApiToggleService.enableFraudApi(
                    tenantId, paymentType, localInstrumentationCode, clearingSystemCode, reason, createdBy);

            return ResponseEntity.status(HttpStatus.CREATED).body(configuration);

        } catch (Exception e) {
            logger.error("Error enabling fraud API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Disable fraud API for a specific context
     */
    @PostMapping("/disable")
    public ResponseEntity<FraudApiToggleConfiguration> disableFraudApi(@RequestBody Map<String, Object> request) {
        logger.info("Disabling fraud API: {}", request);

        try {
            String tenantId = (String) request.get("tenantId");
            String paymentType = (String) request.get("paymentType");
            String localInstrumentationCode = (String) request.get("localInstrumentationCode");
            String clearingSystemCode = (String) request.get("clearingSystemCode");
            String reason = (String) request.get("reason");
            String createdBy = (String) request.getOrDefault("createdBy", "system");

            FraudApiToggleConfiguration configuration = fraudApiToggleService.disableFraudApi(
                    tenantId, paymentType, localInstrumentationCode, clearingSystemCode, reason, createdBy);

            return ResponseEntity.status(HttpStatus.CREATED).body(configuration);

        } catch (Exception e) {
            logger.error("Error disabling fraud API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Toggle fraud API for a specific context
     */
    @PostMapping("/toggle")
    public ResponseEntity<FraudApiToggleConfiguration> toggleFraudApi(@RequestBody Map<String, Object> request) {
        logger.info("Toggling fraud API: {}", request);

        try {
            String tenantId = (String) request.get("tenantId");
            String paymentType = (String) request.get("paymentType");
            String localInstrumentationCode = (String) request.get("localInstrumentationCode");
            String clearingSystemCode = (String) request.get("clearingSystemCode");
            String reason = (String) request.get("reason");
            String createdBy = (String) request.getOrDefault("createdBy", "system");

            FraudApiToggleConfiguration configuration = fraudApiToggleService.toggleFraudApi(
                    tenantId, paymentType, localInstrumentationCode, clearingSystemCode, reason, createdBy);

            return ResponseEntity.status(HttpStatus.CREATED).body(configuration);

        } catch (Exception e) {
            logger.error("Error toggling fraud API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Schedule a configuration for future effective time
     */
    @PostMapping("/configurations/schedule")
    public ResponseEntity<FraudApiToggleConfiguration> scheduleConfiguration(@Valid @RequestBody FraudApiToggleConfiguration configuration) {
        logger.info("Scheduling fraud API toggle configuration: {}", configuration);

        try {
            FraudApiToggleConfiguration scheduled = fraudApiToggleService.scheduleConfiguration(configuration);
            return ResponseEntity.status(HttpStatus.CREATED).body(scheduled);

        } catch (Exception e) {
            logger.error("Error scheduling configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get configuration statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<FraudApiToggleService.FraudApiToggleStatistics> getStatistics() {
        logger.info("Getting fraud API toggle statistics");

        try {
            FraudApiToggleService.FraudApiToggleStatistics statistics = fraudApiToggleService.getStatistics();
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            logger.error("Error getting statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Refresh cache
     */
    @PostMapping("/cache/refresh")
    public ResponseEntity<Map<String, String>> refreshCache() {
        logger.info("Refreshing fraud API toggle cache");

        try {
            fraudApiToggleService.refreshCache();
            return ResponseEntity.ok(Map.of("message", "Cache refreshed successfully", "timestamp", LocalDateTime.now().toString()));

        } catch (Exception e) {
            logger.error("Error refreshing cache: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to refresh cache: " + e.getMessage()));
        }
    }
}