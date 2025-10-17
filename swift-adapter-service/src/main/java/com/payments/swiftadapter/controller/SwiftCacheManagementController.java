package com.payments.swiftadapter.controller;

import com.payments.swiftadapter.service.SwiftCacheService;
import com.payments.swiftadapter.service.SwiftOAuth2TokenService;
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
 * SWIFT Cache Management Controller
 *
 * <p>REST controller for SWIFT cache management operations: - Cache statistics - Cache clearing - Token management - Cache health
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/swift/cache")
@RequiredArgsConstructor
@Tag(name = "SWIFT Cache Management", description = "SWIFT cache management operations")
public class SwiftCacheManagementController {

  private final SwiftCacheService swiftCacheService;
  private final SwiftOAuth2TokenService swiftOAuth2TokenService;

  /**
   * Get SWIFT cache statistics
   *
   * @return Cache statistics
   */
  @GetMapping("/statistics")
  @Operation(
      summary = "Get SWIFT cache statistics",
      description = "Retrieve SWIFT cache statistics and metrics")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<SwiftCacheService.CacheStatistics> getCacheStatistics() {
    log.info("Getting SWIFT cache statistics");
    SwiftCacheService.CacheStatistics statistics = swiftCacheService.getCacheStatistics();
    return ResponseEntity.ok(statistics);
  }

  /**
   * Clear SWIFT adapter cache
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @return Success response
   */
  @DeleteMapping("/adapters/{adapterId}")
  @Operation(summary = "Clear SWIFT adapter cache", description = "Clear cache for specific SWIFT adapter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Adapter cache cleared successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearAdapterCache(
      @Parameter(description = "Adapter ID", required = true) @PathVariable
          String adapterId,
      @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID")
          String tenantId) {

    log.info("Clearing cache for SWIFT adapter: {} and tenant: {}", adapterId, tenantId);
    swiftCacheService.clearAdapterCache(adapterId, tenantId);

    return ResponseEntity.ok(
        Map.of(
            "message",
            "SWIFT adapter cache cleared successfully",
            "adapterId",
            adapterId,
            "tenantId",
            tenantId,
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear SWIFT tenant cache
   *
   * @param tenantId Tenant ID
   * @return Success response
   */
  @DeleteMapping("/tenants/{tenantId}")
  @Operation(summary = "Clear SWIFT tenant cache", description = "Clear cache for specific tenant")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tenant cache cleared successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearTenantCache(
      @Parameter(description = "Tenant ID", required = true) @PathVariable String tenantId) {

    log.info("Clearing cache for SWIFT tenant: {}", tenantId);
    swiftCacheService.clearTenantCache(tenantId);

    return ResponseEntity.ok(
        Map.of(
            "message",
            "SWIFT tenant cache cleared successfully",
            "tenantId",
            tenantId,
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear all SWIFT cache
   *
   * @return Success response
   */
  @DeleteMapping("/all")
  @Operation(summary = "Clear all SWIFT cache", description = "Clear all SWIFT cache entries")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "All cache cleared successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearAllCache() {
    log.info("Clearing all SWIFT cache");
    swiftCacheService.clearAllCache();

    return ResponseEntity.ok(
        Map.of(
            "message", "All SWIFT cache cleared successfully", "timestamp", System.currentTimeMillis()));
  }

  /**
   * Clear SWIFT OAuth2 token cache
   *
   * @return Success response
   */
  @DeleteMapping("/tokens")
  @Operation(summary = "Clear SWIFT OAuth2 token cache", description = "Clear SWIFT OAuth2 token cache")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 token cache cleared successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearTokenCache() {
    log.info("Clearing SWIFT OAuth2 token cache");
    swiftOAuth2TokenService.clearTokenCache();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "SWIFT OAuth2 token cache cleared successfully",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Refresh SWIFT OAuth2 token
   *
   * @return Success response
   */
  @PostMapping("/tokens/refresh")
  @Operation(summary = "Refresh SWIFT OAuth2 token", description = "Refresh SWIFT OAuth2 access token")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 token refreshed successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> refreshToken() {
    log.info("Refreshing SWIFT OAuth2 token");
    String newToken = swiftOAuth2TokenService.refreshAccessToken();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "SWIFT OAuth2 token refreshed successfully",
            "token",
            newToken.substring(0, Math.min(20, newToken.length())) + "...",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Get SWIFT OAuth2 token info
   *
   * @return Token information
   */
  @GetMapping("/tokens/info")
  @Operation(summary = "Get SWIFT OAuth2 token info", description = "Get SWIFT OAuth2 token information")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token information retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getTokenInfo() {
    log.info("Getting SWIFT OAuth2 token info");
    SwiftOAuth2TokenService.TokenInfo tokenInfo = swiftOAuth2TokenService.getTokenInfo();

    if (tokenInfo != null) {
      return ResponseEntity.ok(
          Map.of(
              "tokenType", tokenInfo.getTokenType(),
              "expiresIn", tokenInfo.getExpiresIn(),
              "expiresAt", tokenInfo.getExpiresAt(),
              "isValid", swiftOAuth2TokenService.isTokenValid(),
              "timestamp", System.currentTimeMillis()));
    } else {
      return ResponseEntity.ok(
          Map.of(
              "message",
              "No SWIFT token information available",
              "isValid",
              false,
              "timestamp",
              System.currentTimeMillis()));
    }
  }

  /**
   * Get SWIFT cache health
   *
   * @return Cache health status
   */
  @GetMapping("/health")
  @Operation(summary = "Get SWIFT cache health", description = "Get SWIFT cache health status")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache health retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getCacheHealth() {
    log.info("Getting SWIFT cache health");
    boolean isHealthy = swiftCacheService.isCacheHealthy();

    return ResponseEntity.ok(
        Map.of(
            "healthy", isHealthy,
            "timestamp", System.currentTimeMillis(),
            "service", "SWIFT Cache"));
  }
}
