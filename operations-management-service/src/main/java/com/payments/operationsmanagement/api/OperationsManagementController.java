package com.payments.operationsmanagement.api;

import com.payments.operationsmanagement.dto.ServiceHealthDto;
import com.payments.operationsmanagement.dto.FeatureFlagDto;
import com.payments.operationsmanagement.service.ServiceHealthAggregator;
import com.payments.operationsmanagement.service.FeatureFlagService;
import com.payments.operationsmanagement.service.CircuitBreakerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Operations Management Controller
 * 
 * REST API for operations management functionality including:
 * - Service health monitoring
 * - Circuit breaker management
 * - Feature flag management
 * - Kubernetes pod management
 * 
 * Base URL: /api/ops/v1
 * Port: 8021
 */
@RestController
@RequestMapping("/api/ops/v1")
@RequiredArgsConstructor
@Slf4j
public class OperationsManagementController {

    private final ServiceHealthAggregator serviceHealthAggregator;
    private final FeatureFlagService featureFlagService;
    private final CircuitBreakerService circuitBreakerService;

    /**
     * Get health status for all services
     * 
     * GET /api/ops/v1/services
     */
    @GetMapping("/services")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<ServiceHealthDto>> getAllServices() {
        log.info("Getting health for all services");
        
        List<ServiceHealthDto> services = serviceHealthAggregator.getAllServicesHealth();
        
        return ResponseEntity.ok(services);
    }

    /**
     * Get health status for a specific service
     * 
     * GET /api/ops/v1/services/{service}
     */
    @GetMapping("/services/{service}")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<ServiceHealthDto> getServiceHealth(@PathVariable String service) {
        log.info("Getting health for service: {}", service);
        
        ServiceHealthDto serviceHealth = serviceHealthAggregator.getServiceHealth(service);
        
        return ResponseEntity.ok(serviceHealth);
    }

