package com.payments.settlement.repository;

import com.payments.settlement.domain.NettingCycle;
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
 * Repository interface for NettingCycle entity.
 *
 * <p>This repository provides data access methods for netting cycles
 * including querying by various criteria, status management, and cycle tracking.
 *
 * @since PE-408
 */
@Repository
public interface NettingCycleRepository extends JpaRepository<NettingCycle, Long> {
  
  /**
   * Finds cycle by cycle ID.
   *
   * @param cycleId the cycle ID
   * @return optional cycle
   */
  Optional<NettingCycle> findByCycleId(String cycleId);
  
  /**
   * Finds cycles by tenant ID with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination information
   * @return page of cycles
   */
  Page<NettingCycle> findByTenantId(String tenantId, Pageable pageable);
  
  /**
   * Finds cycles by status and tenant ID.
   *
   * @param status the cycle status
   * @param tenantId the tenant ID
   * @return list of cycles
   */
  List<NettingCycle> findByStatusAndTenantId(NettingCycle.CycleStatus status, String tenantId);
  
  /**
   * Finds cycles by cycle type and tenant ID.
   *
   * @param cycleType the cycle type
   * @param tenantId the tenant ID
   * @return list of cycles
   */
  List<NettingCycle> findByCycleTypeAndTenantId(NettingCycle.CycleType cycleType, String tenantId);
  
  /**
   * Finds active cycles by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of active cycles
   */
  @Query("SELECT c FROM NettingCycle c WHERE c.tenantId = :tenantId AND c.status = 'ACTIVE'")
  List<NettingCycle> findActiveCyclesByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds cycles ready for settlement.
   *
   * @param tenantId the tenant ID
   * @return list of cycles ready for settlement
   */
  @Query("SELECT c FROM NettingCycle c WHERE c.tenantId = :tenantId " +
         "AND c.status = 'COMPLETED' AND c.netAmount = 0 AND c.cutOffTime <= :currentTime")
  List<NettingCycle> findCyclesReadyForSettlement(@Param("tenantId") String tenantId, 
                                                  @Param("currentTime") LocalDateTime currentTime);
  
