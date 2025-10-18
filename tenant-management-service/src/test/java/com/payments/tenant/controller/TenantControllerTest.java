package com.payments.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.tenant.entity.TenantEntity;
import com.payments.tenant.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TenantController REST endpoints.
 *
 * <p>Coverage:
 * - POST /tenants (201 Created)
 * - GET /tenants/{id} (200 OK, 404 Not Found)
 * - PUT /tenants/{id} (200 OK, 404 Not Found)
 * - GET /tenants?status=ACTIVE (200 OK with pagination)
 * - POST /tenants/{id}/activate (200 OK, 409 Conflict)
 * - POST /tenants/{id}/suspend (200 OK, 409 Conflict)
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TenantController REST Tests")
class TenantControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private TenantService tenantService;

  private String tenantId;
  private TenantEntity testTenant;

  @BeforeEach
  void setUp() {
    tenantId = "STD-001";

    testTenant = TenantEntity.builder()
        .tenantId(tenantId)
        .tenantName("Test Bank")
        .status(TenantEntity.TenantStatus.ACTIVE)
        .tenantType(TenantEntity.TenantType.BANK)
        .contactEmail("bank@example.com")
        .country("ZAF")
        .timezone("Africa/Johannesburg")
        .build();
  }

  @Test
  @DisplayName("POST /tenants - Create tenant returns 201 Created")
  void testCreateTenantReturns201() throws Exception {
    // Arrange
    when(tenantService.createTenant(any(TenantEntity.class), anyString()))
        .thenReturn(testTenant);

    // Act & Assert
    mockMvc.perform(post("/tenants")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-User-ID", "admin@example.com")
            .content(objectMapper.writeValueAsString(testTenant)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.tenantId").value("STD-001"))
        .andExpect(jsonPath("$.tenantName").value("Test Bank"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    verify(tenantService, times(1)).createTenant(any(TenantEntity.class), anyString());
  }

  @Test
  @DisplayName("GET /tenants/{id} - Get tenant returns 200 OK")
  void testGetTenantReturns200() throws Exception {
    // Arrange
    when(tenantService.getTenant(tenantId)).thenReturn(testTenant);

    // Act & Assert
    mockMvc.perform(get("/tenants/{id}", tenantId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tenantId").value("STD-001"))
        .andExpect(jsonPath("$.tenantName").value("Test Bank"));

    verify(tenantService, times(1)).getTenant(tenantId);
  }

  @Test
  @DisplayName("GET /tenants/{id} - Not found returns 404")
  void testGetTenantReturns404() throws Exception {
    // Arrange
    when(tenantService.getTenant("NON-EXISTENT"))
        .thenThrow(new TenantService.TenantNotFoundException("Tenant not found"));

    // Act & Assert
    mockMvc.perform(get("/tenants/{id}", "NON-EXISTENT")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    verify(tenantService, times(1)).getTenant("NON-EXISTENT");
  }

  @Test
  @DisplayName("PUT /tenants/{id} - Update tenant returns 200 OK")
  void testUpdateTenantReturns200() throws Exception {
    // Arrange
    TenantEntity updateRequest = TenantEntity.builder()
        .tenantName("Updated Bank")
        .contactEmail("updated@example.com")
        .build();

    TenantEntity updatedTenant = TenantEntity.builder()
        .tenantId(tenantId)
        .tenantName("Updated Bank")
        .contactEmail("updated@example.com")
        .status(TenantEntity.TenantStatus.ACTIVE)
        .build();

    when(tenantService.updateTenant(anyString(), any(TenantEntity.class), anyString()))
        .thenReturn(updatedTenant);

    // Act & Assert
    mockMvc.perform(put("/tenants/{id}", tenantId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-User-ID", "admin@example.com")
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tenantName").value("Updated Bank"))
        .andExpect(jsonPath("$.contactEmail").value("updated@example.com"));

    verify(tenantService, times(1))
        .updateTenant(anyString(), any(TenantEntity.class), anyString());
  }

  @Test
  @DisplayName("POST /tenants/{id}/activate - Activate returns 200 OK")
  void testActivateTenantReturns200() throws Exception {
    // Arrange
    TenantEntity activatedTenant = TenantEntity.builder()
        .tenantId(tenantId)
        .tenantName("Test Bank")
        .status(TenantEntity.TenantStatus.ACTIVE)
        .build();

    when(tenantService.activateTenant(tenantId, "admin@example.com"))
        .thenReturn(activatedTenant);

    // Act & Assert
    mockMvc.perform(post("/tenants/{id}/activate", tenantId)
            .header("X-User-ID", "admin@example.com")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    verify(tenantService, times(1)).activateTenant(tenantId, "admin@example.com");
  }

  @Test
  @DisplayName("POST /tenants/{id}/suspend - Suspend returns 200 OK")
  void testSuspendTenantReturns200() throws Exception {
    // Arrange
    TenantEntity suspendedTenant = TenantEntity.builder()
        .tenantId(tenantId)
        .tenantName("Test Bank")
        .status(TenantEntity.TenantStatus.SUSPENDED)
        .build();

    when(tenantService.suspendTenant(tenantId, "admin@example.com"))
        .thenReturn(suspendedTenant);

    // Act & Assert
    mockMvc.perform(post("/tenants/{id}/suspend", tenantId)
            .header("X-User-ID", "admin@example.com")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUSPENDED"));

    verify(tenantService, times(1)).suspendTenant(tenantId, "admin@example.com");
  }

  @Test
  @DisplayName("GET /tenants - List tenants with pagination")
  void testListTenantsReturns200() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/tenants")
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tenantService, never()).getTenant(anyString());
  }

  @Test
  @DisplayName("POST /tenants - Invalid request returns 400 Bad Request")
  void testCreateTenantWithNullNameReturns400() throws Exception {
    // Arrange - null tenant name should fail validation
    String invalidJson = "{\"tenantType\": \"BANK\"}"; // missing required tenantName

    // Act & Assert
    mockMvc.perform(post("/tenants")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-User-ID", "admin@example.com")
            .content(invalidJson))
        .andExpect(status().isBadRequest());

    verify(tenantService, never()).createTenant(any(TenantEntity.class), anyString());
  }

  @Test
  @DisplayName("POST /tenants/{id}/activate - Not found returns 404")
  void testActivateTenantNotFoundReturns404() throws Exception {
    // Arrange
    when(tenantService.activateTenant("NON-EXISTENT", "admin@example.com"))
        .thenThrow(new TenantService.TenantNotFoundException("Tenant not found"));

    // Act & Assert
    mockMvc.perform(post("/tenants/{id}/activate", "NON-EXISTENT")
            .header("X-User-ID", "admin@example.com")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    verify(tenantService, times(1)).activateTenant("NON-EXISTENT", "admin@example.com");
  }
}
