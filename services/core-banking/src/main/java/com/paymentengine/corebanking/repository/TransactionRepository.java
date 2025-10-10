package com.paymentengine.corebanking.repository;

import com.paymentengine.corebanking.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Transaction entity operations
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    /**
     * Find transaction by reference number
     */
    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    /**
     * Find transaction by external reference
     */
    Optional<Transaction> findByExternalReference(String externalReference);
    
    /**
     * Find transaction by reference with pessimistic lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.transactionReference = :reference")
    Optional<Transaction> findByTransactionReferenceForUpdate(@Param("reference") String reference);
    
    /**
     * Find transactions for an account (both debit and credit)
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findTransactionsByAccountId(@Param("accountId") UUID accountId, Pageable pageable);
    
    /**
     * Find transactions from an account (debits)
     */
    Page<Transaction> findByFromAccountIdOrderByCreatedAtDesc(UUID fromAccountId, Pageable pageable);
    
    /**
     * Find transactions to an account (credits)
     */
    Page<Transaction> findByToAccountIdOrderByCreatedAtDesc(UUID toAccountId, Pageable pageable);
    
    /**
     * Find transactions by status
     */
    Page<Transaction> findByStatusOrderByCreatedAtDesc(Transaction.TransactionStatus status, Pageable pageable);
    
    /**
     * Find transactions by payment type
     */
    Page<Transaction> findByPaymentTypeIdOrderByCreatedAtDesc(UUID paymentTypeId, Pageable pageable);
    
    /**
     * Find pending transactions older than specified time
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' AND t.createdAt < :cutoffTime")
    List<Transaction> findPendingTransactionsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find processing transactions older than specified time
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PROCESSING' AND t.processedAt < :cutoffTime")
    List<Transaction> findProcessingTransactionsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find transactions by date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findTransactionsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * Find transactions by amount range
     */
    @Query("SELECT t FROM Transaction t WHERE t.amount BETWEEN :minAmount AND :maxAmount " +
           "ORDER BY t.amount DESC")
    Page<Transaction> findTransactionsByAmountRange(
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        Pageable pageable
    );
    
    /**
     * Count transactions by status
     */
    long countByStatus(Transaction.TransactionStatus status);
    
    /**
     * Count transactions for an account
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId")
    long countTransactionsByAccountId(@Param("accountId") UUID accountId);
    
    /**
     * Get total transaction amount by account and date range
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.status = 'COMPLETED' " +
           "AND t.completedAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalTransactionAmountByAccountAndDateRange(
        @Param("accountId") UUID accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get daily transaction volume
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.status = 'COMPLETED' " +
           "AND DATE(t.completedAt) = DATE(:date)")
    BigDecimal getDailyTransactionVolume(@Param("date") LocalDateTime date);
    
    /**
     * Update transaction status
     */
    @Modifying
    @Query("UPDATE Transaction t SET t.status = :status, t.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE t.id = :transactionId")
    int updateTransactionStatus(@Param("transactionId") UUID transactionId, 
                               @Param("status") Transaction.TransactionStatus status);
    
    /**
     * Find high-value transactions (above threshold)
     */
    @Query("SELECT t FROM Transaction t WHERE t.amount > :threshold " +
           "AND t.createdAt >= :startDate " +
           "ORDER BY t.amount DESC")
    List<Transaction> findHighValueTransactions(
        @Param("threshold") BigDecimal threshold,
        @Param("startDate") LocalDateTime startDate
    );
    
    /**
     * Find failed transactions for retry
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' " +
           "AND (t.metadata IS NULL OR " +
           "JSON_EXTRACT(t.metadata, '$.retry_count') IS NULL OR " +
           "CAST(JSON_EXTRACT(t.metadata, '$.retry_count') AS INTEGER) < :maxRetries)")
    List<Transaction> findFailedTransactionsForRetry(@Param("maxRetries") Integer maxRetries);
    
    /**
     * Search transactions by multiple criteria
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:transactionReference IS NULL OR t.transactionReference LIKE %:transactionReference%) AND " +
           "(:accountId IS NULL OR t.fromAccountId = :accountId OR t.toAccountId = :accountId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:paymentTypeId IS NULL OR t.paymentTypeId = :paymentTypeId) AND " +
           "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR t.amount <= :maxAmount) AND " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate) " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findTransactionsByCriteria(
        @Param("transactionReference") String transactionReference,
        @Param("accountId") UUID accountId,
        @Param("status") Transaction.TransactionStatus status,
        @Param("paymentTypeId") UUID paymentTypeId,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * Get transaction statistics
     */
    @Query("SELECT " +
           "COUNT(t) as totalCount, " +
           "COALESCE(SUM(t.amount), 0) as totalAmount, " +
           "COALESCE(AVG(t.amount), 0) as averageAmount, " +
           "COALESCE(MAX(t.amount), 0) as maxAmount, " +
           "COALESCE(MIN(t.amount), 0) as minAmount " +
           "FROM Transaction t WHERE t.status = 'COMPLETED' " +
           "AND t.completedAt BETWEEN :startDate AND :endDate")
    Object[] getTransactionStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Check if transaction reference exists (for uniqueness)
     */
    boolean existsByTransactionReference(String transactionReference);
    
    /**
     * Find duplicate transactions (same amount, accounts, and timeframe)
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "t.fromAccountId = :fromAccountId AND " +
           "t.toAccountId = :toAccountId AND " +
           "t.amount = :amount AND " +
           "t.createdAt BETWEEN :startTime AND :endTime AND " +
           "t.id != :excludeTransactionId")
    List<Transaction> findPotentialDuplicateTransactions(
        @Param("fromAccountId") UUID fromAccountId,
        @Param("toAccountId") UUID toAccountId,
        @Param("amount") BigDecimal amount,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeTransactionId") UUID excludeTransactionId
    );
}