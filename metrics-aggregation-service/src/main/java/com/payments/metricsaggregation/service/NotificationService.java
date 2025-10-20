package com.payments.metricsaggregation.service;

import com.payments.metricsaggregation.entity.AlertEventEntity;
import com.payments.metricsaggregation.entity.AlertRuleEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Notification Service
 * 
 * Handles alert notifications through multiple channels.
 * Integrates with email, Slack, and other notification systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    /**
     * Send alert notification
     */
    public void sendAlertNotification(AlertEventEntity alertEvent, AlertRuleEntity alertRule) {
        log.info("Sending alert notification for alert: {} with severity: {}", 
            alertEvent.getAlertId(), alertEvent.getSeverity());
        
        // Parse notification channels
        List<String> channels = parseNotificationChannels(alertRule.getNotificationChannels());
        
        // Send notifications through each channel
        for (String channel : channels) {
            try {
                sendNotificationToChannel(channel, alertEvent, alertRule);
            } catch (Exception e) {
                log.error("Failed to send notification to channel: {}", channel, e);
            }
        }
    }

    /**
     * Send notification to specific channel
     */
    private void sendNotificationToChannel(String channel, AlertEventEntity alertEvent, AlertRuleEntity alertRule) {
        switch (channel.toLowerCase()) {
            case "email" -> sendEmailNotification(alertEvent, alertRule);
            case "slack" -> sendSlackNotification(alertEvent, alertRule);
            case "webhook" -> sendWebhookNotification(alertEvent, alertRule);
            default -> log.warn("Unknown notification channel: {}", channel);
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(AlertEventEntity alertEvent, AlertRuleEntity alertRule) {
        log.info("Sending email notification for alert: {}", alertEvent.getAlertId());
        
        // This would integrate with email service
        // For now, just log the notification
        log.info("EMAIL ALERT: {} - {} {} {} (threshold: {})", 
            alertEvent.getSeverity(),
            alertRule.getServiceName(),
            alertRule.getMetricName(),
            alertEvent.getMetricValue(),
            alertEvent.getThresholdValue());
    }

    /**
     * Send Slack notification
     */
    private void sendSlackNotification(AlertEventEntity alertEvent, AlertRuleEntity alertRule) {
        log.info("Sending Slack notification for alert: {}", alertEvent.getAlertId());
        
        // This would integrate with Slack API
        // For now, just log the notification
        log.info("SLACK ALERT: {} - {} {} {} (threshold: {})", 
            alertEvent.getSeverity(),
            alertRule.getServiceName(),
            alertRule.getMetricName(),
            alertEvent.getMetricValue(),
            alertEvent.getThresholdValue());
    }

    /**
     * Send webhook notification
     */
    private void sendWebhookNotification(AlertEventEntity alertEvent, AlertRuleEntity alertRule) {
        log.info("Sending webhook notification for alert: {}", alertEvent.getAlertId());
        
        // This would send HTTP POST to webhook URL
        // For now, just log the notification
        log.info("WEBHOOK ALERT: {} - {} {} {} (threshold: {})", 
            alertEvent.getSeverity(),
            alertRule.getServiceName(),
            alertRule.getMetricName(),
            alertEvent.getMetricValue(),
            alertEvent.getThresholdValue());
    }

    /**
     * Parse notification channels from JSON
     */
    private List<String> parseNotificationChannels(String channelsJson) {
        // Simple JSON parsing - in production, use Jackson
        return List.of("email", "slack"); // Mock implementation
    }
}
