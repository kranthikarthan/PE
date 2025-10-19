package com.payments.domain.settlement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA entity for settlement metrics management.
 *
 * <p>This entity represents settlement metrics that track settlement performance, statistics, and
 * KPIs. It provides comprehensive metrics collection including performance metrics, business
 * metrics, and operational metrics.
 *
 * @since PE-411
 */
@Entity
@Table(name = "settlement_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementMetrics {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "metrics_id", nullable = false, unique = true, length = 100)
  private String metricsId;

  @Column(name = "metrics_name", nullable = false, length = 255)
  private String metricsName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "metrics_type", nullable = false, length = 20)
  private MetricsType metricsType;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 20)
  private MetricsCategory category;

  @Column(name = "metric_name", nullable = false, length = 100)
  private String metricName;

  @Column(name = "metric_value", precision = 19, scale = 4)
  private BigDecimal metricValue;

  @Column(name = "metric_unit", length = 20)
  private String metricUnit;

  @Column(name = "baseline_value", precision = 19, scale = 4)
  private BigDecimal baselineValue;

  @Column(name = "target_value", precision = 19, scale = 4)
  private BigDecimal targetValue;

  @Column(name = "threshold_min", precision = 19, scale = 4)
  private BigDecimal thresholdMin;

  @Column(name = "threshold_max", precision = 19, scale = 4)
  private BigDecimal thresholdMax;

  @Column(name = "metrics_timestamp", nullable = false)
  private LocalDateTime metricsTimestamp;

  @Column(name = "period_start")
  private LocalDateTime periodStart;

  @Column(name = "period_end")
  private LocalDateTime periodEnd;

  @Column(name = "participant_id", length = 50)
  private String participantId;

  @Column(name = "workflow_id")
  private Long workflowId;

  @Column(name = "orchestration_id")
  private Long orchestrationId;

  @Column(name = "currency", length = 3)
  private String currency;

  @Column(name = "business_unit_id", length = 50)
  private String businessUnitId;

  @Column(name = "tenant_id", nullable = false, length = 50)
  private String tenantId;

  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "created_by", length = 100)
  private String createdBy;

  @Column(name = "updated_by", length = 100)
  private String updatedBy;

  /** Enumeration of metrics types. */
  public enum MetricsType {
    PERFORMANCE,
    BUSINESS,
    OPERATIONAL,
    TECHNICAL,
    FINANCIAL,
    COMPLIANCE
  }

  /** Enumeration of metrics categories. */
  public enum MetricsCategory {
    THROUGHPUT,
    LATENCY,
    AVAILABILITY,
    ACCURACY,
    VOLUME,
    VALUE,
    EFFICIENCY,
    QUALITY
  }

  /**
   * Checks if the metric value is within threshold range.
   *
   * @return true if within threshold, false otherwise
   */
  public boolean isWithinThreshold() {
    if (metricValue == null || thresholdMin == null || thresholdMax == null) return true;
    return metricValue.compareTo(thresholdMin) >= 0 && metricValue.compareTo(thresholdMax) <= 0;
  }

  /**
   * Checks if the metric value exceeds the maximum threshold.
   *
   * @return true if exceeds maximum threshold, false otherwise
   */
  public boolean exceedsMaxThreshold() {
    if (metricValue == null || thresholdMax == null) return false;
    return metricValue.compareTo(thresholdMax) > 0;
  }

  /**
   * Checks if the metric value is below the minimum threshold.
   *
   * @return true if below minimum threshold, false otherwise
   */
  public boolean belowMinThreshold() {
    if (metricValue == null || thresholdMin == null) return false;
    return metricValue.compareTo(thresholdMin) < 0;
  }

  /**
   * Checks if the metric value meets the target.
   *
   * @return true if meets target, false otherwise
   */
  public boolean meetsTarget() {
    if (metricValue == null || targetValue == null) return false;
    return metricValue.compareTo(targetValue) >= 0;
  }

  /**
   * Checks if the metric value exceeds the baseline.
   *
   * @return true if exceeds baseline, false otherwise
   */
  public boolean exceedsBaseline() {
    if (metricValue == null || baselineValue == null) return false;
    return metricValue.compareTo(baselineValue) > 0;
  }

  /**
   * Checks if the metric value is below the baseline.
   *
   * @return true if below baseline, false otherwise
   */
  public boolean belowBaseline() {
    if (metricValue == null || baselineValue == null) return false;
    return metricValue.compareTo(baselineValue) < 0;
  }

  /**
   * Calculates the performance against target.
   *
   * @return the performance percentage
   */
  public BigDecimal calculatePerformanceAgainstTarget() {
    if (metricValue == null || targetValue == null || targetValue.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return metricValue
        .divide(targetValue, 4, BigDecimal.ROUND_HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  /**
   * Calculates the performance against baseline.
   *
   * @return the performance percentage
   */
  public BigDecimal calculatePerformanceAgainstBaseline() {
    if (metricValue == null
        || baselineValue == null
        || baselineValue.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return metricValue
        .divide(baselineValue, 4, BigDecimal.ROUND_HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  /**
   * Calculates the deviation from target.
   *
   * @return the deviation percentage
   */
  public BigDecimal calculateDeviationFromTarget() {
    if (metricValue == null || targetValue == null) return BigDecimal.ZERO;

    return metricValue
        .subtract(targetValue)
        .divide(targetValue, 4, BigDecimal.ROUND_HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  /**
   * Calculates the deviation from baseline.
   *
   * @return the deviation percentage
   */
  public BigDecimal calculateDeviationFromBaseline() {
    if (metricValue == null || baselineValue == null) return BigDecimal.ZERO;

    return metricValue
        .subtract(baselineValue)
        .divide(baselineValue, 4, BigDecimal.ROUND_HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  /**
   * Determines the performance status.
   *
   * @return the performance status
   */
  public String determinePerformanceStatus() {
    if (exceedsMaxThreshold()) {
      return "EXCEEDS_MAX";
    } else if (belowMinThreshold()) {
      return "BELOW_MIN";
    } else if (meetsTarget()) {
      return "MEETS_TARGET";
    } else if (exceedsBaseline()) {
      return "ABOVE_BASELINE";
    } else if (belowBaseline()) {
      return "BELOW_BASELINE";
    } else {
      return "WITHIN_RANGE";
    }
  }

  /**
   * Updates the metric value.
   *
   * @param metricValue the new metric value
   */
  public void updateMetricValue(BigDecimal metricValue) {
    this.metricValue = metricValue;
    this.metricsTimestamp = LocalDateTime.now();
  }

  /**
   * Updates the baseline value.
   *
   * @param baselineValue the new baseline value
   */
  public void updateBaselineValue(BigDecimal baselineValue) {
    this.baselineValue = baselineValue;
  }

  /**
   * Updates the target value.
   *
   * @param targetValue the new target value
   */
  public void updateTargetValue(BigDecimal targetValue) {
    this.targetValue = targetValue;
  }

  /**
   * Updates the threshold values.
   *
   * @param thresholdMin the new minimum threshold
   * @param thresholdMax the new maximum threshold
   */
  public void updateThresholds(BigDecimal thresholdMin, BigDecimal thresholdMax) {
    this.thresholdMin = thresholdMin;
    this.thresholdMax = thresholdMax;
  }

  /**
   * Gets the metrics summary.
   *
   * @return the metrics summary string
   */
  public String getSummary() {
    return String.format(
        "Metrics[%s] %s - %s %s (%s)", metricsId, metricName, metricValue, metricUnit, category);
  }
}
