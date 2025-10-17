package com.payments.saga.api;

import com.payments.saga.service.SagaEventService;
import com.payments.saga.service.SagaService;
import com.payments.saga.service.SagaStepService;
import com.payments.saga.service.TenantContextResolverInterface;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
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
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Health check controller for saga orchestrator */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController implements HealthIndicator {

  private final SagaService sagaService;
  private final SagaStepService sagaStepService;
  private final SagaEventService sagaEventService;
  private final TenantContextResolverInterface tenantContextResolver;
  private final DataSource dataSource;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final CircuitBreakerRegistry circuitBreakerRegistry;
  private final MeterRegistry meterRegistry;
  private final long applicationStartTime = System.currentTimeMillis();

  @Override
  public Health health() {
    try {
      Map<String, Object> details = new HashMap<>();
      details.put("service", "saga-orchestrator");
      details.put("timestamp", Instant.now().toString());

      // Check database connectivity
      boolean dbHealthy = checkDatabaseHealth();
      details.put("database", dbHealthy ? "UP" : "DOWN");

      // Check Kafka connectivity
      boolean kafkaHealthy = checkKafkaHealth();
      details.put("kafka", kafkaHealthy ? "UP" : "DOWN");

      // Check circuit breaker status
      Map<String, String> circuitBreakerStatus = getCircuitBreakerStatus();
      details.put("circuitBreakers", circuitBreakerStatus);

      // Get performance metrics
      Map<String, Object> metrics = getPerformanceMetrics();
      details.put("metrics", metrics);

      boolean overallHealthy = dbHealthy && kafkaHealthy;

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

  /** Detailed health check with tenant-specific information */
  @GetMapping("/detailed")
  public ResponseEntity<Map<String, Object>> detailedHealth(
      @RequestParam(required = false) String tenantId,
      @RequestParam(required = false) String businessUnitId) {
    log.debug(
        "Detailed health check requested for tenant: {}, business unit: {}",
        tenantId,
        businessUnitId);

    try {
      Map<String, Object> health = new HashMap<>();
      health.put("service", "saga-orchestrator");
      health.put("timestamp", Instant.now().toString());

      // Basic health status
      boolean dbHealthy = checkDatabaseHealth();
      boolean kafkaHealthy = checkKafkaHealth();
      health.put("database", dbHealthy ? "UP" : "DOWN");
      health.put("kafka", kafkaHealthy ? "UP" : "DOWN");
      health.put("status", (dbHealthy && kafkaHealthy) ? "UP" : "DOWN");

      // Tenant-specific health if provided
      if (tenantId != null && businessUnitId != null) {
        Map<String, Object> tenantHealth = getTenantSpecificHealth(tenantId, businessUnitId);
        health.put("tenantHealth", tenantHealth);
      }

      // Get active sagas count
      long activeSagasCount = sagaService.getActiveSagas().size();
      health.put("activeSagas", activeSagasCount);

      // Get detailed component status
      Map<String, Object> components = getDetailedComponentStatus();
      health.put("components", components);

      return ResponseEntity.ok(health);

    } catch (Exception e) {
      log.error("Detailed health check failed: {}", e.getMessage(), e);

      Map<String, Object> health = new HashMap<>();
      health.put("status", "DOWN");
      health.put("service", "saga-orchestrator");
      health.put("timestamp", Instant.now().toString());
      health.put("error", e.getMessage());

      return ResponseEntity.status(503).body(health);
    }
  }

  /** Readiness probe for Kubernetes */
  @GetMapping("/ready")
  public ResponseEntity<Map<String, Object>> readiness() {
    log.debug("Readiness check requested");

    try {
      // Check if all critical dependencies are available
      boolean dbReady = checkDatabaseHealth();
      boolean kafkaReady = checkKafkaHealth();

      boolean ready = dbReady && kafkaReady;

      Map<String, Object> response =
          Map.of(
              "status", ready ? "READY" : "NOT_READY",
              "timestamp", Instant.now().toString(),
              "database", dbReady ? "READY" : "NOT_READY",
              "kafka", kafkaReady ? "READY" : "NOT_READY");

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

  /** Liveness probe for Kubernetes */
  @GetMapping("/live")
  public ResponseEntity<Map<String, Object>> liveness() {
    log.debug("Liveness check requested");

    // Liveness check should be lightweight - just check if the service is running
    Map<String, Object> response =
        Map.of(
            "status",
            "ALIVE",
            "timestamp",
            Instant.now().toString(),
            "uptime",
            System.currentTimeMillis() - getStartTime());

    return ResponseEntity.ok(response);
  }

  private boolean checkDatabaseHealth() {
    try {
      // Test database connection with timeout
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

  private boolean checkKafkaHealth() {
    try {
      // Test Kafka connectivity with timeout
      CompletableFuture<Boolean> future =
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  // Send a test message to a health check topic
                  kafkaTemplate
                      .send("health-check", "test-key", "test-message")
                      .get(5, TimeUnit.SECONDS);
                  return true;
                } catch (Exception e) {
                  log.warn("Kafka health check failed: {}", e.getMessage());
                  return false;
                }
              });

      return future.get(10, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.warn("Kafka health check failed: {}", e.getMessage());
      return false;
    }
  }

  private Map<String, String> getCircuitBreakerStatus() {
    Map<String, String> status = new HashMap<>();

    try {
      circuitBreakerRegistry
          .getAllCircuitBreakers()
          .forEach(
              circuitBreaker -> {
                CircuitBreaker.State state = circuitBreaker.getState();
                status.put(circuitBreaker.getName(), state.name());
              });
    } catch (Exception e) {
      log.warn("Failed to get circuit breaker status: {}", e.getMessage());
      status.put("error", "Unable to retrieve circuit breaker status");
    }

    return status;
  }

  private Map<String, Object> getPerformanceMetrics() {
    Map<String, Object> metrics = new HashMap<>();

    try {
      // Get JVM metrics
      Runtime runtime = Runtime.getRuntime();
      metrics.put(
          "memory",
          Map.of(
              "used", runtime.totalMemory() - runtime.freeMemory(),
              "max", runtime.maxMemory(),
              "free", runtime.freeMemory()));

      // Get application metrics
      metrics.put(
          "customMetrics",
          Map.of(
              "activeSagas", sagaService.getActiveSagas().size(),
              "timestamp", System.currentTimeMillis()));

    } catch (Exception e) {
      log.warn("Failed to get performance metrics: {}", e.getMessage());
      metrics.put("error", "Unable to retrieve metrics");
    }

    return metrics;
  }

  private Map<String, Object> getTenantSpecificHealth(String tenantId, String businessUnitId) {
    Map<String, Object> tenantHealth = new HashMap<>();

    try {
      // Test tenant context resolution
      var tenantContext = tenantContextResolver.resolve(tenantId, businessUnitId);
      tenantHealth.put("tenantContext", "RESOLVED");
      tenantHealth.put("tenantId", tenantContext.getTenantId());
      tenantHealth.put("businessUnitId", tenantContext.getBusinessUnitId());

      // Get tenant-specific saga statistics
      var tenantSagas = sagaService.getSagasByTenantAndBusinessUnit(tenantId, businessUnitId);
      tenantHealth.put("tenantSagas", tenantSagas.size());

    } catch (Exception e) {
      log.warn("Tenant-specific health check failed: {}", e.getMessage());
      tenantHealth.put("tenantContext", "FAILED");
      tenantHealth.put("error", e.getMessage());
    }

    return tenantHealth;
  }

  private Map<String, Object> getDetailedComponentStatus() {
    Map<String, Object> components = new HashMap<>();

    // Database status
    boolean dbHealthy = checkDatabaseHealth();
    components.put("database", Map.of("status", dbHealthy ? "UP" : "DOWN", "type", "PostgreSQL"));

    // Kafka status
    boolean kafkaHealthy = checkKafkaHealth();
    components.put("kafka", Map.of("status", kafkaHealthy ? "UP" : "DOWN", "type", "Apache Kafka"));

    // Circuit breaker status
    components.put("circuitBreakers", getCircuitBreakerStatus());

    return components;
  }

  private long getStartTime() {
    // This would typically be stored as a class field when the application starts
    return applicationStartTime;
  }
}
