package com.paymentengine.corebanking.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Payment Engine Metrics for High TPS Monitoring
 * 
 * Comprehensive metrics collection for tracking performance,
 * business metrics, and system health for 2000+ TPS operations.
 */
@Component
public class PaymentEngineMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // TPS Metrics
    private final Counter tpsCounter;
    private final Timer responseTime;
    private final Gauge activeTransactions;
    
    // Error Metrics
    private final Counter errorCounter;
    private final Counter timeoutCounter;
    private final Counter validationErrorCounter;
    private final Counter accountErrorCounter;
    
    // Resource Metrics
    private final Gauge memoryUsage;
    private final Gauge cpuUsage;
    private final Gauge dbConnections;
    private final Gauge cacheHitRate;
    
    // Business Metrics
    private final Counter totalAmountProcessed;
    private final Counter totalFeesCollected;
    private final Gauge averageTransactionAmount;
    private final Counter highValueTransactions;
    
    // Performance Metrics
    private final Timer databaseQueryTime;
    private final Timer cacheOperationTime;
    private final Timer externalApiCallTime;
    private final Timer kafkaPublishTime;
    
    // System Health Metrics
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong queuedTransactions = new AtomicLong(0);
    private final AtomicLong processingTransactions = new AtomicLong(0);
    
    @Autowired
    public PaymentEngineMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // TPS Metrics
        this.tpsCounter = Counter.builder("payment_engine.transactions.processed")
            .description("Total number of transactions processed")
            .tag("service", "core-banking")
            .register(meterRegistry);
            
        this.responseTime = Timer.builder("payment_engine.transactions.response_time")
            .description("Transaction response time")
            .tag("service", "core-banking")
            .register(meterRegistry);
            
        this.activeTransactions = Gauge.builder("payment_engine.transactions.active")
            .description("Number of active transactions")
            .tag("service", "core-banking")
            .register(meterRegistry, this, PaymentEngineMetrics::getActiveTransactions);
        
        // Error Metrics
        this.errorCounter = Counter.builder("payment_engine.errors.total")
            .description("Total number of errors")
            .tag("service", "core-banking")
            .register(meterRegistry);
            
        this.timeoutCounter = Counter.builder("payment_engine.errors.timeout")
            .description("Number of timeout errors")
            .tag("service", "core-banking")
            .register(meterRegistry);
            
        this.validationErrorCounter = Counter.builder("payment_engine.errors.validation")
            .description("Number of validation errors")
            .tag("service", "core-banking")
            .register(meterRegistry);
            
        this.accountErrorCounter = Counter.builder("payment_engine.errors.account")
            .description("Number of account errors")
            .tag("service", "core-banking")
            .register(meterRegistry);
        
        // Resource Metrics
        this.memoryUsage = Gauge.builder("payment_engine.resources.memory_usage")
            .description("Memory usage percentage")
            .tag("service", "core-banking")
            .register(meterRegistry, this, PaymentEngineMetrics::getMemoryUsage);
            
        this.cpuUsage = Gauge.builder("payment_engine.resources.cpu_usage")
            .description("CPU usage percentage")
            .tag("service", "core-banking")
            .register(meterRegistry, this, PaymentEngineMetrics::getCpuUsage);
            
        this.dbConnections = Gauge.builder("payment_engine.resources.database_connections")
            .description("Active database connections")
            .tag("service", "core-banking")
            .register(meterRegistry, this, PaymentEngineMetrics::getDbConnections);
            
        this.cacheHitRate = Gauge.builder("payment_engine.resources.cache_hit_rate")
            .description("Cache hit rate percentage")
            .tag("service", "core-banking")
            .register(meterRegistry, this, PaymentEngineMetrics::getCacheHitRate);
        
        // Business Metrics
        this.totalAmountProcessed = Counter.builder("payment_engine.business.total_amount_processed")
            .description("Total amount processed")
            .tag("service", "core-banking")
            .register(meterRegistry);
            
        this.totalFeesCollected = Counter.builder("payment_engine.business.total_fees_collected")
            .description("Total fees collected")
            .tag("service", "core-banking")
            .register(meterRegistry);
            
        this.averageTransactionAmount = Gauge.builder("payment_engine.business.average_transaction_amount")
            .description("Average transaction amount")
            .tag("service", "core-banking")
            .register(meterRegistry, this, PaymentEngineMetrics::getAverageTransactionAmount);
            
        this.highValueTransactions = Counter.builder("payment_engine.business.high_value_transactions")
            .description("Number of high-value transactions")
            .tag("service", "core-banking")
            .register(meterRegistry);
        
        // Performance Metrics
        this.databaseQueryTime = Timer.builder("payment_engine.performance.database_query_time")
            .description("Database query execution time")
            .tag("service", "core-banking")
            .register(meterRegistry);
            
        this.cacheOperationTime = Timer.builder("payment_engine.performance.cache_operation_time")
            .description("Cache operation time")
            .tag("service", "core-banking")
            .register(meterRegistry);
            
        this.externalApiCallTime = Timer.builder("payment_engine.performance.external_api_call_time")
            .description("External API call time")
            .tag("service", "core-banking")
            .register(meterRegistry);
            
        this.kafkaPublishTime = Timer.builder("payment_engine.performance.kafka_publish_time")
            .description("Kafka message publish time")
            .tag("service", "core-banking")
            .register(meterRegistry);
    }
    
    // TPS Metrics Methods
    public void recordTransactionProcessed() {
        tpsCounter.increment();
    }
    
    public void recordTransactionProcessed(String tenantId, String paymentType) {
        tpsCounter.increment(
            Tags.of("tenant_id", tenantId, "payment_type", paymentType)
        );
    }
    
    public void recordResponseTime(Duration duration) {
        responseTime.record(duration);
    }
    
    public void recordResponseTime(Duration duration, String tenantId) {
        responseTime.record(duration, Tags.of("tenant_id", tenantId));
    }
    
    // Error Metrics Methods
    public void recordError(String errorType) {
        errorCounter.increment(Tags.of("error_type", errorType));
    }
    
    public void recordTimeout() {
        timeoutCounter.increment();
    }
    
    public void recordValidationError() {
        validationErrorCounter.increment();
    }
    
    public void recordAccountError() {
        accountErrorCounter.increment();
    }
    
    // Business Metrics Methods
    public void recordAmountProcessed(double amount, String currency) {
        totalAmountProcessed.increment(amount, Tags.of("currency", currency));
    }
    
    public void recordFeesCollected(double fees, String currency) {
        totalFeesCollected.increment(fees, Tags.of("currency", currency));
    }
    
    public void recordHighValueTransaction() {
        highValueTransactions.increment();
    }
    
    // Performance Metrics Methods
    public void recordDatabaseQueryTime(Duration duration) {
        databaseQueryTime.record(duration);
    }
    
    public void recordCacheOperationTime(Duration duration) {
        cacheOperationTime.record(duration);
    }
    
    public void recordExternalApiCallTime(Duration duration) {
        externalApiCallTime.record(duration);
    }
    
    public void recordKafkaPublishTime(Duration duration) {
        kafkaPublishTime.record(duration);
    }
    
    // System Health Metrics Methods
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }
    
    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }
    
    public void setQueuedTransactions(long count) {
        queuedTransactions.set(count);
    }
    
    public void setProcessingTransactions(long count) {
        processingTransactions.set(count);
    }
    
    // Gauge Methods
    private double getActiveTransactions() {
        return activeConnections.get();
    }
    
    private double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        return (double) usedMemory / maxMemory * 100;
    }
    
    private double getCpuUsage() {
        // This would typically use a system-specific implementation
        // For now, return a placeholder value
        return 0.0;
    }
    
    private double getDbConnections() {
        // This would typically query the connection pool
        // For now, return a placeholder value
        return 0.0;
    }
    
    private double getCacheHitRate() {
        // This would typically query the cache statistics
        // For now, return a placeholder value
        return 0.0;
    }
    
    private double getAverageTransactionAmount() {
        // This would typically calculate from recent transactions
        // For now, return a placeholder value
        return 0.0;
    }
    
    // Custom Metrics
    public void recordCustomMetric(String name, double value, String... tags) {
        Gauge.builder("payment_engine.custom." + name)
            .description("Custom metric: " + name)
            .tag("service", "core-banking")
            .register(meterRegistry, () -> value);
    }
    
    public void recordCustomCounter(String name, String... tags) {
        Counter.builder("payment_engine.custom." + name)
            .description("Custom counter: " + name)
            .tag("service", "core-banking")
            .register(meterRegistry)
            .increment();
    }
    
    public void recordCustomTimer(String name, Duration duration, String... tags) {
        Timer.builder("payment_engine.custom." + name)
            .description("Custom timer: " + name)
            .tag("service", "core-banking")
            .register(meterRegistry)
            .record(duration);
    }
}