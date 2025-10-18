package com.payments.reconciliation.controller;

import com.payments.reconciliation.event.ReconciliationEventPublisher;
import com.payments.reconciliation.event.ReconciliationEvents;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reconciliation")
public class ReconciliationController {

  private final ReconciliationEventPublisher eventPublisher;

  public ReconciliationController(ReconciliationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @PostMapping("/run")
  public ResponseEntity<String> runReconciliation() {
    eventPublisher.publish(
        new ReconciliationEvents.ReconciliationRunStartedEvent(
            "RECON-STARTED", "RTC", Instant.now()));
    return ResponseEntity.accepted().body("RECONCILIATION-RUNNING");
  }

  @GetMapping("/exceptions")
  public ResponseEntity<String> listExceptions() {
    return ResponseEntity.ok("[]");
  }
}
