package com.payments.iam;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * IAM Service Application.
 *
 * <p>This service is responsible for:
 * - OAuth2/JWT token validation (from Azure AD B2C)
 * - Role-Based Access Control (RBAC)
 * - Attribute-Based Access Control (ABAC)
 * - User role management
 * - Audit logging for compliance
 * - Multi-tenancy enforcement
 *
 * <p>Security: All endpoints require JWT token + X-Tenant-ID header
 * Performance: Caching enabled for role lookups (O(1) via Redis)
 * Observability: OpenTelemetry tracing + Micrometer metrics
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.payments.iam.repository")
@EnableCaching
public class IamServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(IamServiceApplication.class, args);
  }

  /**
   * TimedAspect for method-level timing metrics.
   *
   * <p>Automatically records execution time for all @Timed methods
   */
  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }
}
