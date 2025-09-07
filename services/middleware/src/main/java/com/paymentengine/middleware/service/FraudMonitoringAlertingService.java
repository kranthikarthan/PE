package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.FraudRiskAssessment;
import com.paymentengine.middleware.entity.FraudRiskConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for fraud monitoring and alerting
 */
public interface FraudMonitoringAlertingService {
    
    /**
     * Process fraud/risk assessment for monitoring and alerting
     */
    void processAssessmentForMonitoring(FraudRiskAssessment assessment);
    
    /**
     * Check for high-risk patterns and trigger alerts
     */
    void checkHighRiskPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Check for critical-risk patterns and trigger alerts
     */
    void checkCriticalRiskPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Check for API failure patterns and trigger alerts
     */
    void checkApiFailurePatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Check for unusual transaction patterns and trigger alerts
     */
    void checkUnusualTransactionPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Check for velocity-based fraud patterns and trigger alerts
     */
    void checkVelocityBasedPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Check for geographic anomaly patterns and trigger alerts
     */
    void checkGeographicAnomalyPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Check for device fingerprinting anomalies and trigger alerts
     */
    void checkDeviceFingerprintingAnomalies(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Check for account takeover patterns and trigger alerts
     */
    void checkAccountTakeoverPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Check for money laundering patterns and trigger alerts
     */
    void checkMoneyLaunderingPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Send alert notification
     */
    void sendAlert(String alertType, String severity, String message, Map<String, Object> alertData);
    
    /**
     * Send email alert
     */
    void sendEmailAlert(String recipient, String subject, String message, Map<String, Object> alertData);
    
    /**
     * Send SMS alert
     */
    void sendSmsAlert(String phoneNumber, String message, Map<String, Object> alertData);
    
    /**
     * Send webhook alert
     */
    void sendWebhookAlert(String webhookUrl, String message, Map<String, Object> alertData);
    
    /**
     * Send Slack alert
     */
    void sendSlackAlert(String slackChannel, String message, Map<String, Object> alertData);
    
    /**
     * Send Teams alert
     */
    void sendTeamsAlert(String teamsWebhook, String message, Map<String, Object> alertData);
    
    /**
     * Get alert configuration for tenant
     */
    Map<String, Object> getAlertConfiguration(String tenantId);
    
    /**
     * Update alert configuration for tenant
     */
    void updateAlertConfiguration(String tenantId, Map<String, Object> alertConfig);
    
    /**
     * Get alert history for tenant
     */
    List<Map<String, Object>> getAlertHistory(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get alert statistics for tenant
     */
    Map<String, Object> getAlertStatistics(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Acknowledge alert
     */
    void acknowledgeAlert(String alertId, String acknowledgedBy, String acknowledgmentNote);
    
    /**
     * Resolve alert
     */
    void resolveAlert(String alertId, String resolvedBy, String resolutionNote);
    
    /**
     * Escalate alert
     */
    void escalateAlert(String alertId, String escalatedBy, String escalationReason);
    
    /**
     * Get active alerts for tenant
     */
    List<Map<String, Object>> getActiveAlerts(String tenantId);
    
    /**
     * Get alert by ID
     */
    Map<String, Object> getAlertById(String alertId);
    
    /**
     * Check alert thresholds and trigger alerts if necessary
     */
    void checkAlertThresholds(String tenantId);
    
    /**
     * Run scheduled monitoring tasks
     */
    void runScheduledMonitoringTasks();
    
    /**
     * Get monitoring health status
     */
    Map<String, Object> getMonitoringHealthStatus();
    
    /**
     * Get monitoring metrics
     */
    Map<String, Object> getMonitoringMetrics(String tenantId, LocalDateTime startTime, LocalDateTime endTime);
}