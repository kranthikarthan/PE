package com.paymentengine.paymentprocessing.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Security configuration for Payment Processing Service
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()
                        
                        // ISO 20022 endpoints
                        .requestMatchers("/api/v1/iso20022/comprehensive/**")
                            .hasAuthority("SCOPE_iso20022:send")
                        .requestMatchers("/api/v1/iso20022/validate/**")
                            .hasAuthority("SCOPE_iso20022:validate")
                        .requestMatchers("/api/v1/iso20022/transform/**")
                            .hasAuthority("SCOPE_iso20022:transform")
                        .requestMatchers("/api/v1/iso20022/status/**")
                            .hasAuthority("SCOPE_iso20022:status")
                        
                        // Scheme configuration endpoints
                        .requestMatchers("/api/v1/scheme/**")
                            .hasAuthority("SCOPE_scheme:manage")
                        
                        // Clearing system endpoints
                        .requestMatchers("/api/v1/clearing-system/**")
                            .hasAuthority("SCOPE_clearing:manage")
                        
                        // Admin endpoints
                        .requestMatchers("/api/v1/admin/**")
                            .hasAuthority("SCOPE_admin:manage")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extract scopes from JWT token
        List<String> scopes = jwt.getClaimAsStringList("scope");
        if (scopes == null) {
            scopes = Collections.emptyList();
        }
        
        // Extract roles from JWT token
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            roles = Collections.emptyList();
        }
        
        // Convert scopes and roles to authorities
        return scopes.stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toList());
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // In production, this should be configured with the actual JWT issuer URI
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:8081/auth/realms/payment-engine/protocol/openid-connect/certs")
                .build();
    }
}