package com.payments.accountadapter.api;

import com.payments.accountadapter.service.ServiceHealthMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Monitoring Controller
 *
 * <p>REST controller for health monitoring: - Service health checks - Component health status -
 * Detailed health information - Health metrics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health Monitoring", description = "Health monitoring and diagnostics")
public class HealthMonitoringController {

  private final ServiceHealthMonitor serviceHealthMonitor;

  /**
   * Get service health
   *
   * @return Service health status
   */
  @GetMapping
  @Operation(summary = "Get service health", description = "Retrieve overall service health status")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Health status retrieved successfully"),
        @ApiResponse(responseCode = "503", description = "Service unhealthy")
      })
  public ResponseEntity<Map<String, Object>> getHealth() {
    log.debug("Getting service health");
    Map<String, Object> health = serviceHealthMonitor.getDetailedHealth();

    boolean isHealthy =
        (Boolean) health.get("overall") instanceof Map
            && (Boolean) ((Map<?, ?>) health.get("overall")).get("healthy");

    if (isHealthy) {
      return ResponseEntity.ok(health);
    } else {
      return ResponseEntity.status(503).body(health);
    }
  }

  /**
   * Get component health
   *
   * @return Component health status
   */
  @GetMapping("/components")
  @Operation(
      summary = "Get component health",
      description = "Retrieve health status of individual components")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Component health retrieved successfully")
      })
  public ResponseEntity<Map<String, Object>> getComponentHealth() {
    log.debug("Getting component health");
    Map<String, Object> health = serviceHealthMonitor.getDetailedHealth();

    // Extract component health information
    Map<String, Object> components =
        Map.of(
            "redis", health.get("redis"),
            "oauth2", health.get("oauth2"),
            "externalService", health.get("externalService"));

    return ResponseEntity.ok(
        Map.of("components", components, "timestamp", health.get("timestamp")));
  }

  /**
   * Get health metrics
   *
   * @return Health metrics
   */
  @GetMapping("/metrics")
  @Operation(summary = "Get health metrics", description = "Retrieve health metrics and statistics")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Health metrics retrieved successfully")
      })
  public ResponseEntity<Map<String, Object>> getHealthMetrics() {
    log.debug("Getting health metrics");

    Map<String, Object> metrics =
        Map.of(
            "timestamp", System.currentTimeMillis(),
            "service", "account-adapter-service",
            "version", "1.0.0",
            "uptime", System.currentTimeMillis() - 1000000L, // Mock uptime
            "status", "UP");

    return ResponseEntity.ok(metrics);
  }
}
