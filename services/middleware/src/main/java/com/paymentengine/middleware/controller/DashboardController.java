package com.paymentengine.middleware.controller;

import com.paymentengine.middleware.service.DashboardService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Dashboard controller for business intelligence and reporting
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    private final DashboardService dashboardService;
    
    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    /**
     * Get dashboard statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('dashboard:read')")
    @Timed(value = "dashboard.stats", description = "Time taken to get dashboard statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        
        logger.debug("Getting dashboard statistics");
        
        try {
            Map<String, Object> stats = dashboardService.getDashboardStatistics();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting dashboard statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get transaction volume data for charts
     */
    @GetMapping("/transaction-volume")
    @PreAuthorize("hasAuthority('dashboard:read')")
    @Timed(value = "dashboard.transaction_volume", description = "Time taken to get transaction volume data")
    public ResponseEntity<Map<String, Object>> getTransactionVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "day") String granularity) {
        
        logger.debug("Getting transaction volume data from {} to {} with granularity {}", 
                    startDate, endDate, granularity);
        
        try {
            Map<String, Object> volumeData = dashboardService.getTransactionVolumeData(
                startDate, endDate, granularity);
            return ResponseEntity.ok(volumeData);
            
        } catch (Exception e) {
            logger.error("Error getting transaction volume data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get transaction status distribution
     */
    @GetMapping("/transaction-status")
    @PreAuthorize("hasAuthority('dashboard:read')")
    @Timed(value = "dashboard.transaction_status", description = "Time taken to get transaction status distribution")
    public ResponseEntity<Map<String, Object>> getTransactionStatusDistribution() {
        
        logger.debug("Getting transaction status distribution");
        
        try {
            Map<String, Object> statusData = dashboardService.getTransactionStatusDistribution();
            return ResponseEntity.ok(statusData);
            
        } catch (Exception e) {
            logger.error("Error getting transaction status distribution: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get payment type statistics
     */
    @GetMapping("/payment-types")
    @PreAuthorize("hasAuthority('dashboard:read')")
    @Timed(value = "dashboard.payment_types", description = "Time taken to get payment type statistics")
    public ResponseEntity<Map<String, Object>> getPaymentTypeStatistics() {
        
        logger.debug("Getting payment type statistics");
        
        try {
            Map<String, Object> paymentTypeData = dashboardService.getPaymentTypeStatistics();
            return ResponseEntity.ok(paymentTypeData);
            
        } catch (Exception e) {
            logger.error("Error getting payment type statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get real-time metrics
     */
    @GetMapping("/real-time-metrics")
    @PreAuthorize("hasAuthority('dashboard:read')")
    @Timed(value = "dashboard.real_time_metrics", description = "Time taken to get real-time metrics")
    public ResponseEntity<Map<String, Object>> getRealTimeMetrics() {
        
        logger.debug("Getting real-time metrics");
        
        try {
            Map<String, Object> metrics = dashboardService.getRealTimeMetrics();
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            logger.error("Error getting real-time metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get system health overview
     */
    @GetMapping("/system-health")
    @PreAuthorize("hasAuthority('dashboard:read')")
    @Timed(value = "dashboard.system_health", description = "Time taken to get system health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        
        logger.debug("Getting system health overview");
        
        try {
            Map<String, Object> health = dashboardService.getSystemHealthOverview();
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Error getting system health: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get performance metrics
     */
    @GetMapping("/performance")
    @PreAuthorize("hasAuthority('dashboard:read')")
    @Timed(value = "dashboard.performance", description = "Time taken to get performance metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @RequestParam(defaultValue = "1") int hours) {
        
        logger.debug("Getting performance metrics for last {} hours", hours);
        
        try {
            Map<String, Object> performance = dashboardService.getPerformanceMetrics(hours);
            return ResponseEntity.ok(performance);
            
        } catch (Exception e) {
            logger.error("Error getting performance metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get error analytics
     */
    @GetMapping("/errors")
    @PreAuthorize("hasAuthority('dashboard:read')")
    @Timed(value = "dashboard.errors", description = "Time taken to get error analytics")
    public ResponseEntity<Map<String, Object>> getErrorAnalytics(
            @RequestParam(defaultValue = "24") int hours) {
        
        logger.debug("Getting error analytics for last {} hours", hours);
        
        try {
            Map<String, Object> errors = dashboardService.getErrorAnalytics(hours);
            return ResponseEntity.ok(errors);
            
        } catch (Exception e) {
            logger.error("Error getting error analytics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}