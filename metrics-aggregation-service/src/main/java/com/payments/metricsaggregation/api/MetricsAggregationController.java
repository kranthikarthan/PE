package com.payments.metricsaggregation.api;

import com.payments.metricsaggregation.dto.AlertEventDto;
import com.payments.metricsaggregation.dto.AlertRuleDto;
import com.payments.metricsaggregation.dto.MetricsDataDto;
import com.payments.metricsaggregation.service.AlertManagementService;
import com.payments.metricsaggregation.service.MetricsAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Metrics Aggregation Controller
 * 
 * REST API for metrics aggregation and alert management.
 * Provides real-time metrics, dashboards, and alerting capabilities.
 * 
 * Base URL: /api/metrics/v1
 * Port: 8022
 */
@RestController
@RequestMapping("/api/metrics/v1")
@RequiredArgsConstructor
@Slf4j
public class MetricsAggregationController {

    private final MetricsAggregationService metricsAggregationService;
    private final AlertManagementService alertManagementService;

    /**
     * Get real-time metrics for a service
     * 
     * GET /api/metrics/v1/services/{service}/metrics
     */
    @GetMapping("/services/{service}/metrics")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<MetricsDataDto>> getServiceMetrics(
            @PathVariable String service,
            @RequestParam(defaultValue = "300") int timeWindowSeconds) {
        
        log.info("Getting metrics for service: {} with time window: {}s", service, timeWindowSeconds);
        
        Duration timeWindow = Duration.ofSeconds(timeWindowSeconds);
        List<MetricsDataDto> metrics = metricsAggregationService.getServiceMetrics(service, timeWindow);
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get metrics summary for a service
     * 
     * GET /api/metrics/v1/services/{service}/summary
     */
    @GetMapping("/services/{service}/summary")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<MetricsDataDto.MetricsSummary> getMetricsSummary(
            @PathVariable String service,
            @RequestParam String metric,
            @RequestParam(defaultValue = "3600") int timeWindowSeconds) {
        
        log.info("Getting metrics summary for service: {} metric: {}", service, metric);
        
        Duration timeWindow = Duration.ofSeconds(timeWindowSeconds);
        MetricsDataDto.MetricsSummary summary = metricsAggregationService.getMetricsSummary(service, metric, timeWindow);
        
        return ResponseEntity.ok(summary);
    }

    /**
     * Get time series data for charts
     * 
     * GET /api/metrics/v1/services/{service}/timeseries
     */
    @GetMapping("/services/{service}/timeseries")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<MetricsDataDto.TimeSeriesPoint>> getTimeSeriesData(
            @PathVariable String service,
            @RequestParam String metric,
            @RequestParam(defaultValue = "3600") int timeWindowSeconds) {
        
        log.info("Getting time series data for service: {} metric: {}", service, metric);
        
        Duration timeWindow = Duration.ofSeconds(timeWindowSeconds);
        List<MetricsDataDto.TimeSeriesPoint> timeSeries = metricsAggregationService.getTimeSeriesData(service, metric, timeWindow);
        
        return ResponseEntity.ok(timeSeries);
    }

    /**
     * Get dashboard data for all services
     * 
     * GET /api/metrics/v1/dashboard
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestParam(defaultValue = "300") int timeWindowSeconds) {
        
        log.info("Getting dashboard data with time window: {}s", timeWindowSeconds);
        
        // This would return comprehensive dashboard data
        Map<String, Object> dashboardData = Map.of(
            "totalServices", 22,
            "healthyServices", 20,
            "degradedServices", 1,
            "downServices", 1,
            "totalAlerts", 5,
            "criticalAlerts", 1,
            "highAlerts", 2,
            "mediumAlerts", 2,
            "lowAlerts", 0,
            "avgResponseTime", 150.5,
            "totalTPS", 1250.0,
            "errorRate", 0.12
        );
        
        return ResponseEntity.ok(dashboardData);
    }

    /**
     * Get all alert rules
     * 
     * GET /api/metrics/v1/alert-rules
     */
    @GetMapping("/alert-rules")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<AlertRuleDto>> getAllAlertRules() {
        log.info("Getting all alert rules");
        
        List<AlertRuleDto> alertRules = alertManagementService.getAllAlertRules();
        
        return ResponseEntity.ok(alertRules);
    }

    /**
     * Get alert rules for a specific service
     * 
     * GET /api/metrics/v1/alert-rules/service/{service}
     */
    @GetMapping("/alert-rules/service/{service}")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<AlertRuleDto>> getAlertRulesForService(@PathVariable String service) {
        log.info("Getting alert rules for service: {}", service);
        
        List<AlertRuleDto> alertRules = alertManagementService.getAlertRulesForService(service);
        
        return ResponseEntity.ok(alertRules);
    }

    /**
     * Create a new alert rule
     * 
     * POST /api/metrics/v1/alert-rules
     */
    @PostMapping("/alert-rules")
    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<AlertRuleDto> createAlertRule(
            @RequestBody @Valid AlertRuleDto.CreateRequest request,
            @RequestHeader("X-User-ID") String userId) {
        
        log.info("Creating alert rule: {} by user: {}", request.getRuleName(), userId);
        
        AlertRuleDto createdRule = alertManagementService.createAlertRule(request, userId);
        
        return ResponseEntity.ok(createdRule);
    }

    /**
     * Update an existing alert rule
     * 
     * PUT /api/metrics/v1/alert-rules/{ruleId}
     */
    @PutMapping("/alert-rules/{ruleId}")
    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<AlertRuleDto> updateAlertRule(
            @PathVariable Long ruleId,
            @RequestBody @Valid AlertRuleDto.UpdateRequest request) {
        
        log.info("Updating alert rule: {}", ruleId);
        
        AlertRuleDto updatedRule = alertManagementService.updateAlertRule(ruleId, request);
        
        return ResponseEntity.ok(updatedRule);
    }

    /**
     * Get all active alerts
     * 
     * GET /api/metrics/v1/alerts
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<AlertEventDto>> getActiveAlerts() {
        log.info("Getting all active alerts");
        
        List<AlertEventDto> alerts = alertManagementService.getActiveAlerts();
        
        return ResponseEntity.ok(alerts);
    }

    /**
     * Acknowledge an alert
     * 
     * POST /api/metrics/v1/alerts/{alertId}/acknowledge
     */
    @PostMapping("/alerts/{alertId}/acknowledge")
    @PreAuthorize("hasAnyRole('OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<AlertEventDto> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestBody @Valid AlertEventDto.AcknowledgeRequest request) {
        
        log.info("Acknowledging alert: {} by user: {}", alertId, request.getAcknowledgedBy());
        
        AlertEventDto acknowledgedAlert = alertManagementService.acknowledgeAlert(alertId, request);
        
        return ResponseEntity.ok(acknowledgedAlert);
    }

    /**
     * Resolve an alert
     * 
     * POST /api/metrics/v1/alerts/{alertId}/resolve
     */
    @PostMapping("/alerts/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<AlertEventDto> resolveAlert(
            @PathVariable Long alertId,
            @RequestBody @Valid AlertEventDto.ResolveRequest request) {
        
        log.info("Resolving alert: {} by user: {}", alertId, request.getResolvedBy());
        
        AlertEventDto resolvedAlert = alertManagementService.resolveAlert(alertId, request);
        
        return ResponseEntity.ok(resolvedAlert);
    }

    /**
     * Get metrics collection status
     * 
     * GET /api/metrics/v1/collection/status
     */
    @GetMapping("/collection/status")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getCollectionStatus() {
        log.info("Getting metrics collection status");
        
        Map<String, Object> status = Map.of(
            "collectionEnabled", true,
            "lastCollectionTime", java.time.Instant.now().toString(),
            "servicesMonitored", 22,
            "metricsCollected", 1250,
            "collectionInterval", "30s",
            "nextCollectionTime", java.time.Instant.now().plusSeconds(30).toString()
        );
        
        return ResponseEntity.ok(status);
    }

    /**
     * Trigger metrics collection
     * 
     * POST /api/metrics/v1/collection/trigger
     */
    @PostMapping("/collection/trigger")
    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, String>> triggerCollection(
            @RequestHeader("X-User-ID") String userId) {
        
        log.info("Triggering metrics collection by user: {}", userId);
        
        // This would trigger immediate metrics collection
        // For now, return success response
        
        return ResponseEntity.ok(Map.of(
            "message", "Metrics collection triggered",
            "triggeredBy", userId,
            "timestamp", java.time.Instant.now().toString()
        ));
    }
}
