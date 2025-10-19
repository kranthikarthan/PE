package com.payments.settlement.repository;

import com.payments.settlement.domain.NettingTransaction;
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
 * Repository interface for NettingTransaction entity.
 *
 * <p>This repository provides data access methods for netting transactions
 * including querying by various criteria, transaction management, and participant tracking.
 *
 * @since PE-408
 */
@Repository
public interface NettingTransactionRepository extends JpaRepository<NettingTransaction, Long> {
  
  /**
   * Finds transaction by transaction ID.
   *
   * @param transactionId the transaction ID
   * @return optional transaction
   */
  Optional<NettingTransaction> findByTransactionId(String transactionId);
  
  /**
   * Finds transactions by netting cycle ID.
   *
   * @param nettingCycleId the netting cycle ID
   * @return list of transactions
   */
  List<NettingTransaction> findByNettingCycleId(Long nettingCycleId);
  
  /**
   * Finds transactions by netting cycle ID and currency.
   *
   * @param nettingCycleId the netting cycle ID
   * @param currency the currency
   * @return list of transactions
   */
  List<NettingTransaction> findByNettingCycleIdAndCurrency(Long nettingCycleId, String currency);
  
  /**
   * Finds transactions by debtor participant ID and tenant ID.
   *
   * @param debtorParticipantId the debtor participant ID
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  List<NettingTransaction> findByDebtorParticipantIdAndTenantId(String debtorParticipantId, String tenantId);
  
  /**
   * Finds transactions by creditor participant ID and tenant ID.
   *
   * @param creditorParticipantId the creditor participant ID
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  List<NettingTransaction> findByCreditorParticipantIdAndTenantId(String creditorParticipantId, String tenantId);
  
  /**
   * Finds transactions by tenant ID with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination information
   * @return page of transactions
   */
  Page<NettingTransaction> findByTenantId(String tenantId, Pageable pageable);
  
