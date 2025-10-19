package com.payments.settlement.repository;

import com.payments.domain.settlement.SettlementOrchestration;
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
 * Repository interface for SettlementOrchestration entity.
 *
 * <p>This repository provides data access methods for settlement orchestrations
 * including querying by various criteria, status management, and orchestration tracking.
 *
 * @since PE-410
 */
@Repository
public interface SettlementOrchestrationRepository extends JpaRepository<SettlementOrchestration, Long> {
  
  /**
   * Finds orchestration by orchestration ID.
   *
   * @param orchestrationId the orchestration ID
   * @return optional orchestration
   */
  Optional<SettlementOrchestration> findByOrchestrationId(String orchestrationId);
  
  /**
   * Finds orchestrations by tenant ID with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination information
   * @return page of orchestrations
   */
  Page<SettlementOrchestration> findByTenantId(String tenantId, Pageable pageable);
  
  /**
   * Finds orchestrations by status and tenant ID.
   *
   * @param status the orchestration status
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  List<SettlementOrchestration> findByStatusAndTenantId(SettlementOrchestration.OrchestrationStatus status, String tenantId);
  
  /**
   * Finds orchestrations by orchestration type and tenant ID.
   *
   * @param orchestrationType the orchestration type
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  List<SettlementOrchestration> findByOrchestrationTypeAndTenantId(SettlementOrchestration.OrchestrationType orchestrationType, String tenantId);
  
  /**
   * Finds active orchestrations by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of active orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId " +
         "AND o.status IN ('INITIATED', 'COORDINATING', 'EXECUTING', 'MONITORING')")
  List<SettlementOrchestration> findActiveOrchestrationsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds completed orchestrations by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of completed orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId AND o.status = 'COMPLETED'")
  List<SettlementOrchestration> findCompletedOrchestrationsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds failed orchestrations by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of failed orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId AND o.status = 'FAILED'")
  List<SettlementOrchestration> findFailedOrchestrationsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds orchestrations by date range and tenant ID.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId " +
         "AND o.startTime >= :startDate AND o.startTime <= :endDate " +
         "ORDER BY o.startTime DESC")
  List<SettlementOrchestration> findByDateRangeAndTenantId(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds orchestrations by priority range and tenant ID.
   *
   * @param minPriority the minimum priority
   * @param maxPriority the maximum priority
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId " +
         "AND o.priority >= :minPriority AND o.priority <= :maxPriority " +
         "ORDER BY o.priority DESC, o.startTime DESC")
  List<SettlementOrchestration> findByPriorityRangeAndTenantId(
      @Param("minPriority") Integer minPriority,
      @Param("maxPriority") Integer maxPriority,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds orchestrations by currency and tenant ID.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  List<SettlementOrchestration> findByCurrencyAndTenantId(String currency, String tenantId);
  
  /**
   * Finds orchestrations with high settlement amounts.
   *
   * @param tenantId the tenant ID
   * @param minAmount the minimum settlement amount
   * @return list of orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId " +
         "AND o.totalSettlementAmount >= :minAmount " +
         "ORDER BY o.totalSettlementAmount DESC")
  List<SettlementOrchestration> findHighSettlementAmountOrchestrations(
      @Param("tenantId") String tenantId, @Param("minAmount") java.math.BigDecimal minAmount);
  
  /**
   * Finds orchestrations by business unit and tenant ID.
   *
   * @param businessUnitId the business unit ID
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  List<SettlementOrchestration> findByBusinessUnitIdAndTenantId(String businessUnitId, String tenantId);
  
  /**
   * Finds orchestrations by current phase and tenant ID.
   *
   * @param currentPhase the current phase
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  List<SettlementOrchestration> findByCurrentPhaseAndTenantId(String currentPhase, String tenantId);
  
  /**
   * Finds orchestrations by progress range and tenant ID.
   *
   * @param minProgress the minimum progress
   * @param maxProgress the maximum progress
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId " +
         "AND o.phaseProgress >= :minProgress AND o.phaseProgress <= :maxProgress " +
         "ORDER BY o.phaseProgress DESC")
  List<SettlementOrchestration> findByProgressRangeAndTenantId(
      @Param("minProgress") java.math.BigDecimal minProgress,
      @Param("maxProgress") java.math.BigDecimal maxProgress,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds orchestrations by duration range.
   *
   * @param minDurationMinutes the minimum duration in minutes
   * @param maxDurationMinutes the maximum duration in minutes
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId " +
         "AND o.startTime IS NOT NULL AND o.endTime IS NOT NULL " +
         "AND EXTRACT(EPOCH FROM (o.endTime - o.startTime))/60 >= :minDurationMinutes " +
         "AND EXTRACT(EPOCH FROM (o.endTime - o.startTime))/60 <= :maxDurationMinutes " +
         "ORDER BY o.startTime DESC")
  List<SettlementOrchestration> findByDurationRange(
      @Param("minDurationMinutes") Long minDurationMinutes,
      @Param("maxDurationMinutes") Long maxDurationMinutes,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds orchestrations by participant count range.
   *
   * @param minParticipantCount the minimum participant count
   * @param maxParticipantCount the maximum participant count
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId " +
         "AND o.participantCount >= :minParticipantCount AND o.participantCount <= :maxParticipantCount " +
         "ORDER BY o.participantCount DESC")
  List<SettlementOrchestration> findByParticipantCountRange(
      @Param("minParticipantCount") Integer minParticipantCount,
      @Param("maxParticipantCount") Integer maxParticipantCount,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds orchestrations by workflow count range.
   *
   * @param minWorkflowCount the minimum workflow count
   * @param maxWorkflowCount the maximum workflow count
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId " +
         "AND o.workflowCount >= :minWorkflowCount AND o.workflowCount <= :maxWorkflowCount " +
         "ORDER BY o.workflowCount DESC")
  List<SettlementOrchestration> findByWorkflowCountRange(
      @Param("minWorkflowCount") Integer minWorkflowCount,
      @Param("maxWorkflowCount") Integer maxWorkflowCount,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds orchestrations by position count range.
   *
   * @param minPositionCount the minimum position count
   * @param maxPositionCount the maximum position count
   * @param tenantId the tenant ID
   * @return list of orchestrations
   */
  @Query("SELECT o FROM SettlementOrchestration o WHERE o.tenantId = :tenantId " +
         "AND o.positionCount >= :minPositionCount AND o.positionCount <= :maxPositionCount " +
         "ORDER BY o.positionCount DESC")
  List<SettlementOrchestration> findByPositionCountRange(
      @Param("minPositionCount") Integer minPositionCount,
      @Param("maxPositionCount") Integer maxPositionCount,
      @Param("tenantId") String tenantId);
  
