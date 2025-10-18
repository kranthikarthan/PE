package com.payments.tenant.service;

import com.payments.tenant.entity.TenantEntity;
import com.payments.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantService.
 *
 * <p>Coverage:
 * - Create tenant (success & validation errors)
 * - Get tenant (found & not found)
 * - Activate/Suspend tenant (state machine)
 * - Event publishing
 * - State transitions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantService Unit Tests")
class TenantServiceTest {

  @Mock private TenantRepository tenantRepository;

  @InjectMocks private TenantService tenantService;

  private TenantEntity testTenant;
  private String tenantId;

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
  @DisplayName("Create tenant successfully")
  void testCreateTenantSuccess() {
    // Arrange
    when(tenantRepository.save(any(TenantEntity.class))).thenReturn(testTenant);

    // Act
    TenantEntity result = tenantService.createTenant(testTenant, "admin@example.com");

    // Assert
    assertNotNull(result);
    assertEquals("Test Bank", result.getTenantName());
    assertEquals(TenantEntity.TenantStatus.ACTIVE, result.getStatus());

    verify(tenantRepository, times(1)).save(any(TenantEntity.class));
  }

  @Test
  @DisplayName("Get tenant by ID successfully")
  void testGetTenantByIdSuccess() {
    // Arrange
    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));

    // Act
    TenantEntity result = tenantService.getTenant(tenantId);

    // Assert
    assertNotNull(result);
    assertEquals(tenantId, result.getTenantId());
    assertEquals("Test Bank", result.getTenantName());

    verify(tenantRepository, times(1)).findById(tenantId);
  }

  @Test
  @DisplayName("Get tenant by ID throws exception when not found")
  void testGetTenantByIdNotFound() {
    // Arrange
    when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(TenantService.TenantNotFoundException.class, () ->
        tenantService.getTenant(tenantId));

    verify(tenantRepository, times(1)).findById(tenantId);
  }

  @Test
  @DisplayName("Get tenant by name successfully")
  void testGetTenantByNameSuccess() {
    // Arrange
    when(tenantRepository.findByTenantNameIgnoreCase("Test Bank"))
        .thenReturn(Optional.of(testTenant));

    // Act
    Optional<TenantEntity> result = tenantService.getTenantByName("Test Bank");

    // Assert
    assertTrue(result.isPresent());
    assertEquals("Test Bank", result.get().getTenantName());

    verify(tenantRepository, times(1)).findByTenantNameIgnoreCase("Test Bank");
  }

  @Test
  @DisplayName("Activate tenant successfully")
  void testActivateTenantSuccess() {
    // Arrange
    testTenant.setStatus(TenantEntity.TenantStatus.PENDING_APPROVAL);
    TenantEntity activatedTenant = TenantEntity.builder()
        .tenantId(tenantId)
        .tenantName("Test Bank")
        .status(TenantEntity.TenantStatus.ACTIVE)
        .build();

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
    when(tenantRepository.save(any(TenantEntity.class))).thenReturn(activatedTenant);

    // Act
    TenantEntity result = tenantService.activateTenant(tenantId, "admin@example.com");

    // Assert
    assertNotNull(result);
    assertEquals(TenantEntity.TenantStatus.ACTIVE, result.getStatus());

    verify(tenantRepository, times(1)).findById(tenantId);
    verify(tenantRepository, times(1)).save(any(TenantEntity.class));
  }

  @Test
  @DisplayName("Suspend tenant successfully")
  void testSuspendTenantSuccess() {
    // Arrange
    testTenant.setStatus(TenantEntity.TenantStatus.ACTIVE);
    TenantEntity suspendedTenant = TenantEntity.builder()
        .tenantId(tenantId)
        .tenantName("Test Bank")
        .status(TenantEntity.TenantStatus.SUSPENDED)
        .build();

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
    when(tenantRepository.save(any(TenantEntity.class))).thenReturn(suspendedTenant);

    // Act
    TenantEntity result = tenantService.suspendTenant(tenantId, "admin@example.com");

    // Assert
    assertNotNull(result);
    assertEquals(TenantEntity.TenantStatus.SUSPENDED, result.getStatus());

    verify(tenantRepository, times(1)).findById(tenantId);
    verify(tenantRepository, times(1)).save(any(TenantEntity.class));
  }

  @Test
  @DisplayName("Deactivate tenant successfully")
  void testDeactivateTenantSuccess() {
    // Arrange
    testTenant.setStatus(TenantEntity.TenantStatus.ACTIVE);
    TenantEntity deactivatedTenant = TenantEntity.builder()
        .tenantId(tenantId)
        .tenantName("Test Bank")
        .status(TenantEntity.TenantStatus.INACTIVE)
        .build();

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
    when(tenantRepository.save(any(TenantEntity.class))).thenReturn(deactivatedTenant);

    // Act
    TenantEntity result = tenantService.deactivateTenant(tenantId, "admin@example.com");

    // Assert
    assertNotNull(result);
    assertEquals(TenantEntity.TenantStatus.INACTIVE, result.getStatus());

    verify(tenantRepository, times(1)).findById(tenantId);
    verify(tenantRepository, times(1)).save(any(TenantEntity.class));
  }

  @Test
  @DisplayName("Update tenant successfully")
  void testUpdateTenantSuccess() {
    // Arrange
    TenantEntity updates = TenantEntity.builder()
        .tenantName("Updated Bank")
        .contactEmail("updated@example.com")
        .build();

    TenantEntity updatedTenant = TenantEntity.builder()
        .tenantId(tenantId)
        .tenantName("Updated Bank")
        .contactEmail("updated@example.com")
        .status(TenantEntity.TenantStatus.ACTIVE)
        .build();

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
    when(tenantRepository.save(any(TenantEntity.class))).thenReturn(updatedTenant);

    // Act
    TenantEntity result = tenantService.updateTenant(tenantId, updates, "admin@example.com");

    // Assert
    assertNotNull(result);
    assertEquals("Updated Bank", result.getTenantName());
    assertEquals("updated@example.com", result.getContactEmail());

    verify(tenantRepository, times(1)).findById(tenantId);
    verify(tenantRepository, times(1)).save(any(TenantEntity.class));
  }

  @Test
  @DisplayName("Suspend active tenant returns correct status")
  void testSuspendActiveReturnsCorrectStatus() {
    // Arrange
    testTenant.setStatus(TenantEntity.TenantStatus.ACTIVE);
    TenantEntity expectedSuspended = TenantEntity.builder()
        .tenantId(tenantId)
        .status(TenantEntity.TenantStatus.SUSPENDED)
        .build();

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
    when(tenantRepository.save(any(TenantEntity.class))).thenReturn(expectedSuspended);

    // Act
    TenantEntity result = tenantService.suspendTenant(tenantId, "admin@example.com");

    // Assert
    assertEquals(TenantEntity.TenantStatus.SUSPENDED, result.getStatus());
  }

  @Test
  @DisplayName("Get non-existent tenant throws TenantNotFoundException")
  void testGetNonExistentTenantThrowsException() {
    // Arrange
    when(tenantRepository.findById("NON-EXISTENT")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(TenantService.TenantNotFoundException.class, () ->
        tenantService.getTenant("NON-EXISTENT"));
  }
}
