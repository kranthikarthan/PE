package com.payments.payshapadapter.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer metrics configuration for PayShap adapter
 */
@Configuration
public class MetricsConfig {

  @Bean
  public Counter payshapAdapterCreatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("payshap.adapter.created")
        .description("Number of PayShap adapters created")
        .register(meterRegistry);
  }

  @Bean
  public Counter payshapAdapterActivatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("payshap.adapter.activated")
        .description("Number of PayShap adapters activated")
        .register(meterRegistry);
  }

  @Bean
  public Counter payshapAdapterDeactivatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("payshap.adapter.deactivated")
        .description("Number of PayShap adapters deactivated")
        .register(meterRegistry);
  }

  @Bean
  public Counter payshapPaymentProcessedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("payshap.payment.processed")
        .description("Number of PayShap payments processed")
        .register(meterRegistry);
  }

  @Bean
  public Counter payshapPaymentSuccessfulCounter(MeterRegistry meterRegistry) {
    return Counter.builder("payshap.payment.successful")
        .description("Number of successful PayShap payments")
        .register(meterRegistry);
  }

  @Bean
  public Counter payshapPaymentFailedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("payshap.payment.failed")
        .description("Number of failed PayShap payments")
        .register(meterRegistry);
  }

  @Bean
  public Counter payshapProxyLookupCounter(MeterRegistry meterRegistry) {
    return Counter.builder("payshap.proxy.lookup")
        .description("Number of PayShap proxy lookups")
        .register(meterRegistry);
  }

  @Bean
  public Timer payshapAdapterCreationTimer(MeterRegistry meterRegistry) {
    return Timer.builder("payshap.adapter.creation.time")
        .description("Time taken to create PayShap adapter")
        .register(meterRegistry);
  }

  @Bean
  public Timer payshapPaymentProcessingTimer(MeterRegistry meterRegistry) {
    return Timer.builder("payshap.payment.processing.time")
        .description("Time taken to process PayShap payments")
        .register(meterRegistry);
  }

  @Bean
  public Timer payshapProxyLookupTimer(MeterRegistry meterRegistry) {
    return Timer.builder("payshap.proxy.lookup.time")
        .description("Time taken for PayShap proxy lookups")
        .register(meterRegistry);
  }

  // TODO: Fix Gauge beans - temporarily commented out due to compilation issues
  // @Bean
  // public Gauge payshapAdapterActiveGauge(MeterRegistry meterRegistry) {
  //   return Gauge.builder("payshap.adapter.active.count")
  //       .description("Number of active PayShap adapters")
  //       .register(meterRegistry, (registry) -> {
  //         // This will be updated by the service
  //         return 0;
  //       });
  // }

  // @Bean
  // public Gauge payshapPaymentVolumeGauge(MeterRegistry meterRegistry) {
  //   return Gauge.builder("payshap.payment.volume")
  //       .description("PayShap payment volume in ZAR")
  //       .register(meterRegistry, (registry) -> {
  //         // This will be updated by the service
  //         return 0.0;
  //       });
  // }
}
