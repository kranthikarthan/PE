package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Configuration Updated */
@Value
@AllArgsConstructor
public class ConfigurationUpdatedEvent implements DomainEvent {
  TenantId tenantId;
  String configKey;
  String configValue;
  ConfigurationType configType;

  @Override
  public String getEventType() {
    return "ConfigurationUpdated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
