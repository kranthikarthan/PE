package com.payments.analytics.repository;

import com.payments.analytics.entity.TransactionEntity;
import com.payments.analytics.service.TransactionSearchService.TrendDataPoint;
import com.payments.analytics.service.TransactionSearchService.TransactionStatisticsData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Transaction Search Repository
 * 
 * Provides data access for transaction search and reporting operations.
 * Enables operations teams to query and analyze transactions.
 */
@Repository
public interface TransactionSearchRepository extends JpaRepository<TransactionEntity, String> {

    /**
     * Find transactions with advanced filtering
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.tenantId = :tenantId AND t.businessUnitId = :businessUnitId " +
           "AND (:transactionId IS NULL OR t.transactionId = :transactionId) " +
           "AND (:paymentId IS NULL OR t.paymentId = :paymentId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:amountFrom IS NULL OR t.amount >= :amountFrom) " +
           "AND (:amountTo IS NULL OR t.amount <= :amountTo) " +
           "AND (:currency IS NULL OR t.currency = :currency) " +
           "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
           "AND (:clearingSystem IS NULL OR t.clearingSystem = :clearingSystem) " +
           "AND (:channel IS NULL OR t.channel = :channel)")
    Page<TransactionEntity> findTransactions(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("transactionId") String transactionId,
        @Param("paymentId") String paymentId,
        @Param("status") String status,
        @Param("amountFrom") BigDecimal amountFrom,
        @Param("amountTo") BigDecimal amountTo,
        @Param("currency") String currency,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("clearingSystem") String clearingSystem,
        @Param("channel") String channel,
        Pageable pageable);

    /**
     * Find all transactions with filtering (for export)
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.tenantId = :tenantId AND t.businessUnitId = :businessUnitId " +
           "AND (:transactionId IS NULL OR t.transactionId = :transactionId) " +
           "AND (:paymentId IS NULL OR t.paymentId = :paymentId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:amountFrom IS NULL OR t.amount >= :amountFrom) " +
           "AND (:amountTo IS NULL OR t.amount <= :amountTo) " +
           "AND (:currency IS NULL OR t.currency = :currency) " +
           "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
           "AND (:clearingSystem IS NULL OR t.clearingSystem = :clearingSystem) " +
           "AND (:channel IS NULL OR t.channel = :channel)")
    List<TransactionEntity> findAllTransactions(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("transactionId") String transactionId,
        @Param("paymentId") String paymentId,
        @Param("status") String status,
        @Param("amountFrom") BigDecimal amountFrom,
        @Param("amountTo") BigDecimal amountTo,
        @Param("currency") String currency,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("clearingSystem") String clearingSystem,
        @Param("channel") String channel);

    /**
     * Find transaction by ID, tenant, and business unit
     */
    TransactionEntity findByTransactionIdAndTenantIdAndBusinessUnitId(
        String transactionId, String tenantId, String businessUnitId);

    /**
     * Get transaction statistics
     */
    @Query("SELECT " +
           "COUNT(t) as totalTransactions, " +
           "COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as successfulTransactions, " +
           "COUNT(CASE WHEN t.status = 'FAILED' THEN 1 END) as failedTransactions, " +
           "COUNT(CASE WHEN t.status = 'PENDING' THEN 1 END) as pendingTransactions, " +
           "COALESCE(SUM(t.amount), 0) as totalVolume, " +
           "COALESCE(AVG(t.amount), 0) as averageAmount, " +
           "CASE WHEN COUNT(t) > 0 THEN " +
           "  (COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(t)) " +
           "ELSE 0 END as successRate, " +
           "CASE WHEN COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) > 0 THEN " +
           "  AVG(CASE WHEN t.status = 'COMPLETED' THEN " +
           "    EXTRACT(EPOCH FROM (t.completedAt - t.createdAt)) " +
           "  END) " +
           "ELSE 0 END as averageProcessingTime " +
           "FROM TransactionEntity t " +
           "WHERE t.tenantId = :tenantId AND t.businessUnitId = :businessUnitId " +
           "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
           "AND (:currency IS NULL OR t.currency = :currency) " +
           "AND (:clearingSystem IS NULL OR t.clearingSystem = :clearingSystem) " +
           "AND (:channel IS NULL OR t.channel = :channel)")
    TransactionStatisticsData getTransactionStatistics(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("currency") String currency,
        @Param("clearingSystem") String clearingSystem,
        @Param("channel") String channel);

