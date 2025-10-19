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
 * Pure domain entity for Notification Preference - no infrastructure dependencies Contains only
 * business logic and domain rules
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class NotificationPreferenceEntity extends AggregateRoot<PreferenceId> {

  private PreferenceId preferenceId;
  private TenantId tenantId;
  private String userId;
  private NotificationType type;
  private NotificationChannel channel;
  private boolean isEnabled;
  private Map<String, Object> settings;
  private Instant createdAt;
  private Instant updatedAt;

  // Default constructor for builder
  public NotificationPreferenceEntity() {
    // Default constructor for builder compatibility
  }

  // Constructor for creating a new Preference
  public NotificationPreferenceEntity(
      PreferenceId preferenceId,
      TenantId tenantId,
      String userId,
      NotificationType type,
      NotificationChannel channel,
      boolean isEnabled,
      Map<String, Object> settings) {
    if (preferenceId == null
        || tenantId == null
        || userId == null
        || type == null
        || channel == null) {
      throw new IllegalArgumentException("All preference fields must be provided.");
    }

    this.preferenceId = preferenceId;
    this.tenantId = tenantId;
    this.userId = userId;
    this.type = type;
    this.channel = channel;
    this.isEnabled = isEnabled;
    this.settings = settings;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;

    addDomainEvent(
        new PreferenceCreatedEvent(preferenceId, tenantId, userId, type, channel, this.createdAt));
  }

  // Private constructor for re-hydration from persistence (used by infrastructure layer)
  private NotificationPreferenceEntity(
      PreferenceId preferenceId,
      TenantId tenantId,
      String userId,
      NotificationType type,
      NotificationChannel channel,
      boolean isEnabled,
      Map<String, Object> settings,
      Instant createdAt,
      Instant updatedAt) {
    this.preferenceId = preferenceId;
    this.tenantId = tenantId;
    this.userId = userId;
    this.type = type;
    this.channel = channel;
    this.isEnabled = isEnabled;
    this.settings = settings;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public void enable() {
    if (this.isEnabled) {
      throw new InvalidStateTransitionException("Preference is already enabled.");
    }
    this.isEnabled = true;
    this.updatedAt = Instant.now();
    addDomainEvent(
        new PreferenceEnabledEvent(preferenceId, tenantId, userId, type, channel, this.updatedAt));
  }

  public void disable() {
    if (!this.isEnabled) {
      throw new InvalidStateTransitionException("Preference is already disabled.");
    }
    this.isEnabled = false;
    this.updatedAt = Instant.now();
    addDomainEvent(
        new PreferenceDisabledEvent(preferenceId, tenantId, userId, type, channel, this.updatedAt));
  }

  public void updateSettings(Map<String, Object> newSettings) {
    this.settings = newSettings;
    this.updatedAt = Instant.now();
    addDomainEvent(
        new PreferenceUpdatedEvent(preferenceId, tenantId, userId, type, channel, this.updatedAt));
  }

  public boolean isNotificationAllowed(
      NotificationType notificationType, NotificationChannel notificationChannel) {
    return this.isEnabled && this.type == notificationType && this.channel == notificationChannel;
  }

  // Compatibility methods for backward compatibility
  public String getId() {
    return preferenceId != null ? preferenceId.getValue() : null;
  }

  public java.util.Set<NotificationChannel> getPreferredChannels() {
    return java.util.Set.of(this.channel);
  }

  public void setPreferredChannels(java.util.Set<NotificationChannel> channels) {
    if (channels != null && !channels.isEmpty()) {
      this.channel = channels.iterator().next();
    }
  }

  public Boolean isTransactionAlertsOptIn() {
    return settings != null ? (Boolean) settings.get("transactionAlertsOptIn") : null;
  }

  public void setTransactionAlertsOptIn(Boolean optIn) {
    if (settings == null) {
      settings = new java.util.HashMap<>();
    }
    settings.put("transactionAlertsOptIn", optIn);
  }

  public Boolean isMarketingOptIn() {
    return settings != null ? (Boolean) settings.get("marketingOptIn") : null;
  }

  public void setMarketingOptIn(Boolean optIn) {
    if (settings == null) {
      settings = new java.util.HashMap<>();
    }
    settings.put("marketingOptIn", optIn);
  }

  public Boolean isSystemNotificationsOptIn() {
    return settings != null ? (Boolean) settings.get("systemNotificationsOptIn") : null;
  }

  public void setSystemNotificationsOptIn(Boolean optIn) {
    if (settings == null) {
      settings = new java.util.HashMap<>();
    }
    settings.put("systemNotificationsOptIn", optIn);
  }

  public java.time.LocalTime getQuietHoursStart() {
    return settings != null ? (java.time.LocalTime) settings.get("quietHoursStart") : null;
  }

  public void setQuietHoursStart(java.time.LocalTime start) {
    if (settings == null) {
      settings = new java.util.HashMap<>();
    }
    settings.put("quietHoursStart", start);
  }

  public java.time.LocalTime getQuietHoursEnd() {
    return settings != null ? (java.time.LocalTime) settings.get("quietHoursEnd") : null;
  }

  public void setQuietHoursEnd(java.time.LocalTime end) {
    if (settings == null) {
      settings = new java.util.HashMap<>();
    }
    settings.put("quietHoursEnd", end);
  }

  public boolean isInQuietHours() {
    if (getQuietHoursStart() == null || getQuietHoursEnd() == null) {
      return false;
    }
    java.time.LocalTime now = java.time.LocalTime.now();
    return now.isAfter(getQuietHoursStart()) && now.isBefore(getQuietHoursEnd());
  }

  public boolean isNotificationTypeAllowed(String notificationType) {
    return this.isEnabled && this.type != null && this.type.name().equals(notificationType);
  }

  public boolean isChannelPreferred(String channel) {
    return this.isEnabled && this.channel != null && this.channel.name().equals(channel);
  }

  // Builder compatibility methods
  public static NotificationPreferenceEntityBuilder builder() {
    return new NotificationPreferenceEntityBuilder();
  }

  public static class NotificationPreferenceEntityBuilder {
    private NotificationPreferenceEntity preferenceEntity;

    public NotificationPreferenceEntityBuilder() {
      this.preferenceEntity = new NotificationPreferenceEntity();
    }

    public NotificationPreferenceEntityBuilder id(java.util.UUID id) {
      this.preferenceEntity.preferenceId = PreferenceId.of(id.toString());
      return this;
    }

    public NotificationPreferenceEntityBuilder preferenceId(PreferenceId preferenceId) {
      this.preferenceEntity.preferenceId = preferenceId;
      return this;
    }

    public NotificationPreferenceEntityBuilder tenantId(TenantId tenantId) {
      this.preferenceEntity.tenantId = tenantId;
      return this;
    }

    public NotificationPreferenceEntityBuilder userId(String userId) {
      this.preferenceEntity.userId = userId;
      return this;
    }

    public NotificationPreferenceEntityBuilder type(NotificationType type) {
      this.preferenceEntity.type = type;
      return this;
    }

    public NotificationPreferenceEntityBuilder channel(NotificationChannel channel) {
      this.preferenceEntity.channel = channel;
      return this;
    }

    public NotificationPreferenceEntityBuilder isEnabled(boolean isEnabled) {
      this.preferenceEntity.isEnabled = isEnabled;
      return this;
    }

    public NotificationPreferenceEntityBuilder settings(Map<String, Object> settings) {
      this.preferenceEntity.settings = settings;
      return this;
    }

    public NotificationPreferenceEntityBuilder createdAt(Instant createdAt) {
      this.preferenceEntity.createdAt = createdAt;
      return this;
    }

    public NotificationPreferenceEntityBuilder updatedAt(Instant updatedAt) {
      this.preferenceEntity.updatedAt = updatedAt;
      return this;
    }

    public NotificationPreferenceEntity build() {
      return this.preferenceEntity;
    }
  }
}
