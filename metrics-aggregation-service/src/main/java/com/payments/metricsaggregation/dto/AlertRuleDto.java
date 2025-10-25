package com.payments.metricsaggregation.dto;

import com.payments.metricsaggregation.entity.AlertRuleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Alert Rule DTO
 * 
 * Data Transfer Object for alert rules.
 * Used in API responses for alert rule management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleDto {

    private Long id;
    private String ruleName;
    private String description;
    private String serviceName;
    private String metricName;
    private AlertRuleEntity.ConditionType conditionType;
    private Double thresholdValue;
    private Integer evaluationWindowSeconds;
    private AlertRuleEntity.Severity severity;
    private List<String> notificationChannels;
    private Boolean isEnabled;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Alert rule creation request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String ruleName;
        private String description;
        private String serviceName;
        private String metricName;
        private AlertRuleEntity.ConditionType conditionType;
        private Double thresholdValue;
        private Integer evaluationWindowSeconds;
        private AlertRuleEntity.Severity severity;
        private List<String> notificationChannels;
        private Boolean isEnabled;
    }

    /**
     * Alert rule update request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String description;
        private AlertRuleEntity.ConditionType conditionType;
        private Double thresholdValue;
        private Integer evaluationWindowSeconds;
        private AlertRuleEntity.Severity severity;
        private List<String> notificationChannels;
        private Boolean isEnabled;
    }
}
