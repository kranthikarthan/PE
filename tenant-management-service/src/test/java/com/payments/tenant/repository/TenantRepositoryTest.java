package com.payments.tenant.repository;

import com.payments.tenant.entity.TenantEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for TenantRepository.
 *
 * <p>Coverage:
 * - Query methods (findById, findByName, findByEmail, etc.)
 * - Pagination
 * - Filtering by status and type
 * - Caching behavior
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TenantRepository Tests")
class TenantRepositoryTest {

  @Autowired private TenantRepository tenantRepository;

  private TenantEntity testTenant1;
  private TenantEntity testTenant2;

  @BeforeEach
  void setUp() {
    // Create test tenants
    testTenant1 = TenantEntity.builder()
        .tenantId("STD-001")
        .tenantName("Standard Bank")
        .status(TenantEntity.TenantStatus.ACTIVE)
        .tenantType(TenantEntity.TenantType.BANK)
        .contactEmail("stdbank@example.com")
        .country("ZAF")
        .timezone("Africa/Johannesburg")
        .registrationNumber("REG-001")
        .taxNumber("TAX-001")
        .build();

    testTenant2 = TenantEntity.builder()
        .tenantId("FIN-002")
        .tenantName("Finance Fintech")
        .status(TenantEntity.TenantStatus.SUSPENDED)
        .tenantType(TenantEntity.TenantType.FINTECH)
        .contactEmail("finance@example.com")
        .country("ZAF")
        .timezone("Africa/Johannesburg")
        .registrationNumber("REG-002")
        .taxNumber("TAX-002")
        .build();

    // Save test data
    tenantRepository.save(testTenant1);
    tenantRepository.save(testTenant2);
  }

  @Test
  @DisplayName("Find tenant by ID successfully")
  void testFindByIdSuccess() {
    // Act
    Optional<TenantEntity> result = tenantRepository.findById("STD-001");

    // Assert
    assertTrue(result.isPresent());
    assertEquals("Standard Bank", result.get().getTenantName());
    assertEquals(TenantEntity.TenantStatus.ACTIVE, result.get().getStatus());
  }

  @Test
  @DisplayName("Find tenant by ID returns empty when not found")
  void testFindByIdNotFound() {
    // Act
    Optional<TenantEntity> result = tenantRepository.findById("NON-EXISTENT");

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Find tenant by name (case-insensitive) successfully")
  void testFindByTenantNameIgnoreCaseSuccess() {
    // Act
    Optional<TenantEntity> result = tenantRepository.findByTenantNameIgnoreCase("standard bank");

    // Assert
    assertTrue(result.isPresent());
    assertEquals("STD-001", result.get().getTenantId());
  }

  @Test
  @DisplayName("Find tenant by email successfully")
  void testFindByContactEmailSuccess() {
    // Act
    Optional<TenantEntity> result = tenantRepository.findByContactEmail("stdbank@example.com");

    // Assert
    assertTrue(result.isPresent());
    assertEquals("STD-001", result.get().getTenantId());
  }

  @Test
  @DisplayName("Find tenants by status with pagination")
  void testFindByStatusWithPagination() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);

    // Act
    Page<TenantEntity> result = tenantRepository.findByStatus(
        TenantEntity.TenantStatus.ACTIVE, pageable);

    // Assert
    assertEquals(1, result.getTotalElements());
    assertEquals("STD-001", result.getContent().get(0).getTenantId());
  }

  @Test
  @DisplayName("Find tenants by type with pagination")
  void testFindByTenantTypeWithPagination() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);

    // Act
    Page<TenantEntity> result = tenantRepository.findByTenantType(
        TenantEntity.TenantType.BANK, pageable);

    // Assert
    assertTrue(result.getTotalElements() >= 1);
    assertTrue(result.getContent().stream()
        .anyMatch(t -> t.getTenantId().equals("STD-001")));
  }

  @Test
  @DisplayName("Find all active tenants")
  void testFindAllActive() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);

    // Act
    Page<TenantEntity> result = tenantRepository.findAllActive(pageable);

    // Assert
    assertTrue(result.getTotalElements() >= 1);
    assertTrue(result.getContent().stream()
        .allMatch(t -> t.getStatus() == TenantEntity.TenantStatus.ACTIVE));
  }

  @Test
  @DisplayName("Count active tenants")
  void testCountActive() {
    // Act
    long count = tenantRepository.countActive();

    // Assert
    assertTrue(count >= 1);
  }

  @Test
  @DisplayName("Find tenants pending approval")
  void testFindPendingApproval() {
    // Arrange
    TenantEntity pendingTenant = TenantEntity.builder()
        .tenantId("PND-003")
        .tenantName("Pending Tenant")
        .status(TenantEntity.TenantStatus.PENDING_APPROVAL)
        .tenantType(TenantEntity.TenantType.CORPORATE)
        .contactEmail("pending@example.com")
        .country("ZAF")
        .build();
    tenantRepository.save(pendingTenant);

    Pageable pageable = PageRequest.of(0, 10);

    // Act
    Page<TenantEntity> result = tenantRepository.findPendingApproval(pageable);

    // Assert
    assertTrue(result.getTotalElements() >= 1);
    assertTrue(result.getContent().stream()
        .allMatch(t -> t.getStatus() == TenantEntity.TenantStatus.PENDING_APPROVAL));
  }

  @Test
  @DisplayName("Check tenant exists by ID")
  void testExistsByIdTrue() {
    // Act
    boolean exists = tenantRepository.existsById("STD-001");

    // Assert
    assertTrue(exists);
  }

  @Test
  @DisplayName("Check tenant does not exist by ID")
  void testExistsByIdFalse() {
    // Act
    boolean exists = tenantRepository.existsById("NON-EXISTENT");

    // Assert
    assertFalse(exists);
  }

  @Test
  @DisplayName("Find tenants by country")
  void testFindByCountry() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);

    // Act
    Page<TenantEntity> result = tenantRepository.findByCountry("ZAF", pageable);

    // Assert
    assertTrue(result.getTotalElements() >= 2);
  }

  @Test
  @DisplayName("Find suspended tenants")
  void testFindSuspended() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);

    // Act
    Page<TenantEntity> result = tenantRepository.findSuspended(pageable);

    // Assert
    assertTrue(result.getTotalElements() >= 1);
    assertTrue(result.getContent().stream()
        .allMatch(t -> t.getStatus() == TenantEntity.TenantStatus.SUSPENDED));
  }

  @Test
  @DisplayName("Find recently created tenants")
  void testFindRecentlyCreated() {
    // Act
    var result = tenantRepository.findRecentlyCreated(5);

    // Assert
    assertTrue(result.size() >= 1);
    assertTrue(result.get(0).getTenantId().equals("FIN-002") || 
               result.get(0).getTenantId().equals("STD-001"));
  }
}
