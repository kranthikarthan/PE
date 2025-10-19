package com.payments.analytics.repository;

import com.payments.analytics.domain.AnalyticsMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Analytics Metric entities.
 *
 * <p>This repository provides data access methods for analytics metrics
 * with support for filtering, pagination, and aggregation.
 *
 * @since PE-415
 */
@Repository
public interface AnalyticsMetricRepository extends JpaRepository<AnalyticsMetric, UUID> {

    /**
     * Finds analytics metrics by filters.
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
    @Query("SELECT m FROM AnalyticsMetric m WHERE " +
           "m.tenantId = :tenantId AND " +
           "(:businessUnitId IS NULL OR m.businessUnitId = :businessUnitId) AND " +
           "(:metricName IS NULL OR m.metricName = :metricName) AND " +
           "(:category IS NULL OR m.category = :category) AND " +
           "(:startDate IS NULL OR m.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR m.timestamp <= :endDate) " +
           "ORDER BY m.timestamp DESC")
    List<AnalyticsMetric> findByFilters(
            @Param("tenantId") UUID tenantId,
            @Param("businessUnitId") UUID businessUnitId,
            @Param("metricName") String metricName,
            @Param("category") AnalyticsMetric.MetricCategory category,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    /**
     * Finds aggregated metrics for a specific time period.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param metricName the metric name
     * @param aggregationPeriod the aggregation period
     * @param startDate the start date
     * @param endDate the end date
     * @return list of aggregated metrics
     */
    @Query("SELECT m FROM AnalyticsMetric m WHERE " +
           "m.tenantId = :tenantId AND " +
           "(:businessUnitId IS NULL OR m.businessUnitId = :businessUnitId) AND " +
           "m.metricName = :metricName AND " +
           "m.aggregationPeriod = :aggregationPeriod AND " +
           "m.timestamp >= :startDate AND " +
           "m.timestamp <= :endDate " +
           "ORDER BY m.timestamp ASC")
    List<AnalyticsMetric> findAggregatedMetrics(
            @Param("tenantId") UUID tenantId,
            @Param("businessUnitId") UUID businessUnitId,
            @Param("metricName") String metricName,
            @Param("aggregationPeriod") AnalyticsMetric.AggregationPeriod aggregationPeriod,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    /**
     * Finds real-time metrics for dashboard.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of real-time metrics
     */
    @Query("SELECT m FROM AnalyticsMetric m WHERE " +
           "m.tenantId = :tenantId AND " +
           "(:businessUnitId IS NULL OR m.businessUnitId = :businessUnitId) AND " +
           "m.timestamp >= :startDate AND " +
           "m.timestamp <= :endDate AND " +
           "m.aggregationPeriod = 'REAL_TIME' " +
           "ORDER BY m.timestamp DESC")
    List<AnalyticsMetric> findRealTimeMetrics(
            @Param("tenantId") UUID tenantId,
            @Param("businessUnitId") UUID businessUnitId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    /**
     * Finds analytics metrics by entity ID and type.
     *
     * @param entityId the entity ID
     * @param entityType the entity type
     * @param tenantId the tenant ID
     * @return list of analytics metrics
     */
    List<AnalyticsMetric> findByEntityIdAndEntityTypeAndTenantIdOrderByTimestampDesc(
            String entityId, 
            AnalyticsMetric.EntityType entityType, 
            UUID tenantId
    );

    /**
     * Finds analytics metrics by metric name and time range.
     *
     * @param metricName the metric name
     * @param startDate the start date
     * @param endDate the end date
     * @param tenantId the tenant ID
     * @return list of analytics metrics
     */
    List<AnalyticsMetric> findByMetricNameAndTimestampBetweenAndTenantIdOrderByTimestampDesc(
            String metricName,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            UUID tenantId
    );

    /**
     * Finds analytics metrics by category and time range.
     *
     * @param category the category
     * @param startDate the start date
     * @param endDate the end date
     * @param tenantId the tenant ID
     * @return list of analytics metrics
     */
    List<AnalyticsMetric> findByCategoryAndTimestampBetweenAndTenantIdOrderByTimestampDesc(
            AnalyticsMetric.MetricCategory category,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            UUID tenantId
    );

    /**
     * Finds analytics metrics by aggregation period.
     *
     * @param aggregationPeriod the aggregation period
     * @param tenantId the tenant ID
     * @return list of analytics metrics
     */
    List<AnalyticsMetric> findByAggregationPeriodAndTenantIdOrderByTimestampDesc(
            AnalyticsMetric.AggregationPeriod aggregationPeriod, 
            UUID tenantId
    );

    /**
     * Finds analytics metrics by user ID and time range.
     *
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @param tenantId the tenant ID
     * @return list of analytics metrics
     */
    List<AnalyticsMetric> findByUserIdAndTimestampBetweenAndTenantIdOrderByTimestampDesc(
            String userId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            UUID tenantId
    );

    /**
     * Finds analytics metrics by session ID.
     *
     * @param sessionId the session ID
     * @param tenantId the tenant ID
     * @return list of analytics metrics
     */
    List<AnalyticsMetric> findBySessionIdAndTenantIdOrderByTimestampDesc(
            String sessionId, 
            UUID tenantId
    );

    /**
     * Finds analytics metrics by correlation ID.
     *
     * @param correlationId the correlation ID
     * @param tenantId the tenant ID
     * @return list of analytics metrics
     */
    List<AnalyticsMetric> findByCorrelationIdAndTenantIdOrderByTimestampDesc(
            String correlationId, 
            UUID tenantId
    );

    /**
     * Finds analytics metrics by value range.
     *
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param tenantId the tenant ID
     * @return list of analytics metrics
     */
    @Query("SELECT m FROM AnalyticsMetric m WHERE " +
           "m.metricValue >= :minValue AND " +
           "m.metricValue <= :maxValue AND " +
           "m.tenantId = :tenantId " +
           "ORDER BY m.timestamp DESC")
    List<AnalyticsMetric> findByValueRangeAndTenantId(
            @Param("minValue") java.math.BigDecimal minValue,
            @Param("maxValue") java.math.BigDecimal maxValue,
            @Param("tenantId") UUID tenantId
    );

    /**
     * Finds analytics metrics by unit.
     *
     * @param unit the metric unit
     * @param tenantId the tenant ID
     * @return list of analytics metrics
     */
    List<AnalyticsMetric> findByMetricUnitAndTenantIdOrderByTimestampDesc(
            String unit, 
            UUID tenantId
    );

    /**
     * Finds analytics metrics by subcategory.
     *
     * @param subcategory the subcategory
     * @param tenantId the tenant ID
     * @return list of analytics metrics
     */
    List<AnalyticsMetric> findBySubcategoryAndTenantIdOrderByTimestampDesc(
            String subcategory, 
            UUID tenantId
    );

    /**
     * Counts analytics metrics by filters.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param metricName optional metric name filter
     * @param category optional category filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return count of analytics metrics
     */
    @Query("SELECT COUNT(m) FROM AnalyticsMetric m WHERE " +
           "m.tenantId = :tenantId AND " +
           "(:businessUnitId IS NULL OR m.businessUnitId = :businessUnitId) AND " +
           "(:metricName IS NULL OR m.metricName = :metricName) AND " +
           "(:category IS NULL OR m.category = :category) AND " +
           "(:startDate IS NULL OR m.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR m.timestamp <= :endDate)")
    long countByFilters(
            @Param("tenantId") UUID tenantId,
            @Param("businessUnitId") UUID businessUnitId,
            @Param("metricName") String metricName,
            @Param("category") AnalyticsMetric.MetricCategory category,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    /**
     * Deletes analytics metrics older than specified date.
     *
     * @param cutoffDate the cutoff date
     * @param tenantId the tenant ID
     * @return number of deleted metrics
     */
    @Query("DELETE FROM AnalyticsMetric m WHERE m.timestamp < :cutoffDate AND m.tenantId = :tenantId")
    int deleteByTimestampBeforeAndTenantId(
            @Param("cutoffDate") OffsetDateTime cutoffDate,
            @Param("tenantId") UUID tenantId
    );
}
