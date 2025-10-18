package com.payments.notification;

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
 * Spring Boot Application for Notification Service.
 *
 * <p>Enables:
 * - JPA repositories for database access
 * - Kafka listeners (competing consumers pattern)
 * - Caching with Redis
 * - Async operations for channel dispatching
 * - Scheduled tasks for retry logic and batch processing
 * - Micrometer metrics collection
 *
 * @author Payment Engine
 */
@SpringBootApplication(
    scanBasePackages = {
      "com.payments.notification",
      "com.payments.shared.config",
      "com.payments.shared.telemetry"
    })
@EnableJpaRepositories(basePackages = "com.payments.notification.repository")
@EnableKafka
@EnableCaching
@EnableAsync
@EnableScheduling
public class NotificationServiceApplication {

  /**
   * Application entry point.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(NotificationServiceApplication.class, args);
  }

  /**
   * Configure Micrometer TimedAspect for method-level timing metrics.
   *
   * @param meterRegistry the MeterRegistry to use for recording metrics
   * @return TimedAspect bean
   */
  @Bean
  public TimedAspect timedAspect(MeterRegistry meterRegistry) {
    return new TimedAspect(meterRegistry);
  }
}
