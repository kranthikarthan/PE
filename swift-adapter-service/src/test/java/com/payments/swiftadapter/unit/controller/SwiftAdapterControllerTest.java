package com.payments.swiftadapter.unit.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.swiftadapter.controller.SwiftAdapterController;
import com.payments.swiftadapter.domain.SwiftAdapter;
import com.payments.swiftadapter.fixtures.SwiftAdapterTestDataBuilder;
import com.payments.swiftadapter.service.SwiftAdapterService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit Tests for SwiftAdapterController
 * 
 * Tests the REST API endpoints using MockMvc without starting the full Spring context.
 * Each test verifies HTTP status codes, response bodies, and error handling.
 */
@WebMvcTest(SwiftAdapterController.class)
@DisplayName("SwiftAdapterController REST API Tests")
class SwiftAdapterControllerTest {

  // Test configuration to provide mock bean
  @TestConfiguration
  static class TestConfig {
    @Bean
    public SwiftAdapterService swiftAdapterService() {
      return mock(SwiftAdapterService.class);
    }
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private SwiftAdapterService swiftAdapterService;

  private static final String API_V1_SWIFT_ADAPTERS = "/api/v1/swift-adapters";

  @Nested
  @DisplayName("Create Adapter Endpoint Tests")
  class CreateAdapterEndpointTests {

