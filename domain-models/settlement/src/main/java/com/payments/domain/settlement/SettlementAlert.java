package com.payments.domain.settlement;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA entity for settlement alert management.
 *
 * <p>This entity represents settlement alerts that are generated based
 * on monitoring thresholds and conditions. It provides comprehensive
 * alert management including alert generation, notification, and tracking.
 *
 * @since PE-411
 */
@Entity
@Table(name = "settlement_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementAlert {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "alert_id", nullable = false, unique = true, length = 100)
  private String alertId;
  
  @Column(name = "alert_name", nullable = false, length = 255)
  private String alertName;
  
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "alert_type", nullable = false, length = 20)
  private AlertType alertType;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "severity", nullable = false, length = 20)
  private AlertSeverity severity;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AlertStatus status = AlertStatus.ACTIVE;
  
  @Column(name = "alert_message", columnDefinition = "TEXT")
  private String alertMessage;
  
  @Column(name = "alert_timestamp", nullable = false)
  private LocalDateTime alertTimestamp;
  
  @Column(name = "acknowledged_at")
  private LocalDateTime acknowledgedAt;
  
  @Column(name = "acknowledged_by", length = 100)
  private String acknowledgedBy;
  
  @Column(name = "resolved_at")
  private LocalDateTime resolvedAt;
  
  @Column(name = "resolved_by", length = 100)
  private String resolvedBy;
  
  @Column(name = "resolution_notes", columnDefinition = "TEXT")
  private String resolutionNotes;
  
  @Column(name = "participant_id", length = 50)
  private String participantId;
  
  @Column(name = "workflow_id")
  private Long workflowId;
  
  @Column(name = "orchestration_id")
  private Long orchestrationId;
  
  @Column(name = "monitoring_id")
  private Long monitoringId;
  
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
  
  /**
   * Enumeration of alert types.
   */
  public enum AlertType {
    PERFORMANCE, HEALTH, THRESHOLD, ERROR, WARNING, INFO, CRITICAL
  }
  
  /**
   * Enumeration of alert severities.
   */
  public enum AlertSeverity {
    CRITICAL, HIGH, MEDIUM, LOW, INFO
  }
  
  /**
   * Enumeration of alert statuses.
   */
  public enum AlertStatus {
    ACTIVE, ACKNOWLEDGED, RESOLVED, SUPPRESSED, EXPIRED
  }
  
  /**
   * Checks if the alert is active.
   *
   * @return true if active, false otherwise
   */
  public boolean isActive() {
    return status == AlertStatus.ACTIVE;
  }
  
  /**
   * Checks if the alert is acknowledged.
   *
   * @return true if acknowledged, false otherwise
   */
  public boolean isAcknowledged() {
    return status == AlertStatus.ACKNOWLEDGED;
  }
  
  /**
   * Checks if the alert is resolved.
   *
   * @return true if resolved, false otherwise
   */
  public boolean isResolved() {
    return status == AlertStatus.RESOLVED;
  }
  
  /**
   * Checks if the alert is suppressed.
   *
   * @return true if suppressed, false otherwise
   */
  public boolean isSuppressed() {
    return status == AlertStatus.SUPPRESSED;
  }
  
  /**
   * Checks if the alert is expired.
   *
   * @return true if expired, false otherwise
   */
  public boolean isExpired() {
    return status == AlertStatus.EXPIRED;
  }
  
  /**
   * Checks if the alert is critical.
   *
   * @return true if critical, false otherwise
   */
  public boolean isCritical() {
    return severity == AlertSeverity.CRITICAL;
  }
  
  /**
   * Checks if the alert is high severity.
   *
   * @return true if high severity, false otherwise
   */
  public boolean isHighSeverity() {
    return severity == AlertSeverity.HIGH;
  }
  
  /**
   * Checks if the alert is medium severity.
   *
   * @return true if medium severity, false otherwise
   */
  public boolean isMediumSeverity() {
    return severity == AlertSeverity.MEDIUM;
  }
  
  /**
   * Checks if the alert is low severity.
   *
   * @return true if low severity, false otherwise
   */
  public boolean isLowSeverity() {
    return severity == AlertSeverity.LOW;
  }
  
  /**
   * Checks if the alert is info severity.
   *
   * @return true if info severity, false otherwise
   */
  public boolean isInfoSeverity() {
    return severity == AlertSeverity.INFO;
  }
  
  /**
   * Acknowledges the alert.
   *
   * @param acknowledgedBy the user who acknowledged the alert
   */
  public void acknowledge(String acknowledgedBy) {
    this.status = AlertStatus.ACKNOWLEDGED;
    this.acknowledgedAt = LocalDateTime.now();
    this.acknowledgedBy = acknowledgedBy;
  }
  
  /**
   * Resolves the alert.
   *
   * @param resolvedBy the user who resolved the alert
   * @param resolutionNotes the resolution notes
   */
  public void resolve(String resolvedBy, String resolutionNotes) {
    this.status = AlertStatus.RESOLVED;
    this.resolvedAt = LocalDateTime.now();
    this.resolvedBy = resolvedBy;
    this.resolutionNotes = resolutionNotes;
  }
  
  /**
   * Suppresses the alert.
   */
  public void suppress() {
    this.status = AlertStatus.SUPPRESSED;
  }
  
  /**
   * Expires the alert.
   */
  public void expire() {
    this.status = AlertStatus.EXPIRED;
  }
  
  /**
   * Calculates the alert duration in minutes.
   *
   * @return the duration in minutes, or null if not calculable
   */
  public Long calculateDurationMinutes() {
    if (alertTimestamp == null) return null;
    LocalDateTime endTime = resolvedAt != null ? resolvedAt : LocalDateTime.now();
    return java.time.Duration.between(alertTimestamp, endTime).toMinutes();
  }
  
  /**
   * Calculates the time to acknowledgment in minutes.
   *
   * @return the time to acknowledgment in minutes, or null if not acknowledged
   */
  public Long calculateTimeToAcknowledgmentMinutes() {
    if (alertTimestamp == null || acknowledgedAt == null) return null;
    return java.time.Duration.between(alertTimestamp, acknowledgedAt).toMinutes();
  }
  
  /**
   * Calculates the time to resolution in minutes.
   *
   * @return the time to resolution in minutes, or null if not resolved
   */
  public Long calculateTimeToResolutionMinutes() {
    if (alertTimestamp == null || resolvedAt == null) return null;
    return java.time.Duration.between(alertTimestamp, resolvedAt).toMinutes();
  }
  
  /**
   * Gets the alert summary.
   *
   * @return the alert summary string
   */
  public String getSummary() {
    return String.format("Alert[%s] %s - %s (%s)", 
        alertId, alertName, severity, status);
  }
}