  /**
   * Finds cycles by date range and tenant ID.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of cycles
   */
  @Query("SELECT c FROM NettingCycle c WHERE c.tenantId = :tenantId " +
         "AND c.startTime >= :startDate AND c.startTime <= :endDate " +
         "ORDER BY c.startTime DESC")
  List<NettingCycle> findByDateRangeAndTenantId(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds cycles by priority range and tenant ID.
   *
   * @param minPriority the minimum priority
   * @param maxPriority the maximum priority
   * @param tenantId the tenant ID
   * @return list of cycles
   */
  @Query("SELECT c FROM NettingCycle c WHERE c.tenantId = :tenantId " +
         "AND c.priority >= :minPriority AND c.priority <= :maxPriority " +
         "ORDER BY c.priority DESC, c.startTime DESC")
  List<NettingCycle> findByPriorityRangeAndTenantId(
      @Param("minPriority") Integer minPriority,
      @Param("maxPriority") Integer maxPriority,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds cycles by currency and tenant ID.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return list of cycles
   */
  List<NettingCycle> findByCurrencyAndTenantId(String currency, String tenantId);
  
  /**
   * Finds cycles with high transaction counts.
   *
   * @param tenantId the tenant ID
   * @param minTransactionCount the minimum transaction count
   * @return list of cycles
   */
  @Query("SELECT c FROM NettingCycle c WHERE c.tenantId = :tenantId " +
         "AND c.transactionCount >= :minTransactionCount " +
         "ORDER BY c.transactionCount DESC")
  List<NettingCycle> findHighTransactionCountCycles(
      @Param("tenantId") String tenantId, @Param("minTransactionCount") Integer minTransactionCount);
  
  /**
   * Finds cycles with high participant counts.
   *
   * @param tenantId the tenant ID
   * @param minParticipantCount the minimum participant count
   * @return list of cycles
   */
  @Query("SELECT c FROM NettingCycle c WHERE c.tenantId = :tenantId " +
         "AND c.participantCount >= :minParticipantCount " +
         "ORDER BY c.participantCount DESC")
  List<NettingCycle> findHighParticipantCountCycles(
      @Param("tenantId") String tenantId, @Param("minParticipantCount") Integer minParticipantCount);
  
  /**
   * Finds cycles with unbalanced net amounts.
   *
   * @param tenantId the tenant ID
   * @return list of unbalanced cycles
   */
  @Query("SELECT c FROM NettingCycle c WHERE c.tenantId = :tenantId " +
         "AND c.netAmount != 0 " +
         "ORDER BY ABS(c.netAmount) DESC")
  List<NettingCycle> findUnbalancedCycles(@Param("tenantId") String tenantId);
  
  /**
   * Finds cycles by business unit and tenant ID.
   *
   * @param businessUnitId the business unit ID
   * @param tenantId the tenant ID
   * @return list of cycles
   */
  List<NettingCycle> findByBusinessUnitIdAndTenantId(String businessUnitId, String tenantId);
  
  /**
   * Counts cycles by status and tenant ID.
   *
   * @param status the cycle status
   * @param tenantId the tenant ID
   * @return count of cycles
   */
  long countByStatusAndTenantId(NettingCycle.CycleStatus status, String tenantId);
  
  /**
   * Counts cycles by cycle type and tenant ID.
   *
   * @param cycleType the cycle type
   * @param tenantId the tenant ID
   * @return count of cycles
   */
  long countByCycleTypeAndTenantId(NettingCycle.CycleType cycleType, String tenantId);
  
  /**
   * Counts cycles by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return count of cycles
   */
  long countByTenantId(String tenantId);
  
  /**
   * Finds the latest cycle by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return optional latest cycle
   */
  @Query("SELECT c FROM NettingCycle c WHERE c.tenantId = :tenantId " +
         "ORDER BY c.startTime DESC LIMIT 1")
  Optional<NettingCycle> findLatestByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds cycles by duration range.
   *
   * @param minDurationMinutes the minimum duration in minutes
   * @param maxDurationMinutes the maximum duration in minutes
   * @param tenantId the tenant ID
   * @return list of cycles
   */
  @Query("SELECT c FROM NettingCycle c WHERE c.tenantId = :tenantId " +
         "AND c.startTime IS NOT NULL AND c.endTime IS NOT NULL " +
         "AND EXTRACT(EPOCH FROM (c.endTime - c.startTime))/60 >= :minDurationMinutes " +
         "AND EXTRACT(EPOCH FROM (c.endTime - c.startTime))/60 <= :maxDurationMinutes " +
         "ORDER BY c.startTime DESC")
  List<NettingCycle> findByDurationRange(
      @Param("minDurationMinutes") Long minDurationMinutes,
      @Param("maxDurationMinutes") Long maxDurationMinutes,
      @Param("tenantId") String tenantId);
  
  /**
   * Updates cycle status.
   *
   * @param cycleId the cycle ID
   * @param status the new status
   * @return number of updated records
   */
  @Query("UPDATE NettingCycle c SET c.status = :status WHERE c.cycleId = :cycleId")
  int updateCycleStatus(@Param("cycleId") String cycleId, @Param("status") NettingCycle.CycleStatus status);
  
  /**
   * Deletes old cycles by date and tenant ID.
   *
   * @param cutoffDate the cutoff date
   * @param tenantId the tenant ID
   * @return number of deleted records
   */
  @Query("DELETE FROM NettingCycle c WHERE c.tenantId = :tenantId " +
         "AND c.startTime < :cutoffDate")
  int deleteOldCycles(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("tenantId") String tenantId);
  
  /**
   * Gets cycle statistics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return cycle statistics
   */
  @Query("SELECT " +
         "COUNT(c) as totalCycles, " +
         "COUNT(CASE WHEN c.status = 'ACTIVE' THEN 1 END) as activeCycles, " +
         "COUNT(CASE WHEN c.status = 'COMPLETED' THEN 1 END) as completedCycles, " +
         "COUNT(CASE WHEN c.status = 'SETTLED' THEN 1 END) as settledCycles, " +
         "AVG(c.transactionCount) as averageTransactionCount, " +
         "AVG(c.participantCount) as averageParticipantCount " +
         "FROM NettingCycle c WHERE c.tenantId = :tenantId")
  Object[] getCycleStatisticsByTenantId(@Param("tenantId") String tenantId);
}
