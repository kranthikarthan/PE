package com.payments.bankservafricaadapter.controller;

import com.payments.bankservafricaadapter.performance.BankservAfricaPerformanceOptimizationService;
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
 * BankservAfrica Performance Controller
 *
 * <p>REST controller for BankservAfrica performance optimization operations: - Performance metrics - Optimization recommendations - Cache management - Resource optimization
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/bankservafrica/performance")
@RequiredArgsConstructor
@Tag(name = "BankservAfrica Performance", description = "BankservAfrica performance optimization operations")
public class BankservAfricaPerformanceController {

  private final BankservAfricaPerformanceOptimizationService bankservAfricaPerformanceOptimizationService;

  /**
   * Get BankservAfrica performance metrics
   *
   * @return Performance metrics
   */
  @GetMapping("/metrics")
  @Operation(
      summary = "Get BankservAfrica performance metrics",
      description = "Retrieve comprehensive performance metrics for BankservAfrica adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    log.info("Getting BankservAfrica performance metrics");
    Map<String, Object> metrics = bankservAfricaPerformanceOptimizationService.getPerformanceMetrics();
    return ResponseEntity.ok(metrics);
  }

  /**
   * Get BankservAfrica optimization recommendations
   *
   * @return Optimization recommendations
   */
  @GetMapping("/recommendations")
  @Operation(
      summary = "Get BankservAfrica optimization recommendations",
      description = "Retrieve performance optimization recommendations for BankservAfrica adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Optimization recommendations retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getOptimizationRecommendations() {
    log.info("Getting BankservAfrica optimization recommendations");
    Map<String, Object> recommendations = bankservAfricaPerformanceOptimizationService.getOptimizationRecommendations();
    return ResponseEntity.ok(recommendations);
  }

  /**
   * Optimize BankservAfrica resources
   *
   * @return Optimization result
   */
  @PostMapping("/optimize")
  @Operation(
      summary = "Optimize BankservAfrica resources",
      description = "Perform resource optimization for BankservAfrica adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Resources optimized successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> optimizeResources() {
    log.info("Optimizing BankservAfrica resources");
    bankservAfricaPerformanceOptimizationService.optimizeResources();
    
    Map<String, Object> result = Map.of(
        "status", "SUCCESS",
        "message", "BankservAfrica resources optimized successfully",
        "timestamp", java.time.Instant.now().toString()
    );
    
    return ResponseEntity.ok(result);
  }

  /**
   * Optimize BankservAfrica batch processing
   *
   * @return Batch optimization result
   */
  @PostMapping("/optimize-batch")
  @Operation(
      summary = "Optimize BankservAfrica batch processing",
      description = "Perform batch processing optimization for BankservAfrica adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Batch processing optimized successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> optimizeBatchProcessing() {
    log.info("Optimizing BankservAfrica batch processing");
    bankservAfricaPerformanceOptimizationService.optimizeBatchProcessing();
    
    Map<String, Object> result = Map.of(
        "status", "SUCCESS",
        "message", "BankservAfrica batch processing optimized successfully",
        "timestamp", java.time.Instant.now().toString()
    );
    
    return ResponseEntity.ok(result);
  }

  /**
   * Evict BankservAfrica adapter cache
   *
   * @param adapterId Adapter ID
   * @return Cache eviction result
   */
  @DeleteMapping("/cache/{adapterId}")
  @Operation(
      summary = "Evict BankservAfrica adapter cache",
      description = "Evict cache for specific BankservAfrica adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache evicted successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> evictAdapterCache(@PathVariable String adapterId) {
    log.info("Evicting BankservAfrica adapter cache: {}", adapterId);
    bankservAfricaPerformanceOptimizationService.evictAdapterCache(adapterId);
    
    Map<String, Object> result = Map.of(
        "status", "SUCCESS",
        "message", "BankservAfrica adapter cache evicted successfully",
        "adapterId", adapterId,
        "timestamp", java.time.Instant.now().toString()
    );
    
    return ResponseEntity.ok(result);
  }
}
