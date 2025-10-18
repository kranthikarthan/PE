package com.payments.iam.controller;

import com.payments.iam.dto.TokenValidationResponse;
import com.payments.iam.service.RoleService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * AuthController - Authentication and token validation endpoints.
 *
 * <p>Endpoints:
 * - GET /api/auth/validate - Validate current JWT token
 * - GET /api/auth/me - Get current user profile + roles
 *
 * <p>All endpoints require valid JWT token (from Azure AD B2C)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "JWT token validation and user profile")
public class AuthController {

  private final RoleService roleService;

  /**
   * Validate current JWT token and get user info.
   *
   * <p>Returns:
   * - User ID (from token 'sub' claim)
   * - Tenant ID (from token 'tenant_id' claim)
   * - User's roles in this tenant
   * - Token expiry time
   *
   * @return TokenValidationResponse with user details and roles
   */
  @GetMapping("/validate")
  @Timed(value = "auth.validate", description = "Time to validate token")
  @Operation(
      summary = "Validate JWT token",
      description = "Validate current JWT token and return user info + roles")
  @ApiResponse(
      responseCode = "200",
      description = "Token valid",
      content = @Content(schema = @Schema(implementation = TokenValidationResponse.class)))
  @ApiResponse(responseCode = "401", description = "Token invalid or expired")
  public ResponseEntity<TokenValidationResponse> validateToken() {
    log.debug("Validating token for current user");

    // Get authentication from SecurityContext
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Extract user ID (from 'sub' claim)
    String userId = authentication.getName();

    // Extract tenant ID (from 'TENANT_*' authority)
    UUID tenantId = extractTenantIdFromAuth(authentication);

    // Get user's roles in this tenant
    List<String> roles = roleService.getUserRoles(userId, tenantId);

    // Build response
    TokenValidationResponse response = TokenValidationResponse.builder()
        .userId(userId)
        .tenantId(tenantId)
        .roles(roles)
        .valid(true)
        .authorities(authentication.getAuthorities()
            .stream()
            .map(auth -> auth.getAuthority())
            .toList())
        .build();

    log.info("Token validated for user: {} in tenant: {}", userId, tenantId);

    return ResponseEntity.ok(response);
  }

  /**
   * Get current user profile.
   *
   * <p>Returns: User ID and roles (from JWT token + database)
   *
   * @return User profile with roles
   */
  @GetMapping("/me")
  @Timed(value = "auth.me", description = "Time to get user profile")
  @Operation(
      summary = "Get current user profile",
      description = "Get current user profile including roles")
  @ApiResponse(
      responseCode = "200",
      description = "User profile",
      content = @Content(schema = @Schema(implementation = TokenValidationResponse.class)))
  @ApiResponse(responseCode = "401", description = "Not authenticated")
  public ResponseEntity<TokenValidationResponse> getCurrentUser() {
    log.debug("Getting current user profile");

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String userId = authentication.getName();
    UUID tenantId = extractTenantIdFromAuth(authentication);

    List<String> roles = roleService.getUserRoles(userId, tenantId);

    TokenValidationResponse response = TokenValidationResponse.builder()
        .userId(userId)
        .tenantId(tenantId)
        .roles(roles)
        .valid(true)
        .authorities(authentication.getAuthorities()
            .stream()
            .map(auth -> auth.getAuthority())
            .toList())
        .build();

    log.info("User profile retrieved for: {} in tenant: {}", userId, tenantId);

    return ResponseEntity.ok(response);
  }

  /**
   * Extract tenant ID from security authorities.
   *
   * <p>Tenant ID is stored as "TENANT_<uuid>" authority
   *
   * @param authentication current authentication
   * @return tenant UUID
   */
  private UUID extractTenantIdFromAuth(Authentication authentication) {
    return authentication.getAuthorities()
        .stream()
        .filter(auth -> auth.getAuthority().startsWith("TENANT_"))
        .map(auth -> auth.getAuthority().substring("TENANT_".length()))
        .findFirst()
        .map(UUID::fromString)
        .orElse(null);
  }
}
