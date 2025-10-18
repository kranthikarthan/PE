package com.payments.settlement.event;

public interface SettlementEventPublisher {
  void publish(SettlementEvents.SettlementBatchCreatedEvent event);

  void publish(SettlementEvents.SettlementCompletedEvent event);
}
