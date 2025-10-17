package com.payments.samosadapter.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.samosadapter.domain.SamosAdapter;
import com.payments.samosadapter.repository.SamosAdapterRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * SAMOS Domain Model Validation Test
 *
 * <p>Comprehensive validation tests to ensure SAMOS adapter properly aligns with shared
 * clearing-adapter domain model
 */
@DataJpaTest
@ActiveProfiles("test")
public class SamosDomainModelValidationTest {

  @Autowired private SamosAdapterRepository samosAdapterRepository;

  private SamosAdapter testAdapter;

  @BeforeEach
  void setUp() {
    testAdapter =
        SamosAdapter.create(
            ClearingAdapterId.of("samos-test-001"),
            com.payments.domain.shared.TenantContext.of(
                "test-tenant", "Test Tenant", "test-business-unit", "Test Business Unit"),
            "SAMOS Test Adapter",
            "https://test.samos.co.za/api/v1",
            "test-user");
  }

  @Test
  void shouldCreateSamosAdapterWithValidDomainModel() {
    // When
    SamosAdapter savedAdapter = samosAdapterRepository.save(testAdapter);

    // Then
    assertThat(savedAdapter).isNotNull();
    assertThat(savedAdapter.getId()).isEqualTo(ClearingAdapterId.of("samos-test-001"));
    assertThat(savedAdapter.getAdapterName()).isEqualTo("SAMOS Test Adapter");
    assertThat(savedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
    assertThat(savedAdapter.getNetwork()).isEqualTo(ClearingNetwork.SAMOS);
    assertThat(savedAdapter.getEndpoint()).isEqualTo("https://test.samos.co.za/api/v1");
    assertThat(savedAdapter.getApiVersion()).isEqualTo("v1");
    assertThat(savedAdapter.getTimeoutSeconds()).isEqualTo(30);
    assertThat(savedAdapter.getRetryAttempts()).isEqualTo(3);
    assertThat(savedAdapter.getEncryptionEnabled()).isTrue();
  }

  @Test
  void shouldFindSamosAdapterById() {
    // Given
    samosAdapterRepository.save(testAdapter);

    // When
    Optional<SamosAdapter> foundAdapter =
        samosAdapterRepository.findById(ClearingAdapterId.of("samos-test-001"));

    // Then
    assertThat(foundAdapter).isPresent();
    assertThat(foundAdapter.get().getAdapterName()).isEqualTo("SAMOS Test Adapter");
    assertThat(foundAdapter.get().getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
  }

  @Test
  void shouldFindSamosAdaptersByStatus() {
    // Given
    samosAdapterRepository.save(testAdapter);

    // When
    List<SamosAdapter> activeAdapters =
        samosAdapterRepository.findByStatus(AdapterOperationalStatus.ACTIVE);

    // Then
    assertThat(activeAdapters).hasSize(1);
    assertThat(activeAdapters.get(0).getAdapterName()).isEqualTo("SAMOS Test Adapter");
  }

  @Test
  void shouldUpdateSamosAdapterStatus() {
    // Given
    SamosAdapter savedAdapter = samosAdapterRepository.save(testAdapter);

    // When
    savedAdapter.deactivate("Test deactivation", "test-user");
    SamosAdapter updatedAdapter = samosAdapterRepository.save(savedAdapter);

    // Then
    assertThat(updatedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.INACTIVE);
  }

  @Test
  void shouldValidateSamosAdapterDomainFields() {
    // Given
    SamosAdapter savedAdapter = samosAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getId()).isInstanceOf(ClearingAdapterId.class);
    assertThat(savedAdapter.getStatus()).isInstanceOf(AdapterOperationalStatus.class);
    assertThat(savedAdapter.getNetwork()).isInstanceOf(ClearingNetwork.class);
    assertThat(savedAdapter.getTimeoutSeconds()).isInstanceOf(Integer.class);
    assertThat(savedAdapter.getRetryAttempts()).isInstanceOf(Integer.class);
    assertThat(savedAdapter.getEncryptionEnabled()).isInstanceOf(Boolean.class);
  }

  @Test
  void shouldValidateSamosAdapterBusinessRules() {
    // Given
    SamosAdapter savedAdapter = samosAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getTimeoutSeconds()).isGreaterThan(0);
    assertThat(savedAdapter.getRetryAttempts()).isGreaterThanOrEqualTo(0);
    assertThat(savedAdapter.getEndpoint()).isNotBlank();
    assertThat(savedAdapter.getApiVersion()).isNotBlank();
  }

  @Test
  void shouldCountSamosAdaptersByStatus() {
    // Given
    samosAdapterRepository.save(testAdapter);

    // When
    long activeCount = samosAdapterRepository.countByStatus(AdapterOperationalStatus.ACTIVE);

    // Then
    assertThat(activeCount).isEqualTo(1);
  }

  @Test
  void shouldValidateSamosAdapterCollections() {
    // Given
    SamosAdapter savedAdapter = samosAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getRoutes()).isNotNull();
    assertThat(savedAdapter.getMessageLogs()).isNotNull();
  }

  @Test
  void shouldValidateSamosAdapterDomainEvents() {
    // Given
    SamosAdapter savedAdapter = samosAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getCreatedAt()).isNotNull();
    assertThat(savedAdapter.getUpdatedAt()).isNotNull();
    assertThat(savedAdapter.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    assertThat(savedAdapter.getUpdatedAt()).isBeforeOrEqualTo(Instant.now());
  }
}
