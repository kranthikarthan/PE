package com.payments.bankservafricaadapter.controller;

import com.payments.bankservafricaadapter.monitoring.BankservAfricaMonitoringService;
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
 * BankservAfrica Monitoring Controller
 *
 * <p>REST controller for BankservAfrica monitoring operations: - Dashboard metrics - Health
 * monitoring - Performance monitoring - Custom metrics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/bankservafrica/monitoring")
@RequiredArgsConstructor
@Tag(
    name = "BankservAfrica Monitoring",
    description = "BankservAfrica monitoring and metrics operations")
public class BankservAfricaMonitoringController {

  private final BankservAfricaMonitoringService bankservAfricaMonitoringService;

  /**
   * Get BankservAfrica dashboard metrics
   *
   * @return Dashboard metrics
   */
  @GetMapping("/dashboard")
  @Operation(
      summary = "Get BankservAfrica dashboard metrics",
      description = "Retrieve comprehensive dashboard metrics for BankservAfrica adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
    log.info("Getting BankservAfrica dashboard metrics");
    Map<String, Object> metrics = bankservAfricaMonitoringService.getDashboardMetrics();
    return ResponseEntity.ok(metrics);
  }

  /**
   * Get BankservAfrica health metrics
   *
   * @return Health metrics
   */
  @GetMapping("/health")
  @Operation(
      summary = "Get BankservAfrica health metrics",
      description = "Retrieve health status and metrics for BankservAfrica adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Health metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getHealthMetrics() {
    log.info("Getting BankservAfrica health metrics");
    Map<String, Object> health = bankservAfricaMonitoringService.getHealthMetrics();
    return ResponseEntity.ok(health);
  }

  /**
   * Get BankservAfrica performance metrics
   *
   * @return Performance metrics
   */
  @GetMapping("/performance")
  @Operation(
      summary = "Get BankservAfrica performance metrics",
      description = "Retrieve performance metrics for BankservAfrica adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    log.info("Getting BankservAfrica performance metrics");
    Map<String, Object> performance = bankservAfricaMonitoringService.getPerformanceMetrics();
    return ResponseEntity.ok(performance);
  }
}
