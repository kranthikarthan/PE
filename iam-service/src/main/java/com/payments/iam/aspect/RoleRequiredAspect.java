package com.payments.iam.aspect;

import com.payments.iam.annotation.RoleRequired;
import com.payments.iam.exception.ForbiddenException;
import com.payments.iam.service.AuditService;
import com.payments.iam.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * RoleRequiredAspect - AOP aspect for @RoleRequired annotation.
 *
 * <p>Responsibilities:
 * - Intercept methods with @RoleRequired annotation
 * - Extract user ID and tenant ID from SecurityContext
 * - Check if user has required role
 * - Log access attempts (success and denial)
 * - Throw ForbiddenException on authorization failure
 *
 * <p>Performance:
 * - O(1) role checks via Redis cache
 * - No database query required (unless cache miss)
 * - Executed before method execution (fail-fast)
 *
 * <p>Compliance:
 * - All denials logged to audit trail
 * - Includes timestamp, user, resource, reason
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleRequiredAspect {

  private final RoleService roleService;
  private final AuditService auditService;

  /**
   * Intercept methods with @RoleRequired annotation and enforce RBAC.
   *
   * <p>Execution flow:
   * 1. Extract @RoleRequired annotation from method
   * 2. Get current user from SecurityContext
   * 3. Get user's tenant from X-Tenant-ID authority
   * 4. Check if user has at least one required role
   * 5. If yes: continue, log audit trail
   * 6. If no: log denial, throw ForbiddenException
   */
  @Before("@annotation(roleRequired)")
  public void enforceRole(JoinPoint joinPoint, RoleRequired roleRequired) {
    // Extract annotation properties
    String[] requiredRoles = roleRequired.roles();
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getSignature().getDeclaringTypeName();

    log.debug(
        "RoleRequired check: method={}.{}, required_roles={}",
        className,
        methodName,
        Arrays.toString(requiredRoles));

    // Get current authentication
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      log.warn("No authentication found for method {}.{}", className, methodName);
      throw new ForbiddenException("Not authenticated");
    }

    // Extract user ID from principal
    String userId = authentication.getName();
    if (userId == null || userId.isEmpty()) {
      log.warn("No user ID in authentication for method {}.{}", className, methodName);
      throw new ForbiddenException("No user ID found in token");
    }

    // Extract tenant ID from authorities (X-Tenant-ID claim)
    UUID tenantId = extractTenantId(authentication);
    if (tenantId == null) {
      log.warn("No tenant ID in authentication for method {}.{}", className, methodName);
      throw new ForbiddenException("No tenant ID found in token");
    }

    // Check if user has any of the required roles
    List<String> roleList = Arrays.asList(requiredRoles);
    boolean hasRole = roleService.hasAnyRole(userId, tenantId, roleList);

    if (!hasRole) {
      // Access denied
      String reason = String.format(
          "Required roles: %s. User has none of these roles.",
          Arrays.toString(requiredRoles));

      log.warn(
          "Access denied: user={}, tenant={}, method={}.{}, reason={}",
          userId,
          tenantId,
          className,
          methodName,
          reason);

      // Log to audit trail
      auditService.logDenied(
          tenantId,
          userId,
          "ACCESS_DENIED",
          String.format("%s.%s", className, methodName),
          reason);

      throw new ForbiddenException(reason);
    }

    // Access granted
    log.debug(
        "Access granted: user={}, tenant={}, method={}.{}, roles={}",
        userId,
        tenantId,
        className,
        methodName,
        roleList);

    // Log success to audit trail
    auditService.logSuccess(
        tenantId,
        userId,
        "ACCESS_GRANTED",
        String.format("%s.%s", className, methodName),
        null,
        String.format("User has required role(s): %s", Arrays.toString(requiredRoles)));
  }

  /**
   * Extract tenant ID from SecurityContext authorities.
   *
   * <p>Tenant ID is stored as "TENANT_<tenant-uuid>" authority
   * (set by SecurityConfig.extractAuthorities())
   *
   * @param authentication current authentication
   * @return tenant UUID or null if not found
   */
  private UUID extractTenantId(Authentication authentication) {
    return authentication.getAuthorities()
        .stream()
        .filter(auth -> auth.getAuthority().startsWith("TENANT_"))
        .map(auth -> auth.getAuthority().substring("TENANT_".length()))
        .findFirst()
        .map(tenantIdStr -> {
          try {
            return UUID.fromString(tenantIdStr);
          } catch (IllegalArgumentException e) {
            log.warn("Invalid tenant ID format: {}", tenantIdStr);
            return null;
          }
        })
        .orElse(null);
  }
}
