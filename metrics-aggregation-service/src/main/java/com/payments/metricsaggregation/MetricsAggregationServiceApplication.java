package com.payments.metricsaggregation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Metrics Aggregation Service Application
 * 
 * This service provides real-time metrics aggregation and alert management for the payments engine.
 * Features include:
 * - Real-time metrics collection from all 22 microservices
 * - Time-series data storage with TimescaleDB
 * - Prometheus metrics integration
 * - Real-time dashboards and alerting
 * - Performance analytics and reporting
 * 
 * Port: 8022
 * Database: TimescaleDB (PostgreSQL extension) + Redis (cache)
 * 
 * @author Payments Engine Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class MetricsAggregationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricsAggregationServiceApplication.class, args);
    }
}
