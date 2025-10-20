package com.payments.analytics.api;

import com.payments.analytics.service.TransactionSearchService;
import com.payments.analytics.service.TransactionSearchService.TransactionSearchResult;
import com.payments.analytics.service.TransactionSearchService.TransactionDetails;
import com.payments.analytics.service.TransactionSearchService.TransactionStatistics;
import com.payments.analytics.service.TransactionSearchService.TransactionTrendsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Transaction Search Controller Tests
 * 
 * Comprehensive test suite for transaction search and reporting operations.
 * Tests all endpoints with various scenarios and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(TransactionSearchController.class)
@SpringJUnitConfig
class TransactionSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionSearchService transactionSearchService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TENANT_ID = "tenant-123";
    private static final String BUSINESS_UNIT_ID = "business-unit-123";
    private static final String TRANSACTION_ID = "transaction-123";
    private static final String PAYMENT_ID = "payment-123";

    @BeforeEach
    void setUp() {
        // Setup common test data
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void searchTransactions_ShouldReturnTransactionList_WhenValidRequest() throws Exception {
        // Given
        TransactionSearchResult mockResult = TransactionSearchResult.builder()
            .transactions(List.of(createMockTransactionSummary()))
            .totalCount(1)
            .page(0)
            .size(20)
            .build();

        when(transactionSearchService.searchTransactions(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq(0), eq(20), 
            isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), 
            isNull(), isNull(), isNull(), eq("createdAt"), eq("DESC")))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/search")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactions").isArray())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void searchTransactions_ShouldReturnFilteredResults_WhenFiltersApplied() throws Exception {
        // Given
        TransactionSearchResult mockResult = TransactionSearchResult.builder()
            .transactions(List.of(createMockTransactionSummary()))
            .totalCount(1)
            .page(0)
            .size(20)
            .build();

        when(transactionSearchService.searchTransactions(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq(0), eq(20), 
            eq(TRANSACTION_ID), eq(PAYMENT_ID), eq("COMPLETED"), 
            eq("100.00"), eq("1000.00"), eq("USD"), eq("2024-01-01T00:00:00"), 
            eq("2024-12-31T23:59:59"), eq("SAMOS"), eq("MOBILE")))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/search")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("page", "0")
                .param("size", "20")
                .param("transactionId", TRANSACTION_ID)
                .param("paymentId", PAYMENT_ID)
                .param("status", "COMPLETED")
                .param("amountFrom", "100.00")
                .param("amountTo", "1000.00")
                .param("currency", "USD")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59")
                .param("clearingSystem", "SAMOS")
                .param("channel", "MOBILE")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactions").isArray())
            .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getTransactionDetails_ShouldReturnTransactionDetails_WhenTransactionExists() throws Exception {
        // Given
        TransactionDetails mockDetails = TransactionDetails.builder()
            .transactionId(TRANSACTION_ID)
            .paymentId(PAYMENT_ID)
            .status("COMPLETED")
            .amount("100.00")
            .currency("USD")
            .clearingSystem("SAMOS")
            .channel("MOBILE")
            .createdAt(Instant.now().toString())
            .updatedAt(Instant.now().toString())
            .completedAt(Instant.now().toString())
            .metadata(Map.of("key", "value"))
            .build();

        when(transactionSearchService.getTransactionDetails(eq(TRANSACTION_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenReturn(mockDetails);

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/{transactionId}/details", TRANSACTION_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").value(TRANSACTION_ID))
            .andExpect(jsonPath("$.paymentId").value(PAYMENT_ID))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.amount").value("100.00"))
            .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getTransactionDetails_ShouldReturn404_WhenTransactionNotFound() throws Exception {
        // Given
        when(transactionSearchService.getTransactionDetails(eq(TRANSACTION_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenThrow(new IllegalArgumentException("Transaction not found: " + TRANSACTION_ID));

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/{transactionId}/details", TRANSACTION_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getTransactionStatistics_ShouldReturnStatistics_WhenValidRequest() throws Exception {
        // Given
        TransactionStatistics mockStats = TransactionStatistics.builder()
            .totalTransactions(100)
            .successfulTransactions(90)
            .failedTransactions(5)
            .pendingTransactions(5)
            .totalVolume("10000.00")
            .averageAmount("100.00")
            .successRate(90.0)
            .averageProcessingTime(120.5)
            .build();

        when(transactionSearchService.getTransactionStatistics(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), isNull(), isNull(), 
            isNull(), isNull(), isNull()))
            .thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/statistics")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTransactions").value(100))
            .andExpect(jsonPath("$.successfulTransactions").value(90))
            .andExpect(jsonPath("$.failedTransactions").value(5))
            .andExpect(jsonPath("$.pendingTransactions").value(5))
            .andExpect(jsonPath("$.totalVolume").value("10000.00"))
            .andExpect(jsonPath("$.averageAmount").value("100.00"))
            .andExpect(jsonPath("$.successRate").value(90.0))
            .andExpect(jsonPath("$.averageProcessingTime").value(120.5));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getTransactionStatistics_ShouldReturnFilteredStatistics_WhenFiltersApplied() throws Exception {
        // Given
        TransactionStatistics mockStats = TransactionStatistics.builder()
            .totalTransactions(50)
            .successfulTransactions(45)
            .failedTransactions(3)
            .pendingTransactions(2)
            .totalVolume("5000.00")
            .averageAmount("100.00")
            .successRate(90.0)
            .averageProcessingTime(120.5)
            .build();

        when(transactionSearchService.getTransactionStatistics(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq("2024-01-01T00:00:00"), 
            eq("2024-12-31T23:59:59"), eq("USD"), eq("SAMOS"), eq("MOBILE")))
            .thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/statistics")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59")
                .param("currency", "USD")
                .param("clearingSystem", "SAMOS")
                .param("channel", "MOBILE")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTransactions").value(50))
            .andExpect(jsonPath("$.successfulTransactions").value(45))
            .andExpect(jsonPath("$.failedTransactions").value(3))
            .andExpect(jsonPath("$.pendingTransactions").value(2));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void exportTransactionsToCsv_ShouldReturnCsvContent_WhenValidRequest() throws Exception {
        // Given
        String csvContent = "Transaction ID,Payment ID,Status,Amount,Currency\n" +
                          "transaction-123,payment-123,COMPLETED,100.00,USD\n";
        
        when(transactionSearchService.exportTransactionsToCsv(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), isNull(), isNull(), isNull(),
            isNull(), isNull(), isNull(), isNull()))
            .thenReturn(csvContent);

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/export/csv")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=transactions.csv"))
            .andExpect(content().string(csvContent));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void exportTransactionsToCsv_ShouldReturnFilteredCsv_WhenFiltersApplied() throws Exception {
        // Given
        String csvContent = "Transaction ID,Payment ID,Status,Amount,Currency\n" +
                          "transaction-123,payment-123,COMPLETED,100.00,USD\n";
        
        when(transactionSearchService.exportTransactionsToCsv(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq(TRANSACTION_ID), eq(PAYMENT_ID), 
            eq("COMPLETED"), eq("2024-01-01T00:00:00"), eq("2024-12-31T23:59:59"), 
            eq("SAMOS"), eq("MOBILE")))
            .thenReturn(csvContent);

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/export/csv")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("transactionId", TRANSACTION_ID)
                .param("paymentId", PAYMENT_ID)
                .param("status", "COMPLETED")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59")
                .param("clearingSystem", "SAMOS")
                .param("channel", "MOBILE")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=transactions.csv"))
            .andExpect(content().string(csvContent));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getTransactionTrends_ShouldReturnTrends_WhenValidRequest() throws Exception {
        // Given
        TransactionTrendsResponse mockTrends = TransactionTrendsResponse.builder()
            .dataPoints(List.of(createMockTrendDataPoint()))
            .period("7d")
            .granularity("hour")
            .build();

        when(transactionSearchService.getTransactionTrends(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq("7d"), eq("hour"), 
            isNull(), isNull(), isNull()))
            .thenReturn(mockTrends);

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/trends")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("period", "7d")
                .param("granularity", "hour")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dataPoints").isArray())
            .andExpect(jsonPath("$.period").value("7d"))
            .andExpect(jsonPath("$.granularity").value("hour"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getTransactionTrends_ShouldReturnFilteredTrends_WhenFiltersApplied() throws Exception {
        // Given
        TransactionTrendsResponse mockTrends = TransactionTrendsResponse.builder()
            .dataPoints(List.of(createMockTrendDataPoint()))
            .period("30d")
            .granularity("day")
            .build();

        when(transactionSearchService.getTransactionTrends(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq("30d"), eq("day"), 
            eq("USD"), eq("SAMOS"), eq("MOBILE")))
            .thenReturn(mockTrends);

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/trends")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("period", "30d")
                .param("granularity", "day")
                .param("currency", "USD")
                .param("clearingSystem", "SAMOS")
                .param("channel", "MOBILE")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dataPoints").isArray())
            .andExpect(jsonPath("$.period").value("30d"))
            .andExpect(jsonPath("$.granularity").value("day"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void searchTransactions_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(transactionSearchService.searchTransactions(any(), any(), anyInt(), anyInt(), 
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/search")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to search transactions"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getTransactionDetails_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(transactionSearchService.getTransactionDetails(any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/{transactionId}/details", TRANSACTION_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to retrieve transaction details"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getTransactionStatistics_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(transactionSearchService.getTransactionStatistics(any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/statistics")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to retrieve transaction statistics"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void exportTransactionsToCsv_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(transactionSearchService.exportTransactionsToCsv(any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/export/csv")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Failed to export transactions to CSV"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getTransactionTrends_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(transactionSearchService.getTransactionTrends(any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/reporting/v1/transactions/trends")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to retrieve transaction trends"));
    }

    // Helper methods

    private TransactionSearchService.TransactionSummary createMockTransactionSummary() {
        return TransactionSearchService.TransactionSummary.builder()
            .transactionId(TRANSACTION_ID)
            .paymentId(PAYMENT_ID)
            .status("COMPLETED")
            .amount("100.00")
            .currency("USD")
            .clearingSystem("SAMOS")
            .channel("MOBILE")
            .createdAt(Instant.now().toString())
            .updatedAt(Instant.now().toString())
            .build();
    }

    private TransactionSearchService.TrendDataPoint createMockTrendDataPoint() {
        return TransactionSearchService.TrendDataPoint.builder()
            .timestamp(Instant.now().toString())
            .count(10)
            .volume("1000.00")
            .successRate(90.0)
            .build();
    }
}
