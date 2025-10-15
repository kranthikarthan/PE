package com.payments.domain.account;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Adapter Configuration Updated */
@Value
@AllArgsConstructor
public class AdapterConfigurationUpdatedEvent implements DomainEvent {
  AccountAdapterId adapterId;
  String baseUrl;
  String apiVersion;

  @Override
  public String getEventType() {
    return "AdapterConfigurationUpdated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