  /**
   * Finds transactions by status and tenant ID.
   *
   * @param status the transaction status
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  List<NettingTransaction> findByStatusAndTenantId(NettingTransaction.TransactionStatus status, String tenantId);
  
  /**
   * Finds transactions by transaction type and tenant ID.
   *
   * @param transactionType the transaction type
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  List<NettingTransaction> findByTransactionTypeAndTenantId(NettingTransaction.TransactionType transactionType, String tenantId);
  
  /**
   * Finds active transactions by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of active transactions
   */
  @Query("SELECT t FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND t.status NOT IN ('FAILED', 'CANCELLED', 'REVERSED')")
  List<NettingTransaction> findActiveTransactionsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds completed transactions by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of completed transactions
   */
  @Query("SELECT t FROM NettingTransaction t WHERE t.tenantId = :tenantId AND t.status = 'COMPLETED'")
  List<NettingTransaction> findCompletedTransactionsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Finds transactions by amount range.
   *
   * @param minAmount the minimum amount
   * @param maxAmount the maximum amount
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  @Query("SELECT t FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND t.amount >= :minAmount AND t.amount <= :maxAmount " +
         "ORDER BY t.amount DESC")
  List<NettingTransaction> findByAmountRange(
      @Param("minAmount") BigDecimal minAmount,
      @Param("maxAmount") BigDecimal maxAmount,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds transactions with high amounts.
   *
   * @param tenantId the tenant ID
   * @param minAmount the minimum amount
   * @return list of transactions
   */
  @Query("SELECT t FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND ABS(t.amount) >= :minAmount " +
         "ORDER BY ABS(t.amount) DESC")
  List<NettingTransaction> findHighAmountTransactions(
      @Param("tenantId") String tenantId, @Param("minAmount") BigDecimal minAmount);
  
  /**
   * Finds transactions by currency and tenant ID.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  List<NettingTransaction> findByCurrencyAndTenantId(String currency, String tenantId);
  
  /**
   * Finds transactions by business unit and tenant ID.
   *
   * @param businessUnitId the business unit ID
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  List<NettingTransaction> findByBusinessUnitIdAndTenantId(String businessUnitId, String tenantId);
  
  /**
   * Finds transactions by priority range and tenant ID.
   *
   * @param minPriority the minimum priority
   * @param maxPriority the maximum priority
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  @Query("SELECT t FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND t.priority >= :minPriority AND t.priority <= :maxPriority " +
         "ORDER BY t.priority DESC, t.amount DESC")
  List<NettingTransaction> findByPriorityRangeAndTenantId(
      @Param("minPriority") Integer minPriority,
      @Param("maxPriority") Integer maxPriority,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds transactions by transaction date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  @Query("SELECT t FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
         "ORDER BY t.transactionDate DESC")
  List<NettingTransaction> findByTransactionDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds transactions by value date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  @Query("SELECT t FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND t.valueDate >= :startDate AND t.valueDate <= :endDate " +
         "ORDER BY t.valueDate DESC")
  List<NettingTransaction> findByValueDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds transactions by settlement date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  @Query("SELECT t FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND t.settlementDate >= :startDate AND t.settlementDate <= :endDate " +
         "ORDER BY t.settlementDate DESC")
  List<NettingTransaction> findBySettlementDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds transactions by participant (debtor or creditor).
   *
   * @param participantId the participant ID
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  @Query("SELECT t FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND (t.debtorParticipantId = :participantId OR t.creditorParticipantId = :participantId) " +
         "ORDER BY t.transactionDate DESC")
  List<NettingTransaction> findByParticipantAndTenantId(@Param("participantId") String participantId, 
                                                       @Param("tenantId") String tenantId);
  
  /**
   * Finds transactions by reference and tenant ID.
   *
   * @param reference the reference
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  List<NettingTransaction> findByReferenceAndTenantId(String reference, String tenantId);
  
  /**
   * Finds transactions by description containing text.
   *
   * @param description the description text
   * @param tenantId the tenant ID
   * @return list of transactions
   */
  @Query("SELECT t FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
         "ORDER BY t.transactionDate DESC")
  List<NettingTransaction> findByDescriptionContainingAndTenantId(@Param("description") String description, 
                                                                 @Param("tenantId") String tenantId);
  
  /**
   * Counts transactions by status and tenant ID.
   *
   * @param status the transaction status
   * @param tenantId the tenant ID
   * @return count of transactions
   */
  long countByStatusAndTenantId(NettingTransaction.TransactionStatus status, String tenantId);
  
  /**
   * Counts transactions by transaction type and tenant ID.
   *
   * @param transactionType the transaction type
   * @param tenantId the tenant ID
   * @return count of transactions
   */
  long countByTransactionTypeAndTenantId(NettingTransaction.TransactionType transactionType, String tenantId);
  
  /**
   * Counts transactions by participant and tenant ID.
   *
   * @param participantId the participant ID
   * @param tenantId the tenant ID
   * @return count of transactions
   */
  @Query("SELECT COUNT(t) FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND (t.debtorParticipantId = :participantId OR t.creditorParticipantId = :participantId)")
  long countByParticipantAndTenantId(@Param("participantId") String participantId, @Param("tenantId") String tenantId);
  
  /**
   * Counts transactions by currency and tenant ID.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return count of transactions
   */
  long countByCurrencyAndTenantId(String currency, String tenantId);
  
  /**
   * Counts transactions by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return count of transactions
   */
  long countByTenantId(String tenantId);
  
  /**
   * Gets transaction statistics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return transaction statistics
   */
  @Query("SELECT " +
         "COUNT(t) as totalTransactions, " +
         "COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as completedTransactions, " +
         "COUNT(CASE WHEN t.status = 'FAILED' THEN 1 END) as failedTransactions, " +
         "COUNT(CASE WHEN t.transactionType = 'PAYMENT' THEN 1 END) as paymentTransactions, " +
         "COUNT(CASE WHEN t.transactionType = 'TRANSFER' THEN 1 END) as transferTransactions, " +
         "SUM(t.amount) as totalAmount, " +
         "AVG(t.amount) as averageAmount, " +
         "MIN(t.amount) as minAmount, " +
         "MAX(t.amount) as maxAmount " +
         "FROM NettingTransaction t WHERE t.tenantId = :tenantId")
  Object[] getTransactionStatisticsByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Gets transaction statistics by participant.
   *
   * @param participantId the participant ID
   * @param tenantId the tenant ID
   * @return transaction statistics
   */
  @Query("SELECT " +
         "COUNT(t) as totalTransactions, " +
         "SUM(t.amount) as totalAmount, " +
         "AVG(t.amount) as averageAmount, " +
         "MIN(t.amount) as minAmount, " +
         "MAX(t.amount) as maxAmount " +
         "FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND (t.debtorParticipantId = :participantId OR t.creditorParticipantId = :participantId)")
  Object[] getTransactionStatisticsByParticipant(@Param("participantId") String participantId, 
                                                @Param("tenantId") String tenantId);
  
  /**
   * Gets transaction statistics by currency.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return transaction statistics
   */
  @Query("SELECT " +
         "COUNT(t) as totalTransactions, " +
         "SUM(t.amount) as totalAmount, " +
         "AVG(t.amount) as averageAmount, " +
         "MIN(t.amount) as minAmount, " +
         "MAX(t.amount) as maxAmount " +
         "FROM NettingTransaction t WHERE t.currency = :currency AND t.tenantId = :tenantId")
  Object[] getTransactionStatisticsByCurrency(@Param("currency") String currency, 
                                              @Param("tenantId") String tenantId);
  
  /**
   * Updates transaction status.
   *
   * @param transactionId the transaction ID
   * @param status the new status
   * @return number of updated records
   */
  @Query("UPDATE NettingTransaction t SET t.status = :status WHERE t.transactionId = :transactionId")
  int updateTransactionStatus(@Param("transactionId") String transactionId, 
                             @Param("status") NettingTransaction.TransactionStatus status);
  
  /**
   * Deletes old transactions by date and tenant ID.
   *
   * @param cutoffDate the cutoff date
   * @param tenantId the tenant ID
   * @return number of deleted records
   */
  @Query("DELETE FROM NettingTransaction t WHERE t.tenantId = :tenantId " +
         "AND t.transactionDate < :cutoffDate")
  int deleteOldTransactions(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("tenantId") String tenantId);
}
