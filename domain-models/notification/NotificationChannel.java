package com.payments.notification.domain.model;

/**
 * Supported notification channels for multi-channel delivery.
 * 
 * @author Payment Engine
 */
public enum NotificationChannel {
    /**
     * Email delivery via AWS SES.
     */
    EMAIL,
    
    /**
     * SMS delivery via Twilio.
     */
    SMS,
    
    /**
     * Push notifications via Firebase Cloud Messaging.
     */
    PUSH
}
