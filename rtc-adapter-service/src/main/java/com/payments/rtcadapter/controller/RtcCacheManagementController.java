package com.payments.rtcadapter.controller;

import com.payments.rtcadapter.service.RtcCacheService;
import com.payments.rtcadapter.service.RtcOAuth2TokenService;
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
 * RTC Cache Management Controller
 *
 * <p>REST controller for RTC cache management operations: - Cache statistics - Cache clearing - Token management - Cache health
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rtc/cache")
@RequiredArgsConstructor
@Tag(name = "RTC Cache Management", description = "RTC cache management operations")
public class RtcCacheManagementController {

  private final RtcCacheService rtcCacheService;
  private final RtcOAuth2TokenService rtcOAuth2TokenService;

  /**
   * Get RTC cache statistics
   *
   * @return Cache statistics
   */
  @GetMapping("/statistics")
  @Operation(
      summary = "Get RTC cache statistics",
      description = "Retrieve RTC cache statistics and metrics")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<RtcCacheService.CacheStatistics> getCacheStatistics() {
    log.info("Getting RTC cache statistics");
    RtcCacheService.CacheStatistics statistics = rtcCacheService.getCacheStatistics();
    return ResponseEntity.ok(statistics);
  }

  /**
   * Clear RTC adapter cache
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @return Success response
   */
  @DeleteMapping("/adapters/{adapterId}")
  @Operation(summary = "Clear RTC adapter cache", description = "Clear cache for specific RTC adapter")
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

    log.info("Clearing cache for RTC adapter: {} and tenant: {}", adapterId, tenantId);
    rtcCacheService.clearAdapterCache(adapterId, tenantId);

    return ResponseEntity.ok(
        Map.of(
            "message",
            "RTC adapter cache cleared successfully",
            "adapterId",
            adapterId,
            "tenantId",
            tenantId,
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear RTC tenant cache
   *
   * @param tenantId Tenant ID
   * @return Success response
   */
  @DeleteMapping("/tenants/{tenantId}")
  @Operation(summary = "Clear RTC tenant cache", description = "Clear cache for specific tenant")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tenant cache cleared successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearTenantCache(
      @Parameter(description = "Tenant ID", required = true) @PathVariable String tenantId) {

    log.info("Clearing cache for RTC tenant: {}", tenantId);
    rtcCacheService.clearTenantCache(tenantId);

    return ResponseEntity.ok(
        Map.of(
            "message",
            "RTC tenant cache cleared successfully",
            "tenantId",
            tenantId,
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear all RTC cache
   *
   * @return Success response
   */
  @DeleteMapping("/all")
  @Operation(summary = "Clear all RTC cache", description = "Clear all RTC cache entries")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "All cache cleared successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearAllCache() {
    log.info("Clearing all RTC cache");
    rtcCacheService.clearAllCache();

    return ResponseEntity.ok(
        Map.of(
            "message", "All RTC cache cleared successfully", "timestamp", System.currentTimeMillis()));
  }

  /**
   * Clear RTC OAuth2 token cache
   *
   * @return Success response
   */
  @DeleteMapping("/tokens")
  @Operation(summary = "Clear RTC OAuth2 token cache", description = "Clear RTC OAuth2 token cache")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 token cache cleared successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearTokenCache() {
    log.info("Clearing RTC OAuth2 token cache");
    rtcOAuth2TokenService.clearTokenCache();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "RTC OAuth2 token cache cleared successfully",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Refresh RTC OAuth2 token
   *
   * @return Success response
   */
  @PostMapping("/tokens/refresh")
  @Operation(summary = "Refresh RTC OAuth2 token", description = "Refresh RTC OAuth2 access token")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 token refreshed successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> refreshToken() {
    log.info("Refreshing RTC OAuth2 token");
    String newToken = rtcOAuth2TokenService.refreshAccessToken();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "RTC OAuth2 token refreshed successfully",
            "token",
            newToken.substring(0, Math.min(20, newToken.length())) + "...",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Get RTC OAuth2 token info
   *
   * @return Token information
   */
  @GetMapping("/tokens/info")
  @Operation(summary = "Get RTC OAuth2 token info", description = "Get RTC OAuth2 token information")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token information retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getTokenInfo() {
    log.info("Getting RTC OAuth2 token info");
    RtcOAuth2TokenService.TokenInfo tokenInfo = rtcOAuth2TokenService.getTokenInfo();

    if (tokenInfo != null) {
      return ResponseEntity.ok(
          Map.of(
              "tokenType", tokenInfo.getTokenType(),
              "expiresIn", tokenInfo.getExpiresIn(),
              "expiresAt", tokenInfo.getExpiresAt(),
              "isValid", rtcOAuth2TokenService.isTokenValid(),
              "timestamp", System.currentTimeMillis()));
    } else {
      return ResponseEntity.ok(
          Map.of(
              "message",
              "No RTC token information available",
              "isValid",
              false,
              "timestamp",
              System.currentTimeMillis()));
    }
  }

  /**
   * Get RTC cache health
   *
   * @return Cache health status
   */
  @GetMapping("/health")
  @Operation(summary = "Get RTC cache health", description = "Get RTC cache health status")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache health retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getCacheHealth() {
    log.info("Getting RTC cache health");
    boolean isHealthy = rtcCacheService.isCacheHealthy();

    return ResponseEntity.ok(
        Map.of(
            "healthy", isHealthy,
            "timestamp", System.currentTimeMillis(),
            "service", "RTC Cache"));
  }
}
