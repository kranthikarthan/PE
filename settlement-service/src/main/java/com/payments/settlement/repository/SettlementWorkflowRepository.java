package com.payments.settlement.repository;

import com.payments.settlement.domain.SettlementWorkflow;
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
 * Repository interface for SettlementWorkflow entity.
 *
 * <p>This repository provides data access methods for settlement workflows
 * including querying by various criteria, status management, and workflow tracking.
 *
 * @since PE-409
 */
@Repository
public interface SettlementWorkflowRepository extends JpaRepository<SettlementWorkflow, Long> {
  
  /**
   * Finds workflow by workflow ID.
   *
   * @param workflowId the workflow ID
   * @return optional workflow
   */
  Optional<SettlementWorkflow> findByWorkflowId(String workflowId);
  
  /**
   * Finds workflows by tenant ID with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination information
   * @return page of workflows
   */
  Page<SettlementWorkflow> findByTenantId(String tenantId, Pageable pageable);
  
  /**
   * Finds workflows by status and tenant ID.
   *
   * @param status the workflow status
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  List<SettlementWorkflow> findByStatusAndTenantId(SettlementWorkflow.WorkflowStatus status, String tenantId);
  
  /**
   * Finds workflows by workflow type and tenant ID.
   *
   * @param workflowType the workflow type
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  List<SettlementWorkflow> findByWorkflowTypeAndTenantId(SettlementWorkflow.WorkflowType workflowType, String tenantId);
  
  /**
   * Finds active workflows by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of active workflows
   */
  @Query("SELECT w FROM SettlementWorkflow w WHERE w.tenantId = :tenantId " +
         "AND w.status IN ('INITIATED', 'VALIDATING', 'PROCESSING', 'SETTLING')")
  List<SettlementWorkflow> findActiveWorkflowsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds completed workflows by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of completed workflows
   */
  @Query("SELECT w FROM SettlementWorkflow w WHERE w.tenantId = :tenantId AND w.status = 'COMPLETED'")
  List<SettlementWorkflow> findCompletedWorkflowsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds failed workflows by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of failed workflows
   */
  @Query("SELECT w FROM SettlementWorkflow w WHERE w.tenantId = :tenantId AND w.status = 'FAILED'")
  List<SettlementWorkflow> findFailedWorkflowsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds workflows by netting cycle ID.
   *
   * @param nettingCycleId the netting cycle ID
   * @return list of workflows
   */
  List<SettlementWorkflow> findByNettingCycleId(Long nettingCycleId);
  
  /**
   * Finds workflows by date range and tenant ID.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  @Query("SELECT w FROM SettlementWorkflow w WHERE w.tenantId = :tenantId " +
         "AND w.startTime >= :startDate AND w.startTime <= :endDate " +
         "ORDER BY w.startTime DESC")
  List<SettlementWorkflow> findByDateRangeAndTenantId(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds workflows by priority range and tenant ID.
   *
   * @param minPriority the minimum priority
   * @param maxPriority the maximum priority
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  @Query("SELECT w FROM SettlementWorkflow w WHERE w.tenantId = :tenantId " +
         "AND w.priority >= :minPriority AND w.priority <= :maxPriority " +
         "ORDER BY w.priority DESC, w.startTime DESC")
  List<SettlementWorkflow> findByPriorityRangeAndTenantId(
      @Param("minPriority") Integer minPriority,
      @Param("maxPriority") Integer maxPriority,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds workflows by currency and tenant ID.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  List<SettlementWorkflow> findByCurrencyAndTenantId(String currency, String tenantId);
  
  /**
   * Finds workflows with high settlement amounts.
   *
   * @param tenantId the tenant ID
   * @param minAmount the minimum settlement amount
   * @return list of workflows
   */
  @Query("SELECT w FROM SettlementWorkflow w WHERE w.tenantId = :tenantId " +
         "AND w.totalSettlementAmount >= :minAmount " +
         "ORDER BY w.totalSettlementAmount DESC")
  List<SettlementWorkflow> findHighSettlementAmountWorkflows(
      @Param("tenantId") String tenantId, @Param("minAmount") java.math.BigDecimal minAmount);
  
