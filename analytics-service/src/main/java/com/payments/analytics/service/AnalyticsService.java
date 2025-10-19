package com.payments.analytics.service;

import com.payments.analytics.domain.AnalyticsEvent;
import com.payments.analytics.domain.AnalyticsMetric;
import com.payments.analytics.repository.AnalyticsEventRepository;
import com.payments.analytics.repository.AnalyticsMetricRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for Analytics operations.
 *
 * <p>This service handles all analytics-related operations, including
 * event tracking, metric collection, and data aggregation.
 *
 * @since PE-415
 */
@Service
@Slf4j
public class AnalyticsService {

    @Autowired
    private AnalyticsEventRepository analyticsEventRepository;

    @Autowired
    private AnalyticsMetricRepository analyticsMetricRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Tracks an analytics event.
     *
     * @param event the analytics event to track
     * @return the tracked event
     */
    @Transactional
    @CircuitBreaker(name = "analytics-service", fallbackMethod = "trackEventFallback")
    @Retry(name = "analytics-service")
    public AnalyticsEvent trackEvent(AnalyticsEvent event) {
        log.debug("Tracking analytics event: {} for entity: {}", event.getEventType(), event.getEntityId());
        
        // Save to database
        AnalyticsEvent savedEvent = analyticsEventRepository.save(event);
        
        // Publish to Kafka for real-time processing
        try {
            kafkaTemplate.send("analytics-events", savedEvent);
            log.debug("Published analytics event to Kafka: {}", savedEvent.getId());
        } catch (Exception e) {
            log.warn("Failed to publish analytics event to Kafka: {}", e.getMessage());
        }
        
        return savedEvent;
    }

    /**
     * Records an analytics metric.
     *
     * @param metric the analytics metric to record
     * @return the recorded metric
     */
    @Transactional
    @CircuitBreaker(name = "analytics-service", fallbackMethod = "recordMetricFallback")
    @Retry(name = "analytics-service")
    public AnalyticsMetric recordMetric(AnalyticsMetric metric) {
        log.debug("Recording analytics metric: {} with value: {}", metric.getMetricName(), metric.getMetricValue());
        
        // Save to database
        AnalyticsMetric savedMetric = analyticsMetricRepository.save(metric);
        
        // Publish to Kafka for real-time processing
        try {
            kafkaTemplate.send("analytics-metrics", savedMetric);
            log.debug("Published analytics metric to Kafka: {}", savedMetric.getId());
        } catch (Exception e) {
            log.warn("Failed to publish analytics metric to Kafka: {}", e.getMessage());
        }
        
        return savedMetric;
    }

    /**
     * Gets analytics events with filtering and pagination.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param eventType optional event type filter
     * @param entityType optional entity type filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param limit maximum number of results
     * @param offset offset for pagination
     * @return list of analytics events
     */
    @Cacheable(value = "analytics-events", key = "#tenantId + '_' + #businessUnitId + '_' + #eventType + '_' + #entityType + '_' + #startDate + '_' + #endDate + '_' + #limit + '_' + #offset")
    public List<AnalyticsEvent> getAnalyticsEvents(
            UUID tenantId,
            UUID businessUnitId,
            AnalyticsEvent.EventType eventType,
            AnalyticsEvent.EntityType entityType,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Integer limit,
            Integer offset) {
        
        log.debug("Retrieving analytics events for tenant: {}, businessUnit: {}, eventType: {}, entityType: {}", 
                tenantId, businessUnitId, eventType, entityType);
        
        return analyticsEventRepository.findByFilters(
                tenantId,
                businessUnitId,
                eventType,
                entityType,
                startDate,
                endDate,
                limit,
                offset
        );
    }

    /**
     * Gets analytics metrics with filtering and pagination.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param metricName optional metric name filter
     * @param category optional category filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param limit maximum number of results
     * @param offset offset for pagination
     * @return list of analytics metrics
     */
    @Cacheable(value = "analytics-metrics", key = "#tenantId + '_' + #businessUnitId + '_' + #metricName + '_' + #category + '_' + #startDate + '_' + #endDate + '_' + #limit + '_' + #offset")
    public List<AnalyticsMetric> getAnalyticsMetrics(
            UUID tenantId,
            UUID businessUnitId,
            String metricName,
            AnalyticsMetric.MetricCategory category,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Integer limit,
            Integer offset) {
        
        log.debug("Retrieving analytics metrics for tenant: {}, businessUnit: {}, metricName: {}, category: {}", 
                tenantId, businessUnitId, metricName, category);
        
        return analyticsMetricRepository.findByFilters(
                tenantId,
                businessUnitId,
                metricName,
                category,
                startDate,
                endDate,
                limit,
                offset
        );
    }

    /**
     * Gets aggregated metrics for a specific time period.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param metricName the metric name
     * @param aggregationPeriod the aggregation period
     * @param startDate the start date
     * @param endDate the end date
     * @return aggregated metrics
     */
    @Cacheable(value = "aggregated-metrics", key = "#tenantId + '_' + #businessUnitId + '_' + #metricName + '_' + #aggregationPeriod + '_' + #startDate + '_' + #endDate")
    public List<AnalyticsMetric> getAggregatedMetrics(
            UUID tenantId,
            UUID businessUnitId,
            String metricName,
            AnalyticsMetric.AggregationPeriod aggregationPeriod,
            OffsetDateTime startDate,
            OffsetDateTime endDate) {
        
        log.debug("Retrieving aggregated metrics for tenant: {}, businessUnit: {}, metricName: {}, period: {}", 
                tenantId, businessUnitId, metricName, aggregationPeriod);
        
        return analyticsMetricRepository.findAggregatedMetrics(
                tenantId,
                businessUnitId,
                metricName,
                aggregationPeriod,
                startDate,
                endDate
        );
    }

    /**
     * Gets real-time metrics for dashboard.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @return real-time metrics
     */
    @Cacheable(value = "realtime-metrics", key = "#tenantId + '_' + #businessUnitId", unless = "#result.isEmpty()")
    public List<AnalyticsMetric> getRealTimeMetrics(UUID tenantId, UUID businessUnitId) {
        log.debug("Retrieving real-time metrics for tenant: {}, businessUnit: {}", tenantId, businessUnitId);
        
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime oneHourAgo = now.minusHours(1);
        
        return analyticsMetricRepository.findRealTimeMetrics(
                tenantId,
                businessUnitId,
                oneHourAgo,
                now
        );
    }

    /**
     * Clears analytics cache.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     */
    @CacheEvict(value = {"analytics-events", "analytics-metrics", "aggregated-metrics", "realtime-metrics"}, 
                key = "#tenantId + '_' + #businessUnitId")
    public void clearAnalyticsCache(UUID tenantId, UUID businessUnitId) {
        log.debug("Clearing analytics cache for tenant: {}, businessUnit: {}", tenantId, businessUnitId);
    }

    // Fallback methods for circuit breaker
    public AnalyticsEvent trackEventFallback(AnalyticsEvent event, Exception ex) {
        log.error("Fallback: Unable to track analytics event: {}", event.getEventType(), ex);
        return event; // Return the event as-is for fallback
    }

    public AnalyticsMetric recordMetricFallback(AnalyticsMetric metric, Exception ex) {
        log.error("Fallback: Unable to record analytics metric: {}", metric.getMetricName(), ex);
        return metric; // Return the metric as-is for fallback
    }
}
