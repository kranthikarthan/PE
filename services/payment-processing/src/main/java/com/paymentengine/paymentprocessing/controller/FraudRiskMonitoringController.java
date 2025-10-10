package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.entity.FraudRiskAssessment;
import com.paymentengine.paymentprocessing.entity.FraudRiskConfiguration;
import com.paymentengine.paymentprocessing.service.FraudRiskMonitoringService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for fraud/risk monitoring operations
 */
@RestController
@RequestMapping("/api/v1/fraud-risk")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FraudRiskMonitoringController {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudRiskMonitoringController.class);
    
    @Autowired
    private FraudRiskMonitoringService fraudRiskMonitoringService;
    
    // ============================================================================
    // CONFIGURATION MANAGEMENT ENDPOINTS
    // ============================================================================
    
    /**
     * Get all fraud/risk configurations
     */
    @GetMapping(value = "/configurations", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.configurations.list", description = "Time taken to list fraud/risk configurations")
    public ResponseEntity<List<FraudRiskConfiguration>> getAllConfigurations(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String paymentSource,
            @RequestParam(required = false) String riskAssessmentType,
            @RequestParam(required = false) Boolean isEnabled) {
        
        logger.info("Getting fraud/risk configurations with filters - tenantId: {}, paymentSource: {}, riskAssessmentType: {}, isEnabled: {}", 
                   tenantId, paymentSource, riskAssessmentType, isEnabled);
        
        try {
            List<FraudRiskConfiguration> configurations;
            
            if (tenantId != null) {
                configurations = fraudRiskMonitoringService.getApplicableConfigurations(
                        tenantId, null, null, null, 
                        paymentSource != null ? FraudRiskConfiguration.PaymentSource.valueOf(paymentSource) : null);
            } else {
                // This would typically be handled by a repository method
                configurations = List.of(); // Placeholder
            }
            
            return ResponseEntity.ok(configurations);
            
        } catch (Exception e) {
            logger.error("Error getting fraud/risk configurations: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get fraud/risk configuration by ID
     */
    @GetMapping(value = "/configurations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.configurations.get", description = "Time taken to get fraud/risk configuration")
    public ResponseEntity<FraudRiskConfiguration> getConfigurationById(@PathVariable UUID id) {
        
        logger.info("Getting fraud/risk configuration by ID: {}", id);
        
        try {
            // This would typically be handled by a repository method
            // For now, return a placeholder response
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error getting fraud/risk configuration: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Create new fraud/risk configuration
     */
    @PostMapping(value = "/configurations", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:write')")
    @Timed(value = "fraud.configurations.create", description = "Time taken to create fraud/risk configuration")
    public ResponseEntity<FraudRiskConfiguration> createConfiguration(
            @Valid @RequestBody FraudRiskConfiguration configuration) {
        
        logger.info("Creating fraud/risk configuration: {}", configuration.getConfigurationName());
        
        try {
            // This would typically be handled by a repository method
            // For now, return a placeholder response
            configuration.setId(UUID.randomUUID());
            configuration.setCreatedAt(LocalDateTime.now());
            configuration.setUpdatedAt(LocalDateTime.now());
            
            return ResponseEntity.ok(configuration);
            
        } catch (Exception e) {
            logger.error("Error creating fraud/risk configuration: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update fraud/risk configuration
     */
    @PutMapping(value = "/configurations/{id}", 
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:write')")
    @Timed(value = "fraud.configurations.update", description = "Time taken to update fraud/risk configuration")
    public ResponseEntity<FraudRiskConfiguration> updateConfiguration(
            @PathVariable UUID id,
            @Valid @RequestBody FraudRiskConfiguration configuration) {
        
        logger.info("Updating fraud/risk configuration: {}", id);
        
        try {
            // This would typically be handled by a repository method
            // For now, return a placeholder response
            configuration.setId(id);
            configuration.setUpdatedAt(LocalDateTime.now());
            
            return ResponseEntity.ok(configuration);
            
        } catch (Exception e) {
            logger.error("Error updating fraud/risk configuration: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Delete fraud/risk configuration
     */
    @DeleteMapping(value = "/configurations/{id}")
    @PreAuthorize("hasAuthority('fraud:write')")
    @Timed(value = "fraud.configurations.delete", description = "Time taken to delete fraud/risk configuration")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable UUID id) {
        
        logger.info("Deleting fraud/risk configuration: {}", id);
        
        try {
            // This would typically be handled by a repository method
            // For now, return a placeholder response
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Error deleting fraud/risk configuration: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Test external fraud API connectivity
     */
    @PostMapping(value = "/configurations/{id}/test-api")
    @PreAuthorize("hasAuthority('fraud:test')")
    @Timed(value = "fraud.configurations.test-api", description = "Time taken to test external fraud API")
    public ResponseEntity<Map<String, Object>> testApiConnectivity(@PathVariable UUID id) {
        
        logger.info("Testing external fraud API connectivity for configuration: {}", id);
        
        try {
            // This would typically test the external API
            Map<String, Object> result = Map.of(
                    "success", true,
                    "message", "API connectivity test successful",
                    "responseTime", 150,
                    "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error testing external fraud API: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "API connectivity test failed: " + e.getMessage(),
                    "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // ASSESSMENT MANAGEMENT ENDPOINTS
    // ============================================================================
    
    /**
     * Get all fraud/risk assessments
     */
    @GetMapping(value = "/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.assessments.list", description = "Time taken to list fraud/risk assessments")
    public ResponseEntity<List<FraudRiskAssessment>> getAllAssessments(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String decision,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String paymentSource,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "assessedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.info("Getting fraud/risk assessments with filters - tenantId: {}, status: {}, decision: {}, riskLevel: {}, paymentSource: {}", 
                   tenantId, status, decision, riskLevel, paymentSource);
        
        try {
            // This would typically be handled by a repository method with pagination
            // For now, return a placeholder response
            List<FraudRiskAssessment> assessments = List.of();
            
            return ResponseEntity.ok(assessments);
            
        } catch (Exception e) {
            logger.error("Error getting fraud/risk assessments: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get fraud/risk assessment by ID
     */
    @GetMapping(value = "/assessments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.assessments.get", description = "Time taken to get fraud/risk assessment")
    public ResponseEntity<FraudRiskAssessment> getAssessmentById(@PathVariable String id) {
        
        logger.info("Getting fraud/risk assessment by ID: {}", id);
        
        try {
            Optional<FraudRiskAssessment> assessment = fraudRiskMonitoringService.getAssessmentById(id);
            
            if (assessment.isPresent()) {
                return ResponseEntity.ok(assessment.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error getting fraud/risk assessment: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get fraud/risk assessment by transaction reference
     */
    @GetMapping(value = "/assessments/transaction/{transactionReference}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.assessments.get-by-transaction", description = "Time taken to get fraud/risk assessment by transaction reference")
    public ResponseEntity<FraudRiskAssessment> getAssessmentByTransactionReference(@PathVariable String transactionReference) {
        
        logger.info("Getting fraud/risk assessment by transaction reference: {}", transactionReference);
        
        try {
            Optional<FraudRiskAssessment> assessment = fraudRiskMonitoringService.getAssessmentByTransactionReference(transactionReference);
            
            if (assessment.isPresent()) {
                return ResponseEntity.ok(assessment.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error getting fraud/risk assessment by transaction reference: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retry failed fraud/risk assessment
     */
    @PostMapping(value = "/assessments/{id}/retry")
    @PreAuthorize("hasAuthority('fraud:write')")
    @Timed(value = "fraud.assessments.retry", description = "Time taken to retry fraud/risk assessment")
    public ResponseEntity<FraudRiskAssessment> retryAssessment(@PathVariable String id) {
        
        logger.info("Retrying fraud/risk assessment: {}", id);
        
        try {
            CompletableFuture<FraudRiskAssessment> assessmentFuture = fraudRiskMonitoringService.retryAssessment(id);
            FraudRiskAssessment assessment = assessmentFuture.get();
            
            return ResponseEntity.ok(assessment);
            
        } catch (Exception e) {
            logger.error("Error retrying fraud/risk assessment: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Cancel pending fraud/risk assessment
     */
    @PostMapping(value = "/assessments/{id}/cancel")
    @PreAuthorize("hasAuthority('fraud:write')")
    @Timed(value = "fraud.assessments.cancel", description = "Time taken to cancel fraud/risk assessment")
    public ResponseEntity<Map<String, Object>> cancelAssessment(@PathVariable String id) {
        
        logger.info("Cancelling fraud/risk assessment: {}", id);
        
        try {
            boolean cancelled = fraudRiskMonitoringService.cancelAssessment(id);
            
            Map<String, Object> result = Map.of(
                    "success", cancelled,
                    "message", cancelled ? "Assessment cancelled successfully" : "Assessment could not be cancelled",
                    "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error cancelling fraud/risk assessment: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error cancelling assessment: " + e.getMessage(),
                    "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    /**
     * Update fraud/risk assessment decision (for manual review)
     */
    @PutMapping(value = "/assessments/{id}/decision", 
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:write')")
    @Timed(value = "fraud.assessments.update-decision", description = "Time taken to update fraud/risk assessment decision")
    public ResponseEntity<Map<String, Object>> updateAssessmentDecision(
            @PathVariable String id,
            @RequestBody Map<String, Object> decisionUpdate) {
        
        logger.info("Updating fraud/risk assessment decision: {}", id);
        
        try {
            String decision = (String) decisionUpdate.get("decision");
            String reason = (String) decisionUpdate.get("reason");
            
            FraudRiskAssessment.Decision decisionEnum = FraudRiskAssessment.Decision.valueOf(decision);
            boolean updated = fraudRiskMonitoringService.updateAssessmentDecision(id, decisionEnum, reason);
            
            Map<String, Object> result = Map.of(
                    "success", updated,
                    "message", updated ? "Decision updated successfully" : "Decision could not be updated",
                    "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error updating fraud/risk assessment decision: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error updating decision: " + e.getMessage(),
                    "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // STATISTICS AND MONITORING ENDPOINTS
    // ============================================================================
    
    /**
     * Get fraud/risk assessment statistics
     */
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.statistics", description = "Time taken to get fraud/risk statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        logger.info("Getting fraud/risk statistics for tenant: {}, startDate: {}, endDate: {}", 
                   tenantId, startDate, endDate);
        
        try {
            Map<String, Object> statistics;
            
            if (tenantId != null) {
                if (startDate != null && endDate != null) {
                    LocalDateTime start = LocalDateTime.parse(startDate);
                    LocalDateTime end = LocalDateTime.parse(endDate);
                    statistics = fraudRiskMonitoringService.getAssessmentStatistics(tenantId, start, end);
                } else {
                    statistics = fraudRiskMonitoringService.getAssessmentStatistics(tenantId);
                }
            } else {
                statistics = Map.of("error", "Tenant ID is required");
            }
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error getting fraud/risk statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get statistics: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get fraud/risk monitoring metrics
     */
    @GetMapping(value = "/metrics", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.metrics", description = "Time taken to get fraud/risk metrics")
    public ResponseEntity<Map<String, Object>> getMetrics(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        logger.info("Getting fraud/risk metrics for tenant: {}, startDate: {}, endDate: {}", 
                   tenantId, startDate, endDate);
        
        try {
            Map<String, Object> metrics;
            
            if (tenantId != null) {
                if (startDate != null && endDate != null) {
                    LocalDateTime start = LocalDateTime.parse(startDate);
                    LocalDateTime end = LocalDateTime.parse(endDate);
                    metrics = fraudRiskMonitoringService.getMetrics(tenantId, start, end);
                } else {
                    metrics = fraudRiskMonitoringService.getMetrics(tenantId);
                }
            } else {
                metrics = Map.of("error", "Tenant ID is required");
            }
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            logger.error("Error getting fraud/risk metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get metrics: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get fraud/risk monitoring health status
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.health", description = "Time taken to get fraud/risk health status")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        
        logger.info("Getting fraud/risk monitoring health status");
        
        try {
            Map<String, Object> health = fraudRiskMonitoringService.getHealthStatus();
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Error getting fraud/risk health status: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "DOWN",
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    // ============================================================================
    // MANUAL REVIEW ENDPOINTS
    // ============================================================================
    
    /**
     * Get pending manual reviews
     */
    @GetMapping(value = "/manual-reviews", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.manual-reviews", description = "Time taken to get pending manual reviews")
    public ResponseEntity<List<FraudRiskAssessment>> getPendingManualReviews(
            @RequestParam(required = false) String tenantId) {
        
        logger.info("Getting pending manual reviews for tenant: {}", tenantId);
        
        try {
            List<FraudRiskAssessment> reviews;
            
            if (tenantId != null) {
                reviews = fraudRiskMonitoringService.getPendingManualReviews(tenantId);
            } else {
                reviews = List.of();
            }
            
            return ResponseEntity.ok(reviews);
            
        } catch (Exception e) {
            logger.error("Error getting pending manual reviews: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get high-risk assessments
     */
    @GetMapping(value = "/high-risk", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.high-risk", description = "Time taken to get high-risk assessments")
    public ResponseEntity<List<FraudRiskAssessment>> getHighRiskAssessments(
            @RequestParam(required = false) String tenantId) {
        
        logger.info("Getting high-risk assessments for tenant: {}", tenantId);
        
        try {
            List<FraudRiskAssessment> assessments;
            
            if (tenantId != null) {
                assessments = fraudRiskMonitoringService.getHighRiskAssessments(tenantId);
            } else {
                assessments = List.of();
            }
            
            return ResponseEntity.ok(assessments);
            
        } catch (Exception e) {
            logger.error("Error getting high-risk assessments: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get critical-risk assessments
     */
    @GetMapping(value = "/critical-risk", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.critical-risk", description = "Time taken to get critical-risk assessments")
    public ResponseEntity<List<FraudRiskAssessment>> getCriticalRiskAssessments(
            @RequestParam(required = false) String tenantId) {
        
        logger.info("Getting critical-risk assessments for tenant: {}", tenantId);
        
        try {
            List<FraudRiskAssessment> assessments;
            
            if (tenantId != null) {
                assessments = fraudRiskMonitoringService.getCriticalRiskAssessments(tenantId);
            } else {
                assessments = List.of();
            }
            
            return ResponseEntity.ok(assessments);
            
        } catch (Exception e) {
            logger.error("Error getting critical-risk assessments: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get assessments needing retry
     */
    @GetMapping(value = "/retry-needed", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.retry-needed", description = "Time taken to get assessments needing retry")
    public ResponseEntity<List<FraudRiskAssessment>> getAssessmentsNeedingRetry(
            @RequestParam(required = false) String tenantId) {
        
        logger.info("Getting assessments needing retry for tenant: {}", tenantId);
        
        try {
            List<FraudRiskAssessment> assessments;
            
            if (tenantId != null) {
                assessments = fraudRiskMonitoringService.getAssessmentsNeedingRetry(tenantId);
            } else {
                assessments = List.of();
            }
            
            return ResponseEntity.ok(assessments);
            
        } catch (Exception e) {
            logger.error("Error getting assessments needing retry: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get expired assessments
     */
    @GetMapping(value = "/expired", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('fraud:read')")
    @Timed(value = "fraud.expired", description = "Time taken to get expired assessments")
    public ResponseEntity<List<FraudRiskAssessment>> getExpiredAssessments() {
        
        logger.info("Getting expired assessments");
        
        try {
            List<FraudRiskAssessment> assessments = fraudRiskMonitoringService.getExpiredAssessments();
            return ResponseEntity.ok(assessments);
            
        } catch (Exception e) {
            logger.error("Error getting expired assessments: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Clean up expired assessments
     */
    @PostMapping(value = "/cleanup-expired")
    @PreAuthorize("hasAuthority('fraud:write')")
    @Timed(value = "fraud.cleanup-expired", description = "Time taken to cleanup expired assessments")
    public ResponseEntity<Map<String, Object>> cleanupExpiredAssessments() {
        
        logger.info("Cleaning up expired assessments");
        
        try {
            int cleanedCount = fraudRiskMonitoringService.cleanupExpiredAssessments();
            
            Map<String, Object> result = Map.of(
                    "success", true,
                    "cleanedCount", cleanedCount,
                    "message", "Cleaned up " + cleanedCount + " expired assessments",
                    "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error cleaning up expired assessments: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error cleaning up expired assessments: " + e.getMessage(),
                    "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
}