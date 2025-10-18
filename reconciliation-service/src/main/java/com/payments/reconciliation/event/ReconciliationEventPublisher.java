package com.payments.reconciliation.event;

public interface ReconciliationEventPublisher {
  void publish(ReconciliationEvents.ReconciliationRunStartedEvent event);

  void publish(ReconciliationEvents.ReconciliationRunCompletedEvent event);
}
