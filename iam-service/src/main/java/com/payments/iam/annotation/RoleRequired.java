package com.payments.iam.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @RoleRequired - Annotation for role-based access control at method level.
 *
 * <p>Usage:
 * <pre>
 * @GetMapping("/admin-only")
 * @RoleRequired(roles = {"bank_admin"})
 * public ResponseEntity<String> adminOnly() {
 *     // Only bank_admin role can access
 * }
 *
 * @PostMapping("/approve")
 * @RoleRequired(roles = {"bank_admin", "manager"})  // OR relationship
 * public ResponseEntity<String> approvePayment() {
 *     // bank_admin OR manager role required
 * }
 * </pre>
 *
 * <p>Enforcement:
 * - Intercepted by RoleRequiredAspect (AOP)
 * - Checks user's role against allowed roles
 * - Enforced at method entry (fail-fast)
 * - Tenant context extracted from SecurityContext
 * - Access denied requests logged to audit trail
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleRequired {

  /**
   * List of allowed roles (OR relationship).
   *
   * <p>Example: roles = {"bank_admin", "manager"}
   * User must have AT LEAST ONE of these roles in the current tenant
   *
   * @return array of role names
   */
  String[] roles();

  /**
   * Optional description for documentation.
   *
   * @return description
   */
  String description() default "";
}
