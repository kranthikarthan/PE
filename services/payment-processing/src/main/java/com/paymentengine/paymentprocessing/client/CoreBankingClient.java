package com.paymentengine.paymentprocessing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign client for communicating with Core Banking Service
 */
@FeignClient(
    name = "core-banking-service",
    url = "${services.core-banking.url:http://localhost:8081}",
    path = "/core-banking"
)
public interface CoreBankingClient {
    
    /**
     * Validate user credentials
     */
    @PostMapping("/api/v1/auth/validate-credentials")
    Map<String, Object> validateUserCredentials(
        @RequestParam("username") String username,
        @RequestParam("password") String password
    );
    
    /**
     * Get user information
     */
    @GetMapping("/api/v1/users/{userId}")
    Map<String, Object> getUserInfo(@PathVariable("userId") String userId);
    
    /**
     * Update user password
     */
    @PutMapping("/api/v1/users/{userId}/password")
    void updateUserPassword(
        @PathVariable("userId") String userId,
        @RequestBody Map<String, String> passwordData
    );
    
    /**
     * Get user permissions
     */
    @GetMapping("/api/v1/users/{userId}/permissions")
    Map<String, Object> getUserPermissions(@PathVariable("userId") String userId);
    
    /**
     * Check user status
     */
    @GetMapping("/api/v1/users/{userId}/status")
    Map<String, Object> getUserStatus(@PathVariable("userId") String userId);
    
    /**
     * Get dashboard statistics
     */
    @GetMapping("/api/v1/dashboard/stats")
    Map<String, Object> getDashboardStats();
    
    /**
     * Get transaction volume data
     */
    @GetMapping("/api/v1/dashboard/transaction-volume")
    Map<String, Object> getTransactionVolumeData(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate,
        @RequestParam(value = "granularity", defaultValue = "day") String granularity
    );
    
    /**
     * Get transaction status distribution
     */
    @GetMapping("/api/v1/dashboard/transaction-status")
    Map<String, Object> getTransactionStatusDistribution();
    
    /**
     * Get payment type statistics
     */
    @GetMapping("/api/v1/dashboard/payment-types")
    Map<String, Object> getPaymentTypeStatistics();
    
    /**
     * Health check
     */
    @GetMapping("/actuator/health")
    Map<String, Object> healthCheck();
}