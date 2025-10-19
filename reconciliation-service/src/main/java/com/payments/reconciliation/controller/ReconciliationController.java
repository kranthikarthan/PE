package com.payments.reconciliation.controller;

import com.payments.reconciliation.event.ReconciliationEventPublisher;
import com.payments.reconciliation.event.ReconciliationEvents;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reconciliation")
@Validated
public class ReconciliationController {

  private final ReconciliationEventPublisher eventPublisher;

  public ReconciliationController(ReconciliationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @PostMapping("/run")
  public ResponseEntity<String> runReconciliation(
      @RequestParam(name = "system", required = false)
          @Size(min = 2, max = 10, message = "system must be 2-10 chars")
          String system) {
    String clearingSystem = (system == null || system.isBlank()) ? "RTC" : system;
    eventPublisher.publish(
        new ReconciliationEvents.ReconciliationRunStartedEvent(
            "RECON-STARTED", clearingSystem, Instant.now()));
    return ResponseEntity.accepted().body("RECONCILIATION-RUNNING");
  }

  @GetMapping("/exceptions")
  public ResponseEntity<String> listExceptions() {
    return ResponseEntity.ok("[]");
  }
}
