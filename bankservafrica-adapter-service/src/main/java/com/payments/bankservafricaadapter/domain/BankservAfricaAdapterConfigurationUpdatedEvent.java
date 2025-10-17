package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for BankservAfrica adapter configuration update */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaAdapterConfigurationUpdatedEvent implements DomainEvent {

  private ClearingAdapterId adapterId;
  private String endpoint;
  private String apiVersion;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "BankservAfricaAdapterConfigurationUpdated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
