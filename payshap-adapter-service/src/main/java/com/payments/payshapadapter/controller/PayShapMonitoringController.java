package com.payments.payshapadapter.controller;

import com.payments.payshapadapter.monitoring.PayShapMonitoringService;
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
 * PayShap Monitoring Controller
 *
 * <p>REST controller for PayShap monitoring operations: - Dashboard metrics - Health monitoring -
 * Performance monitoring - Custom metrics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payshap/monitoring")
@RequiredArgsConstructor
@Tag(name = "PayShap Monitoring", description = "PayShap monitoring and metrics operations")
public class PayShapMonitoringController {

  private final PayShapMonitoringService payShapMonitoringService;

  /**
   * Get PayShap dashboard metrics
   *
   * @return Dashboard metrics
   */
  @GetMapping("/dashboard")
  @Operation(
      summary = "Get PayShap dashboard metrics",
      description = "Retrieve comprehensive dashboard metrics for PayShap adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
    log.info("Getting PayShap dashboard metrics");
    Map<String, Object> metrics = payShapMonitoringService.getDashboardMetrics();
    return ResponseEntity.ok(metrics);
  }

  /**
   * Get PayShap health metrics
   *
   * @return Health metrics
   */
  @GetMapping("/health")
  @Operation(
      summary = "Get PayShap health metrics",
      description = "Retrieve health status and metrics for PayShap adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Health metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getHealthMetrics() {
    log.info("Getting PayShap health metrics");
    Map<String, Object> health = payShapMonitoringService.getHealthMetrics();
    return ResponseEntity.ok(health);
  }

  /**
   * Get PayShap performance metrics
   *
   * @return Performance metrics
   */
  @GetMapping("/performance")
  @Operation(
      summary = "Get PayShap performance metrics",
      description = "Retrieve performance metrics for PayShap adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    log.info("Getting PayShap performance metrics");
    Map<String, Object> performance = payShapMonitoringService.getPerformanceMetrics();
    return ResponseEntity.ok(performance);
  }
}
