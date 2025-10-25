package com.payments.analytics.api;

import com.payments.analytics.service.TransactionSearchService;
import com.payments.analytics.service.TransactionSearchService.TransactionSearchResult;
import com.payments.analytics.service.TransactionSearchService.TransactionDetails;
import com.payments.analytics.service.TransactionSearchService.TransactionStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Transaction Search Controller
 * 
 * Provides comprehensive transaction search and reporting capabilities.
 * Enables operations teams to search, analyze, and report on transactions.
 * 
 * Base URL: /api/reporting/v1/transactions
 */
@Slf4j
@RestController
@RequestMapping("/api/reporting/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Search & Reporting", description = "Transaction search and reporting API")
public class TransactionSearchController {

    private final TransactionSearchService transactionSearchService;

    /**
     * Search transactions with advanced filtering
     * 
     * GET /api/reporting/v1/transactions/search
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Search Transactions",
        description = "Search transactions with advanced filtering and pagination")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Transactions retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionSearchResponse.class)))
        })
    public ResponseEntity<TransactionSearchResponse> searchTransactions(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Transaction ID") @RequestParam(required = false) String transactionId,
            @Parameter(description = "Payment ID") @RequestParam(required = false) String paymentId,
            @Parameter(description = "Status") @RequestParam(required = false) String status,
            @Parameter(description = "Amount from") @RequestParam(required = false) String amountFrom,
            @Parameter(description = "Amount to") @RequestParam(required = false) String amountTo,
            @Parameter(description = "Currency") @RequestParam(required = false) String currency,
            @Parameter(description = "Start date") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date") @RequestParam(required = false) String endDate,
            @Parameter(description = "Clearing system") @RequestParam(required = false) String clearingSystem,
            @Parameter(description = "Channel") @RequestParam(required = false) String channel,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Searching transactions for tenant: {}, business unit: {}, page: {}, size: {}", 
                tenantId, businessUnitId, page, size);
        
        try {
            TransactionSearchResult result = transactionSearchService.searchTransactions(
                tenantId, businessUnitId, page, size, transactionId, paymentId, status,
                amountFrom, amountTo, currency, startDate, endDate, clearingSystem, channel,
                sortBy, sortDirection);
            
            TransactionSearchResponse response = TransactionSearchResponse.builder()
                .transactions(result.getTransactions())
                .totalCount(result.getTotalCount())
                .page(result.getPage())
                .size(result.getSize())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to search transactions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TransactionSearchResponse.builder()
                    .errorMessage("Failed to search transactions")
                    .build());
        }
    }

    /**
     * Get transaction details
     * 
     * GET /api/reporting/v1/transactions/{transactionId}/details
     */
    @GetMapping(value = "/{transactionId}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Transaction Details",
        description = "Retrieve detailed information about a specific transaction")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Transaction details retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionDetailsResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Transaction not found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<TransactionDetailsResponse> getTransactionDetails(
            @Parameter(description = "Transaction ID", required = true) @PathVariable("transactionId") String transactionId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting transaction details for: {}, tenant: {}, business unit: {}", 
                transactionId, tenantId, businessUnitId);
        
