package com.payments.payshapadapter.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.payshapadapter.domain.PayShapAdapter;
import com.payments.payshapadapter.repository.PayShapAdapterRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * PayShap Domain Model Validation Test
 *
 * <p>Comprehensive validation tests to ensure PayShap adapter properly aligns with shared
 * clearing-adapter domain model
 */
@DataJpaTest
@ActiveProfiles("test")
public class PayShapDomainModelValidationTest {

  @Autowired private PayShapAdapterRepository payShapAdapterRepository;

  private PayShapAdapter testAdapter;

  @BeforeEach
  void setUp() {
    testAdapter =
        PayShapAdapter.builder()
            .id(ClearingAdapterId.of("payshap-test-001"))
            .name("PayShap Test Adapter")
            .description("Test PayShap adapter for domain model validation")
            .status(AdapterOperationalStatus.ACTIVE)
            .network(ClearingNetwork.PAYSHAP)
            .endpoint("https://test.payshap.co.za/api/v1")
            .apiVersion("1.0")
            .timeoutSeconds(30)
            .retryAttempts(3)
            .encryptionEnabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
  }

  @Test
  void shouldCreatePayShapAdapterWithValidDomainModel() {
    // When
    PayShapAdapter savedAdapter = payShapAdapterRepository.save(testAdapter);

    // Then
    assertThat(savedAdapter).isNotNull();
    assertThat(savedAdapter.getId()).isEqualTo(ClearingAdapterId.of("payshap-test-001"));
    assertThat(savedAdapter.getName()).isEqualTo("PayShap Test Adapter");
    assertThat(savedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
    assertThat(savedAdapter.getNetwork()).isEqualTo(ClearingNetwork.PAYSHAP);
    assertThat(savedAdapter.getEndpoint()).isEqualTo("https://test.payshap.co.za/api/v1");
    assertThat(savedAdapter.getApiVersion()).isEqualTo("1.0");
    assertThat(savedAdapter.getTimeoutSeconds()).isEqualTo(30);
    assertThat(savedAdapter.getRetryAttempts()).isEqualTo(3);
    assertThat(savedAdapter.isEncryptionEnabled()).isTrue();
  }

  @Test
  void shouldFindPayShapAdapterById() {
    // Given
    payShapAdapterRepository.save(testAdapter);

    // When
    Optional<PayShapAdapter> foundAdapter =
        payShapAdapterRepository.findById(ClearingAdapterId.of("payshap-test-001"));

    // Then
    assertThat(foundAdapter).isPresent();
    assertThat(foundAdapter.get().getName()).isEqualTo("PayShap Test Adapter");
    assertThat(foundAdapter.get().getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
  }

  @Test
  void shouldFindPayShapAdaptersByStatus() {
    // Given
    payShapAdapterRepository.save(testAdapter);

    // When
    List<PayShapAdapter> activeAdapters =
        payShapAdapterRepository.findByStatus(AdapterOperationalStatus.ACTIVE);

    // Then
    assertThat(activeAdapters).hasSize(1);
    assertThat(activeAdapters.get(0).getName()).isEqualTo("PayShap Test Adapter");
  }

  @Test
  void shouldUpdatePayShapAdapterStatus() {
    // Given
    PayShapAdapter savedAdapter = payShapAdapterRepository.save(testAdapter);

    // When
    savedAdapter.setStatus(AdapterOperationalStatus.INACTIVE);
    savedAdapter.setUpdatedAt(Instant.now());
    PayShapAdapter updatedAdapter = payShapAdapterRepository.save(savedAdapter);

    // Then
    assertThat(updatedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.INACTIVE);
  }

  @Test
  void shouldValidatePayShapAdapterDomainFields() {
    // Given
    PayShapAdapter savedAdapter = payShapAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getId()).isInstanceOf(ClearingAdapterId.class);
    assertThat(savedAdapter.getStatus()).isInstanceOf(AdapterOperationalStatus.class);
    assertThat(savedAdapter.getNetwork()).isInstanceOf(ClearingNetwork.class);
    assertThat(savedAdapter.getTimeoutSeconds()).isInstanceOf(Integer.class);
    assertThat(savedAdapter.getRetryAttempts()).isInstanceOf(Integer.class);
    assertThat(savedAdapter.isEncryptionEnabled()).isInstanceOf(Boolean.class);
  }

  @Test
  void shouldValidatePayShapAdapterBusinessRules() {
    // Given
    PayShapAdapter savedAdapter = payShapAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getTimeoutSeconds()).isGreaterThan(0);
    assertThat(savedAdapter.getRetryAttempts()).isGreaterThanOrEqualTo(0);
    assertThat(savedAdapter.getEndpoint()).isNotBlank();
    assertThat(savedAdapter.getApiVersion()).isNotBlank();
  }

  @Test
  void shouldCountPayShapAdaptersByStatus() {
    // Given
    payShapAdapterRepository.save(testAdapter);

    // When
    long activeCount = payShapAdapterRepository.countByStatus(AdapterOperationalStatus.ACTIVE);

    // Then
    assertThat(activeCount).isEqualTo(1);
  }

  @Test
  void shouldValidatePayShapAdapterCollections() {
    // Given
    PayShapAdapter savedAdapter = payShapAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getRoutes()).isNotNull();
    assertThat(savedAdapter.getMessageLogs()).isNotNull();
  }

  @Test
  void shouldValidatePayShapAdapterDomainEvents() {
    // Given
    PayShapAdapter savedAdapter = payShapAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getCreatedAt()).isNotNull();
    assertThat(savedAdapter.getUpdatedAt()).isNotNull();
    assertThat(savedAdapter.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    assertThat(savedAdapter.getUpdatedAt()).isBeforeOrEqualTo(Instant.now());
  }
}
