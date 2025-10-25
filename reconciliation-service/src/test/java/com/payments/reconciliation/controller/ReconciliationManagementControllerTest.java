package com.payments.reconciliation.controller;

import com.payments.reconciliation.service.ReconciliationManagementService;
import com.payments.reconciliation.service.ReconciliationManagementService.ReconciliationRunResult;
import com.payments.reconciliation.service.ReconciliationManagementService.ReconciliationRunDetails;
import com.payments.reconciliation.service.ReconciliationManagementService.ReconciliationExceptionResult;
import com.payments.reconciliation.service.ReconciliationManagementService.ReconciliationStatistics;
import com.payments.reconciliation.service.ReconciliationManagementService.ReconciliationReport;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Reconciliation Management Controller Tests
 * 
 * Comprehensive test suite for reconciliation management operations.
 * Tests all endpoints with various scenarios and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(ReconciliationManagementController.class)
@SpringJUnitConfig
class ReconciliationManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReconciliationManagementService reconciliationManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TENANT_ID = "tenant-123";
    private static final String BUSINESS_UNIT_ID = "business-unit-123";
    private static final String USER_ID = "user-123";
    private static final String RUN_ID = "run-123";
    private static final String EXCEPTION_ID = "exception-123";

    @BeforeEach
    void setUp() {
        // Setup common test data
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getAllReconciliationRuns_ShouldReturnRunList_WhenValidRequest() throws Exception {
        // Given
        ReconciliationRunResult mockResult = ReconciliationRunResult.builder()
            .runs(List.of(createMockReconciliationRunSummary()))
            .totalCount(1)
            .page(0)
            .size(20)
            .build();

        when(reconciliationManagementService.getAllReconciliationRuns(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq(0), eq(20), 
            isNull(), isNull(), isNull(), isNull()))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/runs")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.runs").isArray())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getAllReconciliationRuns_ShouldReturnFilteredResults_WhenFiltersApplied() throws Exception {
        // Given
        ReconciliationRunResult mockResult = ReconciliationRunResult.builder()
            .runs(List.of(createMockReconciliationRunSummary()))
            .totalCount(1)
            .page(0)
            .size(20)
            .build();

        when(reconciliationManagementService.getAllReconciliationRuns(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq(0), eq(20), 
            eq("COMPLETED"), eq("SAMOS"), eq("2024-01-01T00:00:00"), eq("2024-12-31T23:59:59")))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/runs")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("page", "0")
                .param("size", "20")
                .param("status", "COMPLETED")
                .param("clearingSystem", "SAMOS")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.runs").isArray())
            .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getReconciliationRunDetails_ShouldReturnRunDetails_WhenRunExists() throws Exception {
        // Given
        ReconciliationRunDetails mockDetails = ReconciliationRunDetails.builder()
            .runId(RUN_ID)
            .status("COMPLETED")
            .clearingSystem("SAMOS")
            .startedAt(Instant.now().toString())
            .completedAt(Instant.now().toString())
            .totalRecords(1000)
            .matchedRecords(950)
            .unmatchedRecords(50)
            .exceptions(List.of())
            .build();

        when(reconciliationManagementService.getReconciliationRunDetails(eq(RUN_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenReturn(mockDetails);

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/runs/{runId}/details", RUN_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.runId").value(RUN_ID))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.clearingSystem").value("SAMOS"))
            .andExpect(jsonPath("$.totalRecords").value(1000))
            .andExpect(jsonPath("$.matchedRecords").value(950))
            .andExpect(jsonPath("$.unmatchedRecords").value(50));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getReconciliationRunDetails_ShouldReturn404_WhenRunNotFound() throws Exception {
        // Given
        when(reconciliationManagementService.getReconciliationRunDetails(eq(RUN_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenThrow(new IllegalArgumentException("Reconciliation run not found: " + RUN_ID));

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/runs/{runId}/details", RUN_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void startReconciliationRun_ShouldReturnSuccess_WhenValidRequest() throws Exception {
        // Given
        ReconciliationManagementController.StartReconciliationRequest request = ReconciliationManagementController.StartReconciliationRequest.builder()
            .clearingSystem("SAMOS")
            .description("Test reconciliation run")
            .build();

        ReconciliationManagementService.ReconciliationRunResponse mockResponse = ReconciliationManagementService.ReconciliationRunResponse.builder()
            .runId(RUN_ID)
            .status("RUNNING")
            .message("Reconciliation run started successfully")
            .build();

        when(reconciliationManagementService.startReconciliationRun(
            eq("SAMOS"), eq("Test reconciliation run"), eq(USER_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/management/v1/reconciliation/runs/start")
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.runId").value(RUN_ID))
            .andExpect(jsonPath("$.status").value("RUNNING"))
            .andExpect(jsonPath("$.message").value("Reconciliation run started successfully"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void startReconciliationRun_ShouldReturn400_WhenAlreadyRunning() throws Exception {
        // Given
        ReconciliationManagementController.StartReconciliationRequest request = ReconciliationManagementController.StartReconciliationRequest.builder()
            .clearingSystem("SAMOS")
            .description("Test reconciliation run")
            .build();

        when(reconciliationManagementService.startReconciliationRun(
            eq("SAMOS"), eq("Test reconciliation run"), eq(USER_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenThrow(new IllegalArgumentException("Reconciliation is already running for clearing system: SAMOS"));

        // When & Then
        mockMvc.perform(post("/api/management/v1/reconciliation/runs/start")
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorMessage").value("Reconciliation is already running for clearing system: SAMOS"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void stopReconciliationRun_ShouldReturnSuccess_WhenRunCanBeStopped() throws Exception {
        // Given
        ReconciliationManagementController.StopReconciliationRequest request = ReconciliationManagementController.StopReconciliationRequest.builder()
            .reason("Manual stop requested")
            .build();

        ReconciliationManagementService.ReconciliationRunResponse mockResponse = ReconciliationManagementService.ReconciliationRunResponse.builder()
            .runId(RUN_ID)
            .status("STOPPED")
            .message("Reconciliation run stopped successfully")
            .build();

        when(reconciliationManagementService.stopReconciliationRun(
            eq(RUN_ID), eq("Manual stop requested"), eq(USER_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/management/v1/reconciliation/runs/{runId}/stop", RUN_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.runId").value(RUN_ID))
            .andExpect(jsonPath("$.status").value("STOPPED"))
            .andExpect(jsonPath("$.message").value("Reconciliation run stopped successfully"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void stopReconciliationRun_ShouldReturn400_WhenRunCannotBeStopped() throws Exception {
        // Given
        ReconciliationManagementController.StopReconciliationRequest request = ReconciliationManagementController.StopReconciliationRequest.builder()
            .reason("Manual stop requested")
            .build();

        when(reconciliationManagementService.stopReconciliationRun(
            eq(RUN_ID), eq("Manual stop requested"), eq(USER_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenThrow(new IllegalArgumentException("Reconciliation run is not running: COMPLETED"));

        // When & Then
        mockMvc.perform(post("/api/management/v1/reconciliation/runs/{runId}/stop", RUN_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorMessage").value("Reconciliation run is not running: COMPLETED"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getReconciliationExceptions_ShouldReturnExceptions_WhenValidRequest() throws Exception {
        // Given
        ReconciliationExceptionResult mockResult = ReconciliationExceptionResult.builder()
            .exceptions(List.of(createMockReconciliationExceptionSummary()))
            .totalCount(1)
            .page(0)
            .size(20)
            .build();

        when(reconciliationManagementService.getReconciliationExceptions(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq(0), eq(20), 
            isNull(), isNull(), isNull()))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/exceptions")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exceptions").isArray())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void resolveReconciliationException_ShouldReturnSuccess_WhenExceptionCanBeResolved() throws Exception {
        // Given
        ReconciliationManagementController.ResolveExceptionRequest request = ReconciliationManagementController.ResolveExceptionRequest.builder()
            .resolution("Data correction applied")
            .notes("Exception resolved by data correction")
            .build();

        ReconciliationManagementService.ReconciliationExceptionResponse mockResponse = ReconciliationManagementService.ReconciliationExceptionResponse.builder()
            .exceptionId(EXCEPTION_ID)
            .status("RESOLVED")
            .message("Exception resolved successfully")
            .build();

        when(reconciliationManagementService.resolveReconciliationException(
            eq(EXCEPTION_ID), eq("Data correction applied"), eq("Exception resolved by data correction"), 
            eq(USER_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/management/v1/reconciliation/exceptions/{exceptionId}/resolve", EXCEPTION_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exceptionId").value(EXCEPTION_ID))
            .andExpect(jsonPath("$.status").value("RESOLVED"))
            .andExpect(jsonPath("$.message").value("Exception resolved successfully"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void resolveReconciliationException_ShouldReturn400_WhenExceptionCannotBeResolved() throws Exception {
        // Given
        ReconciliationManagementController.ResolveExceptionRequest request = ReconciliationManagementController.ResolveExceptionRequest.builder()
            .resolution("Data correction applied")
            .notes("Exception resolved by data correction")
            .build();

        when(reconciliationManagementService.resolveReconciliationException(
            eq(EXCEPTION_ID), eq("Data correction applied"), eq("Exception resolved by data correction"), 
            eq(USER_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenThrow(new IllegalArgumentException("Exception is already resolved: " + EXCEPTION_ID));

        // When & Then
        mockMvc.perform(post("/api/management/v1/reconciliation/exceptions/{exceptionId}/resolve", EXCEPTION_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorMessage").value("Exception is already resolved: " + EXCEPTION_ID));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getReconciliationStatistics_ShouldReturnStatistics_WhenValidRequest() throws Exception {
        // Given
        ReconciliationStatistics mockStats = ReconciliationStatistics.builder()
            .totalRuns(100)
            .successfulRuns(85)
            .failedRuns(10)
            .runningRuns(5)
            .totalExceptions(25)
            .resolvedExceptions(20)
            .unresolvedExceptions(5)
            .averageProcessingTime(120.5)
            .successRate(85.0)
            .build();

        when(reconciliationManagementService.getReconciliationStatistics(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), isNull(), isNull(), isNull()))
            .thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/statistics")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRuns").value(100))
            .andExpect(jsonPath("$.successfulRuns").value(85))
            .andExpect(jsonPath("$.failedRuns").value(10))
            .andExpect(jsonPath("$.runningRuns").value(5))
            .andExpect(jsonPath("$.totalExceptions").value(25))
            .andExpect(jsonPath("$.resolvedExceptions").value(20))
            .andExpect(jsonPath("$.unresolvedExceptions").value(5))
            .andExpect(jsonPath("$.averageProcessingTime").value(120.5))
            .andExpect(jsonPath("$.successRate").value(85.0));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void generateReconciliationReport_ShouldReturnReport_WhenValidRequest() throws Exception {
        // Given
        ReconciliationReport mockReport = ReconciliationReport.builder()
            .reportId("REPORT-123")
            .format("JSON")
            .generatedAt(Instant.now().toString())
            .summary(Map.of("totalRuns", 100, "successfulRuns", 85))
            .details(Map.of("runs", List.of(), "exceptions", List.of()))
            .build();

        when(reconciliationManagementService.generateReconciliationReport(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), isNull(), isNull(), isNull(), eq("JSON"), eq(USER_ID)))
            .thenReturn(mockReport);

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/reports/generate")
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("format", "JSON")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reportId").value("REPORT-123"))
            .andExpect(jsonPath("$.format").value("JSON"))
            .andExpect(jsonPath("$.summary").isMap())
            .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getAllReconciliationRuns_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(reconciliationManagementService.getAllReconciliationRuns(any(), any(), anyInt(), anyInt(), 
            any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/runs")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to retrieve reconciliation runs"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void startReconciliationRun_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        ReconciliationManagementController.StartReconciliationRequest request = ReconciliationManagementController.StartReconciliationRequest.builder()
            .clearingSystem("SAMOS")
            .description("Test reconciliation run")
            .build();

        when(reconciliationManagementService.startReconciliationRun(any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/management/v1/reconciliation/runs/start")
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to start reconciliation run"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void stopReconciliationRun_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        ReconciliationManagementController.StopReconciliationRequest request = ReconciliationManagementController.StopReconciliationRequest.builder()
            .reason("Manual stop requested")
            .build();

        when(reconciliationManagementService.stopReconciliationRun(any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/management/v1/reconciliation/runs/{runId}/stop", RUN_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to stop reconciliation run"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getReconciliationExceptions_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(reconciliationManagementService.getReconciliationExceptions(any(), any(), anyInt(), anyInt(), 
            any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/exceptions")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to retrieve reconciliation exceptions"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void resolveReconciliationException_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        ReconciliationManagementController.ResolveExceptionRequest request = ReconciliationManagementController.ResolveExceptionRequest.builder()
            .resolution("Data correction applied")
            .notes("Exception resolved by data correction")
            .build();

        when(reconciliationManagementService.resolveReconciliationException(any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/management/v1/reconciliation/exceptions/{exceptionId}/resolve", EXCEPTION_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to resolve reconciliation exception"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getReconciliationStatistics_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(reconciliationManagementService.getReconciliationStatistics(any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/statistics")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to retrieve reconciliation statistics"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void generateReconciliationReport_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(reconciliationManagementService.generateReconciliationReport(any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/management/v1/reconciliation/reports/generate")
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to generate reconciliation report"));
    }

    // Helper methods

    private ReconciliationManagementService.ReconciliationRunSummary createMockReconciliationRunSummary() {
        return ReconciliationManagementService.ReconciliationRunSummary.builder()
            .runId(RUN_ID)
            .status("COMPLETED")
            .clearingSystem("SAMOS")
            .startedAt(Instant.now().toString())
            .completedAt(Instant.now().toString())
            .totalRecords(1000)
            .matchedRecords(950)
            .unmatchedRecords(50)
            .build();
    }

    private ReconciliationManagementService.ReconciliationExceptionSummary createMockReconciliationExceptionSummary() {
        return ReconciliationManagementService.ReconciliationExceptionSummary.builder()
            .exceptionId(EXCEPTION_ID)
            .type("DATA_MISMATCH")
            .severity("MEDIUM")
            .status("OPEN")
            .description("Transaction amount mismatch")
            .createdAt(Instant.now().toString())
            .build();
    }
}
