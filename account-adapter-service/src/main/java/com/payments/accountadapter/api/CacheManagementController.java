package com.payments.accountadapter.api;

import com.payments.accountadapter.service.AccountCacheService;
import com.payments.accountadapter.service.OAuth2TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Cache Management Controller
 * 
 * REST controller for cache management operations:
 * - Cache statistics
 * - Cache clearing
 * - Token management
 * - Cache health
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cache")
@RequiredArgsConstructor
@Tag(name = "Cache Management", description = "Cache management operations")
public class CacheManagementController {

    private final AccountCacheService accountCacheService;
    private final OAuth2TokenService oAuth2TokenService;

    /**
     * Get cache statistics
     * 
     * @return Cache statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get cache statistics", description = "Retrieve cache statistics and metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AccountCacheService.CacheStatistics> getCacheStatistics() {
        log.info("Getting cache statistics");
        AccountCacheService.CacheStatistics statistics = accountCacheService.getCacheStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Clear account cache
     * 
     * @param accountNumber Account number
     * @param tenantId Tenant ID
     * @return Success response
     */
    @DeleteMapping("/accounts/{accountNumber}")
    @Operation(summary = "Clear account cache", description = "Clear cache for specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account cache cleared successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> clearAccountCache(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber,
            
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-ID") String tenantId) {
        
        log.info("Clearing cache for account: {} and tenant: {}", accountNumber, tenantId);
        accountCacheService.clearAccountCache(accountNumber, tenantId);
        
        return ResponseEntity.ok(Map.of(
                "message", "Account cache cleared successfully",
                "accountNumber", accountNumber,
                "tenantId", tenantId,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Clear tenant cache
     * 
     * @param tenantId Tenant ID
     * @return Success response
     */
    @DeleteMapping("/tenants/{tenantId}")
    @Operation(summary = "Clear tenant cache", description = "Clear cache for specific tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tenant cache cleared successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> clearTenantCache(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable String tenantId) {
        
        log.info("Clearing cache for tenant: {}", tenantId);
        accountCacheService.clearTenantCache(tenantId);
        
        return ResponseEntity.ok(Map.of(
                "message", "Tenant cache cleared successfully",
                "tenantId", tenantId,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Clear all cache
     * 
     * @return Success response
     */
    @DeleteMapping("/all")
    @Operation(summary = "Clear all cache", description = "Clear all cache entries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All cache cleared successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> clearAllCache() {
        log.info("Clearing all cache");
        accountCacheService.clearAllCache();
        
        return ResponseEntity.ok(Map.of(
                "message", "All cache cleared successfully",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Clear OAuth2 token cache
     * 
     * @return Success response
     */
    @DeleteMapping("/tokens")
    @Operation(summary = "Clear OAuth2 token cache", description = "Clear OAuth2 token cache")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OAuth2 token cache cleared successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> clearTokenCache() {
        log.info("Clearing OAuth2 token cache");
        oAuth2TokenService.clearTokenCache();
        
        return ResponseEntity.ok(Map.of(
                "message", "OAuth2 token cache cleared successfully",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Refresh OAuth2 token
     * 
     * @return Success response
     */
    @PostMapping("/tokens/refresh")
    @Operation(summary = "Refresh OAuth2 token", description = "Refresh OAuth2 access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OAuth2 token refreshed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> refreshToken() {
        log.info("Refreshing OAuth2 token");
        String newToken = oAuth2TokenService.refreshAccessToken();
        
        return ResponseEntity.ok(Map.of(
                "message", "OAuth2 token refreshed successfully",
                "token", newToken.substring(0, Math.min(20, newToken.length())) + "...",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Get OAuth2 token info
     * 
     * @return Token information
     */
    @GetMapping("/tokens/info")
    @Operation(summary = "Get OAuth2 token info", description = "Get OAuth2 token information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token information retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getTokenInfo() {
        log.info("Getting OAuth2 token info");
        OAuth2TokenService.TokenInfo tokenInfo = oAuth2TokenService.getTokenInfo();
        
        if (tokenInfo != null) {
            return ResponseEntity.ok(Map.of(
                    "tokenType", tokenInfo.getTokenType(),
                    "expiresIn", tokenInfo.getExpiresIn(),
                    "expiresAt", tokenInfo.getExpiresAt(),
                    "isValid", oAuth2TokenService.isTokenValid(),
                    "timestamp", System.currentTimeMillis()
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "No token information available",
                    "isValid", false,
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }
}
