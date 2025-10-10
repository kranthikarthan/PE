package com.paymentengine.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Security configuration for API Gateway
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(authz -> authz
                        // Public endpoints
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/fallback/**").permitAll()
                        
                        // ISO 20022 endpoints require specific scopes
                        .pathMatchers("/api/v1/iso20022/comprehensive/**")
                            .hasAuthority("SCOPE_iso20022:send")
                        .pathMatchers("/api/v1/iso20022/validate/**")
                            .hasAuthority("SCOPE_iso20022:validate")
                        .pathMatchers("/api/v1/iso20022/transform/**")
                            .hasAuthority("SCOPE_iso20022:transform")
                        .pathMatchers("/api/v1/iso20022/status/**")
                            .hasAuthority("SCOPE_iso20022:status")
                        
                        // Scheme configuration endpoints
                        .pathMatchers("/api/v1/scheme/**")
                            .hasAuthority("SCOPE_scheme:manage")
                        
                        // Clearing system endpoints
                        .pathMatchers("/api/v1/clearing-system/**")
                            .hasAuthority("SCOPE_clearing:manage")
                        
                        // Admin endpoints
                        .pathMatchers("/api/v1/admin/**")
                            .hasAuthority("SCOPE_admin:manage")
                        
                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
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
    public ReactiveJwtDecoder jwtDecoder() {
        // In production, this should be configured with the actual JWT issuer URI
        return NimbusReactiveJwtDecoder.withJwkSetUri("http://localhost:8081/auth/realms/payment-engine/protocol/openid-connect/certs")
                .build();
    }
}