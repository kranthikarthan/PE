package com.payments.domain.account;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Routing Rule Added */
@Value
@AllArgsConstructor
public class RoutingRuleAddedEvent implements DomainEvent {
  AccountAdapterId adapterId;
  RoutingRuleId ruleId;
  String ruleName;
  String condition;

  @Override
  public String getEventType() {
    return "RoutingRuleAdded";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
