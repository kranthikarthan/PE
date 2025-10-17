package com.payments.samosadapter.controller;

import com.payments.samosadapter.monitoring.SamosMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * SAMOS Monitoring Controller
 *
 * <p>REST controller for SAMOS monitoring operations: - Dashboard metrics - Health monitoring - Performance monitoring - Custom metrics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/samos/monitoring")
@RequiredArgsConstructor
@Tag(name = "SAMOS Monitoring", description = "SAMOS monitoring and metrics operations")
public class SamosMonitoringController {

  private final SamosMonitoringService samosMonitoringService;

  /**
   * Get SAMOS dashboard metrics
   *
   * @return Dashboard metrics
   */
  @GetMapping("/dashboard")
  @Operation(
      summary = "Get SAMOS dashboard metrics",
      description = "Retrieve comprehensive dashboard metrics for SAMOS adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Dashboard metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
    log.info("Getting SAMOS dashboard metrics");
    Map<String, Object> metrics = samosMonitoringService.getDashboardMetrics();
    return ResponseEntity.ok(metrics);
  }

  /**
   * Get SAMOS health metrics
   *
   * @return Health metrics
   */
  @GetMapping("/health")
  @Operation(
      summary = "Get SAMOS health metrics",
      description = "Retrieve health status and metrics for SAMOS adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Health metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getHealthMetrics() {
    log.info("Getting SAMOS health metrics");
    Map<String, Object> health = samosMonitoringService.getHealthMetrics();
    return ResponseEntity.ok(health);
  }

  /**
   * Get SAMOS performance metrics
   *
   * @return Performance metrics
   */
  @GetMapping("/performance")
  @Operation(
      summary = "Get SAMOS performance metrics",
      description = "Retrieve performance metrics for SAMOS adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    log.info("Getting SAMOS performance metrics");
    Map<String, Object> performance = samosMonitoringService.getPerformanceMetrics();
    return ResponseEntity.ok(performance);
  }
}
