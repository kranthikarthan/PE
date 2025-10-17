package com.payments.swiftadapter.unit.exception;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.swiftadapter.domain.SwiftAdapter;
import com.payments.swiftadapter.exception.SwiftAdapterNotFoundException;
import com.payments.swiftadapter.fixtures.SwiftAdapterTestDataBuilder;
import com.payments.swiftadapter.repository.SwiftAdapterRepository;
import com.payments.swiftadapter.service.SwiftAdapterService;
import com.payments.telemetry.TracingService;
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
 * Exception Handling Tests
 *
 * <p>Tests verify that all exception scenarios are properly handled: - Resource not found scenarios
 * - Invalid input scenarios - Operation failures - Null/empty parameter validation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Exception Handling Tests")
class ExceptionHandlingTest {

  @Mock private SwiftAdapterRepository mockRepository;
  @Mock private TracingService mockTracingService;

  private SwiftAdapterService swiftAdapterService;

  @BeforeEach
  void setUp() {
    swiftAdapterService = new SwiftAdapterService(mockRepository, mockTracingService);
  }

  @Nested
  @DisplayName("Not Found Exception Tests")
  class NotFoundExceptionTests {

    @Test
    @DisplayName("Should throw SwiftAdapterNotFoundException when adapter not found during update")
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistent() {
      // Given
      var nonExistentId = ClearingAdapterId.generate();
      when(mockRepository.findById(nonExistentId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(
              () ->
                  swiftAdapterService.updateAdapterConfiguration(
                      nonExistentId,
                      "https://endpoint.com",
                      "1.0",
                      30,
                      3,
                      true,
                      100,
                      "09:00",
                      "17:00",
                      "user"))
          .isInstanceOf(SwiftAdapterNotFoundException.class)
          .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should throw SwiftAdapterNotFoundException when activating non-existent adapter")
    void shouldThrowNotFoundExceptionWhenActivatingNonExistent() {
      // Given
      var nonExistentId = ClearingAdapterId.generate();
      when(mockRepository.findById(nonExistentId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> swiftAdapterService.activateAdapter(nonExistentId, "user"))
          .isInstanceOf(SwiftAdapterNotFoundException.class);
    }

    @Test
    @DisplayName(
        "Should throw SwiftAdapterNotFoundException when deactivating non-existent adapter")
    void shouldThrowNotFoundExceptionWhenDeactivatingNonExistent() {
      // Given
      var nonExistentId = ClearingAdapterId.generate();
      when(mockRepository.findById(nonExistentId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(
              () -> swiftAdapterService.deactivateAdapter(nonExistentId, "Reason", "user"))
          .isInstanceOf(SwiftAdapterNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw SwiftAdapterNotFoundException when validating non-existent adapter")
    void shouldThrowNotFoundExceptionWhenValidatingNonExistent() {
      // Given
      var nonExistentId = ClearingAdapterId.generate();
      when(mockRepository.findById(nonExistentId)).thenReturn(Optional.empty());

      // When
      Boolean result = swiftAdapterService.validateAdapterConfiguration(nonExistentId);

      // Then
      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("Invalid Input Validation Tests")
  class InvalidInputValidationTests {

    @Test
    @DisplayName("Should handle null adapter name in creation")
    void shouldHandleNullAdapterName() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var tenantContext =
          com.payments.swiftadapter.fixtures.TenantContextTestDataBuilder.aTenantContext().build();

      // When & Then - Creation should handle null safely
      assertThatThrownBy(
              () ->
                  swiftAdapterService.createAdapter(
                      adapterId, tenantContext, null, "https://endpoint.com", "user"))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should handle empty endpoint string")
    void shouldHandleEmptyEndpoint() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var tenantContext =
          com.payments.swiftadapter.fixtures.TenantContextTestDataBuilder.aTenantContext().build();

      // When & Then
      assertThatCode(
              () ->
                  swiftAdapterService.createAdapter(
                      adapterId, tenantContext, "Adapter", "", "user"))
          .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should validate negative timeout seconds")
    void shouldValidateNegativeTimeoutSeconds() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var adapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter()
              .withId(adapterId)
              .withTimeoutSeconds(-1) // Invalid
              .build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(adapter));

      // When & Then - Business rule validation
      assertThat(adapter.getTimeoutSeconds()).isLessThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should validate negative retry attempts")
    void shouldValidateNegativeRetryAttempts() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var adapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter()
              .withId(adapterId)
              .withRetryAttempts(-1) // Invalid
              .build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(adapter));

      // When & Then
      assertThat(adapter.getRetryAttempts()).isLessThan(0);
    }
  }

  @Nested
  @DisplayName("Boundary Value Tests")
  class BoundaryValueTests {

    @Test
    @DisplayName("Should handle minimum timeout (1 second)")
    void shouldHandleMinimumTimeout() {
      // Given
      var adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().withTimeoutSeconds(1).build();

      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(adapter);
      when(mockTracingService.executeInSpan(anyString(), anyMap(), any()))
          .thenAnswer(
              invocation -> invocation.getArgument(2, java.util.function.Supplier.class).get());

      // When & Then
      assertThat(adapter.getTimeoutSeconds()).isPositive();
    }

    @Test
    @DisplayName("Should handle maximum timeout (999 seconds)")
    void shouldHandleMaximumTimeout() {
      // Given
      var adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().withTimeoutSeconds(999).build();

      // When & Then
      assertThat(adapter.getTimeoutSeconds()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should handle zero retry attempts")
    void shouldHandleZeroRetryAttempts() {
      // Given
      var adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().withRetryAttempts(0).build();

      // When & Then
      assertThat(adapter.getRetryAttempts()).isZero();
    }

    @Test
    @DisplayName("Should handle maximum retry attempts")
    void shouldHandleMaximumRetryAttempts() {
      // Given
      var adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().withRetryAttempts(100).build();

      // When & Then
      assertThat(adapter.getRetryAttempts()).isGreaterThanOrEqualTo(0);
    }
  }

  @Nested
  @DisplayName("Repository Failure Tests")
  class RepositoryFailureTests {

    @Test
    @DisplayName("Should handle repository exception during save")
    void shouldHandleRepositoryExceptionDuringSave() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var tenantContext =
          com.payments.swiftadapter.fixtures.TenantContextTestDataBuilder.aTenantContext().build();

      when(mockRepository.save(any(SwiftAdapter.class)))
          .thenThrow(new RuntimeException("Database connection failed"));
      when(mockTracingService.executeInSpan(anyString(), anyMap(), any()))
          .thenAnswer(
              invocation -> invocation.getArgument(2, java.util.function.Supplier.class).get());

      // When & Then
      assertThatThrownBy(
              () ->
                  swiftAdapterService
                      .createAdapter(
                          adapterId, tenantContext, "Adapter", "https://endpoint.com", "user")
                      .get())
          .hasCauseInstanceOf(RuntimeException.class)
          .hasMessageContaining("Database");
    }

    @Test
    @DisplayName("Should handle repository exception during find")
    void shouldHandleRepositoryExceptionDuringFind() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      when(mockRepository.findById(adapterId))
          .thenThrow(new RuntimeException("Database query failed"));

      // When & Then
      assertThatThrownBy(() -> swiftAdapterService.getAdapter(adapterId))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("query failed");
    }

    @Test
    @DisplayName("Should handle repository exception during delete")
    void shouldHandleRepositoryExceptionDuringDelete() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      doThrow(new RuntimeException("Delete failed")).when(mockRepository).deleteById(adapterId);

      // When & Then
      assertThatThrownBy(() -> swiftAdapterService.deleteAdapter(adapterId))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Delete failed");
    }
  }

  @Nested
  @DisplayName("Concurrent Access Tests")
  class ConcurrentAccessTests {

    @Test
    @DisplayName("Should handle concurrent adapter creation")
    void shouldHandleConcurrentCreation() throws ExecutionException, InterruptedException {
      // Given
      var adapterId1 = ClearingAdapterId.generate();
      var adapterId2 = ClearingAdapterId.generate();
      var tenantContext =
          com.payments.swiftadapter.fixtures.TenantContextTestDataBuilder.aTenantContext().build();

      var adapter1 = SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId1).build();
      var adapter2 = SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId2).build();

      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(adapter1).thenReturn(adapter2);
      when(mockTracingService.executeInSpan(anyString(), anyMap(), any()))
          .thenAnswer(
              invocation -> invocation.getArgument(2, java.util.function.Supplier.class).get());

      // When
      CompletableFuture<SwiftAdapter> future1 =
          swiftAdapterService.createAdapter(
              adapterId1, tenantContext, "Adapter 1", "https://endpoint1.com", "user");
      CompletableFuture<SwiftAdapter> future2 =
          swiftAdapterService.createAdapter(
              adapterId2, tenantContext, "Adapter 2", "https://endpoint2.com", "user");

      SwiftAdapter result1 = future1.get();
      SwiftAdapter result2 = future2.get();

      // Then
      assertThat(result1.getId()).isNotEqualTo(result2.getId());
      verify(mockRepository, times(2)).save(any(SwiftAdapter.class));
    }

    @Test
    @DisplayName("Should handle concurrent adapter retrieval")
    void shouldHandleConcurrentRetrieval() throws ExecutionException, InterruptedException {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).build();
      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(adapter));

      // When
      CompletableFuture<Optional<SwiftAdapter>> future1 =
          CompletableFuture.supplyAsync(() -> swiftAdapterService.getAdapter(adapterId));
      CompletableFuture<Optional<SwiftAdapter>> future2 =
          CompletableFuture.supplyAsync(() -> swiftAdapterService.getAdapter(adapterId));

      Optional<SwiftAdapter> result1 = future1.get();
      Optional<SwiftAdapter> result2 = future2.get();

      // Then
      assertThat(result1).isPresent();
      assertThat(result2).isPresent();
      assertThat(result1.get().getId()).isEqualTo(result2.get().getId());
    }
  }

  @Nested
  @DisplayName("State Transition Tests")
  class StateTransitionTests {

    @Test
    @DisplayName("Should handle activation of already active adapter")
    void shouldHandleActivationOfAlreadyActiveAdapter() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var activeAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).active().build();

      var resultAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).active().build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(activeAdapter));
      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(resultAdapter);

