package com.payments.payshapadapter.monitoring;

import com.payments.payshapadapter.repository.PayShapAdapterRepository;
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
 * PayShap Monitoring Service
 *
 * <p>Advanced monitoring service for PayShap adapter: - Custom metrics collection - Performance
 * monitoring - Health monitoring - Dashboard data preparation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayShapMonitoringService {

  private final MeterRegistry meterRegistry;
  private final PayShapAdapterRepository payShapAdapterRepository;

  // Custom metrics
  private final Counter payShapTransactionCounter;
  private final Counter payShapErrorCounter;
  private final Counter payShapComplianceCheckCounter;
  private final Counter payShapFraudDetectionCounter;
  private final Counter payShapRiskAssessmentCounter;
  private final Counter payShapP2PProcessingCounter;
  private final Timer payShapTransactionTimer;
  private final Timer payShapComplianceTimer;
  private final Timer payShapFraudDetectionTimer;
  private final Timer payShapRiskAssessmentTimer;
  private final Timer payShapP2PProcessingTimer;
  private final AtomicLong payShapActiveAdapters = new AtomicLong(0);
  private final AtomicLong payShapTotalTransactions = new AtomicLong(0);
  private final AtomicLong payShapSuccessfulTransactions = new AtomicLong(0);
  private final AtomicLong payShapFailedTransactions = new AtomicLong(0);

  public PayShapMonitoringService(
      MeterRegistry meterRegistry, PayShapAdapterRepository payShapAdapterRepository) {
    this.meterRegistry = meterRegistry;
    this.payShapAdapterRepository = payShapAdapterRepository;

    // Initialize counters
    this.payShapTransactionCounter =
        Counter.builder("payshap.transactions.total")
            .description("Total number of PayShap transactions")
            .register(meterRegistry);

    this.payShapErrorCounter =
        Counter.builder("payshap.errors.total")
            .description("Total number of PayShap errors")
            .register(meterRegistry);

    this.payShapComplianceCheckCounter =
        Counter.builder("payshap.compliance.checks.total")
            .description("Total number of PayShap compliance checks")
            .register(meterRegistry);

    this.payShapFraudDetectionCounter =
        Counter.builder("payshap.fraud.detections.total")
            .description("Total number of PayShap fraud detections")
            .register(meterRegistry);

    this.payShapRiskAssessmentCounter =
        Counter.builder("payshap.risk.assessments.total")
            .description("Total number of PayShap risk assessments")
            .register(meterRegistry);

    this.payShapP2PProcessingCounter =
        Counter.builder("payshap.p2p.processing.total")
            .description("Total number of PayShap P2P processing operations")
            .register(meterRegistry);

    // Initialize timers
    this.payShapTransactionTimer =
        Timer.builder("payshap.transactions.duration")
            .description("PayShap transaction processing duration")
            .register(meterRegistry);

    this.payShapComplianceTimer =
        Timer.builder("payshap.compliance.duration")
            .description("PayShap compliance check duration")
            .register(meterRegistry);

    this.payShapFraudDetectionTimer =
        Timer.builder("payshap.fraud.detection.duration")
            .description("PayShap fraud detection duration")
            .register(meterRegistry);

    this.payShapRiskAssessmentTimer =
        Timer.builder("payshap.risk.assessment.duration")
            .description("PayShap risk assessment duration")
            .register(meterRegistry);

    this.payShapP2PProcessingTimer =
        Timer.builder("payshap.p2p.processing.duration")
            .description("PayShap P2P processing duration")
            .register(meterRegistry);

    // Initialize gauges
    Gauge.builder("payshap.adapters.active", this, PayShapMonitoringService::getActiveAdapterCount)
        .description("Number of active PayShap adapters")
        .register(meterRegistry);

    Gauge.builder(
            "payshap.transactions.success.rate", this, PayShapMonitoringService::getSuccessRate)
        .description("PayShap transaction success rate")
        .register(meterRegistry);
  }

  /** Record transaction metrics */
  public void recordTransaction(String transactionId, boolean success, long durationMs) {
    payShapTransactionCounter.increment();
    payShapTotalTransactions.incrementAndGet();

    if (success) {
      payShapSuccessfulTransactions.incrementAndGet();
    } else {
      payShapFailedTransactions.incrementAndGet();
      payShapErrorCounter.increment();
    }

    payShapTransactionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded PayShap transaction metrics: transactionId={}, success={}, duration={}ms",
        transactionId,
        success,
        durationMs);
  }

  /** Record compliance check metrics */
  public void recordComplianceCheck(String adapterId, boolean compliant, long durationMs) {
    payShapComplianceCheckCounter.increment();
    payShapComplianceTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded PayShap compliance check metrics: adapterId={}, compliant={}, duration={}ms",
        adapterId,
        compliant,
        durationMs);
  }

  /** Record fraud detection metrics */
  public void recordFraudDetection(String adapterId, boolean fraudDetected, long durationMs) {
    payShapFraudDetectionCounter.increment();
    payShapFraudDetectionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded PayShap fraud detection metrics: adapterId={}, fraudDetected={}, duration={}ms",
        adapterId,
        fraudDetected,
        durationMs);
  }

  /** Record risk assessment metrics */
  public void recordRiskAssessment(String adapterId, String riskLevel, long durationMs) {
    payShapRiskAssessmentCounter.increment();
    payShapRiskAssessmentTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded PayShap risk assessment metrics: adapterId={}, riskLevel={}, duration={}ms",
        adapterId,
        riskLevel,
        durationMs);
  }

  /** Record P2P processing metrics */
  public void recordP2PProcessing(String transactionId, long durationMs) {
    payShapP2PProcessingCounter.increment();
    payShapP2PProcessingTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded PayShap P2P processing metrics: transactionId={}, duration={}ms",
        transactionId,
        durationMs);
  }

  /** Get dashboard metrics */
  public Map<String, Object> getDashboardMetrics() {
    Map<String, Object> metrics = new HashMap<>();

    // Transaction metrics
    metrics.put("totalTransactions", payShapTotalTransactions.get());
    metrics.put("successfulTransactions", payShapSuccessfulTransactions.get());
    metrics.put("failedTransactions", payShapFailedTransactions.get());
    metrics.put("successRate", getSuccessRate());

    // Adapter metrics
    metrics.put("activeAdapters", getActiveAdapterCount());
    metrics.put("totalAdapters", payShapAdapterRepository.count());

    // Performance metrics
    metrics.put(
        "averageTransactionTime",
        payShapTransactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageComplianceTime",
        payShapComplianceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageFraudDetectionTime",
        payShapFraudDetectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageRiskAssessmentTime",
        payShapRiskAssessmentTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageP2PProcessingTime",
        payShapP2PProcessingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));

    // Counter metrics
    metrics.put("totalComplianceChecks", payShapComplianceCheckCounter.count());
    metrics.put("totalFraudDetections", payShapFraudDetectionCounter.count());
    metrics.put("totalRiskAssessments", payShapRiskAssessmentCounter.count());
    metrics.put("totalP2PProcessing", payShapP2PProcessingCounter.count());
    metrics.put("totalErrors", payShapErrorCounter.count());

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
    health.put("totalAdapters", payShapAdapterRepository.count());
    health.put("timestamp", Instant.now().toString());

    return health;
  }

  /** Get performance metrics */
  public Map<String, Object> getPerformanceMetrics() {
    Map<String, Object> performance = new HashMap<>();

    performance.put(
        "averageTransactionTime",
        payShapTransactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "maxTransactionTime",
        payShapTransactionTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageComplianceTime",
        payShapComplianceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageFraudDetectionTime",
        payShapFraudDetectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageRiskAssessmentTime",
        payShapRiskAssessmentTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageP2PProcessingTime",
        payShapP2PProcessingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("timestamp", Instant.now().toString());

    return performance;
  }

  /** Get active adapter count */
  private double getActiveAdapterCount() {
    return payShapAdapterRepository.countByStatus(
        com.payments.domain.clearing.AdapterOperationalStatus.ACTIVE);
  }

  /** Get success rate */
  private double getSuccessRate() {
    long total = payShapTotalTransactions.get();
    if (total == 0) {
      return 0.0;
    }
    return (double) payShapSuccessfulTransactions.get() / total;
  }
}
