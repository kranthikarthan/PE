package com.payments.batch.repository;

import com.payments.batch.domain.BatchJobExecutionMetadata;
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
 * Repository interface for BatchJobExecutionMetadata entity.
 *
 * <p>This repository provides data access methods for batch job execution metadata
 * including querying by various criteria, performance metrics, and audit trails.
 *
 * @since PE-406
 */
@Repository
public interface BatchJobExecutionMetadataRepository extends JpaRepository<BatchJobExecutionMetadata, Long> {
  
  /**
   * Finds metadata by job execution ID.
   *
   * @param jobExecutionId the job execution ID
   * @return optional metadata
   */
  Optional<BatchJobExecutionMetadata> findByJobExecutionId(Long jobExecutionId);
  
  /**
   * Finds metadata by job name and tenant ID.
   *
   * @param jobName the job name
   * @param tenantId the tenant ID
   * @return list of metadata
   */
  List<BatchJobExecutionMetadata> findByJobNameAndTenantId(String jobName, String tenantId);
  
  /**
   * Finds metadata by tenant ID with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination information
   * @return page of metadata
   */
  Page<BatchJobExecutionMetadata> findByTenantId(String tenantId, Pageable pageable);
  
  /**
   * Finds metadata by status and tenant ID.
   *
   * @param status the execution status
   * @param tenantId the tenant ID
   * @return list of metadata
   */
  List<BatchJobExecutionMetadata> findByStatusAndTenantId(
      BatchJobExecutionMetadata.ExecutionStatus status, String tenantId);
  
