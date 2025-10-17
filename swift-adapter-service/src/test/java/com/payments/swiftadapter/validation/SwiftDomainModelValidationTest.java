package com.payments.swiftadapter.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.swiftadapter.domain.SwiftAdapter;
import com.payments.swiftadapter.repository.SwiftAdapterRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * SWIFT Domain Model Validation Test
 *
 * <p>Comprehensive validation tests to ensure SWIFT adapter properly aligns with shared
 * clearing-adapter domain model
 */
@DataJpaTest
@ActiveProfiles("test")
public class SwiftDomainModelValidationTest {

  @Autowired private SwiftAdapterRepository swiftAdapterRepository;

  private SwiftAdapter testAdapter;

  @BeforeEach
  void setUp() {
    testAdapter =
        SwiftAdapter.builder()
            .id(ClearingAdapterId.of("swift-test-001"))
            .adapterName("SWIFT Test Adapter")
            .status(AdapterOperationalStatus.ACTIVE)
            .network(ClearingNetwork.SWIFT)
            .endpoint("https://test.swift.com/api/v1")
            .apiVersion("1.0")
            .timeoutSeconds(30)
            .retryAttempts(3)
            .encryptionEnabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
  }

  @Test
  void shouldCreateSwiftAdapterWithValidDomainModel() {
    // When
    SwiftAdapter savedAdapter = swiftAdapterRepository.save(testAdapter);

    // Then
    assertThat(savedAdapter).isNotNull();
    assertThat(savedAdapter.getId()).isEqualTo(ClearingAdapterId.of("swift-test-001"));
    assertThat(savedAdapter.getAdapterName()).isEqualTo("SWIFT Test Adapter");
    assertThat(savedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
    assertThat(savedAdapter.getNetwork()).isEqualTo(ClearingNetwork.SWIFT);
    assertThat(savedAdapter.getEndpoint()).isEqualTo("https://test.swift.com/api/v1");
    assertThat(savedAdapter.getApiVersion()).isEqualTo("1.0");
    assertThat(savedAdapter.getTimeoutSeconds()).isEqualTo(30);
    assertThat(savedAdapter.getRetryAttempts()).isEqualTo(3);
    assertThat(savedAdapter.getEncryptionEnabled()).isTrue();
  }

  @Test
  void shouldFindSwiftAdapterById() {
    // Given
    swiftAdapterRepository.save(testAdapter);

    // When
    Optional<SwiftAdapter> foundAdapter =
        swiftAdapterRepository.findById(ClearingAdapterId.of("swift-test-001"));

    // Then
    assertThat(foundAdapter).isPresent();
    assertThat(foundAdapter.get().getAdapterName()).isEqualTo("SWIFT Test Adapter");
    assertThat(foundAdapter.get().getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
  }

  @Test
  void shouldFindSwiftAdaptersByStatus() {
    // Given
    swiftAdapterRepository.save(testAdapter);

    // When
    List<SwiftAdapter> activeAdapters =
        swiftAdapterRepository.findByStatus(AdapterOperationalStatus.ACTIVE);

    // Then
    assertThat(activeAdapters).hasSize(1);
    assertThat(activeAdapters.get(0).getAdapterName()).isEqualTo("SWIFT Test Adapter");
  }

  @Test
  void shouldUpdateSwiftAdapterStatus() {
    // Given
    SwiftAdapter savedAdapter = swiftAdapterRepository.save(testAdapter);

    // When
    savedAdapter.setStatus(AdapterOperationalStatus.INACTIVE);
    savedAdapter.setUpdatedAt(Instant.now());
    SwiftAdapter updatedAdapter = swiftAdapterRepository.save(savedAdapter);

    // Then
    assertThat(updatedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.INACTIVE);
  }

  @Test
  void shouldValidateSwiftAdapterDomainFields() {
    // Given
    SwiftAdapter savedAdapter = swiftAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getId()).isInstanceOf(ClearingAdapterId.class);
    assertThat(savedAdapter.getStatus()).isInstanceOf(AdapterOperationalStatus.class);
    assertThat(savedAdapter.getNetwork()).isInstanceOf(ClearingNetwork.class);
    assertThat(savedAdapter.getTimeoutSeconds()).isInstanceOf(Integer.class);
    assertThat(savedAdapter.getRetryAttempts()).isInstanceOf(Integer.class);
    assertThat(savedAdapter.getEncryptionEnabled()).isInstanceOf(Boolean.class);
    
    // Additionally verify actual values, not just types
    assertThat(savedAdapter.getId()).isEqualTo(testAdapter.getId());
    assertThat(savedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
    assertThat(savedAdapter.getNetwork()).isEqualTo(ClearingNetwork.SWIFT);
  }

  @Test
  void shouldValidateSwiftAdapterBusinessRules() {
    // Given
    SwiftAdapter savedAdapter = swiftAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getTimeoutSeconds()).isGreaterThan(0);
    assertThat(savedAdapter.getRetryAttempts()).isGreaterThanOrEqualTo(0);
    assertThat(savedAdapter.getEndpoint()).isNotBlank();
    assertThat(savedAdapter.getApiVersion()).isNotBlank();
  }

  @Test
  void shouldCountSwiftAdaptersByStatus() {
    // Given
    swiftAdapterRepository.save(testAdapter);

    // When
    long activeCount = swiftAdapterRepository.countByStatus(AdapterOperationalStatus.ACTIVE);

    // Then
    assertThat(activeCount).isEqualTo(1);
  }

  @Test
  void shouldValidateSwiftAdapterCollections() {
    // Given
    SwiftAdapter savedAdapter = swiftAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getRoutes()).isNotNull();
    assertThat(savedAdapter.getMessageLogs()).isNotNull();
  }

  @Test
  void shouldValidateSwiftAdapterDomainEvents() {
    // Given
    SwiftAdapter savedAdapter = swiftAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getCreatedAt()).isNotNull();
    assertThat(savedAdapter.getUpdatedAt()).isNotNull();
    assertThat(savedAdapter.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    assertThat(savedAdapter.getUpdatedAt()).isBeforeOrEqualTo(Instant.now());
  }
  
  @Test
  void shouldNotFindNonExistentAdapter() {
    // When
    Optional<SwiftAdapter> foundAdapter = 
        swiftAdapterRepository.findById(ClearingAdapterId.of("non-existent-id"));

    // Then
    assertThat(foundAdapter).isEmpty();
  }
}
