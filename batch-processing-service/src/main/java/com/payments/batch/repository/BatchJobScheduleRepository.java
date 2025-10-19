package com.payments.batch.repository;

import com.payments.batch.domain.BatchJobSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BatchJobSchedule entity.
 *
 * <p>This repository provides data access methods for batch job scheduling
 * including querying by various criteria, schedule management, and execution tracking.
 *
 * @since PE-406
 */
@Repository
public interface BatchJobScheduleRepository extends JpaRepository<BatchJobSchedule, Long> {
  
  /**
   * Finds schedule by schedule ID.
   *
   * @param scheduleId the schedule ID
   * @return optional schedule
   */
  Optional<BatchJobSchedule> findByScheduleId(String scheduleId);
  
  /**
   * Finds schedules by job name and tenant ID.
   *
   * @param jobName the job name
   * @param tenantId the tenant ID
   * @return list of schedules
   */
  List<BatchJobSchedule> findByJobNameAndTenantId(String jobName, String tenantId);
  
  /**
   * Finds schedules by tenant ID with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination information
   * @return page of schedules
   */
  Page<BatchJobSchedule> findByTenantId(String tenantId, Pageable pageable);
  
  /**
   * Finds active schedules by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of active schedules
   */
  @Query("SELECT s FROM BatchJobSchedule s WHERE s.tenantId = :tenantId " +
         "AND s.status = 'ACTIVE' AND s.enabled = true")
  List<BatchJobSchedule> findActiveSchedulesByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds schedules ready for execution.
   *
   * @param currentTime the current time
   * @param tenantId the tenant ID
   * @return list of schedules ready for execution
   */
  @Query("SELECT s FROM BatchJobSchedule s WHERE s.tenantId = :tenantId " +
         "AND s.status = 'ACTIVE' AND s.enabled = true " +
         "AND s.nextExecutionTime <= :currentTime " +
         "AND (s.maxExecutions IS NULL OR s.executionCount < s.maxExecutions) " +
         "AND (s.startDate IS NULL OR s.startDate <= :currentTime) " +
         "AND (s.endDate IS NULL OR s.endDate > :currentTime)")
  List<BatchJobSchedule> findSchedulesReadyForExecution(
      @Param("currentTime") LocalDateTime currentTime, @Param("tenantId") String tenantId);
  
  /**
   * Finds schedules by status and tenant ID.
   *
   * @param status the schedule status
   * @param tenantId the tenant ID
   * @return list of schedules
   */
  List<BatchJobSchedule> findByStatusAndTenantId(BatchJobSchedule.ScheduleStatus status, String tenantId);
  
  /**
   * Finds schedules by enabled status and tenant ID.
   *
   * @param enabled the enabled status
   * @param tenantId the tenant ID
   * @return list of schedules
   */
  List<BatchJobSchedule> findByEnabledAndTenantId(Boolean enabled, String tenantId);
  
