package com.payments.settlement.repository;

import com.payments.settlement.domain.NettingPosition;
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
 * Repository interface for NettingPosition entity.
 *
 * <p>This repository provides data access methods for netting positions
 * including querying by various criteria, position management, and balance tracking.
 *
 * @since PE-408
 */
@Repository
public interface NettingPositionRepository extends JpaRepository<NettingPosition, Long> {
  
  /**
   * Finds positions by netting cycle ID.
   *
   * @param nettingCycleId the netting cycle ID
   * @return list of positions
   */
  List<NettingPosition> findByNettingCycleId(Long nettingCycleId);
  
  /**
   * Finds positions by participant ID and tenant ID.
   *
   * @param participantId the participant ID
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<NettingPosition> findByParticipantIdAndTenantId(String participantId, String tenantId);
  
  /**
   * Finds positions by currency and tenant ID.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<NettingPosition> findByCurrencyAndTenantId(String currency, String tenantId);
  
  /**
   * Finds positions by tenant ID with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination information
   * @return page of positions
   */
  Page<NettingPosition> findByTenantId(String tenantId, Pageable pageable);
  
  /**
   * Finds positions by status and tenant ID.
   *
   * @param status the position status
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<NettingPosition> findByStatusAndTenantId(NettingPosition.PositionStatus status, String tenantId);
  
  /**
   * Finds positions by position type and tenant ID.
   *
   * @param positionType the position type
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<NettingPosition> findByPositionTypeAndTenantId(NettingPosition.PositionType positionType, String tenantId);
  
  /**
   * Finds active positions by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of active positions
   */
  @Query("SELECT p FROM NettingPosition p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE'")
  List<NettingPosition> findActivePositionsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds settled positions by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of settled positions
   */
  @Query("SELECT p FROM NettingPosition p WHERE p.tenantId = :tenantId AND p.status = 'SETTLED'")
  List<NettingPosition> findSettledPositionsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds positions by net amount range.
   *
   * @param minAmount the minimum net amount
   * @param maxAmount the maximum net amount
   * @param tenantId the tenant ID
   * @return list of positions
   */
  @Query("SELECT p FROM NettingPosition p WHERE p.tenantId = :tenantId " +
         "AND p.netAmount >= :minAmount AND p.netAmount <= :maxAmount " +
         "ORDER BY p.netAmount DESC")
  List<NettingPosition> findByNetAmountRange(
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
  @Query("SELECT p FROM NettingPosition p WHERE p.tenantId = :tenantId " +
         "AND ABS(p.netAmount) >= :minAmount " +
         "ORDER BY ABS(p.netAmount) DESC")
  List<NettingPosition> findHighNetAmountPositions(
      @Param("tenantId") String tenantId, @Param("minAmount") BigDecimal minAmount);
  
  /**
   * Finds positions by participant and currency.
   *
   * @param participantId the participant ID
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<NettingPosition> findByParticipantIdAndCurrencyAndTenantId(
      String participantId, String currency, String tenantId);
  
  /**
   * Finds positions by business unit and tenant ID.
   *
   * @param businessUnitId the business unit ID
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<NettingPosition> findByBusinessUnitIdAndTenantId(String businessUnitId, String tenantId);
  
  /**
   * Finds positions by priority range and tenant ID.
   *
   * @param minPriority the minimum priority
   * @param maxPriority the maximum priority
   * @param tenantId the tenant ID
   * @return list of positions
   */
  @Query("SELECT p FROM NettingPosition p WHERE p.tenantId = :tenantId " +
         "AND p.priority >= :minPriority AND p.priority <= :maxPriority " +
         "ORDER BY p.priority DESC, p.netAmount DESC")
  List<NettingPosition> findByPriorityRangeAndTenantId(
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
  @Query("SELECT p FROM NettingPosition p WHERE p.tenantId = :tenantId " +
         "AND p.settlementDate >= :startDate AND p.settlementDate <= :endDate " +
         "ORDER BY p.settlementDate DESC")
  List<NettingPosition> findBySettlementDateRange(
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
  @Query("SELECT p FROM NettingPosition p WHERE p.tenantId = :tenantId " +
         "AND p.valueDate >= :startDate AND p.valueDate <= :endDate " +
         "ORDER BY p.valueDate DESC")
  List<NettingPosition> findByValueDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds positions with high transaction counts.
   *
   * @param tenantId the tenant ID
   * @param minTransactionCount the minimum transaction count
   * @return list of positions
   */
  @Query("SELECT p FROM NettingPosition p WHERE p.tenantId = :tenantId " +
         "AND p.transactionCount >= :minTransactionCount " +
         "ORDER BY p.transactionCount DESC")
  List<NettingPosition> findHighTransactionCountPositions(
      @Param("tenantId") String tenantId, @Param("minTransactionCount") Integer minTransactionCount);
  
  /**
   * Finds positions by participant and status.
   *
   * @param participantId the participant ID
   * @param status the position status
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<NettingPosition> findByParticipantIdAndStatusAndTenantId(
      String participantId, NettingPosition.PositionStatus status, String tenantId);
  
  /**
   * Finds positions by currency and status.
   *
   * @param currency the currency
   * @param status the position status
   * @param tenantId the tenant ID
   * @return list of positions
   */
  List<NettingPosition> findByCurrencyAndStatusAndTenantId(
      String currency, NettingPosition.PositionStatus status, String tenantId);
  
  /**
   * Counts positions by status and tenant ID.
   *
   * @param status the position status
   * @param tenantId the tenant ID
   * @return count of positions
   */
  long countByStatusAndTenantId(NettingPosition.PositionStatus status, String tenantId);
  
  /**
   * Counts positions by position type and tenant ID.
   *
   * @param positionType the position type
   * @param tenantId the tenant ID
   * @return count of positions
   */
  long countByPositionTypeAndTenantId(NettingPosition.PositionType positionType, String tenantId);
  
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
   * Counts positions by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return count of positions
   */
  long countByTenantId(String tenantId);
  
  /**
   * Gets position statistics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return position statistics
   */
  @Query("SELECT " +
         "COUNT(p) as totalPositions, " +
         "COUNT(CASE WHEN p.status = 'ACTIVE' THEN 1 END) as activePositions, " +
         "COUNT(CASE WHEN p.status = 'SETTLED' THEN 1 END) as settledPositions, " +
         "COUNT(CASE WHEN p.positionType = 'DEBIT' THEN 1 END) as debitPositions, " +
         "COUNT(CASE WHEN p.positionType = 'CREDIT' THEN 1 END) as creditPositions, " +
         "SUM(p.netAmount) as totalNetAmount, " +
         "SUM(p.debitAmount) as totalDebitAmount, " +
         "SUM(p.creditAmount) as totalCreditAmount " +
         "FROM NettingPosition p WHERE p.tenantId = :tenantId")
  Object[] getPositionStatisticsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Gets position statistics by participant.
   *
   * @param participantId the participant ID
   * @param tenantId the tenant ID
   * @return position statistics
   */
  @Query("SELECT " +
         "COUNT(p) as totalPositions, " +
         "SUM(p.netAmount) as totalNetAmount, " +
         "SUM(p.debitAmount) as totalDebitAmount, " +
         "SUM(p.creditAmount) as totalCreditAmount, " +
         "SUM(p.transactionCount) as totalTransactionCount " +
         "FROM NettingPosition p WHERE p.participantId = :participantId AND p.tenantId = :tenantId")
  Object[] getPositionStatisticsByParticipant(@Param("participantId") String participantId, 
                                             @Param("tenantId") String tenantId);
  
  /**
   * Gets position statistics by currency.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return position statistics
   */
  @Query("SELECT " +
         "COUNT(p) as totalPositions, " +
         "SUM(p.netAmount) as totalNetAmount, " +
         "SUM(p.debitAmount) as totalDebitAmount, " +
         "SUM(p.creditAmount) as totalCreditAmount, " +
         "SUM(p.transactionCount) as totalTransactionCount " +
         "FROM NettingPosition p WHERE p.currency = :currency AND p.tenantId = :tenantId")
  Object[] getPositionStatisticsByCurrency(@Param("currency") String currency, 
                                           @Param("tenantId") String tenantId);
  
  /**
   * Updates position status.
   *
   * @param positionId the position ID
   * @param status the new status
   * @return number of updated records
   */
  @Query("UPDATE NettingPosition p SET p.status = :status WHERE p.id = :positionId")
  int updatePositionStatus(@Param("positionId") Long positionId, 
                          @Param("status") NettingPosition.PositionStatus status);
  
  /**
   * Deletes old positions by date and tenant ID.
   *
   * @param cutoffDate the cutoff date
   * @param tenantId the tenant ID
   * @return number of deleted records
   */
  @Query("DELETE FROM NettingPosition p WHERE p.tenantId = :tenantId " +
         "AND p.createdAt < :cutoffDate")
  int deleteOldPositions(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("tenantId") String tenantId);
}
