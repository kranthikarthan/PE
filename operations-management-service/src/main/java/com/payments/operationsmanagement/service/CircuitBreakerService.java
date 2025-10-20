package com.payments.operationsmanagement.service;

import com.payments.operationsmanagement.entity.OperationsAuditLogEntity;
import com.payments.operationsmanagement.repository.OperationsAuditLogRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Circuit Breaker Service
 * 
 * Manages circuit breakers for all services using Resilience4j.
 * Provides functionality to open/close circuit breakers and monitor their state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CircuitBreakerService {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final OperationsAuditLogRepository auditLogRepository;

    /**
     * Get all circuit breakers
     */
    public List<Map<String, Object>> getAllCircuitBreakers() {
        log.info("Getting all circuit breakers");
        
        return circuitBreakerRegistry.getAllCircuitBreakers()
            .stream()
            .map(this::mapCircuitBreakerToMap)
            .collect(Collectors.toList());
    }

    /**
     * Get circuit breaker for a specific service
     */
    public Map<String, Object> getCircuitBreaker(String serviceName) {
        log.info("Getting circuit breaker for service: {}", serviceName);
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
        return mapCircuitBreakerToMap(circuitBreaker);
    }

    /**
     * Open a circuit breaker
     */
    public void openCircuitBreaker(String serviceName) {
        log.info("Opening circuit breaker for service: {}", serviceName);
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
        
        // Transition to OPEN state
        circuitBreaker.transitionToOpenState();
        
        log.info("Circuit breaker opened for service: {}", serviceName);
    }

    /**
     * Close a circuit breaker
     */
    public void closeCircuitBreaker(String serviceName) {
        log.info("Closing circuit breaker for service: {}", serviceName);
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
        
        // Transition to CLOSED state
        circuitBreaker.transitionToClosedState();
        
        log.info("Circuit breaker closed for service: {}", serviceName);
    }

    /**
     * Get circuit breaker state
     */
    public CircuitBreaker.State getCircuitBreakerState(String serviceName) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
        return circuitBreaker.getState();
    }

    /**
     * Get circuit breaker metrics
     */
    public Map<String, Object> getCircuitBreakerMetrics(String serviceName) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
        
        return Map.of(
            "state", circuitBreaker.getState().name(),
            "failureRate", circuitBreaker.getMetrics().getFailureRate(),
            "successfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls(),
            "failedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls(),
            "notPermittedCalls", circuitBreaker.getMetrics().getNumberOfNotPermittedCalls(),
            "totalCalls", circuitBreaker.getMetrics().getNumberOfBufferedCalls()
        );
    }

    /**
     * Map circuit breaker to map for API response
     */
    private Map<String, Object> mapCircuitBreakerToMap(CircuitBreaker circuitBreaker) {
        return Map.of(
            "name", circuitBreaker.getName(),
            "state", circuitBreaker.getState().name(),
            "failureRate", circuitBreaker.getMetrics().getFailureRate(),
            "successfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls(),
            "failedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls(),
            "notPermittedCalls", circuitBreaker.getMetrics().getNumberOfNotPermittedCalls(),
            "totalCalls", circuitBreaker.getMetrics().getNumberOfBufferedCalls(),
            "lastStateTransition", circuitBreaker.getState().getCreationTime()
        );
    }

    /**
     * Log audit action
     */
    private void logAuditAction(OperationsAuditLogEntity.ActionType actionType,
                              String entityId,
                              String userId) {
        try {
            OperationsAuditLogEntity auditLog = OperationsAuditLogEntity.builder()
                .auditId(UUID.randomUUID().toString())
                .tenantId("default") // Would get from context
                .userId(userId)
                .actionType(actionType)
                .entityType(OperationsAuditLogEntity.EntityType.CIRCUIT_BREAKER)
                .entityId(entityId)
                .actionDetails("{}")
                .ipAddress("127.0.0.1") // Would get from request
                .userAgent("Operations Management Service")
                .performedAt(Instant.now())
                .build();

            auditLogRepository.save(auditLog);
            log.info("Logged audit action: {} for circuit breaker: {}", actionType, entityId);

        } catch (Exception e) {
            log.error("Failed to log audit action: {} for circuit breaker: {}", actionType, entityId, e);
        }
    }
}
