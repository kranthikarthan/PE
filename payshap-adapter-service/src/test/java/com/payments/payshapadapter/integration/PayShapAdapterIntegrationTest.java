package com.payments.payshapadapter.integration;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.payshapadapter.domain.PayShapAdapter;
import com.payments.payshapadapter.repository.PayShapAdapterRepository;
import com.payments.payshapadapter.service.PayShapAdapterService;
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
 * Integration tests for PayShap adapter using Testcontainers
 */
@SpringBootTest
@Testcontainers
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class PayShapAdapterIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
      .withDatabaseName("payshap_adapter_test")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private PayShapAdapterService payShapAdapterService;

  @Autowired
  private PayShapAdapterRepository payShapAdapterRepository;

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
  void shouldCreatePayShapAdapter() throws Exception {
    // Given
    String adapterName = "Test PayShap Adapter";
    String endpoint = "https://payshap.test.com/api";
    String createdBy = "test-user";

    // When
    CompletableFuture<PayShapAdapter> future = payShapAdapterService.createAdapter(
        adapterId, tenantContext, adapterName, endpoint, createdBy);
    PayShapAdapter adapter = future.get();

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
  void shouldActivatePayShapAdapter() throws Exception {
    // Given
    PayShapAdapter adapter = createTestAdapter();
    String activatedBy = "admin-user";

    // When
    CompletableFuture<PayShapAdapter> future = payShapAdapterService.activateAdapter(
        adapter.getId(), activatedBy);
    PayShapAdapter activatedAdapter = future.get();

    // Then
    assertThat(activatedAdapter.isActive()).isTrue();
    assertThat(activatedAdapter.getUpdatedBy()).isEqualTo(activatedBy);
  }

  @Test
  void shouldDeactivatePayShapAdapter() throws Exception {
    // Given
    PayShapAdapter adapter = createTestAdapter();
    payShapAdapterService.activateAdapter(adapter.getId(), "admin").get();
    String deactivatedBy = "admin-user";
    String reason = "Maintenance";

    // When
    CompletableFuture<PayShapAdapter> future = payShapAdapterService.deactivateAdapter(
        adapter.getId(), reason, deactivatedBy);
    PayShapAdapter deactivatedAdapter = future.get();

    // Then
    assertThat(deactivatedAdapter.isActive()).isFalse();
    assertThat(deactivatedAdapter.getUpdatedBy()).isEqualTo(deactivatedBy);
  }

  @Test
  void shouldUpdateAdapterConfiguration() throws Exception {
    // Given
    PayShapAdapter adapter = createTestAdapter();
    String newEndpoint = "https://payshap.new.com/api";
    String newApiVersion = "2.0";
    Integer newTimeoutSeconds = 30;
    Integer newRetryAttempts = 5;
    Boolean newEncryptionEnabled = true;
    String newCertificatePath = "/path/to/cert.pem";
    String newCertificatePassword = "password123";
    String updatedBy = "admin-user";

    // When
    CompletableFuture<PayShapAdapter> future = payShapAdapterService.updateAdapterConfiguration(
        adapter.getId(),
        newEndpoint,
        newApiVersion,
        newTimeoutSeconds,
        newRetryAttempts,
        newEncryptionEnabled,
        newCertificatePath,
        newCertificatePassword,
        updatedBy);
    PayShapAdapter updatedAdapter = future.get();

    // Then
    assertThat(updatedAdapter.getEndpoint()).isEqualTo(newEndpoint);
    assertThat(updatedAdapter.getApiVersion()).isEqualTo(newApiVersion);
    assertThat(updatedAdapter.getTimeoutSeconds()).isEqualTo(newTimeoutSeconds);
    assertThat(updatedAdapter.getRetryAttempts()).isEqualTo(newRetryAttempts);
    assertThat(updatedAdapter.getEncryptionEnabled()).isEqualTo(newEncryptionEnabled);
    assertThat(updatedAdapter.getCertificatePath()).isEqualTo(newCertificatePath);
    assertThat(updatedAdapter.getCertificatePassword()).isEqualTo(newCertificatePassword);
    assertThat(updatedAdapter.getUpdatedBy()).isEqualTo(updatedBy);
  }

  @Test
  void shouldGetAdapterById() throws Exception {
    // Given
    PayShapAdapter expectedAdapter = createTestAdapter();

    // When
    CompletableFuture<Optional<PayShapAdapter>> future = payShapAdapterService.getAdapter(expectedAdapter.getId());
    Optional<PayShapAdapter> actualAdapter = future.get();

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
    CompletableFuture<List<PayShapAdapter>> future = payShapAdapterService.getAdaptersByTenant(
        tenantContext.getTenantId(), tenantContext.getBusinessUnitId());
    List<PayShapAdapter> adapters = future.get();

    // Then
    assertThat(adapters).hasSize(1);
    assertThat(adapters.get(0).getTenantContext().getTenantId()).isEqualTo(tenantContext.getTenantId());
  }

  @Test
  void shouldGetActiveAdaptersByTenant() throws Exception {
    // Given
    PayShapAdapter adapter1 = createTestAdapter();
    PayShapAdapter adapter2 = createTestAdapter();
    payShapAdapterService.activateAdapter(adapter1.getId(), "admin").get();

    // When
    CompletableFuture<List<PayShapAdapter>> future = payShapAdapterService.getActiveAdaptersByTenant(
        tenantContext.getTenantId());
    List<PayShapAdapter> activeAdapters = future.get();

    // Then
    assertThat(activeAdapters).hasSize(1);
    assertThat(activeAdapters.get(0).getId()).isEqualTo(adapter1.getId());
    assertThat(activeAdapters.get(0).isActive()).isTrue();
  }

  @Test
  void shouldValidateAdapterConfiguration() throws Exception {
    // Given
    PayShapAdapter adapter = createTestAdapter();

    // When
    CompletableFuture<Boolean> future = payShapAdapterService.validateAdapterConfiguration(adapter.getId());
    Boolean isValid = future.get();

    // Then
    assertThat(isValid).isTrue();
  }

  @Test
  void shouldDeleteAdapter() throws Exception {
    // Given
    PayShapAdapter adapter = createTestAdapter();

    // When
    payShapAdapterService.deleteAdapter(adapter.getId());

    // Then
    CompletableFuture<Optional<PayShapAdapter>> future = payShapAdapterService.getAdapter(adapter.getId());
    Optional<PayShapAdapter> deletedAdapter = future.get();
    assertThat(deletedAdapter).isEmpty();
  }

  private PayShapAdapter createTestAdapter() throws Exception {
    String adapterName = "Test PayShap Adapter";
    String endpoint = "https://payshap.test.com/api";
    String createdBy = "test-user";

    CompletableFuture<PayShapAdapter> future = payShapAdapterService.createAdapter(
        ClearingAdapterId.generate(), tenantContext, adapterName, endpoint, createdBy);
    return future.get();
  }

  private PayShapAdapter createTestAdapterWithDifferentTenant() throws Exception {
    TenantContext differentTenant = TenantContext.of(
        "tenant-789",
        "Different Tenant",
        "business-unit-101",
        "Different Business Unit"
    );

    String adapterName = "Different Tenant Adapter";
    String endpoint = "https://payshap.different.com/api";
    String createdBy = "different-user";

    CompletableFuture<PayShapAdapter> future = payShapAdapterService.createAdapter(
        ClearingAdapterId.generate(), differentTenant, adapterName, endpoint, createdBy);
    return future.get();
  }
}
