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
 * JPA entity for settlement monitoring management.
 *
 * <p>This entity represents settlement monitoring data that tracks settlement performance, metrics,
 * and health. It provides comprehensive monitoring capabilities including metrics collection,
 * performance tracking, and health monitoring.
 *
 * @since PE-411
 */
@Entity
@Table(name = "settlement_monitoring")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementMonitoring {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "monitoring_id", nullable = false, unique = true, length = 100)
  private String monitoringId;

  @Column(name = "monitoring_name", nullable = false, length = 255)
  private String monitoringName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "monitoring_type", nullable = false, length = 20)
  private MonitoringType monitoringType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private MonitoringStatus status = MonitoringStatus.ACTIVE;

  @Column(name = "metric_name", nullable = false, length = 100)
  private String metricName;

  @Column(name = "metric_value", precision = 19, scale = 4)
  private BigDecimal metricValue;

  @Column(name = "metric_unit", length = 20)
  private String metricUnit;

  @Column(name = "threshold_value", precision = 19, scale = 4)
  private BigDecimal thresholdValue;

  @Column(name = "alert_level", length = 20)
  private String alertLevel;

  @Column(name = "monitoring_timestamp", nullable = false)
  private LocalDateTime monitoringTimestamp;

  @Column(name = "duration_seconds")
  private Long durationSeconds;

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

  /** Enumeration of monitoring types. */
  public enum MonitoringType {
    PERFORMANCE,
    HEALTH,
    METRICS,
    ALERT,
    DASHBOARD,
    REPORTING
  }

  /** Enumeration of monitoring statuses. */
  public enum MonitoringStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    FAILED,
    COMPLETED
  }

  /**
   * Checks if the monitoring is active.
   *
   * @return true if active, false otherwise
   */
  public boolean isActive() {
    return status == MonitoringStatus.ACTIVE;
  }

  /**
   * Checks if the monitoring is inactive.
   *
   * @return true if inactive, false otherwise
   */
  public boolean isInactive() {
    return status == MonitoringStatus.INACTIVE;
  }

  /**
   * Checks if the monitoring is suspended.
   *
   * @return true if suspended, false otherwise
   */
  public boolean isSuspended() {
    return status == MonitoringStatus.SUSPENDED;
  }

  /**
   * Checks if the monitoring is failed.
   *
   * @return true if failed, false otherwise
   */
  public boolean isFailed() {
    return status == MonitoringStatus.FAILED;
  }

  /**
   * Checks if the monitoring is completed.
   *
   * @return true if completed, false otherwise
   */
  public boolean isCompleted() {
    return status == MonitoringStatus.COMPLETED;
  }

  /**
   * Checks if the metric value exceeds the threshold.
   *
   * @return true if exceeds threshold, false otherwise
   */
  public boolean exceedsThreshold() {
    if (metricValue == null || thresholdValue == null) return false;
    return metricValue.compareTo(thresholdValue) > 0;
  }

  /**
   * Checks if the metric value is below the threshold.
   *
   * @return true if below threshold, false otherwise
   */
  public boolean belowThreshold() {
    if (metricValue == null || thresholdValue == null) return false;
    return metricValue.compareTo(thresholdValue) < 0;
  }

  /**
   * Checks if the metric value equals the threshold.
   *
   * @return true if equals threshold, false otherwise
   */
  public boolean equalsThreshold() {
    if (metricValue == null || thresholdValue == null) return false;
    return metricValue.compareTo(thresholdValue) == 0;
  }

  /**
   * Calculates the metric deviation from threshold.
   *
   * @return the deviation percentage
   */
  public BigDecimal calculateDeviation() {
    if (metricValue == null
        || thresholdValue == null
        || thresholdValue.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return metricValue
        .subtract(thresholdValue)
        .divide(thresholdValue, 4, BigDecimal.ROUND_HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  /**
   * Determines the alert level based on metric value and threshold.
   *
   * @return the alert level
   */
  public String determineAlertLevel() {
    if (exceedsThreshold()) {
      BigDecimal deviation = calculateDeviation();
      if (deviation.compareTo(BigDecimal.valueOf(50)) > 0) {
        return "CRITICAL";
      } else if (deviation.compareTo(BigDecimal.valueOf(25)) > 0) {
        return "HIGH";
      } else {
        return "MEDIUM";
      }
    } else if (belowThreshold()) {
      BigDecimal deviation = calculateDeviation().abs();
      if (deviation.compareTo(BigDecimal.valueOf(50)) > 0) {
        return "LOW";
      } else {
        return "INFO";
      }
    } else {
      return "NORMAL";
    }
  }

  /**
   * Updates the monitoring status.
   *
   * @param status the new status
   */
  public void updateStatus(MonitoringStatus status) {
    this.status = status;
  }

  /**
   * Updates the metric value and recalculates alert level.
   *
   * @param metricValue the new metric value
   */
  public void updateMetricValue(BigDecimal metricValue) {
    this.metricValue = metricValue;
    this.alertLevel = determineAlertLevel();
  }

  /**
   * Updates the threshold value and recalculates alert level.
   *
   * @param thresholdValue the new threshold value
   */
  public void updateThresholdValue(BigDecimal thresholdValue) {
    this.thresholdValue = thresholdValue;
    this.alertLevel = determineAlertLevel();
  }

  /**
   * Gets the monitoring summary.
   *
   * @return the monitoring summary string
   */
  public String getSummary() {
    return String.format(
        "Monitoring[%s] %s - %s (%s %s)",
        monitoringId, metricName, metricValue, metricUnit, status);
  }
}
