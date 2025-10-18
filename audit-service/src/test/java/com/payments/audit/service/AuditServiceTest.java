package com.payments.audit.service;

import com.payments.audit.entity.AuditEventEntity;
import com.payments.audit.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditService.
 *
 * <p>Coverage:
 * - All 9 public methods
 * - Query methods with pagination
 * - Search methods (keyword, time range, resource)
 * - Statistics generation
 * - Multi-tenancy enforcement
 * - Validation (tenant_id, user_id, keyword, time range)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Unit Tests")
class AuditServiceTest {

  @Mock private AuditEventRepository auditEventRepository;

  @InjectMocks private AuditService auditService;

  private UUID tenantId;
  private UUID otherTenantId;
  private String userId;
  private String action;
  private String resource;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Pageable pageable;
  private List<AuditEventEntity> testEvents;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    otherTenantId = UUID.randomUUID();
    userId = "user@example.com";
    action = "LOGIN";
    resource = "USER_ACCOUNT";
    startTime = LocalDateTime.now().minusHours(1);
    endTime = LocalDateTime.now();
    pageable = PageRequest.of(0, 10);

    // Create test audit events
    testEvents = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      testEvents.add(
          AuditEventEntity.builder()
              .id(UUID.randomUUID())
              .tenantId(tenantId)
              .userId(userId)
              .action(action)
              .resource(resource)
              .result(AuditEventEntity.AuditResult.SUCCESS)
              .timestamp(LocalDateTime.now().minusHours(i))
              .build());
    }
  }

  @Test
  @DisplayName("Get audit logs - returns paginated results")
  void testGetAuditLogs() {
    // Arrange
    Page<AuditEventEntity> expected = new PageImpl<>(testEvents, pageable, testEvents.size());
    when(auditEventRepository.findByTenantId(tenantId, pageable)).thenReturn(expected);

    // Act
    Page<AuditEventEntity> result = auditService.getAuditLogs(tenantId, pageable);

    // Assert
    assertEquals(5, result.getNumberOfElements());
    assertEquals(5, result.getTotalElements());
    verify(auditEventRepository, times(1)).findByTenantId(tenantId, pageable);
  }

  @Test
  @DisplayName("Get audit logs - throws on null tenant ID")
  void testGetAuditLogsNullTenant() {
    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> auditService.getAuditLogs(null, pageable));
  }

  @Test
  @DisplayName("Get audit logs by user - filters correctly")
  void testGetAuditLogsByUser() {
    // Arrange
    Page<AuditEventEntity> expected = new PageImpl<>(testEvents, pageable, testEvents.size());
    when(auditEventRepository.findByTenantIdAndUserId(tenantId, userId, pageable))
        .thenReturn(expected);

    // Act
    Page<AuditEventEntity> result = auditService.getAuditLogsByUser(tenantId, userId, pageable);

    // Assert
    assertEquals(5, result.getNumberOfElements());
    verify(auditEventRepository, times(1))
        .findByTenantIdAndUserId(tenantId, userId, pageable);
  }

  @Test
  @DisplayName("Get audit logs by user - throws on null user ID")
  void testGetAuditLogsByUserNullUserId() {
    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> auditService.getAuditLogsByUser(tenantId, null, pageable));
  }

  @Test
  @DisplayName("Get audit logs by action - filters correctly")
  void testGetAuditLogsByAction() {
    // Arrange
    Page<AuditEventEntity> expected = new PageImpl<>(testEvents, pageable, testEvents.size());
    when(auditEventRepository.findByTenantIdAndAction(tenantId, action, pageable))
        .thenReturn(expected);

    // Act
    Page<AuditEventEntity> result = auditService.getAuditLogsByAction(tenantId, action, pageable);

    // Assert
    assertEquals(5, result.getNumberOfElements());
    verify(auditEventRepository, times(1))
        .findByTenantIdAndAction(tenantId, action, pageable);
  }

  @Test
  @DisplayName("Get denied access attempts - returns security incidents")
  void testGetDeniedAccessAttempts() {
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

    Page<AuditEventEntity> expected = new PageImpl<>(deniedEvents, pageable, 1);
    when(auditEventRepository.findDeniedAccessAttempts(tenantId, pageable)).thenReturn(expected);

    // Act
    Page<AuditEventEntity> result = auditService.getDeniedAccessAttempts(tenantId, pageable);

    // Assert
    assertEquals(1, result.getNumberOfElements());
    assertEquals(AuditEventEntity.AuditResult.DENIED, result.getContent().get(0).getResult());
    verify(auditEventRepository, times(1)).findDeniedAccessAttempts(tenantId, pageable);
  }

  @Test
  @DisplayName("Get error events - returns system failures")
  void testGetErrorEvents() {
    // Arrange
    List<AuditEventEntity> errorEvents = new ArrayList<>();
    errorEvents.add(
        AuditEventEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .userId(userId)
            .action("PROCESS_PAYMENT")
            .result(AuditEventEntity.AuditResult.ERROR)
            .details("Database connection timeout")
            .timestamp(LocalDateTime.now())
            .build());

    Page<AuditEventEntity> expected = new PageImpl<>(errorEvents, pageable, 1);
    when(auditEventRepository.findErrorEvents(tenantId, pageable)).thenReturn(expected);

    // Act
    Page<AuditEventEntity> result = auditService.getErrorEvents(tenantId, pageable);

    // Assert
    assertEquals(1, result.getNumberOfElements());
    assertEquals(AuditEventEntity.AuditResult.ERROR, result.getContent().get(0).getResult());
    verify(auditEventRepository, times(1)).findErrorEvents(tenantId, pageable);
  }

  @Test
  @DisplayName("Search by time range - returns events within window")
  void testSearchByTimeRange() {
    // Arrange
    Page<AuditEventEntity> expected = new PageImpl<>(testEvents, pageable, testEvents.size());
    when(auditEventRepository.findByTenantIdAndTimestampBetween(
            tenantId, startTime, endTime, pageable))
        .thenReturn(expected);

    // Act
    Page<AuditEventEntity> result =
        auditService.searchByTimeRange(tenantId, startTime, endTime, pageable);

    // Assert
    assertEquals(5, result.getNumberOfElements());
    verify(auditEventRepository, times(1))
        .findByTenantIdAndTimestampBetween(tenantId, startTime, endTime, pageable);
  }

  @Test
  @DisplayName("Search by time range - throws when start after end")
  void testSearchByTimeRangeInvalid() {
    // Arrange
    LocalDateTime invalidStart = endTime.plusHours(1);

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> auditService.searchByTimeRange(tenantId, invalidStart, endTime, pageable));
  }

  @Test
  @DisplayName("Search by resource - filters correctly")
  void testSearchByResource() {
    // Arrange
    Page<AuditEventEntity> expected = new PageImpl<>(testEvents, pageable, testEvents.size());
    when(auditEventRepository.findByTenantIdAndResource(tenantId, resource, pageable))
        .thenReturn(expected);

    // Act
    Page<AuditEventEntity> result = auditService.searchByResource(tenantId, resource, pageable);

    // Assert
    assertEquals(5, result.getNumberOfElements());
    verify(auditEventRepository, times(1))
        .findByTenantIdAndResource(tenantId, resource, pageable);
  }

  @Test
  @DisplayName("Search by keyword - returns matching events")
  void testSearchByKeyword() {
    // Arrange
    String keyword = "pay";
    Page<AuditEventEntity> expected = new PageImpl<>(testEvents, pageable, testEvents.size());
    when(auditEventRepository.searchByKeyword(tenantId, keyword, pageable))
        .thenReturn(expected);

    // Act
    Page<AuditEventEntity> result = auditService.search(tenantId, keyword, pageable);

    // Assert
    assertEquals(5, result.getNumberOfElements());
    verify(auditEventRepository, times(1)).searchByKeyword(tenantId, keyword, pageable);
  }

  @Test
  @DisplayName("Search by keyword - throws on keyword too short")
  void testSearchByKeywordTooShort() {
    // Act & Assert
    assertThrows(
        IllegalArgumentException.class, () -> auditService.search(tenantId, "a", pageable));
  }

  @Test
  @DisplayName("Get audit stats - returns counts by result type")
  void testGetAuditStats() {
    // Arrange
    when(auditEventRepository.countByTenantIdAndResult(tenantId, "SUCCESS")).thenReturn(1450L);
    when(auditEventRepository.countByTenantIdAndResult(tenantId, "DENIED")).thenReturn(35L);
    when(auditEventRepository.countByTenantIdAndResult(tenantId, "ERROR")).thenReturn(15L);

    // Act
    Map<String, Long> stats = auditService.getAuditStats(tenantId);

    // Assert
    assertEquals(1500L, stats.get("total"));
    assertEquals(1450L, stats.get("success"));
    assertEquals(35L, stats.get("denied"));
    assertEquals(15L, stats.get("errors"));
    verify(auditEventRepository, times(3)).countByTenantIdAndResult(eq(tenantId), anyString());
  }

  @Test
  @DisplayName("Multi-tenancy - no data leakage between tenants")
  void testMultiTenancyIsolation() {
    // Arrange
    Page<AuditEventEntity> tenant1Events =
        new PageImpl<>(testEvents, pageable, testEvents.size());
    Page<AuditEventEntity> tenant2Events = new PageImpl<>(new ArrayList<>(), pageable, 0);

    when(auditEventRepository.findByTenantId(tenantId, pageable)).thenReturn(tenant1Events);
    when(auditEventRepository.findByTenantId(otherTenantId, pageable))
        .thenReturn(tenant2Events);

    // Act
    Page<AuditEventEntity> result1 = auditService.getAuditLogs(tenantId, pageable);
    Page<AuditEventEntity> result2 = auditService.getAuditLogs(otherTenantId, pageable);

    // Assert
    assertEquals(5, result1.getNumberOfElements());
    assertEquals(0, result2.getNumberOfElements());
    verify(auditEventRepository, times(1)).findByTenantId(tenantId, pageable);
    verify(auditEventRepository, times(1)).findByTenantId(otherTenantId, pageable);
  }

  @Test
  @DisplayName("Validation - catches all invalid inputs")
  void testValidationComprehensive() {
    // Test null tenant ID
    assertThrows(
        IllegalArgumentException.class, () -> auditService.getAuditLogs(null, pageable));

    // Test null user ID
    assertThrows(
        IllegalArgumentException.class,
        () -> auditService.getAuditLogsByUser(tenantId, null, pageable));

    // Test empty user ID
    assertThrows(
        IllegalArgumentException.class,
        () -> auditService.getAuditLogsByUser(tenantId, "", pageable));

    // Test null action
    assertThrows(
        IllegalArgumentException.class,
        () -> auditService.getAuditLogsByAction(tenantId, null, pageable));

    // Test null resource
    assertThrows(
        IllegalArgumentException.class,
        () -> auditService.searchByResource(tenantId, null, pageable));

    // Test null keyword
    assertThrows(
        IllegalArgumentException.class, () -> auditService.search(tenantId, null, pageable));

    // Test short keyword
    assertThrows(
        IllegalArgumentException.class, () -> auditService.search(tenantId, "a", pageable));
  }

  @Test
  @DisplayName("Pagination - respects page and size parameters")
  void testPagination() {
    // Arrange
    Pageable page0 = PageRequest.of(0, 10);
    Pageable page1 = PageRequest.of(1, 10);

    List<AuditEventEntity> page0Events = testEvents.subList(0, 5);
    List<AuditEventEntity> page1Events = new ArrayList<>();

    Page<AuditEventEntity> page0Result = new PageImpl<>(page0Events, page0, 15);
    Page<AuditEventEntity> page1Result = new PageImpl<>(page1Events, page1, 15);

    when(auditEventRepository.findByTenantId(tenantId, page0)).thenReturn(page0Result);
    when(auditEventRepository.findByTenantId(tenantId, page1)).thenReturn(page1Result);

    // Act
    Page<AuditEventEntity> result0 = auditService.getAuditLogs(tenantId, page0);
    Page<AuditEventEntity> result1 = auditService.getAuditLogs(tenantId, page1);

    // Assert
    assertEquals(5, result0.getNumberOfElements());
    assertEquals(0, result1.getNumberOfElements());
    assertEquals(0, result0.getNumber());
    assertEquals(1, result1.getNumber());
  }
}
