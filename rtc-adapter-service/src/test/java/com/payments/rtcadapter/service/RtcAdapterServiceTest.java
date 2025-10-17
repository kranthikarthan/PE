package com.payments.rtcadapter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.rtcadapter.domain.RtcAdapter;
import com.payments.rtcadapter.repository.RtcAdapterRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for RtcAdapterService */
@ExtendWith(MockitoExtension.class)
class RtcAdapterServiceTest {

  @Mock private RtcAdapterRepository rtcAdapterRepository;

  @InjectMocks private RtcAdapterService rtcAdapterService;

  private ClearingAdapterId adapterId;
  private TenantContext tenantContext;
  private RtcAdapter rtcAdapter;

  @BeforeEach
  void setUp() {
    adapterId = ClearingAdapterId.generate();
    tenantContext = TenantContext.of("tenant-123", "Tenant Name", "bu-456", "Business Unit");

    rtcAdapter =
        RtcAdapter.builder()
            .id(adapterId)
            .tenantContext(tenantContext)
            .adapterName("Test RTC Adapter")
            .endpoint("https://rtc.test.com/api")
            .status(AdapterOperationalStatus.INACTIVE)
            .build();
  }

  @Test
  void createAdapter_ShouldCreateAdapter_WhenValidInput() {
    // Given
    when(rtcAdapterRepository.save(any(RtcAdapter.class))).thenReturn(rtcAdapter);

    // When
    RtcAdapter result =
        rtcAdapterService.createAdapter(
            adapterId, tenantContext, "Test RTC Adapter", "https://rtc.test.com/api", "test-user");

    // Then
    assertNotNull(result);
    assertEquals(adapterId, result.getId());
    assertEquals("Test RTC Adapter", result.getAdapterName());
    verify(rtcAdapterRepository).save(any(RtcAdapter.class));
  }

