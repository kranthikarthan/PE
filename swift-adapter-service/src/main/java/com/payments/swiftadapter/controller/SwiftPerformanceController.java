package com.payments.swiftadapter.controller;

import com.payments.swiftadapter.performance.SwiftPerformanceOptimizationService;
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
 * SWIFT Performance Controller
 *
 * <p>REST controller for SWIFT performance optimization operations: - Performance metrics -
 * Optimization recommendations - Cache management - Resource optimization
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/swift/performance")
@RequiredArgsConstructor
@Tag(name = "SWIFT Performance", description = "SWIFT performance optimization operations")
public class SwiftPerformanceController {

  private final SwiftPerformanceOptimizationService swiftPerformanceOptimizationService;

  /**
   * Get SWIFT performance metrics
   *
   * @return Performance metrics
   */
  @GetMapping("/metrics")
  @Operation(
      summary = "Get SWIFT performance metrics",
      description = "Retrieve comprehensive performance metrics for SWIFT adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    log.info("Getting SWIFT performance metrics");
    Map<String, Object> metrics = swiftPerformanceOptimizationService.getPerformanceMetrics();
    return ResponseEntity.ok(metrics);
  }

  /**
   * Get SWIFT optimization recommendations
   *
   * @return Optimization recommendations
   */
  @GetMapping("/recommendations")
  @Operation(
      summary = "Get SWIFT optimization recommendations",
      description = "Retrieve performance optimization recommendations for SWIFT adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Optimization recommendations retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getOptimizationRecommendations() {
    log.info("Getting SWIFT optimization recommendations");
    Map<String, Object> recommendations =
        swiftPerformanceOptimizationService.getOptimizationRecommendations();
    return ResponseEntity.ok(recommendations);
  }

  /**
   * Optimize SWIFT resources
   *
   * @return Optimization result
   */
  @PostMapping("/optimize")
  @Operation(
      summary = "Optimize SWIFT resources",
      description = "Perform resource optimization for SWIFT adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Resources optimized successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> optimizeResources() {
    log.info("Optimizing SWIFT resources");
    swiftPerformanceOptimizationService.optimizeResources();

    Map<String, Object> result =
        Map.of(
            "status", "SUCCESS",
            "message", "SWIFT resources optimized successfully",
            "timestamp", java.time.Instant.now().toString());

    return ResponseEntity.ok(result);
  }

  /**
   * Optimize SWIFT international processing
   *
   * @return International optimization result
   */
  @PostMapping("/optimize-international")
  @Operation(
      summary = "Optimize SWIFT international processing",
      description = "Perform international processing optimization for SWIFT adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "International processing optimized successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> optimizeInternationalProcessing() {
    log.info("Optimizing SWIFT international processing");
    swiftPerformanceOptimizationService.optimizeInternationalProcessing();

    Map<String, Object> result =
        Map.of(
            "status", "SUCCESS",
            "message", "SWIFT international processing optimized successfully",
            "timestamp", java.time.Instant.now().toString());

    return ResponseEntity.ok(result);
  }

  /**
   * Evict SWIFT adapter cache
   *
   * @param adapterId Adapter ID
   * @return Cache eviction result
   */
  @DeleteMapping("/cache/{adapterId}")
  @Operation(
      summary = "Evict SWIFT adapter cache",
      description = "Evict cache for specific SWIFT adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache evicted successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> evictAdapterCache(@PathVariable String adapterId) {
    log.info("Evicting SWIFT adapter cache: {}", adapterId);
    swiftPerformanceOptimizationService.evictAdapterCache(adapterId);

    Map<String, Object> result =
        Map.of(
            "status",
            "SUCCESS",
            "message",
            "SWIFT adapter cache evicted successfully",
            "adapterId",
            adapterId,
            "timestamp",
            java.time.Instant.now().toString());

    return ResponseEntity.ok(result);
  }
}
