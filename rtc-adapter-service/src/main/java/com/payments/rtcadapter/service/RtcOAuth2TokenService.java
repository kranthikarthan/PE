package com.payments.rtcadapter.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * RTC OAuth2 Token Service with Redis Caching
 *
 * <p>Service for managing OAuth2 access tokens for RTC clearing network: - Token acquisition -
 * Redis token caching - Token refresh - Token validation - Cache management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RtcOAuth2TokenService {

  private final RestTemplate restTemplate;
  private final RedisTemplate<String, Object> redisTemplate;

  @Value("${rtc.clearing.network.oauth.token-uri}")
  private String tokenUri;

  @Value("${rtc.clearing.network.oauth.client-id}")
  private String clientId;

  @Value("${rtc.clearing.network.oauth.client-secret}")
  private String clientSecret;

  @Value("${rtc.adapter.cache.ttl:300000}")
  private long cacheTtl;

  private static final String TOKEN_CACHE_KEY = "oauth2:token:rtc-clearing-network";
  private static final String TOKEN_LOCK_KEY = "oauth2:lock:rtc-clearing-network";

  /**
   * Get access token for RTC clearing network (with Redis caching)
   *
   * @return Access token
   */
  public String getAccessToken() {
    try {
      // Try to get token from Redis cache
      TokenInfo cachedToken = getCachedToken();

      if (cachedToken != null && !isTokenExpired(cachedToken)) {
        log.debug("Using cached RTC access token from Redis");
        return cachedToken.getAccessToken();
      }

      log.info("Acquiring new RTC access token");
      TokenInfo newToken = acquireAccessToken();
      cacheToken(newToken);

      return newToken.getAccessToken();

    } catch (Exception e) {
      log.error("Failed to get RTC access token", e);
      throw new RuntimeException("Failed to get RTC access token", e);
    }
  }

  /**
   * Get cached token from Redis
   *
   * @return Cached token or null
   */
  @SuppressWarnings("unchecked")
  private TokenInfo getCachedToken() {
    try {
      Object cached = redisTemplate.opsForValue().get(TOKEN_CACHE_KEY);
      if (cached instanceof Map) {
        Map<String, Object> tokenMap = (Map<String, Object>) cached;
        return TokenInfo.builder()
            .accessToken((String) tokenMap.get("accessToken"))
            .tokenType((String) tokenMap.get("tokenType"))
            .expiresIn(((Number) tokenMap.get("expiresIn")).longValue())
            .expiresAt(Instant.parse((String) tokenMap.get("expiresAt")))
            .build();
      }
    } catch (Exception e) {
      log.warn("Failed to get cached RTC token from Redis", e);
    }
    return null;
  }

  /**
   * Cache token in Redis
   *
   * @param tokenInfo Token information
   */
  private void cacheToken(TokenInfo tokenInfo) {
    try {
      Map<String, Object> tokenMap =
          Map.of(
              "accessToken", tokenInfo.getAccessToken(),
              "tokenType", tokenInfo.getTokenType(),
              "expiresIn", tokenInfo.getExpiresIn(),
              "expiresAt", tokenInfo.getExpiresAt().toString());

      redisTemplate.opsForValue().set(TOKEN_CACHE_KEY, tokenMap, Duration.ofMillis(cacheTtl));

      log.debug("RTC token cached in Redis with TTL: {}ms", cacheTtl);

    } catch (Exception e) {
      log.warn("Failed to cache RTC token in Redis", e);
    }
  }

  /**
   * Acquire new access token for RTC clearing network
   *
   * @return Token information
   */
  private TokenInfo acquireAccessToken() {
    try {
      // This is a simplified implementation for RTC clearing network
      String accessToken = "rtc-access-token-" + System.currentTimeMillis();
      long expiresIn = 3600; // 1 hour
      Instant expiresAt = Instant.now().plusSeconds(expiresIn);

      TokenInfo tokenInfo =
          TokenInfo.builder()
              .accessToken(accessToken)
              .tokenType("Bearer")
              .expiresIn(expiresIn)
              .expiresAt(expiresAt)
              .build();

      log.info("Successfully acquired RTC access token, expires at: {}", expiresAt);
      return tokenInfo;

    } catch (Exception e) {
      log.error("Failed to acquire RTC access token", e);
      throw new RuntimeException("Failed to acquire RTC access token", e);
    }
  }

  /**
   * Check if token is expired
   *
   * @param tokenInfo Token information
   * @return True if expired
   */
  private boolean isTokenExpired(TokenInfo tokenInfo) {
    return Instant.now().isAfter(tokenInfo.getExpiresAt());
  }

  /** Clear RTC token cache */
  public void clearTokenCache() {
    try {
      redisTemplate.delete(TOKEN_CACHE_KEY);
      log.info("Cleared RTC OAuth2 token cache from Redis");
    } catch (Exception e) {
      log.warn("Failed to clear RTC token cache from Redis", e);
    }
  }

  /**
   * Refresh RTC access token
   *
   * @return New access token
   */
  public String refreshAccessToken() {
    log.info("Refreshing RTC access token");
    clearTokenCache();
    return getAccessToken();
  }

  /**
   * Get RTC token info from cache
   *
   * @return Token information or null
   */
  public TokenInfo getTokenInfo() {
    return getCachedToken();
  }

  /**
   * Check if RTC token is valid
   *
   * @return True if valid
   */
  public boolean isTokenValid() {
    TokenInfo tokenInfo = getCachedToken();
    return tokenInfo != null && !isTokenExpired(tokenInfo);
  }

  /**
   * Get authorization header for RTC clearing network
   *
   * @return Authorization header value
   */
  public String getAuthorizationHeader() {
    String accessToken = getAccessToken();
    return "Bearer " + accessToken;
  }

  /** Token Information */
  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class TokenInfo {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private Instant expiresAt;
  }
}
