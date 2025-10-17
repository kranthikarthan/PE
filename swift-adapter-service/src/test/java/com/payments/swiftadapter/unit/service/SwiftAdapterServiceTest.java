package com.payments.swiftadapter.unit.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.swiftadapter.domain.SwiftAdapter;
import com.payments.swiftadapter.exception.SwiftAdapterNotFoundException;
import com.payments.swiftadapter.fixtures.SwiftAdapterTestDataBuilder;
import com.payments.swiftadapter.fixtures.TenantContextTestDataBuilder;
import com.payments.swiftadapter.repository.SwiftAdapterRepository;
import com.payments.swiftadapter.service.SwiftAdapterService;
import com.payments.telemetry.TracingService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit Tests for SwiftAdapterService
 *
 * <p>These tests verify the business logic of SwiftAdapterService with mocked dependencies. Each
 * test focuses on a specific behavior and is isolated from database/infrastructure.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SwiftAdapterService Unit Tests")
class SwiftAdapterServiceTest {

  @Mock private SwiftAdapterRepository mockRepository;
  @Mock private TracingService mockTracingService;

  private SwiftAdapterService swiftAdapterService;

  @BeforeEach
  void setUp() {
    swiftAdapterService = new SwiftAdapterService(mockRepository, mockTracingService);
  }

  @Nested
  @DisplayName("Create Adapter Tests")
  class CreateAdapterTests {

    @Test
    @DisplayName("Should create adapter successfully with valid input")
    void shouldCreateAdapterSuccessfully() throws ExecutionException, InterruptedException {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var tenantContext = TenantContextTestDataBuilder.aTenantContext().build();
      String adapterName = "Production SWIFT Adapter";
      String endpoint = "https://swift.prod.com/api";
      String createdBy = "system-user";

      var expectedAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter()
              .withId(adapterId)
              .withAdapterName(adapterName)
              .withEndpoint(endpoint)
              .withTenantContext(tenantContext)
              .withCreatedBy(createdBy)
              .build();

      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(expectedAdapter);
      when(mockTracingService.executeInSpan(anyString(), anyMap(), any()))
          .thenAnswer(
              invocation -> invocation.getArgument(2, java.util.function.Supplier.class).get());

      // When
      CompletableFuture<SwiftAdapter> result =
          swiftAdapterService.createAdapter(
              adapterId, tenantContext, adapterName, endpoint, createdBy);
      SwiftAdapter createdAdapter = result.get();

      // Then
      assertThat(createdAdapter).isNotNull();
      assertThat(createdAdapter.getId()).isEqualTo(adapterId);
      assertThat(createdAdapter.getAdapterName()).isEqualTo(adapterName);
      assertThat(createdAdapter.getEndpoint()).isEqualTo(endpoint);
      assertThat(createdAdapter.getTenantContext()).isEqualTo(tenantContext);
      assertThat(createdAdapter.getCreatedBy()).isEqualTo(createdBy);

      verify(mockRepository, times(1)).save(any(SwiftAdapter.class));
      verify(mockTracingService, times(1)).executeInSpan(anyString(), anyMap(), any());
    }

    @Test
    @DisplayName("Should persist adapter to repository")
    void shouldPersistAdapterToRepository() throws ExecutionException, InterruptedException {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var tenantContext = TenantContextTestDataBuilder.aTenantContext().build();
      var expectedAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter()
              .withId(adapterId)
              .withTenantContext(tenantContext)
              .build();

      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(expectedAdapter);
      when(mockTracingService.executeInSpan(anyString(), anyMap(), any()))
          .thenAnswer(
              invocation -> invocation.getArgument(2, java.util.function.Supplier.class).get());

      // When
      swiftAdapterService
          .createAdapter(adapterId, tenantContext, "Test Adapter", "https://test.com", "user")
          .get();

      // Then
      verify(mockRepository, times(1))
          .save(
              argThat(
                  adapter ->
                      adapter.getId().equals(adapterId)
                          && adapter.getAdapterName().equals("Test Adapter")));
    }
  }

  @Nested
  @DisplayName("Get Adapter Tests")
  class GetAdapterTests {

    @Test
    @DisplayName("Should return adapter when found by ID")
    void shouldReturnAdapterWhenFound() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var expectedAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(expectedAdapter));

