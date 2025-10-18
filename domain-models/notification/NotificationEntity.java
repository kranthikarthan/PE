package com.payments.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a notification queued for delivery.
 * 
 * Supports:
 * - Multi-tenancy via tenantId (with RLS)
 * - JSON template data storage
 * - Retry tracking with attempt counters
 * - Status lifecycle tracking
 * - Full audit trail (createdAt, updatedAt)
 * 
 * @author Payment Engine
 */
@Entity
@Table(
    name = "notification_queue",
    indexes = {
        @Index(name = "idx_notification_tenant_user_created", 
            columnList = "tenant_id,user_id,created_at DESC"),
        @Index(name = "idx_notification_tenant_status_updated", 
            columnList = "tenant_id,status,updated_at"),
        @Index(name = "idx_notification_retry_candidates", 
            columnList = "status,last_attempt_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique identifier for this notification.
     */
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    /**
     * Tenant identifier (multi-tenancy enforcement via RLS).
     */
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;
    
    /**
     * User identifier (recipient).
     */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;
    
    /**
     * Reference to the notification template used.
     */
    @Column(name = "template_id", nullable = false, columnDefinition = "UUID")
    private UUID templateId;
    
    /**
     * Type of notification (PAYMENT_INITIATED, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;
    
    /**
     * Channel for delivery (EMAIL, SMS, PUSH).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 20)
    private NotificationChannel channelType;
    
    /**
     * Recipient address (email, phone, or push token).
     */
    @Column(name = "recipient_address", nullable = false)
    private String recipientAddress;
    
    /**
     * Template variables in JSON format for rendering.
     */
    @Column(name = "template_data", nullable = false, columnDefinition = "TEXT")
    private String templateData;
    
    /**
     * Current status of the notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;
    
    /**
     * Number of delivery attempts (0-3).
     */
    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;
    
    /**
     * Timestamp of the last delivery attempt.
     */
    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;
    
    /**
     * Timestamp when notification was successfully sent.
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    /**
     * Reason for failure (if applicable).
     */
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    /**
     * External message ID from provider (e.g., SES messageId).
     */
    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;
    
    /**
     * Creation timestamp (auto-populated by Hibernate).
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Update timestamp (auto-populated by Hibernate).
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Check if this notification can be retried.
     * @return true if status is RETRY or FAILED with attempts < 3
     */
    public boolean canRetry() {
        return (status == NotificationStatus.RETRY || 
                status == NotificationStatus.FAILED) && 
               attempts < 3;
    }
    
    /**
     * Check if this notification is eligible for deletion (old and completed).
     * @return true if older than 30 days and status is SENT or FAILED
     */
    public boolean isEligibleForArchival() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return (status == NotificationStatus.SENT || 
                status == NotificationStatus.FAILED) && 
               createdAt.isBefore(thirtyDaysAgo);
    }
}
