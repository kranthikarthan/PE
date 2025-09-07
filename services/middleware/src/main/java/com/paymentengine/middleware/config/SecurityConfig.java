package com.paymentengine.middleware.config;

import com.paymentengine.middleware.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the middleware service
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Tenant management endpoints - require authentication and specific permissions
                .requestMatchers("/api/tenant-management/tenants").hasAnyAuthority("tenant:read", "tenant:manage", "ROLE_ADMIN")
                .requestMatchers("/api/tenant-management/clone").hasAnyAuthority("tenant:manage", "ROLE_ADMIN")
                .requestMatchers("/api/tenant-management/export").hasAnyAuthority("tenant:export", "tenant:manage", "ROLE_ADMIN")
                .requestMatchers("/api/tenant-management/import").hasAnyAuthority("tenant:import", "tenant:manage", "ROLE_ADMIN")
                .requestMatchers("/api/tenant-management/versions/**").hasAnyAuthority("tenant:read", "tenant:manage", "ROLE_ADMIN")
                .requestMatchers("/api/tenant-management/history/**").hasAnyAuthority("tenant:read", "tenant:manage", "ROLE_ADMIN")
                .requestMatchers("/api/tenant-management/templates/**").hasAnyAuthority("tenant:manage", "ROLE_ADMIN")
                .requestMatchers("/api/tenant-management/statistics").hasAnyAuthority("tenant:read", "tenant:manage", "ROLE_ADMIN")
                
                // All other tenant management endpoints
                .requestMatchers("/api/tenant-management/**").hasAnyAuthority("tenant:manage", "ROLE_ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}