    @Test
    @DisplayName("Should create adapter and return 201 CREATED")
    void shouldCreateAdapterAndReturn201() throws Exception {
      // Given
      var adapterId = ClearingAdapterId.generate();
      var createdAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter()
          .withId(adapterId)
          .build();

      when(swiftAdapterService.createAdapter(any(), any(), anyString(), anyString(), anyString()))
          .thenReturn(CompletableFuture.completedFuture(createdAdapter));

      String requestBody = """
          {
            "tenantId": "tenant-001",
            "tenantName": "Test Tenant",
            "businessUnitId": "bu-001",
            "businessUnitName": "Test Business Unit",
            "adapterName": "Test SWIFT Adapter",
            "endpoint": "https://swift.test.com/api",
            "createdBy": "test-user"
          }
          """;

      // When & Then
      mockMvc.perform(post(API_V1_SWIFT_ADAPTERS)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.adapterName").value("Test SWIFT Adapter"))
          .andExpect(jsonPath("$.id").exists())
          .andExpect(jsonPath("$.status").value("ACTIVE"));

      verify(swiftAdapterService, times(1)).createAdapter(
          any(ClearingAdapterId.class), any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST for invalid input")
    void shouldReturnBadRequestForInvalidInput() throws Exception {
      // Given - missing required fields
      String invalidRequest = """
          {
            "tenantId": "tenant-001"
          }
          """;

      // When & Then
      mockMvc.perform(post(API_V1_SWIFT_ADAPTERS)
              .contentType(MediaType.APPLICATION_JSON)
              .content(invalidRequest))
          .andExpect(status().isBadRequest());

      verify(swiftAdapterService, never()).createAdapter(any(), any(), anyString(), anyString(), anyString());
    }
  }

  @Nested
  @DisplayName("Get Adapter Endpoint Tests")
  class GetAdapterEndpointTests {

    @Test
    @DisplayName("Should return adapter with 200 OK")
    void shouldReturnAdapterWith200OK() throws Exception {
      // Given
      var adapterId = ClearingAdapterId.of("swift-adapter-001");
      var expectedAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter()
          .withId(adapterId)
          .withAdapterName("Production SWIFT Adapter")
          .build();

      when(swiftAdapterService.findById(adapterId))
          .thenReturn(Optional.of(expectedAdapter));

      // When & Then
      mockMvc.perform(get(API_V1_SWIFT_ADAPTERS + "/{adapterId}", adapterId.getValue())
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value("swift-adapter-001"))
          .andExpect(jsonPath("$.adapterName").value("Production SWIFT Adapter"))
          .andExpect(jsonPath("$.status").value("ACTIVE"));

      verify(swiftAdapterService, times(1)).findById(adapterId);
    }

    @Test
    @DisplayName("Should return 404 NOT FOUND when adapter does not exist")
    void shouldReturn404WhenAdapterNotFound() throws Exception {
      // Given
      var adapterId = ClearingAdapterId.of("non-existent-id");
      when(swiftAdapterService.findById(adapterId))
          .thenReturn(Optional.empty());

      // When & Then
      mockMvc.perform(get(API_V1_SWIFT_ADAPTERS + "/{adapterId}", adapterId.getValue())
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());

      verify(swiftAdapterService, times(1)).findById(adapterId);
    }
  }

  @Nested
  @DisplayName("Update Adapter Configuration Endpoint Tests")
  class UpdateAdapterConfigurationEndpointTests {

    @Test
    @DisplayName("Should update adapter configuration and return 200 OK")
    void shouldUpdateAdapterConfigurationAndReturn200() throws Exception {
      // Given
      var adapterId = ClearingAdapterId.of("swift-adapter-001");
      var updatedAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter()
          .withId(adapterId)
          .withEndpoint("https://new.swift.endpoint.com/api")
          .withTimeoutSeconds(60)
          .build();

      when(swiftAdapterService.updateAdapterConfiguration(
          eq(adapterId), anyString(), anyString(), anyInt(), anyInt(), anyBoolean(),
          anyInt(), anyString(), anyString(), anyString()))
          .thenReturn(updatedAdapter);

      String requestBody = """
          {
            "endpoint": "https://new.swift.endpoint.com/api",
            "apiVersion": "2.0",
            "timeoutSeconds": 60,
            "retryAttempts": 5,
            "encryptionEnabled": true,
            "batchSize": 200,
            "processingWindowStart": "09:00",
            "processingWindowEnd": "17:00"
          }
          """;

      // When & Then
      mockMvc.perform(put(API_V1_SWIFT_ADAPTERS + "/{adapterId}/configuration", adapterId.getValue())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.endpoint").value("https://new.swift.endpoint.com/api"))
          .andExpect(jsonPath("$.timeoutSeconds").value(60));

      verify(swiftAdapterService, times(1)).updateAdapterConfiguration(
          any(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean(), anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent adapter")
    void shouldReturn404WhenUpdatingNonExistentAdapter() throws Exception {
      // Given
      var adapterId = ClearingAdapterId.of("non-existent-id");
      when(swiftAdapterService.updateAdapterConfiguration(
          any(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean(),
          anyInt(), anyString(), anyString(), anyString()))
          .thenThrow(new com.payments.swiftadapter.exception.SwiftAdapterNotFoundException(
              "Adapter not found: " + adapterId.getValue()));

      String requestBody = """
          {
            "endpoint": "https://new.endpoint.com/api",
            "apiVersion": "2.0",
            "timeoutSeconds": 30,
            "retryAttempts": 3,
            "encryptionEnabled": true,
            "batchSize": 100,
            "processingWindowStart": "09:00",
            "processingWindowEnd": "17:00"
          }
          """;

      // When & Then
      mockMvc.perform(put(API_V1_SWIFT_ADAPTERS + "/{adapterId}/configuration", adapterId.getValue())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Activate Adapter Endpoint Tests")
  class ActivateAdapterEndpointTests {

    @Test
    @DisplayName("Should activate adapter and return 200 OK")
    void shouldActivateAdapterAndReturn200() throws Exception {
      // Given
      var adapterId = ClearingAdapterId.of("swift-adapter-001");
      var activatedAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter()
          .withId(adapterId)
          .active()
          .build();

      when(swiftAdapterService.activateAdapter(eq(adapterId), anyString()))
          .thenReturn(activatedAdapter);

      String requestBody = """
          {
            "activatedBy": "admin-user"
          }
          """;

      // When & Then
      mockMvc.perform(post(API_V1_SWIFT_ADAPTERS + "/{adapterId}/activate", adapterId.getValue())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("ACTIVE"));

      verify(swiftAdapterService, times(1)).activateAdapter(eq(adapterId), anyString());
    }
  }

  @Nested
  @DisplayName("Deactivate Adapter Endpoint Tests")
  class DeactivateAdapterEndpointTests {

    @Test
    @DisplayName("Should deactivate adapter and return 200 OK")
    void shouldDeactivateAdapterAndReturn200() throws Exception {
      // Given
      var adapterId = ClearingAdapterId.of("swift-adapter-001");
      var deactivatedAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter()
          .withId(adapterId)
          .inactive()
          .build();

      when(swiftAdapterService.deactivateAdapter(eq(adapterId), anyString(), anyString()))
          .thenReturn(deactivatedAdapter);

      String requestBody = """
          {
            "reason": "Maintenance",
            "deactivatedBy": "admin-user"
          }
          """;

      // When & Then
      mockMvc.perform(post(API_V1_SWIFT_ADAPTERS + "/{adapterId}/deactivate", adapterId.getValue())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("INACTIVE"));

      verify(swiftAdapterService, times(1)).deactivateAdapter(
          eq(adapterId), anyString(), anyString());
    }
  }

  @Nested
  @DisplayName("List Adapters By Tenant Endpoint Tests")
  class ListAdaptersByTenantEndpointTests {

    @Test
    @DisplayName("Should return list of adapters for tenant")
    void shouldReturnListOfAdaptersForTenant() throws Exception {
      // Given
      String tenantId = "tenant-001";
      var adapter1 = SwiftAdapterTestDataBuilder.aSwiftAdapter()
          .withAdapterName("Adapter 1")
          .build();
      var adapter2 = SwiftAdapterTestDataBuilder.aSwiftAdapter()
          .withAdapterName("Adapter 2")
          .build();
      List<SwiftAdapter> adapters = Arrays.asList(adapter1, adapter2);

      when(swiftAdapterService.getAdaptersByTenant(eq(tenantId), anyString()))
          .thenReturn(adapters);

      // When & Then
      mockMvc.perform(get(API_V1_SWIFT_ADAPTERS + "/tenant/{tenantId}", tenantId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(2));

      verify(swiftAdapterService, times(1)).getAdaptersByTenant(eq(tenantId), anyString());
    }

    @Test
    @DisplayName("Should return empty list when no adapters found for tenant")
    void shouldReturnEmptyListWhenNoAdaptersFound() throws Exception {
      // Given
      String tenantId = "tenant-001";
      when(swiftAdapterService.getAdaptersByTenant(eq(tenantId), anyString()))
          .thenReturn(Arrays.asList());

      // When & Then
      mockMvc.perform(get(API_V1_SWIFT_ADAPTERS + "/tenant/{tenantId}", tenantId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(0));
    }
  }

  @Nested
  @DisplayName("Delete Adapter Endpoint Tests")
  class DeleteAdapterEndpointTests {

    @Test
    @DisplayName("Should delete adapter and return 204 NO CONTENT")
    void shouldDeleteAdapterAndReturn204() throws Exception {
      // Given
      var adapterId = ClearingAdapterId.of("swift-adapter-001");
      doNothing().when(swiftAdapterService).deleteAdapter(adapterId);

      // When & Then
      mockMvc.perform(delete(API_V1_SWIFT_ADAPTERS + "/{adapterId}", adapterId.getValue())
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNoContent());

      verify(swiftAdapterService, times(1)).deleteAdapter(adapterId);
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should return 400 for malformed JSON")
    void shouldReturnBadRequestForMalformedJson() throws Exception {
      // Given
      String malformedJson = "{invalid json}";

      // When & Then
      mockMvc.perform(post(API_V1_SWIFT_ADAPTERS)
              .contentType(MediaType.APPLICATION_JSON)
              .content(malformedJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 415 for unsupported media type")
    void shouldReturnUnsupportedMediaTypeForWrongContentType() throws Exception {
      // Given
      String requestBody = """
          {
            "tenantId": "tenant-001"
          }
          """;

      // When & Then
      mockMvc.perform(post(API_V1_SWIFT_ADAPTERS)
              .contentType(MediaType.TEXT_PLAIN)
              .content(requestBody))
          .andExpect(status().isUnsupportedMediaType());
    }
  }
}
