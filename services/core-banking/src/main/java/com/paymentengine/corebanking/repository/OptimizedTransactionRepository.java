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
 * Optimized Transaction Repository for High TPS
 * 
 * This repository includes performance-optimized queries and batch operations
 * designed to handle 2000+ transactions per second.
 */
@Repository
public interface OptimizedTransactionRepository extends JpaRepository<Transaction, UUID> {
    
    /**
     * Find transaction by reference with optimistic lock
     * Optimized for high-frequency lookups
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionReference = :reference")
    Optional<Transaction> findByTransactionReferenceOptimized(@Param("reference") String reference);
    
    /**
     * Find transaction by reference with pessimistic lock for updates
     * Used for critical update operations
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.transactionReference = :reference")
    Optional<Transaction> findByTransactionReferenceForUpdate(@Param("reference") String reference);
    
    /**
     * Batch insert transactions using native SQL for maximum performance
     * Supports bulk operations for high TPS scenarios
     */
    @Modifying
    @Query(value = """
        INSERT INTO payment_engine.transactions (
            id, transaction_reference, external_reference, from_account_id, 
            to_account_id, payment_type_id, amount, currency_code, fee_amount,
            status, transaction_type, description, metadata, initiated_at, 
            processed_at, completed_at, created_at, updated_at, tenant_id
        ) VALUES (
            unnest(?), unnest(?), unnest(?), unnest(?), unnest(?), 
            unnest(?), unnest(?), unnest(?), unnest(?), unnest(?), 
            unnest(?), unnest(?), unnest(?), unnest(?), unnest(?), 
            unnest(?), unnest(?), unnest(?), unnest(?)
        )
        """, nativeQuery = true)
    void batchInsertTransactions(
        @Param("ids") UUID[] ids,
        @Param("references") String[] references,
        @Param("externalReferences") String[] externalReferences,
        @Param("fromAccountIds") UUID[] fromAccountIds,
        @Param("toAccountIds") UUID[] toAccountIds,
        @Param("paymentTypeIds") UUID[] paymentTypeIds,
        @Param("amounts") BigDecimal[] amounts,
        @Param("currencyCodes") String[] currencyCodes,
        @Param("feeAmounts") BigDecimal[] feeAmounts,
        @Param("statuses") String[] statuses,
        @Param("transactionTypes") String[] transactionTypes,
        @Param("descriptions") String[] descriptions,
        @Param("metadata") String[] metadata,
        @Param("initiatedAts") LocalDateTime[] initiatedAts,
        @Param("processedAts") LocalDateTime[] processedAts,
        @Param("completedAts") LocalDateTime[] completedAts,
        @Param("createdAts") LocalDateTime[] createdAts,
        @Param("updatedAts") LocalDateTime[] updatedAts,
        @Param("tenantIds") String[] tenantIds
    );
    
    /**
     * Batch update transaction statuses
     * Optimized for bulk status updates
     */
    @Modifying
    @Query("UPDATE Transaction t SET t.status = :status, t.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE t.id IN :transactionIds")
    int batchUpdateTransactionStatus(@Param("transactionIds") List<UUID> transactionIds, 
                                   @Param("status") Transaction.TransactionStatus status);
    