      // When
      Optional<SwiftAdapter> result = swiftAdapterService.getAdapter(adapterId);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(adapterId);
      verify(mockRepository, times(1)).findById(adapterId);
    }

    @Test
    @DisplayName("Should return empty when adapter not found")
    void shouldReturnEmptyWhenNotFound() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      when(mockRepository.findById(adapterId)).thenReturn(Optional.empty());

      // When
      Optional<SwiftAdapter> result = swiftAdapterService.getAdapter(adapterId);

      // Then
      assertThat(result).isEmpty();
      verify(mockRepository, times(1)).findById(adapterId);
    }

    @Test
    @DisplayName("Should find adapter using findById method")
    void shouldFindAdapterUsingFindById() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var expectedAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(expectedAdapter));

      // When
      Optional<SwiftAdapter> result = swiftAdapterService.findById(adapterId);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(adapterId);
    }
  }

  @Nested
  @DisplayName("Update Adapter Tests")
  class UpdateAdapterTests {

    @Test
    @DisplayName("Should update adapter configuration successfully")
    void shouldUpdateAdapterConfigurationSuccessfully() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var existingAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter()
              .withId(adapterId)
              .withEndpoint("https://old.endpoint.com")
              .withTimeoutSeconds(30)
              .build();

      var updatedAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter()
              .withId(adapterId)
              .withEndpoint("https://new.endpoint.com")
              .withTimeoutSeconds(60)
              .build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(existingAdapter));
      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(updatedAdapter);

      // When
      SwiftAdapter result =
          swiftAdapterService.updateAdapterConfiguration(
              adapterId,
              "https://new.endpoint.com",
              "2.0",
              60,
              5,
              true,
              200,
              "09:00",
              "17:00",
              "admin-user");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getEndpoint()).isEqualTo("https://new.endpoint.com");
      assertThat(result.getTimeoutSeconds()).isEqualTo(60);
      verify(mockRepository, times(1)).findById(adapterId);
      verify(mockRepository, times(1)).save(any(SwiftAdapter.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent adapter")
    void shouldThrowExceptionWhenUpdatingNonExistentAdapter() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      when(mockRepository.findById(adapterId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(
              () ->
                  swiftAdapterService.updateAdapterConfiguration(
                      adapterId,
                      "https://endpoint.com",
                      "2.0",
                      30,
                      3,
                      true,
                      100,
                      "09:00",
                      "17:00",
                      "user"))
          .isInstanceOf(SwiftAdapterNotFoundException.class);

      verify(mockRepository, times(1)).findById(adapterId);
      verify(mockRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Activate/Deactivate Adapter Tests")
  class ActivateDeactivateTests {

    @Test
    @DisplayName("Should activate adapter successfully")
    void shouldActivateAdapterSuccessfully() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var inactiveAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).inactive().build();

      var activeAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).active().build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(inactiveAdapter));
      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(activeAdapter);

      // When
      SwiftAdapter result = swiftAdapterService.activateAdapter(adapterId, "admin");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.isActive()).isTrue();
      verify(mockRepository, times(1)).findById(adapterId);
      verify(mockRepository, times(1)).save(any(SwiftAdapter.class));
    }

    @Test
    @DisplayName("Should deactivate adapter successfully")
    void shouldDeactivateAdapterSuccessfully() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var activeAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).active().build();

      var inactiveAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).inactive().build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(activeAdapter));
      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(inactiveAdapter);

      // When
      SwiftAdapter result =
          swiftAdapterService.deactivateAdapter(adapterId, "Maintenance", "admin");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.isActive()).isFalse();
      verify(mockRepository, times(1)).findById(adapterId);
      verify(mockRepository, times(1)).save(any(SwiftAdapter.class));
    }
  }

  @Nested
  @DisplayName("Query Adapter Tests")
  class QueryAdapterTests {

    @Test
    @DisplayName("Should get all adapters by tenant")
    void shouldGetAdaptersByTenant() {
      // Given
      var tenantId = "tenant-001";
      var adapter1 = SwiftAdapterTestDataBuilder.aSwiftAdapter().build();
      var adapter2 = SwiftAdapterTestDataBuilder.aSwiftAdapter().build();
      List<SwiftAdapter> expectedAdapters = Arrays.asList(adapter1, adapter2);

      when(mockRepository.findByTenantIdAndBusinessUnitId(tenantId, "bu-001"))
          .thenReturn(expectedAdapters);

      // When
      List<SwiftAdapter> result = swiftAdapterService.getAdaptersByTenant(tenantId, "bu-001");

      // Then
      assertThat(result).hasSize(2);
      assertThat(result).containsExactlyInAnyOrder(adapter1, adapter2);
      verify(mockRepository, times(1)).findByTenantIdAndBusinessUnitId(tenantId, "bu-001");
    }

    @Test
    @DisplayName("Should get active adapters by tenant")
    void shouldGetActiveAdaptersByTenant() {
      // Given
      var tenantId = "tenant-001";
      var activeAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().active().build();
      List<SwiftAdapter> expectedAdapters = Arrays.asList(activeAdapter);

      when(mockRepository.findByTenantIdAndStatus(tenantId, AdapterOperationalStatus.ACTIVE))
          .thenReturn(expectedAdapters);

      // When
      List<SwiftAdapter> result = swiftAdapterService.getActiveAdaptersByTenant(tenantId);

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).isActive()).isTrue();
    }
  }

  @Nested
  @DisplayName("Delete Adapter Tests")
  class DeleteAdapterTests {

    @Test
    @DisplayName("Should delete adapter successfully")
    void shouldDeleteAdapterSuccessfully() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      when(mockRepository.existsById(adapterId)).thenReturn(true);

      // When
      swiftAdapterService.deleteAdapter(adapterId);

      // Then
      verify(mockRepository, times(1)).deleteById(adapterId);
    }

    @Test
    @DisplayName("Should handle deletion of non-existent adapter")
    void shouldHandleDeletionOfNonExistentAdapter() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      when(mockRepository.existsById(adapterId)).thenReturn(false);

      // When & Then - should not throw exception
      assertThatNoException().isThrownBy(() -> swiftAdapterService.deleteAdapter(adapterId));
      verify(mockRepository, times(1)).deleteById(adapterId);
    }
  }

  @Nested
  @DisplayName("Validation Tests")
  class ValidationTests {

    @Test
    @DisplayName("Should validate adapter configuration successfully")
    void shouldValidateAdapterConfigurationSuccessfully() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var validAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter()
              .withId(adapterId)
              .withEndpoint("https://valid.endpoint.com")
              .active()
              .build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(validAdapter));

      // When
      Boolean result = swiftAdapterService.validateAdapterConfiguration(adapterId);

      // Then
      assertThat(result).isTrue();
      verify(mockRepository, times(1)).findById(adapterId);
    }

    @Test
    @DisplayName("Should return false for invalid adapter configuration")
    void shouldReturnFalseForInvalidConfiguration() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      when(mockRepository.findById(adapterId)).thenReturn(Optional.empty());

      // When
      Boolean result = swiftAdapterService.validateAdapterConfiguration(adapterId);

      // Then
      assertThat(result).isFalse();
    }
  }
}
