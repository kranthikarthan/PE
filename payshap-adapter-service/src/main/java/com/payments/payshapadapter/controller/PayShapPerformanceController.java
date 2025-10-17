package com.payments.payshapadapter.controller;

import com.payments.payshapadapter.performance.PayShapPerformanceOptimizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PayShap Performance Controller
 *
 * <p>REST controller for PayShap performance optimization operations: - Performance metrics - Optimization recommendations - Cache management - Resource optimization
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payshap/performance")
@RequiredArgsConstructor
@Tag(name = "PayShap Performance", description = "PayShap performance optimization operations")
public class PayShapPerformanceController {

  private final PayShapPerformanceOptimizationService payShapPerformanceOptimizationService;

  /**
   * Get PayShap performance metrics
   *
   * @return Performance metrics
   */
  @GetMapping("/metrics")
  @Operation(
      summary = "Get PayShap performance metrics",
      description = "Retrieve comprehensive performance metrics for PayShap adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    log.info("Getting PayShap performance metrics");
    Map<String, Object> metrics = payShapPerformanceOptimizationService.getPerformanceMetrics();
    return ResponseEntity.ok(metrics);
  }

  /**
   * Get PayShap optimization recommendations
   *
   * @return Optimization recommendations
   */
  @GetMapping("/recommendations")
  @Operation(
      summary = "Get PayShap optimization recommendations",
      description = "Retrieve performance optimization recommendations for PayShap adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Optimization recommendations retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getOptimizationRecommendations() {
    log.info("Getting PayShap optimization recommendations");
    Map<String, Object> recommendations = payShapPerformanceOptimizationService.getOptimizationRecommendations();
    return ResponseEntity.ok(recommendations);
  }

  /**
   * Optimize PayShap resources
   *
   * @return Optimization result
   */
  @PostMapping("/optimize")
  @Operation(
      summary = "Optimize PayShap resources",
      description = "Perform resource optimization for PayShap adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Resources optimized successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> optimizeResources() {
    log.info("Optimizing PayShap resources");
    payShapPerformanceOptimizationService.optimizeResources();
    
    Map<String, Object> result = Map.of(
        "status", "SUCCESS",
        "message", "PayShap resources optimized successfully",
        "timestamp", java.time.Instant.now().toString()
    );
    
    return ResponseEntity.ok(result);
  }

  /**
   * Optimize PayShap P2P processing
   *
   * @return P2P optimization result
   */
  @PostMapping("/optimize-p2p")
  @Operation(
      summary = "Optimize PayShap P2P processing",
      description = "Perform P2P processing optimization for PayShap adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "P2P processing optimized successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> optimizeP2PProcessing() {
    log.info("Optimizing PayShap P2P processing");
    payShapPerformanceOptimizationService.optimizeP2PProcessing();
    
    Map<String, Object> result = Map.of(
        "status", "SUCCESS",
        "message", "PayShap P2P processing optimized successfully",
        "timestamp", java.time.Instant.now().toString()
    );
    
    return ResponseEntity.ok(result);
  }

  /**
   * Evict PayShap adapter cache
   *
   * @param adapterId Adapter ID
   * @return Cache eviction result
   */
  @DeleteMapping("/cache/{adapterId}")
  @Operation(
      summary = "Evict PayShap adapter cache",
      description = "Evict cache for specific PayShap adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache evicted successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> evictAdapterCache(@PathVariable String adapterId) {
    log.info("Evicting PayShap adapter cache: {}", adapterId);
    payShapPerformanceOptimizationService.evictAdapterCache(adapterId);
    
    Map<String, Object> result = Map.of(
        "status", "SUCCESS",
        "message", "PayShap adapter cache evicted successfully",
        "adapterId", adapterId,
        "timestamp", java.time.Instant.now().toString()
    );
    
    return ResponseEntity.ok(result);
  }
}
