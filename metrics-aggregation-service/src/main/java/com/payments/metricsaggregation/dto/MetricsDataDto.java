package com.payments.metricsaggregation.dto;

import com.payments.metricsaggregation.entity.MetricsDataEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Metrics Data DTO
 * 
 * Data Transfer Object for metrics data.
 * Used in API responses for metrics queries and real-time dashboards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsDataDto {

    private String serviceName;
    private MetricsDataEntity.MetricType metricType;
    private String metricName;
    private Double value;
    private String unit;
    private Map<String, String> tags;
    private Instant timestamp;

    /**
     * Time series data point for charts
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesPoint {
        private Instant timestamp;
        private Double value;
        private Map<String, String> labels;
    }

    /**
     * Aggregated metrics summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsSummary {
        private String serviceName;
        private String metricName;
        private Double currentValue;
        private Double averageValue;
        private Double minValue;
        private Double maxValue;
        private Long dataPointCount;
        private Instant lastUpdated;
    }
}
