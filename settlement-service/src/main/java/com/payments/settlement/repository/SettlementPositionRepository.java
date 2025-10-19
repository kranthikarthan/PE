package com.payments.settlement.repository;

import com.payments.settlement.domain.SettlementPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SettlementPosition entity.
 *
 * <p>This repository provides data access methods for settlement positions
 * including querying by various criteria, position management, and settlement tracking.
 *
 * @since PE-409
 */
@Repository
public interface SettlementPositionRepository extends JpaRepository<SettlementPosition, Long> {
  
  /**
   * Finds positions by workflow ID.
   *
   * @param workflowId the workflow ID
   * @return list of positions
   */
  List<SettlementPosition> findByWorkflowId(Long workflowId);
  
  /**
   * Finds positions by netting position ID.
   *
   * @param nettingPositionId the netting position ID
   * @return list of positions
   */
  List<SettlementPosition> findByNettingPositionId(Long nettingPositionId);
  
  /**
   * Finds positions by participant ID and tenant ID.
   *
   * @param participantId the participant ID
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<SettlementPosition> findByParticipantIdAndTenantId(String participantId, String tenantId);
  
  /**
   * Finds positions by tenant ID with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination information
   * @return page of positions
   */
  Page<SettlementPosition> findByTenantId(String tenantId, Pageable pageable);
  