  @Test
  void getAdapter_ShouldReturnAdapter_WhenAdapterExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));

    // When
    Optional<RtcAdapter> result = rtcAdapterService.getAdapter(adapterId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(adapterId, result.get().getId());
    verify(rtcAdapterRepository).findById(adapterId);
  }

  @Test
  void getAdapter_ShouldReturnEmpty_WhenAdapterNotExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.empty());

    // When
    Optional<RtcAdapter> result = rtcAdapterService.getAdapter(adapterId);

    // Then
    assertFalse(result.isPresent());
    verify(rtcAdapterRepository).findById(adapterId);
  }

  @Test
  void updateAdapterConfiguration_ShouldUpdateAdapter_WhenAdapterExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));
    when(rtcAdapterRepository.save(any(RtcAdapter.class))).thenReturn(rtcAdapter);

    // When
    RtcAdapter result =
        rtcAdapterService.updateAdapterConfiguration(
            adapterId,
            "https://rtc.new.com/api",
            "2.0",
            15,
            5,
            true,
            200,
            "08:00",
            "18:00",
            "admin");

    // Then
    assertNotNull(result);
    verify(rtcAdapterRepository).findById(adapterId);
    verify(rtcAdapterRepository).save(any(RtcAdapter.class));
  }

  @Test
  void updateAdapterConfiguration_ShouldThrowException_WhenAdapterNotExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () ->
            rtcAdapterService.updateAdapterConfiguration(
                adapterId,
                "https://rtc.new.com/api",
                "2.0",
                15,
                5,
                true,
                200,
                "08:00",
                "18:00",
                "admin"));

    verify(rtcAdapterRepository).findById(adapterId);
    verify(rtcAdapterRepository, never()).save(any(RtcAdapter.class));
  }

  @Test
  void activateAdapter_ShouldActivateAdapter_WhenAdapterExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));
    when(rtcAdapterRepository.save(any(RtcAdapter.class))).thenReturn(rtcAdapter);

    // When
    RtcAdapter result = rtcAdapterService.activateAdapter(adapterId, "admin");

    // Then
    assertNotNull(result);
    verify(rtcAdapterRepository).findById(adapterId);
    verify(rtcAdapterRepository).save(any(RtcAdapter.class));
  }

  @Test
  void activateAdapter_ShouldThrowException_WhenAdapterNotExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> rtcAdapterService.activateAdapter(adapterId, "admin"));

    verify(rtcAdapterRepository).findById(adapterId);
    verify(rtcAdapterRepository, never()).save(any(RtcAdapter.class));
  }

  @Test
  void deactivateAdapter_ShouldDeactivateAdapter_WhenAdapterExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));
    when(rtcAdapterRepository.save(any(RtcAdapter.class))).thenReturn(rtcAdapter);

    // When
    RtcAdapter result = rtcAdapterService.deactivateAdapter(adapterId, "Maintenance", "admin");

    // Then
    assertNotNull(result);
    verify(rtcAdapterRepository).findById(adapterId);
    verify(rtcAdapterRepository).save(any(RtcAdapter.class));
  }

  @Test
  void deactivateAdapter_ShouldThrowException_WhenAdapterNotExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> rtcAdapterService.deactivateAdapter(adapterId, "Maintenance", "admin"));

    verify(rtcAdapterRepository).findById(adapterId);
    verify(rtcAdapterRepository, never()).save(any(RtcAdapter.class));
  }

  @Test
  void isAdapterActive_ShouldReturnTrue_WhenAdapterIsActive() {
    // Given
    rtcAdapter.setStatus(AdapterOperationalStatus.ACTIVE);
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));

    // When
    boolean result = rtcAdapterService.isAdapterActive(adapterId);

    // Then
    assertTrue(result);
    verify(rtcAdapterRepository).findById(adapterId);
  }

  @Test
  void isAdapterActive_ShouldReturnFalse_WhenAdapterIsInactive() {
    // Given
    rtcAdapter.setStatus(AdapterOperationalStatus.INACTIVE);
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));

    // When
    boolean result = rtcAdapterService.isAdapterActive(adapterId);

    // Then
    assertFalse(result);
    verify(rtcAdapterRepository).findById(adapterId);
  }

  @Test
  void isAdapterActive_ShouldReturnFalse_WhenAdapterNotExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.empty());

    // When
    boolean result = rtcAdapterService.isAdapterActive(adapterId);

    // Then
    assertFalse(result);
    verify(rtcAdapterRepository).findById(adapterId);
  }

  @Test
  void validateAdapterConfiguration_ShouldReturnTrue_WhenConfigurationIsValid() {
    // Given
    rtcAdapter.setEndpoint("https://rtc.test.com/api");
    rtcAdapter.setApiVersion("1.0");
    rtcAdapter.setTimeoutSeconds(10);
    rtcAdapter.setRetryAttempts(3);
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));

    // When
    boolean result = rtcAdapterService.validateAdapterConfiguration(adapterId);

    // Then
    assertTrue(result);
    verify(rtcAdapterRepository).findById(adapterId);
  }

  @Test
  void validateAdapterConfiguration_ShouldReturnFalse_WhenConfigurationIsInvalid() {
    // Given
    rtcAdapter.setEndpoint(null);
    rtcAdapter.setApiVersion(null);
    rtcAdapter.setTimeoutSeconds(null);
    rtcAdapter.setRetryAttempts(null);
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));

    // When
    boolean result = rtcAdapterService.validateAdapterConfiguration(adapterId);

    // Then
    assertFalse(result);
    verify(rtcAdapterRepository).findById(adapterId);
  }

  @Test
  void validateAdapterConfiguration_ShouldReturnFalse_WhenAdapterNotExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.empty());

    // When
    boolean result = rtcAdapterService.validateAdapterConfiguration(adapterId);

    // Then
    assertFalse(result);
    verify(rtcAdapterRepository).findById(adapterId);
  }
}
