package com.payments.samosadapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.samosadapter.domain.SamosAdapter;
import com.payments.samosadapter.repository.SamosAdapterRepository;
import com.payments.samosadapter.service.SamosAdapterService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SAMOS Adapter Service Tests
 *
 * <p>Unit tests for SAMOS adapter service business logic.
 */
@ExtendWith(MockitoExtension.class)
class SamosAdapterServiceTest {

  @Mock private SamosAdapterRepository samosAdapterRepository;

  @InjectMocks private SamosAdapterService samosAdapterService;

  private TenantContext tenantContext;
  private SamosAdapter samosAdapter;

  @BeforeEach
  void setUp() {
    tenantContext =
        TenantContext.builder()
            .tenantId(UUID.randomUUID().toString())
            .businessUnitId(UUID.randomUUID().toString())
            .build();

    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    samosAdapter =
        SamosAdapter.create(
            adapterId, tenantContext, "test-adapter", "https://samos.sarb.co.za/rtgs", "test-user");
  }

  @Test
  void createAdapter_ShouldCreateAdapter_WhenValidInput() {
    // Given
    when(samosAdapterRepository.existsByTenantIdAndAdapterName(anyString(), anyString()))
        .thenReturn(false);
    when(samosAdapterRepository.save(any(SamosAdapter.class))).thenReturn(samosAdapter);

    // When
    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    SamosAdapter result =
        samosAdapterService.createAdapter(
            adapterId, tenantContext, "test-adapter", "https://samos.sarb.co.za/rtgs", "test-user");

    // Then
    assertNotNull(result);
    assertEquals("test-adapter", result.getAdapterName());
    assertEquals("https://samos.sarb.co.za/rtgs", result.getEndpoint());
    verify(samosAdapterRepository).save(any(SamosAdapter.class));
  }

  @Test
  void createAdapter_ShouldThrowException_WhenAdapterNameExists() {
    // Given
    when(samosAdapterRepository.existsByTenantIdAndAdapterName(anyString(), anyString()))
        .thenReturn(true);

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ClearingAdapterId adapterId = ClearingAdapterId.generate();
          samosAdapterService.createAdapter(
              adapterId,
              tenantContext,
              "test-adapter",
              "https://samos.sarb.co.za/rtgs",
              "test-user");
        });
  }

  @Test
  void getAdapter_ShouldReturnAdapter_WhenAdapterExists() {
    // Given
    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    when(samosAdapterRepository.findById(any(ClearingAdapterId.class)))
        .thenReturn(Optional.of(samosAdapter));
    // ensure adapter starts inactive for activation test
    samosAdapter.deactivate("setup", "tester");

    // When
    Optional<SamosAdapter> result = samosAdapterService.getAdapter(adapterId);

    // Then
    assertTrue(result.isPresent());
    assertEquals("test-adapter", result.get().getAdapterName());
  }

  @Test
  void getAdapter_ShouldReturnEmpty_WhenAdapterNotExists() {
    // Given
    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    when(samosAdapterRepository.findById(any(ClearingAdapterId.class)))
        .thenReturn(Optional.empty());

    // When
    Optional<SamosAdapter> result = samosAdapterService.getAdapter(adapterId);

    // Then
    assertFalse(result.isPresent());
  }

  @Test
  void activateAdapter_ShouldActivateAdapter_WhenAdapterExists() {
    // Given
    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    when(samosAdapterRepository.findById(any(ClearingAdapterId.class)))
        .thenReturn(Optional.of(samosAdapter));
    when(samosAdapterRepository.save(any(SamosAdapter.class))).thenReturn(samosAdapter);

    // When
    SamosAdapter result = samosAdapterService.activateAdapter(adapterId, "test-user");

    // Then
    assertNotNull(result);
    verify(samosAdapterRepository).save(any(SamosAdapter.class));
  }

  @Test
  void activateAdapter_ShouldThrowException_WhenAdapterNotExists() {
    // Given
    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    when(samosAdapterRepository.findById(any(ClearingAdapterId.class)))
        .thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          samosAdapterService.activateAdapter(adapterId, "test-user");
        });
  }

  @Test
  void isAdapterActive_ShouldReturnTrue_WhenAdapterIsActive() {
    // Given
    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    when(samosAdapterRepository.findById(any(ClearingAdapterId.class)))
        .thenReturn(Optional.of(samosAdapter));

    // When
    boolean result = samosAdapterService.isAdapterActive(adapterId);

    // Then
    assertTrue(result);
  }

  @Test
  void isAdapterActive_ShouldReturnFalse_WhenAdapterNotExists() {
    // Given
    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    when(samosAdapterRepository.findById(any(ClearingAdapterId.class)))
        .thenReturn(Optional.empty());

    // When
    boolean result = samosAdapterService.isAdapterActive(adapterId);

    // Then
    assertFalse(result);
  }
}
