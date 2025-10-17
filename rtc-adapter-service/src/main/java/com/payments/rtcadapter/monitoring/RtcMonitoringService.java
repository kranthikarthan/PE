package com.payments.rtcadapter.monitoring;

import com.payments.rtcadapter.repository.RtcAdapterRepository;
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
 * RTC Monitoring Service
 *
 * <p>Advanced monitoring service for RTC adapter: - Custom metrics collection - Performance monitoring - Health monitoring - Dashboard data preparation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RtcMonitoringService {

  private final MeterRegistry meterRegistry;
  private final RtcAdapterRepository rtcAdapterRepository;

  // Custom metrics
  private final Counter rtcTransactionCounter;
  private final Counter rtcErrorCounter;
  private final Counter rtcComplianceCheckCounter;
  private final Counter rtcFraudDetectionCounter;
  private final Counter rtcRiskAssessmentCounter;
  private final Counter rtcRealTimeProcessingCounter;
  private final Timer rtcTransactionTimer;
  private final Timer rtcComplianceTimer;
  private final Timer rtcFraudDetectionTimer;
  private final Timer rtcRiskAssessmentTimer;
  private final Timer rtcRealTimeProcessingTimer;
  private final AtomicLong rtcActiveAdapters = new AtomicLong(0);
  private final AtomicLong rtcTotalTransactions = new AtomicLong(0);
  private final AtomicLong rtcSuccessfulTransactions = new AtomicLong(0);
  private final AtomicLong rtcFailedTransactions = new AtomicLong(0);

  public RtcMonitoringService(MeterRegistry meterRegistry, RtcAdapterRepository rtcAdapterRepository) {
    this.meterRegistry = meterRegistry;
    this.rtcAdapterRepository = rtcAdapterRepository;
    
    // Initialize counters
    this.rtcTransactionCounter = Counter.builder("rtc.transactions.total")
        .description("Total number of RTC transactions")
        .register(meterRegistry);
    
    this.rtcErrorCounter = Counter.builder("rtc.errors.total")
        .description("Total number of RTC errors")
        .register(meterRegistry);
    
    this.rtcComplianceCheckCounter = Counter.builder("rtc.compliance.checks.total")
        .description("Total number of RTC compliance checks")
        .register(meterRegistry);
    
    this.rtcFraudDetectionCounter = Counter.builder("rtc.fraud.detections.total")
        .description("Total number of RTC fraud detections")
        .register(meterRegistry);
    
    this.rtcRiskAssessmentCounter = Counter.builder("rtc.risk.assessments.total")
        .description("Total number of RTC risk assessments")
        .register(meterRegistry);
    
    this.rtcRealTimeProcessingCounter = Counter.builder("rtc.realtime.processing.total")
        .description("Total number of RTC real-time processing operations")
        .register(meterRegistry);
    
    // Initialize timers
    this.rtcTransactionTimer = Timer.builder("rtc.transactions.duration")
        .description("RTC transaction processing duration")
        .register(meterRegistry);
    
    this.rtcComplianceTimer = Timer.builder("rtc.compliance.duration")
        .description("RTC compliance check duration")
        .register(meterRegistry);
    
    this.rtcFraudDetectionTimer = Timer.builder("rtc.fraud.detection.duration")
        .description("RTC fraud detection duration")
        .register(meterRegistry);
    
    this.rtcRiskAssessmentTimer = Timer.builder("rtc.risk.assessment.duration")
        .description("RTC risk assessment duration")
        .register(meterRegistry);
    
    this.rtcRealTimeProcessingTimer = Timer.builder("rtc.realtime.processing.duration")
        .description("RTC real-time processing duration")
        .register(meterRegistry);
    
    // Initialize gauges
    Gauge.builder("rtc.adapters.active")
        .description("Number of active RTC adapters")
        .register(meterRegistry, this, RtcMonitoringService::getActiveAdapterCount);
    
    Gauge.builder("rtc.transactions.success.rate")
        .description("RTC transaction success rate")
        .register(meterRegistry, this, RtcMonitoringService::getSuccessRate);
  }

  /**
   * Record transaction metrics
   */
  public void recordTransaction(String transactionId, boolean success, long durationMs) {
    rtcTransactionCounter.increment();
    rtcTotalTransactions.incrementAndGet();
    
    if (success) {
      rtcSuccessfulTransactions.incrementAndGet();
    } else {
      rtcFailedTransactions.incrementAndGet();
      rtcErrorCounter.increment();
    }
    
    rtcTransactionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    
    log.debug("Recorded RTC transaction metrics: transactionId={}, success={}, duration={}ms", 
              transactionId, success, durationMs);
  }

  /**
   * Record compliance check metrics
   */
  public void recordComplianceCheck(String adapterId, boolean compliant, long durationMs) {
    rtcComplianceCheckCounter.increment();
    rtcComplianceTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    
    log.debug("Recorded RTC compliance check metrics: adapterId={}, compliant={}, duration={}ms", 
              adapterId, compliant, durationMs);
  }

  /**
   * Record fraud detection metrics
   */
  public void recordFraudDetection(String adapterId, boolean fraudDetected, long durationMs) {
    rtcFraudDetectionCounter.increment();
    rtcFraudDetectionTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    
    log.debug("Recorded RTC fraud detection metrics: adapterId={}, fraudDetected={}, duration={}ms", 
              adapterId, fraudDetected, durationMs);
  }

  /**
   * Record risk assessment metrics
   */
  public void recordRiskAssessment(String adapterId, String riskLevel, long durationMs) {
    rtcRiskAssessmentCounter.increment();
    rtcRiskAssessmentTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    
    log.debug("Recorded RTC risk assessment metrics: adapterId={}, riskLevel={}, duration={}ms", 
              adapterId, riskLevel, durationMs);
  }

  /**
   * Record real-time processing metrics
   */
  public void recordRealTimeProcessing(String transactionId, long durationMs) {
    rtcRealTimeProcessingCounter.increment();
    rtcRealTimeProcessingTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    
    log.debug("Recorded RTC real-time processing metrics: transactionId={}, duration={}ms", 
              transactionId, durationMs);
  }

  /**
   * Get dashboard metrics
   */
  public Map<String, Object> getDashboardMetrics() {
    Map<String, Object> metrics = new HashMap<>();
    
    // Transaction metrics
    metrics.put("totalTransactions", rtcTotalTransactions.get());
    metrics.put("successfulTransactions", rtcSuccessfulTransactions.get());
    metrics.put("failedTransactions", rtcFailedTransactions.get());
    metrics.put("successRate", getSuccessRate());
    
    // Adapter metrics
    metrics.put("activeAdapters", getActiveAdapterCount());
    metrics.put("totalAdapters", rtcAdapterRepository.count());
    
    // Performance metrics
    metrics.put("averageTransactionTime", rtcTransactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("averageComplianceTime", rtcComplianceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("averageFraudDetectionTime", rtcFraudDetectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("averageRiskAssessmentTime", rtcRiskAssessmentTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("averageRealTimeProcessingTime", rtcRealTimeProcessingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    
    // Counter metrics
    metrics.put("totalComplianceChecks", rtcComplianceCheckCounter.count());
    metrics.put("totalFraudDetections", rtcFraudDetectionCounter.count());
    metrics.put("totalRiskAssessments", rtcRiskAssessmentCounter.count());
    metrics.put("totalRealTimeProcessing", rtcRealTimeProcessingCounter.count());
    metrics.put("totalErrors", rtcErrorCounter.count());
    
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
    health.put("totalAdapters", rtcAdapterRepository.count());
    health.put("timestamp", Instant.now().toString());
    
    return health;
  }

  /**
   * Get performance metrics
   */
  public Map<String, Object> getPerformanceMetrics() {
    Map<String, Object> performance = new HashMap<>();
    
    performance.put("averageTransactionTime", rtcTransactionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("maxTransactionTime", rtcTransactionTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("averageComplianceTime", rtcComplianceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("averageFraudDetectionTime", rtcFraudDetectionTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("averageRiskAssessmentTime", rtcRiskAssessmentTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("averageRealTimeProcessingTime", rtcRealTimeProcessingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    performance.put("timestamp", Instant.now().toString());
    
    return performance;
  }

  /**
   * Get active adapter count
   */
  private double getActiveAdapterCount() {
    return rtcAdapterRepository.countByStatus(com.payments.domain.clearing.AdapterOperationalStatus.ACTIVE);
  }

  /**
   * Get success rate
   */
  private double getSuccessRate() {
    long total = rtcTotalTransactions.get();
    if (total == 0) {
      return 0.0;
    }
    return (double) rtcSuccessfulTransactions.get() / total;
  }
}
