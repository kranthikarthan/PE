package com.payments.notification.domain.model;

/**
 * Types of notifications that can be sent by the payment engine.
 * 
 * @author Payment Engine
 */
public enum NotificationType {
    /**
     * Notification sent when a payment is initiated.
     */
    PAYMENT_INITIATED,
    
    /**
     * Notification sent when payment validation is complete.
     */
    PAYMENT_VALIDATED,
    
    /**
     * Notification sent when payment clears successfully.
     */
    PAYMENT_CLEARED,
    
    /**
     * Notification sent when payment fails.
     */
    PAYMENT_FAILED,
    
    /**
     * Notification sent when payment is reversed.
     */
    PAYMENT_REVERSED,
    
    /**
     * Notification for tenant alerts.
     */
    TENANT_ALERT,
    
    /**
     * Notification for system maintenance.
     */
    SYSTEM_NOTIFICATION,
    
    /**
     * Marketing/promotional notification.
     */
    MARKETING
}
