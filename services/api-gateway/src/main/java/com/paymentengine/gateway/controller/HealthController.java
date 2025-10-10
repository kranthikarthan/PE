package com.paymentengine.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Health check controller for API Gateway
 */
@RestController
public class HealthController {
    
    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;
    
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return checkRedisHealth()
            .map(redisHealth -> {
                boolean isHealthy = redisHealth.equals("UP");
                
                Map<String, Object> health = Map.of(
                    "status", isHealthy ? "UP" : "DOWN",
                    "timestamp", Instant.now().toString(),
                    "components", Map.of(
                        "gateway", "UP",
                        "redis", redisHealth
                    ),
                    "details", Map.of(
                        "version", "1.0.0",
                        "environment", System.getProperty("spring.profiles.active", "default")
                    )
                );
                
                return ResponseEntity.ok(health);
            })
            .onErrorReturn(ResponseEntity.status(503).body(Map.of(
                "status", "DOWN",
                "timestamp", Instant.now().toString(),
                "error", "Health check failed"
            )));
    }
    
    @GetMapping("/health/ready")
    public Mono<ResponseEntity<Map<String, Object>>> readiness() {
        return checkRedisHealth()
            .map(redisHealth -> {
                boolean isReady = redisHealth.equals("UP");
                
                Map<String, Object> readiness = Map.of(
                    "status", isReady ? "READY" : "NOT_READY",
                    "timestamp", Instant.now().toString(),
                    "checks", Map.of(
                        "redis", redisHealth
                    )
                );
                
                return isReady 
                    ? ResponseEntity.ok(readiness)
                    : ResponseEntity.status(503).body(readiness);
            })
            .onErrorReturn(ResponseEntity.status(503).body(Map.of(
                "status", "NOT_READY",
                "timestamp", Instant.now().toString(),
                "error", "Readiness check failed"
            )));
    }
    
    @GetMapping("/health/live")
    public Mono<ResponseEntity<Map<String, Object>>> liveness() {
        Map<String, Object> liveness = Map.of(
            "status", "ALIVE",
            "timestamp", Instant.now().toString(),
            "uptime", getUptime()
        );
        
        return Mono.just(ResponseEntity.ok(liveness));
    }
    
    private Mono<String> checkRedisHealth() {
        return redisTemplate.opsForValue()
            .set("health:check", "ping")
            .then(redisTemplate.opsForValue().get("health:check"))
            .timeout(Duration.ofSeconds(2))
            .map(result -> "ping".equals(result) ? "UP" : "DOWN")
            .onErrorReturn("DOWN");
    }
    
    private String getUptime() {
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeSeconds = uptimeMs / 1000;
        
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}