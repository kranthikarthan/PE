package com.payments.rtcadapter.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.rtcadapter.domain.RtcAdapter;
import com.payments.rtcadapter.repository.RtcAdapterRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * RTC Domain Model Validation Test
 *
 * <p>Comprehensive validation tests to ensure RTC adapter properly aligns with shared
 * clearing-adapter domain model
 */
@DataJpaTest
@ActiveProfiles("test")
public class RtcDomainModelValidationTest {

  @Autowired private RtcAdapterRepository rtcAdapterRepository;

  private RtcAdapter testAdapter;

  @BeforeEach
  void setUp() {
    testAdapter =
        RtcAdapter.builder()
            .id(ClearingAdapterId.of("rtc-test-001"))
            .name("RTC Test Adapter")
            .description("Test RTC adapter for domain model validation")
            .status(AdapterOperationalStatus.ACTIVE)
            .network(ClearingNetwork.RTC)
            .endpoint("https://test.rtc.co.za/api/v1")
            .apiVersion("1.0")
            .timeoutSeconds(30)
            .retryAttempts(3)
            .encryptionEnabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
  }

  @Test
  void shouldCreateRtcAdapterWithValidDomainModel() {
    // When
    RtcAdapter savedAdapter = rtcAdapterRepository.save(testAdapter);

    // Then
    assertThat(savedAdapter).isNotNull();
    assertThat(savedAdapter.getId()).isEqualTo(ClearingAdapterId.of("rtc-test-001"));
    assertThat(savedAdapter.getName()).isEqualTo("RTC Test Adapter");
    assertThat(savedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
    assertThat(savedAdapter.getNetwork()).isEqualTo(ClearingNetwork.RTC);
    assertThat(savedAdapter.getEndpoint()).isEqualTo("https://test.rtc.co.za/api/v1");
    assertThat(savedAdapter.getApiVersion()).isEqualTo("1.0");
    assertThat(savedAdapter.getTimeoutSeconds()).isEqualTo(30);
    assertThat(savedAdapter.getRetryAttempts()).isEqualTo(3);
    assertThat(savedAdapter.isEncryptionEnabled()).isTrue();
  }

  @Test
  void shouldFindRtcAdapterById() {
    // Given
    rtcAdapterRepository.save(testAdapter);

    // When
    Optional<RtcAdapter> foundAdapter =
        rtcAdapterRepository.findById(ClearingAdapterId.of("rtc-test-001"));

    // Then
    assertThat(foundAdapter).isPresent();
    assertThat(foundAdapter.get().getName()).isEqualTo("RTC Test Adapter");
    assertThat(foundAdapter.get().getStatus()).isEqualTo(AdapterOperationalStatus.ACTIVE);
  }

  @Test
  void shouldFindRtcAdaptersByStatus() {
    // Given
    rtcAdapterRepository.save(testAdapter);

    // When
    List<RtcAdapter> activeAdapters =
        rtcAdapterRepository.findByStatus(AdapterOperationalStatus.ACTIVE);

    // Then
    assertThat(activeAdapters).hasSize(1);
    assertThat(activeAdapters.get(0).getName()).isEqualTo("RTC Test Adapter");
  }

  @Test
  void shouldUpdateRtcAdapterStatus() {
    // Given
    RtcAdapter savedAdapter = rtcAdapterRepository.save(testAdapter);

    // When
    savedAdapter.setStatus(AdapterOperationalStatus.INACTIVE);
    savedAdapter.setUpdatedAt(Instant.now());
    RtcAdapter updatedAdapter = rtcAdapterRepository.save(savedAdapter);

    // Then
    assertThat(updatedAdapter.getStatus()).isEqualTo(AdapterOperationalStatus.INACTIVE);
  }

  @Test
  void shouldValidateRtcAdapterDomainFields() {
    // Given
    RtcAdapter savedAdapter = rtcAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getId()).isInstanceOf(ClearingAdapterId.class);
    assertThat(savedAdapter.getStatus()).isInstanceOf(AdapterOperationalStatus.class);
    assertThat(savedAdapter.getNetwork()).isInstanceOf(ClearingNetwork.class);
    assertThat(savedAdapter.getTimeoutSeconds()).isInstanceOf(Integer.class);
    assertThat(savedAdapter.getRetryAttempts()).isInstanceOf(Integer.class);
    assertThat(savedAdapter.isEncryptionEnabled()).isInstanceOf(Boolean.class);
  }

  @Test
  void shouldValidateRtcAdapterBusinessRules() {
    // Given
    RtcAdapter savedAdapter = rtcAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getTimeoutSeconds()).isGreaterThan(0);
    assertThat(savedAdapter.getRetryAttempts()).isGreaterThanOrEqualTo(0);
    assertThat(savedAdapter.getEndpoint()).isNotBlank();
    assertThat(savedAdapter.getApiVersion()).isNotBlank();
  }

  @Test
  void shouldCountRtcAdaptersByStatus() {
    // Given
    rtcAdapterRepository.save(testAdapter);

    // When
    long activeCount = rtcAdapterRepository.countByStatus(AdapterOperationalStatus.ACTIVE);

    // Then
    assertThat(activeCount).isEqualTo(1);
  }

  @Test
  void shouldValidateRtcAdapterCollections() {
    // Given
    RtcAdapter savedAdapter = rtcAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getRoutes()).isNotNull();
    assertThat(savedAdapter.getMessageLogs()).isNotNull();
  }

  @Test
  void shouldValidateRtcAdapterDomainEvents() {
    // Given
    RtcAdapter savedAdapter = rtcAdapterRepository.save(testAdapter);

    // When & Then
    assertThat(savedAdapter.getCreatedAt()).isNotNull();
    assertThat(savedAdapter.getUpdatedAt()).isNotNull();
    assertThat(savedAdapter.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    assertThat(savedAdapter.getUpdatedAt()).isBeforeOrEqualTo(Instant.now());
  }
}
