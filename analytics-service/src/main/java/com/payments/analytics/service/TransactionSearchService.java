package com.payments.analytics.service;

import com.payments.analytics.repository.TransactionSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Transaction Search Service
 * 
 * Provides business logic for transaction search and reporting operations.
 * Enables operations teams to search, analyze, and report on transactions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionSearchService {

    private final TransactionSearchRepository transactionSearchRepository;

    /**
     * Search transactions with advanced filtering
     */
    @Transactional(readOnly = true)
    public TransactionSearchResult searchTransactions(
            String tenantId,
            String businessUnitId,
            int page,
            int size,
            String transactionId,
            String paymentId,
            String status,
            String amountFrom,
            String amountTo,
            String currency,
            String startDate,
            String endDate,
            String clearingSystem,
            String channel,
            String sortBy,
            String sortDirection) {
        
        log.info("Searching transactions for tenant: {}, business unit: {}, page: {}, size: {}", 
                tenantId, businessUnitId, page, size);
        
        try {
            // Parse dates
            LocalDateTime startDateTime = startDate != null ? 
                LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            LocalDateTime endDateTime = endDate != null ? 
                LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            
            // Parse amounts
            BigDecimal amountFromDecimal = amountFrom != null ? new BigDecimal(amountFrom) : null;
            BigDecimal amountToDecimal = amountTo != null ? new BigDecimal(amountTo) : null;
            
            // Create sort
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Search transactions
            Page<TransactionEntity> transactionPage = transactionSearchRepository.findTransactions(
                tenantId, businessUnitId, transactionId, paymentId, status,
                amountFromDecimal, amountToDecimal, currency, startDateTime, endDateTime,
                clearingSystem, channel, pageable);
            
            List<TransactionSummary> transactions = transactionPage.getContent().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
            
            return TransactionSearchResult.builder()
                .transactions(transactions)
                .totalCount((int) transactionPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to search transactions for tenant: {}, business unit: {}", 
                    tenantId, businessUnitId, e);
            throw new RuntimeException("Failed to search transactions", e);
        }
    }

    /**
     * Get transaction details
     */
    @Transactional(readOnly = true)
    public TransactionDetails getTransactionDetails(String transactionId, String tenantId, String businessUnitId) {
        log.info("Getting transaction details for: {}, tenant: {}, business unit: {}", 
                transactionId, tenantId, businessUnitId);
        
        try {
            TransactionEntity entity = transactionSearchRepository.findByTransactionIdAndTenantIdAndBusinessUnitId(
                transactionId, tenantId, businessUnitId);
            
            if (entity == null) {
                throw new IllegalArgumentException("Transaction not found: " + transactionId);
            }
            
            return mapToDetails(entity);
                
        } catch (IllegalArgumentException e) {
            log.warn("Transaction not found: {}", transactionId);
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve transaction details for: {}", transactionId, e);
            throw new RuntimeException("Failed to retrieve transaction details", e);
        }
    }

    /**
     * Get transaction statistics
     */
    @Transactional(readOnly = true)
    public TransactionStatistics getTransactionStatistics(
            String tenantId,
            String businessUnitId,
            String startDate,
            String endDate,
            String currency,
            String clearingSystem,
            String channel) {
        
        log.info("Getting transaction statistics for tenant: {}, business unit: {}", tenantId, businessUnitId);
        
        try {
            // Parse dates
            LocalDateTime startDateTime = startDate != null ? 
                LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            LocalDateTime endDateTime = endDate != null ? 
                LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            
            // Get statistics
            TransactionStatisticsData statsData = transactionSearchRepository.getTransactionStatistics(
                tenantId, businessUnitId, startDateTime, endDateTime, currency, clearingSystem, channel);
            
            return TransactionStatistics.builder()
                .totalTransactions(statsData.getTotalTransactions())
                .successfulTransactions(statsData.getSuccessfulTransactions())
                .failedTransactions(statsData.getFailedTransactions())
                .pendingTransactions(statsData.getPendingTransactions())
                .totalVolume(statsData.getTotalVolume().toString())
                .averageAmount(statsData.getAverageAmount().toString())
                .successRate(statsData.getSuccessRate())
                .averageProcessingTime(statsData.getAverageProcessingTime())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to retrieve transaction statistics", e);
            throw new RuntimeException("Failed to retrieve transaction statistics", e);
        }
    }

    /**
     * Export transactions to CSV
     */
    @Transactional(readOnly = true)
    public String exportTransactionsToCsv(
            String tenantId,
            String businessUnitId,
            String transactionId,
            String paymentId,
            String status,
            String startDate,
            String endDate,
            String clearingSystem,
            String channel) {
        
        log.info("Exporting transactions to CSV for tenant: {}, business unit: {}", tenantId, businessUnitId);
        
        try {
            // Parse dates
            LocalDateTime startDateTime = startDate != null ? 
                LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            LocalDateTime endDateTime = endDate != null ? 
                LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            
            // Parse amounts
            BigDecimal amountFromDecimal = null;
            BigDecimal amountToDecimal = null;
            
            // Get all transactions (no pagination for export)
            List<TransactionEntity> transactions = transactionSearchRepository.findAllTransactions(
                tenantId, businessUnitId, transactionId, paymentId, status,
                amountFromDecimal, amountToDecimal, null, startDateTime, endDateTime,
                clearingSystem, channel);
            
            // Generate CSV
            StringBuilder csv = new StringBuilder();
            csv.append("Transaction ID,Payment ID,Status,Amount,Currency,Clearing System,Channel,Created At,Updated At,Completed At,Error Message\n");
            
            for (TransactionEntity entity : transactions) {
                csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    entity.getTransactionId(),
                    entity.getPaymentId(),
                    entity.getStatus(),
                    entity.getAmount(),
                    entity.getCurrency(),
                    entity.getClearingSystem(),
                    entity.getChannel(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt(),
                    entity.getCompletedAt(),
                    entity.getErrorMessage() != null ? entity.getErrorMessage().replace(",", ";") : ""
                ));
            }
            
            return csv.toString();
            
        } catch (Exception e) {
            log.error("Failed to export transactions to CSV", e);
            throw new RuntimeException("Failed to export transactions to CSV", e);
        }
    }

    /**
     * Get transaction trends
     */
    @Transactional(readOnly = true)
    public TransactionTrendsResponse getTransactionTrends(
            String tenantId,
            String businessUnitId,
            String period,
            String granularity,
            String currency,
            String clearingSystem,
            String channel) {
        
        log.info("Getting transaction trends for tenant: {}, business unit: {}, period: {}", 
                tenantId, businessUnitId, period);
        
        try {
            List<TrendDataPoint> dataPoints = transactionSearchRepository.getTransactionTrends(
                tenantId, businessUnitId, period, granularity, currency, clearingSystem, channel);
            
            return TransactionTrendsResponse.builder()
                .dataPoints(dataPoints)
                .period(period)
                .granularity(granularity)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to retrieve transaction trends", e);
            throw new RuntimeException("Failed to retrieve transaction trends", e);
        }
    }

    /**
     * Map entity to summary
     */
    private TransactionSummary mapToSummary(TransactionEntity entity) {
        return TransactionSummary.builder()
            .transactionId(entity.getTransactionId())
            .paymentId(entity.getPaymentId())
            .status(entity.getStatus())
            .amount(entity.getAmount().toString())
            .currency(entity.getCurrency())
            .clearingSystem(entity.getClearingSystem())
            .channel(entity.getChannel())
            .createdAt(entity.getCreatedAt().toString())
            .updatedAt(entity.getUpdatedAt().toString())
            .build();
    }

    /**
     * Map entity to details
     */
    private TransactionDetails mapToDetails(TransactionEntity entity) {
        return TransactionDetails.builder()
            .transactionId(entity.getTransactionId())
            .paymentId(entity.getPaymentId())
            .status(entity.getStatus())
            .amount(entity.getAmount().toString())
            .currency(entity.getCurrency())
            .clearingSystem(entity.getClearingSystem())
            .channel(entity.getChannel())
            .createdAt(entity.getCreatedAt().toString())
            .updatedAt(entity.getUpdatedAt().toString())
            .completedAt(entity.getCompletedAt() != null ? entity.getCompletedAt().toString() : null)
            .errorMessage(entity.getErrorMessage())
            .metadata(entity.getMetadata())
            .build();
    }

    // Result DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionSearchResult {
        private List<TransactionSummary> transactions;
        private Integer totalCount;
        private Integer page;
        private Integer size;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionSummary {
        private String transactionId;
        private String paymentId;
        private String status;
        private String amount;
        private String currency;
        private String clearingSystem;
        private String channel;
        private String createdAt;
        private String updatedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionDetails {
        private String transactionId;
        private String paymentId;
        private String status;
        private String amount;
        private String currency;
        private String clearingSystem;
        private String channel;
        private String createdAt;
        private String updatedAt;
        private String completedAt;
        private String errorMessage;
        private Map<String, Object> metadata;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionStatistics {
        private Integer totalTransactions;
        private Integer successfulTransactions;
        private Integer failedTransactions;
        private Integer pendingTransactions;
        private String totalVolume;
        private String averageAmount;
        private Double successRate;
        private Double averageProcessingTime;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionTrendsResponse {
        private List<TrendDataPoint> dataPoints;
        private String period;
        private String granularity;
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TrendDataPoint {
        private String timestamp;
        private Integer count;
        private String volume;
        private Double successRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionStatisticsData {
        private Integer totalTransactions;
        private Integer successfulTransactions;
        private Integer failedTransactions;
        private Integer pendingTransactions;
        private BigDecimal totalVolume;
        private BigDecimal averageAmount;
        private Double successRate;
        private Double averageProcessingTime;
    }
}
