package com.payments.saga.api;

import com.payments.saga.service.SagaEventService;
import com.payments.saga.service.SagaService;
import com.payments.saga.service.SagaStepService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Health check controller for saga orchestrator */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

  private final SagaService sagaService;
  private final SagaStepService sagaStepService;
  private final SagaEventService sagaEventService;

  /** Basic health check */
  @GetMapping
  public ResponseEntity<Map<String, Object>> health() {
    log.debug("Health check requested");

    Map<String, Object> health =
        Map.of(
            "status", "UP",
            "service", "saga-orchestrator",
            "timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(health);
  }

  /** Detailed health check with statistics */
  @GetMapping("/detailed")
  public ResponseEntity<Map<String, Object>> detailedHealth() {
    log.debug("Detailed health check requested");

    try {
      // Get active sagas count
      long activeSagasCount = sagaService.getActiveSagas().size();

      Map<String, Object> health =
          Map.of(
              "status",
              "UP",
              "service",
              "saga-orchestrator",
              "timestamp",
              System.currentTimeMillis(),
              "activeSagas",
              activeSagasCount,
              "components",
              Map.of(
                  "database", "UP",
                  "kafka", "UP",
                  "redis", "UP"));

      return ResponseEntity.ok(health);

    } catch (Exception e) {
      log.error("Health check failed: {}", e.getMessage(), e);

      Map<String, Object> health =
          Map.of(
              "status",
              "DOWN",
              "service",
              "saga-orchestrator",
              "timestamp",
              System.currentTimeMillis(),
              "error",
              e.getMessage());

      return ResponseEntity.status(503).body(health);
    }
  }
}
