package com.payments.metricsaggregation.repository;

import com.payments.metricsaggregation.entity.AlertRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Alert Rule Repository
 * 
 * Repository for alert rule entities.
 * Provides queries for alert rule management.
 */
@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRuleEntity, Long> {

    /**
     * Find alert rules by service name
     */
    List<AlertRuleEntity> findByServiceName(String serviceName);

    /**
     * Find enabled alert rules
     */
    List<AlertRuleEntity> findByIsEnabledTrue();

    /**
     * Find alert rules by service name and enabled status
     */
    List<AlertRuleEntity> findByServiceNameAndIsEnabled(String serviceName, Boolean isEnabled);

    /**
     * Find alert rules by severity
     */
    List<AlertRuleEntity> findBySeverity(AlertRuleEntity.Severity severity);

    /**
     * Find alert rules by created by
     */
    List<AlertRuleEntity> findByCreatedBy(String createdBy);

    /**
     * Check if alert rule exists by name
     */
    boolean existsByRuleName(String ruleName);

    /**
     * Find alert rules by service name and metric name
     */
    List<AlertRuleEntity> findByServiceNameAndMetricName(String serviceName, String metricName);
}
