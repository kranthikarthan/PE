package com.payments.samosadapter.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Micrometer metrics configuration for SAMOS adapter */
@Configuration
public class MetricsConfig {

  @Bean
  public Counter samosAdapterCreatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("samos.adapter.created")
        .description("Number of SAMOS adapters created")
        .register(meterRegistry);
  }

  @Bean
  public Counter samosAdapterActivatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("samos.adapter.activated")
        .description("Number of SAMOS adapters activated")
        .register(meterRegistry);
  }

  @Bean
  public Counter samosAdapterDeactivatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("samos.adapter.deactivated")
        .description("Number of SAMOS adapters deactivated")
        .register(meterRegistry);
  }

  @Bean
  public Counter samosAdapterConfigurationUpdatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("samos.adapter.configuration.updated")
        .description("Number of SAMOS adapter configuration updates")
        .register(meterRegistry);
  }

  @Bean
  public Timer samosAdapterCreationTimer(MeterRegistry meterRegistry) {
    return Timer.builder("samos.adapter.creation.time")
        .description("Time taken to create SAMOS adapter")
        .register(meterRegistry);
  }

  @Bean
  public Timer samosAdapterActivationTimer(MeterRegistry meterRegistry) {
    return Timer.builder("samos.adapter.activation.time")
        .description("Time taken to activate SAMOS adapter")
        .register(meterRegistry);
  }

  @Bean
  public Timer samosAdapterDeactivationTimer(MeterRegistry meterRegistry) {
    return Timer.builder("samos.adapter.deactivation.time")
        .description("Time taken to deactivate SAMOS adapter")
        .register(meterRegistry);
  }

  @Bean
  public Timer samosAdapterConfigurationUpdateTimer(MeterRegistry meterRegistry) {
    return Timer.builder("samos.adapter.configuration.update.time")
        .description("Time taken to update SAMOS adapter configuration")
        .register(meterRegistry);
  }

  // TODO: Fix Gauge beans - temporarily commented out due to compilation issues
  // @Bean
  // public Gauge samosAdapterActiveGauge(MeterRegistry meterRegistry) {
  //   return Gauge.builder("samos.adapter.active.count")
  //       .description("Number of active SAMOS adapters")
  //       .register(meterRegistry, this, (obj) -> 0.0);
  // }

  // @Bean
  // public Gauge samosAdapterInactiveGauge(MeterRegistry meterRegistry) {
  //   return Gauge.builder("samos.adapter.inactive.count")
  //       .description("Number of inactive SAMOS adapters")
  //       .register(meterRegistry, this, (obj) -> 0.0);
  // }
}
