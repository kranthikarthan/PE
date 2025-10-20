package com.payments.operationsmanagement.dto;

import com.payments.operationsmanagement.entity.ServiceHealthEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Service Health DTO
 * 
 * Data Transfer Object for service health information.
 * Used in API responses for service health monitoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthDto {

    private String name;
    private ServiceHealthEntity.ServiceStatus status;
    private Double uptime;
    private Double requestRate;
    private Double errorRate;
    private ResponseTimeDto responseTime;
    private CircuitBreakerDto circuitBreaker;
    private List<PodInfoDto> pods;
    private Instant lastHealthCheck;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseTimeDto {
        private Long p50;
        private Long p95;
        private Long p99;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitBreakerDto {
        private ServiceHealthEntity.CircuitBreakerState state;
        private Double failureRate;
    }
}
