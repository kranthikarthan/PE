package com.payments.bankservafricaadapter.monitoring;

import com.payments.bankservafricaadapter.repository.BankservAfricaAdapterRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BankservAfrica Monitoring Service
 *
 * <p>Advanced monitoring service for BankservAfrica adapter: - Custom metrics collection - Performance monitoring - Health monitoring - Dashboard data preparation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankservAfricaMonitoringService {

  private final MeterRegistry meterRegistry;
  private final BankservAfricaAdapterRepository bankservAfricaAdapterRepository;

  // Custom metrics
  private final Counter bankservAfricaTransactionCounter;
  private final Counter bankservAfricaErrorCounter;
  private final Counter bankservAfricaComplianceCheckCounter;
  private final Counter bankservAfricaFraudDetectionCounter;
  private final Counter bankservAfricaRiskAssessmentCounter;
  private final Counter bankservAfricaBatchProcessingCounter;
  private final Timer bankservAfricaTransactionTimer;
  private final Timer bankservAfricaComplianceTimer;
  private final Timer bankservAfricaFraudDetectionTimer;
  private final Timer bankservAfricaRiskAssessmentTimer;
  private final Timer bankservAfricaBatchProcessingTimer;
  private final AtomicLong bankservAfricaActiveAdapters = new AtomicLong(0);
  private final AtomicLong bankservAfricaTotalTransactions = new AtomicLong(0);
  private final AtomicLong bankservAfricaSuccessfulTransactions = new AtomicLong(0);
  private final AtomicLong bankservAfricaFailedTransactions = new AtomicLong(0);

  public BankservAfricaMonitoringService(MeterRegistry meterRegistry, BankservAfricaAdapterRepository bankservAfricaAdapterRepository) {
    this.meterRegistry = meterRegistry;
    this.bankservAfricaAdapterRepository = bankservAfricaAdapterRepository;
    
    // Initialize counters
    this.bankservAfricaTransactionCounter = Counter.builder("bankservafrica.transactions.total")
        .description("Total number of BankservAfrica transactions")
        .register(meterRegistry);
    
    this.bankservAfricaErrorCounter = Counter.builder("bankservafrica.errors.total")
        .description("Total number of BankservAfrica errors")
        .register(meterRegistry);
    
    this.bankservAfricaComplianceCheckCounter = Counter.builder("bankservafrica.compliance.checks.total")
        .description("Total number of BankservAfrica compliance checks")
        .register(meterRegistry);
    
    this.bankservAfricaFraudDetectionCounter = Counter.builder("bankservafrica.fraud.detections.total")
        .description("Total number of BankservAfrica fraud detections")
        .register(meterRegistry);
    
    this.bankservAfricaRiskAssessmentCounter = Counter.builder("bankservafrica.risk.assessments.total")
        .description("Total number of BankservAfrica risk assessments")
        .register(meterRegistry);
    
    this.bankservAfricaBatchProcessingCounter = Counter.builder("bankservafrica.batch.processing.total")
        .description("Total number of BankservAfrica batch processing operations")
        .register(meterRegistry);
    
    // Initialize timers
    this.bankservAfricaTransactionTimer = Timer.builder("bankservafrica.transactions.duration")
        .description("BankservAfrica transaction processing duration")
        .register(meterRegistry);
    
    this.bankservAfricaComplianceTimer = Timer.builder("bankservafrica.compliance.duration")
        .description("BankservAfrica compliance check duration")
        .register(meterRegistry);
    
    this.bankservAfricaFraudDetectionTimer = Timer.builder("bankservafrica.fraud.detection.duration")
        .description("BankservAfrica fraud detection duration")
        .register(meterRegistry);
    
    this.bankservAfricaRiskAssessmentTimer = Timer.builder("bankservafrica.risk.assessment.duration")
        .description("BankservAfrica risk assessment duration")
        .register(meterRegistry);
    
    this.bankservAfricaBatchProcessingTimer = Timer.builder("bankservafrica.batch.processing.duration")
        .description("BankservAfrica batch processing duration")
        .register(meterRegistry);
    
    // Initialize gauges
    Gauge.builder("bankservafrica.adapters.active")
        .description("Number of active BankservAfrica adapters")
        .register(meterRegistry, this, BankservAfricaMonitoringService::getActiveAdapterCount);
    
    Gauge.builder("bankservafrica.transactions.success.rate")
        .description("BankservAfrica transaction success rate")
        .register(meterRegistry, this, BankservAfricaMonitoringService::getSuccessRate);
  }

  /**
   * Record transaction metrics
   */
  public void recordTransaction(String transactionId, boolean success, long durationMs) {
    bankservAfricaTransactionCounter.increment();
    bankservAfricaTotalTransactions.incrementAndGet();
    
    if (success) {
      bankservAfricaSuccessfulTransactions.incrementAndGet();
    } else {
      bankservAfricaFailedTransactions.incrementAndGet();
      bankservAfricaErrorCounter.increment();
    }
    
    bankservAfricaTransactionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    
    log.debug("Recorded BankservAfrica transaction metrics: transactionId={}, success={}, duration={}ms", 
              transactionId, success, durationMs);
  }

  /**
   * Record compliance check metrics
   */
  public void recordComplianceCheck(String adapterId, boolean compliant, long durationMs) {
    bankservAfricaComplianceCheckCounter.increment();
    bankservAfricaComplianceTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    
    log.debug("Recorded BankservAfrica compliance check metrics: adapterId={}, compliant={}, duration={}ms", 
              adapterId, compliant, durationMs);
  }

  /**
   * Record fraud detection metrics
   */
  public void recordFraudDetection(String adapterId, boolean fraudDetected, long durationMs) {
    bankservAfricaFraudDetectionCounter.increment();
    bankservAfricaFraudDetectionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    
    log.debug("Recorded BankservAfrica fraud detection metrics: adapterId={}, fraudDetected={}, duration={}ms", 
              adapterId, fraudDetected, durationMs);
  }

  /**
   * Record risk assessment metrics
   */
  public void recordRiskAssessment(String adapterId, String riskLevel, long durationMs) {
    bankservAfricaRiskAssessmentCounter.increment();
    bankservAfricaRiskAssessmentTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    
    log.debug("Recorded BankservAfrica risk assessment metrics: adapterId={}, riskLevel={}, duration={}ms", 
              adapterId, riskLevel, durationMs);
  }

  /**
   * Record batch processing metrics
   */
  public void recordBatchProcessing(String batchId, int transactionCount, long durationMs) {
    bankservAfricaBatchProcessingCounter.increment();
    bankservAfricaBatchProcessingTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    
    log.debug("Recorded BankservAfrica batch processing metrics: batchId={}, transactionCount={}, duration={}ms", 
              batchId, transactionCount, durationMs);
  }

  /**
   * Get dashboard metrics
   */
  public Map<String, Object> getDashboardMetrics() {
    Map<String, Object> metrics = new HashMap<>();
    
    // Transaction metrics
    metrics.put("totalTransactions", bankservAfricaTotalTransactions.get());
    metrics.put("successfulTransactions", bankservAfricaSuccessfulTransactions.get());
    metrics.put("failedTransactions", bankservAfricaFailedTransactions.get());
    metrics.put("successRate", getSuccessRate());
    
    // Adapter metrics
    metrics.put("activeAdapters", getActiveAdapterCount());
    metrics.put("totalAdapters", bankservAfricaAdapterRepository.count());
    
    // Performance metrics
    metrics.put("averageTransactionTime", bankservAfricaTransactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("averageComplianceTime", bankservAfricaComplianceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("averageFraudDetectionTime", bankservAfricaFraudDetectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("averageRiskAssessmentTime", bankservAfricaRiskAssessmentTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("averageBatchProcessingTime", bankservAfricaBatchProcessingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    
    // Counter metrics
    metrics.put("totalComplianceChecks", bankservAfricaComplianceCheckCounter.count());
    metrics.put("totalFraudDetections", bankservAfricaFraudDetectionCounter.count());
    metrics.put("totalRiskAssessments", bankservAfricaRiskAssessmentCounter.count());
    metrics.put("totalBatchProcessing", bankservAfricaBatchProcessingCounter.count());
    metrics.put("totalErrors", bankservAfricaErrorCounter.count());
    
    // Timestamp
    metrics.put("timestamp", Instant.now().toString());
    
    return metrics;
  }

  /**
   * Get health metrics
   */
  public Map<String, Object> getHealthMetrics() {
    Map<String, Object> health = new HashMap<>();
    
    long activeAdapters = getActiveAdapterCount();
    double successRate = getSuccessRate();
    
    health.put("status", activeAdapters > 0 && successRate > 0.95 ? "HEALTHY" : "DEGRADED");
    health.put("activeAdapters", activeAdapters);
    health.put("successRate", successRate);
    health.put("totalAdapters", bankservAfricaAdapterRepository.count());
    health.put("timestamp", Instant.now().toString());
    
    return health;
  }

  /**
   * Get performance metrics
   */
  public Map<String, Object> getPerformanceMetrics() {
    Map<String, Object> performance = new HashMap<>();
    
    performance.put("averageTransactionTime", bankservAfricaTransactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("maxTransactionTime", bankservAfricaTransactionTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("averageComplianceTime", bankservAfricaComplianceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("averageFraudDetectionTime", bankservAfricaFraudDetectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("averageRiskAssessmentTime", bankservAfricaRiskAssessmentTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("averageBatchProcessingTime", bankservAfricaBatchProcessingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("timestamp", Instant.now().toString());
    
    return performance;
  }

  /**
   * Get active adapter count
   */
  private double getActiveAdapterCount() {
    return bankservAfricaAdapterRepository.countByStatus(com.payments.domain.clearing.AdapterOperationalStatus.ACTIVE);
  }

  /**
   * Get success rate
   */
  private double getSuccessRate() {
    long total = bankservAfricaTotalTransactions.get();
    if (total == 0) {
      return 0.0;
    }
    return (double) bankservAfricaSuccessfulTransactions.get() / total;
  }
}
