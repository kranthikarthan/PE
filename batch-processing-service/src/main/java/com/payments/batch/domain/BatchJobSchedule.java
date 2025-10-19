package com.payments.batch.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity for batch job scheduling.
 *
 * <p>This entity represents job scheduling configuration including cron expressions,
 * time-based triggers, and execution parameters. It provides comprehensive
 * scheduling capabilities for batch processing operations.
 *
 * @since PE-406
 */
@Entity
@Table(name = "batch_job_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobSchedule {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "schedule_id", nullable = false, unique = true, length = 100)
  private String scheduleId;
  
  @Column(name = "job_name", nullable = false, length = 255)
  private String jobName;
  
  @Column(name = "tenant_id", nullable = false, length = 50)
  private String tenantId;
  
  @Column(name = "business_unit_id", length = 50)
  private String businessUnitId;
  
  @Column(name = "schedule_name", nullable = false, length = 255)
  private String scheduleName;
  
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
  
  @Column(name = "cron_expression", length = 255)
  private String cronExpression;
  
  @Column(name = "scheduled_time")
  private LocalDateTime scheduledTime;
  
  @Column(name = "time_zone", length = 100)
  private String timeZone = "UTC";
  
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private ScheduleStatus status = ScheduleStatus.ACTIVE;
  
  @Column(name = "enabled", nullable = false)
  private Boolean enabled = true;
  
  @Column(name = "start_date")
  private LocalDateTime startDate;
  
  @Column(name = "end_date")
  private LocalDateTime endDate;
  
  @Column(name = "max_executions")
  private Integer maxExecutions;
  
  @Column(name = "execution_count")
  private Integer executionCount = 0;
  
  @Column(name = "last_execution_time")
  private LocalDateTime lastExecutionTime;
  
  @Column(name = "last_execution_status", length = 50)
  private String lastExecutionStatus;
  
  @Column(name = "next_execution_time")
  private LocalDateTime nextExecutionTime;
  
  @Column(name = "priority")
  private Integer priority = 5;
  
  @Column(name = "async")
  private Boolean async = false;
  
  @Column(name = "parameters", columnDefinition = "JSONB")
  private String parameters;
  
  @Column(name = "configuration", columnDefinition = "JSONB")
  private String configuration;
  
  @Column(name = "notification_settings", columnDefinition = "JSONB")
  private String notificationSettings;
  
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
   * Enumeration of schedule statuses.
   */
  public enum ScheduleStatus {
    ACTIVE, INACTIVE, PAUSED, DISABLED
  }
  
  /**
   * Checks if the schedule is currently active.
   *
   * @return true if active, false otherwise
   */
  public boolean isActive() {
    return status == ScheduleStatus.ACTIVE && enabled && isWithinDateRange();
  }
  
  /**
   * Checks if the current time is within the schedule date range.
   *
   * @return true if within range, false otherwise
   */
  public boolean isWithinDateRange() {
    LocalDateTime now = LocalDateTime.now();
    
    if (startDate != null && now.isBefore(startDate)) {
      return false;
    }
    
    if (endDate != null && now.isAfter(endDate)) {
      return false;
    }
    
    return true;
  }
  
  /**
   * Checks if the schedule has reached its maximum executions.
   *
   * @return true if max executions reached, false otherwise
   */
  public boolean hasReachedMaxExecutions() {
    return maxExecutions != null && executionCount != null && executionCount >= maxExecutions;
  }
  
  /**
   * Checks if the schedule can be executed.
   *
   * @return true if can be executed, false otherwise
   */
  public boolean canExecute() {
    return isActive() && !hasReachedMaxExecutions();
  }
  
  /**
   * Increments the execution count.
   */
  public void incrementExecutionCount() {
    if (executionCount == null) {
      executionCount = 0;
    }
    executionCount++;
  }
  
  /**
   * Updates the last execution information.
   *
   * @param executionTime the execution time
   * @param executionStatus the execution status
   */
  public void updateLastExecution(LocalDateTime executionTime, String executionStatus) {
    this.lastExecutionTime = executionTime;
    this.lastExecutionStatus = executionStatus;
  }
  
  /**
   * Calculates the next execution time based on the schedule.
   *
   * @return next execution time, or null if not calculable
   */
  public LocalDateTime calculateNextExecutionTime() {
    if (cronExpression != null && !cronExpression.trim().isEmpty()) {
      // TODO: Implement cron expression parsing
      return LocalDateTime.now().plusHours(1);
    }
    
    if (scheduledTime != null) {
      return scheduledTime;
    }
    
    return LocalDateTime.now().plusMinutes(5);
  }
  
  /**
   * Gets the schedule summary.
   *
   * @return schedule summary string
   */
  public String getSummary() {
    return String.format("Schedule[%s] %s - %s (%s)", 
        scheduleId, jobName, status, 
        nextExecutionTime != null ? nextExecutionTime.toString() : "N/A");
  }
}
