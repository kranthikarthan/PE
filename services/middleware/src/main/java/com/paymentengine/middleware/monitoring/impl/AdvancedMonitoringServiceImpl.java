package com.paymentengine.middleware.monitoring.impl;

import com.paymentengine.middleware.monitoring.AdvancedMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AdvancedMonitoringServiceImpl implements AdvancedMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedMonitoringServiceImpl.class);
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private final Map<String, MetricData> metrics = new ConcurrentHashMap<>();
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();
    private final Map<String, HealthCheckFunction> healthChecks = new ConcurrentHashMap<>();
    private final Map<String, Dashboard> dashboards = new ConcurrentHashMap<>();
    private final Map<String, NotificationChannel> notificationChannels = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, Double> gauges = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> timers = new ConcurrentHashMap<>();
    
    @Override
    public void recordMetric(String metricName, double value) {
        recordMetric(metricName, value, new HashMap<>());
    }
    
    @Override
    public void recordMetric(String metricName, double value, Map<String, String> tags) {
        MetricData metric = new MetricData();
        metric.setName(metricName);
        metric.setValue(value);
        metric.setTags(tags);
        metric.setTimestamp(LocalDateTime.now());
        
        metrics.put(metricName, metric);
        
        // Check alert rules
        checkAlertRules(metricName, value);
        
        // Publish to Kafka
        kafkaTemplate.send("metrics", metric);
        
        logger.debug("Recorded metric: {} = {}", metricName, value);
    }
    
    @Override
    public void recordCounter(String counterName, long increment) {
        recordCounter(counterName, increment, new HashMap<>());
    }
    
    @Override
    public void recordCounter(String counterName, long increment, Map<String, String> tags) {
        counters.computeIfAbsent(counterName, k -> new AtomicLong(0)).addAndGet(increment);
        
        MetricData metric = new MetricData();
        metric.setName(counterName);
        metric.setValue(counters.get(counterName).get());
        metric.setTags(tags);
        metric.setTimestamp(LocalDateTime.now());
        metric.setType("COUNTER");
        
        metrics.put(counterName, metric);
        
        // Check alert rules
        checkAlertRules(counterName, metric.getValue());
        
        // Publish to Kafka
        kafkaTemplate.send("metrics", metric);
        
        logger.debug("Recorded counter: {} += {}", counterName, increment);
    }
    
    @Override
    public void recordTimer(String timerName, long duration) {
        recordTimer(timerName, duration, new HashMap<>());
    }
    
    @Override
    public void recordTimer(String timerName, long duration, Map<String, String> tags) {
        timers.computeIfAbsent(timerName, k -> new ArrayList<>()).add(duration);
        
        MetricData metric = new MetricData();
        metric.setName(timerName);
        metric.setValue(duration);
        metric.setTags(tags);
        metric.setTimestamp(LocalDateTime.now());
        metric.setType("TIMER");
        
        metrics.put(timerName, metric);
        
        // Check alert rules
        checkAlertRules(timerName, duration);
        
        // Publish to Kafka
        kafkaTemplate.send("metrics", metric);
        
        logger.debug("Recorded timer: {} = {}ms", timerName, duration);
    }
    
    @Override
    public void recordGauge(String gaugeName, double value) {
        recordGauge(gaugeName, value, new HashMap<>());
    }
    
    @Override
    public void recordGauge(String gaugeName, double value, Map<String, String> tags) {
        gauges.put(gaugeName, value);
        
        MetricData metric = new MetricData();
        metric.setName(gaugeName);
        metric.setValue(value);
        metric.setTags(tags);
        metric.setTimestamp(LocalDateTime.now());
        metric.setType("GAUGE");
        
        metrics.put(gaugeName, metric);
        
        // Check alert rules
        checkAlertRules(gaugeName, value);
        
        // Publish to Kafka
        kafkaTemplate.send("metrics", metric);
        
        logger.debug("Recorded gauge: {} = {}", gaugeName, value);
    }
    
    @Override
    public void recordBusinessMetric(String metricName, double value, String tenantId, String serviceName) {
        Map<String, String> tags = new HashMap<>();
        tags.put("tenantId", tenantId);
        tags.put("serviceName", serviceName);
        tags.put("metricType", "BUSINESS");
        
        recordMetric(metricName, value, tags);
    }
    
    @Override
    public void recordPerformanceMetric(String metricName, double value, String operation, String serviceName) {
        Map<String, String> tags = new HashMap<>();
        tags.put("operation", operation);
        tags.put("serviceName", serviceName);
        tags.put("metricType", "PERFORMANCE");
        
        recordMetric(metricName, value, tags);
    }
    
    @Override
    public void recordErrorMetric(String errorType, String serviceName, String operation) {
        Map<String, String> tags = new HashMap<>();
        tags.put("errorType", errorType);
        tags.put("serviceName", serviceName);
        tags.put("operation", operation);
        tags.put("metricType", "ERROR");
        
        recordCounter("error_count", 1, tags);
    }
    
    @Override
    public void recordSlaMetric(String slaName, boolean met, String serviceName, String operation) {
        Map<String, String> tags = new HashMap<>();
        tags.put("slaName", slaName);
        tags.put("serviceName", serviceName);
        tags.put("operation", operation);
        tags.put("metricType", "SLA");
        
        recordGauge("sla_compliance", met ? 1.0 : 0.0, tags);
    }
    
    @Override
    public void createAlert(String alertName, String condition, String severity, String description) {
        AlertRule rule = new AlertRule();
        rule.setName(alertName);
        rule.setCondition(condition);
        rule.setSeverity(severity);
        rule.setDescription(description);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setActive(true);
        
        alertRules.put(alertName, rule);
        
        logger.info("Created alert: {}", alertName);
    }
    
    @Override
    public void updateAlert(String alertName, String condition, String severity, String description) {
        AlertRule rule = alertRules.get(alertName);
        if (rule != null) {
            rule.setCondition(condition);
            rule.setSeverity(severity);
            rule.setDescription(description);
            rule.setUpdatedAt(LocalDateTime.now());
            
            logger.info("Updated alert: {}", alertName);
        }
    }
    
    @Override
    public void deleteAlert(String alertName) {
        alertRules.remove(alertName);
        logger.info("Deleted alert: {}", alertName);
    }
    
    @Override
    public void enableAlert(String alertName) {
        AlertRule rule = alertRules.get(alertName);
        if (rule != null) {
            rule.setActive(true);
            logger.info("Enabled alert: {}", alertName);
        }
    }
    
    @Override
    public void disableAlert(String alertName) {
        AlertRule rule = alertRules.get(alertName);
        if (rule != null) {
            rule.setActive(false);
            logger.info("Disabled alert: {}", alertName);
        }
    }
    
    @Override
    public List<Map<String, Object>> getActiveAlerts() {
        List<Map<String, Object>> activeAlerts = new ArrayList<>();
        
        for (AlertRule rule : alertRules.values()) {
            if (rule.isActive()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("name", rule.getName());
                alert.put("condition", rule.getCondition());
                alert.put("severity", rule.getSeverity());
                alert.put("description", rule.getDescription());
                alert.put("createdAt", rule.getCreatedAt());
                alert.put("updatedAt", rule.getUpdatedAt());
                activeAlerts.add(alert);
            }
        }
        
        return activeAlerts;
    }
    
    @Override
    public List<Map<String, Object>> getAlertHistory() {
        // Implementation for alert history
        return new ArrayList<>();
    }
    
    @Override
    public void createAlertRule(String ruleName, String metricName, String operator, double threshold, String severity) {
        AlertRule rule = new AlertRule();
        rule.setName(ruleName);
        rule.setMetricName(metricName);
        rule.setOperator(operator);
        rule.setThreshold(threshold);
        rule.setSeverity(severity);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setActive(true);
        
        alertRules.put(ruleName, rule);
        
        logger.info("Created alert rule: {}", ruleName);
    }
    
    @Override
    public void updateAlertRule(String ruleName, String metricName, String operator, double threshold, String severity) {
        AlertRule rule = alertRules.get(ruleName);
        if (rule != null) {
            rule.setMetricName(metricName);
            rule.setOperator(operator);
            rule.setThreshold(threshold);
            rule.setSeverity(severity);
            rule.setUpdatedAt(LocalDateTime.now());
            
            logger.info("Updated alert rule: {}", ruleName);
        }
    }
    
    @Override
    public void deleteAlertRule(String ruleName) {
        alertRules.remove(ruleName);
        logger.info("Deleted alert rule: {}", ruleName);
    }
    
    @Override
    public List<Map<String, Object>> getAlertRules() {
        List<Map<String, Object>> rules = new ArrayList<>();
        
        for (AlertRule rule : alertRules.values()) {
            Map<String, Object> ruleData = new HashMap<>();
            ruleData.put("name", rule.getName());
            ruleData.put("metricName", rule.getMetricName());
            ruleData.put("operator", rule.getOperator());
            ruleData.put("threshold", rule.getThreshold());
            ruleData.put("severity", rule.getSeverity());
            ruleData.put("active", rule.isActive());
            ruleData.put("createdAt", rule.getCreatedAt());
            ruleData.put("updatedAt", rule.getUpdatedAt());
            rules.add(ruleData);
        }
        
        return rules;
    }
    
    @Override
    public void sendAlertNotification(String alertName, String message, String severity) {
        sendAlertNotification(alertName, message, severity, new ArrayList<>());
    }
    
    @Override
    public void sendAlertNotification(String alertName, String message, String severity, List<String> recipients) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("alertName", alertName);
        notification.put("message", message);
        notification.put("severity", severity);
        notification.put("recipients", recipients);
        notification.put("timestamp", LocalDateTime.now());
        
        kafkaTemplate.send("alert-notifications", notification);
        
        logger.warn("Sent alert notification: {} - {}", alertName, message);
    }
    
    @Override
    public void configureNotificationChannel(String channelName, String type, Map<String, String> config) {
        NotificationChannel channel = new NotificationChannel();
        channel.setName(channelName);
        channel.setType(type);
        channel.setConfig(config);
        channel.setCreatedAt(LocalDateTime.now());
        channel.setActive(true);
        
        notificationChannels.put(channelName, channel);
        
        logger.info("Configured notification channel: {}", channelName);
    }
    
    @Override
    public List<Map<String, Object>> getNotificationChannels() {
        List<Map<String, Object>> channels = new ArrayList<>();
        
        for (NotificationChannel channel : notificationChannels.values()) {
            Map<String, Object> channelData = new HashMap<>();
            channelData.put("name", channel.getName());
            channelData.put("type", channel.getType());
            channelData.put("config", channel.getConfig());
            channelData.put("active", channel.isActive());
            channelData.put("createdAt", channel.getCreatedAt());
            channels.add(channelData);
        }
        
        return channels;
    }
    
    @Override
    public void registerHealthCheck(String serviceName, String checkName, HealthCheckFunction checkFunction) {
        String key = serviceName + ":" + checkName;
        healthChecks.put(key, checkFunction);
        
        logger.info("Registered health check: {}:{}", serviceName, checkName);
    }
    
    @Override
    public void unregisterHealthCheck(String serviceName, String checkName) {
        String key = serviceName + ":" + checkName;
        healthChecks.remove(key);
        
        logger.info("Unregistered health check: {}:{}", serviceName, checkName);
    }
    
    @Override
    public Map<String, Object> getHealthStatus(String serviceName) {
        Map<String, Object> status = new HashMap<>();
        status.put("serviceName", serviceName);
        status.put("overallStatus", "HEALTHY");
        status.put("checks", new ArrayList<>());
        
        for (Map.Entry<String, HealthCheckFunction> entry : healthChecks.entrySet()) {
            if (entry.getKey().startsWith(serviceName + ":")) {
                String checkName = entry.getKey().substring(serviceName.length() + 1);
                boolean isHealthy = entry.getValue().check();
                
                Map<String, Object> check = new HashMap<>();
                check.put("name", checkName);
                check.put("status", isHealthy ? "HEALTHY" : "UNHEALTHY");
                check.put("timestamp", LocalDateTime.now());
                
                ((List<Map<String, Object>>) status.get("checks")).add(check);
            }
        }
        
        return status;
    }
    
    @Override
    public Map<String, Object> getOverallHealthStatus() {
        Map<String, Object> overallStatus = new HashMap<>();
        overallStatus.put("overallStatus", "HEALTHY");
        overallStatus.put("services", new ArrayList<>());
        
        Set<String> services = new HashSet<>();
        for (String key : healthChecks.keySet()) {
            services.add(key.split(":")[0]);
        }
        
        for (String service : services) {
            Map<String, Object> serviceStatus = getHealthStatus(service);
            ((List<Map<String, Object>>) overallStatus.get("services")).add(serviceStatus);
        }
        
        return overallStatus;
    }
    
    @Override
    public List<Map<String, Object>> getAllHealthChecks() {
        List<Map<String, Object>> checks = new ArrayList<>();
        
        for (Map.Entry<String, HealthCheckFunction> entry : healthChecks.entrySet()) {
            String[] parts = entry.getKey().split(":");
            String serviceName = parts[0];
            String checkName = parts[1];
            
            Map<String, Object> check = new HashMap<>();
            check.put("serviceName", serviceName);
            check.put("checkName", checkName);
            check.put("status", entry.getValue().check() ? "HEALTHY" : "UNHEALTHY");
            check.put("timestamp", LocalDateTime.now());
            checks.add(check);
        }
        
        return checks;
    }
    
    // Private helper methods
    private void checkAlertRules(String metricName, double value) {
        for (AlertRule rule : alertRules.values()) {
            if (rule.isActive() && rule.getMetricName() != null && rule.getMetricName().equals(metricName)) {
                boolean shouldAlert = false;
                
                switch (rule.getOperator()) {
                    case ">" -> shouldAlert = value > rule.getThreshold();
                    case ">=" -> shouldAlert = value >= rule.getThreshold();
                    case "<" -> shouldAlert = value < rule.getThreshold();
                    case "<=" -> shouldAlert = value <= rule.getThreshold();
                    case "==" -> shouldAlert = value == rule.getThreshold();
                    case "!=" -> shouldAlert = value != rule.getThreshold();
                }
                
                if (shouldAlert) {
                    sendAlertNotification(rule.getName(), 
                        String.format("Alert: %s %s %.2f (threshold: %.2f)", 
                            metricName, rule.getOperator(), value, rule.getThreshold()), 
                        rule.getSeverity());
                }
            }
        }
    }
    
    // Placeholder implementations for remaining methods
    @Override
    public void startPerformanceMonitoring(String operationName) {
        // Implementation for performance monitoring
    }
    
    @Override
    public void endPerformanceMonitoring(String operationName, boolean success) {
        // Implementation for performance monitoring
    }
    
    @Override
    public Map<String, Object> getPerformanceMetrics(String operationName) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getAllPerformanceMetrics() {
        return new HashMap<>();
    }
    
    @Override
    public void monitorCpuUsage(String serviceName, double cpuUsage) {
        recordGauge("cpu_usage", cpuUsage, Map.of("serviceName", serviceName));
    }
    
    @Override
    public void monitorMemoryUsage(String serviceName, double memoryUsage) {
        recordGauge("memory_usage", memoryUsage, Map.of("serviceName", serviceName));
    }
    
    @Override
    public void monitorDiskUsage(String serviceName, double diskUsage) {
        recordGauge("disk_usage", diskUsage, Map.of("serviceName", serviceName));
    }
    
    @Override
    public void monitorNetworkUsage(String serviceName, double networkUsage) {
        recordGauge("network_usage", networkUsage, Map.of("serviceName", serviceName));
    }
    
    @Override
    public Map<String, Object> getResourceMetrics(String serviceName) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getAllResourceMetrics() {
        return new HashMap<>();
    }
    
    @Override
    public void recordTransactionCount(String serviceName, String operation, long count) {
        recordCounter("transaction_count", count, Map.of("serviceName", serviceName, "operation", operation));
    }
    
    @Override
    public void recordTransactionValue(String serviceName, String operation, double value) {
        recordMetric("transaction_value", value, Map.of("serviceName", serviceName, "operation", operation));
    }
    
    @Override
    public void recordUserActivity(String userId, String action, String serviceName) {
        recordCounter("user_activity", 1, Map.of("userId", userId, "action", action, "serviceName", serviceName));
    }
    
    @Override
    public void recordApiUsage(String apiName, String method, int responseCode, long responseTime) {
        recordTimer("api_response_time", responseTime, Map.of("apiName", apiName, "method", method, "responseCode", String.valueOf(responseCode)));
    }
    
    @Override
    public Map<String, Object> getBusinessMetrics(String serviceName) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getAllBusinessMetrics() {
        return new HashMap<>();
    }
    
    @Override
    public void createDashboard(String dashboardName, String description, List<Map<String, Object>> widgets) {
        Dashboard dashboard = new Dashboard();
        dashboard.setName(dashboardName);
        dashboard.setDescription(description);
        dashboard.setWidgets(widgets);
        dashboard.setCreatedAt(LocalDateTime.now());
        
        dashboards.put(dashboardName, dashboard);
    }
    
    @Override
    public void updateDashboard(String dashboardName, String description, List<Map<String, Object>> widgets) {
        Dashboard dashboard = dashboards.get(dashboardName);
        if (dashboard != null) {
            dashboard.setDescription(description);
            dashboard.setWidgets(widgets);
            dashboard.setUpdatedAt(LocalDateTime.now());
        }
    }
    
    @Override
    public void deleteDashboard(String dashboardName) {
        dashboards.remove(dashboardName);
    }
    
    @Override
    public List<Map<String, Object>> getDashboards() {
        List<Map<String, Object>> dashboardList = new ArrayList<>();
        
        for (Dashboard dashboard : dashboards.values()) {
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("name", dashboard.getName());
            dashboardData.put("description", dashboard.getDescription());
            dashboardData.put("widgets", dashboard.getWidgets());
            dashboardData.put("createdAt", dashboard.getCreatedAt());
            dashboardData.put("updatedAt", dashboard.getUpdatedAt());
            dashboardList.add(dashboardData);
        }
        
        return dashboardList;
    }
    
    @Override
    public Map<String, Object> getDashboard(String dashboardName) {
        Dashboard dashboard = dashboards.get(dashboardName);
        if (dashboard != null) {
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("name", dashboard.getName());
            dashboardData.put("description", dashboard.getDescription());
            dashboardData.put("widgets", dashboard.getWidgets());
            dashboardData.put("createdAt", dashboard.getCreatedAt());
            dashboardData.put("updatedAt", dashboard.getUpdatedAt());
            return dashboardData;
        }
        return null;
    }
    
    // Placeholder implementations for remaining methods
    @Override
    public String generateReport(String reportType, String serviceName, LocalDateTime from, LocalDateTime to) {
        return null;
    }
    
    @Override
    public String generatePerformanceReport(String serviceName, LocalDateTime from, LocalDateTime to) {
        return null;
    }
    
    @Override
    public String generateErrorReport(String serviceName, LocalDateTime from, LocalDateTime to) {
        return null;
    }
    
    @Override
    public String generateSlaReport(String serviceName, LocalDateTime from, LocalDateTime to) {
        return null;
    }
    
    @Override
    public List<String> getAvailableReports() {
        return new ArrayList<>();
    }
    
    @Override
    public void setDataRetentionPolicy(String metricType, int retentionDays) {
        // Implementation for data retention policy
    }
    
    @Override
    public void cleanupOldData() {
        // Implementation for data cleanup
    }
    
    @Override
    public Map<String, Object> getDataRetentionPolicies() {
        return new HashMap<>();
    }
    
    @Override
    public String exportMetrics(String serviceName, String format) {
        return null;
    }
    
    @Override
    public void importMetrics(String serviceName, String data, String format) {
        // Implementation for metrics import
    }
    
    @Override
    public String exportAlerts(String format) {
        return null;
    }
    
    @Override
    public void importAlerts(String data, String format) {
        // Implementation for alerts import
    }
    
    @Override
    public Map<String, Object> getMetricAnalytics(String metricName) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getTrendAnalysis(String metricName, int days) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getAnomalyDetection(String metricName) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getCorrelationAnalysis(List<String> metricNames) {
        return new HashMap<>();
    }
    
    @Override
    public void setMetricThreshold(String metricName, double threshold, String operator) {
        // Implementation for metric threshold
    }
    
    @Override
    public void updateMetricThreshold(String metricName, double threshold, String operator) {
        // Implementation for metric threshold update
    }
    
    @Override
    public void deleteMetricThreshold(String metricName) {
        // Implementation for metric threshold deletion
    }
    
    @Override
    public List<Map<String, Object>> getMetricThresholds() {
        return new ArrayList<>();
    }
    
    @Override
    public void createCustomDashboard(String dashboardName, String query, List<String> metrics) {
        // Implementation for custom dashboard
    }
    
    @Override
    public void updateCustomDashboard(String dashboardName, String query, List<String> metrics) {
        // Implementation for custom dashboard update
    }
    
    @Override
    public void deleteCustomDashboard(String dashboardName) {
        // Implementation for custom dashboard deletion
    }
    
    @Override
    public List<Map<String, Object>> getCustomDashboards() {
        return new ArrayList<>();
    }
    
    @Override
    public void startRealTimeMonitoring(String metricName) {
        // Implementation for real-time monitoring
    }
    
    @Override
    public void stopRealTimeMonitoring(String metricName) {
        // Implementation for real-time monitoring
    }
    
    @Override
    public List<String> getRealTimeMonitoredMetrics() {
        return new ArrayList<>();
    }
    
    @Override
    public void correlateEvents(String eventType1, String eventType2, String correlationRule) {
        // Implementation for event correlation
    }
    
    @Override
    public List<Map<String, Object>> getEventCorrelations() {
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, Object> predictMetricTrend(String metricName, int days) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> predictAnomaly(String metricName) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> predictCapacity(String serviceName) {
        return new HashMap<>();
    }
    
    @Override
    public void setComplianceRule(String ruleName, String metricName, String condition, String severity) {
        // Implementation for compliance rule
    }
    
    @Override
    public List<Map<String, Object>> getComplianceViolations() {
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, Object> getComplianceStatus() {
        return new HashMap<>();
    }
    
    @Override
    public void recordCostMetric(String serviceName, String resourceType, double cost) {
        recordMetric("cost", cost, Map.of("serviceName", serviceName, "resourceType", resourceType));
    }
    
    @Override
    public Map<String, Object> getCostMetrics(String serviceName) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getAllCostMetrics() {
        return new HashMap<>();
    }
    
    @Override
    public void setSlaTarget(String serviceName, String operation, double target, String metric) {
        // Implementation for SLA target
    }
    
    @Override
    public Map<String, Object> getSlaStatus(String serviceName) {
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getAllSlaStatus() {
        return new HashMap<>();
    }
    
    // Inner classes for data structures
    private static class MetricData {
        private String name;
        private double value;
        private Map<String, String> tags;
        private LocalDateTime timestamp;
        private String type;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public Map<String, String> getTags() { return tags; }
        public void setTags(Map<String, String> tags) { this.tags = tags; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
    
    private static class AlertRule {
        private String name;
        private String condition;
        private String severity;
        private String description;
        private String metricName;
        private String operator;
        private double threshold;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    private static class Dashboard {
        private String name;
        private String description;
        private List<Map<String, Object>> widgets;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<Map<String, Object>> getWidgets() { return widgets; }
        public void setWidgets(List<Map<String, Object>> widgets) { this.widgets = widgets; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    private static class NotificationChannel {
        private String name;
        private String type;
        private Map<String, String> config;
        private boolean active;
        private LocalDateTime createdAt;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Map<String, String> getConfig() { return config; }
        public void setConfig(Map<String, String> config) { this.config = config; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}