  /**
   * Counts orchestrations by status and tenant ID.
   *
   * @param status the orchestration status
   * @param tenantId the tenant ID
   * @return count of orchestrations
   */
  long countByStatusAndTenantId(SettlementOrchestration.OrchestrationStatus status, String tenantId);
  
  /**
   * Counts orchestrations by orchestration type and tenant ID.
   *
   * @param orchestrationType the orchestration type
   * @param tenantId the tenant ID
   * @return count of orchestrations
   */
  long countByOrchestrationTypeAndTenantId(SettlementOrchestration.OrchestrationType orchestrationType, String tenantId);
  
  /**
   * Counts orchestrations by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return count of orchestrations
   */
  long countByTenantId(String tenantId);
  
  /**
   * Gets orchestration statistics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return orchestration statistics
   */
  @Query("SELECT " +
         "COUNT(o) as totalOrchestrations, " +
         "COUNT(CASE WHEN o.status = 'ACTIVE' THEN 1 END) as activeOrchestrations, " +
         "COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) as completedOrchestrations, " +
         "COUNT(CASE WHEN o.status = 'FAILED' THEN 1 END) as failedOrchestrations, " +
         "AVG(o.totalSettlementAmount) as averageSettlementAmount, " +
         "SUM(o.totalSettlementAmount) as totalSettlementAmount, " +
         "AVG(o.participantCount) as averageParticipantCount, " +
         "AVG(o.workflowCount) as averageWorkflowCount, " +
         "AVG(o.positionCount) as averagePositionCount " +
         "FROM SettlementOrchestration o WHERE o.tenantId = :tenantId")
  Object[] getOrchestrationStatisticsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Gets orchestration statistics by orchestration type.
   *
   * @param orchestrationType the orchestration type
   * @param tenantId the tenant ID
   * @return orchestration statistics
   */
  @Query("SELECT " +
         "COUNT(o) as totalOrchestrations, " +
         "AVG(o.totalSettlementAmount) as averageSettlementAmount, " +
         "SUM(o.totalSettlementAmount) as totalSettlementAmount, " +
         "AVG(o.participantCount) as averageParticipantCount, " +
         "AVG(o.workflowCount) as averageWorkflowCount, " +
         "AVG(o.positionCount) as averagePositionCount " +
         "FROM SettlementOrchestration o WHERE o.orchestrationType = :orchestrationType AND o.tenantId = :tenantId")
  Object[] getOrchestrationStatisticsByType(@Param("orchestrationType") SettlementOrchestration.OrchestrationType orchestrationType, 
                                          @Param("tenantId") String tenantId);
  
  /**
   * Updates orchestration status.
   *
   * @param orchestrationId the orchestration ID
   * @param status the new status
   * @return number of updated records
   */
  @Query("UPDATE SettlementOrchestration o SET o.status = :status WHERE o.orchestrationId = :orchestrationId")
  int updateOrchestrationStatus(@Param("orchestrationId") String orchestrationId, 
                               @Param("status") SettlementOrchestration.OrchestrationStatus status);
  
  /**
   * Deletes old orchestrations by date and tenant ID.
   *
   * @param cutoffDate the cutoff date
   * @param tenantId the tenant ID
   * @return number of deleted records
   */
  @Query("DELETE FROM SettlementOrchestration o WHERE o.tenantId = :tenantId " +
         "AND o.startTime < :cutoffDate")
  int deleteOldOrchestrations(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("tenantId") String tenantId);
}
