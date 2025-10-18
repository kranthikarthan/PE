package com.payments.notification.domain.model;

/**
 * Lifecycle status of a notification.
 * 
 * PENDING → SENT → (success)
 * PENDING → RETRY → SENT → (success)
 * PENDING → FAILED → DLQ (after max retries)
 * 
 * @author Payment Engine
 */
public enum NotificationStatus {
    /**
     * Notification queued and waiting to be sent.
     */
    PENDING,
    
    /**
     * Notification currently being retried.
     */
    RETRY,
    
    /**
     * Notification successfully sent to channel provider.
     */
    SENT,
    
    /**
     * Notification failed and exhausted retries.
     */
    FAILED
}