  /**
   * Finds positions by status and tenant ID.
   *
   * @param status the position status
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<SettlementPosition> findByStatusAndTenantId(SettlementPosition.SettlementStatus status, String tenantId);
  
  /**
   * Finds positions by position type and tenant ID.
   *
   * @param positionType the position type
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<SettlementPosition> findByPositionTypeAndTenantId(SettlementPosition.PositionType positionType, String tenantId);
  
  /**
   * Finds active positions by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of active positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId " +
         "AND p.status IN ('PENDING', 'PROCESSING')")
  List<SettlementPosition> findActivePositionsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds settled positions by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of settled positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId AND p.status = 'SETTLED'")
  List<SettlementPosition> findSettledPositionsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds failed positions by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of failed positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId AND p.status = 'FAILED'")
  List<SettlementPosition> findFailedPositionsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds positions by currency and tenant ID.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<SettlementPosition> findByCurrencyAndTenantId(String currency, String tenantId);
  
  /**
   * Finds positions by business unit and tenant ID.
   *
   * @param businessUnitId the business unit ID
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<SettlementPosition> findByBusinessUnitIdAndTenantId(String businessUnitId, String tenantId);
  
  /**
   * Finds positions by net amount range.
   *
   * @param minAmount the minimum net amount
   * @param maxAmount the maximum net amount
   * @param tenantId the tenant ID
   * @return list of positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId " +
         "AND p.netAmount >= :minAmount AND p.netAmount <= :maxAmount " +
         "ORDER BY p.netAmount DESC")
  List<SettlementPosition> findByNetAmountRange(
      @Param("minAmount") BigDecimal minAmount,
      @Param("maxAmount") BigDecimal maxAmount,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds positions by settlement amount range.
   *
   * @param minAmount the minimum settlement amount
   * @param maxAmount the maximum settlement amount
   * @param tenantId the tenant ID
   * @return list of positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId " +
         "AND p.settlementAmount >= :minAmount AND p.settlementAmount <= :maxAmount " +
         "ORDER BY p.settlementAmount DESC")
  List<SettlementPosition> findBySettlementAmountRange(
      @Param("minAmount") BigDecimal minAmount,
      @Param("maxAmount") BigDecimal maxAmount,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds positions with high net amounts.
   *
   * @param tenantId the tenant ID
   * @param minAmount the minimum net amount
   * @return list of positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId " +
         "AND ABS(p.netAmount) >= :minAmount " +
         "ORDER BY ABS(p.netAmount) DESC")
  List<SettlementPosition> findHighNetAmountPositions(
      @Param("tenantId") String tenantId, @Param("minAmount") BigDecimal minAmount);
  
  /**
   * Finds positions with high settlement amounts.
   *
   * @param tenantId the tenant ID
   * @param minAmount the minimum settlement amount
   * @return list of positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId " +
         "AND ABS(p.settlementAmount) >= :minAmount " +
         "ORDER BY ABS(p.settlementAmount) DESC")
  List<SettlementPosition> findHighSettlementAmountPositions(
      @Param("tenantId") String tenantId, @Param("minAmount") BigDecimal minAmount);
  
  /**
   * Finds positions by priority range and tenant ID.
   *
   * @param minPriority the minimum priority
   * @param maxPriority the maximum priority
   * @param tenantId the tenant ID
   * @return list of positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId " +
         "AND p.priority >= :minPriority AND p.priority <= :maxPriority " +
         "ORDER BY p.priority DESC, p.netAmount DESC")
  List<SettlementPosition> findByPriorityRangeAndTenantId(
      @Param("minPriority") Integer minPriority,
      @Param("maxPriority") Integer maxPriority,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds positions by settlement date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId " +
         "AND p.settlementDate >= :startDate AND p.settlementDate <= :endDate " +
         "ORDER BY p.settlementDate DESC")
  List<SettlementPosition> findBySettlementDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds positions by value date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId " +
         "AND p.valueDate >= :startDate AND p.valueDate <= :endDate " +
         "ORDER BY p.valueDate DESC")
  List<SettlementPosition> findByValueDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds positions by retry count range.
   *
   * @param minRetryCount the minimum retry count
   * @param maxRetryCount the maximum retry count
   * @param tenantId the tenant ID
   * @return list of positions
   */
  @Query("SELECT p FROM SettlementPosition p WHERE p.tenantId = :tenantId " +
         "AND p.retryCount >= :minRetryCount AND p.retryCount <= :maxRetryCount " +
         "ORDER BY p.retryCount DESC")
  List<SettlementPosition> findByRetryCountRange(
      @Param("minRetryCount") Integer minRetryCount,
      @Param("maxRetryCount") Integer maxRetryCount,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds positions by settlement reference and tenant ID.
   *
   * @param settlementReference the settlement reference
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<SettlementPosition> findBySettlementReferenceAndTenantId(String settlementReference, String tenantId);
  
  /**
   * Finds positions by participant and status.
   *
   * @param participantId the participant ID
   * @param status the position status
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<SettlementPosition> findByParticipantIdAndStatusAndTenantId(
      String participantId, SettlementPosition.SettlementStatus status, String tenantId);
  
  /**
   * Finds positions by currency and status.
   *
   * @param currency the currency
   * @param status the position status
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<SettlementPosition> findByCurrencyAndStatusAndTenantId(
      String currency, SettlementPosition.SettlementStatus status, String tenantId);
  
  /**
   * Finds positions by workflow and status.
   *
   * @param workflowId the workflow ID
   * @param status the position status
   * @return list of positions
   */
  List<SettlementPosition> findByWorkflowIdAndStatus(Long workflowId, SettlementPosition.SettlementStatus status);
  
  /**
   * Finds positions by workflow and participant.
   *
   * @param workflowId the workflow ID
   * @param participantId the participant ID
   * @return list of positions
   */
  List<SettlementPosition> findByWorkflowIdAndParticipantId(Long workflowId, String participantId);
  
  /**
   * Finds positions by workflow and currency.
   *
   * @param workflowId the workflow ID
   * @param currency the currency
   * @return list of positions
   */
  List<SettlementPosition> findByWorkflowIdAndCurrency(Long workflowId, String currency);
  
  /**
   * Counts positions by status and tenant ID.
   *
   * @param status the position status
   * @param tenantId the tenant ID
   * @return count of positions
   */
  long countByStatusAndTenantId(SettlementPosition.SettlementStatus status, String tenantId);
  
  /**
   * Counts positions by position type and tenant ID.
   *
   * @param positionType the position type
   * @param tenantId the tenant ID
   * @return count of positions
   */
  long countByPositionTypeAndTenantId(SettlementPosition.PositionType positionType, String tenantId);
  
  /**
   * Counts positions by participant and tenant ID.
   *
   * @param participantId the participant ID
   * @param tenantId the tenant ID
   * @return count of positions
   */
  long countByParticipantIdAndTenantId(String participantId, String tenantId);
  
  /**
   * Counts positions by currency and tenant ID.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return count of positions
   */
  long countByCurrencyAndTenantId(String currency, String tenantId);
  
  /**
   * Counts positions by workflow ID.
   *
   * @param workflowId the workflow ID
   * @return count of positions
   */
  long countByWorkflowId(Long workflowId);
  
  /**
   * Gets position statistics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return position statistics
   */
  @Query("SELECT " +
         "COUNT(p) as totalPositions, " +
         "COUNT(CASE WHEN p.status = 'SETTLED' THEN 1 END) as settledPositions, " +
         "COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failedPositions, " +
         "COUNT(CASE WHEN p.positionType = 'DEBIT' THEN 1 END) as debitPositions, " +
         "COUNT(CASE WHEN p.positionType = 'CREDIT' THEN 1 END) as creditPositions, " +
         "SUM(p.netAmount) as totalNetAmount, " +
         "SUM(p.settlementAmount) as totalSettlementAmount, " +
         "SUM(p.settledAmount) as totalSettledAmount " +
         "FROM SettlementPosition p WHERE p.tenantId = :tenantId")
  Object[] getPositionStatisticsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Gets position statistics by workflow.
   *
   * @param workflowId the workflow ID
   * @return position statistics
   */
  @Query("SELECT " +
         "COUNT(p) as totalPositions, " +
         "COUNT(CASE WHEN p.status = 'SETTLED' THEN 1 END) as settledPositions, " +
         "COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failedPositions, " +
         "SUM(p.netAmount) as totalNetAmount, " +
         "SUM(p.settlementAmount) as totalSettlementAmount, " +
         "SUM(p.settledAmount) as totalSettledAmount " +
         "FROM SettlementPosition p WHERE p.workflowId = :workflowId")
  Object[] getPositionStatisticsByWorkflow(@Param("workflowId") Long workflowId);
  
  /**
   * Gets position statistics by participant.
   *
   * @param participantId the participant ID
   * @param tenantId the tenant ID
   * @return position statistics
   */
  @Query("SELECT " +
         "COUNT(p) as totalPositions, " +
         "COUNT(CASE WHEN p.status = 'SETTLED' THEN 1 END) as settledPositions, " +
         "COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failedPositions, " +
         "SUM(p.netAmount) as totalNetAmount, " +
         "SUM(p.settlementAmount) as totalSettlementAmount, " +
         "SUM(p.settledAmount) as totalSettledAmount " +
         "FROM SettlementPosition p WHERE p.participantId = :participantId AND p.tenantId = :tenantId")
  Object[] getPositionStatisticsByParticipant(@Param("participantId") String participantId, 
                                             @Param("tenantId") String tenantId);
  
  /**
   * Updates position status.
   *
   * @param positionId the position ID
   * @param status the new status
   * @return number of updated records
   */
  @Query("UPDATE SettlementPosition p SET p.status = :status WHERE p.id = :positionId")
  int updatePositionStatus(@Param("positionId") Long positionId, 
                          @Param("status") SettlementPosition.SettlementStatus status);
  
  /**
   * Deletes old positions by date and tenant ID.
   *
   * @param cutoffDate the cutoff date
   * @param tenantId the tenant ID
   * @return number of deleted records
   */
  @Query("DELETE FROM SettlementPosition p WHERE p.tenantId = :tenantId " +
         "AND p.createdAt < :cutoffDate")
  int deleteOldPositions(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("tenantId") String tenantId);
}
