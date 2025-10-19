package com.payments.domain.events;

import com.payments.domain.shared.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TemplateDeactivatedEvent implements DomainEvent {

    private TemplateId templateId;
    private TenantId tenantId;
    private String name;
    private Instant occurredAt;

    public TemplateDeactivatedEvent(TemplateId templateId, TenantId tenantId, String name, Instant occurredAt) {
        this.templateId = templateId;
        this.tenantId = tenantId;
        this.name = name;
        this.occurredAt = occurredAt;
    }

    @Override
    public String getEventType() {
        return "TemplateDeactivated";
    }
}
