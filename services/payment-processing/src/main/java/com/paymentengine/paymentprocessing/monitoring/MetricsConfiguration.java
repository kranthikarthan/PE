package com.paymentengine.paymentprocessing.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.DistributionSummary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Configuration for application metrics using Micrometer
 */
@Configuration
public class MetricsConfiguration {

    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong totalMessagesProcessed = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                "application", "payment-engine-payment-processing",
                "version", "1.0.0",
                "environment", "production"
        );
    }

    @Bean
    public Counter messageProcessedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("iso20022.messages.processed")
                .description("Total number of ISO 20022 messages processed")
                .tag("type", "total")
                .register(meterRegistry);
    }

    @Bean
    public Counter messageProcessedByTypeCounter(MeterRegistry meterRegistry) {
        return Counter.builder("iso20022.messages.processed.by.type")
                .description("Number of ISO 20022 messages processed by type")
                .register(meterRegistry);
    }

    @Bean
    public Counter messageErrorsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("iso20022.messages.errors")
                .description("Total number of message processing errors")
                .register(meterRegistry);
    }

    @Bean
    public Timer messageProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("iso20022.messages.processing.time")
                .description("Time taken to process ISO 20022 messages")
                .register(meterRegistry);
    }

    @Bean
    public Timer clearingSystemResponseTimer(MeterRegistry meterRegistry) {
        return Timer.builder("clearing.system.response.time")
                .description("Time taken for clearing system responses")
                .register(meterRegistry);
    }

    @Bean
    public DistributionSummary messageSizeDistribution(MeterRegistry meterRegistry) {
        return DistributionSummary.builder("iso20022.messages.size")
                .description("Distribution of ISO 20022 message sizes")
                .baseUnit("bytes")
                .register(meterRegistry);
    }

    @Bean
    public Gauge activeConnectionsGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("iso20022.connections.active", this, MetricsConfiguration::getActiveConnections)
                .description("Number of active connections")
                .register(meterRegistry);
    }

    @Bean
    public Gauge totalMessagesProcessedGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("iso20022.messages.total.processed", this, MetricsConfiguration::getTotalMessagesProcessed)
                .description("Total number of messages processed")
                .register(meterRegistry);
    }

    @Bean
    public Gauge totalErrorsGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("iso20022.errors.total", this, MetricsConfiguration::getTotalErrors)
                .description("Total number of errors")
                .register(meterRegistry);
    }

    @Bean
    public Counter webhookDeliveryCounter(MeterRegistry meterRegistry) {
        return Counter.builder("webhook.delivery.attempts")
                .description("Number of webhook delivery attempts")
                .register(meterRegistry);
    }

    @Bean
    public Counter webhookDeliverySuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("webhook.delivery.success")
                .description("Number of successful webhook deliveries")
                .register(meterRegistry);
    }

    @Bean
    public Counter webhookDeliveryFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("webhook.delivery.failure")
                .description("Number of failed webhook deliveries")
                .register(meterRegistry);
    }

    @Bean
    public Timer webhookDeliveryTimer(MeterRegistry meterRegistry) {
        return Timer.builder("webhook.delivery.time")
                .description("Time taken for webhook delivery")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageSentCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.messages.sent")
                .description("Number of messages sent to Kafka")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageReceivedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.messages.received")
                .description("Number of messages received from Kafka")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.messages.errors")
                .description("Number of Kafka message processing errors")
                .register(meterRegistry);
    }

    @Bean
    public Timer kafkaMessageProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("kafka.messages.processing.time")
                .description("Time taken to process Kafka messages")
                .register(meterRegistry);
    }

    @Bean
    public Counter circuitBreakerOpenCounter(MeterRegistry meterRegistry) {
        return Counter.builder("circuit.breaker.open")
                .description("Number of times circuit breaker opened")
                .register(meterRegistry);
    }

    @Bean
    public Counter circuitBreakerClosedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("circuit.breaker.closed")
                .description("Number of times circuit breaker closed")
                .register(meterRegistry);
    }

    @Bean
    public Counter circuitBreakerHalfOpenCounter(MeterRegistry meterRegistry) {
        return Counter.builder("circuit.breaker.half.open")
                .description("Number of times circuit breaker went half-open")
                .register(meterRegistry);
    }

    // Gauge value methods
    public double getActiveConnections() { return activeConnections.get(); }

    public double getTotalMessagesProcessed() { return totalMessagesProcessed.get(); }

    public double getTotalErrors() { return totalErrors.get(); }

    // Methods to update gauge values
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }

    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }

    public void incrementTotalMessagesProcessed() {
        totalMessagesProcessed.incrementAndGet();
    }

    public void incrementTotalErrors() {
        totalErrors.incrementAndGet();
    }
}