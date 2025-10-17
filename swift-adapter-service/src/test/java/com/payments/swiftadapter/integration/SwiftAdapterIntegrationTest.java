package com.payments.swiftadapter.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.swiftadapter.domain.SwiftAdapter;
import com.payments.swiftadapter.fixtures.TenantContextTestDataBuilder;
import com.payments.swiftadapter.repository.SwiftAdapterRepository;
import com.payments.swiftadapter.service.SwiftAdapterService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for SWIFT adapter using Testcontainers
 *
 * <p>These tests verify end-to-end behavior with a real database. They use test data builders for
 * consistency and maintainability.
 */
@SpringBootTest
@Testcontainers
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("SWIFT Adapter Integration Tests")
class SwiftAdapterIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("swift_adapter_test")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private SwiftAdapterService swiftAdapterService;
  @Autowired private SwiftAdapterRepository swiftAdapterRepository;

  private TenantContext tenantContext;
  private ClearingAdapterId adapterId;

  @BeforeEach
  void setUp() {
    tenantContext = TenantContextTestDataBuilder.aTenantContext().build();
    adapterId = ClearingAdapterId.generate();
    // Clear repository before each test for isolation
    swiftAdapterRepository.deleteAll();
  }

  @Nested
  @DisplayName("Create Adapter Integration Tests")
  class CreateAdapterIntegrationTests {

    @Test
    @DisplayName("Should create SWIFT adapter end-to-end")
    void shouldCreateSwiftAdapter() throws Exception {
      // Given
      String adapterName = "Test SWIFT Adapter";
      String endpoint = "https://swift.test.com/api";
      String createdBy = "test-user";

      // When
      SwiftAdapter adapter =
          swiftAdapterService
              .createAdapter(adapterId, tenantContext, adapterName, endpoint, createdBy)
              .get();

      // Then
      assertThat(adapter).isNotNull();
      assertThat(adapter.getId()).isEqualTo(adapterId);
      assertThat(adapter.getAdapterName()).isEqualTo(adapterName);
      assertThat(adapter.getEndpoint()).isEqualTo(endpoint);
      assertThat(adapter.getTenantContext()).isEqualTo(tenantContext);
      assertThat(adapter.getCreatedBy()).isEqualTo(createdBy);
      assertThat(adapter.isActive()).isFalse(); // Initially inactive
    }

    @Test
    @DisplayName("Should persist adapter to database")
    void shouldPersistAdapterToDatabase() throws Exception {
      // Given
      SwiftAdapter adapter =
          swiftAdapterService
              .createAdapter(
                  adapterId,
                  tenantContext,
                  "Test SWIFT Adapter",
                  "https://swift.test.com/api",
                  "user")
              .get();

      // When
      Optional<SwiftAdapter> foundAdapter = swiftAdapterRepository.findById(adapterId);

      // Then
      assertThat(foundAdapter).isPresent();
      assertThat(foundAdapter.get().getId()).isEqualTo(adapterId);
      assertThat(foundAdapter.get().getAdapterName()).isEqualTo("Test SWIFT Adapter");
    }
  }

  @Nested
  @DisplayName("Activate/Deactivate Integration Tests")
  class ActivateDeactivateIntegrationTests {

    @Test
    @DisplayName("Should activate SWIFT adapter successfully")
    void shouldActivateSwiftAdapter() throws Exception {
      // Given
      SwiftAdapter adapter = createTestAdapter();
      String activatedBy = "admin-user";

      // When
      SwiftAdapter activatedAdapter =
          swiftAdapterService.activateAdapter(adapter.getId(), activatedBy);

      // Then
      assertThat(activatedAdapter.isActive()).isTrue();
      assertThat(activatedAdapter.getUpdatedBy()).isEqualTo(activatedBy);

      // Verify persistence
      Optional<SwiftAdapter> persistedAdapter = swiftAdapterRepository.findById(adapter.getId());
      assertThat(persistedAdapter).isPresent();
      assertThat(persistedAdapter.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("Should deactivate SWIFT adapter successfully")
    void shouldDeactivateSwiftAdapter() throws Exception {
      // Given
      SwiftAdapter adapter = createTestAdapter();
      swiftAdapterService.activateAdapter(adapter.getId(), "admin");
      String deactivatedBy = "admin-user";
      String reason = "Maintenance";

      // When
      SwiftAdapter deactivatedAdapter =
          swiftAdapterService.deactivateAdapter(adapter.getId(), reason, deactivatedBy);

      // Then
      assertThat(deactivatedAdapter.isActive()).isFalse();
      assertThat(deactivatedAdapter.getUpdatedBy()).isEqualTo(deactivatedBy);

      // Verify persistence
      Optional<SwiftAdapter> persistedAdapter = swiftAdapterRepository.findById(adapter.getId());
      assertThat(persistedAdapter).isPresent();
      assertThat(persistedAdapter.get().isActive()).isFalse();
    }
  }

  @Nested
  @DisplayName("Update Configuration Integration Tests")
  class UpdateConfigurationIntegrationTests {

    @Test
    @DisplayName("Should update adapter configuration end-to-end")
    void shouldUpdateAdapterConfiguration() throws Exception {
      // Given
      SwiftAdapter adapter = createTestAdapter();
      String newEndpoint = "https://swift.new.com/api";
      String newApiVersion = "2.0";
      Integer newTimeoutSeconds = 30;
      Integer newRetryAttempts = 5;
      Boolean newEncryptionEnabled = true;
      Integer newBatchSize = 200;
      String newProcessingWindowStart = "09:00";
      String newProcessingWindowEnd = "17:00";
      String updatedBy = "admin-user";

      // When
      SwiftAdapter updatedAdapter =
          swiftAdapterService.updateAdapterConfiguration(
              adapter.getId(),
              newEndpoint,
              newApiVersion,
              newTimeoutSeconds,
              newRetryAttempts,
              newEncryptionEnabled,
              newBatchSize,
              newProcessingWindowStart,
              newProcessingWindowEnd,
              updatedBy);

      // Then
      assertThat(updatedAdapter.getEndpoint()).isEqualTo(newEndpoint);
      assertThat(updatedAdapter.getApiVersion()).isEqualTo(newApiVersion);
      assertThat(updatedAdapter.getTimeoutSeconds()).isEqualTo(newTimeoutSeconds);
      assertThat(updatedAdapter.getRetryAttempts()).isEqualTo(newRetryAttempts);
      assertThat(updatedAdapter.getEncryptionEnabled()).isEqualTo(newEncryptionEnabled);
      assertThat(updatedAdapter.getUpdatedBy()).isEqualTo(updatedBy);

      // Verify persistence
      Optional<SwiftAdapter> persistedAdapter = swiftAdapterRepository.findById(adapter.getId());
      assertThat(persistedAdapter).isPresent();
      assertThat(persistedAdapter.get().getEndpoint()).isEqualTo(newEndpoint);
    }
  }

  @Nested
  @DisplayName("Query Integration Tests")
  class QueryIntegrationTests {

    @Test
    @DisplayName("Should get adapter by ID")
    void shouldGetAdapterById() throws Exception {
      // Given
      SwiftAdapter expectedAdapter = createTestAdapter();

      // When
      Optional<SwiftAdapter> actualAdapter =
          swiftAdapterService.getAdapter(expectedAdapter.getId());

      // Then
      assertThat(actualAdapter).isPresent();
      assertThat(actualAdapter.get().getId()).isEqualTo(expectedAdapter.getId());
    }

    @Test
    @DisplayName("Should get adapters by tenant")
    void shouldGetAdaptersByTenant() throws Exception {
      // Given
      createTestAdapter();
      createTestAdapterWithDifferentTenant();

      // When
      List<SwiftAdapter> adapters =
          swiftAdapterService.getAdaptersByTenant(
              tenantContext.getTenantId(), tenantContext.getBusinessUnitId());

      // Then
      assertThat(adapters).hasSize(1);
      assertThat(adapters.get(0).getTenantContext().getTenantId())
          .isEqualTo(tenantContext.getTenantId());
    }

    @Test
    @DisplayName("Should get active adapters by tenant")
    void shouldGetActiveAdaptersByTenant() throws Exception {
      // Given
      SwiftAdapter adapter1 = createTestAdapter();
      SwiftAdapter adapter2 = createTestAdapter();
      swiftAdapterService.activateAdapter(adapter1.getId(), "admin");

      // When
      List<SwiftAdapter> activeAdapters =
          swiftAdapterService.getActiveAdaptersByTenant(tenantContext.getTenantId());

      // Then
      assertThat(activeAdapters).hasSize(1);
      assertThat(activeAdapters.get(0).getId()).isEqualTo(adapter1.getId());
      assertThat(activeAdapters.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle empty results for tenant with no adapters")
    void shouldHandleEmptyResultsForTenantWithNoAdapters() {
      // Given
      String unknownTenantId = "unknown-tenant";

      // When
      List<SwiftAdapter> adapters =
          swiftAdapterService.getAdaptersByTenant(unknownTenantId, "unknown-bu");

      // Then
      assertThat(adapters).isEmpty();
    }
  }

  @Nested
  @DisplayName("Delete Integration Tests")
  class DeleteIntegrationTests {

    @Test
    @DisplayName("Should delete adapter successfully")
    void shouldDeleteAdapter() throws Exception {
      // Given
      SwiftAdapter adapter = createTestAdapter();

      // When
      swiftAdapterService.deleteAdapter(adapter.getId());

      // Then
      Optional<SwiftAdapter> deletedAdapter = swiftAdapterService.getAdapter(adapter.getId());
      assertThat(deletedAdapter).isEmpty();
    }
  }

  @Nested
  @DisplayName("Validation Integration Tests")
  class ValidationIntegrationTests {

    @Test
    @DisplayName("Should validate adapter configuration successfully")
    void shouldValidateAdapterConfiguration() throws Exception {
      // Given
      SwiftAdapter adapter = createTestAdapter();

      // When
      Boolean isValid = swiftAdapterService.validateAdapterConfiguration(adapter.getId());

      // Then
      assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-existent adapter validation")
    void shouldReturnFalseForNonExistentAdapterValidation() {
      // Given
      ClearingAdapterId nonExistentId = ClearingAdapterId.generate();

      // When
      Boolean isValid = swiftAdapterService.validateAdapterConfiguration(nonExistentId);

      // Then
      assertThat(isValid).isFalse();
    }
  }

  /** Test fixture: Creates a test adapter using the test data builder */
  private SwiftAdapter createTestAdapter() throws Exception {
    return swiftAdapterService
        .createAdapter(
            ClearingAdapterId.generate(),
            tenantContext,
            "Test SWIFT Adapter",
            "https://swift.test.com/api",
            "test-user")
        .get();
  }

  /** Test fixture: Creates a test adapter with a different tenant */
  private SwiftAdapter createTestAdapterWithDifferentTenant() throws Exception {
    TenantContext differentTenant =
        TenantContextTestDataBuilder.aTenantContext()
            .withTenantId("tenant-different")
            .withBusinessUnitId("bu-different")
            .build();

    return swiftAdapterService
        .createAdapter(
            ClearingAdapterId.generate(),
            differentTenant,
            "Different Tenant Adapter",
            "https://swift.different.com/api",
            "test-user")
        .get();
  }
}
