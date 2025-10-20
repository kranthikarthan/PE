package com.payments.operationsmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Operations Management Service Application
 * 
 * This service provides operations management capabilities including:
 * - Service health monitoring for all 22 microservices
 * - Circuit breaker management and control
 * - Feature flag management with Unleash integration
 * - Kubernetes pod management (restart, scale, logs)
 * - Error log aggregation and analysis
 * 
 * Port: 8021
 * Database: PostgreSQL + Redis (cache)
 * 
 * @author Payments Engine Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class OperationsManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperationsManagementServiceApplication.class, args);
    }
}
