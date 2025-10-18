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
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA Entity for user notification preferences.
 * 
 * Stores:
 * - Preferred notification channels
 * - Unsubscribed channels
 * - Quiet hours (no notifications)
 * - Marketing/transaction alert opt-ins
 * 
 * Supports:
 * - Multi-tenancy via tenantId (with RLS)
 * - GDPR compliance (right to be forgotten)
 * - Per-user opt-in/opt-out management
 * - Quiet hours enforcement
 * 
 * @author Payment Engine
 */
@Entity
@Table(
    name = "notification_preferences",
    indexes = {
        @Index(name = "idx_preference_tenant_user", 
            columnList = "tenant_id,user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_preference_tenant_user", 
            columnNames = {"tenant_id", "user_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique identifier for this preference record.
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
     * User identifier (the user owning these preferences).
     */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;
    
    /**
     * Set of channels user prefers to receive notifications on.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "preference_preferred_channels",
        joinColumns = @JoinColumn(name = "preference_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", length = 20)
    @Builder.Default
    private Set<NotificationChannel> preferredChannels = new HashSet<>();
    
    /**
     * Set of channels user has unsubscribed from.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "preference_unsubscribed_channels",
        joinColumns = @JoinColumn(name = "preference_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", length = 20)
    @Builder.Default
    private Set<NotificationChannel> unsubscribedChannels = new HashSet<>();
    
    /**
     * Quiet hours start time (e.g., 22:00). Null means no quiet hours.
     */
    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;
    
    /**
     * Quiet hours end time (e.g., 08:00). Null means no quiet hours.
     */
    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;
    
    /**
     * Whether user opted in to transaction alerts (payment initiated, cleared, failed).
     */
    @Column(name = "transaction_alerts_opt_in", nullable = false)
    @Builder.Default
    private Boolean transactionAlertsOptIn = true;
    
    /**
     * Whether user opted in to marketing notifications.
     */
    @Column(name = "marketing_opt_in", nullable = false)
    @Builder.Default
    private Boolean marketingOptIn = false;
    
    /**
     * Whether user opted in to system notifications (maintenance, updates).
     */
    @Column(name = "system_notifications_opt_in", nullable = false)
    @Builder.Default
    private Boolean systemNotificationsOptIn = true;
    
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
     * Check if user wants to receive notifications on a specific channel.
     * @param channel the channel to check
     * @return true if user prefers this channel and hasn't unsubscribed
     */
    public boolean isChannelPreferred(NotificationChannel channel) {
        if (unsubscribedChannels != null && unsubscribedChannels.contains(channel)) {
            return false;
        }
        return preferredChannels != null && preferredChannels.contains(channel);
    }
    
    /**
     * Check if current time falls within quiet hours.
     * @return true if quiet hours are configured and current time is within them
     */
    public boolean isInQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            // Quiet hours don't wrap around midnight
            return !now.isBefore(quietHoursStart) && now.isBefore(quietHoursEnd);
        } else {
            // Quiet hours wrap around midnight (e.g., 22:00 to 08:00)
            return !now.isBefore(quietHoursStart) || now.isBefore(quietHoursEnd);
        }
    }
    
    /**
     * Check if user wants to receive a specific type of notification.
     * @param notificationType the type to check
     * @return true if user opted in for this type
     */
    public boolean isNotificationTypeAllowed(NotificationType notificationType) {
        switch (notificationType) {
            case PAYMENT_INITIATED:
            case PAYMENT_VALIDATED:
            case PAYMENT_CLEARED:
            case PAYMENT_FAILED:
            case PAYMENT_REVERSED:
                return transactionAlertsOptIn;
            case MARKETING:
                return marketingOptIn;
            case SYSTEM_NOTIFICATION:
            case TENANT_ALERT:
                return systemNotificationsOptIn;
            default:
                return true;
        }
    }
}
