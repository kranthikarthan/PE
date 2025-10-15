package com.payments.routing.api;

import com.payments.routing.engine.RoutingDecision;
import com.payments.routing.engine.RoutingRequest;
import com.payments.routing.service.RoutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Routing Controller
 * 
 * REST API for payment routing decisions:
 * - Route payment to appropriate clearing system
 * - Get routing statistics
 * - Manage routing cache
 * 
 * Security: JWT validation, tenant isolation
 * Performance: Redis caching, DSA-optimized data structures
 * Resilience: Istio for internal calls (not Resilience4j)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/routing")
@RequiredArgsConstructor
@Tag(name = "Routing Service", description = "Payment routing decisions and clearing system selection")
public class RoutingController {

    private final RoutingService routingService;

    /**
     * Get routing decision for payment
     * 
     * @param request Routing request
     * @param tenantId Tenant ID from header
     * @param correlationId Correlation ID for tracing
     * @return Routing decision
     */
    @PostMapping("/decisions")
    @Operation(
        summary = "Get routing decision",
        description = "Determine the appropriate clearing system for a payment based on routing rules"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Routing decision made successfully",
            content = @Content(schema = @Schema(implementation = RoutingDecision.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<RoutingDecision> getRoutingDecision(
            @Valid @RequestBody RoutingRequest request,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "Tenant ID") String tenantId,
            @RequestHeader(value = "X-Correlation-ID", required = false) @Parameter(description = "Correlation ID") String correlationId) {
        
        log.info("Getting routing decision for payment: {} with tenant: {}", 
                request.getPaymentId(), tenantId);

        try {
            // Validate tenant context
            if (!tenantId.equals(request.getTenantId())) {
                log.warn("Tenant ID mismatch: header={}, request={}", tenantId, request.getTenantId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Get routing decision
            RoutingDecision decision = routingService.getRoutingDecision(request);
            
            log.info("Routing decision made for payment: {} - clearing system: {}, priority: {}", 
                    request.getPaymentId(), decision.getClearingSystem(), decision.getPriority());

            return ResponseEntity.ok(decision);

        } catch (Exception e) {
            log.error("Error getting routing decision for payment: {}", request.getPaymentId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get routing decision with fallback clearing system
     * 
     * @param request Routing request
     * @param fallbackClearingSystem Fallback clearing system
     * @param tenantId Tenant ID from header
     * @param correlationId Correlation ID for tracing
     * @return Routing decision
     */
    @PostMapping("/decisions/with-fallback")
    @Operation(
        summary = "Get routing decision with fallback",
        description = "Determine the appropriate clearing system for a payment with fallback option"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Routing decision made successfully",
            content = @Content(schema = @Schema(implementation = RoutingDecision.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<RoutingDecision> getRoutingDecisionWithFallback(
            @Valid @RequestBody RoutingRequest request,
            @RequestParam("fallbackClearingSystem") @Parameter(description = "Fallback clearing system") String fallbackClearingSystem,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "Tenant ID") String tenantId,
            @RequestHeader(value = "X-Correlation-ID", required = false) @Parameter(description = "Correlation ID") String correlationId) {
        
        log.info("Getting routing decision with fallback for payment: {} with tenant: {}, fallback: {}", 
                request.getPaymentId(), tenantId, fallbackClearingSystem);

        try {
            // Validate tenant context
            if (!tenantId.equals(request.getTenantId())) {
                log.warn("Tenant ID mismatch: header={}, request={}", tenantId, request.getTenantId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Get routing decision with fallback
            RoutingDecision decision = routingService.getRoutingDecisionWithFallback(request, fallbackClearingSystem);
            
            log.info("Routing decision made for payment: {} - clearing system: {}, priority: {}", 
                    request.getPaymentId(), decision.getClearingSystem(), decision.getPriority());

            return ResponseEntity.ok(decision);

        } catch (Exception e) {
            log.error("Error getting routing decision with fallback for payment: {}", request.getPaymentId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get routing statistics
     * 
     * @param tenantId Tenant ID from header
     * @param correlationId Correlation ID for tracing
     * @return Routing statistics
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Get routing statistics",
        description = "Get routing service statistics including rule counts and cache metrics"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = RoutingStatistics.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<RoutingStatistics> getRoutingStatistics(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "Tenant ID") String tenantId,
            @RequestHeader(value = "X-Correlation-ID", required = false) @Parameter(description = "Correlation ID") String correlationId) {
        
        log.info("Getting routing statistics for tenant: {}", tenantId);

        try {
            var serviceStats = routingService.getRoutingStatistics();
            
            // Convert service statistics to API DTO
            RoutingStatistics apiStats = new RoutingStatistics();
            apiStats.setTotalRules(serviceStats.getTotalRules());
            apiStats.setActiveRules(serviceStats.getActiveRules());
            apiStats.setCacheSize(serviceStats.getCacheSize());
            apiStats.setTotalDecisions(serviceStats.getTotalDecisions());
            apiStats.setCacheHitRate(serviceStats.getCacheHitRate());
            apiStats.setAverageDecisionTime(serviceStats.getAverageDecisionTime());
            
            return ResponseEntity.ok(apiStats);

        } catch (Exception e) {
            log.error("Error getting routing statistics for tenant: {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clear routing cache for specific payment
     * 
     * @param paymentId Payment ID
     * @param tenantId Tenant ID from header
     * @param correlationId Correlation ID for tracing
     * @return Success response
     */
    @DeleteMapping("/cache/{paymentId}")
    @Operation(
        summary = "Clear routing cache",
        description = "Clear routing cache for a specific payment"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Cache cleared successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Map<String, String>> clearRoutingCache(
            @PathVariable("paymentId") @Parameter(description = "Payment ID") String paymentId,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "Tenant ID") String tenantId,
            @RequestHeader(value = "X-Correlation-ID", required = false) @Parameter(description = "Correlation ID") String correlationId) {
        
        log.info("Clearing routing cache for payment: {} with tenant: {}", paymentId, tenantId);

        try {
            routingService.clearRoutingCache(paymentId);
            return ResponseEntity.ok(Map.of("message", "Cache cleared successfully", "paymentId", paymentId));

        } catch (Exception e) {
            log.error("Error clearing routing cache for payment: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clear all routing cache
     * 
     * @param tenantId Tenant ID from header
     * @param correlationId Correlation ID for tracing
     * @return Success response
     */
    @DeleteMapping("/cache")
    @Operation(
        summary = "Clear all routing cache",
        description = "Clear all routing cache entries"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "All cache cleared successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Map<String, String>> clearAllRoutingCache(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "Tenant ID") String tenantId,
            @RequestHeader(value = "X-Correlation-ID", required = false) @Parameter(description = "Correlation ID") String correlationId) {
        
        log.info("Clearing all routing cache for tenant: {}", tenantId);

        try {
            routingService.clearAllRoutingCache();
            return ResponseEntity.ok(Map.of("message", "All cache cleared successfully"));

        } catch (Exception e) {
            log.error("Error clearing all routing cache for tenant: {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Error Response DTO
     */
    @Schema(description = "Error response")
    public static class ErrorResponse {
        @Schema(description = "Error code")
        private String code;
        
        @Schema(description = "Error message")
        private String message;
        
        @Schema(description = "Correlation ID")
        private String correlationId;
        
        @Schema(description = "Timestamp")
        private String timestamp;

        // Constructors, getters, setters
        public ErrorResponse() {}

        public ErrorResponse(String code, String message, String correlationId, String timestamp) {
            this.code = code;
            this.message = message;
            this.correlationId = correlationId;
            this.timestamp = timestamp;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Routing Statistics DTO
     */
    @Schema(description = "Routing statistics")
    public static class RoutingStatistics {
        @Schema(description = "Total number of routing rules")
        private Long totalRules;
        
        @Schema(description = "Number of active routing rules")
        private Long activeRules;
        
        @Schema(description = "Cache size")
        private Long cacheSize;
        
        @Schema(description = "Total decisions made")
        private Long totalDecisions;
        
        @Schema(description = "Cache hit rate")
        private Double cacheHitRate;
        
        @Schema(description = "Average decision time")
        private Double averageDecisionTime;
        
        @Schema(description = "Timestamp")
        private String timestamp;

        // Constructors, getters, setters
        public RoutingStatistics() {}

        public RoutingStatistics(Long totalRules, Long activeRules, Long cacheSize, String timestamp) {
            this.totalRules = totalRules;
            this.activeRules = activeRules;
            this.cacheSize = cacheSize;
            this.timestamp = timestamp;
        }

        public Long getTotalRules() { return totalRules; }
        public void setTotalRules(Long totalRules) { this.totalRules = totalRules; }
        
        public Long getActiveRules() { return activeRules; }
        public void setActiveRules(Long activeRules) { this.activeRules = activeRules; }
        
        public Long getCacheSize() { return cacheSize; }
        public void setCacheSize(Long cacheSize) { this.cacheSize = cacheSize; }
        
        public Long getTotalDecisions() { return totalDecisions; }
        public void setTotalDecisions(Long totalDecisions) { this.totalDecisions = totalDecisions; }
        
        public Double getCacheHitRate() { return cacheHitRate; }
        public void setCacheHitRate(Double cacheHitRate) { this.cacheHitRate = cacheHitRate; }
        
        public Double getAverageDecisionTime() { return averageDecisionTime; }
        public void setAverageDecisionTime(Double averageDecisionTime) { this.averageDecisionTime = averageDecisionTime; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
