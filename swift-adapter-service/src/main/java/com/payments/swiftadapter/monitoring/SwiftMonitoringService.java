package com.payments.swiftadapter.monitoring;

import com.payments.swiftadapter.repository.SwiftAdapterRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SWIFT Monitoring Service
 *
 * <p>Advanced monitoring service for SWIFT adapter: - Custom metrics collection - Performance
 * monitoring - Health monitoring - Dashboard data preparation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SwiftMonitoringService {

  private final MeterRegistry meterRegistry;
  private final SwiftAdapterRepository swiftAdapterRepository;

  // Custom metrics
  private final Counter swiftTransactionCounter;
  private final Counter swiftErrorCounter;
  private final Counter swiftComplianceCheckCounter;
  private final Counter swiftFraudDetectionCounter;
  private final Counter swiftRiskAssessmentCounter;
  private final Counter swiftInternationalProcessingCounter;
  private final Timer swiftTransactionTimer;
  private final Timer swiftComplianceTimer;
  private final Timer swiftFraudDetectionTimer;
  private final Timer swiftRiskAssessmentTimer;
  private final Timer swiftInternationalProcessingTimer;
  private final AtomicLong swiftActiveAdapters = new AtomicLong(0);
  private final AtomicLong swiftTotalTransactions = new AtomicLong(0);
  private final AtomicLong swiftSuccessfulTransactions = new AtomicLong(0);
  private final AtomicLong swiftFailedTransactions = new AtomicLong(0);

  public SwiftMonitoringService(
      MeterRegistry meterRegistry, SwiftAdapterRepository swiftAdapterRepository) {
    this.meterRegistry = meterRegistry;
    this.swiftAdapterRepository = swiftAdapterRepository;

    // Initialize counters
    this.swiftTransactionCounter =
        Counter.builder("swift.transactions.total")
            .description("Total number of SWIFT transactions")
            .register(meterRegistry);

    this.swiftErrorCounter =
        Counter.builder("swift.errors.total")
            .description("Total number of SWIFT errors")
            .register(meterRegistry);

    this.swiftComplianceCheckCounter =
        Counter.builder("swift.compliance.checks.total")
            .description("Total number of SWIFT compliance checks")
            .register(meterRegistry);

    this.swiftFraudDetectionCounter =
        Counter.builder("swift.fraud.detections.total")
            .description("Total number of SWIFT fraud detections")
            .register(meterRegistry);

    this.swiftRiskAssessmentCounter =
        Counter.builder("swift.risk.assessments.total")
            .description("Total number of SWIFT risk assessments")
            .register(meterRegistry);

    this.swiftInternationalProcessingCounter =
        Counter.builder("swift.international.processing.total")
            .description("Total number of SWIFT international processing operations")
            .register(meterRegistry);

    // Initialize timers
    this.swiftTransactionTimer =
        Timer.builder("swift.transactions.duration")
            .description("SWIFT transaction processing duration")
            .register(meterRegistry);

    this.swiftComplianceTimer =
        Timer.builder("swift.compliance.duration")
            .description("SWIFT compliance check duration")
            .register(meterRegistry);

    this.swiftFraudDetectionTimer =
        Timer.builder("swift.fraud.detection.duration")
            .description("SWIFT fraud detection duration")
            .register(meterRegistry);

    this.swiftRiskAssessmentTimer =
        Timer.builder("swift.risk.assessment.duration")
            .description("SWIFT risk assessment duration")
            .register(meterRegistry);

    this.swiftInternationalProcessingTimer =
        Timer.builder("swift.international.processing.duration")
            .description("SWIFT international processing duration")
            .register(meterRegistry);

    // Initialize gauges
    Gauge.builder("swift.adapters.active", this, SwiftMonitoringService::getActiveAdapterCount)
        .description("Number of active SWIFT adapters")
        .register(meterRegistry);

    Gauge.builder("swift.transactions.success.rate", this, SwiftMonitoringService::getSuccessRate)
        .description("SWIFT transaction success rate")
        .register(meterRegistry);
  }

  /** Record transaction metrics */
  public void recordTransaction(String transactionId, boolean success, long durationMs) {
    swiftTransactionCounter.increment();
    swiftTotalTransactions.incrementAndGet();

    if (success) {
      swiftSuccessfulTransactions.incrementAndGet();
    } else {
      swiftFailedTransactions.incrementAndGet();
      swiftErrorCounter.increment();
    }

    swiftTransactionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded SWIFT transaction metrics: transactionId={}, success={}, duration={}ms",
        transactionId,
        success,
        durationMs);
  }

  /** Record compliance check metrics */
  public void recordComplianceCheck(String adapterId, boolean compliant, long durationMs) {
    swiftComplianceCheckCounter.increment();
    swiftComplianceTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded SWIFT compliance check metrics: adapterId={}, compliant={}, duration={}ms",
        adapterId,
        compliant,
        durationMs);
  }

  /** Record fraud detection metrics */
  public void recordFraudDetection(String adapterId, boolean fraudDetected, long durationMs) {
    swiftFraudDetectionCounter.increment();
    swiftFraudDetectionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded SWIFT fraud detection metrics: adapterId={}, fraudDetected={}, duration={}ms",
        adapterId,
        fraudDetected,
        durationMs);
  }

  /** Record risk assessment metrics */
  public void recordRiskAssessment(String adapterId, String riskLevel, long durationMs) {
    swiftRiskAssessmentCounter.increment();
    swiftRiskAssessmentTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded SWIFT risk assessment metrics: adapterId={}, riskLevel={}, duration={}ms",
        adapterId,
        riskLevel,
        durationMs);
  }

  /** Record international processing metrics */
  public void recordInternationalProcessing(String transactionId, long durationMs) {
    swiftInternationalProcessingCounter.increment();
    swiftInternationalProcessingTimer.record(
        durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded SWIFT international processing metrics: transactionId={}, duration={}ms",
        transactionId,
        durationMs);
  }

  /** Get dashboard metrics */
  public Map<String, Object> getDashboardMetrics() {
    Map<String, Object> metrics = new HashMap<>();

    // Transaction metrics
    metrics.put("totalTransactions", swiftTotalTransactions.get());
    metrics.put("successfulTransactions", swiftSuccessfulTransactions.get());
    metrics.put("failedTransactions", swiftFailedTransactions.get());
    metrics.put("successRate", getSuccessRate());

    // Adapter metrics
    metrics.put("activeAdapters", getActiveAdapterCount());
    metrics.put("totalAdapters", swiftAdapterRepository.count());

    // Performance metrics
    metrics.put(
        "averageTransactionTime",
        swiftTransactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageComplianceTime",
        swiftComplianceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageFraudDetectionTime",
        swiftFraudDetectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageRiskAssessmentTime",
        swiftRiskAssessmentTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageInternationalProcessingTime",
        swiftInternationalProcessingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));

    // Counter metrics
    metrics.put("totalComplianceChecks", swiftComplianceCheckCounter.count());
    metrics.put("totalFraudDetections", swiftFraudDetectionCounter.count());
    metrics.put("totalRiskAssessments", swiftRiskAssessmentCounter.count());
    metrics.put("totalInternationalProcessing", swiftInternationalProcessingCounter.count());
    metrics.put("totalErrors", swiftErrorCounter.count());

    // Timestamp
    metrics.put("timestamp", Instant.now().toString());

    return metrics;
  }

  /** Get health metrics */
  public Map<String, Object> getHealthMetrics() {
    Map<String, Object> health = new HashMap<>();

    double activeAdapters = getActiveAdapterCount();
    double successRate = getSuccessRate();

    health.put("status", activeAdapters > 0 && successRate > 0.95 ? "HEALTHY" : "DEGRADED");
    health.put("activeAdapters", activeAdapters);
    health.put("successRate", successRate);
    health.put("totalAdapters", swiftAdapterRepository.count());
    health.put("timestamp", Instant.now().toString());

    return health;
  }

  /** Get performance metrics */
  public Map<String, Object> getPerformanceMetrics() {
    Map<String, Object> performance = new HashMap<>();

    performance.put(
        "averageTransactionTime",
        swiftTransactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "maxTransactionTime",
        swiftTransactionTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageComplianceTime",
        swiftComplianceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageFraudDetectionTime",
        swiftFraudDetectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageRiskAssessmentTime",
        swiftRiskAssessmentTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageInternationalProcessingTime",
        swiftInternationalProcessingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("timestamp", Instant.now().toString());

    return performance;
  }

  /** Get active adapter count */
  private double getActiveAdapterCount() {
    return swiftAdapterRepository.countByStatus(
        com.payments.domain.clearing.AdapterOperationalStatus.ACTIVE);
  }

  /** Get success rate */
  private double getSuccessRate() {
    long total = swiftTotalTransactions.get();
    if (total == 0) {
      return 0.0;
    }
    return (double) swiftSuccessfulTransactions.get() / total;
  }
}
