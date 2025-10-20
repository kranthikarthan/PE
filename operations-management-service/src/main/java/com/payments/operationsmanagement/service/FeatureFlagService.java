package com.payments.operationsmanagement.service;

import com.payments.operationsmanagement.dto.FeatureFlagDto;
import com.payments.operationsmanagement.entity.OperationsAuditLogEntity;
import com.payments.operationsmanagement.repository.OperationsAuditLogRepository;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Feature Flag Service
 * 
 * Manages feature flags using Unleash integration.
 * Provides functionality to toggle flags, set rollout percentages, and track usage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {

    private final Unleash unleash;
    private final OperationsAuditLogRepository auditLogRepository;

    /**
     * Get all feature flags
     */
    public List<FeatureFlagDto> getAllFeatureFlags() {
        log.info("Getting all feature flags");
        
        // This would integrate with Unleash API to get all flags
        // For now, return mock data
        return List.of(
            FeatureFlagDto.builder()
                .name("new-fraud-engine")
                .description("Enable new fraud detection engine")
                .enabled(true)
                .rolloutPercentage(50)
                .strategy("gradualRollout")
                .variants(new String[]{"enabled", "disabled"})
                .createdAt(Instant.now().minusSeconds(86400))
                .updatedAt(Instant.now())
                .createdBy("admin")
                .updatedBy("admin")
                .build(),
            FeatureFlagDto.builder()
                .name("enhanced-reporting")
                .description("Enable enhanced reporting features")
                .enabled(false)
                .rolloutPercentage(0)
                .strategy("userWithId")
                .variants(new String[]{"enabled", "disabled"})
                .createdAt(Instant.now().minusSeconds(172800))
                .updatedAt(Instant.now().minusSeconds(3600))
                .createdBy("admin")
                .updatedBy("ops-admin")
                .build()
        );
    }

    /**
     * Toggle a feature flag
     */
    public FeatureFlagDto toggleFeatureFlag(String flagName, 
                                          OperationsManagementController.FeatureFlagToggleRequest request, 
                                          String userId) {
        log.info("Toggling feature flag: {} to {} by user: {}", flagName, request.getEnabled(), userId);
        
        // This would call Unleash API to toggle the flag
        // For now, return mock response
        
        FeatureFlagDto updatedFlag = FeatureFlagDto.builder()
            .name(flagName)
            .description("Feature flag description")
            .enabled(request.getEnabled())
            .rolloutPercentage(request.getRolloutPercentage() != null ? request.getRolloutPercentage() : 0)
            .strategy("gradualRollout")
            .variants(new String[]{"enabled", "disabled"})
            .createdAt(Instant.now().minusSeconds(86400))
            .updatedAt(Instant.now())
            .createdBy("admin")
            .updatedBy(userId)
            .build();

        // Log audit trail
        logAuditAction(
            OperationsAuditLogEntity.ActionType.FEATURE_FLAG_TOGGLE,
            OperationsAuditLogEntity.EntityType.FEATURE_FLAG,
            flagName,
            Map.of(
                "enabled", request.getEnabled(),
                "rolloutPercentage", request.getRolloutPercentage()
            ),
            userId
        );

        return updatedFlag;
    }

    /**
     * Set rollout percentage for a feature flag
     */
    public FeatureFlagDto setFeatureFlagRollout(String flagName, 
                                              OperationsManagementController.FeatureFlagRolloutRequest request, 
                                              String userId) {
        log.info("Setting rollout for feature flag: {} to {}% by user: {}", 
                flagName, request.getRolloutPercentage(), userId);
        
        // This would call Unleash API to set rollout percentage
        // For now, return mock response
        
        FeatureFlagDto updatedFlag = FeatureFlagDto.builder()
            .name(flagName)
            .description("Feature flag description")
            .enabled(true)
            .rolloutPercentage(request.getRolloutPercentage())
            .strategy("gradualRollout")
            .variants(new String[]{"enabled", "disabled"})
            .createdAt(Instant.now().minusSeconds(86400))
            .updatedAt(Instant.now())
            .createdBy("admin")
            .updatedBy(userId)
            .build();

        // Log audit trail
        logAuditAction(
            OperationsAuditLogEntity.ActionType.FEATURE_FLAG_ROLLOUT,
            OperationsAuditLogEntity.EntityType.FEATURE_FLAG,
            flagName,
            Map.of("rolloutPercentage", request.getRolloutPercentage()),
            userId
        );

        return updatedFlag;
    }

    /**
     * Check if a feature flag is enabled
     */
    public boolean isFeatureEnabled(String flagName, String userId) {
        log.debug("Checking if feature flag {} is enabled for user: {}", flagName, userId);
        
        UnleashContext context = UnleashContext.builder()
            .userId(userId)
            .build();
        
        return unleash.isEnabled(flagName, context);
    }

    /**
     * Get feature flag variant
     */
    public String getFeatureVariant(String flagName, String userId) {
        log.debug("Getting variant for feature flag {} for user: {}", flagName, userId);
        
        UnleashContext context = UnleashContext.builder()
            .userId(userId)
            .build();
        
        return unleash.getVariant(flagName, context).getName();
    }

    /**
     * Log audit action
     */
    private void logAuditAction(OperationsAuditLogEntity.ActionType actionType,
                              OperationsAuditLogEntity.EntityType entityType,
                              String entityId,
                              Map<String, Object> actionDetails,
                              String userId) {
        try {
            OperationsAuditLogEntity auditLog = OperationsAuditLogEntity.builder()
                .auditId(UUID.randomUUID().toString())
                .tenantId("default") // Would get from context
                .userId(userId)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .actionDetails(convertToJson(actionDetails))
                .ipAddress("127.0.0.1") // Would get from request
                .userAgent("Operations Management Service")
                .performedAt(Instant.now())
                .build();

            auditLogRepository.save(auditLog);
            log.info("Logged audit action: {} for entity: {}", actionType, entityId);

        } catch (Exception e) {
            log.error("Failed to log audit action: {} for entity: {}", actionType, entityId, e);
        }
    }

    /**
     * Convert map to JSON string
     */
    private String convertToJson(Map<String, Object> map) {
        // Simple JSON conversion - in production, use Jackson or similar
        return map.toString();
    }
}
