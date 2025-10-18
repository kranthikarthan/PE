package com.payments.settlement.controller;

import com.payments.settlement.event.SettlementEventPublisher;
import com.payments.settlement.event.SettlementEvents;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settlement")
public class SettlementController {

  private final SettlementEventPublisher eventPublisher;

  public SettlementController(SettlementEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @PostMapping("/batches")
  public ResponseEntity<String> createBatch() {
    eventPublisher.publish(
        new SettlementEvents.SettlementBatchCreatedEvent(
            "BATCH-CREATED", "RTC", Instant.now(), 0, BigDecimal.ZERO));
    return ResponseEntity.status(201).body("BATCH-CREATED");
  }

  @GetMapping("/positions")
  public ResponseEntity<String> getPositions() {
    return ResponseEntity.ok("OK");
  }
}
