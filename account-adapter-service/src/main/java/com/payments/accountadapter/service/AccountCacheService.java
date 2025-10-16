package com.payments.accountadapter.service;

import com.payments.accountadapter.dto.*;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Account Cache Service
 *
 * <p>Service for caching account data in Redis: - Account balance caching - Account validation
 * caching - Account status caching - Cache management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCacheService {

  private final RedisTemplate<String, Object> redisTemplate;

  @Value("${account.service.cache.ttl:300000}")
  private long cacheTtl;

  private static final String BALANCE_CACHE_PREFIX = "account:balance:";
  private static final String VALIDATION_CACHE_PREFIX = "account:validation:";
  private static final String STATUS_CACHE_PREFIX = "account:status:";

  /**
   * Cache account balance
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @param response Balance response
   */
  public void cacheAccountBalance(
      String accountNumber, String tenantId, AccountBalanceResponse response) {
    try {
      String cacheKey = BALANCE_CACHE_PREFIX + tenantId + ":" + accountNumber;
      redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMillis(cacheTtl));
      log.debug("Cached account balance for account: {} with TTL: {}ms", accountNumber, cacheTtl);
    } catch (Exception e) {
      log.warn("Failed to cache account balance for account: {}", accountNumber, e);
    }
  }

  /**
   * Get cached account balance
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @return Cached balance response or null
   */
  public AccountBalanceResponse getCachedAccountBalance(String accountNumber, String tenantId) {
    try {
      String cacheKey = BALANCE_CACHE_PREFIX + tenantId + ":" + accountNumber;
      Object cached = redisTemplate.opsForValue().get(cacheKey);
      if (cached instanceof AccountBalanceResponse) {
        log.debug("Retrieved cached account balance for account: {}", accountNumber);
        return (AccountBalanceResponse) cached;
      }
    } catch (Exception e) {
      log.warn("Failed to get cached account balance for account: {}", accountNumber, e);
    }
    return null;
  }

  /**
   * Cache account validation
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @param response Validation response
   */
  public void cacheAccountValidation(
      String accountNumber, String tenantId, AccountValidationResponse response) {
    try {
      String cacheKey = VALIDATION_CACHE_PREFIX + tenantId + ":" + accountNumber;
      redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMillis(cacheTtl));
      log.debug(
          "Cached account validation for account: {} with TTL: {}ms", accountNumber, cacheTtl);
    } catch (Exception e) {
      log.warn("Failed to cache account validation for account: {}", accountNumber, e);
    }
  }

  /**
   * Get cached account validation
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @return Cached validation response or null
   */
  public AccountValidationResponse getCachedAccountValidation(
      String accountNumber, String tenantId) {
    try {
      String cacheKey = VALIDATION_CACHE_PREFIX + tenantId + ":" + accountNumber;
      Object cached = redisTemplate.opsForValue().get(cacheKey);
      if (cached instanceof AccountValidationResponse) {
        log.debug("Retrieved cached account validation for account: {}", accountNumber);
        return (AccountValidationResponse) cached;
      }
    } catch (Exception e) {
      log.warn("Failed to get cached account validation for account: {}", accountNumber, e);
    }
    return null;
  }

  /**
   * Cache account status
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @param response Status response
   */
  public void cacheAccountStatus(
      String accountNumber, String tenantId, AccountStatusResponse response) {
    try {
      String cacheKey = STATUS_CACHE_PREFIX + tenantId + ":" + accountNumber;
      redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMillis(cacheTtl));
      log.debug("Cached account status for account: {} with TTL: {}ms", accountNumber, cacheTtl);
    } catch (Exception e) {
      log.warn("Failed to cache account status for account: {}", accountNumber, e);
    }
  }

  /**
   * Get cached account status
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @return Cached status response or null
   */
  public AccountStatusResponse getCachedAccountStatus(String accountNumber, String tenantId) {
    try {
      String cacheKey = STATUS_CACHE_PREFIX + tenantId + ":" + accountNumber;
      Object cached = redisTemplate.opsForValue().get(cacheKey);
      if (cached instanceof AccountStatusResponse) {
        log.debug("Retrieved cached account status for account: {}", accountNumber);
        return (AccountStatusResponse) cached;
      }
    } catch (Exception e) {
      log.warn("Failed to get cached account status for account: {}", accountNumber, e);
    }
    return null;
  }

  /**
   * Clear account cache
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   */
  public void clearAccountCache(String accountNumber, String tenantId) {
    try {
      String balanceKey = BALANCE_CACHE_PREFIX + tenantId + ":" + accountNumber;
      String validationKey = VALIDATION_CACHE_PREFIX + tenantId + ":" + accountNumber;
      String statusKey = STATUS_CACHE_PREFIX + tenantId + ":" + accountNumber;

      redisTemplate.delete(balanceKey);
      redisTemplate.delete(validationKey);
      redisTemplate.delete(statusKey);

      log.info("Cleared account cache for account: {} and tenant: {}", accountNumber, tenantId);
    } catch (Exception e) {
      log.warn("Failed to clear account cache for account: {}", accountNumber, e);
    }
  }

  /**
   * Clear all cache for tenant
   *
   * @param tenantId Tenant ID
   */
  public void clearTenantCache(String tenantId) {
    try {
      String balancePattern = BALANCE_CACHE_PREFIX + tenantId + ":*";
      String validationPattern = VALIDATION_CACHE_PREFIX + tenantId + ":*";
      String statusPattern = STATUS_CACHE_PREFIX + tenantId + ":*";

      redisTemplate.delete(redisTemplate.keys(balancePattern));
      redisTemplate.delete(redisTemplate.keys(validationPattern));
      redisTemplate.delete(redisTemplate.keys(statusPattern));

      log.info("Cleared all cache for tenant: {}", tenantId);
    } catch (Exception e) {
      log.warn("Failed to clear tenant cache for tenant: {}", tenantId, e);
    }
  }

  /** Clear all cache */
  public void clearAllCache() {
    try {
      redisTemplate.delete(redisTemplate.keys(BALANCE_CACHE_PREFIX + "*"));
      redisTemplate.delete(redisTemplate.keys(VALIDATION_CACHE_PREFIX + "*"));
      redisTemplate.delete(redisTemplate.keys(STATUS_CACHE_PREFIX + "*"));

      log.info("Cleared all account cache");
    } catch (Exception e) {
      log.warn("Failed to clear all cache", e);
    }
  }

  /**
   * Get cache statistics
   *
   * @return Cache statistics
   */
  public CacheStatistics getCacheStatistics() {
    try {
      long balanceCount = redisTemplate.keys(BALANCE_CACHE_PREFIX + "*").size();
      long validationCount = redisTemplate.keys(VALIDATION_CACHE_PREFIX + "*").size();
      long statusCount = redisTemplate.keys(STATUS_CACHE_PREFIX + "*").size();

      return CacheStatistics.builder()
          .balanceCacheCount(balanceCount)
          .validationCacheCount(validationCount)
          .statusCacheCount(statusCount)
          .totalCacheCount(balanceCount + validationCount + statusCount)
          .cacheTtl(cacheTtl)
          .build();
    } catch (Exception e) {
      log.warn("Failed to get cache statistics", e);
      return CacheStatistics.builder()
          .balanceCacheCount(0L)
          .validationCacheCount(0L)
          .statusCacheCount(0L)
          .totalCacheCount(0L)
          .cacheTtl(cacheTtl)
          .build();
    }
  }

  /** Cache Statistics */
  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class CacheStatistics {
    private Long balanceCacheCount;
    private Long validationCacheCount;
    private Long statusCacheCount;
    private Long totalCacheCount;
    private Long cacheTtl;
  }
}
