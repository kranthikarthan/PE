package com.payments.routing.api;

import com.payments.routing.repository.RoutingRuleRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Health check controller for routing service */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController implements HealthIndicator {

  private final DataSource dataSource;
  private final RedisConnectionFactory redisConnectionFactory;
  private final RoutingRuleRepository routingRuleRepository;
  private final long applicationStartTime = System.currentTimeMillis();

  @Override
  public Health health() {
    try {
      Map<String, Object> details = new HashMap<>();
      details.put("service", "routing-service");
      details.put("timestamp", Instant.now().toString());

      boolean dbHealthy = checkDatabaseHealth();
      details.put("database", dbHealthy ? "UP" : "DOWN");

      boolean redisHealthy = checkRedisHealth();
      details.put("redis", redisHealthy ? "UP" : "DOWN");

      details.put("metrics", getPerformanceMetrics());

      boolean overallHealthy = dbHealthy && redisHealthy;

      return overallHealthy
          ? Health.up().withDetails(details).build()
          : Health.down().withDetails(details).build();

    } catch (Exception e) {
      log.error("Health check failed: {}", e.getMessage(), e);
      return Health.down()
          .withDetail("error", e.getMessage())
          .withDetail("timestamp", Instant.now().toString())
          .build();
    }
  }

  /** Basic health check endpoint */
  @GetMapping
  public ResponseEntity<Map<String, Object>> healthEndpoint() {
    log.debug("Health check requested");

    Health health = health();
    Map<String, Object> response = new HashMap<>(health.getDetails());
    response.put("status", health.getStatus().getCode());

    return health.getStatus().equals(Status.UP)
        ? ResponseEntity.ok(response)
        : ResponseEntity.status(503).body(response);
  }

  /** Readiness probe */
  @GetMapping("/ready")
  public ResponseEntity<Map<String, Object>> readiness() {
    log.debug("Readiness check requested");

    try {
      boolean dbReady = checkDatabaseHealth();
      boolean redisReady = checkRedisHealth();

      boolean ready = dbReady && redisReady;

      Map<String, Object> response =
          Map.of(
              "status", ready ? "READY" : "NOT_READY",
              "timestamp", Instant.now().toString(),
              "database", dbReady ? "READY" : "NOT_READY",
              "redis", redisReady ? "READY" : "NOT_READY");

      return ready ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);

    } catch (Exception e) {
      log.error("Readiness check failed: {}", e.getMessage(), e);
      return ResponseEntity.status(503)
          .body(
              Map.of(
                  "status", "NOT_READY",
                  "error", e.getMessage(),
                  "timestamp", Instant.now().toString()));
    }
  }

  /** Liveness probe */
  @GetMapping("/live")
  public ResponseEntity<Map<String, Object>> liveness() {
    log.debug("Liveness check requested");

    Map<String, Object> response =
        Map.of(
            "status",
            "ALIVE",
            "timestamp",
            Instant.now().toString(),
            "uptime",
            System.currentTimeMillis() - applicationStartTime);

    return ResponseEntity.ok(response);
  }

  private boolean checkDatabaseHealth() {
    try {
      CompletableFuture<Boolean> future =
          CompletableFuture.supplyAsync(
              () -> {
                try (var connection = dataSource.getConnection()) {
                  return connection.isValid(5);
                } catch (Exception e) {
                  log.warn("Database health check failed: {}", e.getMessage());
                  return false;
                }
              });

      return future.get(10, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.warn("Database health check failed: {}", e.getMessage());
      return false;
    }
  }

  private boolean checkRedisHealth() {
    RedisConnection connection = null;
    try {
      connection = redisConnectionFactory.getConnection();
      String pong = connection.ping();
      return pong != null && !pong.isBlank();
    } catch (Exception e) {
      log.warn("Redis health check failed: {}", e.getMessage());
      return false;
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception ignore) {
        // ignore close exceptions
      }
    }
  }

  private Map<String, Object> getPerformanceMetrics() {
    Map<String, Object> metrics = new HashMap<>();

    try {
      Runtime runtime = Runtime.getRuntime();
      metrics.put(
          "memory",
          Map.of(
              "used", runtime.totalMemory() - runtime.freeMemory(),
              "max", runtime.maxMemory(),
              "free", runtime.freeMemory()));

      metrics.put(
          "customMetrics",
          Map.of(
              "totalRules", routingRuleRepository.count(),
              "timestamp", System.currentTimeMillis()));

    } catch (Exception e) {
      log.warn("Failed to get performance metrics: {}", e.getMessage());
      metrics.put("error", "Unable to retrieve metrics");
    }

    return metrics;
  }
}
