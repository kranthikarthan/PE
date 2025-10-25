package com.payments.metricsaggregation.repository;

import com.payments.metricsaggregation.entity.MetricsDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Metrics Data Repository
 * 
 * Repository for metrics data entities.
 * Provides time-series queries optimized for TimescaleDB.
 */
@Repository
public interface MetricsDataRepository extends JpaRepository<MetricsDataEntity, Long> {

    /**
     * Find metrics by service name and timestamp range
     */
    List<MetricsDataEntity> findByServiceNameAndTimestampAfter(String serviceName, Instant timestamp);

    /**
     * Find metrics by service name, metric name and timestamp range
     */
    List<MetricsDataEntity> findByServiceNameAndMetricNameAndTimestampAfter(
        String serviceName, String metricName, Instant timestamp);

    /**
     * Find metrics by service name and metric type
     */
    List<MetricsDataEntity> findByServiceNameAndMetricType(String serviceName, MetricsDataEntity.MetricType metricType);

    /**
     * Find latest metrics for a service
     */
    @Query("SELECT m FROM MetricsDataEntity m WHERE m.serviceName = :serviceName ORDER BY m.timestamp DESC")
    List<MetricsDataEntity> findLatestByServiceName(@Param("serviceName") String serviceName);

    /**
     * Find metrics by time range
     */
    @Query("SELECT m FROM MetricsDataEntity m WHERE m.timestamp BETWEEN :startTime AND :endTime ORDER BY m.timestamp ASC")
    List<MetricsDataEntity> findByTimeRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    /**
     * Find metrics by service and time range
     */
    @Query("SELECT m FROM MetricsDataEntity m WHERE m.serviceName = :serviceName AND m.timestamp BETWEEN :startTime AND :endTime ORDER BY m.timestamp ASC")
    List<MetricsDataEntity> findByServiceAndTimeRange(
        @Param("serviceName") String serviceName, 
        @Param("startTime") Instant startTime, 
        @Param("endTime") Instant endTime);

    /**
     * Get aggregated metrics for a service
     */
    @Query("SELECT m.serviceName, m.metricName, AVG(m.value) as avgValue, MIN(m.value) as minValue, MAX(m.value) as maxValue, COUNT(m) as count " +
           "FROM MetricsDataEntity m WHERE m.serviceName = :serviceName AND m.timestamp >= :startTime " +
           "GROUP BY m.serviceName, m.metricName")
    List<Object[]> getAggregatedMetrics(@Param("serviceName") String serviceName, @Param("startTime") Instant startTime);

    /**
     * Delete old metrics data (cleanup)
     */
    @Query("DELETE FROM MetricsDataEntity m WHERE m.timestamp < :cutoffTime")
    void deleteOldMetrics(@Param("cutoffTime") Instant cutoffTime);

    /**
     * Count metrics by service
     */
    long countByServiceName(String serviceName);

    /**
     * Count metrics by service and time range
     */
    long countByServiceNameAndTimestampAfter(String serviceName, Instant timestamp);
}
