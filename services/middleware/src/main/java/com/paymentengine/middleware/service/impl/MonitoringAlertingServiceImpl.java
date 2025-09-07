package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.service.MonitoringAlertingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of comprehensive monitoring and alerting service
 */
@Service
public class MonitoringAlertingServiceImpl implements MonitoringAlertingService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringAlertingServiceImpl.class);

    private final MeterRegistry meterRegistry;
    private final Counter messageProcessedCounter;
    private final Counter messageErrorsCounter;
    private final Timer messageProcessingTimer;
    private final Counter webhookDeliveryCounter;
    private final Counter webhookDeliverySuccessCounter;
    private final Counter webhookDeliveryFailureCounter;
    private final Timer webhookDeliveryTimer;
    private final Counter kafkaMessageSentCounter;
    private final Counter kafkaMessageReceivedCounter;
    private final Counter kafkaMessageErrorCounter;
    private final Timer kafkaMessageProcessingTimer;
    private final Counter circuitBreakerOpenCounter;
    private final Counter circuitBreakerClosedCounter;
    private final Counter circuitBreakerHalfOpenCounter;

    // In-memory storage for alerts and metrics (in production, use a proper database)
    private final Map<String, Alert> alerts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> messageCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final List<Long> processingTimes = Collections.synchronizedList(new ArrayList<>());

    public MonitoringAlertingServiceImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters and timers
        this.messageProcessedCounter = Counter.builder("iso20022.messages.processed")
                .description("Total number of ISO 20022 messages processed")
                .register(meterRegistry);
        
        this.messageErrorsCounter = Counter.builder("iso20022.messages.errors")
                .description("Total number of message processing errors")
                .register(meterRegistry);
        
        this.messageProcessingTimer = Timer.builder("iso20022.messages.processing.time")
                .description("Time taken to process ISO 20022 messages")
                .register(meterRegistry);
        
        this.webhookDeliveryCounter = Counter.builder("webhook.delivery.attempts")
                .description("Number of webhook delivery attempts")
                .register(meterRegistry);
        
        this.webhookDeliverySuccessCounter = Counter.builder("webhook.delivery.success")
                .description("Number of successful webhook deliveries")
                .register(meterRegistry);
        
        this.webhookDeliveryFailureCounter = Counter.builder("webhook.delivery.failure")
                .description("Number of failed webhook deliveries")
                .register(meterRegistry);
        
        this.webhookDeliveryTimer = Timer.builder("webhook.delivery.time")
                .description("Time taken for webhook delivery")
                .register(meterRegistry);
        
        this.kafkaMessageSentCounter = Counter.builder("kafka.messages.sent")
                .description("Number of messages sent to Kafka")
                .register(meterRegistry);
        
        this.kafkaMessageReceivedCounter = Counter.builder("kafka.messages.received")
                .description("Number of messages received from Kafka")
                .register(meterRegistry);
        
        this.kafkaMessageErrorCounter = Counter.builder("kafka.messages.errors")
                .description("Number of Kafka message processing errors")
                .register(meterRegistry);
        
        this.kafkaMessageProcessingTimer = Timer.builder("kafka.messages.processing.time")
                .description("Time taken to process Kafka messages")
                .register(meterRegistry);
        
        this.circuitBreakerOpenCounter = Counter.builder("circuit.breaker.open")
                .description("Number of times circuit breaker opened")
                .register(meterRegistry);
        
        this.circuitBreakerClosedCounter = Counter.builder("circuit.breaker.closed")
                .description("Number of times circuit breaker closed")
                .register(meterRegistry);
        
        this.circuitBreakerHalfOpenCounter = Counter.builder("circuit.breaker.half.open")
                .description("Number of times circuit breaker went half-open")
                .register(meterRegistry);
    }

    @Override
    public void recordMessageProcessingMetrics(String messageType, String tenantId, 
                                             String clearingSystemCode, long processingTimeMs, 
                                             boolean success, String correlationId) {
        try {
            // Record basic metrics
            messageProcessedCounter.increment(
                    Tags.of("messageType", messageType, "tenantId", tenantId, 
                           "clearingSystemCode", clearingSystemCode, "success", String.valueOf(success))
            );
            
            if (!success) {
                messageErrorsCounter.increment(
                        Tags.of("messageType", messageType, "tenantId", tenantId, 
                               "clearingSystemCode", clearingSystemCode)
                );
            }
            
            // Record processing time
            messageProcessingTimer.record(processingTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            // Update in-memory counters
            String key = tenantId + ":" + messageType;
            messageCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
            
            if (!success) {
                errorCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
            }
            
            // Store processing time for statistics
            processingTimes.add(processingTimeMs);
            
            // Keep only last 1000 processing times to avoid memory issues
            if (processingTimes.size() > 1000) {
                processingTimes.remove(0);
            }
            
            logger.debug("Recorded message processing metrics: messageType={}, tenantId={}, processingTime={}ms, success={}", 
                    messageType, tenantId, processingTimeMs, success);
            
        } catch (Exception e) {
            logger.error("Error recording message processing metrics", e);
        }
    }

    @Override
    public void recordClearingSystemMetrics(String clearingSystemCode, String endpointType, 
                                          long responseTimeMs, int statusCode, boolean success, 
                                          String correlationId) {
        try {
            Timer.Sample sample = Timer.start(meterRegistry);
            sample.stop(Timer.builder("clearing.system.response.time")
                    .description("Time taken for clearing system responses")
                    .tag("clearingSystemCode", clearingSystemCode)
                    .tag("endpointType", endpointType)
                    .tag("statusCode", String.valueOf(statusCode))
                    .tag("success", String.valueOf(success))
                    .register(meterRegistry));
            
            logger.debug("Recorded clearing system metrics: clearingSystemCode={}, responseTime={}ms, success={}", 
                    clearingSystemCode, responseTimeMs, success);
            
        } catch (Exception e) {
            logger.error("Error recording clearing system metrics", e);
        }
    }

    @Override
    public void recordTransformationMetrics(String fromMessageType, String toMessageType, 
                                          long transformationTimeMs, boolean success, 
                                          String correlationId) {
        try {
            Timer.Sample sample = Timer.start(meterRegistry);
            sample.stop(Timer.builder("message.transformation.time")
                    .description("Time taken for message transformation")
                    .tag("fromMessageType", fromMessageType)
                    .tag("toMessageType", toMessageType)
                    .tag("success", String.valueOf(success))
                    .register(meterRegistry));
            
            logger.debug("Recorded transformation metrics: from={}, to={}, time={}ms, success={}", 
                    fromMessageType, toMessageType, transformationTimeMs, success);
            
        } catch (Exception e) {
            logger.error("Error recording transformation metrics", e);
        }
    }

    @Override
    public void recordWebhookMetrics(String webhookUrl, long deliveryTimeMs, int statusCode, 
                                   boolean success, String correlationId) {
        try {
            webhookDeliveryCounter.increment(
                    Tags.of("webhookUrl", webhookUrl, "statusCode", String.valueOf(statusCode))
            );
            
            if (success) {
                webhookDeliverySuccessCounter.increment(Tags.of("webhookUrl", webhookUrl));
            } else {
                webhookDeliveryFailureCounter.increment(Tags.of("webhookUrl", webhookUrl));
            }
            
            webhookDeliveryTimer.record(deliveryTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            logger.debug("Recorded webhook metrics: webhookUrl={}, deliveryTime={}ms, success={}", 
                    webhookUrl, deliveryTimeMs, success);
            
        } catch (Exception e) {
            logger.error("Error recording webhook metrics", e);
        }
    }

    @Override
    public void recordKafkaMetrics(String topic, long productionTimeMs, boolean success, 
                                 String correlationId) {
        try {
            if (success) {
                kafkaMessageSentCounter.increment(Tags.of("topic", topic));
            } else {
                kafkaMessageErrorCounter.increment(Tags.of("topic", topic));
            }
            
            kafkaMessageProcessingTimer.record(productionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            logger.debug("Recorded Kafka metrics: topic={}, productionTime={}ms, success={}", 
                    topic, productionTimeMs, success);
            
        } catch (Exception e) {
            logger.error("Error recording Kafka metrics", e);
        }
    }

    @Override
    public CompletableFuture<List<Alert>> checkAlertConditions() {
        return CompletableFuture.supplyAsync(() -> {
            List<Alert> newAlerts = new ArrayList<>();
            
            try {
                // Check for high error rates
                checkErrorRateAlerts(newAlerts);
                
                // Check for slow processing times
                checkProcessingTimeAlerts(newAlerts);
                
                // Check for circuit breaker states
                checkCircuitBreakerAlerts(newAlerts);
                
                // Check for webhook delivery failures
                checkWebhookDeliveryAlerts(newAlerts);
                
                // Store new alerts
                newAlerts.forEach(alert -> alerts.put(alert.getId(), alert));
                
                logger.debug("Checked alert conditions, found {} new alerts", newAlerts.size());
                
            } catch (Exception e) {
                logger.error("Error checking alert conditions", e);
            }
            
            return newAlerts;
        });
    }

    @Override
    public CompletableFuture<Boolean> sendAlert(Alert alert) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // In a real implementation, this would send alerts via email, SMS, Slack, etc.
                logger.warn("ALERT: {} - {} - {}", alert.getSeverity(), alert.getTitle(), alert.getDescription());
                
                // Store the alert
                alerts.put(alert.getId(), alert);
                
                return true;
            } catch (Exception e) {
                logger.error("Error sending alert", e);
                return false;
            }
        });
    }

    @Override
    public SystemHealthStatus getSystemHealthStatus() {
        try {
            Map<String, String> components = new HashMap<>();
            components.put("database", "UP");
            components.put("kafka", "UP");
            components.put("redis", "UP");
            components.put("clearing-systems", "UP");
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalMessages", messageCounts.values().stream().mapToLong(AtomicLong::get).sum());
            metrics.put("totalErrors", errorCounts.values().stream().mapToLong(AtomicLong::get).sum());
            metrics.put("averageProcessingTime", calculateAverageProcessingTime());
            metrics.put("activeAlerts", alerts.values().stream().filter(alert -> !alert.isAcknowledged()).count());
            
            String status = "UP";
            if (metrics.get("totalErrors") != null && (Long) metrics.get("totalErrors") > 100) {
                status = "DEGRADED";
            }
            if (metrics.get("activeAlerts") != null && (Long) metrics.get("activeAlerts") > 5) {
                status = "DOWN";
            }
            
            return new SystemHealthStatus(status, components, metrics, System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Error getting system health status", e);
            return new SystemHealthStatus("UNKNOWN", Map.of(), Map.of(), System.currentTimeMillis());
        }
    }

    @Override
    public MetricsSummary getMetricsSummary(String tenantId, String timeRange) {
        try {
            long totalMessages = messageCounts.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(tenantId + ":"))
                    .mapToLong(entry -> entry.getValue().get())
                    .sum();
            
            long totalErrors = errorCounts.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(tenantId + ":"))
                    .mapToLong(entry -> entry.getValue().get())
                    .sum();
            
            long successfulMessages = totalMessages - totalErrors;
            double successRate = totalMessages > 0 ? (double) successfulMessages / totalMessages * 100 : 0;
            double averageProcessingTime = calculateAverageProcessingTime();
            
            Map<String, Long> messageTypeCounts = new HashMap<>();
            Map<String, Long> clearingSystemCounts = new HashMap<>();
            
            return new MetricsSummary(tenantId, timeRange, totalMessages, successfulMessages, 
                    totalErrors, successRate, averageProcessingTime, messageTypeCounts, 
                    clearingSystemCounts, System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Error getting metrics summary", e);
            return new MetricsSummary(tenantId, timeRange, 0, 0, 0, 0, 0, 
                    Map.of(), Map.of(), System.currentTimeMillis());
        }
    }

    @Override
    public PerformanceMetrics getPerformanceMetrics(String tenantId, String messageType, String timeRange) {
        try {
            double averageProcessingTime = calculateAverageProcessingTime();
            double p50ProcessingTime = calculatePercentile(50);
            double p95ProcessingTime = calculatePercentile(95);
            double p99ProcessingTime = calculatePercentile(99);
            double maxProcessingTime = processingTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            double minProcessingTime = processingTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            
            return new PerformanceMetrics(tenantId, messageType, timeRange, averageProcessingTime,
                    p50ProcessingTime, p95ProcessingTime, p99ProcessingTime, maxProcessingTime,
                    minProcessingTime, System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Error getting performance metrics", e);
            return new PerformanceMetrics(tenantId, messageType, timeRange, 0, 0, 0, 0, 0, 0, 
                    System.currentTimeMillis());
        }
    }

    @Override
    public ErrorMetrics getErrorMetrics(String tenantId, String timeRange) {
        try {
            long totalErrors = errorCounts.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(tenantId + ":"))
                    .mapToLong(entry -> entry.getValue().get())
                    .sum();
            
            Map<String, Long> errorTypeCounts = new HashMap<>();
            Map<String, Long> errorMessageCounts = new HashMap<>();
            Map<String, Long> clearingSystemErrorCounts = new HashMap<>();
            
            double errorRate = 0; // Calculate based on total messages
            
            return new ErrorMetrics(tenantId, timeRange, totalErrors, errorTypeCounts,
                    errorMessageCounts, clearingSystemErrorCounts, errorRate, System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Error getting error metrics", e);
            return new ErrorMetrics(tenantId, timeRange, 0, Map.of(), Map.of(), Map.of(), 0, 
                    System.currentTimeMillis());
        }
    }

    @Override
    public ThroughputMetrics getThroughputMetrics(String tenantId, String timeRange) {
        try {
            // Calculate throughput based on time range
            double messagesPerSecond = 0;
            double messagesPerMinute = 0;
            double messagesPerHour = 0;
            
            Map<String, Double> messageTypeThroughput = new HashMap<>();
            Map<String, Double> clearingSystemThroughput = new HashMap<>();
            
            return new ThroughputMetrics(tenantId, timeRange, messagesPerSecond, messagesPerMinute,
                    messagesPerHour, messageTypeThroughput, clearingSystemThroughput, System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Error getting throughput metrics", e);
            return new ThroughputMetrics(tenantId, timeRange, 0, 0, 0, Map.of(), Map.of(), 
                    System.currentTimeMillis());
        }
    }

    // Helper methods
    private void checkErrorRateAlerts(List<Alert> newAlerts) {
        // Check if error rate is above threshold
        long totalErrors = errorCounts.values().stream().mapToLong(AtomicLong::get).sum();
        long totalMessages = messageCounts.values().stream().mapToLong(AtomicLong::get).sum();
        
        if (totalMessages > 0) {
            double errorRate = (double) totalErrors / totalMessages * 100;
            if (errorRate > 5.0) { // 5% error rate threshold
                Alert alert = new Alert(
                        UUID.randomUUID().toString(),
                        "HIGH_ERROR_RATE",
                        "WARNING",
                        "High Error Rate Detected",
                        String.format("Error rate is %.2f%% (threshold: 5%%)", errorRate),
                        "system",
                        "all",
                        "all",
                        Map.of("errorRate", errorRate, "totalErrors", totalErrors, "totalMessages", totalMessages),
                        System.currentTimeMillis()
                );
                newAlerts.add(alert);
            }
        }
    }

    private void checkProcessingTimeAlerts(List<Alert> newAlerts) {
        double averageProcessingTime = calculateAverageProcessingTime();
        if (averageProcessingTime > 5000) { // 5 second threshold
            Alert alert = new Alert(
                    UUID.randomUUID().toString(),
                    "SLOW_PROCESSING",
                    "WARNING",
                    "Slow Processing Time Detected",
                    String.format("Average processing time is %.2f ms (threshold: 5000 ms)", averageProcessingTime),
                    "system",
                    "all",
                    "all",
                    Map.of("averageProcessingTime", averageProcessingTime),
                    System.currentTimeMillis()
            );
            newAlerts.add(alert);
        }
    }

    private void checkCircuitBreakerAlerts(List<Alert> newAlerts) {
        // This would check actual circuit breaker states in a real implementation
    }

    private void checkWebhookDeliveryAlerts(List<Alert> newAlerts) {
        // This would check webhook delivery failure rates in a real implementation
    }

    private double calculateAverageProcessingTime() {
        if (processingTimes.isEmpty()) {
            return 0;
        }
        return processingTimes.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private double calculatePercentile(int percentile) {
        if (processingTimes.isEmpty()) {
            return 0;
        }
        List<Long> sortedTimes = new ArrayList<>(processingTimes);
        Collections.sort(sortedTimes);
        int index = (int) Math.ceil(percentile / 100.0 * sortedTimes.size()) - 1;
        return sortedTimes.get(Math.max(0, index));
    }
}