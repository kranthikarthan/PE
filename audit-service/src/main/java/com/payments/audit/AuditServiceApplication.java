package com.payments.audit;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Audit Service Application.
 *
 * <p>This service is responsible for:
 * - Consuming audit events from Kafka (durable subscriber pattern)
 * - Storing audit logs in PostgreSQL for compliance
 * - Providing REST API for querying audit trails
 * - Supporting multi-tenant audit isolation
 * - Maintaining 7-year retention (via CosmosDB or archival)
 *
 * <p>Key Features:
 * - Event-driven (Kafka consumer)
 * - POPIA/FICA/PCI-DSS compliance
 * - Multi-tenancy enforcement
 * - Observability (metrics, tracing)
 * - Caching for performance
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.payments.audit.repository")
@EnableCaching
@EnableKafka
@EnableAsync
@EnableScheduling
public class AuditServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuditServiceApplication.class, args);
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