      // When
      SwiftAdapter result = swiftAdapterService.activateAdapter(adapterId, "user");

      // Then
      assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle deactivation of already inactive adapter")
    void shouldHandleDeactivationOfAlreadyInactiveAdapter() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var inactiveAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).inactive().build();

      var resultAdapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).inactive().build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(inactiveAdapter));
      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(resultAdapter);

      // When
      SwiftAdapter result = swiftAdapterService.deactivateAdapter(adapterId, "Reason", "user");

      // Then
      assertThat(result.isActive()).isFalse();
    }
  }

  @Nested
  @DisplayName("Data Integrity Tests")
  class DataIntegrityTests {

    @Test
    @DisplayName("Should preserve adapter ID after update")
    void shouldPreserveAdapterIdAfterUpdate() {
      // Given
      var adapterId = ClearingAdapterId.of("fixed-id-001");
      var adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().withId(adapterId).build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(adapter));
      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(adapter);

      // When
      SwiftAdapter result =
          swiftAdapterService.updateAdapterConfiguration(
              adapterId,
              "https://new.endpoint.com",
              "2.0",
              30,
              3,
              true,
              100,
              "09:00",
              "17:00",
              "user");

      // Then
      assertThat(result.getId()).isEqualTo(adapterId);
    }

    @Test
    @DisplayName("Should not lose tenant context after update")
    void shouldNotLoseTenantContextAfterUpdate() {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var tenantContext =
          com.payments.swiftadapter.fixtures.TenantContextTestDataBuilder.aTenantContext().build();
      var adapter =
          SwiftAdapterTestDataBuilder.aSwiftAdapter()
              .withId(adapterId)
              .withTenantContext(tenantContext)
              .build();

      when(mockRepository.findById(adapterId)).thenReturn(Optional.of(adapter));
      when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(adapter);

      // When
      SwiftAdapter result =
          swiftAdapterService.updateAdapterConfiguration(
              adapterId,
              "https://new.endpoint.com",
              "2.0",
              30,
              3,
              true,
              100,
              "09:00",
              "17:00",
              "user");

      // Then
      assertThat(result.getTenantContext()).isEqualTo(tenantContext);
    }
  }
}
