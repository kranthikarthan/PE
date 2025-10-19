package com.payments.domain.events;

import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TemplateCreatedEvent implements DomainEvent {

    private TemplateId templateId;
    private TenantId tenantId;
    private String name;
    private NotificationType type;
    private NotificationChannel channel;
    private Instant occurredAt;

    public TemplateCreatedEvent(TemplateId templateId, TenantId tenantId, String name, NotificationType type, NotificationChannel channel, Instant occurredAt) {
        this.templateId = templateId;
        this.tenantId = tenantId;
        this.name = name;
        this.type = type;
        this.channel = channel;
        this.occurredAt = occurredAt;
    }

    @Override
    public String getEventType() {
        return "TemplateCreated";
    }
}
