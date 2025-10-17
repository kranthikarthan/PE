package com.payments.bankservafricaadapter.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.bankservafricaadapter.domain.BankservAfricaAdapter;
import com.payments.bankservafricaadapter.repository.BankservAfricaAdapterRepository;
import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * BankservAfrica Domain Model Validation Test
 *
 * <p>Comprehensive validation tests to ensure BankservAfrica adapter properly aligns with shared
 * clearing-adapter domain model
 */
@DataJpaTest
@ActiveProfiles("test")
public class BankservAfricaDomainModelValidationTest {

  @Autowired private BankservAfricaAdapterRepository bankservAfricaAdapterRepository;

  private BankservAfricaAdapter testAdapter;

  @BeforeEach
  void setUp() {
    testAdapter =
        BankservAfricaAdapter.builder()
            .id(ClearingAdapterId.of("bankservafrica-test-001"))
            .name("BankservAfrica Test Adapter")
            .description("Test BankservAfrica adapter for domain model validation")
            .status(AdapterOperationalStatus.ACTIVE)
            .network(ClearingNetwork.BANKSERVAFRICA)
            .endpoint("https://test.bankservafrica.co.za/api/v1")
            .apiVersion("1.0")
            .timeoutSeconds(30)
            .retryAttempts(3)
            .encryptionEnabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
  }

  @Test
  void shouldCreateBankservAfricaAdapterWithValidDomainModel() {
    // When
    BankservAfricaAdapter savedAdapter = bankservAfricaAdapterRepository.save(testAdapter);

    // Then
    assertThat(savedAdapter).isNotNull();
    assertThat(savedAdapter.getId()).isEqualTo(ClearingAdapterId.of("bankservafrica-test-001"));
    assertThat(savedAdapter.getName()).isEqualTo("BankservAfrica Test Adapter");
    assertThat(savedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
    assertThat(savedAdapter.getNetwork()).isEqualTo(ClearingNetwork.BANKSERVAFRICA);
    assertThat(savedAdapter.getEndpoint()).isEqualTo("https://test.bankservafrica.co.za/api/v1");
    assertThat(savedAdapter.getApiVersion()).isEqualTo("1.0");
    assertThat(savedAdapter.getTimeoutSeconds()).isEqualTo(30);
    assertThat(savedAdapter.getRetryAttempts()).isEqualTo(3);
    assertThat(savedAdapter.isEncryptionEnabled()).isTrue();
  }

  @Test
  void shouldFindBankservAfricaAdapterById() {
    // Given
    bankservAfricaAdapterRepository.save(testAdapter);

    // When
    Optional<BankservAfricaAdapter> foundAdapter =
        bankservAfricaAdapterRepository.findById(ClearingAdapterId.of("bankservafrica-test-001"));

    // Then
    assertThat(foundAdapter).isPresent();
    assertThat(foundAdapter.get().getName()).isEqualTo("BankservAfrica Test Adapter");
    assertThat(foundAdapter.get().getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
  }

  @Test
  void shouldFindBankservAfricaAdaptersByStatus() {
    // Given
    bankservAfricaAdapterRepository.save(testAdapter);

    // When
    List<BankservAfricaAdapter> activeAdapters =
        bankservAfricaAdapterRepository.findByStatus(AdapterOperationalStatus.ACTIVE);

    // Then
    assertThat(activeAdapters).hasSize(1);
    assertThat(activeAdapters.get(0).getName()).isEqualTo("BankservAfrica Test Adapter");
  }

  @Test
  void shouldUpdateBankservAfricaAdapterStatus() {
    // Given
    BankservAfricaAdapter savedAdapter = bankservAfricaAdapterRepository.save(testAdapter);

    // When
    savedAdapter.setStatus(AdapterOperationalStatus.INACTIVE);
    savedAdapter.setUpdatedAt(Instant.now());
    BankservAfricaAdapter updatedAdapter = bankservAfricaAdapterRepository.save(savedAdapter);

    // Then
    assertThat(updatedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.INACTIVE);
  }

  @Test
  void shouldValidateBankservAfricaAdapterDomainFields() {
    // Given
    BankservAfricaAdapter savedAdapter = bankservAfricaAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getId()).isInstanceOf(ClearingAdapterId.class);
    assertThat(savedAdapter.getStatus()).isInstanceOf(AdapterOperationalStatus.class);
    assertThat(savedAdapter.getNetwork()).isInstanceOf(ClearingNetwork.class);
    assertThat(savedAdapter.getTimeoutSeconds()).isInstanceOf(Integer.class);
    assertThat(savedAdapter.getRetryAttempts()).isInstanceOf(Integer.class);
    assertThat(savedAdapter.isEncryptionEnabled()).isInstanceOf(Boolean.class);
  }

  @Test
  void shouldValidateBankservAfricaAdapterBusinessRules() {
    // Given
    BankservAfricaAdapter savedAdapter = bankservAfricaAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getTimeoutSeconds()).isGreaterThan(0);
    assertThat(savedAdapter.getRetryAttempts()).isGreaterThanOrEqualTo(0);
    assertThat(savedAdapter.getEndpoint()).isNotBlank();
    assertThat(savedAdapter.getApiVersion()).isNotBlank();
  }

  @Test
  void shouldCountBankservAfricaAdaptersByStatus() {
    // Given
    bankservAfricaAdapterRepository.save(testAdapter);

    // When
    long activeCount =
        bankservAfricaAdapterRepository.countByStatus(AdapterOperationalStatus.ACTIVE);

    // Then
    assertThat(activeCount).isEqualTo(1);
  }

  @Test
  void shouldValidateBankservAfricaAdapterCollections() {
    // Given
    BankservAfricaAdapter savedAdapter = bankservAfricaAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getRoutes()).isNotNull();
    assertThat(savedAdapter.getMessageLogs()).isNotNull();
  }

  @Test
  void shouldValidateBankservAfricaAdapterDomainEvents() {
    // Given
    BankservAfricaAdapter savedAdapter = bankservAfricaAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getCreatedAt()).isNotNull();
    assertThat(savedAdapter.getUpdatedAt()).isNotNull();
    assertThat(savedAdapter.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    assertThat(savedAdapter.getUpdatedAt()).isBeforeOrEqualTo(Instant.now());
  }
}
