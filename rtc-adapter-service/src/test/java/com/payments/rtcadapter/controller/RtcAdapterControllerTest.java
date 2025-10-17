package com.payments.rtcadapter.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.rtcadapter.domain.RtcAdapter;
import com.payments.rtcadapter.service.RtcAdapterService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for RtcAdapterController */
@ExtendWith(MockitoExtension.class)
class RtcAdapterControllerTest {

  @Mock private RtcAdapterService rtcAdapterService;

  @InjectMocks private RtcAdapterController rtcAdapterController;

  private ClearingAdapterId adapterId;
  private TenantContext tenantContext;
  private RtcAdapter rtcAdapter;
  private RtcAdapterController.CreateRtcAdapterRequest createRequest;
  private RtcAdapterController.UpdateRtcAdapterConfigurationRequest updateRequest;

  @BeforeEach
  void setUp() {
    adapterId = ClearingAdapterId.generate();
    tenantContext = TenantContext.of("tenant-123", "Tenant Name", "bu-456", "Business Unit");

    rtcAdapter =
        RtcAdapter.builder()
            .id(adapterId)
            .tenantContext(tenantContext)
            .adapterName("Test RTC Adapter")
            .network(ClearingNetwork.RTC)
            .status(AdapterOperationalStatus.ACTIVE)
            .endpoint("https://rtc.test.com/api")
            .apiVersion("1.0")
            .timeoutSeconds(10)
            .retryAttempts(3)
            .encryptionEnabled(true)
            .batchSize(100)
            .amountLimit(5000.00)
            .currencyCode("ZAR")
            .build();

    createRequest = new RtcAdapterController.CreateRtcAdapterRequest();
    createRequest.setTenantId("tenant-123");
    createRequest.setTenantName("Tenant Name");
    createRequest.setBusinessUnitId("bu-456");
    createRequest.setBusinessUnitName("Business Unit");
    createRequest.setAdapterName("Test RTC Adapter");
    createRequest.setNetwork(ClearingNetwork.RTC);
    createRequest.setEndpoint("https://rtc.test.com/api");
    createRequest.setCreatedBy("test-user");

    updateRequest = new RtcAdapterController.UpdateRtcAdapterConfigurationRequest();
    updateRequest.setEndpoint("https://rtc.new.com/api");
    updateRequest.setApiVersion("2.0");
    updateRequest.setTimeoutSeconds(15);
    updateRequest.setRetryAttempts(5);
    updateRequest.setEncryptionEnabled(true);
    updateRequest.setBatchSize(200);
    updateRequest.setProcessingWindowStart("08:00");
    updateRequest.setProcessingWindowEnd("18:00");
    updateRequest.setUpdatedBy("admin");
  }

