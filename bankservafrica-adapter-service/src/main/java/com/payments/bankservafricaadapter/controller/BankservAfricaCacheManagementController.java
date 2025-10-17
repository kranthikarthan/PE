package com.payments.bankservafricaadapter.controller;

import com.payments.bankservafricaadapter.service.BankservAfricaCacheService;
import com.payments.bankservafricaadapter.service.BankservAfricaOAuth2TokenService;
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
 * BankservAfrica Cache Management Controller
 *
 * <p>REST controller for BankservAfrica cache management operations: - Cache statistics - Cache
 * clearing - Token management - Cache health
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/bankservafrica/cache")
@RequiredArgsConstructor
@Tag(
    name = "BankservAfrica Cache Management",
    description = "BankservAfrica cache management operations")
public class BankservAfricaCacheManagementController {

  private final BankservAfricaCacheService bankservAfricaCacheService;
  private final BankservAfricaOAuth2TokenService bankservAfricaOAuth2TokenService;

  /**
   * Get BankservAfrica cache statistics
   *
   * @return Cache statistics
   */
  @GetMapping("/statistics")
  @Operation(
      summary = "Get BankservAfrica cache statistics",
      description = "Retrieve BankservAfrica cache statistics and metrics")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<BankservAfricaCacheService.CacheStatistics> getCacheStatistics() {
    log.info("Getting BankservAfrica cache statistics");
    BankservAfricaCacheService.CacheStatistics statistics =
        bankservAfricaCacheService.getCacheStatistics();
    return ResponseEntity.ok(statistics);
  }

  /**
   * Clear BankservAfrica adapter cache
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @return Success response
   */
  @DeleteMapping("/adapters/{adapterId}")
  @Operation(
      summary = "Clear BankservAfrica adapter cache",
      description = "Clear cache for specific BankservAfrica adapter")
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

    log.info("Clearing cache for BankservAfrica adapter: {} and tenant: {}", adapterId, tenantId);
    bankservAfricaCacheService.clearAdapterCache(adapterId, tenantId);

    return ResponseEntity.ok(
        Map.of(
            "message",
            "BankservAfrica adapter cache cleared successfully",
            "adapterId",
            adapterId,
            "tenantId",
            tenantId,
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear BankservAfrica tenant cache
   *
   * @param tenantId Tenant ID
   * @return Success response
   */
  @DeleteMapping("/tenants/{tenantId}")
  @Operation(
      summary = "Clear BankservAfrica tenant cache",
      description = "Clear cache for specific tenant")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tenant cache cleared successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearTenantCache(
      @Parameter(description = "Tenant ID", required = true) @PathVariable String tenantId) {

    log.info("Clearing cache for BankservAfrica tenant: {}", tenantId);
    bankservAfricaCacheService.clearTenantCache(tenantId);

    return ResponseEntity.ok(
        Map.of(
            "message",
            "BankservAfrica tenant cache cleared successfully",
            "tenantId",
            tenantId,
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear all BankservAfrica cache
   *
   * @return Success response
   */
  @DeleteMapping("/all")
  @Operation(
      summary = "Clear all BankservAfrica cache",
      description = "Clear all BankservAfrica cache entries")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "All cache cleared successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearAllCache() {
    log.info("Clearing all BankservAfrica cache");
    bankservAfricaCacheService.clearAllCache();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "All BankservAfrica cache cleared successfully",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Clear BankservAfrica OAuth2 token cache
   *
   * @return Success response
   */
  @DeleteMapping("/tokens")
  @Operation(
      summary = "Clear BankservAfrica OAuth2 token cache",
      description = "Clear BankservAfrica OAuth2 token cache")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 token cache cleared successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> clearTokenCache() {
    log.info("Clearing BankservAfrica OAuth2 token cache");
    bankservAfricaOAuth2TokenService.clearTokenCache();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "BankservAfrica OAuth2 token cache cleared successfully",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Refresh BankservAfrica OAuth2 token
   *
   * @return Success response
   */
  @PostMapping("/tokens/refresh")
  @Operation(
      summary = "Refresh BankservAfrica OAuth2 token",
      description = "Refresh BankservAfrica OAuth2 access token")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "OAuth2 token refreshed successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> refreshToken() {
    log.info("Refreshing BankservAfrica OAuth2 token");
    String newToken = bankservAfricaOAuth2TokenService.refreshAccessToken();

    return ResponseEntity.ok(
        Map.of(
            "message",
            "BankservAfrica OAuth2 token refreshed successfully",
            "token",
            newToken.substring(0, Math.min(20, newToken.length())) + "...",
            "timestamp",
            System.currentTimeMillis()));
  }

  /**
   * Get BankservAfrica OAuth2 token info
   *
   * @return Token information
   */
  @GetMapping("/tokens/info")
  @Operation(
      summary = "Get BankservAfrica OAuth2 token info",
      description = "Get BankservAfrica OAuth2 token information")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token information retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getTokenInfo() {
    log.info("Getting BankservAfrica OAuth2 token info");
    BankservAfricaOAuth2TokenService.TokenInfo tokenInfo =
        bankservAfricaOAuth2TokenService.getTokenInfo();

    if (tokenInfo != null) {
      return ResponseEntity.ok(
          Map.of(
              "tokenType", tokenInfo.getTokenType(),
              "expiresIn", tokenInfo.getExpiresIn(),
              "expiresAt", tokenInfo.getExpiresAt(),
              "isValid", bankservAfricaOAuth2TokenService.isTokenValid(),
              "timestamp", System.currentTimeMillis()));
    } else {
      return ResponseEntity.ok(
          Map.of(
              "message",
              "No BankservAfrica token information available",
              "isValid",
              false,
              "timestamp",
              System.currentTimeMillis()));
    }
  }

  /**
   * Get BankservAfrica cache health
   *
   * @return Cache health status
   */
  @GetMapping("/health")
  @Operation(
      summary = "Get BankservAfrica cache health",
      description = "Get BankservAfrica cache health status")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cache health retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Object>> getCacheHealth() {
    log.info("Getting BankservAfrica cache health");
    boolean isHealthy = bankservAfricaCacheService.isCacheHealthy();

    return ResponseEntity.ok(
        Map.of(
            "healthy",
            isHealthy,
            "timestamp",
            System.currentTimeMillis(),
            "service",
            "BankservAfrica Cache"));
  }
}
