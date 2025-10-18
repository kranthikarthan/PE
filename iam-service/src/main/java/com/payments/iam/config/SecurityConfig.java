package com.payments.iam.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Spring Security Configuration for OAuth2 Resource Server.
 *
 * <p>Configures JWT validation from Azure AD B2C:
 * 1. All endpoints require JWT token (except /health, /actuator)
 * 2. Stateless session (no cookies)
 * 3. JWT claims extracted to SecurityContext
 * 4. Method-level security via @PreAuthorize, @RoleRequired
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  /**
   * Configure HTTP security with OAuth2 resource server.
   *
   * <p>Authorization flow:
   * 1. Client sends: Authorization: Bearer <JWT>
   * 2. Spring validates JWT signature (via JWKS endpoint)
   * 3. JWT claims extracted to SecurityContext
   * 4. @RoleRequired annotation checks roles (via AOP)
   * 5. Request proceeds or 403 Forbidden
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public endpoints (no auth required)
                    .requestMatchers("/health", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())
        // OAuth2 Resource Server configuration
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.jwtAuthenticationConverter(
                            jwtAuthenticationConverter()))) // Extract claims to SecurityContext
        // Stateless: no session cookies, JWT is stateless
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  /**
   * JwtAuthenticationConverter - Extract claims from JWT and create Authentication.
   *
   * <p>This converter:
   * 1. Extracts JWT claims (sub, tenant_id, email, roles)
   * 2. Creates JwtAuthenticationToken with authorities
   * 3. Populates SecurityContext with user details
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    // Custom converter to extract roles and tenant_id
    converter.setJwtGrantedAuthoritiesConverter(jwt -> extractAuthorities(jwt));

    // Set Principal claim to 'sub' (Azure AD B2C subject)
    converter.setPrincipalClaimName("sub");

    return converter;
  }

  /**
   * Extract authorities from JWT claims.
   *
   * <p>Extracts:
   * - Scopes (scp claim) as SCOPE_<scope>
   * - Roles (roles claim) as ROLE_<role>
   * - Tenant ID (tenant_id claim) as TENANT_<tenant_id>
   */
  private Set<GrantedAuthority> extractAuthorities(Jwt jwt) {
    Set<GrantedAuthority> authorities = new HashSet<>();

    // Extract scopes (if present)
    if (jwt.hasClaim("scp")) {
      String scopes = jwt.getClaimAsString("scp");
      if (scopes != null) {
        for (String scope : scopes.split(" ")) {
          authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
        }
      }
    }

    // Extract roles (if present in custom claim)
    if (jwt.hasClaim("roles")) {
      @SuppressWarnings("unchecked")
      List<String> roles = (List<String>) jwt.getClaim("roles");
      if (roles != null) {
        for (String role : roles) {
          authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        }
      }
    }

    // Extract tenant_id for multi-tenancy
    if (jwt.hasClaim("tenant_id")) {
      authorities.add(new SimpleGrantedAuthority("TENANT_" + jwt.getClaimAsString("tenant_id")));
    }

    return authorities;
  }
}
