package com.payments.domain.events;

import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationCancelledEvent implements DomainEvent {

    private NotificationId notificationId;
    private TenantId tenantId;
    private NotificationType type;
    private NotificationChannel channel;
    private Instant occurredAt;

    public NotificationCancelledEvent(NotificationId notificationId, TenantId tenantId, NotificationType type, NotificationChannel channel, Instant occurredAt) {
        this.notificationId = notificationId;
        this.tenantId = tenantId;
        this.type = type;
        this.channel = channel;
        this.occurredAt = occurredAt;
    }

    @Override
    public String getEventType() {
        return "NotificationCancelled";
    }
}
