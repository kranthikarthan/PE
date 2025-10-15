package com.payments.domain.account;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Account Cached */
@Value
@AllArgsConstructor
public class AccountCachedEvent implements DomainEvent {
  AccountAdapterId adapterId;
  AccountNumber accountNumber;
  String accountHolderName;

  @Override
  public String getEventType() {
    return "AccountCached";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
