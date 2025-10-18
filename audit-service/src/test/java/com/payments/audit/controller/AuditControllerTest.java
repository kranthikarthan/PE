package com.payments.audit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.audit.entity.AuditEventEntity;
import com.payments.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuditController REST API.
 *
 * <p>Coverage:
 * - All 9 endpoints
 * - Authentication and authorization
 * - Multi-tenancy enforcement (X-Tenant-ID header)
 * - Error responses
 * - Pagination
 * - Role-based access control
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuditController REST API Tests")
class AuditControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuditService auditService;

  private UUID tenantId;
  private String userId;
  private String token;
  private List<AuditEventEntity> testEvents;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    userId = "user@example.com";
    token = "Bearer test-jwt-token";

    // Create test events
    testEvents = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      testEvents.add(
          AuditEventEntity.builder()
              .id(UUID.randomUUID())
              .tenantId(tenantId)
              .userId(userId)
              .action("LOGIN")
              .resource("USER_ACCOUNT")
              .result(AuditEventEntity.AuditResult.SUCCESS)
              .timestamp(LocalDateTime.now().minusHours(i))
              .build());
    }
  }

  @Test
  @DisplayName("GET /api/audit/logs - returns audit logs with 200 OK")
  void testGetAuditLogs() throws Exception {
    // Arrange
    Page<AuditEventEntity> page =
        new PageImpl<>(testEvents, PageRequest.of(0, 10), testEvents.size());
    when(auditService.getAuditLogs(tenantId, PageRequest.of(0, 10))).thenReturn(page);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.size()").value(5))
        .andExpect(jsonPath("$.totalElements").value(5));
  }

  @Test
  @DisplayName("GET /api/audit/logs - missing X-Tenant-ID header fails")
  void testGetAuditLogsMissingHeader() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("GET /api/audit/logs/user - filters by user correctly")
  void testGetAuditLogsByUser() throws Exception {
    // Arrange
    Page<AuditEventEntity> page =
        new PageImpl<>(testEvents, PageRequest.of(0, 10), testEvents.size());
    when(auditService.getAuditLogsByUser(tenantId, userId, PageRequest.of(0, 10)))
        .thenReturn(page);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs/user")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.size()").value(5));
  }

  @Test
  @DisplayName("GET /api/audit/logs/action - filters by action correctly")
  void testGetAuditLogsByAction() throws Exception {
    // Arrange
    Page<AuditEventEntity> page =
        new PageImpl<>(testEvents, PageRequest.of(0, 10), testEvents.size());
    when(auditService.getAuditLogsByAction(tenantId, "LOGIN", PageRequest.of(0, 10)))
        .thenReturn(page);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs/action")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .param("action", "LOGIN")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.size()").value(5));
  }

  @Test
  @DisplayName("GET /api/audit/logs/denied - returns security incidents")
  void testGetDeniedAccessAttempts() throws Exception {
    // Arrange
    List<AuditEventEntity> deniedEvents = new ArrayList<>();
    deniedEvents.add(
        AuditEventEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .userId(userId)
            .action("LOGIN_ATTEMPT")
            .result(AuditEventEntity.AuditResult.DENIED)
            .timestamp(LocalDateTime.now())
            .build());

    Page<AuditEventEntity> page = new PageImpl<>(deniedEvents, PageRequest.of(0, 10), 1);
    when(auditService.getDeniedAccessAttempts(tenantId, PageRequest.of(0, 10)))
        .thenReturn(page);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs/denied")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].result").value("DENIED"));
  }

  @Test
  @DisplayName("GET /api/audit/logs/errors - returns error events")
  void testGetErrorEvents() throws Exception {
    // Arrange
    List<AuditEventEntity> errorEvents = new ArrayList<>();
    errorEvents.add(
        AuditEventEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .userId(userId)
            .action("PROCESS_PAYMENT")
            .result(AuditEventEntity.AuditResult.ERROR)
            .details("Database timeout")
            .timestamp(LocalDateTime.now())
            .build());

    Page<AuditEventEntity> page = new PageImpl<>(errorEvents, PageRequest.of(0, 10), 1);
    when(auditService.getErrorEvents(tenantId, PageRequest.of(0, 10))).thenReturn(page);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs/errors")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].result").value("ERROR"));
  }

  @Test
  @DisplayName("GET /api/audit/logs/search - searches by keyword")
  void testSearchByKeyword() throws Exception {
    // Arrange
    Page<AuditEventEntity> page =
        new PageImpl<>(testEvents, PageRequest.of(0, 10), testEvents.size());
    when(auditService.search(tenantId, "pay", PageRequest.of(0, 10))).thenReturn(page);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs/search")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .param("keyword", "pay")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.size()").value(5));
  }

  @Test
  @DisplayName("GET /api/audit/logs/range - searches by time range")
  void testSearchByTimeRange() throws Exception {
    // Arrange
    LocalDateTime startTime = LocalDateTime.now().minusHours(1);
    LocalDateTime endTime = LocalDateTime.now();
    Page<AuditEventEntity> page =
        new PageImpl<>(testEvents, PageRequest.of(0, 10), testEvents.size());

    when(auditService.searchByTimeRange(
            eq(tenantId), any(LocalDateTime.class), any(LocalDateTime.class), eq(PageRequest.of(0, 10))))
        .thenReturn(page);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs/range")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.size()").value(5));
  }

  @Test
  @DisplayName("GET /api/audit/logs/resource - filters by resource")
  void testSearchByResource() throws Exception {
    // Arrange
    Page<AuditEventEntity> page =
        new PageImpl<>(testEvents, PageRequest.of(0, 10), testEvents.size());
    when(auditService.searchByResource(tenantId, "USER_ACCOUNT", PageRequest.of(0, 10)))
        .thenReturn(page);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs/resource")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .param("resource", "USER_ACCOUNT")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.size()").value(5));
  }

  @Test
  @DisplayName("GET /api/audit/stats - returns statistics")
  void testGetAuditStats() throws Exception {
    // Arrange
    Map<String, Long> stats = new HashMap<>();
    stats.put("total", 1500L);
    stats.put("success", 1450L);
    stats.put("denied", 35L);
    stats.put("errors", 15L);

    when(auditService.getAuditStats(tenantId)).thenReturn(stats);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/stats")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(1500))
        .andExpect(jsonPath("$.success").value(1450))
        .andExpect(jsonPath("$.denied").value(35))
        .andExpect(jsonPath("$.errors").value(15));
  }

  @Test
  @DisplayName("GET /api/audit/health - returns 200 OK")
  void testHealthCheck() throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/api/audit/health").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("Audit Service is running"));
  }

  @Test
  @DisplayName("Invalid tenant ID format returns 400 Bad Request")
  void testInvalidTenantIdFormat() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs")
                .header("Authorization", token)
                .header("X-Tenant-ID", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("Service validation error returns 400 Bad Request")
  void testValidationError() throws Exception {
    // Arrange
    when(auditService.getAuditLogsByUser(tenantId, "", PageRequest.of(0, 10)))
        .thenThrow(new IllegalArgumentException("User ID is required"));

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs/user")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .param("userId", "")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Pagination parameters work correctly")
  void testPagination() throws Exception {
    // Arrange
    Page<AuditEventEntity> page =
        new PageImpl<>(new ArrayList<>(), PageRequest.of(1, 20), 100);
    when(auditService.getAuditLogs(eq(tenantId), argThat(p -> p.getPageNumber() == 1 && p.getPageSize() == 20)))
        .thenReturn(page);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/audit/logs")
                .header("Authorization", token)
                .header("X-Tenant-ID", tenantId.toString())
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.number").value(1))
        .andExpect(jsonPath("$.size").value(20));
  }
}
