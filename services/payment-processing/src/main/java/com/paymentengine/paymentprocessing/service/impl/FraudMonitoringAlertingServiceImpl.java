package com.paymentengine.paymentprocessing.service.impl;

import com.paymentengine.paymentprocessing.entity.FraudRiskAssessment;
import com.paymentengine.paymentprocessing.entity.FraudRiskConfiguration;
import com.paymentengine.paymentprocessing.repository.FraudRiskAssessmentRepository;
import com.paymentengine.paymentprocessing.repository.FraudRiskConfigurationRepository;
import com.paymentengine.paymentprocessing.service.FraudMonitoringAlertingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of fraud monitoring and alerting service
 */
@Service
public class FraudMonitoringAlertingServiceImpl implements FraudMonitoringAlertingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudMonitoringAlertingServiceImpl.class);
    
    @Autowired
    private FraudRiskAssessmentRepository fraudRiskAssessmentRepository;
    
    @Autowired
    private FraudRiskConfigurationRepository fraudRiskConfigurationRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final DateTimeFormatter ALERT_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void processAssessmentForMonitoring(FraudRiskAssessment assessment) {
        logger.debug("Processing assessment for monitoring: {}", assessment.getAssessmentId());
        
        try {
            // Check if assessment requires immediate alerting
            if (assessment.getRiskLevel() == FraudRiskAssessment.RiskLevel.CRITICAL) {
                sendCriticalRiskAlert(assessment);
            } else if (assessment.getRiskLevel() == FraudRiskAssessment.RiskLevel.HIGH) {
                sendHighRiskAlert(assessment);
            }
            
            // Check for patterns that might require alerting
            checkAssessmentPatterns(assessment);
            
        } catch (Exception e) {
            logger.error("Error processing assessment for monitoring: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void checkHighRiskPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Checking high-risk patterns for tenant: {} between {} and {}", tenantId, startTime, endTime);
        
        try {
            List<FraudRiskAssessment> highRiskAssessments = fraudRiskAssessmentRepository
                    .findByTenantIdAndRiskLevelAndAssessedAtBetween(tenantId, FraudRiskAssessment.RiskLevel.HIGH, startTime, endTime);
            
            if (highRiskAssessments.size() >= 5) { // Threshold for high-risk pattern
                Map<String, Object> alertData = Map.of(
                        "tenantId", tenantId,
                        "patternType", "HIGH_RISK_PATTERN",
                        "assessmentCount", highRiskAssessments.size(),
                        "timeRange", Map.of("start", startTime, "end", endTime),
                        "assessments", highRiskAssessments.stream()
                                .map(a -> Map.of(
                                        "assessmentId", a.getAssessmentId(),
                                        "transactionReference", a.getTransactionReference(),
                                        "riskScore", a.getRiskScore(),
                                        "decision", a.getDecision(),
                                        "assessedAt", a.getAssessedAt()
                                ))
                                .collect(Collectors.toList())
                );
                
                sendAlert("HIGH_RISK_PATTERN", "HIGH", 
                         String.format("High-risk pattern detected: %d high-risk assessments in the last hour", highRiskAssessments.size()),
                         alertData);
            }
            
        } catch (Exception e) {
            logger.error("Error checking high-risk patterns: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void checkCriticalRiskPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Checking critical-risk patterns for tenant: {} between {} and {}", tenantId, startTime, endTime);
        
        try {
            List<FraudRiskAssessment> criticalRiskAssessments = fraudRiskAssessmentRepository
                    .findByTenantIdAndRiskLevelAndAssessedAtBetween(tenantId, FraudRiskAssessment.RiskLevel.CRITICAL, startTime, endTime);
            
            if (criticalRiskAssessments.size() >= 2) { // Lower threshold for critical risk
                Map<String, Object> alertData = Map.of(
                        "tenantId", tenantId,
                        "patternType", "CRITICAL_RISK_PATTERN",
                        "assessmentCount", criticalRiskAssessments.size(),
                        "timeRange", Map.of("start", startTime, "end", endTime),
                        "assessments", criticalRiskAssessments.stream()
                                .map(a -> Map.of(
                                        "assessmentId", a.getAssessmentId(),
                                        "transactionReference", a.getTransactionReference(),
                                        "riskScore", a.getRiskScore(),
                                        "decision", a.getDecision(),
                                        "assessedAt", a.getAssessedAt()
                                ))
                                .collect(Collectors.toList())
                );
                
                sendAlert("CRITICAL_RISK_PATTERN", "CRITICAL", 
                         String.format("Critical-risk pattern detected: %d critical-risk assessments in the last hour", criticalRiskAssessments.size()),
                         alertData);
            }
            
        } catch (Exception e) {
            logger.error("Error checking critical-risk patterns: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void checkApiFailurePatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Checking API failure patterns for tenant: {} between {} and {}", tenantId, startTime, endTime);
        
        try {
            List<FraudRiskAssessment> failedAssessments = fraudRiskAssessmentRepository
                    .findByTenantIdAndAssessedAtBetween(tenantId, startTime, endTime)
                    .stream()
                    .filter(a -> a.getStatus() == FraudRiskAssessment.AssessmentStatus.FAILED || 
                               a.getStatus() == FraudRiskAssessment.AssessmentStatus.ERROR)
                    .collect(Collectors.toList());
            
            if (failedAssessments.size() >= 3) { // Threshold for API failure pattern
                Map<String, Object> alertData = Map.of(
                        "tenantId", tenantId,
                        "patternType", "API_FAILURE_PATTERN",
                        "failureCount", failedAssessments.size(),
                        "timeRange", Map.of("start", startTime, "end", endTime),
                        "failures", failedAssessments.stream()
                                .map(a -> Map.of(
                                        "assessmentId", a.getAssessmentId(),
                                        "status", a.getStatus(),
                                        "errorMessage", a.getErrorMessage(),
                                        "assessedAt", a.getAssessedAt()
                                ))
                                .collect(Collectors.toList())
                );
                
                sendAlert("API_FAILURE_PATTERN", "HIGH", 
                         String.format("API failure pattern detected: %d failed assessments in the last hour", failedAssessments.size()),
                         alertData);
            }
            
        } catch (Exception e) {
            logger.error("Error checking API failure patterns: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void checkUnusualTransactionPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Checking unusual transaction patterns for tenant: {} between {} and {}", tenantId, startTime, endTime);
        
        try {
            List<FraudRiskAssessment> assessments = fraudRiskAssessmentRepository
                    .findByTenantIdAndAssessedAtBetween(tenantId, startTime, endTime);
            
            // Check for unusual amounts
            Map<String, Long> amountPatterns = assessments.stream()
                    .filter(a -> a.getRiskFactors() != null && a.getRiskFactors().containsKey("highAmount"))
                    .collect(Collectors.groupingBy(
                            a -> a.getRiskFactors().get("highAmount").toString(),
                            Collectors.counting()
                    ));
            
            if (amountPatterns.values().stream().anyMatch(count -> count >= 3)) {
                Map<String, Object> alertData = Map.of(
                        "tenantId", tenantId,
                        "patternType", "UNUSUAL_AMOUNT_PATTERN",
                        "amountPatterns", amountPatterns,
                        "timeRange", Map.of("start", startTime, "end", endTime)
                );
                
                sendAlert("UNUSUAL_AMOUNT_PATTERN", "MEDIUM", 
                         "Unusual transaction amount pattern detected",
                         alertData);
            }
            
            // Check for unusual times
            long offHoursCount = assessments.stream()
                    .filter(a -> a.getRiskFactors() != null && a.getRiskFactors().containsKey("offHours"))
                    .count();
            
            if (offHoursCount >= 5) {
                Map<String, Object> alertData = Map.of(
                        "tenantId", tenantId,
                        "patternType", "UNUSUAL_TIME_PATTERN",
                        "offHoursCount", offHoursCount,
                        "timeRange", Map.of("start", startTime, "end", endTime)
                );
                
                sendAlert("UNUSUAL_TIME_PATTERN", "MEDIUM", 
                         String.format("Unusual transaction time pattern detected: %d off-hours transactions", offHoursCount),
                         alertData);
            }
            
        } catch (Exception e) {
            logger.error("Error checking unusual transaction patterns: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void checkVelocityBasedPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Checking velocity-based patterns for tenant: {} between {} and {}", tenantId, startTime, endTime);
        
        try {
            List<FraudRiskAssessment> assessments = fraudRiskAssessmentRepository
                    .findByTenantIdAndAssessedAtBetween(tenantId, startTime, endTime);
            
            // Group by account number and check velocity
            Map<String, List<FraudRiskAssessment>> accountGroups = assessments.stream()
                    .filter(a -> a.getRiskFactors() != null && a.getRiskFactors().containsKey("highDailyFrequency"))
                    .collect(Collectors.groupingBy(a -> a.getRiskFactors().get("fromAccountNumber").toString()));
            
            for (Map.Entry<String, List<FraudRiskAssessment>> entry : accountGroups.entrySet()) {
                String accountNumber = entry.getKey();
                List<FraudRiskAssessment> accountAssessments = entry.getValue();
                
                if (accountAssessments.size() >= 10) { // High velocity threshold
                    Map<String, Object> alertData = Map.of(
                            "tenantId", tenantId,
                            "patternType", "VELOCITY_BASED_PATTERN",
                            "accountNumber", accountNumber,
                            "transactionCount", accountAssessments.size(),
                            "timeRange", Map.of("start", startTime, "end", endTime)
                    );
                    
                    sendAlert("VELOCITY_BASED_PATTERN", "HIGH", 
                             String.format("High velocity pattern detected for account %s: %d transactions", accountNumber, accountAssessments.size()),
                             alertData);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error checking velocity-based patterns: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void checkGeographicAnomalyPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Checking geographic anomaly patterns for tenant: {} between {} and {}", tenantId, startTime, endTime);
        
        try {
            List<FraudRiskAssessment> assessments = fraudRiskAssessmentRepository
                    .findByTenantIdAndAssessedAtBetween(tenantId, startTime, endTime);
            
            // Check for high-risk country patterns
            Map<String, Long> countryPatterns = assessments.stream()
                    .filter(a -> a.getRiskFactors() != null && a.getRiskFactors().containsKey("highRiskCountry"))
                    .collect(Collectors.groupingBy(
                            a -> a.getRiskFactors().get("country").toString(),
                            Collectors.counting()
                    ));
            
            if (countryPatterns.values().stream().anyMatch(count -> count >= 3)) {
                Map<String, Object> alertData = Map.of(
                        "tenantId", tenantId,
                        "patternType", "GEOGRAPHIC_ANOMALY_PATTERN",
                        "countryPatterns", countryPatterns,
                        "timeRange", Map.of("start", startTime, "end", endTime)
                );
                
                sendAlert("GEOGRAPHIC_ANOMALY_PATTERN", "MEDIUM", 
                         "Geographic anomaly pattern detected",
                         alertData);
            }
            
        } catch (Exception e) {
            logger.error("Error checking geographic anomaly patterns: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void checkDeviceFingerprintingAnomalies(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Checking device fingerprinting anomalies for tenant: {} between {} and {}", tenantId, startTime, endTime);
        
        try {
            List<FraudRiskAssessment> assessments = fraudRiskAssessmentRepository
                    .findByTenantIdAndAssessedAtBetween(tenantId, startTime, endTime);
            
            // Check for new device patterns
            Map<String, Long> devicePatterns = assessments.stream()
                    .filter(a -> a.getRiskFactors() != null && a.getRiskFactors().containsKey("newDevice"))
                    .collect(Collectors.groupingBy(
                            a -> a.getRiskFactors().get("deviceId").toString(),
                            Collectors.counting()
                    ));
            
            if (devicePatterns.values().stream().anyMatch(count -> count >= 5)) {
                Map<String, Object> alertData = Map.of(
                        "tenantId", tenantId,
                        "patternType", "DEVICE_FINGERPRINTING_ANOMALY",
                        "devicePatterns", devicePatterns,
                        "timeRange", Map.of("start", startTime, "end", endTime)
                );
                
                sendAlert("DEVICE_FINGERPRINTING_ANOMALY", "MEDIUM", 
                         "Device fingerprinting anomaly detected",
                         alertData);
            }
            
        } catch (Exception e) {
            logger.error("Error checking device fingerprinting anomalies: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void checkAccountTakeoverPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Checking account takeover patterns for tenant: {} between {} and {}", tenantId, startTime, endTime);
        
        try {
            List<FraudRiskAssessment> assessments = fraudRiskAssessmentRepository
                    .findByTenantIdAndAssessedAtBetween(tenantId, startTime, endTime);
            
            // Check for multiple new devices for same account
            Map<String, Set<String>> accountDeviceMap = new HashMap<>();
            
            for (FraudRiskAssessment assessment : assessments) {
                if (assessment.getRiskFactors() != null && 
                    assessment.getRiskFactors().containsKey("newDevice") &&
                    assessment.getRiskFactors().containsKey("fromAccountNumber")) {
                    
                    String accountNumber = assessment.getRiskFactors().get("fromAccountNumber").toString();
                    String deviceId = assessment.getRiskFactors().get("deviceId").toString();
                    
                    accountDeviceMap.computeIfAbsent(accountNumber, k -> new HashSet<>()).add(deviceId);
                }
            }
            
            for (Map.Entry<String, Set<String>> entry : accountDeviceMap.entrySet()) {
                String accountNumber = entry.getKey();
                Set<String> devices = entry.getValue();
                
                if (devices.size() >= 3) { // Multiple new devices for same account
                    Map<String, Object> alertData = Map.of(
                            "tenantId", tenantId,
                            "patternType", "ACCOUNT_TAKEOVER_PATTERN",
                            "accountNumber", accountNumber,
                            "deviceCount", devices.size(),
                            "devices", devices,
                            "timeRange", Map.of("start", startTime, "end", endTime)
                    );
                    
                    sendAlert("ACCOUNT_TAKEOVER_PATTERN", "HIGH", 
                             String.format("Potential account takeover detected for account %s: %d new devices", accountNumber, devices.size()),
                             alertData);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error checking account takeover patterns: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void checkMoneyLaunderingPatterns(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Checking money laundering patterns for tenant: {} between {} and {}", tenantId, startTime, endTime);
        
        try {
            List<FraudRiskAssessment> assessments = fraudRiskAssessmentRepository
                    .findByTenantIdAndAssessedAtBetween(tenantId, startTime, endTime);
            
            // Check for structuring patterns (amounts just under reporting thresholds)
            long structuringCount = assessments.stream()
                    .filter(a -> a.getRiskFactors() != null && a.getRiskFactors().containsKey("structuring"))
                    .count();
            
            if (structuringCount >= 3) {
                Map<String, Object> alertData = Map.of(
                        "tenantId", tenantId,
                        "patternType", "MONEY_LAUNDERING_PATTERN",
                        "structuringCount", structuringCount,
                        "timeRange", Map.of("start", startTime, "end", endTime)
                );
                
                sendAlert("MONEY_LAUNDERING_PATTERN", "HIGH", 
                         String.format("Potential money laundering pattern detected: %d structuring transactions", structuringCount),
                         alertData);
            }
            
        } catch (Exception e) {
            logger.error("Error checking money laundering patterns: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendAlert(String alertType, String severity, String message, Map<String, Object> alertData) {
        logger.info("Sending alert: {} - {} - {}", alertType, severity, message);
        
        try {
            Map<String, Object> alert = Map.of(
                    "alertId", UUID.randomUUID().toString(),
                    "alertType", alertType,
                    "severity", severity,
                    "message", message,
                    "alertData", alertData,
                    "timestamp", LocalDateTime.now().format(ALERT_TIMESTAMP_FORMATTER),
                    "status", "ACTIVE"
            );
            
            // Send to multiple channels based on severity
            if ("CRITICAL".equals(severity)) {
                sendCriticalAlert(alert);
            } else if ("HIGH".equals(severity)) {
                sendHighSeverityAlert(alert);
            } else {
                sendStandardAlert(alert);
            }
            
            // Log the alert
            logger.warn("ALERT SENT - Type: {}, Severity: {}, Message: {}", alertType, severity, message);
            
        } catch (Exception e) {
            logger.error("Error sending alert: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendEmailAlert(String recipient, String subject, String message, Map<String, Object> alertData) {
        logger.info("Sending email alert to: {} - {}", recipient, subject);
        
        try {
            // This would integrate with an email service (e.g., SendGrid, AWS SES)
            Map<String, Object> emailData = Map.of(
                    "to", recipient,
                    "subject", subject,
                    "message", message,
                    "alertData", alertData,
                    "timestamp", LocalDateTime.now().toString()
            );
            
            // Placeholder for email service integration
            logger.info("Email alert sent: {}", emailData);
            
        } catch (Exception e) {
            logger.error("Error sending email alert: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendSmsAlert(String phoneNumber, String message, Map<String, Object> alertData) {
        logger.info("Sending SMS alert to: {} - {}", phoneNumber, message);
        
        try {
            // This would integrate with an SMS service (e.g., Twilio, AWS SNS)
            Map<String, Object> smsData = Map.of(
                    "to", phoneNumber,
                    "message", message,
                    "alertData", alertData,
                    "timestamp", LocalDateTime.now().toString()
            );
            
            // Placeholder for SMS service integration
            logger.info("SMS alert sent: {}", smsData);
            
        } catch (Exception e) {
            logger.error("Error sending SMS alert: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendWebhookAlert(String webhookUrl, String message, Map<String, Object> alertData) {
        logger.info("Sending webhook alert to: {} - {}", webhookUrl, message);
        
        try {
            Map<String, Object> webhookData = Map.of(
                    "message", message,
                    "alertData", alertData,
                    "timestamp", LocalDateTime.now().toString()
            );
            
            // Send webhook
            restTemplate.postForObject(webhookUrl, webhookData, String.class);
            
        } catch (Exception e) {
            logger.error("Error sending webhook alert: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendSlackAlert(String slackChannel, String message, Map<String, Object> alertData) {
        logger.info("Sending Slack alert to channel: {} - {}", slackChannel, message);
        
        try {
            // This would integrate with Slack API
            Map<String, Object> slackData = Map.of(
                    "channel", slackChannel,
                    "text", message,
                    "attachments", List.of(Map.of(
                            "color", getSlackColor(alertData.get("severity").toString()),
                            "fields", List.of(
                                    Map.of("title", "Alert Type", "value", alertData.get("alertType"), "short", true),
                                    Map.of("title", "Severity", "value", alertData.get("severity"), "short", true),
                                    Map.of("title", "Timestamp", "value", alertData.get("timestamp"), "short", true)
                            )
                    ))
            );
            
            // Placeholder for Slack integration
            logger.info("Slack alert sent: {}", slackData);
            
        } catch (Exception e) {
            logger.error("Error sending Slack alert: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendTeamsAlert(String teamsWebhook, String message, Map<String, Object> alertData) {
        logger.info("Sending Teams alert to webhook: {} - {}", teamsWebhook, message);
        
        try {
            // This would integrate with Microsoft Teams webhook
            Map<String, Object> teamsData = Map.of(
                    "@type", "MessageCard",
                    "@context", "http://schema.org/extensions",
                    "themeColor", getTeamsColor(alertData.get("severity").toString()),
                    "summary", message,
                    "sections", List.of(Map.of(
                            "activityTitle", message,
                            "activitySubtitle", "Fraud/Risk Monitoring Alert",
                            "facts", List.of(
                                    Map.of("name", "Alert Type", "value", alertData.get("alertType")),
                                    Map.of("name", "Severity", "value", alertData.get("severity")),
                                    Map.of("name", "Timestamp", "value", alertData.get("timestamp"))
                            )
                    ))
            );
            
            // Placeholder for Teams integration
            logger.info("Teams alert sent: {}", teamsData);
            
        } catch (Exception e) {
            logger.error("Error sending Teams alert: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> getAlertConfiguration(String tenantId) {
        // This would typically retrieve from database
        return Map.of(
                "tenantId", tenantId,
                "emailAlerts", Map.of("enabled", true, "recipients", List.of("admin@tenant.com")),
                "smsAlerts", Map.of("enabled", false, "recipients", List.of()),
                "webhookAlerts", Map.of("enabled", true, "urls", List.of("https://tenant.com/webhook")),
                "slackAlerts", Map.of("enabled", true, "channels", List.of("#fraud-alerts")),
                "teamsAlerts", Map.of("enabled", false, "webhooks", List.of()),
                "severityThresholds", Map.of(
                        "CRITICAL", Map.of("email", true, "sms", true, "webhook", true, "slack", true),
                        "HIGH", Map.of("email", true, "sms", false, "webhook", true, "slack", true),
                        "MEDIUM", Map.of("email", true, "sms", false, "webhook", false, "slack", false),
                        "LOW", Map.of("email", false, "sms", false, "webhook", false, "slack", false)
                )
        );
    }
    
    @Override
    public void updateAlertConfiguration(String tenantId, Map<String, Object> alertConfig) {
        logger.info("Updating alert configuration for tenant: {}", tenantId);
        // This would typically save to database
    }
    
    @Override
    public List<Map<String, Object>> getAlertHistory(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        // This would typically retrieve from database
        return List.of();
    }
    
    @Override
    public Map<String, Object> getAlertStatistics(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        // This would typically calculate from database
        return Map.of(
                "totalAlerts", 0,
                "criticalAlerts", 0,
                "highAlerts", 0,
                "mediumAlerts", 0,
                "lowAlerts", 0,
                "acknowledgedAlerts", 0,
                "resolvedAlerts", 0,
                "activeAlerts", 0
        );
    }
    
    @Override
    public void acknowledgeAlert(String alertId, String acknowledgedBy, String acknowledgmentNote) {
        logger.info("Acknowledging alert: {} by {}", alertId, acknowledgedBy);
        // This would typically update database
    }
    
    @Override
    public void resolveAlert(String alertId, String resolvedBy, String resolutionNote) {
        logger.info("Resolving alert: {} by {}", alertId, resolvedBy);
        // This would typically update database
    }
    
    @Override
    public void escalateAlert(String alertId, String escalatedBy, String escalationReason) {
        logger.info("Escalating alert: {} by {}", alertId, escalatedBy);
        // This would typically update database
    }
    
    @Override
    public List<Map<String, Object>> getActiveAlerts(String tenantId) {
        // This would typically retrieve from database
        return List.of();
    }
    
    @Override
    public Map<String, Object> getAlertById(String alertId) {
        // This would typically retrieve from database
        return Map.of();
    }
    
    @Override
    public void checkAlertThresholds(String tenantId) {
        logger.info("Checking alert thresholds for tenant: {}", tenantId);
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);
            
            // Check various patterns
            checkHighRiskPatterns(tenantId, oneHourAgo, now);
            checkCriticalRiskPatterns(tenantId, oneHourAgo, now);
            checkApiFailurePatterns(tenantId, oneHourAgo, now);
            checkUnusualTransactionPatterns(tenantId, oneHourAgo, now);
            checkVelocityBasedPatterns(tenantId, oneHourAgo, now);
            checkGeographicAnomalyPatterns(tenantId, oneHourAgo, now);
            checkDeviceFingerprintingAnomalies(tenantId, oneHourAgo, now);
            checkAccountTakeoverPatterns(tenantId, oneHourAgo, now);
            checkMoneyLaunderingPatterns(tenantId, oneHourAgo, now);
            
        } catch (Exception e) {
            logger.error("Error checking alert thresholds: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void runScheduledMonitoringTasks() {
        logger.debug("Running scheduled monitoring tasks");
        
        try {
            // Get all active tenants
            List<String> tenantIds = fraudRiskConfigurationRepository.findAll()
                    .stream()
                    .map(FraudRiskConfiguration::getTenantId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // Check alert thresholds for each tenant
            for (String tenantId : tenantIds) {
                CompletableFuture.runAsync(() -> checkAlertThresholds(tenantId));
            }
            
        } catch (Exception e) {
            logger.error("Error running scheduled monitoring tasks: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> getMonitoringHealthStatus() {
        return Map.of(
                "status", "UP",
                "lastCheck", LocalDateTime.now().toString(),
                "scheduledTasksRunning", true,
                "alertChannels", Map.of(
                        "email", "UP",
                        "sms", "UP",
                        "webhook", "UP",
                        "slack", "UP",
                        "teams", "UP"
                )
        );
    }
    
    @Override
    public Map<String, Object> getMonitoringMetrics(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        return Map.of(
                "alertsSent", 0,
                "patternsDetected", 0,
                "averageResponseTime", 0,
                "successRate", 100.0,
                "errorRate", 0.0
        );
    }
    
    // Private helper methods
    
    private void checkAssessmentPatterns(FraudRiskAssessment assessment) {
        // Check for immediate patterns that require alerting
        if (assessment.getRiskFactors() != null) {
            if (assessment.getRiskFactors().containsKey("accountTakeover")) {
                sendAccountTakeoverAlert(assessment);
            }
            if (assessment.getRiskFactors().containsKey("moneyLaundering")) {
                sendMoneyLaunderingAlert(assessment);
            }
        }
    }
    
    private void sendCriticalRiskAlert(FraudRiskAssessment assessment) {
        Map<String, Object> alertData = Map.of(
                "assessmentId", assessment.getAssessmentId(),
                "transactionReference", assessment.getTransactionReference(),
                "riskScore", assessment.getRiskScore(),
                "riskLevel", assessment.getRiskLevel(),
                "decision", assessment.getDecision(),
                "tenantId", assessment.getTenantId()
        );
        
        sendAlert("CRITICAL_RISK_ASSESSMENT", "CRITICAL", 
                 String.format("Critical risk assessment: %s (Score: %s)", 
                              assessment.getTransactionReference(), assessment.getRiskScore()),
                 alertData);
    }
    
    private void sendHighRiskAlert(FraudRiskAssessment assessment) {
        Map<String, Object> alertData = Map.of(
                "assessmentId", assessment.getAssessmentId(),
                "transactionReference", assessment.getTransactionReference(),
                "riskScore", assessment.getRiskScore(),
                "riskLevel", assessment.getRiskLevel(),
                "decision", assessment.getDecision(),
                "tenantId", assessment.getTenantId()
        );
        
        sendAlert("HIGH_RISK_ASSESSMENT", "HIGH", 
                 String.format("High risk assessment: %s (Score: %s)", 
                              assessment.getTransactionReference(), assessment.getRiskScore()),
                 alertData);
    }
    
    private void sendAccountTakeoverAlert(FraudRiskAssessment assessment) {
        Map<String, Object> alertData = Map.of(
                "assessmentId", assessment.getAssessmentId(),
                "transactionReference", assessment.getTransactionReference(),
                "riskFactors", assessment.getRiskFactors(),
                "tenantId", assessment.getTenantId()
        );
        
        sendAlert("ACCOUNT_TAKEOVER_DETECTED", "CRITICAL", 
                 String.format("Potential account takeover detected: %s", assessment.getTransactionReference()),
                 alertData);
    }
    
    private void sendMoneyLaunderingAlert(FraudRiskAssessment assessment) {
        Map<String, Object> alertData = Map.of(
                "assessmentId", assessment.getAssessmentId(),
                "transactionReference", assessment.getTransactionReference(),
                "riskFactors", assessment.getRiskFactors(),
                "tenantId", assessment.getTenantId()
        );
        
        sendAlert("MONEY_LAUNDERING_DETECTED", "HIGH", 
                 String.format("Potential money laundering detected: %s", assessment.getTransactionReference()),
                 alertData);
    }
    
    private void sendCriticalAlert(Map<String, Object> alert) {
        // Send to all channels for critical alerts
        sendEmailAlert("admin@tenant.com", "CRITICAL FRAUD ALERT", alert.get("message").toString(), alert);
        sendSmsAlert("+1234567890", alert.get("message").toString(), alert);
        sendSlackAlert("#fraud-alerts", alert.get("message").toString(), alert);
        sendWebhookAlert("https://tenant.com/webhook", alert.get("message").toString(), alert);
    }
    
    private void sendHighSeverityAlert(Map<String, Object> alert) {
        // Send to email, Slack, and webhook for high severity alerts
        sendEmailAlert("admin@tenant.com", "HIGH PRIORITY FRAUD ALERT", alert.get("message").toString(), alert);
        sendSlackAlert("#fraud-alerts", alert.get("message").toString(), alert);
        sendWebhookAlert("https://tenant.com/webhook", alert.get("message").toString(), alert);
    }
    
    private void sendStandardAlert(Map<String, Object> alert) {
        // Send to email only for standard alerts
        sendEmailAlert("admin@tenant.com", "FRAUD ALERT", alert.get("message").toString(), alert);
    }
    
    private String getSlackColor(String severity) {
        switch (severity) {
            case "CRITICAL": return "danger";
            case "HIGH": return "warning";
            case "MEDIUM": return "good";
            case "LOW": return "#36a64f";
            default: return "#36a64f";
        }
    }
    
    private String getTeamsColor(String severity) {
        switch (severity) {
            case "CRITICAL": return "FF0000";
            case "HIGH": return "FFA500";
            case "MEDIUM": return "FFFF00";
            case "LOW": return "00FF00";
            default: return "00FF00";
        }
    }
}