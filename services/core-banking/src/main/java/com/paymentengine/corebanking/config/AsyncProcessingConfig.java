package com.paymentengine.corebanking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async Processing Configuration for High TPS
 * 
 * Optimized thread pool configurations for handling 2000+ TPS
 * with proper resource management and performance tuning.
 */
@Configuration
@EnableAsync
public class AsyncProcessingConfig {

    /**
     * Payment Processing Executor
     * Optimized for high-throughput payment processing
     */
    @Bean(name = "paymentProcessingExecutor")
    public Executor paymentProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size - number of threads to keep in pool
        executor.setCorePoolSize(50);
        
        // Maximum pool size - maximum number of threads
        executor.setMaxPoolSize(200);
        
        // Queue capacity - number of tasks to queue before creating new threads
        executor.setQueueCapacity(1000);
        
        // Thread name prefix for monitoring
        executor.setThreadNamePrefix("PaymentProcessor-");
        
        // Rejection policy - what to do when queue is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Keep alive time for idle threads
        executor.setKeepAliveSeconds(60);
        
        // Allow core threads to timeout
        executor.setAllowCoreThreadTimeOut(true);
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // Maximum time to wait for tasks to complete
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Notification Executor
     * Optimized for sending notifications and alerts
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Notification-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Audit Logging Executor
     * Optimized for audit trail and logging operations
     */
    @Bean(name = "auditLoggingExecutor")
    public Executor auditLoggingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("AuditLog-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Batch Processing Executor
     * Optimized for batch operations and bulk processing
     */
    @Bean(name = "batchProcessingExecutor")
    public Executor batchProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(30);
        executor.setMaxPoolSize(150);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("BatchProcessor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }

    /**
     * ISO 20022 Processing Executor
     * Optimized for ISO 20022 message processing
     */
    @Bean(name = "iso20022ProcessingExecutor")
    public Executor iso20022ProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(25);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(800);
        executor.setThreadNamePrefix("ISO20022-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Fraud Detection Executor
     * Optimized for fraud detection and risk assessment
     */
    @Bean(name = "fraudDetectionExecutor")
    public Executor fraudDetectionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(15);
        executor.setMaxPoolSize(75);
        executor.setQueueCapacity(300);
        executor.setThreadNamePrefix("FraudDetection-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Scheduled Task Executor
     * Optimized for scheduled tasks and cron jobs
     */
    @Bean(name = "scheduledTaskExecutor")
    public ThreadPoolTaskScheduler scheduledTaskExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        scheduler.setPoolSize(20);
        scheduler.setThreadNamePrefix("ScheduledTask-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Database Operation Executor
     * Optimized for database operations and queries
     */
    @Bean(name = "databaseOperationExecutor")
    public Executor databaseOperationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(40);
        executor.setMaxPoolSize(160);
        executor.setQueueCapacity(1500);
        executor.setThreadNamePrefix("DatabaseOp-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * External API Call Executor
     * Optimized for external API calls and integrations
     */
    @Bean(name = "externalApiExecutor")
    public Executor externalApiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ExternalAPI-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Cache Operation Executor
     * Optimized for cache operations and Redis interactions
     */
    @Bean(name = "cacheOperationExecutor")
    public Executor cacheOperationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(15);
        executor.setMaxPoolSize(75);
        executor.setQueueCapacity(300);
        executor.setThreadNamePrefix("CacheOp-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }
}