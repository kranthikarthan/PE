package com.paymentengine.corebanking.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Caching Configuration for High TPS
 * 
 * Optimized Redis caching configuration for handling 2000+ TPS
 * with proper cache eviction, serialization, and performance tuning.
 */
@Configuration
@EnableCaching
public class CachingConfig {
    
    /**
     * Redis Cache Manager with performance optimizations
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues()
            .enableStatistics();
        
        // Cache-specific configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Account cache - longer TTL for account data
        cacheConfigurations.put("account-cache", defaultConfig
            .entryTtl(Duration.ofHours(1))
            .prefixCacheNameWith("account:"));
        
        // Payment type cache - medium TTL
        cacheConfigurations.put("payment-type-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(30))
            .prefixCacheNameWith("payment-type:"));
        
        // Tenant configuration cache - longer TTL
        cacheConfigurations.put("tenant-config-cache", defaultConfig
            .entryTtl(Duration.ofHours(2))
            .prefixCacheNameWith("tenant-config:"));
        
        // Fraud rules cache - medium TTL
        cacheConfigurations.put("fraud-rules-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(15))
            .prefixCacheNameWith("fraud-rules:"));
        
        // Transaction status cache - short TTL for real-time data
        cacheConfigurations.put("transaction-status-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(5))
            .prefixCacheNameWith("transaction-status:"));
        
        // Account balance cache - short TTL for accuracy
        cacheConfigurations.put("account-balance-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(2))
            .prefixCacheNameWith("account-balance:"));
        
        // Payment routing cache - medium TTL
        cacheConfigurations.put("payment-routing-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(20))
            .prefixCacheNameWith("payment-routing:"));
        
        // ISO 20022 message cache - short TTL
        cacheConfigurations.put("iso20022-message-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(5))
            .prefixCacheNameWith("iso20022-message:"));
        
        // External API response cache - medium TTL
        cacheConfigurations.put("external-api-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(10))
            .prefixCacheNameWith("external-api:"));
        
        // Rate limiting cache - very short TTL
        cacheConfigurations.put("rate-limit-cache", defaultConfig
            .entryTtl(Duration.ofSeconds(60))
            .prefixCacheNameWith("rate-limit:"));
        
        // Session cache - medium TTL
        cacheConfigurations.put("session-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(30))
            .prefixCacheNameWith("session:"));
        
        // Audit log cache - short TTL
        cacheConfigurations.put("audit-log-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(5))
            .prefixCacheNameWith("audit-log:"));
        
        // Performance metrics cache - short TTL
        cacheConfigurations.put("performance-metrics-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(1))
            .prefixCacheNameWith("performance-metrics:"));
        
        // Configuration cache - long TTL
        cacheConfigurations.put("configuration-cache", defaultConfig
            .entryTtl(Duration.ofHours(4))
            .prefixCacheNameWith("configuration:"));
        
        // User permissions cache - medium TTL
        cacheConfigurations.put("user-permissions-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(15))
            .prefixCacheNameWith("user-permissions:"));
        
        // Notification template cache - long TTL
        cacheConfigurations.put("notification-template-cache", defaultConfig
            .entryTtl(Duration.ofHours(6))
            .prefixCacheNameWith("notification-template:"));
        
        // Currency exchange rate cache - short TTL for accuracy
        cacheConfigurations.put("currency-exchange-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(5))
            .prefixCacheNameWith("currency-exchange:"));
        
        // Bank routing cache - long TTL
        cacheConfigurations.put("bank-routing-cache", defaultConfig
            .entryTtl(Duration.ofHours(12))
            .prefixCacheNameWith("bank-routing:"));
        
        // Compliance rules cache - medium TTL
        cacheConfigurations.put("compliance-rules-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(30))
            .prefixCacheNameWith("compliance-rules:"));
        
        // Risk assessment cache - short TTL
        cacheConfigurations.put("risk-assessment-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(10))
            .prefixCacheNameWith("risk-assessment:"));
        
        // Transaction limits cache - medium TTL
        cacheConfigurations.put("transaction-limits-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(20))
            .prefixCacheNameWith("transaction-limits:"));
        
        // Fee calculation cache - medium TTL
        cacheConfigurations.put("fee-calculation-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(15))
            .prefixCacheNameWith("fee-calculation:"));
        
        // Validation rules cache - long TTL
        cacheConfigurations.put("validation-rules-cache", defaultConfig
            .entryTtl(Duration.ofHours(2))
            .prefixCacheNameWith("validation-rules:"));
        
        // Workflow state cache - short TTL
        cacheConfigurations.put("workflow-state-cache", defaultConfig
            .entryTtl(Duration.ofMinutes(5))
            .prefixCacheNameWith("workflow-state:"));
        
        // Message template cache - long TTL
        cacheConfigurations.put("message-template-cache", defaultConfig
            .entryTtl(Duration.ofHours(8))
            .prefixCacheNameWith("message-template:"));
        
        // System configuration cache - very long TTL
        cacheConfigurations.put("system-config-cache", defaultConfig
            .entryTtl(Duration.ofHours(24))
            .prefixCacheNameWith("system-config:"));
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}