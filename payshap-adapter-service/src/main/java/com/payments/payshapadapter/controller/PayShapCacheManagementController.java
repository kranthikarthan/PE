package com.payments.payshapadapter.controller;

import com.payments.payshapadapter.service.PayShapCacheService;
import com.payments.payshapadapter.service.PayShapOAuth2TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PayShap Cache Management Controller
 *
 * <p>REST controller for PayShap cache management operations: - Cache statistics - Cache clearing -
 * Token management - Cache health
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payshap/cache")
@RequiredArgsConstructor
@Tag(name = "PayShap Cache Management", description = "PayShap cache management operations")
public class PayShapCacheManagementController {

  private final PayShapCacheService payShapCacheService;
  private final PayShapOAuth2TokenService payShapOAuth2TokenService;

  /**
   * Get PayShap cache statistics
   *
   * @return Cache statistics
   */
  @GetMapping("/statistics")
  @Operation(
      summary = "Get PayShap cache statistics",
      description = "Retrieve PayShap cache statistics and metrics")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<PayShapCacheService.CacheStatistics> getCacheStatistics() {
    log.info("Getting PayShap cache statistics");
    PayShapCacheService.CacheStatistics statistics = payShapCacheService.getCacheStatistics();
    return ResponseEntity.ok(statistics);
  }

  /**
   * Clear PayShap adapter cache
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @return Success response
   */
  @DeleteMapping("/adapters/{adapterId}")
  @Operation(
      summary = "Clear PayShap adapter cache",
      description = "Clear cache for specific PayShap adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Adapter cache cleared successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearAdapterCache(
      @Parameter(description = "Adapter ID", required = true) @PathVariable String adapterId,
      @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID")
          String tenantId) {

    log.info("Clearing cache for PayShap adapter: {} and tenant: {}", adapterId, tenantId);
    payShapCacheService.clearAdapterCache(adapterId, tenantId);

    return ResponseEntity.ok(
        Map.of(
            "message",
            "PayShap adapter cache cleared successfully",
            "adapterId",
            adapterId,
            "tenantId",
            tenantId,
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear PayShap tenant cache
   *
   * @param tenantId Tenant ID
   * @return Success response
   */
  @DeleteMapping("/tenants/{tenantId}")
  @Operation(
      summary = "Clear PayShap tenant cache",
      description = "Clear cache for specific tenant")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tenant cache cleared successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearTenantCache(
      @Parameter(description = "Tenant ID", required = true) @PathVariable String tenantId) {

    log.info("Clearing cache for PayShap tenant: {}", tenantId);
    payShapCacheService.clearTenantCache(tenantId);

    return ResponseEntity.ok(
        Map.of(
            "message",
            "PayShap tenant cache cleared successfully",
            "tenantId",
            tenantId,
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear all PayShap cache
   *
   * @return Success response
   */
  @DeleteMapping("/all")
  @Operation(summary = "Clear all PayShap cache", description = "Clear all PayShap cache entries")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "All cache cleared successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearAllCache() {
    log.info("Clearing all PayShap cache");
    payShapCacheService.clearAllCache();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "All PayShap cache cleared successfully",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear PayShap OAuth2 token cache
   *
   * @return Success response
   */
  @DeleteMapping("/tokens")
  @Operation(
      summary = "Clear PayShap OAuth2 token cache",
      description = "Clear PayShap OAuth2 token cache")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 token cache cleared successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearTokenCache() {
    log.info("Clearing PayShap OAuth2 token cache");
    payShapOAuth2TokenService.clearTokenCache();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "PayShap OAuth2 token cache cleared successfully",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Refresh PayShap OAuth2 token
   *
   * @return Success response
   */
  @PostMapping("/tokens/refresh")
  @Operation(
      summary = "Refresh PayShap OAuth2 token",
      description = "Refresh PayShap OAuth2 access token")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 token refreshed successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> refreshToken() {
    log.info("Refreshing PayShap OAuth2 token");
    String newToken = payShapOAuth2TokenService.refreshAccessToken();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "PayShap OAuth2 token refreshed successfully",
            "token",
            newToken.substring(0, Math.min(20, newToken.length())) + "...",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Get PayShap OAuth2 token info
   *
   * @return Token information
   */
  @GetMapping("/tokens/info")
  @Operation(
      summary = "Get PayShap OAuth2 token info",
      description = "Get PayShap OAuth2 token information")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token information retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getTokenInfo() {
    log.info("Getting PayShap OAuth2 token info");
    PayShapOAuth2TokenService.TokenInfo tokenInfo = payShapOAuth2TokenService.getTokenInfo();

    if (tokenInfo != null) {
      return ResponseEntity.ok(
          Map.of(
              "tokenType", tokenInfo.getTokenType(),
              "expiresIn", tokenInfo.getExpiresIn(),
              "expiresAt", tokenInfo.getExpiresAt(),
              "isValid", payShapOAuth2TokenService.isTokenValid(),
              "timestamp", System.currentTimeMillis()));
    } else {
      return ResponseEntity.ok(
          Map.of(
              "message",
              "No PayShap token information available",
              "isValid",
              false,
              "timestamp",
              System.currentTimeMillis()));
    }
  }

  /**
   * Get PayShap cache health
   *
   * @return Cache health status
   */
  @GetMapping("/health")
  @Operation(summary = "Get PayShap cache health", description = "Get PayShap cache health status")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache health retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getCacheHealth() {
    log.info("Getting PayShap cache health");
    boolean isHealthy = payShapCacheService.isCacheHealthy();

    return ResponseEntity.ok(
        Map.of(
            "healthy",
            isHealthy,
            "timestamp",
            System.currentTimeMillis(),
            "service",
            "PayShap Cache"));
  }
}
