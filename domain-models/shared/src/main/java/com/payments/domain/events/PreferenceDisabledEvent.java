package com.payments.domain.events;

import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PreferenceDisabledEvent implements DomainEvent {

    private PreferenceId preferenceId;
    private TenantId tenantId;
    private String userId;
    private NotificationType type;
    private NotificationChannel channel;
    private Instant occurredAt;

    public PreferenceDisabledEvent(PreferenceId preferenceId, TenantId tenantId, String userId, NotificationType type, NotificationChannel channel, Instant occurredAt) {
        this.preferenceId = preferenceId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.occurredAt = occurredAt;
    }

    @Override
    public String getEventType() {
        return "PreferenceDisabled";
    }
}