    /**
     * Get metrics for a specific service
     * 
     * GET /api/ops/v1/services/{service}/metrics
     */
    @GetMapping("/services/{service}/metrics")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getServiceMetrics(@PathVariable String service) {
        log.info("Getting metrics for service: {}", service);
        
        // This would return detailed metrics from Prometheus
        Map<String, Object> metrics = Map.of(
            "tps", 150.5,
            "errorRate", 0.12,
            "responseTime", Map.of(
                "p50", 50,
                "p95", 120,
                "p99", 250
            ),
            "circuitBreaker", Map.of(
                "state", "CLOSED",
                "failureRate", 0.1
            )
        );
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get errors for a specific service
     * 
     * GET /api/ops/v1/services/{service}/errors
     */
    @GetMapping("/services/{service}/errors")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getServiceErrors(@PathVariable String service) {
        log.info("Getting errors for service: {}", service);
        
        // This would return recent errors from logs
        List<Map<String, Object>> errors = List.of(
            Map.of(
                "timestamp", "2025-10-20T10:30:00Z",
                "level", "ERROR",
                "message", "Payment validation failed",
                "exception", "ValidationException",
                "stackTrace", "com.payments.validation..."
            )
        );
        
        return ResponseEntity.ok(errors);
    }

    /**
     * Restart a service
     * 
     * POST /api/ops/v1/services/{service}/restart
     */
    @PostMapping("/services/{service}/restart")
    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, String>> restartService(
            @PathVariable String service,
            @RequestHeader("X-User-ID") String userId) {
        
        log.info("Restarting service: {} by user: {}", service, userId);
        
        // This would trigger a pod restart via Kubernetes API
        // For now, return success response
        
        return ResponseEntity.ok(Map.of(
            "message", "Service restart initiated",
            "service", service,
            "initiatedBy", userId,
            "timestamp", java.time.Instant.now().toString()
        ));
    }

    /**
     * Get all circuit breakers
     * 
     * GET /api/ops/v1/circuit-breakers
     */
    @GetMapping("/circuit-breakers")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllCircuitBreakers() {
        log.info("Getting all circuit breakers");
        
        List<Map<String, Object>> circuitBreakers = circuitBreakerService.getAllCircuitBreakers();
        
        return ResponseEntity.ok(circuitBreakers);
    }

    /**
     * Get circuit breaker for a specific service
     * 
     * GET /api/ops/v1/circuit-breakers/{service}
     */
    @GetMapping("/circuit-breakers/{service}")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getCircuitBreaker(@PathVariable String service) {
        log.info("Getting circuit breaker for service: {}", service);
        
        Map<String, Object> circuitBreaker = circuitBreakerService.getCircuitBreaker(service);
        
        return ResponseEntity.ok(circuitBreaker);
    }

    /**
     * Open a circuit breaker
     * 
     * POST /api/ops/v1/circuit-breakers/{service}/open
     */
    @PostMapping("/circuit-breakers/{service}/open")
    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, String>> openCircuitBreaker(
            @PathVariable String service,
            @RequestHeader("X-User-ID") String userId) {
        
        log.info("Opening circuit breaker for service: {} by user: {}", service, userId);
        
        circuitBreakerService.openCircuitBreaker(service);
        
        return ResponseEntity.ok(Map.of(
            "message", "Circuit breaker opened",
            "service", service,
            "openedBy", userId,
            "timestamp", java.time.Instant.now().toString()
        ));
    }

    /**
     * Close a circuit breaker
     * 
     * POST /api/ops/v1/circuit-breakers/{service}/close
     */
    @PostMapping("/circuit-breakers/{service}/close")
    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, String>> closeCircuitBreaker(
            @PathVariable String service,
            @RequestHeader("X-User-ID") String userId) {
        
        log.info("Closing circuit breaker for service: {} by user: {}", service, userId);
        
        circuitBreakerService.closeCircuitBreaker(service);
        
        return ResponseEntity.ok(Map.of(
            "message", "Circuit breaker closed",
            "service", service,
            "closedBy", userId,
            "timestamp", java.time.Instant.now().toString()
        ));
    }

    /**
     * Get all feature flags
     * 
     * GET /api/ops/v1/feature-flags
     */
    @GetMapping("/feature-flags")
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<FeatureFlagDto>> getAllFeatureFlags() {
        log.info("Getting all feature flags");
        
        List<FeatureFlagDto> featureFlags = featureFlagService.getAllFeatureFlags();
        
        return ResponseEntity.ok(featureFlags);
    }

    /**
     * Toggle a feature flag
     * 
     * PUT /api/ops/v1/feature-flags/{flag}/toggle
     */
    @PutMapping("/feature-flags/{flag}/toggle")
    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<FeatureFlagDto> toggleFeatureFlag(
            @PathVariable String flag,
            @RequestBody @Valid FeatureFlagToggleRequest request,
            @RequestHeader("X-User-ID") String userId) {
        
        log.info("Toggling feature flag: {} by user: {}", flag, userId);
        
        FeatureFlagDto updatedFlag = featureFlagService.toggleFeatureFlag(flag, request, userId);
        
        return ResponseEntity.ok(updatedFlag);
    }

    /**
     * Set rollout percentage for a feature flag
     * 
     * PUT /api/ops/v1/feature-flags/{flag}/rollout
     */
    @PutMapping("/feature-flags/{flag}/rollout")
    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<FeatureFlagDto> setFeatureFlagRollout(
            @PathVariable String flag,
            @RequestBody @Valid FeatureFlagRolloutRequest request,
            @RequestHeader("X-User-ID") String userId) {
        
        log.info("Setting rollout for feature flag: {} by user: {}", flag, userId);
        
        FeatureFlagDto updatedFlag = featureFlagService.setFeatureFlagRollout(flag, request, userId);
        
        return ResponseEntity.ok(updatedFlag);
    }

    /**
     * Feature flag toggle request DTO
     */
    public static class FeatureFlagToggleRequest {
        private Boolean enabled;
        private Integer rolloutPercentage;

        // Getters and setters
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public Integer getRolloutPercentage() { return rolloutPercentage; }
        public void setRolloutPercentage(Integer rolloutPercentage) { this.rolloutPercentage = rolloutPercentage; }
    }

    /**
     * Feature flag rollout request DTO
     */
    public static class FeatureFlagRolloutRequest {
        private Integer rolloutPercentage;

        // Getters and setters
        public Integer getRolloutPercentage() { return rolloutPercentage; }
        public void setRolloutPercentage(Integer rolloutPercentage) { this.rolloutPercentage = rolloutPercentage; }
    }
}
