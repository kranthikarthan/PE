package com.payments.settlement.service;

import com.payments.domain.settlement.SettlementMonitoring;
import com.payments.domain.settlement.SettlementAlert;
import com.payments.domain.settlement.SettlementMetrics;
import com.payments.settlement.repository.SettlementMonitoringRepository;
import com.payments.settlement.repository.SettlementAlertRepository;
import com.payments.settlement.repository.SettlementMetricsRepository;
import com.payments.settlement.dto.SettlementMonitoringRequest;
import com.payments.settlement.dto.SettlementMonitoringResponse;
import com.payments.settlement.dto.SettlementAlertRequest;
import com.payments.settlement.dto.SettlementAlertResponse;
import com.payments.settlement.dto.SettlementMetricsRequest;
import com.payments.settlement.dto.SettlementMetricsResponse;
import com.payments.settlement.exception.SettlementMonitoringException;
import com.payments.domain.shared.TenantContext;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing settlement monitoring operations.
 *
 * <p>This service provides comprehensive monitoring capabilities including
 * metrics collection, alert generation, performance tracking, and health monitoring.
 * It integrates with the settlement monitoring domain models and provides
 * business logic for monitoring operations.
 *
 * @since PE-411
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementMonitoringService {
  
  private final SettlementMonitoringRepository monitoringRepository;
  private final SettlementAlertRepository alertRepository;
  private final SettlementMetricsRepository metricsRepository;
  
  /**
   * Creates a new settlement monitoring entry.
   *
   * @param request the monitoring request
   * @return the created monitoring response
   * @throws SettlementMonitoringException if creation fails
   */
  @Transactional
  @CircuitBreaker(name = "settlement-monitoring", fallbackMethod = "createMonitoringFallback")
  @Retry(name = "settlement-monitoring")
  @TimeLimiter(name = "settlement-monitoring")
  public SettlementMonitoringResponse createMonitoring(SettlementMonitoringRequest request) {
    try {
      log.info("Creating settlement monitoring entry: {}", request.getMonitoringName());
      
      SettlementMonitoring monitoring = SettlementMonitoring.builder()
          .monitoringId(UUID.randomUUID().toString())
          .monitoringName(request.getMonitoringName())
          .description(request.getDescription())
          .monitoringType(request.getMonitoringType())
          .metricName(request.getMetricName())
          .metricValue(request.getMetricValue())
          .metricUnit(request.getMetricUnit())
          .thresholdValue(request.getThresholdValue())
          .monitoringTimestamp(LocalDateTime.now())
          .participantId(request.getParticipantId())
          .workflowId(request.getWorkflowId())
          .orchestrationId(request.getOrchestrationId())
          .currency(request.getCurrency())
          .metadata(request.getMetadata())
          .createdBy(TenantContext.getCurrentUserId())
          .build();
      
      monitoring.setTenantAndBusinessUnit();
      monitoring.setAlertLevel(monitoring.determineAlertLevel());
      
      SettlementMonitoring savedMonitoring = monitoringRepository.save(monitoring);
      
      log.info("Successfully created settlement monitoring entry: {}", savedMonitoring.getMonitoringId());
      
      return SettlementMonitoringResponse.builder()
          .monitoringId(savedMonitoring.getMonitoringId())
          .monitoringName(savedMonitoring.getMonitoringName())
          .description(savedMonitoring.getDescription())
          .monitoringType(savedMonitoring.getMonitoringType())
          .status(savedMonitoring.getStatus())
          .metricName(savedMonitoring.getMetricName())
          .metricValue(savedMonitoring.getMetricValue())
          .metricUnit(savedMonitoring.getMetricUnit())
          .thresholdValue(savedMonitoring.getThresholdValue())
          .alertLevel(savedMonitoring.getAlertLevel())
          .monitoringTimestamp(savedMonitoring.getMonitoringTimestamp())
          .participantId(savedMonitoring.getParticipantId())
          .workflowId(savedMonitoring.getWorkflowId())
          .orchestrationId(savedMonitoring.getOrchestrationId())
          .currency(savedMonitoring.getCurrency())
          .metadata(savedMonitoring.getMetadata())
          .createdAt(savedMonitoring.getCreatedAt())
          .createdBy(savedMonitoring.getCreatedBy())
          .build();
      
    } catch (Exception e) {
      log.error("Failed to create settlement monitoring entry: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to create settlement monitoring entry", e);
    }
  }
  
  /**
   * Creates a new settlement alert.
   *
   * @param request the alert request
   * @return the created alert response
   * @throws SettlementMonitoringException if creation fails
   */
  @Transactional
  @CircuitBreaker(name = "settlement-alert", fallbackMethod = "createAlertFallback")
  @Retry(name = "settlement-alert")
  @TimeLimiter(name = "settlement-alert")
  public SettlementAlertResponse createAlert(SettlementAlertRequest request) {
    try {
      log.info("Creating settlement alert: {}", request.getAlertName());
      
      SettlementAlert alert = SettlementAlert.builder()
          .alertId(UUID.randomUUID().toString())
          .alertName(request.getAlertName())
          .description(request.getDescription())
          .alertType(request.getAlertType())
          .severity(request.getSeverity())
          .alertMessage(request.getAlertMessage())
          .alertTimestamp(LocalDateTime.now())
          .participantId(request.getParticipantId())
          .workflowId(request.getWorkflowId())
          .orchestrationId(request.getOrchestrationId())
          .monitoringId(request.getMonitoringId())
          .metadata(request.getMetadata())
          .createdBy(TenantContext.getCurrentUserId())
          .build();
      
      alert.setTenantAndBusinessUnit();
      
      SettlementAlert savedAlert = alertRepository.save(alert);
      
      log.info("Successfully created settlement alert: {}", savedAlert.getAlertId());
      
      return SettlementAlertResponse.builder()
          .alertId(savedAlert.getAlertId())
          .alertName(savedAlert.getAlertName())
          .description(savedAlert.getDescription())
          .alertType(savedAlert.getAlertType())
          .severity(savedAlert.getSeverity())
          .status(savedAlert.getStatus())
          .alertMessage(savedAlert.getAlertMessage())
          .alertTimestamp(savedAlert.getAlertTimestamp())
          .participantId(savedAlert.getParticipantId())
          .workflowId(savedAlert.getWorkflowId())
          .orchestrationId(savedAlert.getOrchestrationId())
          .monitoringId(savedAlert.getMonitoringId())
          .metadata(savedAlert.getMetadata())
          .createdAt(savedAlert.getCreatedAt())
          .createdBy(savedAlert.getCreatedBy())
          .build();
      
    } catch (Exception e) {
      log.error("Failed to create settlement alert: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to create settlement alert", e);
    }
  }
  
  /**
   * Creates a new settlement metrics entry.
   *
   * @param request the metrics request
   * @return the created metrics response
   * @throws SettlementMonitoringException if creation fails
   */
  @Transactional
  @CircuitBreaker(name = "settlement-metrics", fallbackMethod = "createMetricsFallback")
  @Retry(name = "settlement-metrics")
  @TimeLimiter(name = "settlement-metrics")
  public SettlementMetricsResponse createMetrics(SettlementMetricsRequest request) {
    try {
      log.info("Creating settlement metrics entry: {}", request.getMetricsName());
      
      SettlementMetrics metrics = SettlementMetrics.builder()
          .metricsId(UUID.randomUUID().toString())
          .metricsName(request.getMetricsName())
          .description(request.getDescription())
          .metricsType(request.getMetricsType())
          .category(request.getCategory())
          .metricName(request.getMetricName())
          .metricValue(request.getMetricValue())
          .metricUnit(request.getMetricUnit())
          .baselineValue(request.getBaselineValue())
          .targetValue(request.getTargetValue())
          .thresholdMin(request.getThresholdMin())
          .thresholdMax(request.getThresholdMax())
          .metricsTimestamp(LocalDateTime.now())
          .periodStart(request.getPeriodStart())
          .periodEnd(request.getPeriodEnd())
          .participantId(request.getParticipantId())
          .workflowId(request.getWorkflowId())
          .orchestrationId(request.getOrchestrationId())
          .currency(request.getCurrency())
          .metadata(request.getMetadata())
          .createdBy(TenantContext.getCurrentUserId())
          .build();
      
      metrics.setTenantAndBusinessUnit();
      
      SettlementMetrics savedMetrics = metricsRepository.save(metrics);
      
      log.info("Successfully created settlement metrics entry: {}", savedMetrics.getMetricsId());
      
      return SettlementMetricsResponse.builder()
          .metricsId(savedMetrics.getMetricsId())
          .metricsName(savedMetrics.getMetricsName())
          .description(savedMetrics.getDescription())
          .metricsType(savedMetrics.getMetricsType())
          .category(savedMetrics.getCategory())
          .metricName(savedMetrics.getMetricName())
          .metricValue(savedMetrics.getMetricValue())
          .metricUnit(savedMetrics.getMetricUnit())
          .baselineValue(savedMetrics.getBaselineValue())
          .targetValue(savedMetrics.getTargetValue())
          .thresholdMin(savedMetrics.getThresholdMin())
          .thresholdMax(savedMetrics.getThresholdMax())
          .metricsTimestamp(savedMetrics.getMetricsTimestamp())
          .periodStart(savedMetrics.getPeriodStart())
          .periodEnd(savedMetrics.getPeriodEnd())
          .participantId(savedMetrics.getParticipantId())
          .workflowId(savedMetrics.getWorkflowId())
          .orchestrationId(savedMetrics.getOrchestrationId())
          .currency(savedMetrics.getCurrency())
          .metadata(savedMetrics.getMetadata())
          .createdAt(savedMetrics.getCreatedAt())
          .createdBy(savedMetrics.getCreatedBy())
          .build();
      
    } catch (Exception e) {
      log.error("Failed to create settlement metrics entry: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to create settlement metrics entry", e);
    }
  }
  
  /**
   * Gets all monitoring entries for the current tenant.
   *
   * @return list of monitoring responses
   */
  public List<SettlementMonitoringResponse> getAllMonitoring() {
    try {
      log.info("Retrieving all settlement monitoring entries for tenant: {}", TenantContext.getCurrentTenantId());
      
      List<SettlementMonitoring> monitoringList = monitoringRepository.findByTenantId(TenantContext.getCurrentTenantId());
      
      return monitoringList.stream()
          .map(this::mapToMonitoringResponse)
          .collect(Collectors.toList());
      
    } catch (Exception e) {
      log.error("Failed to retrieve settlement monitoring entries: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to retrieve settlement monitoring entries", e);
    }
  }
  
  /**
   * Gets all alerts for the current tenant.
   *
   * @return list of alert responses
   */
  public List<SettlementAlertResponse> getAllAlerts() {
    try {
      log.info("Retrieving all settlement alerts for tenant: {}", TenantContext.getCurrentTenantId());
      
      List<SettlementAlert> alertList = alertRepository.findByTenantId(TenantContext.getCurrentTenantId());
      
      return alertList.stream()
          .map(this::mapToAlertResponse)
          .collect(Collectors.toList());
      
    } catch (Exception e) {
      log.error("Failed to retrieve settlement alerts: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to retrieve settlement alerts", e);
    }
  }
  
  /**
   * Gets all metrics for the current tenant.
   *
   * @return list of metrics responses
   */
  public List<SettlementMetricsResponse> getAllMetrics() {
    try {
      log.info("Retrieving all settlement metrics for tenant: {}", TenantContext.getCurrentTenantId());
      
      List<SettlementMetrics> metricsList = metricsRepository.findByTenantId(TenantContext.getCurrentTenantId());
      
      return metricsList.stream()
          .map(this::mapToMetricsResponse)
          .collect(Collectors.toList());
      
    } catch (Exception e) {
      log.error("Failed to retrieve settlement metrics: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to retrieve settlement metrics", e);
    }
  }
  
  /**
   * Acknowledges an alert.
   *
   * @param alertId the alert ID
   * @param acknowledgedBy the user acknowledging the alert
   * @return the updated alert response
   */
  @Transactional
  public SettlementAlertResponse acknowledgeAlert(String alertId, String acknowledgedBy) {
    try {
      log.info("Acknowledging settlement alert: {} by user: {}", alertId, acknowledgedBy);
      
      Optional<SettlementAlert> alertOpt = alertRepository.findByAlertIdAndTenantId(alertId, TenantContext.getCurrentTenantId());
      if (alertOpt.isEmpty()) {
        throw new SettlementMonitoringException("Alert not found: " + alertId);
      }
      
      SettlementAlert alert = alertOpt.get();
      alert.acknowledge(acknowledgedBy);
      alert.setUpdatedBy(TenantContext.getCurrentUserId());
      
      SettlementAlert savedAlert = alertRepository.save(alert);
      
      log.info("Successfully acknowledged settlement alert: {}", alertId);
      
      return mapToAlertResponse(savedAlert);
      
    } catch (Exception e) {
      log.error("Failed to acknowledge settlement alert: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to acknowledge settlement alert", e);
    }
  }
  
  /**
   * Resolves an alert.
   *
   * @param alertId the alert ID
   * @param resolvedBy the user resolving the alert
   * @param resolutionNotes the resolution notes
   * @return the updated alert response
   */
  @Transactional
  public SettlementAlertResponse resolveAlert(String alertId, String resolvedBy, String resolutionNotes) {
    try {
      log.info("Resolving settlement alert: {} by user: {}", alertId, resolvedBy);
      
      Optional<SettlementAlert> alertOpt = alertRepository.findByAlertIdAndTenantId(alertId, TenantContext.getCurrentTenantId());
      if (alertOpt.isEmpty()) {
        throw new SettlementMonitoringException("Alert not found: " + alertId);
      }
      
      SettlementAlert alert = alertOpt.get();
      alert.resolve(resolvedBy, resolutionNotes);
      alert.setUpdatedBy(TenantContext.getCurrentUserId());
      
      SettlementAlert savedAlert = alertRepository.save(alert);
      
      log.info("Successfully resolved settlement alert: {}", alertId);
      
      return mapToAlertResponse(savedAlert);
      
    } catch (Exception e) {
      log.error("Failed to resolve settlement alert: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to resolve settlement alert", e);
    }
  }
  
  /**
   * Maps monitoring entity to response DTO.
   *
   * @param monitoring the monitoring entity
   * @return the monitoring response
   */
  private SettlementMonitoringResponse mapToMonitoringResponse(SettlementMonitoring monitoring) {
    return SettlementMonitoringResponse.builder()
        .monitoringId(monitoring.getMonitoringId())
        .monitoringName(monitoring.getMonitoringName())
        .description(monitoring.getDescription())
        .monitoringType(monitoring.getMonitoringType())
        .status(monitoring.getStatus())
        .metricName(monitoring.getMetricName())
        .metricValue(monitoring.getMetricValue())
        .metricUnit(monitoring.getMetricUnit())
        .thresholdValue(monitoring.getThresholdValue())
        .alertLevel(monitoring.getAlertLevel())
        .monitoringTimestamp(monitoring.getMonitoringTimestamp())
        .participantId(monitoring.getParticipantId())
        .workflowId(monitoring.getWorkflowId())
        .orchestrationId(monitoring.getOrchestrationId())
        .currency(monitoring.getCurrency())
        .metadata(monitoring.getMetadata())
        .createdAt(monitoring.getCreatedAt())
        .createdBy(monitoring.getCreatedBy())
        .build();
  }
  
  /**
   * Maps alert entity to response DTO.
   *
   * @param alert the alert entity
   * @return the alert response
   */
  private SettlementAlertResponse mapToAlertResponse(SettlementAlert alert) {
    return SettlementAlertResponse.builder()
        .alertId(alert.getAlertId())
        .alertName(alert.getAlertName())
        .description(alert.getDescription())
        .alertType(alert.getAlertType())
        .severity(alert.getSeverity())
        .status(alert.getStatus())
        .alertMessage(alert.getAlertMessage())
        .alertTimestamp(alert.getAlertTimestamp())
        .acknowledgedAt(alert.getAcknowledgedAt())
        .acknowledgedBy(alert.getAcknowledgedBy())
        .resolvedAt(alert.getResolvedAt())
        .resolvedBy(alert.getResolvedBy())
        .resolutionNotes(alert.getResolutionNotes())
        .participantId(alert.getParticipantId())
        .workflowId(alert.getWorkflowId())
        .orchestrationId(alert.getOrchestrationId())
        .monitoringId(alert.getMonitoringId())
        .metadata(alert.getMetadata())
        .createdAt(alert.getCreatedAt())
        .createdBy(alert.getCreatedBy())
        .build();
  }
  
  /**
   * Maps metrics entity to response DTO.
   *
   * @param metrics the metrics entity
   * @return the metrics response
   */
  private SettlementMetricsResponse mapToMetricsResponse(SettlementMetrics metrics) {
    return SettlementMetricsResponse.builder()
        .metricsId(metrics.getMetricsId())
        .metricsName(metrics.getMetricsName())
        .description(metrics.getDescription())
        .metricsType(metrics.getMetricsType())
        .category(metrics.getCategory())
        .metricName(metrics.getMetricName())
        .metricValue(metrics.getMetricValue())
        .metricUnit(metrics.getMetricUnit())
        .baselineValue(metrics.getBaselineValue())
        .targetValue(metrics.getTargetValue())
        .thresholdMin(metrics.getThresholdMin())
        .thresholdMax(metrics.getThresholdMax())
        .metricsTimestamp(metrics.getMetricsTimestamp())
        .periodStart(metrics.getPeriodStart())
        .periodEnd(metrics.getPeriodEnd())
        .participantId(metrics.getParticipantId())
        .workflowId(metrics.getWorkflowId())
        .orchestrationId(metrics.getOrchestrationId())
        .currency(metrics.getCurrency())
        .metadata(metrics.getMetadata())
        .createdAt(metrics.getCreatedAt())
        .createdBy(metrics.getCreatedBy())
        .build();
  }
  
  // Fallback methods for circuit breaker
  public SettlementMonitoringResponse createMonitoringFallback(SettlementMonitoringRequest request, Exception ex) {
    log.error("Fallback: Failed to create settlement monitoring entry", ex);
    throw new SettlementMonitoringException("Service temporarily unavailable", ex);
  }
  
  public SettlementAlertResponse createAlertFallback(SettlementAlertRequest request, Exception ex) {
    log.error("Fallback: Failed to create settlement alert", ex);
    throw new SettlementMonitoringException("Service temporarily unavailable", ex);
  }
  
  public SettlementMetricsResponse createMetricsFallback(SettlementMetricsRequest request, Exception ex) {
    log.error("Fallback: Failed to create settlement metrics entry", ex);
    throw new SettlementMonitoringException("Service temporarily unavailable", ex);
  }
}
