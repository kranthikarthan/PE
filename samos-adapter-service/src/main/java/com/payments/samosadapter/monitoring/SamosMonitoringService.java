package com.payments.samosadapter.monitoring;

import com.payments.samosadapter.repository.SamosAdapterRepository;
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
 * SAMOS Monitoring Service
 *
 * <p>Advanced monitoring service for SAMOS adapter: - Custom metrics collection - Performance
 * monitoring - Health monitoring - Dashboard data preparation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamosMonitoringService {

  private final MeterRegistry meterRegistry;
  private final SamosAdapterRepository samosAdapterRepository;

  // Custom metrics
  private final Counter samosTransactionCounter;
  private final Counter samosErrorCounter;
  private final Counter samosComplianceCheckCounter;
  private final Counter samosFraudDetectionCounter;
  private final Counter samosRiskAssessmentCounter;
  private final Timer samosTransactionTimer;
  private final Timer samosComplianceTimer;
  private final Timer samosFraudDetectionTimer;
  private final Timer samosRiskAssessmentTimer;
  private final AtomicLong samosActiveAdapters = new AtomicLong(0);
  private final AtomicLong samosTotalTransactions = new AtomicLong(0);
  private final AtomicLong samosSuccessfulTransactions = new AtomicLong(0);
  private final AtomicLong samosFailedTransactions = new AtomicLong(0);

  public SamosMonitoringService(
      MeterRegistry meterRegistry, SamosAdapterRepository samosAdapterRepository) {
    this.meterRegistry = meterRegistry;
    this.samosAdapterRepository = samosAdapterRepository;

    // Initialize counters
    this.samosTransactionCounter =
        Counter.builder("samos.transactions.total")
            .description("Total number of SAMOS transactions")
            .register(meterRegistry);

    this.samosErrorCounter =
        Counter.builder("samos.errors.total")
            .description("Total number of SAMOS errors")
            .register(meterRegistry);

    this.samosComplianceCheckCounter =
        Counter.builder("samos.compliance.checks.total")
            .description("Total number of SAMOS compliance checks")
            .register(meterRegistry);

    this.samosFraudDetectionCounter =
        Counter.builder("samos.fraud.detections.total")
            .description("Total number of SAMOS fraud detections")
            .register(meterRegistry);

    this.samosRiskAssessmentCounter =
        Counter.builder("samos.risk.assessments.total")
            .description("Total number of SAMOS risk assessments")
            .register(meterRegistry);

    // Initialize timers
    this.samosTransactionTimer =
        Timer.builder("samos.transactions.duration")
            .description("SAMOS transaction processing duration")
            .register(meterRegistry);

    this.samosComplianceTimer =
        Timer.builder("samos.compliance.duration")
            .description("SAMOS compliance check duration")
            .register(meterRegistry);

    this.samosFraudDetectionTimer =
        Timer.builder("samos.fraud.detection.duration")
            .description("SAMOS fraud detection duration")
            .register(meterRegistry);

    this.samosRiskAssessmentTimer =
        Timer.builder("samos.risk.assessment.duration")
            .description("SAMOS risk assessment duration")
            .register(meterRegistry);

    // Initialize gauges
    Gauge.builder("samos.adapters.active", this, SamosMonitoringService::getActiveAdapterCount)
        .description("Number of active SAMOS adapters")
        .register(meterRegistry);

    Gauge.builder("samos.transactions.success.rate", this, SamosMonitoringService::getSuccessRate)
        .description("SAMOS transaction success rate")
        .register(meterRegistry);
  }

  /** Record transaction metrics */
  public void recordTransaction(String transactionId, boolean success, long durationMs) {
    samosTransactionCounter.increment();
    samosTotalTransactions.incrementAndGet();

    if (success) {
      samosSuccessfulTransactions.incrementAndGet();
    } else {
      samosFailedTransactions.incrementAndGet();
      samosErrorCounter.increment();
    }

    samosTransactionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded SAMOS transaction metrics: transactionId={}, success={}, duration={}ms",
        transactionId,
        success,
        durationMs);
  }

  /** Record compliance check metrics */
  public void recordComplianceCheck(String adapterId, boolean compliant, long durationMs) {
    samosComplianceCheckCounter.increment();
    samosComplianceTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded SAMOS compliance check metrics: adapterId={}, compliant={}, duration={}ms",
        adapterId,
        compliant,
        durationMs);
  }

  /** Record fraud detection metrics */
  public void recordFraudDetection(String adapterId, boolean fraudDetected, long durationMs) {
    samosFraudDetectionCounter.increment();
    samosFraudDetectionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded SAMOS fraud detection metrics: adapterId={}, fraudDetected={}, duration={}ms",
        adapterId,
        fraudDetected,
        durationMs);
  }

  /** Record risk assessment metrics */
  public void recordRiskAssessment(String adapterId, String riskLevel, long durationMs) {
    samosRiskAssessmentCounter.increment();
    samosRiskAssessmentTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

    log.debug(
        "Recorded SAMOS risk assessment metrics: adapterId={}, riskLevel={}, duration={}ms",
        adapterId,
        riskLevel,
        durationMs);
  }

  /** Get dashboard metrics */
  public Map<String, Object> getDashboardMetrics() {
    Map<String, Object> metrics = new HashMap<>();

    // Transaction metrics
    metrics.put("totalTransactions", samosTotalTransactions.get());
    metrics.put("successfulTransactions", samosSuccessfulTransactions.get());
    metrics.put("failedTransactions", samosFailedTransactions.get());
    metrics.put("successRate", getSuccessRate());

    // Adapter metrics
    metrics.put("activeAdapters", getActiveAdapterCount());
    metrics.put("totalAdapters", samosAdapterRepository.count());

    // Performance metrics
    metrics.put(
        "averageTransactionTime",
        samosTransactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageComplianceTime",
        samosComplianceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageFraudDetectionTime",
        samosFraudDetectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put(
        "averageRiskAssessmentTime",
        samosRiskAssessmentTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));

    // Counter metrics
    metrics.put("totalComplianceChecks", samosComplianceCheckCounter.count());
    metrics.put("totalFraudDetections", samosFraudDetectionCounter.count());
    metrics.put("totalRiskAssessments", samosRiskAssessmentCounter.count());
    metrics.put("totalErrors", samosErrorCounter.count());

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
    health.put("totalAdapters", samosAdapterRepository.count());
    health.put("timestamp", Instant.now().toString());

    return health;
  }

  /** Get performance metrics */
  public Map<String, Object> getPerformanceMetrics() {
    Map<String, Object> performance = new HashMap<>();

    performance.put(
        "averageTransactionTime",
        samosTransactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "maxTransactionTime",
        samosTransactionTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageComplianceTime",
        samosComplianceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageFraudDetectionTime",
        samosFraudDetectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put(
        "averageRiskAssessmentTime",
        samosRiskAssessmentTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("timestamp", Instant.now().toString());

    return performance;
  }

  /** Get active adapter count */
  private double getActiveAdapterCount() {
    return (double)
        samosAdapterRepository.countByStatus(
            com.payments.domain.clearing.AdapterOperationalStatus.ACTIVE);
  }

  /** Get success rate */
  private double getSuccessRate() {
    long total = samosTotalTransactions.get();
    if (total == 0) {
      return 0.0;
    }
    return (double) samosSuccessfulTransactions.get() / total;
  }
}
