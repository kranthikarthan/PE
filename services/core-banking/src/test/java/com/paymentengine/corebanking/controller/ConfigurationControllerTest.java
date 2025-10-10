package com.paymentengine.corebanking.controller;

import com.paymentengine.shared.config.ConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ConfigurationController
 */
@WebMvcTest(ConfigurationController.class)
public class ConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigurationService configurationService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Setup common mock behaviors
    }

    @Test
    @WithMockUser(authorities = {"tenant:read"})
    void testListTenants() throws Exception {
        mockMvc.perform(get("/api/v1/config/tenants")
                .header("X-Tenant-ID", "default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].tenantId").value("default"));
    }

    @Test
    @WithMockUser(authorities = {"tenant:create"})
    void testCreateTenant() throws Exception {
        Map<String, Object> tenantRequest = new HashMap<>();
        tenantRequest.put("tenantId", "test-bank");
        tenantRequest.put("tenantName", "Test Bank");
        tenantRequest.put("tenantType", "BANK");

        doNothing().when(configurationService).createTenant(anyString(), anyString(), any());

        mockMvc.perform(post("/api/v1/config/tenants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tenantRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("test-bank"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @WithMockUser(authorities = {"tenant:read"})
    void testGetTenant() throws Exception {
        Map<String, Object> tenantInfo = new HashMap<>();
        tenantInfo.put("tenantId", "test-bank");
        tenantInfo.put("tenantName", "Test Bank");
        tenantInfo.put("status", "ACTIVE");

        when(configurationService.getTenantInfo("test-bank")).thenReturn(tenantInfo);

        mockMvc.perform(get("/api/v1/config/tenants/test-bank")
                .header("X-Tenant-ID", "test-bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("test-bank"))
                .andExpect(jsonPath("$.tenantName").value("Test Bank"));
    }

    @Test
    @WithMockUser(authorities = {"tenant:config:update"})
    void testSetConfiguration() throws Exception {
        Map<String, Object> configRequest = new HashMap<>();
        configRequest.put("configKey", "payment.max_amount");
        configRequest.put("configValue", "10000.00");
        configRequest.put("environment", "production");

        doNothing().when(configurationService).setConfigValue(
                eq("test-bank"), eq("payment.max_amount"), eq("10000.00"), eq("production"));

        mockMvc.perform(post("/api/v1/config/tenants/test-bank/config")
                .with(csrf())
                .header("X-Tenant-ID", "test-bank")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Configuration updated successfully"));
    }

    @Test
    @WithMockUser(authorities = {"payment-type:create"})
    void testAddPaymentType() throws Exception {
        Map<String, Object> paymentTypeConfig = new HashMap<>();
        paymentTypeConfig.put("code", "TEST_TRANSFER");
        paymentTypeConfig.put("name", "Test Transfer");
        paymentTypeConfig.put("isSynchronous", true);
        paymentTypeConfig.put("maxAmount", 50000.00);

        doNothing().when(configurationService).addPaymentType(eq("test-bank"), any());

        mockMvc.perform(post("/api/v1/config/tenants/test-bank/payment-types")
                .with(csrf())
                .header("X-Tenant-ID", "test-bank")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentTypeConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payment type added successfully"));
    }

    @Test
    @WithMockUser(authorities = {"feature:read"})
    void testCheckFeatureFlag() throws Exception {
        when(configurationService.isFeatureEnabled("test-bank", "advanced-fraud-detection"))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/config/tenants/test-bank/features/advanced-fraud-detection")
                .header("X-Tenant-ID", "test-bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.featureName").value("advanced-fraud-detection"));
    }

    @Test
    @WithMockUser(authorities = {"feature:update"})
    void testSetFeatureFlag() throws Exception {
        Map<String, Object> featureRequest = new HashMap<>();
        featureRequest.put("enabled", true);
        Map<String, Object> config = new HashMap<>();
        config.put("rolloutPercentage", 50);
        featureRequest.put("config", config);

        doNothing().when(configurationService).setFeatureFlag(
                eq("test-bank"), eq("advanced-fraud-detection"), eq(true), any());

        mockMvc.perform(post("/api/v1/config/tenants/test-bank/features/advanced-fraud-detection")
                .with(csrf())
                .header("X-Tenant-ID", "test-bank")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Feature flag updated successfully"));
    }

    @Test
    @WithMockUser(authorities = {"rate-limit:read"})
    void testGetRateLimitConfig() throws Exception {
        Map<String, Object> rateLimitConfig = new HashMap<>();
        rateLimitConfig.put("rateLimitPerMinute", 1000);
        rateLimitConfig.put("burstCapacity", 1500);

        when(configurationService.getRateLimitConfig("test-bank", "/api/v1/transactions"))
                .thenReturn(rateLimitConfig);

        mockMvc.perform(get("/api/v1/config/tenants/test-bank/rate-limits")
                .header("X-Tenant-ID", "test-bank")
                .param("endpoint", "/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rateLimitConfig.rateLimitPerMinute").value(1000));
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/v1/config/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("configuration-service"));
    }

    @Test
    @WithMockUser(authorities = {"tenant:read"})
    void testGetTenantNotFound() throws Exception {
        when(configurationService.getTenantInfo("non-existent"))
                .thenThrow(new IllegalArgumentException("Tenant not found"));

        mockMvc.perform(get("/api/v1/config/tenants/non-existent")
                .header("X-Tenant-ID", "non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"tenant:config:read"})
    void testGetConfigurationHistory() throws Exception {
        mockMvc.perform(get("/api/v1/config/tenants/test-bank/config/history")
                .header("X-Tenant-ID", "test-bank")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("test-bank"));
    }
}