package com.payments.metricsaggregation.service;

import com.payments.metricsaggregation.dto.MetricsDataDto;
import com.payments.metricsaggregation.entity.MetricsDataEntity;
import com.payments.metricsaggregation.repository.MetricsDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Metrics Aggregation Service
 * 
 * Aggregates metrics from all microservices in the payments engine.
 * Provides real-time metrics collection, storage, and querying capabilities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsAggregationService {

    private final MetricsDataRepository metricsDataRepository;
    private final WebClient webClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    /**
     * Collect metrics from all services
     */
    public Flux<MetricsDataDto> collectMetricsFromAllServices() {
        log.info("Starting metrics collection from all services");
        
        return Flux.fromIterable(getAllServiceNames())
            .flatMap(this::collectServiceMetrics)
            .doOnNext(metrics -> log.debug("Collected metrics: {}", metrics.getMetricName()))
            .doOnComplete(() -> log.info("Completed metrics collection from all services"));
    }

    /**
     * Collect metrics from a specific service
     */
    public Flux<MetricsDataDto> collectServiceMetrics(String serviceName) {
        log.debug("Collecting metrics from service: {}", serviceName);
        
        return webClient
            .get()
            .uri("/actuator/prometheus")
            .retrieve()
            .bodyToMono(String.class)
            .flatMapMany(prometheusData -> parsePrometheusMetrics(serviceName, prometheusData))
            .doOnError(error -> log.error("Failed to collect metrics from service: {}", serviceName, error))
            .onErrorResume(error -> Flux.empty());
    }

    /**
     * Get real-time metrics for a service
     */
    @Cacheable(value = "serviceMetrics", key = "#serviceName", unless = "#result.isEmpty()")
    public List<MetricsDataDto> getServiceMetrics(String serviceName, Duration timeWindow) {
        log.info("Getting metrics for service: {} within time window: {}", serviceName, timeWindow);
        
        Instant startTime = Instant.now().minus(timeWindow);
        
        return metricsDataRepository.findByServiceNameAndTimestampAfter(serviceName, startTime)
            .stream()
            .map(this::mapToDto)
            .toList();
    }

    /**
     * Get aggregated metrics summary
     */
    public MetricsDataDto.MetricsSummary getMetricsSummary(String serviceName, String metricName, Duration timeWindow) {
        log.info("Getting metrics summary for service: {} metric: {}", serviceName, metricName);
        
        Instant startTime = Instant.now().minus(timeWindow);
        
        List<MetricsDataEntity> metrics = metricsDataRepository
            .findByServiceNameAndMetricNameAndTimestampAfter(serviceName, metricName, startTime);
        
        if (metrics.isEmpty()) {
            return MetricsDataDto.MetricsSummary.builder()
                .serviceName(serviceName)
                .metricName(metricName)
                .currentValue(0.0)
                .averageValue(0.0)
                .minValue(0.0)
                .maxValue(0.0)
                .dataPointCount(0L)
                .lastUpdated(Instant.now())
                .build();
        }
        
        double currentValue = metrics.get(metrics.size() - 1).getValue();
        double averageValue = metrics.stream().mapToDouble(MetricsDataEntity::getValue).average().orElse(0.0);
        double minValue = metrics.stream().mapToDouble(MetricsDataEntity::getValue).min().orElse(0.0);
        double maxValue = metrics.stream().mapToDouble(MetricsDataEntity::getValue).max().orElse(0.0);
        
        return MetricsDataDto.MetricsSummary.builder()
            .serviceName(serviceName)
            .metricName(metricName)
            .currentValue(currentValue)
            .averageValue(averageValue)
            .minValue(minValue)
            .maxValue(maxValue)
            .dataPointCount((long) metrics.size())
            .lastUpdated(metrics.get(metrics.size() - 1).getTimestamp())
            .build();
    }

    /**
     * Get time series data for charts
     */
    public List<MetricsDataDto.TimeSeriesPoint> getTimeSeriesData(String serviceName, String metricName, Duration timeWindow) {
        log.info("Getting time series data for service: {} metric: {}", serviceName, metricName);
        
        Instant startTime = Instant.now().minus(timeWindow);
        
        return metricsDataRepository
            .findByServiceNameAndMetricNameAndTimestampAfter(serviceName, metricName, startTime)
            .stream()
            .map(metric -> MetricsDataDto.TimeSeriesPoint.builder()
                .timestamp(metric.getTimestamp())
                .value(metric.getValue())
                .labels(parseTags(metric.getTags()))
                .build())
            .toList();
    }

    /**
     * Store metrics data
     */
    public Mono<Void> storeMetrics(List<MetricsDataDto> metricsData) {
        log.debug("Storing {} metrics data points", metricsData.size());
        
        List<MetricsDataEntity> entities = metricsData.stream()
            .map(this::mapToEntity)
            .toList();
        
        return Mono.fromRunnable(() -> {
            metricsDataRepository.saveAll(entities);
            log.debug("Stored {} metrics data points", entities.size());
        });
    }

    /**
     * Parse Prometheus metrics format
     */
    private Flux<MetricsDataDto> parsePrometheusMetrics(String serviceName, String prometheusData) {
        return Flux.fromIterable(prometheusData.split("\n"))
            .filter(line -> !line.startsWith("#") && !line.isEmpty())
            .map(line -> parsePrometheusLine(serviceName, line))
            .filter(metric -> metric != null);
    }

    /**
     * Parse a single Prometheus metric line
     */
    private MetricsDataDto parsePrometheusLine(String serviceName, String line) {
        try {
            String[] parts = line.split(" ");
            if (parts.length < 2) return null;
            
            String metricLine = parts[0];
            String valueStr = parts[1];
            
            String[] metricParts = metricLine.split("\\{");
            String metricName = metricParts[0];
            String tags = metricParts.length > 1 ? 
                "{" + metricParts[1].substring(0, metricParts[1].length() - 1) : "{}";
            
            return MetricsDataDto.builder()
                .serviceName(serviceName)
                .metricType(MetricsDataEntity.MetricType.GAUGE)
                .metricName(metricName)
                .value(Double.parseDouble(valueStr))
                .unit("")
                .tags(parseTags(tags))
                .timestamp(Instant.now())
                .build();
                
        } catch (Exception e) {
            log.warn("Failed to parse Prometheus line: {}", line, e);
            return null;
        }
    }

    /**
     * Get all service names
     */
    private List<String> getAllServiceNames() {
        return List.of(
            "payment-initiation-service",
            "validation-service",
            "account-adapter-service",
            "routing-service",
            "transaction-processing-service",
            "saga-orchestrator",
            "samos-adapter-service",
            "bankservafrica-adapter-service",
            "rtc-adapter-service",
            "payshap-adapter-service",
            "swift-adapter-service",
            "reconciliation-service",
            "settlement-service",
            "batch-processing-service",
            "analytics-service",
            "reporting-service",
            "web-bff-service",
            "tenant-management-service",
            "iam-service",
            "audit-service",
            "notification-service",
            "operations-management-service"
        );
    }

    /**
     * Map entity to DTO
     */
    private MetricsDataDto mapToDto(MetricsDataEntity entity) {
        return MetricsDataDto.builder()
            .serviceName(entity.getServiceName())
            .metricType(entity.getMetricType())
            .metricName(entity.getMetricName())
            .value(entity.getValue())
            .unit(entity.getUnit())
            .tags(parseTags(entity.getTags()))
            .timestamp(entity.getTimestamp())
            .build();
    }

    /**
     * Map DTO to entity
     */
    private MetricsDataEntity mapToEntity(MetricsDataDto dto) {
        return MetricsDataEntity.builder()
            .serviceName(dto.getServiceName())
            .metricType(dto.getMetricType())
            .metricName(dto.getMetricName())
            .value(dto.getValue())
            .unit(dto.getUnit())
            .tags(convertTagsToJson(dto.getTags()))
            .timestamp(dto.getTimestamp())
            .build();
    }

    /**
     * Parse JSON tags string to map
     */
    private Map<String, String> parseTags(String tagsJson) {
        // Simple JSON parsing - in production, use Jackson
        if (tagsJson == null || tagsJson.equals("{}")) {
            return Map.of();
        }
        // This would be properly implemented with Jackson
        return Map.of("service", "unknown");
    }

    /**
     * Convert tags map to JSON string
     */
    private String convertTagsToJson(Map<String, String> tags) {
        // Simple JSON conversion - in production, use Jackson
        return tags.toString();
    }
}
