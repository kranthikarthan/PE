package com.payments.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for configuration management
 */
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "payments.config.management.enabled", havingValue = "true", matchIfMissing = true)
public class ConfigurationController {

    private final ConfigurationManager configurationManager;
    private final SecretManager secretManager;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    /**
     * Get current configuration
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentConfiguration() {
        log.debug("Getting current configuration for profile: {}", activeProfile);
        return configurationManager.getConfigurationSummary();
    }

    /**
     * Get service configuration
     */
    @GetMapping("/service/{serviceName}")
    public Map<String, Object> getServiceConfiguration(@PathVariable String serviceName) {
        log.debug("Getting configuration for service: {}", serviceName);
        return configurationManager.getServiceConfiguration(serviceName);
    }

    /**
     * Get environment configuration
     */
    @GetMapping("/environment")
    public Map<String, Object> getEnvironmentConfiguration() {
        log.debug("Getting environment configuration");
        return configurationManager.getEnvironmentConfiguration();
    }

    /**
     * Get database configuration
     */
    @GetMapping("/database")
    public Map<String, Object> getDatabaseConfiguration() {
        log.debug("Getting database configuration");
        return configurationManager.getDatabaseConfiguration();
    }

    /**
     * Get cache configuration
     */
    @GetMapping("/cache")
    public Map<String, Object> getCacheConfiguration() {
        log.debug("Getting cache configuration");
        return configurationManager.getCacheConfiguration();
    }

    /**
     * Get messaging configuration
     */
    @GetMapping("/messaging")
    public Map<String, Object> getMessagingConfiguration() {
        log.debug("Getting messaging configuration");
        return configurationManager.getMessagingConfiguration();
    }

    /**
     * Get security configuration
     */
    @GetMapping("/security")
    public Map<String, Object> getSecurityConfiguration() {
        log.debug("Getting security configuration");
        return configurationManager.getSecurityConfiguration();
    }

    /**
     * Get telemetry configuration
     */
    @GetMapping("/telemetry")
    public Map<String, Object> getTelemetryConfiguration() {
        log.debug("Getting telemetry configuration");
        return configurationManager.getTelemetryConfiguration();
    }

    /**
     * Get secret information (without values)
     */
    @GetMapping("/secrets")
    public Map<String, Object> getSecretInfo() {
        log.debug("Getting secret information");
        return Map.of(
            "cacheStats", secretManager.getCacheStats(),
            "hasDatabasePassword", secretManager.hasSecret("DATABASE_PASSWORD"),
            "hasJwtSecret", secretManager.hasSecret("JWT_SECRET"),
            "hasOAuth2Secret", secretManager.hasSecret("OAUTH2_CLIENT_SECRET"),
            "hasRedisPassword", secretManager.hasSecret("REDIS_PASSWORD"),
            "hasKafkaConfig", secretManager.hasSecret("KAFKA_BOOTSTRAP_SERVERS")
        );
    }

    /**
     * Clear secret cache
     */
    @PostMapping("/secrets/clear-cache")
    public Map<String, Object> clearSecretCache() {
        log.info("Clearing secret cache");
        secretManager.clearCache();
        return Map.of(
            "message", "Secret cache cleared",
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * Get configuration health
     */
    @GetMapping("/health")
    public Map<String, Object> getConfigurationHealth() {
        log.debug("Getting configuration health");
        
        boolean databaseConfigured = secretManager.hasSecret("DATABASE_URL");
        boolean cacheConfigured = secretManager.hasSecret("REDIS_HOST");
        boolean messagingConfigured = secretManager.hasSecret("KAFKA_BOOTSTRAP_SERVERS");
        boolean securityConfigured = secretManager.hasSecret("JWT_SECRET");
        
        boolean healthy = databaseConfigured && cacheConfigured && messagingConfigured && securityConfigured;
        
        return Map.of(
            "healthy", healthy,
            "database", databaseConfigured,
            "cache", cacheConfigured,
            "messaging", messagingConfigured,
            "security", securityConfigured,
            "profile", activeProfile,
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * Get profile information
     */
    @GetMapping("/profile")
    public Map<String, Object> getProfileInfo() {
        log.debug("Getting profile information");
        return Map.of(
            "activeProfile", activeProfile,
            "environment", getEnvironmentFromProfile(activeProfile),
            "host", getHostFromProfile(activeProfile),
            "debugEnabled", isDebugEnabled(activeProfile),
            "loggingLevel", getLoggingLevel(activeProfile)
        );
    }

    private String getEnvironmentFromProfile(String profile) {
        return switch (profile.toLowerCase()) {
            case "local" -> "development";
            case "dev" -> "development";
            case "staging" -> "staging";
            case "prod" -> "production";
            default -> "unknown";
        };
    }

    private String getHostFromProfile(String profile) {
        return switch (profile.toLowerCase()) {
            case "local" -> "localhost";
            case "dev" -> "dev.payments.com";
            case "staging" -> "staging.payments.com";
            case "prod" -> "payments.com";
            default -> "unknown";
        };
    }

    private boolean isDebugEnabled(String profile) {
        return switch (profile.toLowerCase()) {
            case "local", "dev" -> true;
            case "staging", "prod" -> false;
            default -> false;
        };
    }

    private String getLoggingLevel(String profile) {
        return switch (profile.toLowerCase()) {
            case "local" -> "DEBUG";
            case "dev" -> "INFO";
            case "staging" -> "WARN";
            case "prod" -> "ERROR";
            default -> "INFO";
        };
    }
}






