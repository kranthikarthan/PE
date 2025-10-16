package com.payments.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;

/**
 * Profile management for different environments
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ProfileManager {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    /**
     * Local development profile
     */
    @Bean
    @Profile("local")
    public LocalProfileConfig localProfileConfig() {
        log.info("Configuring local profile");
        return new LocalProfileConfig();
    }

    /**
     * Development profile
     */
    @Bean
    @Profile("dev")
    public DevProfileConfig devProfileConfig() {
        log.info("Configuring dev profile");
        return new DevProfileConfig();
    }

    /**
     * Staging profile
     */
    @Bean
    @Profile("staging")
    public StagingProfileConfig stagingProfileConfig() {
        log.info("Configuring staging profile");
        return new StagingProfileConfig();
    }

    /**
     * Production profile
     */
    @Bean
    @Profile("prod")
    public ProdProfileConfig prodProfileConfig() {
        log.info("Configuring prod profile");
        return new ProdProfileConfig();
    }

    /**
     * Base profile configuration
     */
    public static abstract class BaseProfileConfig {
        public abstract String getProfileName();
        public abstract String getEnvironment();
        public abstract String getHost();
        public abstract Map<String, Object> getConfiguration();
    }

    /**
     * Local development profile configuration
     */
    public static class LocalProfileConfig extends BaseProfileConfig {
        public String getProfileName() {
            return "local";
        }

        public String getEnvironment() {
            return "development";
        }

        public String getHost() {
            return "localhost";
        }

        public Map<String, Object> getConfiguration() {
            return Map.ofEntries(
                Map.entry("debug.enabled", true),
                Map.entry("logging.level", "DEBUG"),
                Map.entry("database.url", "jdbc:postgresql://localhost:5432/payments_local"),
                Map.entry("cache.host", "localhost"),
                Map.entry("cache.port", 6379),
                Map.entry("kafka.bootstrap.servers", "localhost:9092"),
                Map.entry("telemetry.tracing.enabled", true),
                Map.entry("telemetry.metrics.enabled", true),
                Map.entry("telemetry.logging.enabled", true),
                Map.entry("security.encryption.enabled", false),
                Map.entry("monitoring.enabled", true)
            );
        }
    }

    /**
     * Development profile configuration
     */
    public static class DevProfileConfig extends BaseProfileConfig {
        public String getProfileName() {
            return "dev";
        }

        public String getEnvironment() {
            return "development";
        }

        public String getHost() {
            return "dev.payments.com";
        }

        public Map<String, Object> getConfiguration() {
            return Map.ofEntries(
                Map.entry("debug.enabled", true),
                Map.entry("logging.level", "INFO"),
                Map.entry("database.url", "jdbc:postgresql://dev-db.payments.com:5432/payments_dev"),
                Map.entry("cache.host", "dev-redis.payments.com"),
                Map.entry("cache.port", 6379),
                Map.entry("kafka.bootstrap.servers", "dev-kafka.payments.com:9092"),
                Map.entry("telemetry.tracing.enabled", true),
                Map.entry("telemetry.metrics.enabled", true),
                Map.entry("telemetry.logging.enabled", true),
                Map.entry("security.encryption.enabled", true),
                Map.entry("monitoring.enabled", true)
            );
        }
    }

    /**
     * Staging profile configuration
     */
    public static class StagingProfileConfig extends BaseProfileConfig {
        public String getProfileName() {
            return "staging";
        }

        public String getEnvironment() {
            return "staging";
        }

        public String getHost() {
            return "staging.payments.com";
        }

        public Map<String, Object> getConfiguration() {
            return Map.ofEntries(
                Map.entry("debug.enabled", false),
                Map.entry("logging.level", "WARN"),
                Map.entry("database.url", "jdbc:postgresql://staging-db.payments.com:5432/payments_staging"),
                Map.entry("cache.host", "staging-redis.payments.com"),
                Map.entry("cache.port", 6379),
                Map.entry("kafka.bootstrap.servers", "staging-kafka.payments.com:9092"),
                Map.entry("telemetry.tracing.enabled", true),
                Map.entry("telemetry.metrics.enabled", true),
                Map.entry("telemetry.logging.enabled", true),
                Map.entry("security.encryption.enabled", true),
                Map.entry("monitoring.enabled", true)
            );
        }
    }

    /**
     * Production profile configuration
     */
    public static class ProdProfileConfig extends BaseProfileConfig {
        public String getProfileName() {
            return "prod";
        }

        public String getEnvironment() {
            return "production";
        }

        public String getHost() {
            return "payments.com";
        }

        public Map<String, Object> getConfiguration() {
            return Map.ofEntries(
                Map.entry("debug.enabled", false),
                Map.entry("logging.level", "ERROR"),
                Map.entry("database.url", "jdbc:postgresql://prod-db.payments.com:5432/payments_prod"),
                Map.entry("cache.host", "prod-redis.payments.com"),
                Map.entry("cache.port", 6379),
                Map.entry("kafka.bootstrap.servers", "prod-kafka.payments.com:9092"),
                Map.entry("telemetry.tracing.enabled", true),
                Map.entry("telemetry.metrics.enabled", true),
                Map.entry("telemetry.logging.enabled", true),
                Map.entry("security.encryption.enabled", true),
                Map.entry("monitoring.enabled", true)
            );
        }
    }
}
