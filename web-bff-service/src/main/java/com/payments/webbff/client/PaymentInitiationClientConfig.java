package com.payments.webbff.client;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Configuration for Payment Initiation Service OpenFeign client.
 *
 * <p>This configuration sets up authentication, logging, and other
 * client-specific configurations for the Payment Initiation Service.
 *
 * @since PE-414
 */
@Configuration
public class PaymentInitiationClientConfig {

    /**
     * Configures the Feign logger level.
     *
     * @return logger level
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Configures request interceptor for authentication.
     *
     * @return request interceptor
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Add JWT token to request headers
                try {
                    Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    if (jwt != null) {
                        template.header("Authorization", "Bearer " + jwt.getTokenValue());
                    }
                } catch (Exception e) {
                    // Handle case where no authentication context is available
                    // This might happen in some test scenarios
                }
                
                // Add tenant context headers
                template.header("X-Tenant-ID", getCurrentTenantId());
                template.header("X-Business-Unit-ID", getCurrentBusinessUnitId());
            }
        };
    }

    private String getCurrentTenantId() {
        // Implementation to get current tenant ID from security context
        // This would typically come from JWT claims or other security context
        return "default-tenant"; // Placeholder implementation
    }

    private String getCurrentBusinessUnitId() {
        // Implementation to get current business unit ID from security context
        // This would typically come from JWT claims or other security context
        return "default-business-unit"; // Placeholder implementation
    }
}
