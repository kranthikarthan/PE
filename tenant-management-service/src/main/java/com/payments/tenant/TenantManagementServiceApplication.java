package com.payments.tenant;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Tenant Management Service Application.
 *
 * <p>This service is responsible for:
 * - Tenant lifecycle management (create, activate, suspend, delete)
 * - Business unit management
 * - Configuration management
 * - Multi-tenancy enforcement
 *
 * <p>Security: All endpoints require JWT token + X-Tenant-ID header
 * Performance: Caching enabled for tenant lookups (O(1) via Redis)
 * Observability: OpenTelemetry tracing + Micrometer metrics
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.payments.tenant.repository")
@EnableCaching
public class TenantManagementServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(TenantManagementServiceApplication.class, args);
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
