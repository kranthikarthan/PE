package com.payments.domain.account;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: API Call Logged */
@Value
@AllArgsConstructor
public class ApiCallLoggedEvent implements DomainEvent {
  AccountAdapterId adapterId;
  String endpoint;
  String method;
  Integer statusCode;

  @Override
  public String getEventType() {
    return "ApiCallLogged";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
