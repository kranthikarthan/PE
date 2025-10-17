package com.payments.rtcadapter.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.rtcadapter.domain.RtcAdapter;
import com.payments.rtcadapter.repository.RtcAdapterRepository;
import com.payments.rtcadapter.service.RtcAdapterService;
import java.util.concurrent.CompletableFuture;
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

/** Integration tests for RTC adapter using Testcontainers */
@SpringBootTest
@Testcontainers
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class RtcAdapterIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("rtc_adapter_test")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private RtcAdapterService rtcAdapterService;

  @Autowired private RtcAdapterRepository rtcAdapterRepository;

  private TenantContext tenantContext;
  private ClearingAdapterId adapterId;

  @BeforeEach
  void setUp() {
    tenantContext =
        TenantContext.of("tenant-123", "Test Tenant", "business-unit-456", "Test Business Unit");
    adapterId = ClearingAdapterId.generate();
  }

  @Test
  void shouldCreateRtcAdapter() throws Exception {
    // Given
    String adapterName = "Test RTC Adapter";
    String endpoint = "https://rtc.test.com/api";
    String createdBy = "test-user";

    // When
    CompletableFuture<RtcAdapter> future =
        rtcAdapterService.createAdapter(adapterId, tenantContext, adapterName, endpoint, createdBy);
    RtcAdapter adapter = future.get();

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
  void shouldActivateRtcAdapter() throws Exception {
    // Given
    RtcAdapter adapter = createTestAdapter();
    String activatedBy = "admin-user";

    // When
    CompletableFuture<RtcAdapter> future =
        rtcAdapterService.activateAdapter(adapter.getId(), activatedBy);
    RtcAdapter activatedAdapter = future.get();

    // Then
    assertThat(activatedAdapter.isActive()).isTrue();
    assertThat(activatedAdapter.getUpdatedBy()).isEqualTo(activatedBy);
  }

  @Test
  void shouldDeactivateRtcAdapter() throws Exception {
    // Given
    RtcAdapter adapter = createTestAdapter();
    rtcAdapterService.activateAdapter(adapter.getId(), "admin").get();
    String deactivatedBy = "admin-user";
    String reason = "Maintenance";

    // When
    CompletableFuture<RtcAdapter> future =
        rtcAdapterService.deactivateAdapter(adapter.getId(), reason, deactivatedBy);
    RtcAdapter deactivatedAdapter = future.get();

    // Then
    assertThat(deactivatedAdapter.isActive()).isFalse();
    assertThat(deactivatedAdapter.getUpdatedBy()).isEqualTo(deactivatedBy);
  }

  @Test
  void shouldUpdateAdapterConfiguration() throws Exception {
    // Given
    RtcAdapter adapter = createTestAdapter();
    String newEndpoint = "https://rtc.new.com/api";
    String newApiVersion = "2.0";
    Integer newTimeoutSeconds = 30;
    Integer newRetryAttempts = 5;
    Boolean newEncryptionEnabled = true;
    String newCertificatePath = "/path/to/cert.pem";
    String newCertificatePassword = "password123";
    String updatedBy = "admin-user";

    // When
    CompletableFuture<RtcAdapter> future =
        rtcAdapterService.updateAdapterConfiguration(
            adapter.getId(),
            newEndpoint,
            newApiVersion,
            newTimeoutSeconds,
            newRetryAttempts,
            newEncryptionEnabled,
            newCertificatePath,
            newCertificatePassword,
            updatedBy);
    RtcAdapter updatedAdapter = future.get();

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
    RtcAdapter expectedAdapter = createTestAdapter();

    // When
    CompletableFuture<Optional<RtcAdapter>> future =
        rtcAdapterService.getAdapter(expectedAdapter.getId());
    Optional<RtcAdapter> actualAdapter = future.get();

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
    CompletableFuture<List<RtcAdapter>> future =
        rtcAdapterService.getAdaptersByTenant(
            tenantContext.getTenantId(), tenantContext.getBusinessUnitId());
    List<RtcAdapter> adapters = future.get();

    // Then
    assertThat(adapters).hasSize(1);
    assertThat(adapters.get(0).getTenantContext().getTenantId())
        .isEqualTo(tenantContext.getTenantId());
  }

  @Test
  void shouldGetActiveAdaptersByTenant() throws Exception {
    // Given
    RtcAdapter adapter1 = createTestAdapter();
    RtcAdapter adapter2 = createTestAdapter();
    rtcAdapterService.activateAdapter(adapter1.getId(), "admin").get();

    // When
    CompletableFuture<List<RtcAdapter>> future =
        rtcAdapterService.getActiveAdaptersByTenant(tenantContext.getTenantId());
    List<RtcAdapter> activeAdapters = future.get();

    // Then
    assertThat(activeAdapters).hasSize(1);
    assertThat(activeAdapters.get(0).getId()).isEqualTo(adapter1.getId());
    assertThat(activeAdapters.get(0).isActive()).isTrue();
  }

  @Test
  void shouldValidateAdapterConfiguration() throws Exception {
    // Given
    RtcAdapter adapter = createTestAdapter();

    // When
    CompletableFuture<Boolean> future =
        rtcAdapterService.validateAdapterConfiguration(adapter.getId());
    Boolean isValid = future.get();

    // Then
    assertThat(isValid).isTrue();
  }

  @Test
  void shouldDeleteAdapter() throws Exception {
    // Given
    RtcAdapter adapter = createTestAdapter();

    // When
    rtcAdapterService.deleteAdapter(adapter.getId());

    // Then
    CompletableFuture<Optional<RtcAdapter>> future = rtcAdapterService.getAdapter(adapter.getId());
    Optional<RtcAdapter> deletedAdapter = future.get();
    assertThat(deletedAdapter).isEmpty();
  }

  private RtcAdapter createTestAdapter() throws Exception {
    String adapterName = "Test RTC Adapter";
    String endpoint = "https://rtc.test.com/api";
    String createdBy = "test-user";

    CompletableFuture<RtcAdapter> future =
        rtcAdapterService.createAdapter(
            ClearingAdapterId.generate(), tenantContext, adapterName, endpoint, createdBy);
    return future.get();
  }

  private RtcAdapter createTestAdapterWithDifferentTenant() throws Exception {
    TenantContext differentTenant =
        TenantContext.of(
            "tenant-789", "Different Tenant", "business-unit-101", "Different Business Unit");

    String adapterName = "Different Tenant Adapter";
    String endpoint = "https://rtc.different.com/api";
    String createdBy = "different-user";

    CompletableFuture<RtcAdapter> future =
        rtcAdapterService.createAdapter(
            ClearingAdapterId.generate(), differentTenant, adapterName, endpoint, createdBy);
    return future.get();
  }
}