  /**
   * Finds schedules by priority range and tenant ID.
   *
   * @param minPriority the minimum priority
   * @param maxPriority the maximum priority
   * @param tenantId the tenant ID
   * @return list of schedules
   */
  @Query("SELECT s FROM BatchJobSchedule s WHERE s.tenantId = :tenantId " +
         "AND s.priority >= :minPriority AND s.priority <= :maxPriority " +
         "ORDER BY s.priority DESC, s.nextExecutionTime ASC")
  List<BatchJobSchedule> findByPriorityRangeAndTenantId(
      @Param("minPriority") Integer minPriority,
      @Param("maxPriority") Integer maxPriority,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds schedules by execution type and tenant ID.
   *
   * @param async the async flag
   * @param tenantId the tenant ID
   * @return list of schedules
   */
  List<BatchJobSchedule> findByAsyncAndTenantId(Boolean async, String tenantId);
  
  /**
   * Finds schedules with high execution counts.
   *
   * @param tenantId the tenant ID
   * @param minExecutionCount the minimum execution count
   * @return list of schedules
   */
  @Query("SELECT s FROM BatchJobSchedule s WHERE s.tenantId = :tenantId " +
         "AND s.executionCount >= :minExecutionCount " +
         "ORDER BY s.executionCount DESC")
  List<BatchJobSchedule> findHighExecutionCountSchedules(
      @Param("tenantId") String tenantId, @Param("minExecutionCount") Integer minExecutionCount);
  
  /**
   * Finds schedules by date range and tenant ID.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of schedules
   */
  @Query("SELECT s FROM BatchJobSchedule s WHERE s.tenantId = :tenantId " +
         "AND s.createdAt >= :startDate AND s.createdAt <= :endDate " +
         "ORDER BY s.createdAt DESC")
  List<BatchJobSchedule> findByDateRangeAndTenantId(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds schedules by next execution time range.
   *
   * @param startTime the start time
   * @param endTime the end time
   * @param tenantId the tenant ID
   * @return list of schedules
   */
  @Query("SELECT s FROM BatchJobSchedule s WHERE s.tenantId = :tenantId " +
         "AND s.nextExecutionTime >= :startTime AND s.nextExecutionTime <= :endTime " +
         "ORDER BY s.nextExecutionTime ASC")
  List<BatchJobSchedule> findByNextExecutionTimeRange(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds schedules by last execution time range.
   *
   * @param startTime the start time
   * @param endTime the end time
   * @param tenantId the tenant ID
   * @return list of schedules
   */
  @Query("SELECT s FROM BatchJobSchedule s WHERE s.tenantId = :tenantId " +
         "AND s.lastExecutionTime >= :startTime AND s.lastExecutionTime <= :endTime " +
         "ORDER BY s.lastExecutionTime DESC")
  List<BatchJobSchedule> findByLastExecutionTimeRange(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds schedules by last execution status.
   *
   * @param lastExecutionStatus the last execution status
   * @param tenantId the tenant ID
   * @return list of schedules
   */
  List<BatchJobSchedule> findByLastExecutionStatusAndTenantId(String lastExecutionStatus, String tenantId);
  
  /**
   * Counts schedules by status and tenant ID.
   *
   * @param status the schedule status
   * @param tenantId the tenant ID
   * @return count of schedules
   */
  long countByStatusAndTenantId(BatchJobSchedule.ScheduleStatus status, String tenantId);
  
  /**
   * Counts schedules by job name and tenant ID.
   *
   * @param jobName the job name
   * @param tenantId the tenant ID
   * @return count of schedules
   */
  long countByJobNameAndTenantId(String jobName, String tenantId);
  
  /**
   * Finds schedules with expired end dates.
   *
   * @param currentTime the current time
   * @param tenantId the tenant ID
   * @return list of expired schedules
   */
  @Query("SELECT s FROM BatchJobSchedule s WHERE s.tenantId = :tenantId " +
         "AND s.endDate IS NOT NULL AND s.endDate <= :currentTime")
  List<BatchJobSchedule> findExpiredSchedules(
      @Param("currentTime") LocalDateTime currentTime, @Param("tenantId") String tenantId);
  
  /**
   * Finds schedules with max executions reached.
   *
   * @param tenantId the tenant ID
   * @return list of schedules with max executions reached
   */
  @Query("SELECT s FROM BatchJobSchedule s WHERE s.tenantId = :tenantId " +
         "AND s.maxExecutions IS NOT NULL AND s.executionCount >= s.maxExecutions")
  List<BatchJobSchedule> findSchedulesWithMaxExecutionsReached(@Param("tenantId") String tenantId);
  
  /**
   * Updates next execution time for a schedule.
   *
   * @param scheduleId the schedule ID
   * @param nextExecutionTime the next execution time
   * @return number of updated records
   */
  @Query("UPDATE BatchJobSchedule s SET s.nextExecutionTime = :nextExecutionTime " +
         "WHERE s.scheduleId = :scheduleId")
  int updateNextExecutionTime(@Param("scheduleId") String scheduleId, 
                              @Param("nextExecutionTime") LocalDateTime nextExecutionTime);
  
  /**
   * Increments execution count for a schedule.
   *
   * @param scheduleId the schedule ID
   * @return number of updated records
   */
  @Query("UPDATE BatchJobSchedule s SET s.executionCount = s.executionCount + 1 " +
         "WHERE s.scheduleId = :scheduleId")
  int incrementExecutionCount(@Param("scheduleId") String scheduleId);
  
  /**
   * Updates last execution information for a schedule.
   *
   * @param scheduleId the schedule ID
   * @param lastExecutionTime the last execution time
   * @param lastExecutionStatus the last execution status
   * @return number of updated records
   */
  @Query("UPDATE BatchJobSchedule s SET s.lastExecutionTime = :lastExecutionTime, " +
         "s.lastExecutionStatus = :lastExecutionStatus WHERE s.scheduleId = :scheduleId")
  int updateLastExecution(@Param("scheduleId") String scheduleId,
                          @Param("lastExecutionTime") LocalDateTime lastExecutionTime,
                          @Param("lastExecutionStatus") String lastExecutionStatus);
  
  /**
   * Deletes old schedules by date and tenant ID.
   *
   * @param cutoffDate the cutoff date
   * @param tenantId the tenant ID
   * @return number of deleted records
   */
  @Query("DELETE FROM BatchJobSchedule s WHERE s.tenantId = :tenantId " +
         "AND s.createdAt < :cutoffDate")
  int deleteOldSchedules(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("tenantId") String tenantId);
  
  /**
   * Gets schedule statistics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return schedule statistics
   */
  @Query("SELECT " +
         "COUNT(s) as totalSchedules, " +
         "COUNT(CASE WHEN s.status = 'ACTIVE' THEN 1 END) as activeSchedules, " +
         "COUNT(CASE WHEN s.enabled = true THEN 1 END) as enabledSchedules, " +
         "SUM(s.executionCount) as totalExecutions " +
         "FROM BatchJobSchedule s WHERE s.tenantId = :tenantId")
  Object[] getScheduleStatisticsByTenantId(@Param("tenantId") String tenantId);
}
