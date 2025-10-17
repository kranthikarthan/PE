package com.payments.bankservafricaadapter.integration;

import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.bankservafricaadapter.domain.BankservAfricaAdapter;
import com.payments.bankservafricaadapter.repository.BankservAfricaAdapterRepository;
import com.payments.bankservafricaadapter.service.BankservAfricaAdapterService;
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
 * Integration tests for BankservAfrica adapter using Testcontainers
 */
@SpringBootTest
@Testcontainers
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class BankservAfricaAdapterIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
      .withDatabaseName("bankservafrica_adapter_test")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private BankservAfricaAdapterService adapterService;

  @Autowired
  private BankservAfricaAdapterRepository adapterRepository;

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
  void shouldCreateBankservAfricaAdapter() throws Exception {
    // Given
    String adapterName = "Test BankservAfrica Adapter";
    ClearingNetwork network = ClearingNetwork.BANKSERVAFRICA;
    String endpoint = "https://bankservafrica.test.com/api";
    String createdBy = "test-user";

    // When
    CompletableFuture<BankservAfricaAdapter> future = adapterService.createAdapter(
        adapterId, tenantContext, adapterName, network, endpoint, createdBy);
    BankservAfricaAdapter adapter = future.get();

    // Then
    assertThat(adapter).isNotNull();
    assertThat(adapter.getId()).isEqualTo(adapterId);
    assertThat(adapter.getAdapterName()).isEqualTo(adapterName);
    assertThat(adapter.getNetwork()).isEqualTo(network);
    assertThat(adapter.getEndpoint()).isEqualTo(endpoint);
    assertThat(adapter.getTenantContext()).isEqualTo(tenantContext);
    assertThat(adapter.getCreatedBy()).isEqualTo(createdBy);
    assertThat(adapter.isActive()).isFalse(); // Initially inactive
  }

  @Test
  void shouldActivateBankservAfricaAdapter() throws Exception {
    // Given
    BankservAfricaAdapter adapter = createTestAdapter();
    String activatedBy = "admin-user";

    // When
    CompletableFuture<BankservAfricaAdapter> future = adapterService.activateAdapter(
        adapter.getId(), activatedBy);
    BankservAfricaAdapter activatedAdapter = future.get();

    // Then
    assertThat(activatedAdapter.isActive()).isTrue();
    assertThat(activatedAdapter.getUpdatedBy()).isEqualTo(activatedBy);
  }

  @Test
  void shouldDeactivateBankservAfricaAdapter() throws Exception {
    // Given
    BankservAfricaAdapter adapter = createTestAdapter();
    adapterService.activateAdapter(adapter.getId(), "admin").get();
    String deactivatedBy = "admin-user";
    String reason = "Maintenance";

    // When
    CompletableFuture<BankservAfricaAdapter> future = adapterService.deactivateAdapter(
        adapter.getId(), reason, deactivatedBy);
    BankservAfricaAdapter deactivatedAdapter = future.get();

    // Then
    assertThat(deactivatedAdapter.isActive()).isFalse();
    assertThat(deactivatedAdapter.getUpdatedBy()).isEqualTo(deactivatedBy);
  }

  @Test
  void shouldUpdateAdapterConfiguration() throws Exception {
    // Given
    BankservAfricaAdapter adapter = createTestAdapter();
    String newEndpoint = "https://bankservafrica.new.com/api";
    String newApiVersion = "2.0";
    Integer newTimeoutSeconds = 30;
    Integer newRetryAttempts = 5;
    Boolean newEncryptionEnabled = true;
    String newCertificatePath = "/path/to/cert.pem";
    String newCertificatePassword = "password123";
    String updatedBy = "admin-user";

    // When
    CompletableFuture<BankservAfricaAdapter> future = adapterService.updateAdapterConfiguration(
        adapter.getId(),
        newEndpoint,
        newApiVersion,
        newTimeoutSeconds,
        newRetryAttempts,
        newEncryptionEnabled,
        newCertificatePath,
        newCertificatePassword,
        updatedBy);
    BankservAfricaAdapter updatedAdapter = future.get();

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
    BankservAfricaAdapter expectedAdapter = createTestAdapter();

    // When
    CompletableFuture<Optional<BankservAfricaAdapter>> future = adapterService.getAdapter(expectedAdapter.getId());
    Optional<BankservAfricaAdapter> actualAdapter = future.get();

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
    CompletableFuture<List<BankservAfricaAdapter>> future = adapterService.getAdaptersByTenant(
        tenantContext.getTenantId(), tenantContext.getBusinessUnitId());
    List<BankservAfricaAdapter> adapters = future.get();

    // Then
    assertThat(adapters).hasSize(1);
    assertThat(adapters.get(0).getTenantContext().getTenantId()).isEqualTo(tenantContext.getTenantId());
  }

  @Test
  void shouldGetActiveAdaptersByTenant() throws Exception {
    // Given
    BankservAfricaAdapter adapter1 = createTestAdapter();
    BankservAfricaAdapter adapter2 = createTestAdapter();
    adapterService.activateAdapter(adapter1.getId(), "admin").get();

    // When
    CompletableFuture<List<BankservAfricaAdapter>> future = adapterService.getActiveAdaptersByTenant(
        tenantContext.getTenantId());
    List<BankservAfricaAdapter> activeAdapters = future.get();

    // Then
    assertThat(activeAdapters).hasSize(1);
    assertThat(activeAdapters.get(0).getId()).isEqualTo(adapter1.getId());
    assertThat(activeAdapters.get(0).isActive()).isTrue();
  }

  @Test
  void shouldValidateAdapterConfiguration() throws Exception {
    // Given
    BankservAfricaAdapter adapter = createTestAdapter();

    // When
    CompletableFuture<Boolean> future = adapterService.validateAdapterConfiguration(adapter.getId());
    Boolean isValid = future.get();

    // Then
    assertThat(isValid).isTrue();
  }

  @Test
  void shouldDeleteAdapter() throws Exception {
    // Given
    BankservAfricaAdapter adapter = createTestAdapter();

    // When
    adapterService.deleteAdapter(adapter.getId());

    // Then
    CompletableFuture<Optional<BankservAfricaAdapter>> future = adapterService.getAdapter(adapter.getId());
    Optional<BankservAfricaAdapter> deletedAdapter = future.get();
    assertThat(deletedAdapter).isEmpty();
  }

  private BankservAfricaAdapter createTestAdapter() throws Exception {
    String adapterName = "Test BankservAfrica Adapter";
    ClearingNetwork network = ClearingNetwork.BANKSERVAFRICA;
    String endpoint = "https://bankservafrica.test.com/api";
    String createdBy = "test-user";

    CompletableFuture<BankservAfricaAdapter> future = adapterService.createAdapter(
        ClearingAdapterId.generate(), tenantContext, adapterName, network, endpoint, createdBy);
    return future.get();
  }

  private BankservAfricaAdapter createTestAdapterWithDifferentTenant() throws Exception {
    TenantContext differentTenant = TenantContext.of(
        "tenant-789",
        "Different Tenant",
        "business-unit-101",
        "Different Business Unit"
    );

    String adapterName = "Different Tenant Adapter";
    ClearingNetwork network = ClearingNetwork.BANKSERVAFRICA;
    String endpoint = "https://bankservafrica.different.com/api";
    String createdBy = "different-user";

    CompletableFuture<BankservAfricaAdapter> future = adapterService.createAdapter(
        ClearingAdapterId.generate(), differentTenant, adapterName, network, endpoint, createdBy);
    return future.get();
  }
}
