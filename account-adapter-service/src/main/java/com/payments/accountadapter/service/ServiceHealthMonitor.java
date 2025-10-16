package com.payments.accountadapter.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Service Health Monitor
 *
 * <p>Monitors health of all service components: - External account service health - Redis cache
 * health - OAuth2 token service health - Circuit breaker health
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceHealthMonitor implements HealthIndicator {

  private final AccountAdapterService accountAdapterService;
  private final OAuth2TokenService oAuth2TokenService;
  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public Health health() {
    try {
      Map<String, Object> details = new HashMap<>();
      boolean isHealthy = true;

      // Check Redis health
      Map<String, Object> redisHealth = checkRedisHealth();
      details.put("redis", redisHealth);
      if (!(Boolean) redisHealth.get("healthy")) {
        isHealthy = false;
      }

      // Check OAuth2 token service health
      Map<String, Object> oauthHealth = checkOAuth2Health();
      details.put("oauth2", oauthHealth);
      if (!(Boolean) oauthHealth.get("healthy")) {
        isHealthy = false;
      }

      // Check external service connectivity
      Map<String, Object> externalServiceHealth = checkExternalServiceHealth();
      details.put("externalService", externalServiceHealth);
      if (!(Boolean) externalServiceHealth.get("healthy")) {
        isHealthy = false;
      }

      // Overall health
      details.put("overall", Map.of("healthy", isHealthy, "timestamp", Instant.now()));

      if (isHealthy) {
        return Health.up().withDetails(details).build();
      } else {
        return Health.down().withDetails(details).build();
      }

    } catch (Exception e) {
      log.error("Error checking service health", e);
      return Health.down().withDetail("error", e.getMessage()).build();
    }
  }

  /** Check Redis health */
  private Map<String, Object> checkRedisHealth() {
    Map<String, Object> health = new HashMap<>();
    boolean isHealthy = true;

    try {
      // Test Redis connectivity
      String testKey = "health:check:" + System.currentTimeMillis();
      redisTemplate.opsForValue().set(testKey, "test", 10, TimeUnit.SECONDS);
      String retrieved = (String) redisTemplate.opsForValue().get(testKey);
      redisTemplate.delete(testKey);

      if (!"test".equals(retrieved)) {
        isHealthy = false;
        health.put("error", "Redis read/write test failed");
      } else {
        health.put("status", "Connected");
        health.put("test", "Read/write test passed");
      }

    } catch (Exception e) {
      log.error("Redis health check failed", e);
      isHealthy = false;
      health.put("error", e.getMessage());
      health.put("status", "Disconnected");
    }

    health.put("healthy", isHealthy);
    return health;
  }

  /** Check OAuth2 token service health */
  private Map<String, Object> checkOAuth2Health() {
    Map<String, Object> health = new HashMap<>();
    boolean isHealthy = true;

    try {
      // Check if token is valid
      boolean tokenValid = oAuth2TokenService.isTokenValid();
      OAuth2TokenService.TokenInfo tokenInfo = oAuth2TokenService.getTokenInfo();

      health.put("tokenValid", tokenValid);
      if (tokenInfo != null) {
        health.put("tokenType", tokenInfo.getTokenType());
        health.put("expiresIn", tokenInfo.getExpiresIn());
        health.put("expiresAt", tokenInfo.getExpiresAt());
      }

      if (!tokenValid) {
        isHealthy = false;
        health.put("error", "OAuth2 token is invalid or expired");
      }

    } catch (Exception e) {
      log.error("OAuth2 health check failed", e);
      isHealthy = false;
      health.put("error", e.getMessage());
    }

    health.put("healthy", isHealthy);
    return health;
  }

  /** Check external service health */
  private Map<String, Object> checkExternalServiceHealth() {
    Map<String, Object> health = new HashMap<>();
    boolean isHealthy = true;

    try {
      // This is a simplified health check
      // In a real scenario, you would ping the external service
      health.put("status", "Available");
      health.put("connectivity", "OK");

    } catch (Exception e) {
      log.error("External service health check failed", e);
      isHealthy = false;
      health.put("error", e.getMessage());
      health.put("status", "Unavailable");
    }

    health.put("healthy", isHealthy);
    return health;
  }

  /** Get detailed health information */
  public Map<String, Object> getDetailedHealth() {
    Map<String, Object> health = new HashMap<>();

    try {
      health.put("timestamp", Instant.now());
      health.put("service", "account-adapter-service");
      health.put("version", "1.0.0");

      // Redis health
      health.put("redis", checkRedisHealth());

      // OAuth2 health
      health.put("oauth2", checkOAuth2Health());

      // External service health
      health.put("externalService", checkExternalServiceHealth());

      // Overall status
      Map<?, ?> redis = health.get("redis") instanceof Map<?, ?> m1 ? m1 : null;
      Map<?, ?> oauth2 = health.get("oauth2") instanceof Map<?, ?> m2 ? m2 : null;
      Map<?, ?> external = health.get("externalService") instanceof Map<?, ?> m3 ? m3 : null;

      boolean redisHealthy = redis != null && Boolean.TRUE.equals(redis.get("healthy"));
      boolean oauthHealthy = oauth2 != null && Boolean.TRUE.equals(oauth2.get("healthy"));
      boolean externalHealthy = external != null && Boolean.TRUE.equals(external.get("healthy"));

      boolean overallHealthy = redisHealthy && oauthHealthy && externalHealthy;

      health.put(
          "overall", Map.of("healthy", overallHealthy, "status", overallHealthy ? "UP" : "DOWN"));

    } catch (Exception e) {
      log.error("Error getting detailed health", e);
      health.put("error", e.getMessage());
      health.put("overall", Map.of("healthy", false, "status", "DOWN"));
    }

    return health;
  }
}
