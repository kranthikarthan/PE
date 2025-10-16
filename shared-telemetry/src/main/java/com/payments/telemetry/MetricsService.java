package com.payments.telemetry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Service for managing metrics collection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final MetricsConfig.PaymentMetrics paymentMetrics;

    /**
     * Increment a counter
     */
    public void incrementCounter(String name, String... tags) {
        Counter.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Increment a counter with tag map
     */
    public void incrementCounter(String name, Map<String, String> tags) {
        Counter.Builder builder = Counter.builder(name);
        tags.forEach((k, v) -> builder.tag(k, v));
        builder.register(meterRegistry).increment();
    }

    /**
     * Record a timer
     */
    public void recordTimer(String name, long duration, String... tags) {
        Timer.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Record a timer with tag map
     */
    public void recordTimer(String name, long duration, Map<String, String> tags) {
        Timer.Builder builder = Timer.builder(name);
        tags.forEach((k, v) -> builder.tag(k, v));
        builder.register(meterRegistry).record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Execute code and record timing
     */
    public <T> T recordTiming(String name, Supplier<T> operation) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return operation.get();
        } finally {
            sample.stop(Timer.builder(name).register(meterRegistry));
        }
    }

    /**
     * Execute code and record timing with tags
     */
    public <T> T recordTiming(String name, Map<String, String> tags, Supplier<T> operation) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return operation.get();
        } finally {
            Timer.Builder builder = Timer.builder(name);
            tags.forEach((k, v) -> builder.tag(k, v));
            sample.stop(builder.register(meterRegistry));
        }
    }

    /**
     * Record payment metrics
     */
    public void recordPaymentInitiated(String paymentId, String tenantId) {
        paymentMetrics.incrementPaymentsInitiated();
        addAttribute("payment.id", paymentId);
        addAttribute("tenant.id", tenantId);
    }

    /**
     * Record payment completion
     */
    public void recordPaymentCompleted(String paymentId, String tenantId) {
        paymentMetrics.incrementPaymentsCompleted();
        addAttribute("payment.id", paymentId);
        addAttribute("tenant.id", tenantId);
    }

    /**
     * Record payment failure
     */
    public void recordPaymentFailed(String paymentId, String tenantId, String errorType) {
        paymentMetrics.incrementPaymentsFailed();
        paymentMetrics.incrementErrors(errorType);
        addAttribute("payment.id", paymentId);
        addAttribute("tenant.id", tenantId);
        addAttribute("error.type", errorType);
    }

    /**
     * Record saga metrics
     */
    public void recordSagaStarted(String sagaId, String templateName) {
        paymentMetrics.incrementSagasStarted();
        addAttribute("saga.id", sagaId);
        addAttribute("saga.template", templateName);
    }

    /**
     * Record saga completion
     */
    public void recordSagaCompleted(String sagaId, String templateName) {
        paymentMetrics.incrementSagasCompleted();
        addAttribute("saga.id", sagaId);
        addAttribute("saga.template", templateName);
    }

    /**
     * Record saga compensation
     */
    public void recordSagaCompensated(String sagaId, String templateName) {
        paymentMetrics.incrementSagasCompensated();
        addAttribute("saga.id", sagaId);
        addAttribute("saga.template", templateName);
    }

    /**
     * Record step execution timing
     */
    public <T> T recordStepExecution(String stepName, String stepType, Supplier<T> operation) {
        return recordTiming("saga.step.execution.duration", 
                Map.of("step.name", stepName, "step.type", stepType), operation);
    }

    /**
     * Record external service call timing
     */
    public <T> T recordExternalServiceCall(String serviceName, String operation, Supplier<T> operationCode) {
        return recordTiming("external.service.call.duration",
                Map.of("service.name", serviceName, "operation", operation), operationCode);
    }

    /**
     * Record database operation timing
     */
    public <T> T recordDatabaseOperation(String operation, String table, Supplier<T> operationCode) {
        return recordTiming("database.operation.duration",
                Map.of("operation", operation, "table", table), operationCode);
    }

    /**
     * Record cache operation timing
     */
    public <T> T recordCacheOperation(String operation, String cacheName, Supplier<T> operationCode) {
        return recordTiming("cache.operation.duration",
                Map.of("operation", operation, "cache.name", cacheName), operationCode);
    }

    /**
     * Record Kafka operation timing
     */
    public <T> T recordKafkaOperation(String operation, String topic, Supplier<T> operationCode) {
        return recordTiming("kafka.operation.duration",
                Map.of("operation", operation, "topic", topic), operationCode);
    }

    /**
     * Record error metrics
     */
    public void recordError(String errorType, String errorMessage, String serviceName) {
        paymentMetrics.incrementErrors(errorType);
        addAttribute("error.type", errorType);
        addAttribute("error.message", errorMessage);
        addAttribute("service.name", serviceName);
    }

    /**
     * Record validation error
     */
    public void recordValidationError(String paymentId, String ruleName, String errorMessage) {
        recordError("validation", errorMessage, "validation-service");
        addAttribute("payment.id", paymentId);
        addAttribute("rule.name", ruleName);
    }

    /**
     * Record routing error
     */
    public void recordRoutingError(String paymentId, String errorMessage) {
        recordError("routing", errorMessage, "routing-service");
        addAttribute("payment.id", paymentId);
    }

    /**
     * Record transaction error
     */
    public void recordTransactionError(String transactionId, String errorMessage) {
        recordError("transaction", errorMessage, "transaction-processing-service");
        addAttribute("transaction.id", transactionId);
    }

    /**
     * Record saga error
     */
    public void recordSagaError(String sagaId, String stepName, String errorMessage) {
        recordError("saga", errorMessage, "saga-orchestrator");
        addAttribute("saga.id", sagaId);
        addAttribute("step.name", stepName);
    }

    /**
     * Add custom attribute to current context
     */
    private void addAttribute(String key, String value) {
        // This would typically be added to the current span or context
        // For now, we'll just log it
        log.debug("Adding metric attribute: {} = {}", key, value);
    }

    /**
     * Get meter registry
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    /**
     * Get payment metrics
     */
    public MetricsConfig.PaymentMetrics getPaymentMetrics() {
        return paymentMetrics;
    }
}