    /**
     * Get transaction trends
     */
    @Query(value = 
        "SELECT " +
        "  DATE_TRUNC(:granularity, t.created_at) as timestamp, " +
        "  COUNT(*) as count, " +
        "  COALESCE(SUM(t.amount), 0) as volume, " +
        "  CASE WHEN COUNT(*) > 0 THEN " +
        "    (COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(*)) " +
        "  ELSE 0 END as success_rate " +
        "FROM transaction_entity t " +
        "WHERE t.tenant_id = :tenantId AND t.business_unit_id = :businessUnitId " +
        "AND t.created_at >= :startDate " +
        "AND (:currency IS NULL OR t.currency = :currency) " +
        "AND (:clearingSystem IS NULL OR t.clearing_system = :clearingSystem) " +
        "AND (:channel IS NULL OR t.channel = :channel) " +
        "GROUP BY DATE_TRUNC(:granularity, t.created_at) " +
        "ORDER BY timestamp", 
        nativeQuery = true)
    List<Object[]> getTransactionTrendsRaw(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("granularity") String granularity,
        @Param("startDate") LocalDateTime startDate,
        @Param("currency") String currency,
        @Param("clearingSystem") String clearingSystem,
        @Param("channel") String channel);

    /**
     * Get transaction trends (converted to DTOs)
     */
    default List<TrendDataPoint> getTransactionTrends(
            String tenantId,
            String businessUnitId,
            String period,
            String granularity,
            String currency,
            String clearingSystem,
            String channel) {
        
        // Calculate start date based on period
        LocalDateTime startDate = calculateStartDate(period);
        
        List<Object[]> rawResults = getTransactionTrendsRaw(
            tenantId, businessUnitId, granularity, startDate, currency, clearingSystem, channel);
        
        return rawResults.stream()
            .map(row -> TrendDataPoint.builder()
                .timestamp(row[0].toString())
                .count(((Number) row[1]).intValue())
                .volume(row[2].toString())
                .successRate(((Number) row[3]).doubleValue())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Calculate start date based on period
     */
    default LocalDateTime calculateStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case "1d" -> now.minusDays(1);
            case "7d" -> now.minusDays(7);
            case "30d" -> now.minusDays(30);
            case "90d" -> now.minusDays(90);
            case "1y" -> now.minusYears(1);
            default -> now.minusDays(7);
        };
    }

    /**
     * Find transactions by payment ID
     */
    List<TransactionEntity> findByPaymentIdAndTenantIdAndBusinessUnitId(
        String paymentId, String tenantId, String businessUnitId);

    /**
     * Find transactions by status
     */
    List<TransactionEntity> findByStatusAndTenantIdAndBusinessUnitId(
        String status, String tenantId, String businessUnitId);

    /**
     * Find transactions by clearing system
     */
    List<TransactionEntity> findByClearingSystemAndTenantIdAndBusinessUnitId(
        String clearingSystem, String tenantId, String businessUnitId);

    /**
     * Find transactions by channel
     */
    List<TransactionEntity> findByChannelAndTenantIdAndBusinessUnitId(
        String channel, String tenantId, String businessUnitId);

    /**
     * Find transactions by currency
     */
    List<TransactionEntity> findByCurrencyAndTenantIdAndBusinessUnitId(
        String currency, String tenantId, String businessUnitId);

    /**
     * Find transactions by amount range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.tenantId = :tenantId AND t.businessUnitId = :businessUnitId " +
           "AND t.amount BETWEEN :amountFrom AND :amountTo")
    List<TransactionEntity> findByAmountRange(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("amountFrom") BigDecimal amountFrom,
        @Param("amountTo") BigDecimal amountTo);

    /**
     * Find transactions by date range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.tenantId = :tenantId AND t.businessUnitId = :businessUnitId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    List<TransactionEntity> findByDateRange(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * Count transactions by status
     */
    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.tenantId = :tenantId AND t.businessUnitId = :businessUnitId " +
           "AND t.status = :status")
    long countByStatus(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("status") String status);

    /**
     * Count total transactions
     */
    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.tenantId = :tenantId AND t.businessUnitId = :businessUnitId")
    long countByTenantAndBusinessUnit(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Find transactions by multiple criteria
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.tenantId = :tenantId AND t.businessUnitId = :businessUnitId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:currency IS NULL OR t.currency = :currency) " +
           "AND (:clearingSystem IS NULL OR t.clearingSystem = :clearingSystem) " +
           "AND (:channel IS NULL OR t.channel = :channel) " +
           "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR t.createdAt <= :endDate)")
    List<TransactionEntity> findByMultipleCriteria(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("status") String status,
        @Param("currency") String currency,
        @Param("clearingSystem") String clearingSystem,
        @Param("channel") String channel,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}
