package com.payments.swiftadapter.integration;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.swiftadapter.domain.SwiftAdapter;
import com.payments.swiftadapter.repository.SwiftAdapterRepository;
import com.payments.swiftadapter.service.SwiftAdapterService;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SWIFT adapter using Testcontainers
 */
@SpringBootTest
@Testcontainers
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class SwiftAdapterIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
      .withDatabaseName("swift_adapter_test")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private SwiftAdapterService swiftAdapterService;

  @Autowired
  private SwiftAdapterRepository swiftAdapterRepository;

  private TenantContext tenantContext;
  private ClearingAdapterId adapterId;

  @BeforeEach
  void setUp() {
    tenantContext = TenantContext.of(
        "tenant-123",
        "Test Tenant",
        "business-unit-456",
        "Test Business Unit"
    );
    adapterId = ClearingAdapterId.generate();
  }

  @Test
  void shouldCreateSwiftAdapter() throws Exception {
    // Given
    String adapterName = "Test SWIFT Adapter";
    String endpoint = "https://swift.test.com/api";
    String createdBy = "test-user";

    // When
    CompletableFuture<SwiftAdapter> future = swiftAdapterService.createAdapter(
        adapterId, tenantContext, adapterName, endpoint, createdBy);
    SwiftAdapter adapter = future.get();

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
  void shouldActivateSwiftAdapter() throws Exception {
    // Given
    SwiftAdapter adapter = createTestAdapter();
    String activatedBy = "admin-user";

    // When
    CompletableFuture<SwiftAdapter> future = swiftAdapterService.activateAdapter(
        adapter.getId(), activatedBy);
    SwiftAdapter activatedAdapter = future.get();

    // Then
    assertThat(activatedAdapter.isActive()).isTrue();
    assertThat(activatedAdapter.getUpdatedBy()).isEqualTo(activatedBy);
  }

  @Test
  void shouldDeactivateSwiftAdapter() throws Exception {
    // Given
    SwiftAdapter adapter = createTestAdapter();
    swiftAdapterService.activateAdapter(adapter.getId(), "admin").get();
    String deactivatedBy = "admin-user";
    String reason = "Maintenance";

    // When
    CompletableFuture<SwiftAdapter> future = swiftAdapterService.deactivateAdapter(
        adapter.getId(), reason, deactivatedBy);
    SwiftAdapter deactivatedAdapter = future.get();

    // Then
    assertThat(deactivatedAdapter.isActive()).isFalse();
    assertThat(deactivatedAdapter.getUpdatedBy()).isEqualTo(deactivatedBy);
  }

  @Test
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
    CompletableFuture<SwiftAdapter> future = swiftAdapterService.updateAdapterConfiguration(
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
    SwiftAdapter updatedAdapter = future.get();

    // Then
    assertThat(updatedAdapter.getEndpoint()).isEqualTo(newEndpoint);
    assertThat(updatedAdapter.getApiVersion()).isEqualTo(newApiVersion);
    assertThat(updatedAdapter.getTimeoutSeconds()).isEqualTo(newTimeoutSeconds);
    assertThat(updatedAdapter.getRetryAttempts()).isEqualTo(newRetryAttempts);
    assertThat(updatedAdapter.getEncryptionEnabled()).isEqualTo(newEncryptionEnabled);
    assertThat(updatedAdapter.getBatchSize()).isEqualTo(newBatchSize);
    assertThat(updatedAdapter.getProcessingWindowStart()).isEqualTo(newProcessingWindowStart);
    assertThat(updatedAdapter.getProcessingWindowEnd()).isEqualTo(newProcessingWindowEnd);
    assertThat(updatedAdapter.getUpdatedBy()).isEqualTo(updatedBy);
  }

  @Test
  void shouldGetAdapterById() throws Exception {
    // Given
    SwiftAdapter expectedAdapter = createTestAdapter();

    // When
    CompletableFuture<Optional<SwiftAdapter>> future = swiftAdapterService.getAdapter(expectedAdapter.getId());
    Optional<SwiftAdapter> actualAdapter = future.get();

    // Then
    assertThat(actualAdapter).isPresent();
    assertThat(actualAdapter.get().getId()).isEqualTo(expectedAdapter.getId());
  }

  @Test
  void shouldGetAdaptersByTenant() throws Exception {
    // Given
    createTestAdapter();
    createTestAdapterWithDifferentTenant();

    // When
    CompletableFuture<List<SwiftAdapter>> future = swiftAdapterService.getAdaptersByTenant(
        tenantContext.getTenantId(), tenantContext.getBusinessUnitId());
    List<SwiftAdapter> adapters = future.get();

    // Then
    assertThat(adapters).hasSize(1);
    assertThat(adapters.get(0).getTenantContext().getTenantId()).isEqualTo(tenantContext.getTenantId());
  }

  @Test
  void shouldGetActiveAdaptersByTenant() throws Exception {
    // Given
    SwiftAdapter adapter1 = createTestAdapter();
    SwiftAdapter adapter2 = createTestAdapter();
    swiftAdapterService.activateAdapter(adapter1.getId(), "admin").get();

    // When
    CompletableFuture<List<SwiftAdapter>> future = swiftAdapterService.getActiveAdaptersByTenant(
        tenantContext.getTenantId());
    List<SwiftAdapter> activeAdapters = future.get();

    // Then
    assertThat(activeAdapters).hasSize(1);
    assertThat(activeAdapters.get(0).getId()).isEqualTo(adapter1.getId());
    assertThat(activeAdapters.get(0).isActive()).isTrue();
  }

  @Test
  void shouldValidateAdapterConfiguration() throws Exception {
    // Given
    SwiftAdapter adapter = createTestAdapter();

    // When
    CompletableFuture<Boolean> future = swiftAdapterService.validateAdapterConfiguration(adapter.getId());
    Boolean isValid = future.get();

    // Then
    assertThat(isValid).isTrue();
  }

  @Test
  void shouldDeleteAdapter() throws Exception {
    // Given
    SwiftAdapter adapter = createTestAdapter();

    // When
    swiftAdapterService.deleteAdapter(adapter.getId());

    // Then
    CompletableFuture<Optional<SwiftAdapter>> future = swiftAdapterService.getAdapter(adapter.getId());
    Optional<SwiftAdapter> deletedAdapter = future.get();
    assertThat(deletedAdapter).isEmpty();
  }

  private SwiftAdapter createTestAdapter() throws Exception {
    String adapterName = "Test SWIFT Adapter";
    String endpoint = "https://swift.test.com/api";
    String createdBy = "test-user";

    CompletableFuture<SwiftAdapter> future = swiftAdapterService.createAdapter(
        ClearingAdapterId.generate(), tenantContext, adapterName, endpoint, createdBy);
    return future.get();
  }

  private SwiftAdapter createTestAdapterWithDifferentTenant() throws Exception {
    TenantContext differentTenant = TenantContext.of(
        "tenant-789",
        "Different Tenant",
        "business-unit-101",
        "Different Business Unit"
    );

    String adapterName = "Different Tenant Adapter";
    String endpoint = "https://swift.different.com/api";
    String createdBy = "different-user";

    CompletableFuture<SwiftAdapter> future = swiftAdapterService.createAdapter(
        ClearingAdapterId.generate(), differentTenant, adapterName, endpoint, createdBy);
    return future.get();
  }
}
