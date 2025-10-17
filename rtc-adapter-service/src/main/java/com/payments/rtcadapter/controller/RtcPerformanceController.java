package com.payments.rtcadapter.controller;

import com.payments.rtcadapter.performance.RtcPerformanceOptimizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RTC Performance Controller
 *
 * <p>REST controller for RTC performance optimization operations: - Performance metrics -
 * Optimization recommendations - Cache management - Resource optimization
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rtc/performance")
@RequiredArgsConstructor
@Tag(name = "RTC Performance", description = "RTC performance optimization operations")
public class RtcPerformanceController {

  private final RtcPerformanceOptimizationService rtcPerformanceOptimizationService;

  /**
   * Get RTC performance metrics
   *
   * @return Performance metrics
   */
  @GetMapping("/metrics")
  @Operation(
      summary = "Get RTC performance metrics",
      description = "Retrieve comprehensive performance metrics for RTC adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    log.info("Getting RTC performance metrics");
    Map<String, Object> metrics = rtcPerformanceOptimizationService.getPerformanceMetrics();
    return ResponseEntity.ok(metrics);
  }

  /**
   * Get RTC optimization recommendations
   *
   * @return Optimization recommendations
   */
  @GetMapping("/recommendations")
  @Operation(
      summary = "Get RTC optimization recommendations",
      description = "Retrieve performance optimization recommendations for RTC adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Optimization recommendations retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getOptimizationRecommendations() {
    log.info("Getting RTC optimization recommendations");
    Map<String, Object> recommendations =
        rtcPerformanceOptimizationService.getOptimizationRecommendations();
    return ResponseEntity.ok(recommendations);
  }

  /**
   * Optimize RTC resources
   *
   * @return Optimization result
   */
  @PostMapping("/optimize")
  @Operation(
      summary = "Optimize RTC resources",
      description = "Perform resource optimization for RTC adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Resources optimized successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> optimizeResources() {
    log.info("Optimizing RTC resources");
    rtcPerformanceOptimizationService.optimizeResources();

    Map<String, Object> result =
        Map.of(
            "status", "SUCCESS",
            "message", "RTC resources optimized successfully",
            "timestamp", java.time.Instant.now().toString());

    return ResponseEntity.ok(result);
  }

  /**
   * Optimize RTC real-time processing
   *
   * @return Real-time optimization result
   */
  @PostMapping("/optimize-realtime")
  @Operation(
      summary = "Optimize RTC real-time processing",
      description = "Perform real-time processing optimization for RTC adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Real-time processing optimized successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> optimizeRealTimeProcessing() {
    log.info("Optimizing RTC real-time processing");
    rtcPerformanceOptimizationService.optimizeRealTimeProcessing();

    Map<String, Object> result =
        Map.of(
            "status", "SUCCESS",
            "message", "RTC real-time processing optimized successfully",
            "timestamp", java.time.Instant.now().toString());

    return ResponseEntity.ok(result);
  }

  /**
   * Evict RTC adapter cache
   *
   * @param adapterId Adapter ID
   * @return Cache eviction result
   */
  @DeleteMapping("/cache/{adapterId}")
  @Operation(
      summary = "Evict RTC adapter cache",
      description = "Evict cache for specific RTC adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache evicted successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> evictAdapterCache(@PathVariable String adapterId) {
    log.info("Evicting RTC adapter cache: {}", adapterId);
    rtcPerformanceOptimizationService.evictAdapterCache(adapterId);

    Map<String, Object> result =
        Map.of(
            "status",
            "SUCCESS",
            "message",
            "RTC adapter cache evicted successfully",
            "adapterId",
            adapterId,
            "timestamp",
            java.time.Instant.now().toString());

    return ResponseEntity.ok(result);
  }
}
