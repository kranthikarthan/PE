package com.payments.settlement.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemorySettlementEventPublisher implements SettlementEventPublisher {
  private static final Logger log = LoggerFactory.getLogger(InMemorySettlementEventPublisher.class);

  @Override
  public void publish(SettlementEvents.SettlementBatchCreatedEvent event) {
    log.info(
        "SettlementBatchCreatedEvent: batchId={} clearingSystem={}",
        event.batchId(),
        event.clearingSystem());
  }

  @Override
  public void publish(SettlementEvents.SettlementCompletedEvent event) {
    log.info(
        "SettlementCompletedEvent: batchId={} status={}",
        event.batchId(),
        event.settlementStatus());
  }
}