  /**
   * Finds running executions by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of running executions
   */
  @Query("SELECT m FROM BatchJobExecutionMetadata m WHERE m.tenantId = :tenantId AND m.status = 'RUNNING'")
  List<BatchJobExecutionMetadata> findRunningExecutionsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds executions by date range and tenant ID.
   *
   * @param startTime the start time
   * @param endTime the end time
   * @param tenantId the tenant ID
   * @return list of metadata
   */
  @Query("SELECT m FROM BatchJobExecutionMetadata m WHERE m.tenantId = :tenantId " +
         "AND m.startTime >= :startTime AND m.startTime <= :endTime " +
         "ORDER BY m.startTime DESC")
  List<BatchJobExecutionMetadata> findByDateRangeAndTenantId(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds executions by job name and date range.
   *
   * @param jobName the job name
   * @param startTime the start time
   * @param endTime the end time
   * @param tenantId the tenant ID
   * @return list of metadata
   */
  @Query("SELECT m FROM BatchJobExecutionMetadata m WHERE m.jobName = :jobName " +
         "AND m.tenantId = :tenantId AND m.startTime >= :startTime AND m.startTime <= :endTime " +
         "ORDER BY m.startTime DESC")
  List<BatchJobExecutionMetadata> findByJobNameAndDateRange(
      @Param("jobName") String jobName,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      @Param("tenantId") String tenantId);
  
  /**
   * Counts executions by status and tenant ID.
   *
   * @param status the execution status
   * @param tenantId the tenant ID
   * @return count of executions
   */
  long countByStatusAndTenantId(BatchJobExecutionMetadata.ExecutionStatus status, String tenantId);
  
  /**
   * Counts executions by job name and tenant ID.
   *
   * @param jobName the job name
   * @param tenantId the tenant ID
   * @return count of executions
   */
  long countByJobNameAndTenantId(String jobName, String tenantId);
  
  /**
   * Finds the latest execution by job name and tenant ID.
   *
   * @param jobName the job name
   * @param tenantId the tenant ID
   * @return optional metadata
   */
  @Query("SELECT m FROM BatchJobExecutionMetadata m WHERE m.jobName = :jobName " +
         "AND m.tenantId = :tenantId ORDER BY m.startTime DESC LIMIT 1")
  Optional<BatchJobExecutionMetadata> findLatestByJobNameAndTenantId(
      @Param("jobName") String jobName, @Param("tenantId") String tenantId);
  
  /**
   * Finds executions with high failure rates.
   *
   * @param tenantId the tenant ID
   * @param minFailureRate the minimum failure rate percentage
   * @return list of metadata
   */
  @Query("SELECT m FROM BatchJobExecutionMetadata m WHERE m.tenantId = :tenantId " +
         "AND m.status = 'FAILED' AND m.recordsFailed > 0 " +
         "AND (m.recordsFailed * 100.0 / NULLIF(m.totalRecords, 0)) >= :minFailureRate")
  List<BatchJobExecutionMetadata> findHighFailureRateExecutions(
      @Param("tenantId") String tenantId, @Param("minFailureRate") Double minFailureRate);
  
  /**
   * Finds executions by execution type and tenant ID.
   *
   * @param executionType the execution type
   * @param tenantId the tenant ID
   * @return list of metadata
   */
  List<BatchJobExecutionMetadata> findByExecutionTypeAndTenantId(
      BatchJobExecutionMetadata.ExecutionType executionType, String tenantId);
  
  /**
   * Finds executions by priority range and tenant ID.
   *
   * @param minPriority the minimum priority
   * @param maxPriority the maximum priority
   * @param tenantId the tenant ID
   * @return list of metadata
   */
  @Query("SELECT m FROM BatchJobExecutionMetadata m WHERE m.tenantId = :tenantId " +
         "AND m.priority >= :minPriority AND m.priority <= :maxPriority " +
         "ORDER BY m.priority DESC, m.startTime DESC")
  List<BatchJobExecutionMetadata> findByPriorityRangeAndTenantId(
      @Param("minPriority") Integer minPriority,
      @Param("maxPriority") Integer maxPriority,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds executions with long duration.
   *
   * @param tenantId the tenant ID
   * @param minDurationSeconds the minimum duration in seconds
   * @return list of metadata
   */
  @Query("SELECT m FROM BatchJobExecutionMetadata m WHERE m.tenantId = :tenantId " +
         "AND m.durationSeconds >= :minDurationSeconds " +
         "ORDER BY m.durationSeconds DESC")
  List<BatchJobExecutionMetadata> findLongRunningExecutions(
      @Param("tenantId") String tenantId, @Param("minDurationSeconds") Integer minDurationSeconds);
  
  /**
   * Finds executions with high memory usage.
   *
   * @param tenantId the tenant ID
   * @param minMemoryUsageMb the minimum memory usage in MB
   * @return list of metadata
   */
  @Query("SELECT m FROM BatchJobExecutionMetadata m WHERE m.tenantId = :tenantId " +
         "AND m.memoryUsageMb >= :minMemoryUsageMb " +
         "ORDER BY m.memoryUsageMb DESC")
  List<BatchJobExecutionMetadata> findHighMemoryUsageExecutions(
      @Param("tenantId") String tenantId, @Param("minMemoryUsageMb") Double minMemoryUsageMb);
  
  /**
   * Finds executions with high CPU usage.
   *
   * @param tenantId the tenant ID
   * @param minCpuUsagePercentage the minimum CPU usage percentage
   * @return list of metadata
   */
  @Query("SELECT m FROM BatchJobExecutionMetadata m WHERE m.tenantId = :tenantId " +
         "AND m.cpuUsagePercentage >= :minCpuUsagePercentage " +
         "ORDER BY m.cpuUsagePercentage DESC")
  List<BatchJobExecutionMetadata> findHighCpuUsageExecutions(
      @Param("tenantId") String tenantId, @Param("minCpuUsagePercentage") Double minCpuUsagePercentage);
  
  /**
   * Deletes old executions by date and tenant ID.
   *
   * @param cutoffDate the cutoff date
   * @param tenantId the tenant ID
   * @return number of deleted records
   */
  @Query("DELETE FROM BatchJobExecutionMetadata m WHERE m.tenantId = :tenantId " +
         "AND m.startTime < :cutoffDate")
  int deleteOldExecutions(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("tenantId") String tenantId);
  
  /**
   * Gets execution statistics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return execution statistics
   */
  @Query("SELECT " +
         "COUNT(m) as totalExecutions, " +
         "COUNT(CASE WHEN m.status = 'COMPLETED' THEN 1 END) as successfulExecutions, " +
         "COUNT(CASE WHEN m.status = 'FAILED' THEN 1 END) as failedExecutions, " +
         "AVG(m.durationSeconds) as averageDuration, " +
         "SUM(m.recordsProcessed) as totalRecordsProcessed " +
         "FROM BatchJobExecutionMetadata m WHERE m.tenantId = :tenantId")
  Object[] getExecutionStatisticsByTenantId(@Param("tenantId") String tenantId);
}
