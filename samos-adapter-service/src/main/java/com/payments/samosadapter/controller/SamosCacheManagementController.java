package com.payments.samosadapter.controller;

import com.payments.samosadapter.service.SamosCacheService;
import com.payments.samosadapter.service.SamosOAuth2TokenService;
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
 * SAMOS Cache Management Controller
 *
 * <p>REST controller for SAMOS cache management operations: - Cache statistics - Cache clearing -
 * Token management - Cache health
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/samos/cache")
@RequiredArgsConstructor
@Tag(name = "SAMOS Cache Management", description = "SAMOS cache management operations")
public class SamosCacheManagementController {

  private final SamosCacheService samosCacheService;
  private final SamosOAuth2TokenService samosOAuth2TokenService;

  /**
   * Get SAMOS cache statistics
   *
   * @return Cache statistics
   */
  @GetMapping("/statistics")
  @Operation(
      summary = "Get SAMOS cache statistics",
      description = "Retrieve SAMOS cache statistics and metrics")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<SamosCacheService.CacheStatistics> getCacheStatistics() {
    log.info("Getting SAMOS cache statistics");
    SamosCacheService.CacheStatistics statistics = samosCacheService.getCacheStatistics();
    return ResponseEntity.ok(statistics);
  }

  /**
   * Clear SAMOS adapter cache
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @return Success response
   */
  @DeleteMapping("/adapters/{adapterId}")
  @Operation(
      summary = "Clear SAMOS adapter cache",
      description = "Clear cache for specific SAMOS adapter")
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

    log.info("Clearing cache for SAMOS adapter: {} and tenant: {}", adapterId, tenantId);
    samosCacheService.clearAdapterCache(adapterId, tenantId);

    return ResponseEntity.ok(
        Map.of(
            "message",
            "SAMOS adapter cache cleared successfully",
            "adapterId",
            adapterId,
            "tenantId",
            tenantId,
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear SAMOS tenant cache
   *
   * @param tenantId Tenant ID
   * @return Success response
   */
  @DeleteMapping("/tenants/{tenantId}")
  @Operation(summary = "Clear SAMOS tenant cache", description = "Clear cache for specific tenant")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tenant cache cleared successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearTenantCache(
      @Parameter(description = "Tenant ID", required = true) @PathVariable String tenantId) {

    log.info("Clearing cache for SAMOS tenant: {}", tenantId);
    samosCacheService.clearTenantCache(tenantId);

    return ResponseEntity.ok(
        Map.of(
            "message",
            "SAMOS tenant cache cleared successfully",
            "tenantId",
            tenantId,
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear all SAMOS cache
   *
   * @return Success response
   */
  @DeleteMapping("/all")
  @Operation(summary = "Clear all SAMOS cache", description = "Clear all SAMOS cache entries")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "All cache cleared successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearAllCache() {
    log.info("Clearing all SAMOS cache");
    samosCacheService.clearAllCache();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "All SAMOS cache cleared successfully",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear SAMOS OAuth2 token cache
   *
   * @return Success response
   */
  @DeleteMapping("/tokens")
  @Operation(
      summary = "Clear SAMOS OAuth2 token cache",
      description = "Clear SAMOS OAuth2 token cache")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 token cache cleared successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearTokenCache() {
    log.info("Clearing SAMOS OAuth2 token cache");
    samosOAuth2TokenService.clearTokenCache();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "SAMOS OAuth2 token cache cleared successfully",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Refresh SAMOS OAuth2 token
   *
   * @return Success response
   */
  @PostMapping("/tokens/refresh")
  @Operation(
      summary = "Refresh SAMOS OAuth2 token",
      description = "Refresh SAMOS OAuth2 access token")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 token refreshed successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> refreshToken() {
    log.info("Refreshing SAMOS OAuth2 token");
    String newToken = samosOAuth2TokenService.refreshAccessToken();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "SAMOS OAuth2 token refreshed successfully",
            "token",
            newToken.substring(0, Math.min(20, newToken.length())) + "...",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Get SAMOS OAuth2 token info
   *
   * @return Token information
   */
  @GetMapping("/tokens/info")
  @Operation(
      summary = "Get SAMOS OAuth2 token info",
      description = "Get SAMOS OAuth2 token information")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token information retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getTokenInfo() {
    log.info("Getting SAMOS OAuth2 token info");
    SamosOAuth2TokenService.TokenInfo tokenInfo = samosOAuth2TokenService.getTokenInfo();

    if (tokenInfo != null) {
      return ResponseEntity.ok(
          Map.of(
              "tokenType", tokenInfo.getTokenType(),
              "expiresIn", tokenInfo.getExpiresIn(),
              "expiresAt", tokenInfo.getExpiresAt(),
              "isValid", samosOAuth2TokenService.isTokenValid(),
              "timestamp", System.currentTimeMillis()));
    } else {
      return ResponseEntity.ok(
          Map.of(
              "message",
              "No SAMOS token information available",
              "isValid",
              false,
              "timestamp",
              System.currentTimeMillis()));
    }
  }

  /**
   * Get SAMOS cache health
   *
   * @return Cache health status
   */
  @GetMapping("/health")
  @Operation(summary = "Get SAMOS cache health", description = "Get SAMOS cache health status")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache health retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getCacheHealth() {
    log.info("Getting SAMOS cache health");
    boolean isHealthy = samosCacheService.isCacheHealthy();

    return ResponseEntity.ok(
        Map.of(
            "healthy",
            isHealthy,
            "timestamp",
            System.currentTimeMillis(),
            "service",
            "SAMOS Cache"));
  }
}
