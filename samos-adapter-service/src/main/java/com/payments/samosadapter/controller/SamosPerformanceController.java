package com.payments.samosadapter.controller;

import com.payments.samosadapter.performance.SamosPerformanceOptimizationService;
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
 * SAMOS Performance Controller
 *
 * <p>REST controller for SAMOS performance optimization operations: - Performance metrics -
 * Optimization recommendations - Cache management - Resource optimization
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/samos/performance")
@RequiredArgsConstructor
@Tag(name = "SAMOS Performance", description = "SAMOS performance optimization operations")
public class SamosPerformanceController {

  private final SamosPerformanceOptimizationService samosPerformanceOptimizationService;

  /**
   * Get SAMOS performance metrics
   *
   * @return Performance metrics
   */
  @GetMapping("/metrics")
  @Operation(
      summary = "Get SAMOS performance metrics",
      description = "Retrieve comprehensive performance metrics for SAMOS adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    log.info("Getting SAMOS performance metrics");
    Map<String, Object> metrics = samosPerformanceOptimizationService.getPerformanceMetrics();
    return ResponseEntity.ok(metrics);
  }

  /**
   * Get SAMOS optimization recommendations
   *
   * @return Optimization recommendations
   */
  @GetMapping("/recommendations")
  @Operation(
      summary = "Get SAMOS optimization recommendations",
      description = "Retrieve performance optimization recommendations for SAMOS adapter")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Optimization recommendations retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getOptimizationRecommendations() {
    log.info("Getting SAMOS optimization recommendations");
    Map<String, Object> recommendations =
        samosPerformanceOptimizationService.getOptimizationRecommendations();
    return ResponseEntity.ok(recommendations);
  }

  /**
   * Optimize SAMOS resources
   *
   * @return Optimization result
   */
  @PostMapping("/optimize")
  @Operation(
      summary = "Optimize SAMOS resources",
      description = "Perform resource optimization for SAMOS adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Resources optimized successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> optimizeResources() {
    log.info("Optimizing SAMOS resources");
    samosPerformanceOptimizationService.optimizeResources();

    Map<String, Object> result =
        Map.of(
            "status", "SUCCESS",
            "message", "SAMOS resources optimized successfully",
            "timestamp", java.time.Instant.now().toString());

    return ResponseEntity.ok(result);
  }

  /**
   * Evict SAMOS adapter cache
   *
   * @param adapterId Adapter ID
   * @return Cache eviction result
   */
  @DeleteMapping("/cache/{adapterId}")
  @Operation(
      summary = "Evict SAMOS adapter cache",
      description = "Evict cache for specific SAMOS adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache evicted successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> evictAdapterCache(@PathVariable String adapterId) {
    log.info("Evicting SAMOS adapter cache: {}", adapterId);
    samosPerformanceOptimizationService.evictAdapterCache(adapterId);

    Map<String, Object> result =
        Map.of(
            "status",
            "SUCCESS",
            "message",
            "SAMOS adapter cache evicted successfully",
            "adapterId",
            adapterId,
            "timestamp",
            java.time.Instant.now().toString());

    return ResponseEntity.ok(result);
  }
}
