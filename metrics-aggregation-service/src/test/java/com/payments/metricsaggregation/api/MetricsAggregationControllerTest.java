package com.payments.metricsaggregation.api;

import com.payments.metricsaggregation.dto.AlertEventDto;
import com.payments.metricsaggregation.dto.AlertRuleDto;
import com.payments.metricsaggregation.dto.MetricsDataDto;
import com.payments.metricsaggregation.service.AlertManagementService;
import com.payments.metricsaggregation.service.MetricsAggregationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Metrics Aggregation Controller Test
 * 
 * Comprehensive test suite for the Metrics Aggregation REST API.
 * Tests all endpoints with proper authentication and authorization.
 */
@WebMvcTest(MetricsAggregationController.class)
class MetricsAggregationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MetricsAggregationService metricsAggregationService;

    @MockBean
    private AlertManagementService alertManagementService;

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetServiceMetrics_ShouldReturnMetrics() throws Exception {
        // Given
        List<MetricsDataDto> mockMetrics = List.of(
            MetricsDataDto.builder()
                .serviceName("payment-initiation-service")
                .metricName("http_server_requests_total")
                .value(150.5)
                .timestamp(Instant.now())
                .build()
        );
        
        when(metricsAggregationService.getServiceMetrics(anyString(), any(Duration.class)))
            .thenReturn(mockMetrics);

        // When & Then
        mockMvc.perform(get("/api/metrics/v1/services/payment-initiation-service/metrics")
                .param("timeWindowSeconds", "300")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].serviceName").value("payment-initiation-service"))
            .andExpect(jsonPath("$[0].metricName").value("http_server_requests_total"))
            .andExpect(jsonPath("$[0].value").value(150.5));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetMetricsSummary_ShouldReturnSummary() throws Exception {
        // Given
        MetricsDataDto.MetricsSummary mockSummary = MetricsDataDto.MetricsSummary.builder()
            .serviceName("payment-initiation-service")
            .metricName("http_server_requests_total")
            .currentValue(150.5)
            .averageValue(145.2)
            .minValue(100.0)
            .maxValue(200.0)
            .dataPointCount(100L)
            .lastUpdated(Instant.now())
            .build();
        
        when(metricsAggregationService.getMetricsSummary(anyString(), anyString(), any(Duration.class)))
            .thenReturn(mockSummary);

        // When & Then
        mockMvc.perform(get("/api/metrics/v1/services/payment-initiation-service/summary")
                .param("metric", "http_server_requests_total")
                .param("timeWindowSeconds", "3600")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.serviceName").value("payment-initiation-service"))
            .andExpect(jsonPath("$.metricName").value("http_server_requests_total"))
            .andExpect(jsonPath("$.currentValue").value(150.5))
            .andExpect(jsonPath("$.averageValue").value(145.2));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetTimeSeriesData_ShouldReturnTimeSeries() throws Exception {
        // Given
        List<MetricsDataDto.TimeSeriesPoint> mockTimeSeries = List.of(
            MetricsDataDto.TimeSeriesPoint.builder()
                .timestamp(Instant.now())
                .value(150.5)
                .labels(Map.of("method", "GET", "status", "200"))
                .build()
        );
        
        when(metricsAggregationService.getTimeSeriesData(anyString(), anyString(), any(Duration.class)))
            .thenReturn(mockTimeSeries);

        // When & Then
        mockMvc.perform(get("/api/metrics/v1/services/payment-initiation-service/timeseries")
                .param("metric", "http_server_requests_total")
                .param("timeWindowSeconds", "3600")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].value").value(150.5))
            .andExpect(jsonPath("$[0].labels.method").value("GET"));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetDashboardData_ShouldReturnDashboard() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/metrics/v1/dashboard")
                .param("timeWindowSeconds", "300")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalServices").value(22))
            .andExpect(jsonPath("$.healthyServices").value(20))
            .andExpect(jsonPath("$.totalAlerts").value(5))
            .andExpect(jsonPath("$.avgResponseTime").value(150.5));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetAllAlertRules_ShouldReturnAlertRules() throws Exception {
        // Given
        List<AlertRuleDto> mockAlertRules = List.of(
            AlertRuleDto.builder()
                .id(1L)
                .ruleName("High Error Rate")
                .description("Alert when error rate exceeds 5%")
                .serviceName("payment-initiation-service")
                .metricName("http_server_requests_error_rate")
                .severity(AlertRuleDto.AlertRuleEntity.Severity.HIGH)
                .isEnabled(true)
                .build()
        );
        
        when(alertManagementService.getAllAlertRules()).thenReturn(mockAlertRules);

        // When & Then
        mockMvc.perform(get("/api/metrics/v1/alert-rules")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].ruleName").value("High Error Rate"))
            .andExpect(jsonPath("$[0].serviceName").value("payment-initiation-service"))
            .andExpect(jsonPath("$[0].severity").value("HIGH"));
    }

    @Test
    @WithMockUser(roles = "OPS_ADMIN")
    void testCreateAlertRule_ShouldReturnCreatedRule() throws Exception {
        // Given
        AlertRuleDto.CreateRequest request = AlertRuleDto.CreateRequest.builder()
            .ruleName("High Response Time")
            .description("Alert when response time exceeds 1 second")
            .serviceName("payment-initiation-service")
            .metricName("http_server_requests_duration_seconds")
            .conditionType(AlertRuleDto.AlertRuleEntity.ConditionType.GREATER_THAN)
            .thresholdValue(1.0)
            .evaluationWindowSeconds(300)
            .severity(AlertRuleDto.AlertRuleEntity.Severity.MEDIUM)
            .notificationChannels(List.of("email", "slack"))
            .isEnabled(true)
            .build();

        AlertRuleDto mockCreatedRule = AlertRuleDto.builder()
            .id(1L)
            .ruleName("High Response Time")
            .description("Alert when response time exceeds 1 second")
            .serviceName("payment-initiation-service")
            .metricName("http_server_requests_duration_seconds")
            .conditionType(AlertRuleDto.AlertRuleEntity.ConditionType.GREATER_THAN)
            .thresholdValue(1.0)
            .evaluationWindowSeconds(300)
            .severity(AlertRuleDto.AlertRuleEntity.Severity.MEDIUM)
            .notificationChannels(List.of("email", "slack"))
            .isEnabled(true)
            .build();
        
        when(alertManagementService.createAlertRule(any(), anyString())).thenReturn(mockCreatedRule);

        // When & Then
        mockMvc.perform(post("/api/metrics/v1/alert-rules")
                .header("X-User-ID", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ruleName").value("High Response Time"))
            .andExpect(jsonPath("$.serviceName").value("payment-initiation-service"))
            .andExpect(jsonPath("$.severity").value("MEDIUM"));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetActiveAlerts_ShouldReturnAlerts() throws Exception {
        // Given
        List<AlertEventDto> mockAlerts = List.of(
            AlertEventDto.builder()
                .id(1L)
                .alertId("alert-123")
                .ruleId(1L)
                .serviceName("payment-initiation-service")
                .metricName("http_server_requests_error_rate")
                .metricValue(0.08)
                .thresholdValue(0.05)
                .status(AlertEventDto.AlertEventEntity.Status.TRIGGERED)
                .severity(AlertEventDto.AlertRuleEntity.Severity.HIGH)
                .message("Error rate exceeded threshold")
                .triggeredAt(Instant.now())
                .build()
        );
        
        when(alertManagementService.getActiveAlerts()).thenReturn(mockAlerts);

        // When & Then
        mockMvc.perform(get("/api/metrics/v1/alerts")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].alertId").value("alert-123"))
            .andExpect(jsonPath("$[0].serviceName").value("payment-initiation-service"))
            .andExpect(jsonPath("$[0].status").value("TRIGGERED"))
            .andExpect(jsonPath("$[0].severity").value("HIGH"));
    }

    @Test
    @WithMockUser(roles = "OPS_OPERATOR")
    void testAcknowledgeAlert_ShouldReturnAcknowledgedAlert() throws Exception {
        // Given
        AlertEventDto.AcknowledgeRequest request = AlertEventDto.AcknowledgeRequest.builder()
            .acknowledgedBy("operator")
            .message("Alert acknowledged")
            .build();

        AlertEventDto mockAcknowledgedAlert = AlertEventDto.builder()
            .id(1L)
            .alertId("alert-123")
            .status(AlertEventDto.AlertEventEntity.Status.ACKNOWLEDGED)
            .acknowledgedBy("operator")
            .acknowledgedAt(Instant.now())
            .build();
        
        when(alertManagementService.acknowledgeAlert(any(), any())).thenReturn(mockAcknowledgedAlert);

        // When & Then
        mockMvc.perform(post("/api/metrics/v1/alerts/1/acknowledge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.alertId").value("alert-123"))
            .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"))
            .andExpect(jsonPath("$.acknowledgedBy").value("operator"));
    }

    @Test
    @WithMockUser(roles = "OPS_OPERATOR")
    void testResolveAlert_ShouldReturnResolvedAlert() throws Exception {
        // Given
        AlertEventDto.ResolveRequest request = AlertEventDto.ResolveRequest.builder()
            .resolvedBy("operator")
            .message("Alert resolved")
            .build();

        AlertEventDto mockResolvedAlert = AlertEventDto.builder()
            .id(1L)
            .alertId("alert-123")
            .status(AlertEventDto.AlertEventEntity.Status.RESOLVED)
            .resolvedBy("operator")
            .resolvedAt(Instant.now())
            .build();
        
        when(alertManagementService.resolveAlert(any(), any())).thenReturn(mockResolvedAlert);

        // When & Then
        mockMvc.perform(post("/api/metrics/v1/alerts/1/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.alertId").value("alert-123"))
            .andExpect(jsonPath("$.status").value("RESOLVED"))
            .andExpect(jsonPath("$.resolvedBy").value("operator"));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetCollectionStatus_ShouldReturnStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/metrics/v1/collection/status")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collectionEnabled").value(true))
            .andExpect(jsonPath("$.servicesMonitored").value(22))
            .andExpect(jsonPath("$.metricsCollected").value(1250));
    }

    @Test
    @WithMockUser(roles = "OPS_ADMIN")
    void testTriggerCollection_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/metrics/v1/collection/trigger")
                .header("X-User-ID", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Metrics collection triggered"))
            .andExpect(jsonPath("$.triggeredBy").value("admin"));
    }

    @Test
    void testGetServiceMetrics_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/metrics/v1/services/payment-initiation-service/metrics")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }
}
