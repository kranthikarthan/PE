package com.payments.settlement.repository;

import com.payments.settlement.domain.SettlementState;
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
 * Repository interface for SettlementState entity.
 *
 * <p>This repository provides data access methods for settlement states
 * including querying by various criteria, state management, and state tracking.
 *
 * @since PE-409
 */
@Repository
public interface SettlementStateRepository extends JpaRepository<SettlementState, Long> {
  
  /**
   * Finds states by workflow ID.
   *
   * @param workflowId the workflow ID
   * @return list of states
   */
  List<SettlementState> findByWorkflowId(Long workflowId);
  
  /**
   * Finds states by workflow ID ordered by timestamp.
   *
   * @param workflowId the workflow ID
   * @return list of states
   */
  List<SettlementState> findByWorkflowIdOrderByStateTimestampDesc(Long workflowId);
  
  /**
   * Finds states by position ID.
   *
   * @param positionId the position ID
   * @return list of states
   */
  List<SettlementState> findByPositionId(Long positionId);
  
  /**
   * Finds states by position ID ordered by timestamp.
   *
   * @param positionId the position ID
   * @return list of states
   */
  List<SettlementState> findByPositionIdOrderByStateTimestampDesc(Long positionId);
  
  /**
   * Finds states by tenant ID with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination information
   * @return page of states
   */
  Page<SettlementState> findByTenantId(String tenantId, Pageable pageable);
  
  /**
   * Finds states by status and tenant ID.
   *
   * @param status the state status
   * @param tenantId the tenant ID
   * @return list of states
   */
  List<SettlementState> findByStatusAndTenantId(SettlementState.StateStatus status, String tenantId);
  
  /**
   * Finds states by state type and tenant ID.
   *
   * @param stateType the state type
   * @param tenantId the tenant ID
   * @return list of states
   */
  List<SettlementState> findByStateTypeAndTenantId(SettlementState.StateType stateType, String tenantId);
  
  /**
   * Finds active states by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of active states
   */
  @Query("SELECT s FROM SettlementState s WHERE s.tenantId = :tenantId AND s.status = 'ACTIVE'")
  List<SettlementState> findActiveStatesByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds completed states by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of completed states
   */
  @Query("SELECT s FROM SettlementState s WHERE s.tenantId = :tenantId AND s.status = 'COMPLETED'")
  List<SettlementState> findCompletedStatesByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds failed states by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of failed states
   */
  @Query("SELECT s FROM SettlementState s WHERE s.tenantId = :tenantId AND s.status = 'FAILED'")
  List<SettlementState> findFailedStatesByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds states by participant ID and tenant ID.
   *
   * @param participantId the participant ID
   * @param tenantId the tenant ID
   * @return list of states
   */
  List<SettlementState> findByParticipantIdAndTenantId(String participantId, String tenantId);
  
  /**
   * Finds states by transaction ID and tenant ID.
   *
   * @param transactionId the transaction ID
   * @param tenantId the tenant ID
   * @return list of states
   */
  List<SettlementState> findByTransactionIdAndTenantId(String transactionId, String tenantId);
  
  /**
   * Finds states by business unit and tenant ID.
   *
   * @param businessUnitId the business unit ID
   * @param tenantId the tenant ID
   * @return list of states
   */
  List<SettlementState> findByBusinessUnitIdAndTenantId(String businessUnitId, String tenantId);
  
