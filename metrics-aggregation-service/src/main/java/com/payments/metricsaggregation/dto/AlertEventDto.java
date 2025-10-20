package com.payments.metricsaggregation.dto;

import com.payments.metricsaggregation.entity.AlertEventEntity;
import com.payments.metricsaggregation.entity.AlertRuleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Alert Event DTO
 * 
 * Data Transfer Object for alert events.
 * Used in API responses for alert management and monitoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEventDto {

    private Long id;
    private String alertId;
    private Long ruleId;
    private String serviceName;
    private String metricName;
    private Double metricValue;
    private Double thresholdValue;
    private AlertEventEntity.Status status;
    private AlertRuleEntity.Severity severity;
    private String message;
    private String acknowledgedBy;
    private Instant acknowledgedAt;
    private String resolvedBy;
    private Instant resolvedAt;
    private Instant triggeredAt;
    private Instant createdAt;

    /**
     * Alert acknowledgment request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcknowledgeRequest {
        private String acknowledgedBy;
        private String message;
    }

    /**
     * Alert resolution request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolveRequest {
        private String resolvedBy;
        private String message;
    }
}
