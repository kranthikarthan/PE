package com.payments.samosadapter.service;

import com.payments.samosadapter.domain.SamosAdapter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * SAMOS Cache Service
 *
 * <p>Service for caching SAMOS adapter data in Redis: - Adapter configuration caching - Adapter
 * status caching - Route caching - Message log caching - Cache management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamosCacheService {

  private final RedisTemplate<String, Object> redisTemplate;

  @Value("${samos.adapter.cache.ttl:300000}")
  private long cacheTtl;

  private static final String ADAPTER_CACHE_PREFIX = "samos:adapter:";
  private static final String ADAPTER_STATUS_CACHE_PREFIX = "samos:status:";
  private static final String ROUTES_CACHE_PREFIX = "samos:routes:";
  private static final String MESSAGES_CACHE_PREFIX = "samos:messages:";
  private static final String STATS_CACHE_PREFIX = "samos:stats:";

  /**
   * Cache SAMOS adapter
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @param adapter Adapter object
   */
  public void cacheAdapter(String adapterId, String tenantId, SamosAdapter adapter) {
    try {
      String cacheKey = ADAPTER_CACHE_PREFIX + tenantId + ":" + adapterId;
      redisTemplate.opsForValue().set(cacheKey, adapter, Duration.ofMillis(cacheTtl));
      log.debug("Cached SAMOS adapter: {} with TTL: {}ms", adapterId, cacheTtl);
    } catch (Exception e) {
      log.warn("Failed to cache SAMOS adapter: {}", adapterId, e);
    }
  }

  /**
   * Get cached SAMOS adapter
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @return Cached adapter or null
   */
  public SamosAdapter getCachedAdapter(String adapterId, String tenantId) {
    try {
      String cacheKey = ADAPTER_CACHE_PREFIX + tenantId + ":" + adapterId;
      Object cached = redisTemplate.opsForValue().get(cacheKey);
      if (cached instanceof SamosAdapter) {
        log.debug("Retrieved cached SAMOS adapter: {}", adapterId);
        return (SamosAdapter) cached;
      }
    } catch (Exception e) {
      log.warn("Failed to get cached SAMOS adapter: {}", adapterId, e);
    }
    return null;
  }

  /**
   * Cache adapter status
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @param status Status
   */
  public void cacheAdapterStatus(String adapterId, String tenantId, String status) {
    try {
      String cacheKey = ADAPTER_STATUS_CACHE_PREFIX + tenantId + ":" + adapterId;
      redisTemplate.opsForValue().set(cacheKey, status, Duration.ofMillis(cacheTtl));
      log.debug("Cached SAMOS adapter status: {} for adapter: {}", status, adapterId);
    } catch (Exception e) {
      log.warn("Failed to cache SAMOS adapter status: {}", adapterId, e);
    }
  }

  /**
   * Get cached adapter status
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @return Cached status or null
   */
  public String getCachedAdapterStatus(String adapterId, String tenantId) {
    try {
      String cacheKey = ADAPTER_STATUS_CACHE_PREFIX + tenantId + ":" + adapterId;
      Object cached = redisTemplate.opsForValue().get(cacheKey);
      if (cached instanceof String) {
        log.debug("Retrieved cached SAMOS adapter status: {}", adapterId);
        return (String) cached;
      }
    } catch (Exception e) {
      log.warn("Failed to get cached SAMOS adapter status: {}", adapterId, e);
    }
    return null;
  }

  /**
   * Cache adapter routes
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @param routes Routes list
   */
  public void cacheAdapterRoutes(String adapterId, String tenantId, List<?> routes) {
    try {
      String cacheKey = ROUTES_CACHE_PREFIX + tenantId + ":" + adapterId;
      redisTemplate.opsForValue().set(cacheKey, routes, Duration.ofMillis(cacheTtl));
      log.debug("Cached SAMOS adapter routes for adapter: {}", adapterId);
    } catch (Exception e) {
      log.warn("Failed to cache SAMOS adapter routes: {}", adapterId, e);
    }
  }

  /**
   * Get cached adapter routes
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @return Cached routes or null
   */
  @SuppressWarnings("unchecked")
  public List<?> getCachedAdapterRoutes(String adapterId, String tenantId) {
    try {
      String cacheKey = ROUTES_CACHE_PREFIX + tenantId + ":" + adapterId;
      Object cached = redisTemplate.opsForValue().get(cacheKey);
      if (cached instanceof List) {
        log.debug("Retrieved cached SAMOS adapter routes: {}", adapterId);
        return (List<?>) cached;
      }
    } catch (Exception e) {
      log.warn("Failed to get cached SAMOS adapter routes: {}", adapterId, e);
    }
    return null;
  }

  /**
   * Cache adapter message logs
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @param messageLogs Message logs list
   */
  public void cacheAdapterMessageLogs(String adapterId, String tenantId, List<?> messageLogs) {
    try {
      String cacheKey = MESSAGES_CACHE_PREFIX + tenantId + ":" + adapterId;
      redisTemplate.opsForValue().set(cacheKey, messageLogs, Duration.ofMillis(cacheTtl));
      log.debug("Cached SAMOS adapter message logs for adapter: {}", adapterId);
    } catch (Exception e) {
      log.warn("Failed to cache SAMOS adapter message logs: {}", adapterId, e);
    }
  }

  /**
   * Get cached adapter message logs
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   * @return Cached message logs or null
   */
  @SuppressWarnings("unchecked")
  public List<?> getCachedAdapterMessageLogs(String adapterId, String tenantId) {
    try {
      String cacheKey = MESSAGES_CACHE_PREFIX + tenantId + ":" + adapterId;
      Object cached = redisTemplate.opsForValue().get(cacheKey);
      if (cached instanceof List) {
        log.debug("Retrieved cached SAMOS adapter message logs: {}", adapterId);
        return (List<?>) cached;
      }
    } catch (Exception e) {
      log.warn("Failed to get cached SAMOS adapter message logs: {}", adapterId, e);
    }
    return null;
  }

  /**
   * Cache adapter statistics
   *
   * @param tenantId Tenant ID
   * @param stats Statistics map
   */
  public void cacheAdapterStats(String tenantId, Map<String, Object> stats) {
    try {
      String cacheKey = STATS_CACHE_PREFIX + tenantId;
      redisTemplate.opsForValue().set(cacheKey, stats, Duration.ofMillis(cacheTtl));
      log.debug("Cached SAMOS adapter statistics for tenant: {}", tenantId);
    } catch (Exception e) {
      log.warn("Failed to cache SAMOS adapter statistics for tenant: {}", tenantId, e);
    }
  }

  /**
   * Get cached adapter statistics
   *
   * @param tenantId Tenant ID
   * @return Cached statistics or null
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getCachedAdapterStats(String tenantId) {
    try {
      String cacheKey = STATS_CACHE_PREFIX + tenantId;
      Object cached = redisTemplate.opsForValue().get(cacheKey);
      if (cached instanceof Map) {
        log.debug("Retrieved cached SAMOS adapter statistics for tenant: {}", tenantId);
        return (Map<String, Object>) cached;
      }
    } catch (Exception e) {
      log.warn("Failed to get cached SAMOS adapter statistics for tenant: {}", tenantId, e);
    }
    return null;
  }

  /**
   * Clear adapter cache
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   */
  public void clearAdapterCache(String adapterId, String tenantId) {
    try {
      String adapterKey = ADAPTER_CACHE_PREFIX + tenantId + ":" + adapterId;
      String statusKey = ADAPTER_STATUS_CACHE_PREFIX + tenantId + ":" + adapterId;
      String routesKey = ROUTES_CACHE_PREFIX + tenantId + ":" + adapterId;
      String messagesKey = MESSAGES_CACHE_PREFIX + tenantId + ":" + adapterId;

      redisTemplate.delete(adapterKey);
      redisTemplate.delete(statusKey);
      redisTemplate.delete(routesKey);
      redisTemplate.delete(messagesKey);

      log.info("Cleared SAMOS adapter cache for adapter: {} and tenant: {}", adapterId, tenantId);
    } catch (Exception e) {
      log.warn("Failed to clear SAMOS adapter cache for adapter: {}", adapterId, e);
    }
  }

  /**
   * Clear all cache for tenant
   *
   * @param tenantId Tenant ID
   */
  public void clearTenantCache(String tenantId) {
    try {
      String adapterPattern = ADAPTER_CACHE_PREFIX + tenantId + ":*";
      String statusPattern = ADAPTER_STATUS_CACHE_PREFIX + tenantId + ":*";
      String routesPattern = ROUTES_CACHE_PREFIX + tenantId + ":*";
      String messagesPattern = MESSAGES_CACHE_PREFIX + tenantId + ":*";
      String statsPattern = STATS_CACHE_PREFIX + tenantId;

      redisTemplate.delete(redisTemplate.keys(adapterPattern));
      redisTemplate.delete(redisTemplate.keys(statusPattern));
      redisTemplate.delete(redisTemplate.keys(routesPattern));
      redisTemplate.delete(redisTemplate.keys(messagesPattern));
      redisTemplate.delete(statsPattern);

      log.info("Cleared all SAMOS cache for tenant: {}", tenantId);
    } catch (Exception e) {
      log.warn("Failed to clear SAMOS tenant cache for tenant: {}", tenantId, e);
    }
  }

  /** Clear all cache */
  public void clearAllCache() {
    try {
      redisTemplate.delete(redisTemplate.keys(ADAPTER_CACHE_PREFIX + "*"));
      redisTemplate.delete(redisTemplate.keys(ADAPTER_STATUS_CACHE_PREFIX + "*"));
      redisTemplate.delete(redisTemplate.keys(ROUTES_CACHE_PREFIX + "*"));
      redisTemplate.delete(redisTemplate.keys(MESSAGES_CACHE_PREFIX + "*"));
      redisTemplate.delete(redisTemplate.keys(STATS_CACHE_PREFIX + "*"));

      log.info("Cleared all SAMOS cache");
    } catch (Exception e) {
      log.warn("Failed to clear all SAMOS cache", e);
    }
  }

  /**
   * Get cache statistics
   *
   * @return Cache statistics
   */
  public CacheStatistics getCacheStatistics() {
    try {
      long adapterCount = redisTemplate.keys(ADAPTER_CACHE_PREFIX + "*").size();
      long statusCount = redisTemplate.keys(ADAPTER_STATUS_CACHE_PREFIX + "*").size();
      long routesCount = redisTemplate.keys(ROUTES_CACHE_PREFIX + "*").size();
      long messagesCount = redisTemplate.keys(MESSAGES_CACHE_PREFIX + "*").size();
      long statsCount = redisTemplate.keys(STATS_CACHE_PREFIX + "*").size();

      return CacheStatistics.builder()
          .adapterCacheCount(adapterCount)
          .statusCacheCount(statusCount)
          .routesCacheCount(routesCount)
          .messagesCacheCount(messagesCount)
          .statsCacheCount(statsCount)
          .totalCacheCount(adapterCount + statusCount + routesCount + messagesCount + statsCount)
          .cacheTtl(cacheTtl)
          .build();
    } catch (Exception e) {
      log.warn("Failed to get SAMOS cache statistics", e);
      return CacheStatistics.builder()
          .adapterCacheCount(0L)
          .statusCacheCount(0L)
          .routesCacheCount(0L)
          .messagesCacheCount(0L)
          .statsCacheCount(0L)
          .totalCacheCount(0L)
          .cacheTtl(cacheTtl)
          .build();
    }
  }

  /**
   * Check if cache is healthy
   *
   * @return True if cache is healthy
   */
  public boolean isCacheHealthy() {
    try {
      // Simple health check - ping Redis
      String pingResult = redisTemplate.getConnectionFactory().getConnection().ping();
      return "PONG".equals(pingResult);
    } catch (Exception e) {
      log.warn("SAMOS cache health check failed", e);
      return false;
    }
  }

  /** Cache Statistics */
  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class CacheStatistics {
    private Long adapterCacheCount;
    private Long statusCacheCount;
    private Long routesCacheCount;
    private Long messagesCacheCount;
    private Long statsCacheCount;
    private Long totalCacheCount;
    private Long cacheTtl;
  }
}
