package com.payments.operationsmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security Configuration for Operations Management Service
 * 
 * Configures OAuth2 JWT authentication and role-based access control
 * for operations management endpoints.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Security filter chain for operations management API
     */
    @Bean
    public SecurityFilterChain operationsApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/ops/v1/**")
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Health endpoints - no authentication required
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Service Management endpoints
                .requestMatchers("GET", "/api/ops/v1/services/**").hasAnyRole("OPS_VIEWER", "OPS_OPERATOR", "OPS_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers("POST", "/api/ops/v1/services/*/restart").hasAnyRole("OPS_ADMIN", "PLATFORM_ADMIN")
                
                // Circuit Breaker endpoints
                .requestMatchers("GET", "/api/ops/v1/circuit-breakers/**").hasAnyRole("OPS_VIEWER", "OPS_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers("POST", "/api/ops/v1/circuit-breakers/**").hasAnyRole("OPS_ADMIN", "PLATFORM_ADMIN")
                
                // Feature Flag endpoints
                .requestMatchers("GET", "/api/ops/v1/feature-flags/**").hasAnyRole("OPS_VIEWER", "OPS_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers("PUT", "/api/ops/v1/feature-flags/**").hasAnyRole("OPS_ADMIN", "PLATFORM_ADMIN")
                
                // All other operations endpoints require authentication
                .requestMatchers("/api/ops/v1/**").authenticated()
                
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder())));
        
        return http.build();
    }

    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/ops/v1/**", configuration);
        return source;
    }

    /**
     * JWT decoder bean
     */
    @Bean
    public org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder() {
        // This would be configured with the actual JWT decoder
        // For now, return a mock decoder
        return token -> {
            // Mock JWT token implementation
            return org.springframework.security.oauth2.jwt.Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .header("typ", "JWT")
                .claim("sub", "user123")
                .claim("roles", Arrays.asList("OPS_ADMIN"))
                .claim("tenant_id", "default")
                .issuedAt(java.time.Instant.now())
                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                .build();
        };
    }
}