  /**
   * Finds workflows by business unit and tenant ID.
   *
   * @param businessUnitId the business unit ID
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  List<SettlementWorkflow> findByBusinessUnitIdAndTenantId(String businessUnitId, String tenantId);
  
  /**
   * Finds workflows by current step and tenant ID.
   *
   * @param currentStep the current step
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  List<SettlementWorkflow> findByCurrentStepAndTenantId(String currentStep, String tenantId);
  
  /**
   * Finds workflows by progress range and tenant ID.
   *
   * @param minProgress the minimum progress
   * @param maxProgress the maximum progress
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  @Query("SELECT w FROM SettlementWorkflow w WHERE w.tenantId = :tenantId " +
         "AND w.stepProgress >= :minProgress AND w.stepProgress <= :maxProgress " +
         "ORDER BY w.stepProgress DESC")
  List<SettlementWorkflow> findByProgressRangeAndTenantId(
      @Param("minProgress") java.math.BigDecimal minProgress,
      @Param("maxProgress") java.math.BigDecimal maxProgress,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds workflows by duration range.
   *
   * @param minDurationMinutes the minimum duration in minutes
   * @param maxDurationMinutes the maximum duration in minutes
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  @Query("SELECT w FROM SettlementWorkflow w WHERE w.tenantId = :tenantId " +
         "AND w.startTime IS NOT NULL AND w.endTime IS NOT NULL " +
         "AND EXTRACT(EPOCH FROM (w.endTime - w.startTime))/60 >= :minDurationMinutes " +
         "AND EXTRACT(EPOCH FROM (w.endTime - w.startTime))/60 <= :maxDurationMinutes " +
         "ORDER BY w.startTime DESC")
  List<SettlementWorkflow> findByDurationRange(
      @Param("minDurationMinutes") Long minDurationMinutes,
      @Param("maxDurationMinutes") Long maxDurationMinutes,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds workflows by participant count range.
   *
   * @param minParticipantCount the minimum participant count
   * @param maxParticipantCount the maximum participant count
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  @Query("SELECT w FROM SettlementWorkflow w WHERE w.tenantId = :tenantId " +
         "AND w.participantCount >= :minParticipantCount AND w.participantCount <= :maxParticipantCount " +
         "ORDER BY w.participantCount DESC")
  List<SettlementWorkflow> findByParticipantCountRange(
      @Param("minParticipantCount") Integer minParticipantCount,
      @Param("maxParticipantCount") Integer maxParticipantCount,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds workflows by position count range.
   *
   * @param minPositionCount the minimum position count
   * @param maxPositionCount the maximum position count
   * @param tenantId the tenant ID
   * @return list of workflows
   */
  @Query("SELECT w FROM SettlementWorkflow w WHERE w.tenantId = :tenantId " +
         "AND w.positionCount >= :minPositionCount AND w.positionCount <= :maxPositionCount " +
         "ORDER BY w.positionCount DESC")
  List<SettlementWorkflow> findByPositionCountRange(
      @Param("minPositionCount") Integer minPositionCount,
      @Param("maxPositionCount") Integer maxPositionCount,
      @Param("tenantId") String tenantId);
  
  /**
   * Counts workflows by status and tenant ID.
   *
   * @param status the workflow status
   * @param tenantId the tenant ID
   * @return count of workflows
   */
  long countByStatusAndTenantId(SettlementWorkflow.WorkflowStatus status, String tenantId);
  
  /**
   * Counts workflows by workflow type and tenant ID.
   *
   * @param workflowType the workflow type
   * @param tenantId the tenant ID
   * @return count of workflows
   */
  long countByWorkflowTypeAndTenantId(SettlementWorkflow.WorkflowType workflowType, String tenantId);
  
  /**
   * Counts workflows by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return count of workflows
   */
  long countByTenantId(String tenantId);
  
  /**
   * Gets workflow statistics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return workflow statistics
   */
  @Query("SELECT " +
         "COUNT(w) as totalWorkflows, " +
         "COUNT(CASE WHEN w.status = 'ACTIVE' THEN 1 END) as activeWorkflows, " +
         "COUNT(CASE WHEN w.status = 'COMPLETED' THEN 1 END) as completedWorkflows, " +
         "COUNT(CASE WHEN w.status = 'FAILED' THEN 1 END) as failedWorkflows, " +
         "AVG(w.totalSettlementAmount) as averageSettlementAmount, " +
         "SUM(w.totalSettlementAmount) as totalSettlementAmount, " +
         "AVG(w.participantCount) as averageParticipantCount, " +
         "AVG(w.positionCount) as averagePositionCount " +
         "FROM SettlementWorkflow w WHERE w.tenantId = :tenantId")
  Object[] getWorkflowStatisticsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Gets workflow statistics by workflow type.
   *
   * @param workflowType the workflow type
   * @param tenantId the tenant ID
   * @return workflow statistics
   */
  @Query("SELECT " +
         "COUNT(w) as totalWorkflows, " +
         "AVG(w.totalSettlementAmount) as averageSettlementAmount, " +
         "SUM(w.totalSettlementAmount) as totalSettlementAmount, " +
         "AVG(w.participantCount) as averageParticipantCount, " +
         "AVG(w.positionCount) as averagePositionCount " +
         "FROM SettlementWorkflow w WHERE w.workflowType = :workflowType AND w.tenantId = :tenantId")
  Object[] getWorkflowStatisticsByType(@Param("workflowType") SettlementWorkflow.WorkflowType workflowType, 
                                      @Param("tenantId") String tenantId);
  
  /**
   * Updates workflow status.
   *
   * @param workflowId the workflow ID
   * @param status the new status
   * @return number of updated records
   */
  @Query("UPDATE SettlementWorkflow w SET w.status = :status WHERE w.workflowId = :workflowId")
  int updateWorkflowStatus(@Param("workflowId") String workflowId, 
                          @Param("status") SettlementWorkflow.WorkflowStatus status);
  
  /**
   * Deletes old workflows by date and tenant ID.
   *
   * @param cutoffDate the cutoff date
   * @param tenantId the tenant ID
   * @return number of deleted records
   */
  @Query("DELETE FROM SettlementWorkflow w WHERE w.tenantId = :tenantId " +
         "AND w.startTime < :cutoffDate")
  int deleteOldWorkflows(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("tenantId") String tenantId);
}
