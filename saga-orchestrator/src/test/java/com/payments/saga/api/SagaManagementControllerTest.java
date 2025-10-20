package com.payments.saga.api;

import com.payments.saga.service.SagaManagementService;
import com.payments.saga.service.SagaManagementService.SagaListResult;
import com.payments.saga.service.SagaManagementService.SagaDetails;
import com.payments.saga.service.SagaManagementService.SagaStatistics;
import com.payments.saga.service.SagaManagementService.FailedSagasResult;
import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaId;
import com.payments.saga.domain.SagaStatus;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Saga Management Controller Tests
 * 
 * Comprehensive test suite for saga management operations.
 * Tests all endpoints with various scenarios and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(SagaManagementController.class)
@SpringJUnitConfig
class SagaManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SagaManagementService sagaManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TENANT_ID = "tenant-123";
    private static final String BUSINESS_UNIT_ID = "business-unit-123";
    private static final String USER_ID = "user-123";
    private static final String SAGA_ID = "saga-123";
    private static final String CORRELATION_ID = "corr-123";

    @BeforeEach
    void setUp() {
        // Setup common test data
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getAllSagas_ShouldReturnSagaList_WhenValidRequest() throws Exception {
        // Given
        SagaListResult mockResult = SagaListResult.builder()
            .sagas(List.of(createMockSaga()))
            .totalCount(1)
            .page(0)
            .size(20)
            .build();

        when(sagaManagementService.getAllSagas(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq(0), eq(20), 
            isNull(), isNull(), isNull(), isNull()))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/management/v1/sagas")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sagas").isArray())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getAllSagas_ShouldReturnFilteredResults_WhenFiltersApplied() throws Exception {
        // Given
        SagaListResult mockResult = SagaListResult.builder()
            .sagas(List.of(createMockSaga()))
            .totalCount(1)
            .page(0)
            .size(20)
            .build();

        when(sagaManagementService.getAllSagas(
            eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq(0), eq(20), 
            eq("RUNNING"), eq("PaymentProcessingSaga"), isNull(), isNull()))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/management/v1/sagas")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("page", "0")
                .param("size", "20")
                .param("status", "RUNNING")
                .param("template", "PaymentProcessingSaga")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sagas").isArray())
            .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getSagaDetails_ShouldReturnSagaDetails_WhenSagaExists() throws Exception {
        // Given
        SagaDetails mockDetails = SagaDetails.builder()
            .sagaId(SAGA_ID)
            .status("RUNNING")
            .templateName("PaymentProcessingSaga")
            .paymentId("payment-123")
            .correlationId(CORRELATION_ID)
            .startedAt(Instant.now().toString())
            .lastUpdatedAt(Instant.now().toString())
            .steps(List.of())
            .events(List.of())
            .build();

        when(sagaManagementService.getSagaDetails(eq(SAGA_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenReturn(mockDetails);

        // When & Then
        mockMvc.perform(get("/api/management/v1/sagas/{sagaId}/details", SAGA_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sagaId").value(SAGA_ID))
            .andExpect(jsonPath("$.status").value("RUNNING"))
            .andExpect(jsonPath("$.templateName").value("PaymentProcessingSaga"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getSagaDetails_ShouldReturn404_WhenSagaNotFound() throws Exception {
        // Given
        when(sagaManagementService.getSagaDetails(eq(SAGA_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenThrow(new IllegalArgumentException("Saga not found: " + SAGA_ID));

        // When & Then
        mockMvc.perform(get("/api/management/v1/sagas/{sagaId}/details", SAGA_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void resumeSaga_ShouldReturnSuccess_WhenSagaCanBeResumed() throws Exception {
        // Given
        SagaManagementController.ResumeSagaRequest request = SagaManagementController.ResumeSagaRequest.builder()
            .reason("Manual intervention required")
            .forceResume(false)
            .build();

        Saga mockSaga = createMockSaga();
        when(sagaManagementService.resumeSaga(eq(SAGA_ID), eq("Manual intervention required"), 
            eq(false), eq(USER_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenReturn(mockSaga);

        // When & Then
        mockMvc.perform(post("/api/management/v1/sagas/{sagaId}/resume", SAGA_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sagaId").value(SAGA_ID))
            .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void resumeSaga_ShouldReturn400_WhenSagaCannotBeResumed() throws Exception {
        // Given
        SagaManagementController.ResumeSagaRequest request = SagaManagementController.ResumeSagaRequest.builder()
            .reason("Manual intervention required")
            .forceResume(false)
            .build();

        when(sagaManagementService.resumeSaga(eq(SAGA_ID), eq("Manual intervention required"), 
            eq(false), eq(USER_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenThrow(new IllegalArgumentException("Saga cannot be resumed in current state"));

        // When & Then
        mockMvc.perform(post("/api/management/v1/sagas/{sagaId}/resume", SAGA_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorMessage").value("Saga cannot be resumed in current state"));
    }

    @Test
    @WithMockUser(roles = {"OPS_ADMIN"})
    void forceCompleteSaga_ShouldReturnSuccess_WhenSagaCanBeForceCompleted() throws Exception {
        // Given
        SagaManagementController.ForceCompleteSagaRequest request = SagaManagementController.ForceCompleteSagaRequest.builder()
            .reason("Manual intervention required")
            .skipCompensation(false)
            .build();

        Saga mockSaga = createMockSaga();
        when(sagaManagementService.forceCompleteSaga(eq(SAGA_ID), eq("Manual intervention required"), 
            eq(false), eq(USER_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenReturn(mockSaga);

        // When & Then
        mockMvc.perform(post("/api/management/v1/sagas/{sagaId}/force-complete", SAGA_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sagaId").value(SAGA_ID))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = {"OPS_ADMIN"})
    void forceCompleteSaga_ShouldReturn400_WhenSagaCannotBeForceCompleted() throws Exception {
        // Given
        SagaManagementController.ForceCompleteSagaRequest request = SagaManagementController.ForceCompleteSagaRequest.builder()
            .reason("Manual intervention required")
            .skipCompensation(false)
            .build();

        when(sagaManagementService.forceCompleteSaga(eq(SAGA_ID), eq("Manual intervention required"), 
            eq(false), eq(USER_ID), eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenThrow(new IllegalArgumentException("Saga cannot be force completed in current state"));

        // When & Then
        mockMvc.perform(post("/api/management/v1/sagas/{sagaId}/force-complete", SAGA_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorMessage").value("Saga cannot be force completed in current state"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getSagaStatistics_ShouldReturnStatistics_WhenValidRequest() throws Exception {
        // Given
        SagaStatistics mockStats = SagaStatistics.builder()
            .totalSagas(100)
            .runningSagas(25)
            .completedSagas(70)
            .failedSagas(5)
            .averageExecutionTime(120.5)
            .successRate(95.0)
            .build();

        when(sagaManagementService.getSagaStatistics(eq(TENANT_ID), eq(BUSINESS_UNIT_ID)))
            .thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/management/v1/sagas/statistics")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalSagas").value(100))
            .andExpect(jsonPath("$.runningSagas").value(25))
            .andExpect(jsonPath("$.completedSagas").value(70))
            .andExpect(jsonPath("$.failedSagas").value(5))
            .andExpect(jsonPath("$.averageExecutionTime").value(120.5))
            .andExpect(jsonPath("$.successRate").value(95.0));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getFailedSagas_ShouldReturnFailedSagas_WhenValidRequest() throws Exception {
        // Given
        FailedSagasResult mockResult = FailedSagasResult.builder()
            .failedSagas(List.of(createMockSaga()))
            .totalCount(1)
            .page(0)
            .size(20)
            .build();

        when(sagaManagementService.getFailedSagas(eq(TENANT_ID), eq(BUSINESS_UNIT_ID), eq(0), eq(20)))
            .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/management/v1/sagas/failed")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.failedSagas").isArray())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getAllSagas_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(sagaManagementService.getAllSagas(any(), any(), anyInt(), anyInt(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/management/v1/sagas")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to retrieve sagas"));
    }

    @Test
    @WithMockUser(roles = {"OPS_OPERATOR"})
    void resumeSaga_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        SagaManagementController.ResumeSagaRequest request = SagaManagementController.ResumeSagaRequest.builder()
            .reason("Manual intervention required")
            .forceResume(false)
            .build();

        when(sagaManagementService.resumeSaga(any(), any(), anyBoolean(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/management/v1/sagas/{sagaId}/resume", SAGA_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Saga resume failed"));
    }

    @Test
    @WithMockUser(roles = {"OPS_ADMIN"})
    void forceCompleteSaga_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        SagaManagementController.ForceCompleteSagaRequest request = SagaManagementController.ForceCompleteSagaRequest.builder()
            .reason("Manual intervention required")
            .skipCompensation(false)
            .build();

        when(sagaManagementService.forceCompleteSaga(any(), any(), anyBoolean(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/management/v1/sagas/{sagaId}/force-complete", SAGA_ID)
                .header("X-User-ID", USER_ID)
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Saga force complete failed"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getSagaStatistics_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(sagaManagementService.getSagaStatistics(any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/management/v1/sagas/statistics")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to retrieve saga statistics"));
    }

    @Test
    @WithMockUser(roles = {"OPS_VIEWER"})
    void getFailedSagas_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        when(sagaManagementService.getFailedSagas(any(), any(), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/management/v1/sagas/failed")
                .header("X-Tenant-Id", TENANT_ID)
                .header("X-Business-Unit-Id", BUSINESS_UNIT_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value("Failed to retrieve failed sagas"));
    }

    // Helper methods

    private Saga createMockSaga() {
        return Saga.builder()
            .sagaId(SagaId.of(SAGA_ID))
            .templateName("PaymentProcessingSaga")
            .status(SagaStatus.RUNNING)
            .paymentId("payment-123")
            .correlationId(CORRELATION_ID)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }
}
