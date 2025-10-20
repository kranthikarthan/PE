package com.payments.operationsmanagement.config;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Unleash Feature Flag Configuration
 * 
 * Configures Unleash client for feature flag management.
 * Provides runtime feature toggles for operations management.
 */
@Configuration
@Slf4j
public class UnleashConfig {

    @Value("${app.unleash.api-url:http://unleash:4242/api}")
    private String unleashApiUrl;

    @Value("${app.unleash.app-name:operations-management-service}")
    private String appName;

    @Value("${app.unleash.instance-id:operations-management-001}")
    private String instanceId;

    @Value("${app.unleash.api-token:}")
    private String apiToken;

    /**
     * Unleash client bean
     */
    @Bean
    public Unleash unleash() {
        try {
            UnleashConfig config = UnleashConfig.builder()
                .appName(appName)
                .instanceId(instanceId)
                .unleashAPI(unleashApiUrl)
                .apiKey(apiToken)
                .build();

            Unleash unleash = new DefaultUnleash(config);
            log.info("Unleash client configured for app: {} in instance: {}", appName, instanceId);
            return unleash;

        } catch (Exception e) {
            log.error("Failed to configure Unleash client", e);
            // Return a mock unleash client for development
            return new MockUnleash();
        }
    }

    /**
     * Mock Unleash client for development
     */
    private static class MockUnleash implements Unleash {
        @Override
        public boolean isEnabled(String toggleName) {
            return false;
        }

        @Override
        public boolean isEnabled(String toggleName, boolean defaultValue) {
            return defaultValue;
        }

        @Override
        public io.getunleash.Variant getVariant(String toggleName) {
            return io.getunleash.Variant.DISABLED_VARIANT;
        }

        @Override
        public io.getunleash.Variant getVariant(String toggleName, io.getunleash.Variant defaultValue) {
            return defaultValue;
        }

        @Override
        public void shutdown() {
            // No-op
        }
    }
}
