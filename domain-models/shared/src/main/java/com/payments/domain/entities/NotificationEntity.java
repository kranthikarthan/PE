package com.payments.domain.entities;

import com.payments.domain.events.*;
import com.payments.domain.exceptions.*;
import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.*;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Pure domain entity for Notification - no infrastructure dependencies Contains only business logic
 * and domain rules
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class NotificationEntity extends AggregateRoot<NotificationId> {

  private NotificationId notificationId;
  private TenantId tenantId;
  private String userId;
  private NotificationType type;
  private NotificationChannel channel;
  private NotificationStatus status;
  private String subject;
  private String content;
  private String recipient;
  private Map<String, Object> metadata;
  private Instant createdAt;
  private Instant sentAt;
  private Instant deliveredAt;
  private String errorMessage;
  private Integer retryCount;
  private Instant expiresAt;

  // Default constructor for builder
  public NotificationEntity() {
    // Default constructor for builder compatibility
  }

  // Constructor for creating a new Notification
  public NotificationEntity(
      NotificationId notificationId,
      TenantId tenantId,
      String userId,
      NotificationType type,
      NotificationChannel channel,
      String subject,
      String content,
      String recipient,
      Map<String, Object> metadata,
      Instant expiresAt) {
    if (notificationId == null
        || tenantId == null
        || userId == null
        || type == null
        || channel == null
        || subject == null
        || content == null
        || recipient == null) {
      throw new IllegalArgumentException("All notification fields must be provided.");
    }

    this.notificationId = notificationId;
    this.tenantId = tenantId;
    this.userId = userId;
    this.type = type;
    this.channel = channel;
    this.status = NotificationStatus.PENDING;
    this.subject = subject;
    this.content = content;
    this.recipient = recipient;
    this.metadata = metadata;
    this.createdAt = Instant.now();
    this.retryCount = 0;
    this.expiresAt = expiresAt;

    addDomainEvent(
        new NotificationCreatedEvent(notificationId, tenantId, type, channel, this.createdAt));
  }

  // Private constructor for re-hydration from persistence (used by infrastructure layer)
  private NotificationEntity(
      NotificationId notificationId,
      TenantId tenantId,
      String userId,
      NotificationType type,
      NotificationChannel channel,
      NotificationStatus status,
      String subject,
      String content,
      String recipient,
      Map<String, Object> metadata,
      Instant createdAt,
      Instant sentAt,
      Instant deliveredAt,
      String errorMessage,
      Integer retryCount,
      Instant expiresAt) {
    this.notificationId = notificationId;
    this.tenantId = tenantId;
    this.userId = userId;
    this.type = type;
    this.channel = channel;
    this.status = status;
    this.subject = subject;
    this.content = content;
    this.recipient = recipient;
    this.metadata = metadata;
    this.createdAt = createdAt;
    this.sentAt = sentAt;
    this.deliveredAt = deliveredAt;
    this.errorMessage = errorMessage;
    this.retryCount = retryCount;
    this.expiresAt = expiresAt;
  }

  public void markAsSent() {
    if (this.status != NotificationStatus.PENDING) {
      throw new InvalidStateTransitionException(
          "Notification can only be marked as sent from PENDING status.");
    }
    this.status = NotificationStatus.SENT;
    this.sentAt = Instant.now();
    addDomainEvent(new NotificationSentEvent(notificationId, tenantId, type, channel, this.sentAt));
  }

  public void markAsDelivered() {
    if (this.status != NotificationStatus.SENT) {
      throw new InvalidStateTransitionException(
          "Notification can only be marked as delivered from SENT status.");
    }
    this.status = NotificationStatus.DELIVERED;
    this.deliveredAt = Instant.now();
    addDomainEvent(
        new NotificationDeliveredEvent(notificationId, tenantId, type, channel, this.deliveredAt));
  }

  public void markAsFailed(String errorMessage) {
    if (this.status == NotificationStatus.DELIVERED
        || this.status == NotificationStatus.CANCELLED) {
      throw new InvalidStateTransitionException(
          "Cannot fail a delivered or cancelled notification.");
    }
    this.status = NotificationStatus.FAILED;
    this.errorMessage = errorMessage;
    this.retryCount++;
    addDomainEvent(
        new NotificationFailedEvent(
            notificationId, tenantId, type, channel, errorMessage, Instant.now()));
  }

  public void cancel() {
    if (this.status == NotificationStatus.DELIVERED) {
      throw new InvalidStateTransitionException("Cannot cancel a delivered notification.");
    }
    this.status = NotificationStatus.CANCELLED;
    addDomainEvent(
        new NotificationCancelledEvent(notificationId, tenantId, type, channel, Instant.now()));
  }

  public boolean isExpired() {
    return expiresAt != null && Instant.now().isAfter(expiresAt);
  }

  public boolean canRetry() {
    return this.status == NotificationStatus.FAILED && this.retryCount < 3;
  }

  public void resetForRetry() {
    if (!canRetry()) {
      throw new InvalidStateTransitionException("Notification cannot be retried.");
    }
    this.status = NotificationStatus.PENDING;
    this.errorMessage = null;
  }

  // Convenience methods for backward compatibility
  public String getRecipientAddress() {
    return recipient;
  }

  public String getTemplateData() {
    return metadata != null ? metadata.toString() : null;
  }

  public String getNotificationType() {
    return type != null ? type.name() : null;
  }

  public String getChannelType() {
    return channel != null ? channel.name() : null;
  }

  public Integer getAttempts() {
    return retryCount;
  }

  public void setAttempts(Integer attempts) {
    this.retryCount = attempts;
  }

  public void setFailureReason(String reason) {
    this.errorMessage = reason;
  }

  public void setLastAttemptAt(Instant lastAttempt) {
    // This could be stored in metadata or a separate field
    if (metadata == null) {
      metadata = new java.util.HashMap<>();
    }
    metadata.put("lastAttemptAt", lastAttempt.toString());
  }

  // Additional compatibility methods
  public String getId() {
    return notificationId != null ? notificationId.getValue() : null;
  }

  public String getExternalId() {
    return metadata != null ? (String) metadata.get("externalId") : null;
  }

  public String getFailureReason() {
    return errorMessage;
  }

  public Instant getLastAttemptAt() {
    if (metadata != null && metadata.containsKey("lastAttemptAt")) {
      return Instant.parse((String) metadata.get("lastAttemptAt"));
    }
    return null;
  }

  public void setExternalId(String externalId) {
    if (metadata == null) {
      metadata = new java.util.HashMap<>();
    }
    metadata.put("externalId", externalId);
  }

  // Builder compatibility methods
  public static NotificationEntityBuilder builder() {
    return new NotificationEntityBuilder();
  }

  public static class NotificationEntityBuilder {
    private NotificationEntity notificationEntity;

    public NotificationEntityBuilder() {
      this.notificationEntity = new NotificationEntity();
    }

    public NotificationEntityBuilder id(java.util.UUID id) {
      this.notificationEntity.notificationId = NotificationId.of(id.toString());
      return this;
    }

    public NotificationEntityBuilder notificationId(NotificationId notificationId) {
      this.notificationEntity.notificationId = notificationId;
      return this;
    }

    public NotificationEntityBuilder tenantId(TenantId tenantId) {
      this.notificationEntity.tenantId = tenantId;
      return this;
    }

    public NotificationEntityBuilder userId(String userId) {
      this.notificationEntity.userId = userId;
      return this;
    }

    public NotificationEntityBuilder type(NotificationType type) {
      this.notificationEntity.type = type;
      return this;
    }

    public NotificationEntityBuilder notificationType(NotificationType type) {
      this.notificationEntity.type = type;
      return this;
    }

    public NotificationEntityBuilder channel(NotificationChannel channel) {
      this.notificationEntity.channel = channel;
      return this;
    }

    public NotificationEntityBuilder channelType(NotificationChannel channel) {
      this.notificationEntity.channel = channel;
      return this;
    }

    public NotificationEntityBuilder status(NotificationStatus status) {
      this.notificationEntity.status = status;
      return this;
    }

    public NotificationEntityBuilder subject(String subject) {
      this.notificationEntity.subject = subject;
      return this;
    }

    public NotificationEntityBuilder content(String content) {
      this.notificationEntity.content = content;
      return this;
    }

    public NotificationEntityBuilder recipient(String recipient) {
      this.notificationEntity.recipient = recipient;
      return this;
    }

    public NotificationEntityBuilder recipientAddress(String recipient) {
      this.notificationEntity.recipient = recipient;
      return this;
    }

    public NotificationEntityBuilder metadata(Map<String, Object> metadata) {
      this.notificationEntity.metadata = metadata;
      return this;
    }

    public NotificationEntityBuilder templateId(java.util.UUID templateId) {
      if (this.notificationEntity.metadata == null) {
        this.notificationEntity.metadata = new java.util.HashMap<>();
      }
      this.notificationEntity.metadata.put("templateId", templateId.toString());
      return this;
    }

    public NotificationEntityBuilder createdAt(Instant createdAt) {
      this.notificationEntity.createdAt = createdAt;
      return this;
    }

    public NotificationEntityBuilder sentAt(Instant sentAt) {
      this.notificationEntity.sentAt = sentAt;
      return this;
    }

    public NotificationEntityBuilder deliveredAt(Instant deliveredAt) {
      this.notificationEntity.deliveredAt = deliveredAt;
      return this;
    }

    public NotificationEntityBuilder errorMessage(String errorMessage) {
      this.notificationEntity.errorMessage = errorMessage;
      return this;
    }

    public NotificationEntityBuilder retryCount(Integer retryCount) {
      this.notificationEntity.retryCount = retryCount;
      return this;
    }

    public NotificationEntityBuilder expiresAt(Instant expiresAt) {
      this.notificationEntity.expiresAt = expiresAt;
      return this;
    }

    public NotificationEntity build() {
      return this.notificationEntity;
    }
  }

  // Business logic methods
  public boolean isInQuietHours() {
    // Check if current time is within quiet hours
    // This would typically check user preferences
    return false; // Default implementation
  }

  public boolean isChannelPreferred(NotificationChannel preferredChannel) {
    return this.channel != null && this.channel.equals(preferredChannel);
  }

  public boolean isNotificationTypeAllowed(NotificationType allowedType) {
    return this.type != null && this.type.equals(allowedType);
  }
}
