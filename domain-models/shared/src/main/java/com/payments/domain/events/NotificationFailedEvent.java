package com.payments.domain.events;

import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.*;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationFailedEvent implements DomainEvent {

  private NotificationId notificationId;
  private TenantId tenantId;
  private NotificationType type;
  private NotificationChannel channel;
  private String errorMessage;
  private Instant occurredAt;

  public NotificationFailedEvent(
      NotificationId notificationId,
      TenantId tenantId,
      NotificationType type,
      NotificationChannel channel,
      String errorMessage,
      Instant occurredAt) {
    this.notificationId = notificationId;
    this.tenantId = tenantId;
    this.type = type;
    this.channel = channel;
    this.errorMessage = errorMessage;
    this.occurredAt = occurredAt;
  }

  @Override
  public String getEventType() {
    return "NotificationFailed";
  }
}
