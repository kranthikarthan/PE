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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA Entity for notification templates with multi-channel support.
 * 
 * Templates use Mustache/Thymeleaf syntax for variable substitution:
 * - Email: {{paymentAmount}}, {{transactionId}}, etc.
 * - SMS: Limited to 140 characters
 * - Push: JSON structure
 * 
 * Supports:
 * - Multi-tenancy via tenantId (with RLS)
 * - Multiple notification types
 * - Channel-specific rendering
 * - Active/Inactive toggle
 * - Full audit trail
 * 
 * @author Payment Engine
 */
@Entity
@Table(
    name = "notification_templates",
    indexes = {
        @Index(name = "idx_template_tenant_type", 
            columnList = "tenant_id,notification_type"),
        @Index(name = "idx_template_tenant_active", 
            columnList = "tenant_id,is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_template_tenant_name", 
            columnNames = {"tenant_id", "name"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique identifier for this template.
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
     * Template name (unique per tenant).
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    /**
     * Type of notification this template handles.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;
    
    /**
     * Set of channels this template supports.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "template_channels",
        joinColumns = @JoinColumn(name = "template_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", length = 20)
    @Builder.Default
    private Set<NotificationChannel> channels = new HashSet<>();
    
    /**
     * Email template in HTML with Mustache placeholders.
     * Example: "Your payment of {{amount}} has been initiated."
     */
    @Column(name = "email_template", columnDefinition = "TEXT")
    private String emailTemplate;
    
    /**
     * Email subject line with Mustache placeholders.
     */
    @Column(name = "email_subject", length = 255)
    private String emailSubject;
    
    /**
     * SMS template (max 140 chars) with Mustache placeholders.
     * Example: "Payment {{transactionId}} for {{amount}} {{currency}} approved."
     */
    @Column(name = "sms_template", length = 160)
    private String smsTemplate;
    
    /**
     * Push notification template in JSON format.
     * Example: {"title": "Payment {{transactionId}}", "body": "Amount: {{amount}}"}
     */
    @Column(name = "push_template", columnDefinition = "TEXT")
    private String pushTemplate;
    
    /**
     * Whether this template is active and can be used.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
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
     * Check if this template supports a specific channel.
     * @param channel the channel to check
     * @return true if template supports the channel
     */
    public boolean supportsChannel(NotificationChannel channel) {
        return channels != null && channels.contains(channel);
    }
    
    /**
     * Check if template has required fields for a channel.
     * @param channel the channel to validate
     * @return true if all required fields are populated
     */
    public boolean isValidForChannel(NotificationChannel channel) {
        if (!supportsChannel(channel)) {
            return false;
        }
        
        switch (channel) {
            case EMAIL:
                return emailTemplate != null && !emailTemplate.trim().isEmpty() &&
                       emailSubject != null && !emailSubject.trim().isEmpty();
            case SMS:
                return smsTemplate != null && !smsTemplate.trim().isEmpty() &&
                       smsTemplate.length() <= 160;
            case PUSH:
                return pushTemplate != null && !pushTemplate.trim().isEmpty();
            default:
                return false;
        }
    }
}
