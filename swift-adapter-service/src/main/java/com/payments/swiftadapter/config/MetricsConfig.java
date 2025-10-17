package com.payments.swiftadapter.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer metrics configuration for SWIFT adapter
 */
@Configuration
public class MetricsConfig {

  @Bean
  public Counter swiftAdapterCreatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.adapter.created")
        .description("Number of SWIFT adapters created")
        .register(meterRegistry);
  }

  @Bean
  public Counter swiftAdapterActivatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.adapter.activated")
        .description("Number of SWIFT adapters activated")
        .register(meterRegistry);
  }

  @Bean
  public Counter swiftAdapterDeactivatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.adapter.deactivated")
        .description("Number of SWIFT adapters deactivated")
        .register(meterRegistry);
  }

  @Bean
  public Counter swiftPaymentProcessedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.payment.processed")
        .description("Number of SWIFT payments processed")
        .register(meterRegistry);
  }

  @Bean
  public Counter swiftPaymentSuccessfulCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.payment.successful")
        .description("Number of successful SWIFT payments")
        .register(meterRegistry);
  }

  @Bean
  public Counter swiftPaymentFailedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.payment.failed")
        .description("Number of failed SWIFT payments")
        .register(meterRegistry);
  }

  @Bean
  public Counter swiftSanctionsScreeningCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.sanctions.screening")
        .description("Number of SWIFT sanctions screenings")
        .register(meterRegistry);
  }

  @Bean
  public Counter swiftFxConversionCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.fx.conversion")
        .description("Number of SWIFT FX conversions")
        .register(meterRegistry);
  }

  @Bean
  public Counter swiftMt103ProcessedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.mt103.processed")
        .description("Number of MT103 messages processed")
        .register(meterRegistry);
  }

  @Bean
  public Counter swiftPacs008ProcessedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.pacs008.processed")
        .description("Number of pacs.008 messages processed")
        .register(meterRegistry);
  }

  @Bean
  public Counter swiftPacs002ProcessedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("swift.pacs002.processed")
        .description("Number of pacs.002 messages processed")
        .register(meterRegistry);
  }

  @Bean
  public Timer swiftAdapterCreationTimer(MeterRegistry meterRegistry) {
    return Timer.builder("swift.adapter.creation.time")
        .description("Time taken to create SWIFT adapter")
        .register(meterRegistry);
  }

  @Bean
  public Timer swiftPaymentProcessingTimer(MeterRegistry meterRegistry) {
    return Timer.builder("swift.payment.processing.time")
        .description("Time taken to process SWIFT payments")
        .register(meterRegistry);
  }

  @Bean
  public Timer swiftSanctionsScreeningTimer(MeterRegistry meterRegistry) {
    return Timer.builder("swift.sanctions.screening.time")
        .description("Time taken for SWIFT sanctions screening")
        .register(meterRegistry);
  }

  @Bean
  public Timer swiftFxConversionTimer(MeterRegistry meterRegistry) {
    return Timer.builder("swift.fx.conversion.time")
        .description("Time taken for SWIFT FX conversion")
        .register(meterRegistry);
  }

  // TODO: Fix Gauge beans - temporarily commented out due to compilation issues
  // @Bean
  // public Gauge swiftAdapterActiveGauge(MeterRegistry meterRegistry) {
  //   return Gauge.builder("swift.adapter.active.count")
  //       .description("Number of active SWIFT adapters")
  //       .register(meterRegistry, (registry) -> {
  //         // This will be updated by the service
  //         return 0;
  //       });
  // }

  // @Bean
  // public Gauge swiftPaymentVolumeGauge(MeterRegistry meterRegistry) {
  //   return Gauge.builder("swift.payment.volume")
  //       .description("SWIFT payment volume in USD")
  //       .register(meterRegistry, (registry) -> {
  //         // This will be updated by the service
  //         return 0.0;
  //       });
  // }
}
