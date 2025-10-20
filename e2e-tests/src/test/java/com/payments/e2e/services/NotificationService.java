package com.payments.e2e.services;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Notification Service for E2E Tests
 * 
 * Simulates notification system interactions for comprehensive E2E testing
 * including payment notifications, clearing events, and system alerts.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private Map<String, List<NotificationEvent>> notifications = new HashMap<>();
    private Map<String, Boolean> notificationChannels = new HashMap<>();

    public NotificationService() {
        initializeNotificationChannels();
    }

    private void initializeNotificationChannels() {
        logger.info("Initializing notification channels for E2E tests");
        
        // Initialize notification channels
        notificationChannels.put("EMAIL", true);
        notificationChannels.put("SMS", true);
        notificationChannels.put("PUSH", true);
        notificationChannels.put("WEBHOOK", true);
    }

    public void sendPaymentNotification(String paymentId, String eventType, String message) {
        logger.info("Sending payment notification: {} - {} - {}", paymentId, eventType, message);
        
        NotificationEvent event = new NotificationEvent();
        event.setPaymentId(paymentId);
        event.setEventType(eventType);
        event.setMessage(message);
        event.setTimestamp(System.currentTimeMillis());
        event.setStatus("SENT");
        
        // Store notification for verification
        notifications.computeIfAbsent(paymentId, k -> new ArrayList<>()).add(event);
        
        logger.info("Payment notification sent: {}", event);
    }

    public void sendClearingEvent(String paymentId, String clearingSystem, String eventType) {
        logger.info("Sending clearing event: {} - {} - {}", paymentId, clearingSystem, eventType);
        
        NotificationEvent event = new NotificationEvent();
        event.setPaymentId(paymentId);
        event.setEventType("CLEARING_" + eventType);
        event.setMessage("Clearing event: " + clearingSystem + " - " + eventType);
        event.setTimestamp(System.currentTimeMillis());
        event.setStatus("SENT");
        event.setClearingSystem(clearingSystem);
        
        // Store notification for verification
        notifications.computeIfAbsent(paymentId, k -> new ArrayList<>()).add(event);
        
        logger.info("Clearing event sent: {}", event);
    }

    public void sendSystemAlert(String alertType, String message) {
        logger.info("Sending system alert: {} - {}", alertType, message);
        
        NotificationEvent event = new NotificationEvent();
        event.setEventType("SYSTEM_ALERT_" + alertType);
        event.setMessage(message);
        event.setTimestamp(System.currentTimeMillis());
        event.setStatus("SENT");
        
        // Store system alert
        notifications.computeIfAbsent("SYSTEM", k -> new ArrayList<>()).add(event);
        
        logger.info("System alert sent: {}", event);
    }

    public List<NotificationEvent> getNotificationsForPayment(String paymentId) {
        return notifications.getOrDefault(paymentId, new ArrayList<>());
    }

    public List<NotificationEvent> getAllNotifications() {
        List<NotificationEvent> allNotifications = new ArrayList<>();
        notifications.values().forEach(allNotifications::addAll);
        return allNotifications;
    }

    public void clearNotifications() {
        notifications.clear();
        logger.info("All notifications cleared");
    }

    public boolean isNotificationChannelEnabled(String channel) {
        return notificationChannels.getOrDefault(channel, false);
    }

    public void setNotificationChannel(String channel, boolean enabled) {
        notificationChannels.put(channel, enabled);
        logger.info("Notification channel {} set to: {}", channel, enabled);
    }

    // Inner class for notification events
    public static class NotificationEvent {
        private String paymentId;
        private String eventType;
        private String message;
        private long timestamp;
        private String status;
        private String clearingSystem;
        private String channel;

        // Getters and setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getClearingSystem() { return clearingSystem; }
        public void setClearingSystem(String clearingSystem) { this.clearingSystem = clearingSystem; }
        
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }

        @Override
        public String toString() {
            return "NotificationEvent{" +
                "paymentId='" + paymentId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", clearingSystem='" + clearingSystem + '\'' +
                ", channel='" + channel + '\'' +
                '}';
        }
    }
}