  @Test
  void createRtcAdapter_ShouldReturnCreatedAdapter_WhenValidRequest() {
    // Given
    when(rtcAdapterService.createAdapter(
            any(ClearingAdapterId.class),
            any(TenantContext.class),
            any(String.class),
            any(String.class),
            any(String.class)))
        .thenReturn(rtcAdapter);

    // When
    ResponseEntity<RtcAdapter> response = rtcAdapterController.createRtcAdapter(createRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Test RTC Adapter", response.getBody().getAdapterName());
    verify(rtcAdapterService)
        .createAdapter(
            any(ClearingAdapterId.class),
            any(TenantContext.class),
            any(String.class),
            any(String.class),
            any(String.class));
  }

  @Test
  void updateRtcAdapterConfiguration_ShouldReturnUpdatedAdapter_WhenValidRequest() {
    // Given
    when(rtcAdapterService.updateAdapterConfiguration(
            any(ClearingAdapterId.class),
            any(String.class),
            any(String.class),
            any(Integer.class),
            any(Integer.class),
            any(Boolean.class),
            any(Integer.class),
            any(String.class),
            any(String.class),
            any(String.class)))
        .thenReturn(rtcAdapter);

    // When
    ResponseEntity<RtcAdapter> response =
        rtcAdapterController.updateRtcAdapterConfiguration(adapterId.toString(), updateRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(rtcAdapterService)
        .updateAdapterConfiguration(
            any(ClearingAdapterId.class),
            any(String.class),
            any(String.class),
            any(Integer.class),
            any(Integer.class),
            any(Boolean.class),
            any(Integer.class),
            any(String.class),
            any(String.class),
            any(String.class));
  }

  @Test
  void activateRtcAdapter_ShouldReturnActivatedAdapter_WhenValidRequest() {
    // Given
    RtcAdapterController.ActivateRtcAdapterRequest activateRequest =
        new RtcAdapterController.ActivateRtcAdapterRequest();
    activateRequest.setActivatedBy("admin");

    when(rtcAdapterService.activateAdapter(any(ClearingAdapterId.class), any(String.class)))
        .thenReturn(rtcAdapter);

    // When
    ResponseEntity<RtcAdapter> response =
        rtcAdapterController.activateRtcAdapter(adapterId.toString(), activateRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(rtcAdapterService).activateAdapter(any(ClearingAdapterId.class), any(String.class));
  }

  @Test
  void deactivateRtcAdapter_ShouldReturnDeactivatedAdapter_WhenValidRequest() {
    // Given
    RtcAdapterController.DeactivateRtcAdapterRequest deactivateRequest =
        new RtcAdapterController.DeactivateRtcAdapterRequest();
    deactivateRequest.setReason("Maintenance");
    deactivateRequest.setDeactivatedBy("admin");

    when(rtcAdapterService.deactivateAdapter(
            any(ClearingAdapterId.class), any(String.class), any(String.class)))
        .thenReturn(rtcAdapter);

    // When
    ResponseEntity<RtcAdapter> response =
        rtcAdapterController.deactivateRtcAdapter(adapterId.toString(), deactivateRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(rtcAdapterService)
        .deactivateAdapter(any(ClearingAdapterId.class), any(String.class), any(String.class));
  }

  @Test
  void getRtcAdapter_ShouldReturnAdapter_WhenAdapterExists() {
    // Given
    when(rtcAdapterService.findById(any(ClearingAdapterId.class)))
        .thenReturn(Optional.of(rtcAdapter));

    // When
    ResponseEntity<RtcAdapter> response = rtcAdapterController.getRtcAdapter(adapterId.toString());

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(adapterId, response.getBody().getId());
    verify(rtcAdapterService).findById(any(ClearingAdapterId.class));
  }

  @Test
  void getRtcAdapter_ShouldReturnNotFound_WhenAdapterNotExists() {
    // Given
    when(rtcAdapterService.findById(any(ClearingAdapterId.class))).thenReturn(Optional.empty());

    // When
    ResponseEntity<RtcAdapter> response = rtcAdapterController.getRtcAdapter(adapterId.toString());

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
    verify(rtcAdapterService).findById(any(ClearingAdapterId.class));
  }

  @Test
  void getRtcAdaptersByTenant_ShouldReturnAdapters_WhenTenantExists() {
    // Given
    List<RtcAdapter> adapters = List.of(rtcAdapter);
    when(rtcAdapterService.getAdaptersByTenant(any(String.class), any(String.class)))
        .thenReturn(adapters);

    // When
    ResponseEntity<List<RtcAdapter>> response =
        rtcAdapterController.getRtcAdaptersByTenant("tenant-123", "bu-456");

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(rtcAdapterService).getAdaptersByTenant("tenant-123", "bu-456");
  }

  @Test
  void getActiveRtcAdaptersByTenant_ShouldReturnActiveAdapters_WhenTenantExists() {
    // Given
    List<RtcAdapter> adapters = List.of(rtcAdapter);
    when(rtcAdapterService.getActiveAdaptersByTenant(any(String.class))).thenReturn(adapters);

    // When
    ResponseEntity<List<RtcAdapter>> response =
        rtcAdapterController.getActiveRtcAdaptersByTenant("tenant-123");

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(rtcAdapterService).getActiveAdaptersByTenant("tenant-123");
  }

  @Test
  void getRtcAdapterStatus_ShouldReturnStatus_WhenAdapterExists() {
    // Given
    when(rtcAdapterService.isAdapterActive(any(ClearingAdapterId.class))).thenReturn(true);
    when(rtcAdapterService.validateAdapterConfiguration(any(ClearingAdapterId.class)))
        .thenReturn(true);

    // When
    ResponseEntity<RtcAdapterController.AdapterStatusResponse> response =
        rtcAdapterController.getRtcAdapterStatus(adapterId.toString());

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isActive());
    assertTrue(response.getBody().isValid());
    verify(rtcAdapterService).isAdapterActive(any(ClearingAdapterId.class));
    verify(rtcAdapterService).validateAdapterConfiguration(any(ClearingAdapterId.class));
  }

  @Test
  void validateRtcAdapterConfiguration_ShouldReturnValidationResult_WhenAdapterExists() {
    // Given
    when(rtcAdapterService.validateAdapterConfiguration(any(ClearingAdapterId.class)))
        .thenReturn(true);

    // When
    ResponseEntity<RtcAdapterController.ValidationResponse> response =
        rtcAdapterController.validateRtcAdapterConfiguration(adapterId.toString());

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isValid());
    verify(rtcAdapterService).validateAdapterConfiguration(any(ClearingAdapterId.class));
  }

  @Test
  void validateRtcAdapterConfiguration_ShouldReturnInvalidResult_WhenAdapterInvalid() {
    // Given
    when(rtcAdapterService.validateAdapterConfiguration(any(ClearingAdapterId.class)))
        .thenReturn(false);

    // When
    ResponseEntity<RtcAdapterController.ValidationResponse> response =
        rtcAdapterController.validateRtcAdapterConfiguration(adapterId.toString());

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isValid());
    verify(rtcAdapterService).validateAdapterConfiguration(any(ClearingAdapterId.class));
  }
}
