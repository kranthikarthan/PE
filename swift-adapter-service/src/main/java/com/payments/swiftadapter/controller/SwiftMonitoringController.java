package com.payments.swiftadapter.controller;

import com.payments.swiftadapter.monitoring.SwiftMonitoringService;
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
 * SWIFT Monitoring Controller
 *
 * <p>REST controller for SWIFT monitoring operations: - Dashboard metrics - Health monitoring -
 * Performance monitoring - Custom metrics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/swift/monitoring")
@RequiredArgsConstructor
@Tag(name = "SWIFT Monitoring", description = "SWIFT monitoring and metrics operations")
public class SwiftMonitoringController {

  private final SwiftMonitoringService swiftMonitoringService;

  /**
   * Get SWIFT dashboard metrics
   *
   * @return Dashboard metrics
   */
  @GetMapping("/dashboard")
  @Operation(
      summary = "Get SWIFT dashboard metrics",
      description = "Retrieve comprehensive dashboard metrics for SWIFT adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
    log.info("Getting SWIFT dashboard metrics");
    Map<String, Object> metrics = swiftMonitoringService.getDashboardMetrics();
    return ResponseEntity.ok(metrics);
  }

  /**
   * Get SWIFT health metrics
   *
   * @return Health metrics
   */
  @GetMapping("/health")
  @Operation(
      summary = "Get SWIFT health metrics",
      description = "Retrieve health status and metrics for SWIFT adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Health metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getHealthMetrics() {
    log.info("Getting SWIFT health metrics");
    Map<String, Object> health = swiftMonitoringService.getHealthMetrics();
    return ResponseEntity.ok(health);
  }

  /**
   * Get SWIFT performance metrics
   *
   * @return Performance metrics
   */
  @GetMapping("/performance")
  @Operation(
      summary = "Get SWIFT performance metrics",
      description = "Retrieve performance metrics for SWIFT adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    log.info("Getting SWIFT performance metrics");
    Map<String, Object> performance = swiftMonitoringService.getPerformanceMetrics();
    return ResponseEntity.ok(performance);
  }
}
