package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Domain event for BankservAfrica message logging
 */
@Value
@AllArgsConstructor
public class BankservAfricaMessageLoggedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String direction;
  String messageType;
  Integer statusCode;
  Instant occurredAt;

  @Override
  public String getEventType() {
    return "BankservAfricaMessageLogged";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
