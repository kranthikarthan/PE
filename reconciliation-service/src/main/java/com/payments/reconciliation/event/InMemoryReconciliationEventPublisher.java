package com.payments.reconciliation.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemoryReconciliationEventPublisher implements ReconciliationEventPublisher {
  private static final Logger log =
      LoggerFactory.getLogger(InMemoryReconciliationEventPublisher.class);

  @Override
  public void publish(ReconciliationEvents.ReconciliationRunStartedEvent event) {
    log.info(
        "ReconciliationRunStartedEvent: id={} system={}",
        event.reconciliationId(),
        event.clearingSystem());
  }

  @Override
  public void publish(ReconciliationEvents.ReconciliationRunCompletedEvent event) {
    log.info(
        "ReconciliationRunCompletedEvent: id={} status={}",
        event.reconciliationId(),
        event.status());
  }
}
