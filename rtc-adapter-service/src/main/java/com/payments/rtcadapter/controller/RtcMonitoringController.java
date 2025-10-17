package com.payments.rtcadapter.controller;

import com.payments.rtcadapter.monitoring.RtcMonitoringService;
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
 * RTC Monitoring Controller
 *
 * <p>REST controller for RTC monitoring operations: - Dashboard metrics - Health monitoring -
 * Performance monitoring - Custom metrics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rtc/monitoring")
@RequiredArgsConstructor
@Tag(name = "RTC Monitoring", description = "RTC monitoring and metrics operations")
public class RtcMonitoringController {

  private final RtcMonitoringService rtcMonitoringService;

  /**
   * Get RTC dashboard metrics
   *
   * @return Dashboard metrics
   */
  @GetMapping("/dashboard")
  @Operation(
      summary = "Get RTC dashboard metrics",
      description = "Retrieve comprehensive dashboard metrics for RTC adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
    log.info("Getting RTC dashboard metrics");
    Map<String, Object> metrics = rtcMonitoringService.getDashboardMetrics();
    return ResponseEntity.ok(metrics);
  }

  /**
   * Get RTC health metrics
   *
   * @return Health metrics
   */
  @GetMapping("/health")
  @Operation(
      summary = "Get RTC health metrics",
      description = "Retrieve health status and metrics for RTC adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Health metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getHealthMetrics() {
    log.info("Getting RTC health metrics");
    Map<String, Object> health = rtcMonitoringService.getHealthMetrics();
    return ResponseEntity.ok(health);
  }

  /**
   * Get RTC performance metrics
   *
   * @return Performance metrics
   */
  @GetMapping("/performance")
  @Operation(
      summary = "Get RTC performance metrics",
      description = "Retrieve performance metrics for RTC adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    log.info("Getting RTC performance metrics");
    Map<String, Object> performance = rtcMonitoringService.getPerformanceMetrics();
    return ResponseEntity.ok(performance);
  }
}
