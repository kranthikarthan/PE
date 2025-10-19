package com.payments.webbff.config;

import com.payments.webbff.dto.PaymentDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration for Web BFF Service.
 *
 * <p>This configuration sets up Redis for caching and real-time subscriptions
 * in the Web BFF service.
 *
 * @since PE-414
 */
@Configuration
public class RedisConfig {

    /**
     * Configures Redis template for synchronous operations.
     *
     * @param connectionFactory Redis connection factory
     * @return configured Redis template
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configures reactive Redis template for asynchronous operations.
     *
     * @param connectionFactory Redis connection factory
     * @return configured reactive Redis template
     */
    @Bean
    public ReactiveRedisTemplate<String, PaymentDto> reactiveRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
        
        RedisSerializationContext.RedisSerializationContextBuilder<String, PaymentDto> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);
        
        RedisSerializationContext<String, PaymentDto> context = builder
                .value(valueSerializer)
                .build();
        
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
