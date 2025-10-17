package com.payments.bankservafricaadapter.service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * BankservAfrica Cache Service
 *
 * <p>Service for managing BankservAfrica adapter cache: - Cache statistics - Cache clearing - Cache
 * health - Tenant cache management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankservAfricaCacheService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final AtomicLong cacheHits = new AtomicLong(0);
  private final AtomicLong cacheMisses = new AtomicLong(0);

  private static final String CACHE_PREFIX = "bankservafrica:";
  private static final String ADAPTER_CACHE_PREFIX = CACHE_PREFIX + "adapter:";
  private static final String TENANT_CACHE_PREFIX = CACHE_PREFIX + "tenant:";

  /**
   * Get cache statistics
   *
   * @return Cache statistics
   */
  public CacheStatistics getCacheStatistics() {
    long hits = cacheHits.get();
    long misses = cacheMisses.get();
    long total = hits + misses;
    double hitRate = total > 0 ? (double) hits / total : 0.0;

    return CacheStatistics.builder()
        .cacheHits(hits)
        .cacheMisses(misses)
        .cacheHitRate(hitRate)
        .timestamp(Instant.now().toString())
        .build();
  }

  /**
   * Clear adapter cache
   *
   * @param adapterId Adapter ID
   * @param tenantId Tenant ID
   */
  public void clearAdapterCache(String adapterId, String tenantId) {
    String cacheKey = ADAPTER_CACHE_PREFIX + tenantId + ":" + adapterId;
    redisTemplate.delete(cacheKey);
    log.info("Cleared BankservAfrica adapter cache: {}", cacheKey);
  }

  /**
   * Clear tenant cache
   *
   * @param tenantId Tenant ID
   */
  public void clearTenantCache(String tenantId) {
    String pattern = TENANT_CACHE_PREFIX + tenantId + ":*";
    var keys = redisTemplate.keys(pattern);
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
      log.info("Cleared BankservAfrica tenant cache: {} - Count: {}", tenantId, keys.size());
    }
  }

  /** Clear all cache */
  public void clearAllCache() {
    var keys = redisTemplate.keys(CACHE_PREFIX + "*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
      log.info("Cleared all BankservAfrica cache - Count: {}", keys.size());
    }
  }

  /**
   * Check cache health
   *
   * @return True if cache is healthy
   */
  public boolean isCacheHealthy() {
    try {
      redisTemplate.hasKey("health-check");
      return true;
    } catch (Exception e) {
      log.error("Cache health check failed", e);
      return false;
    }
  }

  /** Record cache hit */
  public void recordCacheHit() {
    cacheHits.incrementAndGet();
  }

  /** Record cache miss */
  public void recordCacheMiss() {
    cacheMisses.incrementAndGet();
  }

  /** Cache statistics DTO */
  @Data
  @Builder
  public static class CacheStatistics {
    private long cacheHits;
    private long cacheMisses;
    private double cacheHitRate;
    private String timestamp;
  }
}
