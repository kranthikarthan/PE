package com.payments.telemetry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Service for managing structured logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoggingService {

    private final LoggingConfig.LoggingContext loggingContext;

    /**
     * Add correlation ID to logging context
     */
    public void setCorrelationId(String correlationId) {
        MDC.put("correlationId", correlationId);
        loggingContext.addContext("correlationId", correlationId);
    }

    /**
     * Generate and set correlation ID
     */
    public String generateAndSetCorrelationId() {
        String correlationId = UUID.randomUUID().toString();
        setCorrelationId(correlationId);
        return correlationId;
    }

    /**
     * Add tenant context to logging
     */
    public void setTenantContext(String tenantId, String businessUnitId) {
        MDC.put("tenantId", tenantId);
        MDC.put("businessUnitId", businessUnitId);
        loggingContext.addContext("tenantId", tenantId);
        loggingContext.addContext("businessUnitId", businessUnitId);
    }

    /**
     * Add payment context to logging
     */
    public void setPaymentContext(String paymentId, String paymentType) {
        MDC.put("paymentId", paymentId);
        MDC.put("paymentType", paymentType);
        loggingContext.addContext("paymentId", paymentId);
        loggingContext.addContext("paymentType", paymentType);
    }

    /**
     * Add saga context to logging
     */
    public void setSagaContext(String sagaId, String sagaName, String stepName) {
        MDC.put("sagaId", sagaId);
        MDC.put("sagaName", sagaName);
        MDC.put("stepName", stepName);
        loggingContext.addContext("sagaId", sagaId);
        loggingContext.addContext("sagaName", sagaName);
        loggingContext.addContext("stepName", stepName);
    }

    /**
     * Add transaction context to logging
     */
    public void setTransactionContext(String transactionId, String transactionType) {
        MDC.put("transactionId", transactionId);
        MDC.put("transactionType", transactionType);
        loggingContext.addContext("transactionId", transactionId);
        loggingContext.addContext("transactionType", transactionType);
    }

    /**
     * Add custom context to logging
     */
    public void addContext(String key, String value) {
        MDC.put(key, value);
        loggingContext.addContext(key, value);
    }

    /**
     * Add multiple context values
     */
    public void addContext(Map<String, String> context) {
        context.forEach(this::addContext);
    }

    /**
     * Remove context from logging
     */
    public void removeContext(String key) {
        MDC.remove(key);
        loggingContext.removeContext(key);
    }

    /**
     * Clear all context
     */
    public void clearContext() {
        MDC.clear();
        loggingContext.clearContext();
    }

    /**
     * Execute code with specific context
     */
    public <T> T executeWithContext(Map<String, String> context, Supplier<T> operation) {
        try {
            addContext(context);
            return operation.get();
        } finally {
            context.keySet().forEach(this::removeContext);
        }
    }

    /**
     * Execute code with correlation ID
     */
    public <T> T executeWithCorrelationId(String correlationId, Supplier<T> operation) {
        try {
            setCorrelationId(correlationId);
            return operation.get();
        } finally {
            removeContext("correlationId");
        }
    }

    /**
     * Execute code with tenant context
     */
    public <T> T executeWithTenantContext(String tenantId, String businessUnitId, Supplier<T> operation) {
        try {
            setTenantContext(tenantId, businessUnitId);
            return operation.get();
        } finally {
            removeContext("tenantId");
            removeContext("businessUnitId");
        }
    }

    /**
     * Execute code with payment context
     */
    public <T> T executeWithPaymentContext(String paymentId, String paymentType, Supplier<T> operation) {
        try {
            setPaymentContext(paymentId, paymentType);
            return operation.get();
        } finally {
            removeContext("paymentId");
            removeContext("paymentType");
        }
    }

    /**
     * Execute code with saga context
     */
    public <T> T executeWithSagaContext(String sagaId, String sagaName, String stepName, Supplier<T> operation) {
        try {
            setSagaContext(sagaId, sagaName, stepName);
            return operation.get();
        } finally {
            removeContext("sagaId");
            removeContext("sagaName");
            removeContext("stepName");
        }
    }

    /**
     * Log payment initiation
     */
    public void logPaymentInitiated(String paymentId, String tenantId, String paymentType, String amount) {
        log.info("Payment initiated: paymentId={}, tenantId={}, paymentType={}, amount={}", 
                paymentId, tenantId, paymentType, amount);
    }

    /**
     * Log payment completion
     */
    public void logPaymentCompleted(String paymentId, String tenantId, String status) {
        log.info("Payment completed: paymentId={}, tenantId={}, status={}", 
                paymentId, tenantId, status);
    }

    /**
     * Log payment failure
     */
    public void logPaymentFailed(String paymentId, String tenantId, String errorType, String errorMessage) {
        log.error("Payment failed: paymentId={}, tenantId={}, errorType={}, errorMessage={}", 
                paymentId, tenantId, errorType, errorMessage);
    }

    /**
     * Log saga started
     */
    public void logSagaStarted(String sagaId, String templateName, String paymentId) {
        log.info("Saga started: sagaId={}, templateName={}, paymentId={}", 
                sagaId, templateName, paymentId);
    }

    /**
     * Log saga step started
     */
    public void logSagaStepStarted(String sagaId, String stepName, String stepType) {
        log.info("Saga step started: sagaId={}, stepName={}, stepType={}", 
                sagaId, stepName, stepType);
    }

    /**
     * Log saga step completed
     */
    public void logSagaStepCompleted(String sagaId, String stepName, String stepType) {
        log.info("Saga step completed: sagaId={}, stepName={}, stepType={}", 
                sagaId, stepName, stepType);
    }

    /**
     * Log saga step failed
     */
    public void logSagaStepFailed(String sagaId, String stepName, String stepType, String errorMessage) {
        log.error("Saga step failed: sagaId={}, stepName={}, stepType={}, errorMessage={}", 
                sagaId, stepName, stepType, errorMessage);
    }

    /**
     * Log saga completed
     */
    public void logSagaCompleted(String sagaId, String templateName, String paymentId) {
        log.info("Saga completed: sagaId={}, templateName={}, paymentId={}", 
                sagaId, templateName, paymentId);
    }

    /**
     * Log saga compensation started
     */
    public void logSagaCompensationStarted(String sagaId, String templateName, String reason) {
        log.warn("Saga compensation started: sagaId={}, templateName={}, reason={}", 
                sagaId, templateName, reason);
    }

    /**
     * Log saga compensated
     */
    public void logSagaCompensated(String sagaId, String templateName, String paymentId) {
        log.info("Saga compensated: sagaId={}, templateName={}, paymentId={}", 
                sagaId, templateName, paymentId);
    }

    /**
     * Log external service call
     */
    public void logExternalServiceCall(String serviceName, String operation, String endpoint) {
        log.debug("External service call: serviceName={}, operation={}, endpoint={}", 
                serviceName, operation, endpoint);
    }

    /**
     * Log external service call success
     */
    public void logExternalServiceCallSuccess(String serviceName, String operation, long duration) {
        log.debug("External service call success: serviceName={}, operation={}, duration={}ms", 
                serviceName, operation, duration);
    }

    /**
     * Log external service call failure
     */
    public void logExternalServiceCallFailure(String serviceName, String operation, String errorMessage) {
        log.error("External service call failure: serviceName={}, operation={}, errorMessage={}", 
                serviceName, operation, errorMessage);
    }

    /**
     * Log database operation
     */
    public void logDatabaseOperation(String operation, String table, String entityId) {
        log.debug("Database operation: operation={}, table={}, entityId={}", 
                operation, table, entityId);
    }

    /**
     * Log cache operation
     */
    public void logCacheOperation(String operation, String cacheName, String key) {
        log.debug("Cache operation: operation={}, cacheName={}, key={}", 
                operation, cacheName, key);
    }

    /**
     * Log Kafka operation
     */
    public void logKafkaOperation(String operation, String topic, String key) {
        log.debug("Kafka operation: operation={}, topic={}, key={}", 
                operation, topic, key);
    }

    /**
     * Log business event
     */
    public void logBusinessEvent(String eventType, String entityType, String entityId, Map<String, Object> data) {
        log.info("Business event: eventType={}, entityType={}, entityId={}, data={}", 
                eventType, entityType, entityId, data);
    }

    /**
     * Log security event
     */
    public void logSecurityEvent(String eventType, String userId, String action, String resource) {
        log.warn("Security event: eventType={}, userId={}, action={}, resource={}", 
                eventType, userId, action, resource);
    }

    /**
     * Log performance metric
     */
    public void logPerformanceMetric(String metricName, long value, String unit, Map<String, String> tags) {
        log.info("Performance metric: metricName={}, value={}, unit={}, tags={}", 
                metricName, value, unit, tags);
    }
}






