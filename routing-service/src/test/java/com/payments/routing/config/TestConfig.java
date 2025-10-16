package com.payments.routing.config;

import static org.mockito.Mockito.mock;

import com.payments.routing.repository.RoutingRuleRepository;
import com.payments.routing.service.RoutingCacheService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Test Configuration
 *
 * <p>Mock beans for testing: - Routing rule repository - Routing cache service - Redis template
 *
 * <p>Ensures isolated testing without external dependencies
 */
@TestConfiguration
public class TestConfig {

  @Bean
  @Primary
  public RoutingRuleRepository routingRuleRepository() {
    return mock(RoutingRuleRepository.class);
  }

  @Bean
  @Primary
  public RoutingCacheService routingCacheService() {
    return mock(RoutingCacheService.class);
  }

  @Bean
  @Primary
  public RedisTemplate<String, Object> redisTemplate() {
    return mock(RedisTemplate.class);
  }

  @Bean
  @Primary
  public ValueOperations<String, Object> valueOperations() {
    return mock(ValueOperations.class);
  }
}