    /**
     * Find transactions by tenant with optimized query
     * Uses covering index for better performance
     */
    @Query("SELECT t FROM Transaction t WHERE t.tenantId = :tenantId " +
           "AND t.createdAt >= :startDate AND t.createdAt <= :endDate " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findTransactionsByTenantAndDateRange(
        @Param("tenantId") String tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * Find active transactions for processing
     * Optimized query for high-frequency processing
     */
    @Query("SELECT t FROM Transaction t WHERE t.status IN ('PENDING', 'PROCESSING') " +
           "AND t.createdAt >= :cutoffTime " +
           "ORDER BY t.createdAt ASC")
    List<Transaction> findActiveTransactionsForProcessing(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Count transactions by status for monitoring
     * Optimized for real-time monitoring
     */
    @Query("SELECT t.status, COUNT(t) FROM Transaction t " +
           "WHERE t.createdAt >= :startTime " +
           "GROUP BY t.status")
    List<Object[]> countTransactionsByStatus(@Param("startTime") LocalDateTime startTime);
    
    /**
     * Find high-value transactions for fraud monitoring
     * Optimized query with proper indexing
     */
    @Query("SELECT t FROM Transaction t WHERE t.amount > :threshold " +
           "AND t.createdAt >= :startDate " +
           "AND t.status = 'COMPLETED' " +
           "ORDER BY t.amount DESC")
    List<Transaction> findHighValueTransactions(
        @Param("threshold") BigDecimal threshold,
        @Param("startDate") LocalDateTime startDate
    );
    
    /**
     * Find duplicate transactions for fraud detection
     * Optimized query for duplicate detection
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
    
    /**
     * Get transaction statistics for performance monitoring
     * Optimized query for dashboard and monitoring
     */
    @Query("SELECT " +
           "COUNT(t) as totalCount, " +
           "COALESCE(SUM(t.amount), 0) as totalAmount, " +
           "COALESCE(AVG(t.amount), 0) as averageAmount, " +
           "COALESCE(MAX(t.amount), 0) as maxAmount, " +
           "COALESCE(MIN(t.amount), 0) as minAmount, " +
           "COUNT(t) FILTER (WHERE t.status = 'COMPLETED') as completedCount, " +
           "COUNT(t) FILTER (WHERE t.status = 'FAILED') as failedCount " +
           "FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    Object[] getTransactionStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find transactions for account with optimized query
     * Uses covering index for better performance
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.createdAt >= :startDate " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findTransactionsByAccountOptimized(
        @Param("accountId") UUID accountId,
        @Param("startDate") LocalDateTime startDate,
        Pageable pageable
    );
    
    /**
     * Find transactions by payment type with optimized query
     * Optimized for payment type analysis
     */
    @Query("SELECT t FROM Transaction t WHERE t.paymentTypeId = :paymentTypeId " +
           "AND t.createdAt >= :startDate " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findTransactionsByPaymentTypeOptimized(
        @Param("paymentTypeId") UUID paymentTypeId,
        @Param("startDate") LocalDateTime startDate,
        Pageable pageable
    );
    
    /**
     * Find failed transactions for retry processing
     * Optimized query for retry mechanisms
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' " +
           "AND (t.metadata IS NULL OR " +
           "JSON_EXTRACT(t.metadata, '$.retry_count') IS NULL OR " +
           "CAST(JSON_EXTRACT(t.metadata, '$.retry_count') AS INTEGER) < :maxRetries) " +
           "AND t.createdAt >= :cutoffTime " +
           "ORDER BY t.createdAt ASC")
    List<Transaction> findFailedTransactionsForRetry(
        @Param("maxRetries") Integer maxRetries,
        @Param("cutoffTime") LocalDateTime cutoffTime
    );
    
    /**
     * Update transaction metadata in batch
     * Optimized for bulk metadata updates
     */
    @Modifying
    @Query("UPDATE Transaction t SET t.metadata = :metadata, t.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE t.id = :transactionId")
    int updateTransactionMetadata(@Param("transactionId") UUID transactionId, 
                                @Param("metadata") String metadata);
    
    /**
     * Find transactions by external reference
     * Optimized for external system integration
     */
    @Query("SELECT t FROM Transaction t WHERE t.externalReference = :externalReference " +
           "AND t.tenantId = :tenantId")
    Optional<Transaction> findByExternalReferenceAndTenant(
        @Param("externalReference") String externalReference,
        @Param("tenantId") String tenantId
    );
    
    /**
     * Get daily transaction volume by tenant
     * Optimized for reporting and analytics
     */
    @Query("SELECT DATE(t.createdAt) as transactionDate, " +
           "COUNT(t) as transactionCount, " +
           "COALESCE(SUM(t.amount), 0) as totalAmount " +
           "FROM Transaction t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.createdAt >= :startDate " +
           "AND t.createdAt <= :endDate " +
           "GROUP BY DATE(t.createdAt) " +
           "ORDER BY transactionDate DESC")
    List<Object[]> getDailyTransactionVolumeByTenant(
        @Param("tenantId") String tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find transactions by status and date range
     * Optimized query for status-based filtering
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = :status " +
           "AND t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findTransactionsByStatusAndDateRange(
        @Param("status") Transaction.TransactionStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * Check if transaction reference exists
     * Optimized for uniqueness validation
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM Transaction t WHERE t.transactionReference = :reference")
    boolean existsByTransactionReferenceOptimized(@Param("reference") String reference);
    
    /**
     * Find transactions for cleanup (old completed transactions)
     * Optimized for maintenance operations
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'COMPLETED' " +
           "AND t.completedAt < :cutoffDate " +
           "ORDER BY t.completedAt ASC")
    Page<Transaction> findCompletedTransactionsForCleanup(
        @Param("cutoffDate") LocalDateTime cutoffDate,
        Pageable pageable
    );
    
    /**
     * Get transaction processing time statistics
     * Optimized for performance monitoring
     */
    @Query("SELECT " +
           "AVG(EXTRACT(EPOCH FROM (t.completedAt - t.createdAt))) as avgProcessingTime, " +
           "MIN(EXTRACT(EPOCH FROM (t.completedAt - t.createdAt))) as minProcessingTime, " +
           "MAX(EXTRACT(EPOCH FROM (t.completedAt - t.createdAt))) as maxProcessingTime, " +
           "PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY EXTRACT(EPOCH FROM (t.completedAt - t.createdAt))) as p95ProcessingTime " +
           "FROM Transaction t " +
           "WHERE t.status = 'COMPLETED' " +
           "AND t.completedAt IS NOT NULL " +
           "AND t.createdAt >= :startDate")
    Object[] getTransactionProcessingTimeStatistics(@Param("startDate") LocalDateTime startDate);
}