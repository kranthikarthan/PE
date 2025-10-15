package com.payments.routing.service;

import com.payments.routing.engine.RoutingDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Routing Cache Service
 * 
 * Redis-based caching for routing decisions:
 * - Cache routing decisions by payment attributes
 * - TTL-based expiration
 * - Cache statistics and monitoring
 * 
 * Performance: Redis with optimized data structures
 * Resilience: Graceful degradation on cache failures
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingCacheService {

    private final RedisTemplate<String, RoutingDecision> redisTemplate;
    
    // Cache configuration
    private static final String CACHE_PREFIX = "routing:decision:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    
    // Local cache for fallback when Redis is unavailable
    private final ConcurrentHashMap<String, RoutingDecision> localCache = new ConcurrentHashMap<>();
    private final AtomicLong localCacheSize = new AtomicLong(0);
    private final AtomicLong maxLocalCacheSize = new AtomicLong(1000);

    /**
     * Get routing decision from cache
     * 
     * @param key Cache key
     * @return Routing decision or null if not found
     */
    public RoutingDecision get(String key) {
        try {
            // Try Redis first
            String redisKey = CACHE_PREFIX + key;
            RoutingDecision decision = redisTemplate.opsForValue().get(redisKey);
            
            if (decision != null) {
                log.debug("Cache hit in Redis for key: {}", key);
                return decision;
            }
            
            // Fallback to local cache
            decision = localCache.get(key);
            if (decision != null) {
                log.debug("Cache hit in local cache for key: {}", key);
                return decision;
            }
            
            log.debug("Cache miss for key: {}", key);
            return null;
            
        } catch (Exception e) {
            log.warn("Error getting from Redis cache, trying local cache: {}", e.getMessage());
            
            // Fallback to local cache
            RoutingDecision decision = localCache.get(key);
            if (decision != null) {
                log.debug("Cache hit in local cache (fallback) for key: {}", key);
                return decision;
            }
            
            return null;
        }
    }

    /**
     * Put routing decision in cache
     * 
     * @param key Cache key
     * @param decision Routing decision
     */
    public void put(String key, RoutingDecision decision) {
        try {
            // Store in Redis
            String redisKey = CACHE_PREFIX + key;
            redisTemplate.opsForValue().set(redisKey, decision, DEFAULT_TTL);
            log.debug("Cached routing decision in Redis for key: {}", key);
            
        } catch (Exception e) {
            log.warn("Error storing in Redis cache, using local cache: {}", e.getMessage());
        }
        
        // Always store in local cache as backup
        if (localCacheSize.get() < maxLocalCacheSize.get()) {
            localCache.put(key, decision);
            localCacheSize.incrementAndGet();
            log.debug("Cached routing decision in local cache for key: {}", key);
        }
    }

    /**
     * Evict cache entry by payment ID
     * 
     * @param paymentId Payment ID
     */
    public void evictByPaymentId(String paymentId) {
        try {
            // Evict from Redis
            String pattern = CACHE_PREFIX + "*:" + paymentId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Evicted {} entries from Redis cache for payment: {}", keys.size(), paymentId);
            }
            
        } catch (Exception e) {
            log.warn("Error evicting from Redis cache: {}", e.getMessage());
        }
        
        // Evict from local cache
        localCache.entrySet().removeIf(entry -> entry.getKey().contains(paymentId));
        log.debug("Evicted entries from local cache for payment: {}", paymentId);
    }

    /**
     * Clear all cache entries
     */
    public void clear() {
        try {
            // Clear Redis cache
            String pattern = CACHE_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Cleared {} entries from Redis cache", keys.size());
            }
            
        } catch (Exception e) {
            log.warn("Error clearing Redis cache: {}", e.getMessage());
        }
        
        // Clear local cache
        localCache.clear();
        localCacheSize.set(0);
        log.debug("Cleared local cache");
    }

    /**
     * Get cache size
     * 
     * @return Total cache size
     */
    public long size() {
        try {
            // Get Redis cache size
            String pattern = CACHE_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            long redisSize = keys != null ? keys.size() : 0;
            
            // Add local cache size
            long localSize = localCacheSize.get();
            
            return redisSize + localSize;
            
        } catch (Exception e) {
            log.warn("Error getting cache size: {}", e.getMessage());
            return localCacheSize.get();
        }
    }

    /**
     * Get cache statistics
     * 
     * @return Cache statistics
     */
    public CacheStatistics getStatistics() {
        try {
            // Get Redis statistics
            String pattern = CACHE_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            long redisSize = keys != null ? keys.size() : 0;
            
            return CacheStatistics.builder()
                    .redisSize(redisSize)
                    .localSize(localCacheSize.get())
                    .totalSize(redisSize + localCacheSize.get())
                    .maxLocalSize(maxLocalCacheSize.get())
                    .build();
            
        } catch (Exception e) {
            log.warn("Error getting cache statistics: {}", e.getMessage());
            return CacheStatistics.builder()
                    .redisSize(0)
                    .localSize(localCacheSize.get())
                    .totalSize(localCacheSize.get())
                    .maxLocalSize(maxLocalCacheSize.get())
                    .build();
        }
    }

    /**
     * Cache Statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheStatistics {
        private long redisSize;
        private long localSize;
        private long totalSize;
        private long maxLocalSize;
    }
}
