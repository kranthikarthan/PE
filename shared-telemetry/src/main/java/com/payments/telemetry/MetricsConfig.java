package com.payments.telemetry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for metrics collection */
@Configuration
@Slf4j
public class MetricsConfig {

  @Value("${telemetry.metrics.enabled:true}")
  private boolean metricsEnabled;

  @Value("${telemetry.metrics.prometheus.enabled:true}")
  private boolean prometheusEnabled;

  @Value("${telemetry.metrics.service.name:payments-engine}")
  private String serviceName;

  @Bean
  @ConditionalOnProperty(name = "telemetry.metrics.enabled", havingValue = "true")
  public MeterRegistry meterRegistry() {
    if (prometheusEnabled) {
      log.info("Configuring Prometheus metrics registry");
      return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
    return io.micrometer.core.instrument.Metrics.globalRegistry;
  }

  @Bean
  @ConditionalOnProperty(name = "telemetry.metrics.enabled", havingValue = "true")
  public PaymentMetrics paymentMetrics(MeterRegistry meterRegistry) {
    return new PaymentMetrics(meterRegistry);
  }

  /** Custom metrics for payment processing */
  public static class PaymentMetrics {
    private final MeterRegistry meterRegistry;
    private final AtomicLong activePayments = new AtomicLong(0);
    private final AtomicLong activeSagas = new AtomicLong(0);

    public PaymentMetrics(MeterRegistry meterRegistry) {
      this.meterRegistry = meterRegistry;
      initializeMetrics();
    }

    private void initializeMetrics() {
      // Payment counters
      Counter.builder("payments.initiated.total")
          .description("Total number of payments initiated")
          .register(meterRegistry);

      Counter.builder("payments.completed.total")
          .description("Total number of payments completed")
          .register(meterRegistry);

      Counter.builder("payments.failed.total")
          .description("Total number of payments failed")
          .register(meterRegistry);

      // Payment timers
      Timer.builder("payments.processing.duration")
          .description("Payment processing duration")
          .register(meterRegistry);

      Timer.builder("payments.validation.duration")
          .description("Payment validation duration")
          .register(meterRegistry);

      Timer.builder("payments.routing.duration")
          .description("Payment routing duration")
          .register(meterRegistry);

      // Saga metrics
      Counter.builder("sagas.started.total")
          .description("Total number of sagas started")
          .register(meterRegistry);

      Counter.builder("sagas.completed.total")
          .description("Total number of sagas completed")
          .register(meterRegistry);

      Counter.builder("sagas.compensated.total")
          .description("Total number of sagas compensated")
          .register(meterRegistry);

      Timer.builder("sagas.execution.duration")
          .description("Saga execution duration")
          .register(meterRegistry);

      // Active counts
      Gauge.builder("payments.active.count", activePayments, AtomicLong::get)
          .description("Number of active payments")
          .register(meterRegistry);

      Gauge.builder("sagas.active.count", activeSagas, AtomicLong::get)
          .description("Number of active sagas")
          .register(meterRegistry);

      // Error metrics
      Counter.builder("errors.total").description("Total number of errors").register(meterRegistry);

      Counter.builder("errors.validation.total")
          .description("Total number of validation errors")
          .register(meterRegistry);

      Counter.builder("errors.routing.total")
          .description("Total number of routing errors")
          .register(meterRegistry);

      Counter.builder("errors.transaction.total")
          .description("Total number of transaction errors")
          .register(meterRegistry);
    }

    public void incrementPaymentsInitiated() {
      Counter.builder("payments.initiated.total").register(meterRegistry).increment();
    }

    public void incrementPaymentsCompleted() {
      Counter.builder("payments.completed.total").register(meterRegistry).increment();
    }

    public void incrementPaymentsFailed() {
      Counter.builder("payments.failed.total").register(meterRegistry).increment();
    }

    public Timer.Sample startPaymentProcessing() {
      return Timer.start(meterRegistry);
    }

    public void recordPaymentProcessing(Timer.Sample sample) {
      sample.stop(Timer.builder("payments.processing.duration").register(meterRegistry));
    }

    public void incrementSagasStarted() {
      Counter.builder("sagas.started.total").register(meterRegistry).increment();
      activeSagas.incrementAndGet();
    }

    public void incrementSagasCompleted() {
      Counter.builder("sagas.completed.total").register(meterRegistry).increment();
      activeSagas.decrementAndGet();
    }

    public void incrementSagasCompensated() {
      Counter.builder("sagas.compensated.total").register(meterRegistry).increment();
      activeSagas.decrementAndGet();
    }

    public void incrementErrors(String errorType) {
      Counter.builder("errors.total").register(meterRegistry).increment();

      Counter.builder("errors." + errorType + ".total").register(meterRegistry).increment();
    }

    public void setActivePayments(long count) {
      activePayments.set(count);
    }

    public void setActiveSagas(long count) {
      activeSagas.set(count);
    }
  }
}
