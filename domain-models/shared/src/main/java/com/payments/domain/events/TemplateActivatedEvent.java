package com.payments.domain.events;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemplateActivatedEvent implements DomainEvent {

  private TemplateId templateId;
  private TenantId tenantId;
  private String name;
  private Instant occurredAt;

  public TemplateActivatedEvent(
      TemplateId templateId, TenantId tenantId, String name, Instant occurredAt) {
    this.templateId = templateId;
    this.tenantId = tenantId;
    this.name = name;
    this.occurredAt = occurredAt;
  }

  @Override
  public String getEventType() {
    return "TemplateActivated";
  }
}