        try {
            TransactionDetails details = transactionSearchService.getTransactionDetails(
                transactionId, tenantId, businessUnitId);
            
            TransactionDetailsResponse response = TransactionDetailsResponse.builder()
                .transactionId(details.getTransactionId())
                .paymentId(details.getPaymentId())
                .status(details.getStatus())
                .amount(details.getAmount())
                .currency(details.getCurrency())
                .clearingSystem(details.getClearingSystem())
                .channel(details.getChannel())
                .createdAt(details.getCreatedAt())
                .updatedAt(details.getUpdatedAt())
                .completedAt(details.getCompletedAt())
                .errorMessage(details.getErrorMessage())
                .metadata(details.getMetadata())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Transaction not found: {}", transactionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve transaction details for: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TransactionDetailsResponse.builder()
                    .errorMessage("Failed to retrieve transaction details")
                    .build());
        }
    }

    /**
     * Get transaction statistics
     * 
     * GET /api/reporting/v1/transactions/statistics
     */
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Transaction Statistics",
        description = "Retrieve statistics about transactions")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Transaction statistics retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionStatisticsResponse.class)))
        })
    public ResponseEntity<TransactionStatisticsResponse> getTransactionStatistics(
            @Parameter(description = "Start date") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date") @RequestParam(required = false) String endDate,
            @Parameter(description = "Currency") @RequestParam(required = false) String currency,
            @Parameter(description = "Clearing system") @RequestParam(required = false) String clearingSystem,
            @Parameter(description = "Channel") @RequestParam(required = false) String channel,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting transaction statistics for tenant: {}, business unit: {}", tenantId, businessUnitId);
        
        try {
            TransactionStatistics stats = transactionSearchService.getTransactionStatistics(
                tenantId, businessUnitId, startDate, endDate, currency, clearingSystem, channel);
            
            TransactionStatisticsResponse response = TransactionStatisticsResponse.builder()
                .totalTransactions(stats.getTotalTransactions())
                .successfulTransactions(stats.getSuccessfulTransactions())
                .failedTransactions(stats.getFailedTransactions())
                .pendingTransactions(stats.getPendingTransactions())
                .totalVolume(stats.getTotalVolume())
                .averageAmount(stats.getAverageAmount())
                .successRate(stats.getSuccessRate())
                .averageProcessingTime(stats.getAverageProcessingTime())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve transaction statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TransactionStatisticsResponse.builder()
                    .errorMessage("Failed to retrieve transaction statistics")
                    .build());
        }
    }

    /**
     * Export transactions to CSV
     * 
     * GET /api/reporting/v1/transactions/export/csv
     */
    @GetMapping(value = "/export/csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Export Transactions to CSV",
        description = "Export transactions to CSV format for analysis")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "CSV export generated successfully",
                content = @Content(mediaType = "text/csv"))
        })
    public ResponseEntity<String> exportTransactionsToCsv(
            @Parameter(description = "Transaction ID") @RequestParam(required = false) String transactionId,
            @Parameter(description = "Payment ID") @RequestParam(required = false) String paymentId,
            @Parameter(description = "Status") @RequestParam(required = false) String status,
            @Parameter(description = "Start date") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date") @RequestParam(required = false) String endDate,
            @Parameter(description = "Clearing system") @RequestParam(required = false) String clearingSystem,
            @Parameter(description = "Channel") @RequestParam(required = false) String channel,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Exporting transactions to CSV for tenant: {}, business unit: {}", tenantId, businessUnitId);
        
        try {
            String csvContent = transactionSearchService.exportTransactionsToCsv(
                tenantId, businessUnitId, transactionId, paymentId, status,
                startDate, endDate, clearingSystem, channel);
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=transactions.csv")
                .body(csvContent);
            
        } catch (Exception e) {
            log.error("Failed to export transactions to CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to export transactions to CSV");
        }
    }

    /**
     * Get transaction trends
     * 
     * GET /api/reporting/v1/transactions/trends
     */
    @GetMapping(value = "/trends", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Transaction Trends",
        description = "Retrieve transaction trends and patterns")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Transaction trends retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionTrendsResponse.class)))
        })
    public ResponseEntity<TransactionTrendsResponse> getTransactionTrends(
            @Parameter(description = "Period") @RequestParam(defaultValue = "7d") String period,
            @Parameter(description = "Granularity") @RequestParam(defaultValue = "hour") String granularity,
            @Parameter(description = "Currency") @RequestParam(required = false) String currency,
            @Parameter(description = "Clearing system") @RequestParam(required = false) String clearingSystem,
            @Parameter(description = "Channel") @RequestParam(required = false) String channel,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting transaction trends for tenant: {}, business unit: {}, period: {}", 
                tenantId, businessUnitId, period);
        
        try {
            TransactionTrendsResponse response = transactionSearchService.getTransactionTrends(
                tenantId, businessUnitId, period, granularity, currency, clearingSystem, channel);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve transaction trends", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TransactionTrendsResponse.builder()
                    .errorMessage("Failed to retrieve transaction trends")
                    .build());
        }
    }

    // DTOs for the API

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Transaction search response")
    public static class TransactionSearchResponse {
        @Schema(description = "List of transactions")
        private List<TransactionSummary> transactions;
        
        @Schema(description = "Total count of transactions")
        private Integer totalCount;
        
        @Schema(description = "Page number")
        private Integer page;
        
        @Schema(description = "Page size")
        private Integer size;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Transaction summary")
    public static class TransactionSummary {
        @Schema(description = "Transaction ID")
        private String transactionId;
        
        @Schema(description = "Payment ID")
        private String paymentId;
        
        @Schema(description = "Status")
        private String status;
        
        @Schema(description = "Amount")
        private String amount;
        
        @Schema(description = "Currency")
        private String currency;
        
        @Schema(description = "Clearing system")
        private String clearingSystem;
        
        @Schema(description = "Channel")
        private String channel;
        
        @Schema(description = "Created at")
        private String createdAt;
        
        @Schema(description = "Updated at")
        private String updatedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Transaction details response")
    public static class TransactionDetailsResponse {
        @Schema(description = "Transaction ID")
        private String transactionId;
        
        @Schema(description = "Payment ID")
        private String paymentId;
        
        @Schema(description = "Status")
        private String status;
        
        @Schema(description = "Amount")
        private String amount;
        
        @Schema(description = "Currency")
        private String currency;
        
        @Schema(description = "Clearing system")
        private String clearingSystem;
        
        @Schema(description = "Channel")
        private String channel;
        
        @Schema(description = "Created at")
        private String createdAt;
        
        @Schema(description = "Updated at")
        private String updatedAt;
        
        @Schema(description = "Completed at")
        private String completedAt;
        
        @Schema(description = "Error message")
        private String errorMessage;
        
        @Schema(description = "Metadata")
        private java.util.Map<String, Object> metadata;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Transaction statistics response")
    public static class TransactionStatisticsResponse {
        @Schema(description = "Total transactions")
        private Integer totalTransactions;
        
        @Schema(description = "Successful transactions")
        private Integer successfulTransactions;
        
        @Schema(description = "Failed transactions")
        private Integer failedTransactions;
        
        @Schema(description = "Pending transactions")
        private Integer pendingTransactions;
        
        @Schema(description = "Total volume")
        private String totalVolume;
        
        @Schema(description = "Average amount")
        private String averageAmount;
        
        @Schema(description = "Success rate percentage")
        private Double successRate;
        
        @Schema(description = "Average processing time in seconds")
        private Double averageProcessingTime;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Transaction trends response")
    public static class TransactionTrendsResponse {
        @Schema(description = "Trend data points")
        private List<TrendDataPoint> dataPoints;
        
        @Schema(description = "Period")
        private String period;
        
        @Schema(description = "Granularity")
        private String granularity;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Trend data point")
    public static class TrendDataPoint {
        @Schema(description = "Timestamp")
        private String timestamp;
        
        @Schema(description = "Transaction count")
        private Integer count;
        
        @Schema(description = "Total volume")
        private String volume;
        
        @Schema(description = "Success rate")
        private Double successRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Error response")
    public static class ErrorResponse {
        @Schema(description = "Error message")
        private String message;
        
        @Schema(description = "Error code")
        private String code;
        
        @Schema(description = "Timestamp")
        private String timestamp;
    }
}
