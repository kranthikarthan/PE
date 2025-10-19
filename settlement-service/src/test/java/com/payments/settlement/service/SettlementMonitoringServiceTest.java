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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SettlementMonitoringService.
 *
 * <p>This test class provides comprehensive unit tests for the
 * settlement monitoring service including success scenarios,
 * error handling, and edge cases.
 *
 * @since PE-411
 */
@ExtendWith(MockitoExtension.class)
class SettlementMonitoringServiceTest {
  
  @Mock
  private SettlementMonitoringRepository monitoringRepository;
  
  @Mock
  private SettlementAlertRepository alertRepository;
  
  @Mock
  private SettlementMetricsRepository metricsRepository;
  
  @InjectMocks
  private SettlementMonitoringService monitoringService;
  
  private UUID tenantId;
  private UUID businessUnitId;
  private String userId;
  
  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    businessUnitId = UUID.randomUUID();
    userId = "test-user";
    
    TenantContext.setContext(TenantContext.builder()
        .tenantId(tenantId)
        .tenantName("TestTenant")
        .businessUnitId(businessUnitId)
        .businessUnitName("TestBusinessUnit")
        .build());
  }
  
  @Test
  void testCreateMonitoring_Success() {
    // Given
    SettlementMonitoringRequest request = SettlementMonitoringRequest.builder()
        .monitoringName("Test Monitoring")
        .description("Test Description")
        .monitoringType(SettlementMonitoring.MonitoringType.PERFORMANCE)
        .metricName("test_metric")
        .metricValue(BigDecimal.valueOf(100.0))
        .metricUnit("count")
        .thresholdValue(BigDecimal.valueOf(50.0))
        .participantId("PART001")
        .workflowId(1L)
        .orchestrationId(1L)
        .currency("USD")
        .build();
    
    SettlementMonitoring savedMonitoring = SettlementMonitoring.builder()
        .id(1L)
        .monitoringId("monitoring-123")
        .monitoringName("Test Monitoring")
        .description("Test Description")
        .monitoringType(SettlementMonitoring.MonitoringType.PERFORMANCE)
        .status(SettlementMonitoring.MonitoringStatus.ACTIVE)
        .metricName("test_metric")
        .metricValue(BigDecimal.valueOf(100.0))
        .metricUnit("count")
        .thresholdValue(BigDecimal.valueOf(50.0))
        .alertLevel("HIGH")
        .monitoringTimestamp(LocalDateTime.now())
        .participantId("PART001")
        .workflowId(1L)
        .orchestrationId(1L)
        .currency("USD")
        .tenantId(tenantId)
        .businessUnitId(businessUnitId.toString())
        .createdAt(LocalDateTime.now())
        .createdBy(userId)
        .build();
    
    when(monitoringRepository.save(any(SettlementMonitoring.class))).thenReturn(savedMonitoring);
    
    // When
    SettlementMonitoringResponse response = monitoringService.createMonitoring(request);
    
    // Then
    assertNotNull(response);
    assertEquals("monitoring-123", response.getMonitoringId());
    assertEquals("Test Monitoring", response.getMonitoringName());
    assertEquals("Test Description", response.getDescription());
    assertEquals(SettlementMonitoring.MonitoringType.PERFORMANCE, response.getMonitoringType());
    assertEquals(SettlementMonitoring.MonitoringStatus.ACTIVE, response.getStatus());
    assertEquals("test_metric", response.getMetricName());
    assertEquals(BigDecimal.valueOf(100.0), response.getMetricValue());
    assertEquals("count", response.getMetricUnit());
    assertEquals(BigDecimal.valueOf(50.0), response.getThresholdValue());
    assertEquals("HIGH", response.getAlertLevel());
    assertEquals("PART001", response.getParticipantId());
    assertEquals(1L, response.getWorkflowId());
    assertEquals(1L, response.getOrchestrationId());
    assertEquals("USD", response.getCurrency());
    
    verify(monitoringRepository).save(any(SettlementMonitoring.class));
  }
  
  @Test
  void testCreateMonitoring_Exception() {
    // Given
    SettlementMonitoringRequest request = SettlementMonitoringRequest.builder()
        .monitoringName("Test Monitoring")
        .monitoringType(SettlementMonitoring.MonitoringType.PERFORMANCE)
        .metricName("test_metric")
        .build();
    
    when(monitoringRepository.save(any(SettlementMonitoring.class)))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(SettlementMonitoringException.class, () -> {
      monitoringService.createMonitoring(request);
    });
    
    verify(monitoringRepository).save(any(SettlementMonitoring.class));
  }
  
  @Test
  void testCreateAlert_Success() {
    // Given
    SettlementAlertRequest request = SettlementAlertRequest.builder()
        .alertName("Test Alert")
        .description("Test Alert Description")
        .alertType(SettlementAlert.AlertType.PERFORMANCE)
        .severity(SettlementAlert.AlertSeverity.HIGH)
        .alertMessage("Test alert message")
        .participantId("PART001")
        .workflowId(1L)
        .orchestrationId(1L)
        .monitoringId(1L)
        .build();
    
    SettlementAlert savedAlert = SettlementAlert.builder()
        .id(1L)
        .alertId("alert-123")
        .alertName("Test Alert")
        .description("Test Alert Description")
        .alertType(SettlementAlert.AlertType.PERFORMANCE)
        .severity(SettlementAlert.AlertSeverity.HIGH)
        .status(SettlementAlert.AlertStatus.ACTIVE)
        .alertMessage("Test alert message")
        .alertTimestamp(LocalDateTime.now())
        .participantId("PART001")
        .workflowId(1L)
        .orchestrationId(1L)
        .monitoringId(1L)
        .tenantId(tenantId)
        .businessUnitId(businessUnitId.toString())
        .createdAt(LocalDateTime.now())
        .createdBy(userId)
        .build();
    
    when(alertRepository.save(any(SettlementAlert.class))).thenReturn(savedAlert);
    
    // When
    SettlementAlertResponse response = monitoringService.createAlert(request);
    
    // Then
    assertNotNull(response);
    assertEquals("alert-123", response.getAlertId());
    assertEquals("Test Alert", response.getAlertName());
    assertEquals("Test Alert Description", response.getDescription());
    assertEquals(SettlementAlert.AlertType.PERFORMANCE, response.getAlertType());
    assertEquals(SettlementAlert.AlertSeverity.HIGH, response.getSeverity());
    assertEquals(SettlementAlert.AlertStatus.ACTIVE, response.getStatus());
    assertEquals("Test alert message", response.getAlertMessage());
    assertEquals("PART001", response.getParticipantId());
    assertEquals(1L, response.getWorkflowId());
    assertEquals(1L, response.getOrchestrationId());
    assertEquals(1L, response.getMonitoringId());
    
    verify(alertRepository).save(any(SettlementAlert.class));
  }
  
  @Test
  void testCreateAlert_Exception() {
    // Given
    SettlementAlertRequest request = SettlementAlertRequest.builder()
        .alertName("Test Alert")
        .alertType(SettlementAlert.AlertType.PERFORMANCE)
        .severity(SettlementAlert.AlertSeverity.HIGH)
        .build();
    
    when(alertRepository.save(any(SettlementAlert.class)))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(SettlementMonitoringException.class, () -> {
      monitoringService.createAlert(request);
    });
    
    verify(alertRepository).save(any(SettlementAlert.class));
  }
  
  @Test
  void testCreateMetrics_Success() {
    // Given
    SettlementMetricsRequest request = SettlementMetricsRequest.builder()
        .metricsName("Test Metrics")
        .description("Test Metrics Description")
        .metricsType(SettlementMetrics.MetricsType.PERFORMANCE)
        .category(SettlementMetrics.MetricsCategory.THROUGHPUT)
        .metricName("test_metric")
        .metricValue(BigDecimal.valueOf(100.0))
        .metricUnit("count")
        .baselineValue(BigDecimal.valueOf(80.0))
        .targetValue(BigDecimal.valueOf(120.0))
        .thresholdMin(BigDecimal.valueOf(50.0))
        .thresholdMax(BigDecimal.valueOf(150.0))
        .periodStart(LocalDateTime.now().minusHours(1))
        .periodEnd(LocalDateTime.now())
        .participantId("PART001")
        .workflowId(1L)
        .orchestrationId(1L)
        .currency("USD")
        .build();
    
    SettlementMetrics savedMetrics = SettlementMetrics.builder()
        .id(1L)
        .metricsId("metrics-123")
        .metricsName("Test Metrics")
        .description("Test Metrics Description")
        .metricsType(SettlementMetrics.MetricsType.PERFORMANCE)
        .category(SettlementMetrics.MetricsCategory.THROUGHPUT)
        .metricName("test_metric")
        .metricValue(BigDecimal.valueOf(100.0))
        .metricUnit("count")
        .baselineValue(BigDecimal.valueOf(80.0))
        .targetValue(BigDecimal.valueOf(120.0))
        .thresholdMin(BigDecimal.valueOf(50.0))
        .thresholdMax(BigDecimal.valueOf(150.0))
        .metricsTimestamp(LocalDateTime.now())
        .periodStart(LocalDateTime.now().minusHours(1))
        .periodEnd(LocalDateTime.now())
        .participantId("PART001")
        .workflowId(1L)
        .orchestrationId(1L)
        .currency("USD")
        .tenantId(tenantId)
        .businessUnitId(businessUnitId.toString())
        .createdAt(LocalDateTime.now())
        .createdBy(userId)
        .build();
    
    when(metricsRepository.save(any(SettlementMetrics.class))).thenReturn(savedMetrics);
    
    // When
    SettlementMetricsResponse response = monitoringService.createMetrics(request);
    
    // Then
    assertNotNull(response);
    assertEquals("metrics-123", response.getMetricsId());
    assertEquals("Test Metrics", response.getMetricsName());
    assertEquals("Test Metrics Description", response.getDescription());
    assertEquals(SettlementMetrics.MetricsType.PERFORMANCE, response.getMetricsType());
    assertEquals(SettlementMetrics.MetricsCategory.THROUGHPUT, response.getCategory());
    assertEquals("test_metric", response.getMetricName());
    assertEquals(BigDecimal.valueOf(100.0), response.getMetricValue());
    assertEquals("count", response.getMetricUnit());
    assertEquals(BigDecimal.valueOf(80.0), response.getBaselineValue());
    assertEquals(BigDecimal.valueOf(120.0), response.getTargetValue());
    assertEquals(BigDecimal.valueOf(50.0), response.getThresholdMin());
    assertEquals(BigDecimal.valueOf(150.0), response.getThresholdMax());
    assertEquals("PART001", response.getParticipantId());
    assertEquals(1L, response.getWorkflowId());
    assertEquals(1L, response.getOrchestrationId());
    assertEquals("USD", response.getCurrency());
    
    verify(metricsRepository).save(any(SettlementMetrics.class));
  }
  
  @Test
  void testCreateMetrics_Exception() {
    // Given
    SettlementMetricsRequest request = SettlementMetricsRequest.builder()
        .metricsName("Test Metrics")
        .metricsType(SettlementMetrics.MetricsType.PERFORMANCE)
        .category(SettlementMetrics.MetricsCategory.THROUGHPUT)
        .metricName("test_metric")
        .build();
    
    when(metricsRepository.save(any(SettlementMetrics.class)))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(SettlementMonitoringException.class, () -> {
      monitoringService.createMetrics(request);
    });
    
    verify(metricsRepository).save(any(SettlementMetrics.class));
  }
  
  @Test
  void testGetAllMonitoring_Success() {
    // Given
    SettlementMonitoring monitoring1 = SettlementMonitoring.builder()
        .id(1L)
        .monitoringId("monitoring-1")
        .monitoringName("Monitoring 1")
        .monitoringType(SettlementMonitoring.MonitoringType.PERFORMANCE)
        .status(SettlementMonitoring.MonitoringStatus.ACTIVE)
        .metricName("metric1")
        .metricValue(BigDecimal.valueOf(100.0))
        .tenantId(tenantId)
        .build();
    
    SettlementMonitoring monitoring2 = SettlementMonitoring.builder()
        .id(2L)
        .monitoringId("monitoring-2")
        .monitoringName("Monitoring 2")
        .monitoringType(SettlementMonitoring.MonitoringType.HEALTH)
        .status(SettlementMonitoring.MonitoringStatus.ACTIVE)
        .metricName("metric2")
        .metricValue(BigDecimal.valueOf(200.0))
        .tenantId(tenantId)
        .build();
    
    List<SettlementMonitoring> monitoringList = List.of(monitoring1, monitoring2);
    
    when(monitoringRepository.findByTenantId(tenantId)).thenReturn(monitoringList);
    
    // When
    List<SettlementMonitoringResponse> responses = monitoringService.getAllMonitoring();
    
    // Then
    assertNotNull(responses);
    assertEquals(2, responses.size());
    
    SettlementMonitoringResponse response1 = responses.get(0);
    assertEquals("monitoring-1", response1.getMonitoringId());
    assertEquals("Monitoring 1", response1.getMonitoringName());
    assertEquals(SettlementMonitoring.MonitoringType.PERFORMANCE, response1.getMonitoringType());
    assertEquals(SettlementMonitoring.MonitoringStatus.ACTIVE, response1.getStatus());
    assertEquals("metric1", response1.getMetricName());
    assertEquals(BigDecimal.valueOf(100.0), response1.getMetricValue());
    
    SettlementMonitoringResponse response2 = responses.get(1);
    assertEquals("monitoring-2", response2.getMonitoringId());
    assertEquals("Monitoring 2", response2.getMonitoringName());
    assertEquals(SettlementMonitoring.MonitoringType.HEALTH, response2.getMonitoringType());
    assertEquals(SettlementMonitoring.MonitoringStatus.ACTIVE, response2.getStatus());
    assertEquals("metric2", response2.getMetricName());
    assertEquals(BigDecimal.valueOf(200.0), response2.getMetricValue());
    
    verify(monitoringRepository).findByTenantId(tenantId);
  }
  
  @Test
  void testGetAllMonitoring_Exception() {
    // Given
    when(monitoringRepository.findByTenantId(tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(SettlementMonitoringException.class, () -> {
      monitoringService.getAllMonitoring();
    });
    
    verify(monitoringRepository).findByTenantId(tenantId);
  }
  
  @Test
  void testGetAllAlerts_Success() {
    // Given
    SettlementAlert alert1 = SettlementAlert.builder()
        .id(1L)
        .alertId("alert-1")
        .alertName("Alert 1")
        .alertType(SettlementAlert.AlertType.PERFORMANCE)
        .severity(SettlementAlert.AlertSeverity.HIGH)
        .status(SettlementAlert.AlertStatus.ACTIVE)
        .alertMessage("Alert message 1")
        .tenantId(tenantId)
        .build();
    
    SettlementAlert alert2 = SettlementAlert.builder()
        .id(2L)
        .alertId("alert-2")
        .alertName("Alert 2")
        .alertType(SettlementAlert.AlertType.HEALTH)
        .severity(SettlementAlert.AlertSeverity.MEDIUM)
        .status(SettlementAlert.AlertStatus.ACKNOWLEDGED)
        .alertMessage("Alert message 2")
        .tenantId(tenantId)
        .build();
    
    List<SettlementAlert> alertList = List.of(alert1, alert2);
    
    when(alertRepository.findByTenantId(tenantId)).thenReturn(alertList);
    
    // When
    List<SettlementAlertResponse> responses = monitoringService.getAllAlerts();
    
    // Then
    assertNotNull(responses);
    assertEquals(2, responses.size());
    
    SettlementAlertResponse response1 = responses.get(0);
    assertEquals("alert-1", response1.getAlertId());
    assertEquals("Alert 1", response1.getAlertName());
    assertEquals(SettlementAlert.AlertType.PERFORMANCE, response1.getAlertType());
    assertEquals(SettlementAlert.AlertSeverity.HIGH, response1.getSeverity());
    assertEquals(SettlementAlert.AlertStatus.ACTIVE, response1.getStatus());
    assertEquals("Alert message 1", response1.getAlertMessage());
    
    SettlementAlertResponse response2 = responses.get(1);
    assertEquals("alert-2", response2.getAlertId());
    assertEquals("Alert 2", response2.getAlertName());
    assertEquals(SettlementAlert.AlertType.HEALTH, response2.getAlertType());
    assertEquals(SettlementAlert.AlertSeverity.MEDIUM, response2.getSeverity());
    assertEquals(SettlementAlert.AlertStatus.ACKNOWLEDGED, response2.getStatus());
    assertEquals("Alert message 2", response2.getAlertMessage());
    
    verify(alertRepository).findByTenantId(tenantId);
  }
  
  @Test
  void testGetAllAlerts_Exception() {
    // Given
    when(alertRepository.findByTenantId(tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(SettlementMonitoringException.class, () -> {
      monitoringService.getAllAlerts();
    });
    
    verify(alertRepository).findByTenantId(tenantId);
  }
  
  @Test
  void testGetAllMetrics_Success() {
    // Given
    SettlementMetrics metrics1 = SettlementMetrics.builder()
        .id(1L)
        .metricsId("metrics-1")
        .metricsName("Metrics 1")
        .metricsType(SettlementMetrics.MetricsType.PERFORMANCE)
        .category(SettlementMetrics.MetricsCategory.THROUGHPUT)
        .metricName("metric1")
        .metricValue(BigDecimal.valueOf(100.0))
        .tenantId(tenantId)
        .build();
    
    SettlementMetrics metrics2 = SettlementMetrics.builder()
        .id(2L)
        .metricsId("metrics-2")
        .metricsName("Metrics 2")
        .metricsType(SettlementMetrics.MetricsType.BUSINESS)
        .category(SettlementMetrics.MetricsCategory.VOLUME)
        .metricName("metric2")
        .metricValue(BigDecimal.valueOf(200.0))
        .tenantId(tenantId)
        .build();
    
    List<SettlementMetrics> metricsList = List.of(metrics1, metrics2);
    
    when(metricsRepository.findByTenantId(tenantId)).thenReturn(metricsList);
    
    // When
    List<SettlementMetricsResponse> responses = monitoringService.getAllMetrics();
    
    // Then
    assertNotNull(responses);
    assertEquals(2, responses.size());
    
    SettlementMetricsResponse response1 = responses.get(0);
    assertEquals("metrics-1", response1.getMetricsId());
    assertEquals("Metrics 1", response1.getMetricsName());
    assertEquals(SettlementMetrics.MetricsType.PERFORMANCE, response1.getMetricsType());
    assertEquals(SettlementMetrics.MetricsCategory.THROUGHPUT, response1.getCategory());
    assertEquals("metric1", response1.getMetricName());
    assertEquals(BigDecimal.valueOf(100.0), response1.getMetricValue());
    
    SettlementMetricsResponse response2 = responses.get(1);
    assertEquals("metrics-2", response2.getMetricsId());
    assertEquals("Metrics 2", response2.getMetricsName());
    assertEquals(SettlementMetrics.MetricsType.BUSINESS, response2.getMetricsType());
    assertEquals(SettlementMetrics.MetricsCategory.VOLUME, response2.getCategory());
    assertEquals("metric2", response2.getMetricName());
    assertEquals(BigDecimal.valueOf(200.0), response2.getMetricValue());
    
    verify(metricsRepository).findByTenantId(tenantId);
  }
  
  @Test
  void testGetAllMetrics_Exception() {
    // Given
    when(metricsRepository.findByTenantId(tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(SettlementMonitoringException.class, () -> {
      monitoringService.getAllMetrics();
    });
    
    verify(metricsRepository).findByTenantId(tenantId);
  }
  
  @Test
  void testAcknowledgeAlert_Success() {
    // Given
    String alertId = "alert-123";
    String acknowledgedBy = "user123";
    
    SettlementAlert alert = SettlementAlert.builder()
        .id(1L)
        .alertId(alertId)
        .alertName("Test Alert")
        .alertType(SettlementAlert.AlertType.PERFORMANCE)
        .severity(SettlementAlert.AlertSeverity.HIGH)
        .status(SettlementAlert.AlertStatus.ACTIVE)
        .alertMessage("Test alert message")
        .tenantId(tenantId)
        .build();
    
    SettlementAlert acknowledgedAlert = SettlementAlert.builder()
        .id(1L)
        .alertId(alertId)
        .alertName("Test Alert")
        .alertType(SettlementAlert.AlertType.PERFORMANCE)
        .severity(SettlementAlert.AlertSeverity.HIGH)
        .status(SettlementAlert.AlertStatus.ACKNOWLEDGED)
        .alertMessage("Test alert message")
        .acknowledgedAt(LocalDateTime.now())
        .acknowledgedBy(acknowledgedBy)
        .tenantId(tenantId)
        .build();
    
    when(alertRepository.findByAlertIdAndTenantId(alertId, tenantId))
        .thenReturn(Optional.of(alert));
    when(alertRepository.save(any(SettlementAlert.class)))
        .thenReturn(acknowledgedAlert);
    
    // When
    SettlementAlertResponse response = monitoringService.acknowledgeAlert(alertId, acknowledgedBy);
    
    // Then
    assertNotNull(response);
    assertEquals(alertId, response.getAlertId());
    assertEquals("Test Alert", response.getAlertName());
    assertEquals(SettlementAlert.AlertType.PERFORMANCE, response.getAlertType());
    assertEquals(SettlementAlert.AlertSeverity.HIGH, response.getSeverity());
    assertEquals(SettlementAlert.AlertStatus.ACKNOWLEDGED, response.getStatus());
    assertEquals("Test alert message", response.getAlertMessage());
    assertNotNull(response.getAcknowledgedAt());
    assertEquals(acknowledgedBy, response.getAcknowledgedBy());
    
    verify(alertRepository).findByAlertIdAndTenantId(alertId, tenantId);
    verify(alertRepository).save(any(SettlementAlert.class));
  }
  
  @Test
  void testAcknowledgeAlert_NotFound() {
    // Given
    String alertId = "alert-123";
    String acknowledgedBy = "user123";
    
    when(alertRepository.findByAlertIdAndTenantId(alertId, tenantId))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(SettlementMonitoringException.class, () -> {
      monitoringService.acknowledgeAlert(alertId, acknowledgedBy);
    });
    
    verify(alertRepository).findByAlertIdAndTenantId(alertId, tenantId);
    verify(alertRepository, never()).save(any(SettlementAlert.class));
  }
  
  @Test
  void testAcknowledgeAlert_Exception() {
    // Given
    String alertId = "alert-123";
    String acknowledgedBy = "user123";
    
    when(alertRepository.findByAlertIdAndTenantId(alertId, tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(SettlementMonitoringException.class, () -> {
      monitoringService.acknowledgeAlert(alertId, acknowledgedBy);
    });
    
    verify(alertRepository).findByAlertIdAndTenantId(alertId, tenantId);
  }
  
  @Test
  void testResolveAlert_Success() {
    // Given
    String alertId = "alert-123";
    String resolvedBy = "user123";
    String resolutionNotes = "Alert resolved";
    
    SettlementAlert alert = SettlementAlert.builder()
        .id(1L)
        .alertId(alertId)
        .alertName("Test Alert")
        .alertType(SettlementAlert.AlertType.PERFORMANCE)
        .severity(SettlementAlert.AlertSeverity.HIGH)
        .status(SettlementAlert.AlertStatus.ACTIVE)
        .alertMessage("Test alert message")
        .tenantId(tenantId)
        .build();
    
    SettlementAlert resolvedAlert = SettlementAlert.builder()
        .id(1L)
        .alertId(alertId)
        .alertName("Test Alert")
        .alertType(SettlementAlert.AlertType.PERFORMANCE)
        .severity(SettlementAlert.AlertSeverity.HIGH)
        .status(SettlementAlert.AlertStatus.RESOLVED)
        .alertMessage("Test alert message")
        .resolvedAt(LocalDateTime.now())
        .resolvedBy(resolvedBy)
        .resolutionNotes(resolutionNotes)
        .tenantId(tenantId)
        .build();
    
    when(alertRepository.findByAlertIdAndTenantId(alertId, tenantId))
        .thenReturn(Optional.of(alert));
    when(alertRepository.save(any(SettlementAlert.class)))
        .thenReturn(resolvedAlert);
    
    // When
    SettlementAlertResponse response = monitoringService.resolveAlert(alertId, resolvedBy, resolutionNotes);
    
    // Then
    assertNotNull(response);
    assertEquals(alertId, response.getAlertId());
    assertEquals("Test Alert", response.getAlertName());
    assertEquals(SettlementAlert.AlertType.PERFORMANCE, response.getAlertType());
    assertEquals(SettlementAlert.AlertSeverity.HIGH, response.getSeverity());
    assertEquals(SettlementAlert.AlertStatus.RESOLVED, response.getStatus());
    assertEquals("Test alert message", response.getAlertMessage());
    assertNotNull(response.getResolvedAt());
    assertEquals(resolvedBy, response.getResolvedBy());
    assertEquals(resolutionNotes, response.getResolutionNotes());
    
    verify(alertRepository).findByAlertIdAndTenantId(alertId, tenantId);
    verify(alertRepository).save(any(SettlementAlert.class));
  }
  
  @Test
  void testResolveAlert_NotFound() {
    // Given
    String alertId = "alert-123";
    String resolvedBy = "user123";
    String resolutionNotes = "Alert resolved";
    
    when(alertRepository.findByAlertIdAndTenantId(alertId, tenantId))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(SettlementMonitoringException.class, () -> {
      monitoringService.resolveAlert(alertId, resolvedBy, resolutionNotes);
    });
    
    verify(alertRepository).findByAlertIdAndTenantId(alertId, tenantId);
    verify(alertRepository, never()).save(any(SettlementAlert.class));
  }
  
  @Test
  void testResolveAlert_Exception() {
    // Given
    String alertId = "alert-123";
    String resolvedBy = "user123";
    String resolutionNotes = "Alert resolved";
    
    when(alertRepository.findByAlertIdAndTenantId(alertId, tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(SettlementMonitoringException.class, () -> {
      monitoringService.resolveAlert(alertId, resolvedBy, resolutionNotes);
    });
    
    verify(alertRepository).findByAlertIdAndTenantId(alertId, tenantId);
  }
}
