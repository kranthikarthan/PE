package com.paymentengine.paymentprocessing.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serialization.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Redis caching
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Redis template for general operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache manager with different TTL configurations
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // Cache-specific configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Scheme configuration cache - 1 hour TTL
        cacheConfigurations.put("scheme-config", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Clearing system configuration cache - 2 hours TTL
        cacheConfigurations.put("clearing-system-config", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Message transformation cache - 30 minutes TTL
        cacheConfigurations.put("message-transformation", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Routing rules cache - 1 hour TTL
        cacheConfigurations.put("routing-rules", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Tenant configuration cache - 4 hours TTL
        cacheConfigurations.put("tenant-config", defaultConfig.entryTtl(Duration.ofHours(4)));
        
        // Message validation cache - 15 minutes TTL
        cacheConfigurations.put("message-validation", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Circuit breaker state cache - 5 minutes TTL
        cacheConfigurations.put("circuit-breaker-state", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Webhook configuration cache - 1 hour TTL
        cacheConfigurations.put("webhook-config", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Kafka topic configuration cache - 2 hours TTL
        cacheConfigurations.put("kafka-topic-config", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Monitoring metrics cache - 1 minute TTL
        cacheConfigurations.put("monitoring-metrics", defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}