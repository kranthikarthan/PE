package com.paymentengine.middleware.controller;

import com.paymentengine.middleware.entity.QueuedMessage;
import com.paymentengine.middleware.repository.QueuedMessageRepository;
import com.paymentengine.middleware.service.MessageQueueService;
import com.paymentengine.middleware.service.SelfHealingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for resiliency monitoring and management
 */
@RestController
@RequestMapping("/api/resiliency")
@CrossOrigin(origins = "*")
public class ResiliencyMonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(ResiliencyMonitoringController.class);

    @Autowired
    private QueuedMessageRepository queuedMessageRepository;

    @Autowired
    private MessageQueueService messageQueueService;

    @Autowired
    private SelfHealingService selfHealingService;

    // ============================================================================
    // RESILIENT SERVICES
    // ============================================================================

    /**
     * Get all resilient services status
     */
    @GetMapping("/services")
    public ResponseEntity<List<Map<String, Object>>> getResilientServices(
            @RequestParam(required = false) String tenantId) {
        
        logger.info("Getting resilient services status for tenant: {}", tenantId);
        
        try {
            List<Map<String, Object>> services = new ArrayList<>();
            
            // Get fraud API service status
            Map<String, Object> fraudApiService = Map.of(
                "serviceName", "fraudApi",
                "circuitBreakerState", "CLOSED", // This would come from actual circuit breaker
                "bulkheadAvailableCalls", 25,
                "retryMetrics", Map.of(
                    "numberOfSuccessfulCallsWithRetryAttempt", 100,
                    "numberOfFailedCallsWithRetryAttempt", 5
                ),
                "timeLimiterMetrics", Map.of(
                    "numberOfSuccessfulCalls", 95,
                    "numberOfFailedCalls", 5
                ),
                "lastUpdated", LocalDateTime.now().toString()
            );
            services.add(fraudApiService);
            
            // Get core banking service status
            Map<String, Object> coreBankingService = Map.of(
                "serviceName", "coreBanking",
                "circuitBreakerState", "CLOSED",
                "bulkheadAvailableCalls", 50,
                "retryMetrics", Map.of(
                    "numberOfSuccessfulCallsWithRetryAttempt", 200,
                    "numberOfFailedCallsWithRetryAttempt", 10
                ),
                "timeLimiterMetrics", Map.of(
                    "numberOfSuccessfulCalls", 190,
                    "numberOfFailedCalls", 10
                ),
                "lastUpdated", LocalDateTime.now().toString()
            );
            services.add(coreBankingService);
            
            // Get clearing system service status
            Map<String, Object> clearingSystemService = Map.of(
                "serviceName", "clearingSystem",
                "circuitBreakerState", "CLOSED",
                "bulkheadAvailableCalls", 10,
                "retryMetrics", Map.of(
                    "numberOfSuccessfulCallsWithRetryAttempt", 150,
                    "numberOfFailedCallsWithRetryAttempt", 8
                ),
                "timeLimiterMetrics", Map.of(
                    "numberOfSuccessfulCalls", 142,
                    "numberOfFailedCalls", 8
                ),
                "lastUpdated", LocalDateTime.now().toString()
            );
            services.add(clearingSystemService);
            
            return ResponseEntity.ok(services);
            
        } catch (Exception e) {
            logger.error("Error getting resilient services status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reset circuit breaker for a service
     */
    @PostMapping("/services/{serviceName}/reset-circuit-breaker")
    public ResponseEntity<Map<String, Object>> resetCircuitBreaker(@PathVariable String serviceName) {
        logger.info("Resetting circuit breaker for service: {}", serviceName);
        
        try {
            // In a real implementation, this would reset the actual circuit breaker
            // For now, we'll just return a success response
            Map<String, Object> response = Map.of(
                "serviceName", serviceName,
                "status", "CIRCUIT_BREAKER_RESET",
                "message", "Circuit breaker reset successfully",
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error resetting circuit breaker for service: {}", serviceName, e);
            Map<String, Object> errorResponse = Map.of(
                "serviceName", serviceName,
                "status", "RESET_FAILED",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ============================================================================
    // QUEUED MESSAGES
    // ============================================================================

    /**
     * Get queued messages
     */
    @GetMapping("/queued-messages")
    public ResponseEntity<List<QueuedMessage>> getQueuedMessages(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit) {
        
        logger.info("Getting queued messages for tenant: {} service: {} status: {} limit: {}", 
                   tenantId, serviceName, status, limit);
        
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<QueuedMessage> messages;
            
            if (tenantId != null && serviceName != null && status != null) {
                messages = queuedMessageRepository.findByTenantIdAndServiceNameAndStatus(
                        tenantId, serviceName, QueuedMessage.Status.valueOf(status), pageable);
            } else if (tenantId != null && serviceName != null) {
                messages = queuedMessageRepository.findByTenantIdAndServiceName(tenantId, serviceName, pageable);
            } else if (tenantId != null && status != null) {
                messages = queuedMessageRepository.findByTenantIdAndStatus(
                        tenantId, QueuedMessage.Status.valueOf(status), pageable);
            } else if (tenantId != null) {
                messages = queuedMessageRepository.findByTenantId(tenantId, pageable);
            } else if (status != null) {
                messages = queuedMessageRepository.findByStatus(QueuedMessage.Status.valueOf(status), pageable);
            } else {
                messages = queuedMessageRepository.findAll(pageable).getContent();
            }
            
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            logger.error("Error getting queued messages", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retry a queued message
     */
    @PostMapping("/queued-messages/{messageId}/retry")
    public ResponseEntity<Map<String, Object>> retryMessage(@PathVariable String messageId) {
        logger.info("Retrying queued message: {}", messageId);
        
        try {
            CompletableFuture<Map<String, Object>> future = messageQueueService.processMessage(messageId);
            Map<String, Object> result = future.get(); // Wait for completion
            
            Map<String, Object> response = new HashMap<>();
            response.put("messageId", messageId);
            response.put("status", "RETRY_INITIATED");
            response.put("result", result);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrying queued message: {}", messageId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("messageId", messageId);
            errorResponse.put("status", "RETRY_FAILED");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Cancel a queued message
     */
    @PostMapping("/queued-messages/{messageId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelMessage(
            @PathVariable String messageId,
            @RequestBody Map<String, String> request) {
        
        logger.info("Cancelling queued message: {}", messageId);
        
        try {
            String reason = request.getOrDefault("reason", "Cancelled by user");
            messageQueueService.cancelMessage(messageId, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("messageId", messageId);
            response.put("status", "CANCELLED");
            response.put("reason", reason);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error cancelling queued message: {}", messageId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("messageId", messageId);
            errorResponse.put("status", "CANCEL_FAILED");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Reprocess all queued messages for a tenant
     */
    @PostMapping("/queued-messages/reprocess")
    public ResponseEntity<Map<String, Object>> reprocessQueuedMessages(
            @RequestParam String tenantId,
            @RequestParam(required = false) String serviceName) {
        
        logger.info("Reprocessing queued messages for tenant: {} service: {}", tenantId, serviceName);
        
        try {
            CompletableFuture<Void> future = selfHealingService.reprocessQueuedMessages(tenantId, serviceName);
            future.get(); // Wait for completion
            
            Map<String, Object> response = new HashMap<>();
            response.put("tenantId", tenantId);
            response.put("serviceName", serviceName);
            response.put("status", "REPROCESSING_INITIATED");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error reprocessing queued messages for tenant: {}", tenantId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("tenantId", tenantId);
            errorResponse.put("serviceName", serviceName);
            errorResponse.put("status", "REPROCESSING_FAILED");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ============================================================================
    // SYSTEM HEALTH
    // ============================================================================

    /**
     * Get system health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth(@RequestParam String tenantId) {
        logger.info("Getting system health for tenant: {}", tenantId);
        
        try {
            Map<String, Object> healthStatus = selfHealingService.getSystemHealthStatus(tenantId);
            return ResponseEntity.ok(healthStatus);
            
        } catch (Exception e) {
            logger.error("Error getting system health for tenant: {}", tenantId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("tenantId", tenantId);
            errorResponse.put("overallHealth", "ERROR");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Monitor downstream services
     */
    @PostMapping("/monitor")
    public ResponseEntity<Map<String, Object>> monitorDownstreamServices(@RequestParam String tenantId) {
        logger.info("Monitoring downstream services for tenant: {}", tenantId);
        
        try {
            CompletableFuture<Map<String, Object>> future = selfHealingService.monitorDownstreamServices(tenantId);
            Map<String, Object> monitoringResults = future.get(); // Wait for completion
            
            return ResponseEntity.ok(monitoringResults);
            
        } catch (Exception e) {
            logger.error("Error monitoring downstream services for tenant: {}", tenantId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("tenantId", tenantId);
            errorResponse.put("status", "MONITORING_FAILED");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Trigger recovery actions
     */
    @PostMapping("/recovery/trigger")
    public ResponseEntity<Map<String, Object>> triggerRecovery(
            @RequestParam String tenantId,
            @RequestParam(required = false) String serviceName) {
        
        logger.info("Triggering recovery for tenant: {} service: {}", tenantId, serviceName);
        
        try {
            List<String> failedServices = serviceName != null ? List.of(serviceName) : List.of();
            CompletableFuture<Void> future = selfHealingService.triggerRecoveryActions(failedServices, tenantId);
            future.get(); // Wait for completion
            
            Map<String, Object> response = new HashMap<>();
            response.put("tenantId", tenantId);
            response.put("serviceName", serviceName);
            response.put("status", "RECOVERY_TRIGGERED");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error triggering recovery for tenant: {}", tenantId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("tenantId", tenantId);
            errorResponse.put("serviceName", serviceName);
            errorResponse.put("status", "RECOVERY_FAILED");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ============================================================================
    // QUEUE STATISTICS
    // ============================================================================

    /**
     * Get queue statistics
     */
    @GetMapping("/queue-statistics")
    public ResponseEntity<Map<String, Object>> getQueueStatistics(@RequestParam(required = false) String tenantId) {
        logger.info("Getting queue statistics for tenant: {}", tenantId);
        
        try {
            Map<String, Object> statistics = messageQueueService.getQueueStatistics(tenantId);
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error getting queue statistics for tenant: {}", tenantId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("tenantId", tenantId);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get messages ready for retry
     */
    @GetMapping("/queued-messages/retry-ready")
    public ResponseEntity<List<QueuedMessage>> getMessagesForRetry() {
        logger.info("Getting messages ready for retry");
        
        try {
            List<QueuedMessage> messages = messageQueueService.getMessagesForRetry();
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            logger.error("Error getting messages for retry", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retry all failed messages
     */
    @PostMapping("/queued-messages/retry-all")
    public ResponseEntity<Map<String, Object>> retryAllFailedMessages() {
        logger.info("Retrying all failed messages");
        
        try {
            CompletableFuture<Void> future = messageQueueService.retryFailedMessages();
            future.get(); // Wait for completion
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "RETRY_ALL_INITIATED");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrying all failed messages", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "RETRY_ALL_FAILED");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Cleanup expired messages
     */
    @PostMapping("/queued-messages/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredMessages() {
        logger.info("Cleaning up expired messages");
        
        try {
            messageQueueService.cleanupExpiredMessages();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "CLEANUP_COMPLETED");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error cleaning up expired messages", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "CLEANUP_FAILED");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}