  /**
   * Finds states by date range and tenant ID.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of states
   */
  @Query("SELECT s FROM SettlementState s WHERE s.tenantId = :tenantId " +
         "AND s.stateTimestamp >= :startDate AND s.stateTimestamp <= :endDate " +
         "ORDER BY s.stateTimestamp DESC")
  List<SettlementState> findByDateRangeAndTenantId(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds states by duration range.
   *
   * @param minDurationSeconds the minimum duration in seconds
   * @param maxDurationSeconds the maximum duration in seconds
   * @param tenantId the tenant ID
   * @return list of states
   */
  @Query("SELECT s FROM SettlementState s WHERE s.tenantId = :tenantId " +
         "AND s.durationSeconds >= :minDurationSeconds AND s.durationSeconds <= :maxDurationSeconds " +
         "ORDER BY s.durationSeconds DESC")
  List<SettlementState> findByDurationRange(
      @Param("minDurationSeconds") Long minDurationSeconds,
      @Param("maxDurationSeconds") Long maxDurationSeconds,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds states by retry count range.
   *
   * @param minRetryCount the minimum retry count
   * @param maxRetryCount the maximum retry count
   * @param tenantId the tenant ID
   * @return list of states
   */
  @Query("SELECT s FROM SettlementState s WHERE s.tenantId = :tenantId " +
         "AND s.retryCount >= :minRetryCount AND s.retryCount <= :maxRetryCount " +
         "ORDER BY s.retryCount DESC")
  List<SettlementState> findByRetryCountRange(
      @Param("minRetryCount") Integer minRetryCount,
      @Param("maxRetryCount") Integer maxRetryCount,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds states by state name and tenant ID.
   *
   * @param stateName the state name
   * @param tenantId the tenant ID
   * @return list of states
   */
  List<SettlementState> findByStateNameAndTenantId(String stateName, String tenantId);
  
  /**
   * Finds states by previous state and tenant ID.
   *
   * @param previousState the previous state
   * @param tenantId the tenant ID
   * @return list of states
   */
  List<SettlementState> findByPreviousStateAndTenantId(String previousState, String tenantId);
  
  /**
   * Finds states by next state and tenant ID.
   *
   * @param nextState the next state
   * @param tenantId the tenant ID
   * @return list of states
   */
  List<SettlementState> findByNextStateAndTenantId(String nextState, String tenantId);
  
  /**
   * Finds the current workflow state.
   *
   * @param workflowId the workflow ID
   * @return optional current state
   */
  @Query("SELECT s FROM SettlementState s WHERE s.workflowId = :workflowId " +
         "AND s.stateType = 'WORKFLOW' AND s.status = 'ACTIVE' " +
         "ORDER BY s.stateTimestamp DESC LIMIT 1")
  Optional<SettlementState> findCurrentWorkflowState(@Param("workflowId") Long workflowId);
  
  /**
   * Finds the current position state.
   *
   * @param positionId the position ID
   * @return optional current state
   */
  @Query("SELECT s FROM SettlementState s WHERE s.positionId = :positionId " +
         "AND s.stateType = 'POSITION' AND s.status = 'ACTIVE' " +
         "ORDER BY s.stateTimestamp DESC LIMIT 1")
  Optional<SettlementState> findCurrentPositionState(@Param("positionId") Long positionId);
  
  /**
   * Finds states with high duration.
   *
   * @param tenantId the tenant ID
   * @param minDurationSeconds the minimum duration in seconds
   * @return list of states
   */
  @Query("SELECT s FROM SettlementState s WHERE s.tenantId = :tenantId " +
         "AND s.durationSeconds >= :minDurationSeconds " +
         "ORDER BY s.durationSeconds DESC")
  List<SettlementState> findHighDurationStates(
      @Param("tenantId") String tenantId, @Param("minDurationSeconds") Long minDurationSeconds);
  
  /**
   * Finds states with high retry counts.
   *
   * @param tenantId the tenant ID
   * @param minRetryCount the minimum retry count
   * @return list of states
   */
  @Query("SELECT s FROM SettlementState s WHERE s.tenantId = :tenantId " +
         "AND s.retryCount >= :minRetryCount " +
         "ORDER BY s.retryCount DESC")
  List<SettlementState> findHighRetryCountStates(
      @Param("tenantId") String tenantId, @Param("minRetryCount") Integer minRetryCount);
  
  /**
   * Counts states by status and tenant ID.
   *
   * @param status the state status
   * @param tenantId the tenant ID
   * @return count of states
   */
  long countByStatusAndTenantId(SettlementState.StateStatus status, String tenantId);
  
  /**
   * Counts states by state type and tenant ID.
   *
   * @param stateType the state type
   * @param tenantId the tenant ID
   * @return count of states
   */
  long countByStateTypeAndTenantId(SettlementState.StateType stateType, String tenantId);
  
  /**
   * Counts states by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return count of states
   */
  long countByTenantId(String tenantId);
  
  /**
   * Gets state statistics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return state statistics
   */
  @Query("SELECT " +
         "COUNT(s) as totalStates, " +
         "COUNT(CASE WHEN s.status = 'ACTIVE' THEN 1 END) as activeStates, " +
         "COUNT(CASE WHEN s.status = 'COMPLETED' THEN 1 END) as completedStates, " +
         "COUNT(CASE WHEN s.status = 'FAILED' THEN 1 END) as failedStates, " +
         "COUNT(CASE WHEN s.stateType = 'WORKFLOW' THEN 1 END) as workflowStates, " +
         "COUNT(CASE WHEN s.stateType = 'POSITION' THEN 1 END) as positionStates, " +
         "AVG(s.durationSeconds) as averageDurationSeconds, " +
         "AVG(s.retryCount) as averageRetryCount " +
         "FROM SettlementState s WHERE s.tenantId = :tenantId")
  Object[] getStateStatisticsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Gets state statistics by workflow.
   *
   * @param workflowId the workflow ID
   * @return state statistics
   */
  @Query("SELECT " +
         "COUNT(s) as totalStates, " +
         "COUNT(CASE WHEN s.status = 'ACTIVE' THEN 1 END) as activeStates, " +
         "COUNT(CASE WHEN s.status = 'COMPLETED' THEN 1 END) as completedStates, " +
         "COUNT(CASE WHEN s.status = 'FAILED' THEN 1 END) as failedStates, " +
         "AVG(s.durationSeconds) as averageDurationSeconds, " +
         "AVG(s.retryCount) as averageRetryCount " +
         "FROM SettlementState s WHERE s.workflowId = :workflowId")
  Object[] getStateStatisticsByWorkflow(@Param("workflowId") Long workflowId);
  
  /**
   * Updates state status.
   *
   * @param stateId the state ID
   * @param status the new status
   * @return number of updated records
   */
  @Query("UPDATE SettlementState s SET s.status = :status WHERE s.id = :stateId")
  int updateStateStatus(@Param("stateId") Long stateId, @Param("status") SettlementState.StateStatus status);
  
  /**
   * Deletes old states by date and tenant ID.
   *
   * @param cutoffDate the cutoff date
   * @param tenantId the tenant ID
   * @return number of deleted records
   */
  @Query("DELETE FROM SettlementState s WHERE s.tenantId = :tenantId " +
         "AND s.stateTimestamp < :cutoffDate")
  int deleteOldStates(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("tenantId") String tenantId);
}
