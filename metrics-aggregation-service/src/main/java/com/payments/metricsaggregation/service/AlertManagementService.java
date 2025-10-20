package com.payments.metricsaggregation.service;

import com.payments.metricsaggregation.dto.AlertEventDto;
import com.payments.metricsaggregation.dto.AlertRuleDto;
import com.payments.metricsaggregation.entity.AlertEventEntity;
import com.payments.metricsaggregation.entity.AlertRuleEntity;
import com.payments.metricsaggregation.repository.AlertEventRepository;
import com.payments.metricsaggregation.repository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Alert Management Service
 * 
 * Manages alert rules and processes alert events.
 * Provides real-time alerting based on metrics thresholds.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertManagementService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertEventRepository alertEventRepository;
    private final MetricsAggregationService metricsAggregationService;
    private final NotificationService notificationService;

    /**
     * Create a new alert rule
     */
    public AlertRuleDto createAlertRule(AlertRuleDto.CreateRequest request, String createdBy) {
        log.info("Creating alert rule: {} for service: {}", request.getRuleName(), request.getServiceName());
        
        AlertRuleEntity entity = AlertRuleEntity.builder()
            .ruleName(request.getRuleName())
            .description(request.getDescription())
            .serviceName(request.getServiceName())
            .metricName(request.getMetricName())
            .conditionType(request.getConditionType())
            .thresholdValue(request.getThresholdValue())
            .evaluationWindowSeconds(request.getEvaluationWindowSeconds())
            .severity(request.getSeverity())
            .notificationChannels(convertChannelsToJson(request.getNotificationChannels()))
            .isEnabled(request.getIsEnabled())
            .createdBy(createdBy)
            .build();
        
        AlertRuleEntity savedEntity = alertRuleRepository.save(entity);
        log.info("Created alert rule with ID: {}", savedEntity.getId());
        
        return mapToDto(savedEntity);
    }

    /**
     * Update an existing alert rule
     */
    public AlertRuleDto updateAlertRule(Long ruleId, AlertRuleDto.UpdateRequest request) {
        log.info("Updating alert rule: {}", ruleId);
        
        AlertRuleEntity entity = alertRuleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Alert rule not found: " + ruleId));
        
        entity.setDescription(request.getDescription());
        entity.setConditionType(request.getConditionType());
        entity.setThresholdValue(request.getThresholdValue());
        entity.setEvaluationWindowSeconds(request.getEvaluationWindowSeconds());
        entity.setSeverity(request.getSeverity());
        entity.setNotificationChannels(convertChannelsToJson(request.getNotificationChannels()));
        entity.setIsEnabled(request.getIsEnabled());
        
        AlertRuleEntity savedEntity = alertRuleRepository.save(entity);
        log.info("Updated alert rule: {}", ruleId);
        
        return mapToDto(savedEntity);
    }

    /**
     * Get all alert rules
     */
    public List<AlertRuleDto> getAllAlertRules() {
        log.info("Getting all alert rules");
        
        return alertRuleRepository.findAll()
            .stream()
            .map(this::mapToDto)
            .toList();
    }

    /**
     * Get alert rules for a specific service
     */
    public List<AlertRuleDto> getAlertRulesForService(String serviceName) {
        log.info("Getting alert rules for service: {}", serviceName);
        
        return alertRuleRepository.findByServiceName(serviceName)
            .stream()
            .map(this::mapToDto)
            .toList();
    }

    /**
     * Get all active alerts
     */
    public List<AlertEventDto> getActiveAlerts() {
        log.info("Getting all active alerts");
        
        return alertEventRepository.findByStatusIn(
            List.of(AlertEventEntity.Status.TRIGGERED, AlertEventEntity.Status.ACKNOWLEDGED)
        ).stream()
            .map(this::mapToDto)
            .toList();
    }

    /**
     * Acknowledge an alert
     */
    public AlertEventDto acknowledgeAlert(Long alertId, AlertEventDto.AcknowledgeRequest request) {
        log.info("Acknowledging alert: {} by user: {}", alertId, request.getAcknowledgedBy());
        
        AlertEventEntity entity = alertEventRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        
        entity.setStatus(AlertEventEntity.Status.ACKNOWLEDGED);
        entity.setAcknowledgedBy(request.getAcknowledgedBy());
        entity.setAcknowledgedAt(Instant.now());
        
        AlertEventEntity savedEntity = alertEventRepository.save(entity);
        log.info("Acknowledged alert: {}", alertId);
        
        return mapToDto(savedEntity);
    }

    /**
     * Resolve an alert
     */
    public AlertEventDto resolveAlert(Long alertId, AlertEventDto.ResolveRequest request) {
        log.info("Resolving alert: {} by user: {}", alertId, request.getResolvedBy());
        
        AlertEventEntity entity = alertEventRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        
        entity.setStatus(AlertEventEntity.Status.RESOLVED);
        entity.setResolvedBy(request.getResolvedBy());
        entity.setResolvedAt(Instant.now());
        
        AlertEventEntity savedEntity = alertEventRepository.save(entity);
        log.info("Resolved alert: {}", alertId);
        
        return mapToDto(savedEntity);
    }

    /**
     * Evaluate alert rules (scheduled task)
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    @Async
    public CompletableFuture<Void> evaluateAlertRules() {
        log.debug("Evaluating alert rules");
        
        List<AlertRuleEntity> enabledRules = alertRuleRepository.findByIsEnabledTrue();
        
        for (AlertRuleEntity rule : enabledRules) {
            try {
                evaluateAlertRule(rule);
            } catch (Exception e) {
                log.error("Failed to evaluate alert rule: {}", rule.getRuleName(), e);
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Evaluate a specific alert rule
     */
    private void evaluateAlertRule(AlertRuleEntity rule) {
        // Get current metric value
        var metricsSummary = metricsAggregationService.getMetricsSummary(
            rule.getServiceName(), 
            rule.getMetricName(), 
            java.time.Duration.ofSeconds(rule.getEvaluationWindowSeconds())
        );
        
        double currentValue = metricsSummary.getCurrentValue();
        double threshold = rule.getThresholdValue();
        
        // Check if condition is met
        boolean conditionMet = switch (rule.getConditionType()) {
            case GREATER_THAN -> currentValue > threshold;
            case LESS_THAN -> currentValue < threshold;
            case EQUALS -> currentValue == threshold;
            case NOT_EQUALS -> currentValue != threshold;
            case GREATER_THAN_OR_EQUAL -> currentValue >= threshold;
            case LESS_THAN_OR_EQUAL -> currentValue <= threshold;
        };
        
        if (conditionMet) {
            // Check if there's already an active alert for this rule
            boolean hasActiveAlert = alertEventRepository.existsByRuleIdAndStatusIn(
                rule.getId(),
                List.of(AlertEventEntity.Status.TRIGGERED, AlertEventEntity.Status.ACKNOWLEDGED)
            );
            
            if (!hasActiveAlert) {
                triggerAlert(rule, currentValue);
            }
        }
    }

    /**
     * Trigger an alert
     */
    private void triggerAlert(AlertRuleEntity rule, double currentValue) {
        log.warn("Triggering alert for rule: {} - {} {} {} (threshold: {})", 
            rule.getRuleName(), rule.getMetricName(), rule.getConditionType(), 
            currentValue, rule.getThresholdValue());
        
        AlertEventEntity alertEvent = AlertEventEntity.builder()
            .alertId(UUID.randomUUID().toString())
            .ruleId(rule.getId())
            .serviceName(rule.getServiceName())
            .metricName(rule.getMetricName())
            .metricValue(currentValue)
            .thresholdValue(rule.getThresholdValue())
            .status(AlertEventEntity.Status.TRIGGERED)
            .severity(rule.getSeverity())
            .message(String.format("Alert triggered: %s %s %s (threshold: %s)", 
                rule.getMetricName(), rule.getConditionType(), currentValue, rule.getThresholdValue()))
            .triggeredAt(Instant.now())
            .build();
        
        AlertEventEntity savedAlert = alertEventRepository.save(alertEvent);
        
        // Send notifications
        notificationService.sendAlertNotification(savedAlert, rule);
        
        log.info("Alert triggered with ID: {}", savedAlert.getAlertId());
    }

    /**
     * Map entity to DTO
     */
    private AlertRuleDto mapToDto(AlertRuleEntity entity) {
        return AlertRuleDto.builder()
            .id(entity.getId())
            .ruleName(entity.getRuleName())
            .description(entity.getDescription())
            .serviceName(entity.getServiceName())
            .metricName(entity.getMetricName())
            .conditionType(entity.getConditionType())
            .thresholdValue(entity.getThresholdValue())
            .evaluationWindowSeconds(entity.getEvaluationWindowSeconds())
            .severity(entity.getSeverity())
            .notificationChannels(parseChannelsFromJson(entity.getNotificationChannels()))
            .isEnabled(entity.getIsEnabled())
            .createdBy(entity.getCreatedBy())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Map entity to DTO
     */
    private AlertEventDto mapToDto(AlertEventEntity entity) {
        return AlertEventDto.builder()
            .id(entity.getId())
            .alertId(entity.getAlertId())
            .ruleId(entity.getRuleId())
            .serviceName(entity.getServiceName())
            .metricName(entity.getMetricName())
            .metricValue(entity.getMetricValue())
            .thresholdValue(entity.getThresholdValue())
            .status(entity.getStatus())
            .severity(entity.getSeverity())
            .message(entity.getMessage())
            .acknowledgedBy(entity.getAcknowledgedBy())
            .acknowledgedAt(entity.getAcknowledgedAt())
            .resolvedBy(entity.getResolvedBy())
            .resolvedAt(entity.getResolvedAt())
            .triggeredAt(entity.getTriggeredAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    /**
     * Convert channels list to JSON
     */
    private String convertChannelsToJson(List<String> channels) {
        // Simple JSON conversion - in production, use Jackson
        return channels != null ? channels.toString() : "[]";
    }

    /**
     * Parse channels from JSON
     */
    private List<String> parseChannelsFromJson(String channelsJson) {
        // Simple JSON parsing - in production, use Jackson
        return List.of("email", "slack"); // Mock implementation
    }
}
