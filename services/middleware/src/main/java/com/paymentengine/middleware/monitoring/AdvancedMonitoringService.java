package com.paymentengine.middleware.monitoring;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AdvancedMonitoringService {
    
    // Metrics Collection
    void recordMetric(String metricName, double value);
    
    void recordMetric(String metricName, double value, Map<String, String> tags);
    
    void recordCounter(String counterName, long increment);
    
    void recordCounter(String counterName, long increment, Map<String, String> tags);
    
    void recordTimer(String timerName, long duration);
    
    void recordTimer(String timerName, long duration, Map<String, String> tags);
    
    void recordGauge(String gaugeName, double value);
    
    void recordGauge(String gaugeName, double value, Map<String, String> tags);
    
    // Custom Metrics
    void recordBusinessMetric(String metricName, double value, String tenantId, String serviceName);
    
    void recordPerformanceMetric(String metricName, double value, String operation, String serviceName);
    
    void recordErrorMetric(String errorType, String serviceName, String operation);
    
    void recordSlaMetric(String slaName, boolean met, String serviceName, String operation);
    
    // Alert Management
    void createAlert(String alertName, String condition, String severity, String description);
    
    void updateAlert(String alertName, String condition, String severity, String description);
    
    void deleteAlert(String alertName);
    
    void enableAlert(String alertName);
    
    void disableAlert(String alertName);
    
    List<Map<String, Object>> getActiveAlerts();
    
    List<Map<String, Object>> getAlertHistory();
    
    // Alert Rules
    void createAlertRule(String ruleName, String metricName, String operator, double threshold, String severity);
    
    void updateAlertRule(String ruleName, String metricName, String operator, double threshold, String severity);
    
    void deleteAlertRule(String ruleName);
    
    List<Map<String, Object>> getAlertRules();
    
    // Alert Notifications
    void sendAlertNotification(String alertName, String message, String severity);
    
    void sendAlertNotification(String alertName, String message, String severity, List<String> recipients);
    
    void configureNotificationChannel(String channelName, String type, Map<String, String> config);
    
    List<Map<String, Object>> getNotificationChannels();
    
    // Health Checks
    void registerHealthCheck(String serviceName, String checkName, HealthCheckFunction checkFunction);
    
    void unregisterHealthCheck(String serviceName, String checkName);
    
    Map<String, Object> getHealthStatus(String serviceName);
    
    Map<String, Object> getOverallHealthStatus();
    
    List<Map<String, Object>> getAllHealthChecks();
    
    // Performance Monitoring
    void startPerformanceMonitoring(String operationName);
    
    void endPerformanceMonitoring(String operationName, boolean success);
    
    Map<String, Object> getPerformanceMetrics(String operationName);
    
    Map<String, Object> getAllPerformanceMetrics();
    
    // Resource Monitoring
    void monitorCpuUsage(String serviceName, double cpuUsage);
    
    void monitorMemoryUsage(String serviceName, double memoryUsage);
    
    void monitorDiskUsage(String serviceName, double diskUsage);
    
    void monitorNetworkUsage(String serviceName, double networkUsage);
    
    Map<String, Object> getResourceMetrics(String serviceName);
    
    Map<String, Object> getAllResourceMetrics();
    
    // Business Metrics
    void recordTransactionCount(String serviceName, String operation, long count);
    
    void recordTransactionValue(String serviceName, String operation, double value);
    
    void recordUserActivity(String userId, String action, String serviceName);
    
    void recordApiUsage(String apiName, String method, int responseCode, long responseTime);
    
    Map<String, Object> getBusinessMetrics(String serviceName);
    
    Map<String, Object> getAllBusinessMetrics();
    
    // Dashboard Management
    void createDashboard(String dashboardName, String description, List<Map<String, Object>> widgets);
    
    void updateDashboard(String dashboardName, String description, List<Map<String, Object>> widgets);
    
    void deleteDashboard(String dashboardName);
    
    List<Map<String, Object>> getDashboards();
    
    Map<String, Object> getDashboard(String dashboardName);
    
    // Report Generation
    String generateReport(String reportType, String serviceName, LocalDateTime from, LocalDateTime to);
    
    String generatePerformanceReport(String serviceName, LocalDateTime from, LocalDateTime to);
    
    String generateErrorReport(String serviceName, LocalDateTime from, LocalDateTime to);
    
    String generateSlaReport(String serviceName, LocalDateTime from, LocalDateTime to);
    
    List<String> getAvailableReports();
    
    // Data Retention
    void setDataRetentionPolicy(String metricType, int retentionDays);
    
    void cleanupOldData();
    
    Map<String, Object> getDataRetentionPolicies();
    
    // Export/Import
    String exportMetrics(String serviceName, String format);
    
    void importMetrics(String serviceName, String data, String format);
    
    String exportAlerts(String format);
    
    void importAlerts(String data, String format);
    
    // Analytics
    Map<String, Object> getMetricAnalytics(String metricName);
    
    Map<String, Object> getTrendAnalysis(String metricName, int days);
    
    Map<String, Object> getAnomalyDetection(String metricName);
    
    Map<String, Object> getCorrelationAnalysis(List<String> metricNames);
    
    // Threshold Management
    void setMetricThreshold(String metricName, double threshold, String operator);
    
    void updateMetricThreshold(String metricName, double threshold, String operator);
    
    void deleteMetricThreshold(String metricName);
    
    List<Map<String, Object>> getMetricThresholds();
    
    // Custom Dashboards
    void createCustomDashboard(String dashboardName, String query, List<String> metrics);
    
    void updateCustomDashboard(String dashboardName, String query, List<String> metrics);
    
    void deleteCustomDashboard(String dashboardName);
    
    List<Map<String, Object>> getCustomDashboards();
    
    // Real-time Monitoring
    void startRealTimeMonitoring(String metricName);
    
    void stopRealTimeMonitoring(String metricName);
    
    List<String> getRealTimeMonitoredMetrics();
    
    // Event Correlation
    void correlateEvents(String eventType1, String eventType2, String correlationRule);
    
    List<Map<String, Object>> getEventCorrelations();
    
    // Predictive Analytics
    Map<String, Object> predictMetricTrend(String metricName, int days);
    
    Map<String, Object> predictAnomaly(String metricName);
    
    Map<String, Object> predictCapacity(String serviceName);
    
    // Compliance Monitoring
    void setComplianceRule(String ruleName, String metricName, String condition, String severity);
    
    List<Map<String, Object>> getComplianceViolations();
    
    Map<String, Object> getComplianceStatus();
    
    // Cost Monitoring
    void recordCostMetric(String serviceName, String resourceType, double cost);
    
    Map<String, Object> getCostMetrics(String serviceName);
    
    Map<String, Object> getAllCostMetrics();
    
    // SLA Monitoring
    void setSlaTarget(String serviceName, String operation, double target, String metric);
    
    Map<String, Object> getSlaStatus(String serviceName);
    
    Map<String, Object> getAllSlaStatus();
    
    // Functional interface for health checks
    @FunctionalInterface
    interface HealthCheckFunction {
        boolean check();
    }
}