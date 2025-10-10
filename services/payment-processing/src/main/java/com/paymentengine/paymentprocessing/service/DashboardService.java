package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.client.CoreBankingClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Dashboard service for aggregating business intelligence and metrics
 */
@Service
public class DashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    private final CoreBankingClient coreBankingClient;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    public DashboardService(
            CoreBankingClient coreBankingClient,
            RedisTemplate<String, Object> redisTemplate) {
        this.coreBankingClient = coreBankingClient;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Get comprehensive dashboard statistics
     */
    @CircuitBreaker(name = "dashboard-stats", fallbackMethod = "getDashboardStatsFallback")
    @Retry(name = "dashboard-stats")
    @Cacheable(value = "dashboard-stats", unless = "#result == null")
    public Map<String, Object> getDashboardStatistics() {
        logger.debug("Fetching dashboard statistics");
        
        try {
            // Fetch data from core banking service
            Map<String, Object> coreStats = coreBankingClient.getDashboardStats();
            
            // Enhance with additional metrics
            Map<String, Object> enhancedStats = new HashMap<>(coreStats);
            
            // Add real-time metrics
            enhancedStats.putAll(getRealTimeMetrics());
            
            // Add system health metrics
            enhancedStats.put("systemHealth", getSystemHealthOverview());
            
            // Add performance metrics
            enhancedStats.put("performance", getPerformanceMetrics(1));
            
            return enhancedStats;
            
        } catch (Exception e) {
            logger.error("Error fetching dashboard statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch dashboard statistics", e);
        }
    }
    
    /**
     * Get transaction volume data with different granularities
     */
    @CircuitBreaker(name = "transaction-volume", fallbackMethod = "getTransactionVolumeDataFallback")
    @Retry(name = "transaction-volume")
    @Cacheable(value = "transaction-volume", key = "#startDate + '_' + #endDate + '_' + #granularity")
    public Map<String, Object> getTransactionVolumeData(LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        logger.debug("Fetching transaction volume data from {} to {} with granularity {}", 
                    startDate, endDate, granularity);
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            
            Map<String, Object> volumeData = coreBankingClient.getTransactionVolumeData(
                startDate.format(formatter),
                endDate.format(formatter),
                granularity
            );
            
            // Enhance with additional calculations
            enhanceVolumeData(volumeData);
            
            return volumeData;
            
        } catch (Exception e) {
            logger.error("Error fetching transaction volume data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch transaction volume data", e);
        }
    }
    
    /**
     * Get transaction status distribution
     */
    @CircuitBreaker(name = "transaction-status", fallbackMethod = "getTransactionStatusDistributionFallback")
    @Retry(name = "transaction-status")
    @Cacheable(value = "transaction-status", unless = "#result == null")
    public Map<String, Object> getTransactionStatusDistribution() {
        logger.debug("Fetching transaction status distribution");
        
        try {
            Map<String, Object> statusData = coreBankingClient.getTransactionStatusDistribution();
            
            // Add percentage calculations
            enhanceStatusDistribution(statusData);
            
            return statusData;
            
        } catch (Exception e) {
            logger.error("Error fetching transaction status distribution: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch transaction status distribution", e);
        }
    }
    
    /**
     * Get payment type statistics
     */
    @CircuitBreaker(name = "payment-types", fallbackMethod = "getPaymentTypeStatisticsFallback")
    @Retry(name = "payment-types")
    @Cacheable(value = "payment-types", unless = "#result == null")
    public Map<String, Object> getPaymentTypeStatistics() {
        logger.debug("Fetching payment type statistics");
        
        try {
            Map<String, Object> paymentTypeData = coreBankingClient.getPaymentTypeStatistics();
            
            // Enhance with performance metrics
            enhancePaymentTypeData(paymentTypeData);
            
            return paymentTypeData;
            
        } catch (Exception e) {
            logger.error("Error fetching payment type statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch payment type statistics", e);
        }
    }
    
    /**
     * Get real-time metrics from Redis cache
     */
    public Map<String, Object> getRealTimeMetrics() {
        logger.debug("Fetching real-time metrics");
        
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get cached metrics from Redis
            Map<Object, Object> cachedMetrics = redisTemplate.opsForHash()
                .entries("real_time_metrics");
            
            cachedMetrics.forEach((key, value) -> metrics.put(key.toString(), value));
            
            // Add current timestamp
            metrics.put("lastUpdated", LocalDateTime.now().toString());
            
            // Add system uptime
            long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
            metrics.put("systemUptimeMs", uptimeMs);
            
            return metrics;
            
        } catch (Exception e) {
            logger.warn("Error fetching real-time metrics: {}", e.getMessage());
            return getDefaultRealTimeMetrics();
        }
    }
    
    /**
     * Get system health overview
     */
    public Map<String, Object> getSystemHealthOverview() {
        logger.debug("Fetching system health overview");
        
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check core banking service health
            CompletableFuture<Map<String, Object>> coreBankingHealth = 
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return coreBankingClient.healthCheck();
                    } catch (Exception e) {
                        return Map.of("status", "DOWN", "error", e.getMessage());
                    }
                });
            
            // Check Redis health
            CompletableFuture<String> redisHealth = 
                CompletableFuture.supplyAsync(this::checkRedisHealth);
            
            // Wait for all health checks
            Map<String, Object> coreBankingResult = coreBankingHealth.get();
            String redisResult = redisHealth.get();
            
            health.put("coreBanking", coreBankingResult);
            health.put("redis", Map.of("status", redisResult));
            health.put("payment-processing", Map.of("status", "UP"));
            
            // Overall health status
            boolean allHealthy = "UP".equals(coreBankingResult.get("status")) && 
                               "UP".equals(redisResult);
            
            health.put("overall", Map.of(
                "status", allHealthy ? "UP" : "DEGRADED",
                "timestamp", LocalDateTime.now().toString()
            ));
            
            return health;
            
        } catch (Exception e) {
            logger.error("Error checking system health: {}", e.getMessage(), e);
            return Map.of(
                "overall", Map.of("status", "DOWN", "error", e.getMessage()),
                "timestamp", LocalDateTime.now().toString()
            );
        }
    }
    
    /**
     * Get performance metrics for specified time period
     */
    public Map<String, Object> getPerformanceMetrics(int hours) {
        logger.debug("Fetching performance metrics for last {} hours", hours);
        
        Map<String, Object> performance = new HashMap<>();
        
        try {
            // Get cached performance data
            String key = "performance_metrics_" + hours + "h";
            Map<Object, Object> cachedData = redisTemplate.opsForHash().entries(key);
            
            cachedData.forEach((k, v) -> performance.put(k.toString(), v));
            
            // Add current JVM metrics
            Runtime runtime = Runtime.getRuntime();
            performance.put("jvm", Map.of(
                "totalMemory", runtime.totalMemory(),
                "freeMemory", runtime.freeMemory(),
                "usedMemory", runtime.totalMemory() - runtime.freeMemory(),
                "maxMemory", runtime.maxMemory(),
                "processors", runtime.availableProcessors()
            ));
            
            return performance;
            
        } catch (Exception e) {
            logger.warn("Error fetching performance metrics: {}", e.getMessage());
            return getDefaultPerformanceMetrics();
        }
    }
    
    /**
     * Get error analytics
     */
    public Map<String, Object> getErrorAnalytics(int hours) {
        logger.debug("Fetching error analytics for last {} hours", hours);
        
        Map<String, Object> errors = new HashMap<>();
        
        try {
            // Get error data from cache
            String key = "error_analytics_" + hours + "h";
            Map<Object, Object> cachedData = redisTemplate.opsForHash().entries(key);
            
            cachedData.forEach((k, v) -> errors.put(k.toString(), v));
            
            if (errors.isEmpty()) {
                errors = getDefaultErrorAnalytics();
            }
            
            return errors;
            
        } catch (Exception e) {
            logger.warn("Error fetching error analytics: {}", e.getMessage());
            return getDefaultErrorAnalytics();
        }
    }
    
    // Helper methods
    
    private void enhanceVolumeData(Map<String, Object> volumeData) {
        // Add trend calculations, growth rates, etc.
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) volumeData.get("data");
        
        if (data != null && data.size() > 1) {
            // Calculate growth rate
            double firstValue = ((Number) data.get(0).get("value")).doubleValue();
            double lastValue = ((Number) data.get(data.size() - 1).get("value")).doubleValue();
            double growthRate = ((lastValue - firstValue) / firstValue) * 100;
            
            volumeData.put("growthRate", growthRate);
            volumeData.put("trend", growthRate > 0 ? "up" : "down");
        }
    }
    
    private void enhanceStatusDistribution(Map<String, Object> statusData) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) statusData.get("data");
        
        if (data != null) {
            int total = data.stream()
                .mapToInt(item -> ((Number) item.get("count")).intValue())
                .sum();
            
            data.forEach(item -> {
                int count = ((Number) item.get("count")).intValue();
                double percentage = total > 0 ? (double) count / total * 100 : 0;
                item.put("percentage", Math.round(percentage * 100.0) / 100.0);
            });
            
            statusData.put("total", total);
        }
    }
    
    private void enhancePaymentTypeData(Map<String, Object> paymentTypeData) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) paymentTypeData.get("data");
        
        if (data != null) {
            // Calculate success rates for each payment type
            data.forEach(item -> {
                int successful = ((Number) item.getOrDefault("successful", 0)).intValue();
                int total = ((Number) item.getOrDefault("total", 0)).intValue();
                double successRate = total > 0 ? (double) successful / total * 100 : 0;
                item.put("successRate", Math.round(successRate * 100.0) / 100.0);
            });
        }
    }
    
    private String checkRedisHealth() {
        try {
            redisTemplate.opsForValue().set("health_check", "ping");
            String result = (String) redisTemplate.opsForValue().get("health_check");
            return "ping".equals(result) ? "UP" : "DOWN";
        } catch (Exception e) {
            logger.warn("Redis health check failed: {}", e.getMessage());
            return "DOWN";
        }
    }
    
    // Fallback methods
    
    public Map<String, Object> getDashboardStatsFallback(Exception e) {
        logger.warn("Using fallback for dashboard stats due to: {}", e.getMessage());
        return getDefaultDashboardStats();
    }
    
    public Map<String, Object> getTransactionVolumeDataFallback(LocalDateTime startDate, LocalDateTime endDate, String granularity, Exception e) {
        logger.warn("Using fallback for transaction volume data due to: {}", e.getMessage());
        return getDefaultVolumeData();
    }
    
    public Map<String, Object> getTransactionStatusDistributionFallback(Exception e) {
        logger.warn("Using fallback for transaction status distribution due to: {}", e.getMessage());
        return getDefaultStatusDistribution();
    }
    
    public Map<String, Object> getPaymentTypeStatisticsFallback(Exception e) {
        logger.warn("Using fallback for payment type statistics due to: {}", e.getMessage());
        return getDefaultPaymentTypeStats();
    }
    
    // Default data methods
    
    private Map<String, Object> getDefaultDashboardStats() {
        return Map.of(
            "totalTransactions", 0,
            "totalAmount", 0.0,
            "successfulTransactions", 0,
            "failedTransactions", 0,
            "pendingTransactions", 0,
            "averageTransactionAmount", 0.0,
            "transactionVolumeToday", 0.0,
            "activeAccounts", 0,
            "totalCustomers", 0,
            "fallback", true,
            "message", "Using cached or default data due to service unavailability"
        );
    }
    
    private Map<String, Object> getDefaultVolumeData() {
        List<Map<String, Object>> defaultData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            defaultData.add(Map.of(
                "date", LocalDateTime.now().minusDays(i).toString(),
                "value", 0,
                "count", 0
            ));
        }
        
        return Map.of(
            "data", defaultData,
            "fallback", true
        );
    }
    
    private Map<String, Object> getDefaultStatusDistribution() {
        return Map.of(
            "data", List.of(
                Map.of("status", "COMPLETED", "count", 0, "percentage", 0.0),
                Map.of("status", "PENDING", "count", 0, "percentage", 0.0),
                Map.of("status", "FAILED", "count", 0, "percentage", 0.0)
            ),
            "total", 0,
            "fallback", true
        );
    }
    
    private Map<String, Object> getDefaultPaymentTypeStats() {
        return Map.of(
            "data", List.of(
                Map.of("type", "ACH", "count", 0, "amount", 0.0, "successRate", 0.0),
                Map.of("type", "Wire", "count", 0, "amount", 0.0, "successRate", 0.0),
                Map.of("type", "RTP", "count", 0, "amount", 0.0, "successRate", 0.0)
            ),
            "fallback", true
        );
    }
    
    private Map<String, Object> getDefaultRealTimeMetrics() {
        return Map.of(
            "activeConnections", 0,
            "requestsPerSecond", 0.0,
            "averageResponseTime", 0.0,
            "errorRate", 0.0,
            "lastUpdated", LocalDateTime.now().toString(),
            "fallback", true
        );
    }
    
    private Map<String, Object> getDefaultPerformanceMetrics() {
        Runtime runtime = Runtime.getRuntime();
        
        return Map.of(
            "jvm", Map.of(
                "totalMemory", runtime.totalMemory(),
                "freeMemory", runtime.freeMemory(),
                "usedMemory", runtime.totalMemory() - runtime.freeMemory(),
                "maxMemory", runtime.maxMemory(),
                "processors", runtime.availableProcessors()
            ),
            "fallback", true
        );
    }
    
    private Map<String, Object> getDefaultErrorAnalytics() {
        return Map.of(
            "totalErrors", 0,
            "errorRate", 0.0,
            "topErrors", List.of(),
            "errorsByService", Map.of(),
            "fallback", true
        );
    }
}