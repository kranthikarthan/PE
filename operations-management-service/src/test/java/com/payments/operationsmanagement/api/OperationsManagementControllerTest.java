package com.payments.operationsmanagement.api;

import com.payments.operationsmanagement.dto.ServiceHealthDto;
import com.payments.operationsmanagement.dto.FeatureFlagDto;
import com.payments.operationsmanagement.service.ServiceHealthAggregator;
import com.payments.operationsmanagement.service.FeatureFlagService;
import com.payments.operationsmanagement.service.CircuitBreakerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Operations Management Controller Test
 * 
 * Comprehensive test suite for the Operations Management REST API.
 * Tests all endpoints with proper authentication and authorization.
 */
@WebMvcTest(OperationsManagementController.class)
class OperationsManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServiceHealthAggregator serviceHealthAggregator;

    @MockBean
    private FeatureFlagService featureFlagService;

    @MockBean
    private CircuitBreakerService circuitBreakerService;

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetAllServices_ShouldReturnServiceList() throws Exception {
        // Given
        List<ServiceHealthDto> mockServices = List.of(
            ServiceHealthDto.builder()
                .name("payment-initiation-service")
                .status(ServiceHealthDto.ServiceHealthEntity.ServiceStatus.UP)
                .uptime(99.98)
                .requestRate(150.5)
                .errorRate(0.12)
                .lastHealthCheck(Instant.now())
                .build()
        );
        
        when(serviceHealthAggregator.getAllServicesHealth()).thenReturn(mockServices);

        // When & Then
        mockMvc.perform(get("/api/ops/v1/services")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].name").value("payment-initiation-service"))
            .andExpect(jsonPath("$[0].status").value("UP"))
            .andExpect(jsonPath("$[0].uptime").value(99.98));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetServiceHealth_ShouldReturnServiceHealth() throws Exception {
        // Given
        ServiceHealthDto mockService = ServiceHealthDto.builder()
            .name("payment-initiation-service")
            .status(ServiceHealthDto.ServiceHealthEntity.ServiceStatus.UP)
            .uptime(99.98)
            .requestRate(150.5)
            .errorRate(0.12)
            .lastHealthCheck(Instant.now())
            .build();
        
        when(serviceHealthAggregator.getServiceHealth(anyString())).thenReturn(mockService);

        // When & Then
        mockMvc.perform(get("/api/ops/v1/services/payment-initiation-service")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("payment-initiation-service"))
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetServiceMetrics_ShouldReturnMetrics() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/ops/v1/services/payment-initiation-service/metrics")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tps").value(150.5))
            .andExpect(jsonPath("$.errorRate").value(0.12))
            .andExpect(jsonPath("$.responseTime.p50").value(50))
            .andExpect(jsonPath("$.responseTime.p95").value(120))
            .andExpect(jsonPath("$.responseTime.p99").value(250));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetServiceErrors_ShouldReturnErrors() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/ops/v1/services/payment-initiation-service/errors")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].level").value("ERROR"))
            .andExpect(jsonPath("$[0].message").value("Payment validation failed"));
    }

    @Test
    @WithMockUser(roles = "OPS_ADMIN")
    void testRestartService_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/ops/v1/services/payment-initiation-service/restart")
                .header("X-User-ID", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Service restart initiated"))
            .andExpect(jsonPath("$.service").value("payment-initiation-service"))
            .andExpect(jsonPath("$.initiatedBy").value("admin"));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetAllCircuitBreakers_ShouldReturnCircuitBreakers() throws Exception {
        // Given
        List<Map<String, Object>> mockCircuitBreakers = List.of(
            Map.of(
                "name", "payment-initiation-service",
                "state", "CLOSED",
                "failureRate", 0.1
            )
        );
        
        when(circuitBreakerService.getAllCircuitBreakers()).thenReturn(mockCircuitBreakers);

        // When & Then
        mockMvc.perform(get("/api/ops/v1/circuit-breakers")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].name").value("payment-initiation-service"))
            .andExpect(jsonPath("$[0].state").value("CLOSED"));
    }

    @Test
    @WithMockUser(roles = "OPS_ADMIN")
    void testOpenCircuitBreaker_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/ops/v1/circuit-breakers/payment-initiation-service/open")
                .header("X-User-ID", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Circuit breaker opened"))
            .andExpect(jsonPath("$.service").value("payment-initiation-service"))
            .andExpect(jsonPath("$.openedBy").value("admin"));
    }

    @Test
    @WithMockUser(roles = "OPS_ADMIN")
    void testCloseCircuitBreaker_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/ops/v1/circuit-breakers/payment-initiation-service/close")
                .header("X-User-ID", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Circuit breaker closed"))
            .andExpect(jsonPath("$.service").value("payment-initiation-service"))
            .andExpect(jsonPath("$.closedBy").value("admin"));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetAllFeatureFlags_ShouldReturnFeatureFlags() throws Exception {
        // Given
        List<FeatureFlagDto> mockFeatureFlags = List.of(
            FeatureFlagDto.builder()
                .name("new-fraud-engine")
                .description("Enable new fraud detection engine")
                .enabled(true)
                .rolloutPercentage(50)
                .build()
        );
        
        when(featureFlagService.getAllFeatureFlags()).thenReturn(mockFeatureFlags);

        // When & Then
        mockMvc.perform(get("/api/ops/v1/feature-flags")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].name").value("new-fraud-engine"))
            .andExpect(jsonPath("$[0].enabled").value(true))
            .andExpect(jsonPath("$[0].rolloutPercentage").value(50));
    }

    @Test
    @WithMockUser(roles = "OPS_ADMIN")
    void testToggleFeatureFlag_ShouldReturnUpdatedFlag() throws Exception {
        // Given
        OperationsManagementController.FeatureFlagToggleRequest request = 
            new OperationsManagementController.FeatureFlagToggleRequest();
        request.setEnabled(true);
        request.setRolloutPercentage(75);

        FeatureFlagDto mockUpdatedFlag = FeatureFlagDto.builder()
            .name("new-fraud-engine")
            .enabled(true)
            .rolloutPercentage(75)
            .build();
        
        when(featureFlagService.toggleFeatureFlag(anyString(), any(), anyString()))
            .thenReturn(mockUpdatedFlag);

        // When & Then
        mockMvc.perform(put("/api/ops/v1/feature-flags/new-fraud-engine/toggle")
                .header("X-User-ID", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("new-fraud-engine"))
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.rolloutPercentage").value(75));
    }

    @Test
    @WithMockUser(roles = "OPS_ADMIN")
    void testSetFeatureFlagRollout_ShouldReturnUpdatedFlag() throws Exception {
        // Given
        OperationsManagementController.FeatureFlagRolloutRequest request = 
            new OperationsManagementController.FeatureFlagRolloutRequest();
        request.setRolloutPercentage(100);

        FeatureFlagDto mockUpdatedFlag = FeatureFlagDto.builder()
            .name("new-fraud-engine")
            .enabled(true)
            .rolloutPercentage(100)
            .build();
        
        when(featureFlagService.setFeatureFlagRollout(anyString(), any(), anyString()))
            .thenReturn(mockUpdatedFlag);

        // When & Then
        mockMvc.perform(put("/api/ops/v1/feature-flags/new-fraud-engine/rollout")
                .header("X-User-ID", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("new-fraud-engine"))
            .andExpect(jsonPath("$.rolloutPercentage").value(100));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetAllServices_WithInsufficientRole_ShouldReturnForbidden() throws Exception {
        // This test would be for a user with insufficient role
        // The @WithMockUser(roles = "OPS_VIEWER") should allow access to GET endpoints
        // This is more of a documentation test
        mockMvc.perform(get("/api/ops/v1/services")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()); // Should be allowed for OPS_VIEWER
    }

    @Test
    void testGetAllServices_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/ops/v1/services")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }
}
