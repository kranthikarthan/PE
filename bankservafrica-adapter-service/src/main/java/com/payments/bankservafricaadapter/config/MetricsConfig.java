package com.payments.bankservafricaadapter.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Micrometer metrics configuration for BankservAfrica adapter */
@Configuration
public class MetricsConfig {

  @Bean
  public Counter bankservafricaAdapterCreatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("bankservafrica.adapter.created")
        .description("Number of BankservAfrica adapters created")
        .register(meterRegistry);
  }

  @Bean
  public Counter bankservafricaAdapterActivatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("bankservafrica.adapter.activated")
        .description("Number of BankservAfrica adapters activated")
        .register(meterRegistry);
  }

  @Bean
  public Counter bankservafricaAdapterDeactivatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("bankservafrica.adapter.deactivated")
        .description("Number of BankservAfrica adapters deactivated")
        .register(meterRegistry);
  }

  @Bean
  public Counter bankservafricaAdapterConfigurationUpdatedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("bankservafrica.adapter.configuration.updated")
        .description("Number of BankservAfrica adapter configuration updates")
        .register(meterRegistry);
  }

  @Bean
  public Counter bankservafricaEftProcessedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("bankservafrica.eft.processed")
        .description("Number of EFT transactions processed")
        .register(meterRegistry);
  }

  @Bean
  public Counter bankservafricaIso8583ProcessedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("bankservafrica.iso8583.processed")
        .description("Number of ISO 8583 transactions processed")
        .register(meterRegistry);
  }

  @Bean
  public Counter bankservafricaAchProcessedCounter(MeterRegistry meterRegistry) {
    return Counter.builder("bankservafrica.ach.processed")
        .description("Number of ACH transactions processed")
        .register(meterRegistry);
  }

  @Bean
  public Timer bankservafricaAdapterCreationTimer(MeterRegistry meterRegistry) {
    return Timer.builder("bankservafrica.adapter.creation.time")
        .description("Time taken to create BankservAfrica adapter")
        .register(meterRegistry);
  }

  @Bean
  public Timer bankservafricaEftProcessingTimer(MeterRegistry meterRegistry) {
    return Timer.builder("bankservafrica.eft.processing.time")
        .description("Time taken to process EFT transactions")
        .register(meterRegistry);
  }

  @Bean
  public Timer bankservafricaIso8583ProcessingTimer(MeterRegistry meterRegistry) {
    return Timer.builder("bankservafrica.iso8583.processing.time")
        .description("Time taken to process ISO 8583 transactions")
        .register(meterRegistry);
  }

  @Bean
  public Timer bankservafricaAchProcessingTimer(MeterRegistry meterRegistry) {
    return Timer.builder("bankservafrica.ach.processing.time")
        .description("Time taken to process ACH transactions")
        .register(meterRegistry);
  }

  // TODO: Fix Gauge beans - temporarily commented out due to compilation issues
  // @Bean
  // public Gauge bankservafricaAdapterActiveGauge(MeterRegistry meterRegistry) {
  //   return Gauge.builder("bankservafrica.adapter.active.count")
  //       .description("Number of active BankservAfrica adapters")
  //       .register(meterRegistry, (registry) -> {
  //         // This will be updated by the service
  //         return 0;
  //       });
  // }
}
