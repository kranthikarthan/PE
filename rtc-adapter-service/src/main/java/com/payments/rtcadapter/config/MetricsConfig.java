package com.payments.rtcadapter.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer metrics configuration for RTC adapter
 */
@Configuration
public class MetricsConfig {

  @Bean
  public Counter rtcAdapterCreatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("rtc.adapter.created")
        .description("Number of RTC adapters created")
        .register(meterRegistry);
  }

  @Bean
  public Counter rtcAdapterActivatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("rtc.adapter.activated")
        .description("Number of RTC adapters activated")
        .register(meterRegistry);
  }

  @Bean
  public Counter rtcAdapterDeactivatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("rtc.adapter.deactivated")
        .description("Number of RTC adapters deactivated")
        .register(meterRegistry);
  }

  @Bean
  public Counter rtcPaymentProcessedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("rtc.payment.processed")
        .description("Number of RTC payments processed")
        .register(meterRegistry);
  }

  @Bean
  public Counter rtcPaymentSuccessfulCounter(MeterRegistry meterRegistry) {
    return Counter.builder("rtc.payment.successful")
        .description("Number of successful RTC payments")
        .register(meterRegistry);
  }

  @Bean
  public Counter rtcPaymentFailedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("rtc.payment.failed")
        .description("Number of failed RTC payments")
        .register(meterRegistry);
  }

  @Bean
  public Timer rtcAdapterCreationTimer(MeterRegistry meterRegistry) {
    return Timer.builder("rtc.adapter.creation.time")
        .description("Time taken to create RTC adapter")
        .register(meterRegistry);
  }

  @Bean
  public Timer rtcPaymentProcessingTimer(MeterRegistry meterRegistry) {
    return Timer.builder("rtc.payment.processing.time")
        .description("Time taken to process RTC payments")
        .register(meterRegistry);
  }

  // TODO: Fix Gauge beans - temporarily commented out due to compilation issues
  // @Bean
  // public Gauge rtcAdapterActiveGauge(MeterRegistry meterRegistry) {
  //   return Gauge.builder("rtc.adapter.active.count")
  //       .description("Number of active RTC adapters")
  //       .register(meterRegistry, (registry) -> {
  //         // This will be updated by the service
  //         return 0;
  //       });
  // }

  // @Bean
  // public Gauge rtcPaymentVolumeGauge(MeterRegistry meterRegistry) {
  //   return Gauge.builder("rtc.payment.volume")
  //       .description("RTC payment volume in ZAR")
  //       .register(meterRegistry, (registry) -> {
  //         // This will be updated by the service
  //         return 0.0;
  //       });
  // }
}
