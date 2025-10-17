package com.payments.rtcadapter.controller;

import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.rtcadapter.domain.RtcAdapter;
import com.payments.rtcadapter.service.RtcAdapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for managing RTC Adapter configurations */
@RestController
@RequestMapping("/api/v1/rtc/adapters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RTC Adapter", description = "RTC Adapter management endpoints")
public class RtcAdapterController {

  private final RtcAdapterService rtcAdapterService;

  /** Create a new RTC adapter */
  @PostMapping
  @Operation(summary = "Create RTC adapter", description = "Create a new RTC adapter configuration")
  public ResponseEntity<RtcAdapter> createRtcAdapter(
      @Valid @RequestBody CreateRtcAdapterRequest request) {

    log.info("Creating RTC adapter: {}", request.getAdapterName());

    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    TenantContext tenantContext =
        TenantContext.of(
            request.getTenantId(),
            request.getTenantName(),
            request.getBusinessUnitId(),
            request.getBusinessUnitName());

    RtcAdapter adapter =
        rtcAdapterService.createAdapter(
            adapterId,
            tenantContext,
            request.getAdapterName(),
            request.getEndpoint(),
            request.getCreatedBy()).join();

    return ResponseEntity.ok(adapter);
  }

  /** Update RTC adapter configuration */
  @PutMapping("/{adapterId}")
  @Operation(
      summary = "Update RTC adapter configuration",
      description = "Update RTC adapter configuration")
  public ResponseEntity<RtcAdapter> updateRtcAdapterConfiguration(
      @PathVariable @Parameter(description = "Adapter ID") String adapterId,
      @Valid @RequestBody UpdateRtcAdapterConfigurationRequest request) {

    log.info("Updating RTC adapter configuration: {}", adapterId);

    RtcAdapter adapter =
        rtcAdapterService.updateAdapterConfiguration(
            ClearingAdapterId.of(adapterId),
            request.getEndpoint(),
            request.getApiVersion(),
            request.getTimeoutSeconds(),
            request.getRetryAttempts(),
            request.getEncryptionEnabled(),
            request.getBatchSize(),
            request.getProcessingWindowStart(),
            request.getProcessingWindowEnd(),
            request.getUpdatedBy());

    return ResponseEntity.ok(adapter);
  }

  /** Activate RTC adapter */
  @PostMapping("/{adapterId}/activate")
  @Operation(summary = "Activate RTC adapter", description = "Activate RTC adapter")
  public ResponseEntity<RtcAdapter> activateRtcAdapter(
      @PathVariable @Parameter(description = "Adapter ID") String adapterId,
      @RequestBody ActivateRtcAdapterRequest request) {

    log.info("Activating RTC adapter: {}", adapterId);

    RtcAdapter adapter =
        rtcAdapterService.activateAdapter(
            ClearingAdapterId.of(adapterId), request.getActivatedBy());

    return ResponseEntity.ok(adapter);
  }

  /** Deactivate RTC adapter */
  @PostMapping("/{adapterId}/deactivate")
  @Operation(summary = "Deactivate RTC adapter", description = "Deactivate RTC adapter")
  public ResponseEntity<RtcAdapter> deactivateRtcAdapter(
      @PathVariable @Parameter(description = "Adapter ID") String adapterId,
      @RequestBody DeactivateRtcAdapterRequest request) {

    log.info("Deactivating RTC adapter: {}", adapterId);

    RtcAdapter adapter =
        rtcAdapterService.deactivateAdapter(
            ClearingAdapterId.of(adapterId), request.getReason(), request.getDeactivatedBy());

    return ResponseEntity.ok(adapter);
  }

  /** Get RTC adapter by ID */
  @GetMapping("/{adapterId}")
  @Operation(summary = "Get RTC adapter", description = "Get RTC adapter by ID")
  public ResponseEntity<RtcAdapter> getRtcAdapter(
      @PathVariable @Parameter(description = "Adapter ID") String adapterId) {

    log.info("Getting RTC adapter: {}", adapterId);

    Optional<RtcAdapter> adapter = rtcAdapterService.findById(ClearingAdapterId.of(adapterId));

    return adapter.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /** Get RTC adapters by tenant */
  @GetMapping("/tenant/{tenantId}/business-unit/{businessUnitId}")
  @Operation(
      summary = "Get RTC adapters by tenant",
      description = "Get RTC adapters by tenant and business unit")
  public ResponseEntity<List<RtcAdapter>> getRtcAdaptersByTenant(
      @PathVariable @Parameter(description = "Tenant ID") String tenantId,
      @PathVariable @Parameter(description = "Business Unit ID") String businessUnitId) {

    log.info("Getting RTC adapters for tenant: {} and business unit: {}", tenantId, businessUnitId);

    List<RtcAdapter> adapters = rtcAdapterService.getAdaptersByTenant(tenantId, businessUnitId);
    return ResponseEntity.ok(adapters);
  }

  /** Get active RTC adapters by tenant */
  @GetMapping("/tenant/{tenantId}/active")
  @Operation(summary = "Get active RTC adapters", description = "Get active RTC adapters by tenant")
  public ResponseEntity<List<RtcAdapter>> getActiveRtcAdaptersByTenant(
      @PathVariable @Parameter(description = "Tenant ID") String tenantId) {

    log.info("Getting active RTC adapters for tenant: {}", tenantId);

    List<RtcAdapter> adapters = rtcAdapterService.getActiveAdaptersByTenant(tenantId);
    return ResponseEntity.ok(adapters);
  }

  /** Get RTC adapter status */
  @GetMapping("/{adapterId}/status")
  @Operation(summary = "Get RTC adapter status", description = "Get RTC adapter status")
  public ResponseEntity<AdapterStatusResponse> getRtcAdapterStatus(
      @PathVariable @Parameter(description = "Adapter ID") String adapterId) {

    log.info("Getting RTC adapter status: {}", adapterId);

    boolean isActive = rtcAdapterService.isAdapterActive(ClearingAdapterId.of(adapterId));
    boolean isValid =
        rtcAdapterService.validateAdapterConfiguration(ClearingAdapterId.of(adapterId));

    AdapterStatusResponse status = new AdapterStatusResponse(adapterId, isActive, isValid);
    return ResponseEntity.ok(status);
  }

  /** Validate RTC adapter configuration */
  @PostMapping("/{adapterId}/validate")
  @Operation(
      summary = "Validate RTC adapter configuration",
      description = "Validate RTC adapter configuration")
  public ResponseEntity<ValidationResponse> validateRtcAdapterConfiguration(
      @PathVariable @Parameter(description = "Adapter ID") String adapterId) {

    log.info("Validating RTC adapter configuration: {}", adapterId);

    boolean isValid =
        rtcAdapterService.validateAdapterConfiguration(ClearingAdapterId.of(adapterId));
    ValidationResponse response = new ValidationResponse(adapterId, isValid);

    return ResponseEntity.ok(response);
  }

  // Request DTOs
  public static class CreateRtcAdapterRequest {
    private String tenantId;
    private String tenantName;
    private String businessUnitId;
    private String businessUnitName;
    private String adapterName;
    private ClearingNetwork network;
    private String endpoint;
    private String createdBy;

    // Getters and setters
    public String getTenantId() {
      return tenantId;
    }

    public void setTenantId(String tenantId) {
      this.tenantId = tenantId;
    }

    public String getTenantName() {
      return tenantName;
    }

    public void setTenantName(String tenantName) {
      this.tenantName = tenantName;
    }

    public String getBusinessUnitId() {
      return businessUnitId;
    }

    public void setBusinessUnitId(String businessUnitId) {
      this.businessUnitId = businessUnitId;
    }

    public String getBusinessUnitName() {
      return businessUnitName;
    }

    public void setBusinessUnitName(String businessUnitName) {
      this.businessUnitName = businessUnitName;
    }

    public String getAdapterName() {
      return adapterName;
    }

    public void setAdapterName(String adapterName) {
      this.adapterName = adapterName;
    }

    public ClearingNetwork getNetwork() {
      return network;
    }

    public void setNetwork(ClearingNetwork network) {
      this.network = network;
    }

    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    public String getCreatedBy() {
      return createdBy;
    }

    public void setCreatedBy(String createdBy) {
      this.createdBy = createdBy;
    }
  }

  public static class UpdateRtcAdapterConfigurationRequest {
    private String endpoint;
    private String apiVersion;
    private Integer timeoutSeconds;
    private Integer retryAttempts;
    private Boolean encryptionEnabled;
    private Integer batchSize;
    private String processingWindowStart;
    private String processingWindowEnd;
    private String updatedBy;

    // Getters and setters
    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    public String getApiVersion() {
      return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
      this.apiVersion = apiVersion;
    }

    public Integer getTimeoutSeconds() {
      return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
      this.timeoutSeconds = timeoutSeconds;
    }

    public Integer getRetryAttempts() {
      return retryAttempts;
    }

    public void setRetryAttempts(Integer retryAttempts) {
      this.retryAttempts = retryAttempts;
    }

    public Boolean getEncryptionEnabled() {
      return encryptionEnabled;
    }

    public void setEncryptionEnabled(Boolean encryptionEnabled) {
      this.encryptionEnabled = encryptionEnabled;
    }

    public Integer getBatchSize() {
      return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
      this.batchSize = batchSize;
    }

    public String getProcessingWindowStart() {
      return processingWindowStart;
    }

    public void setProcessingWindowStart(String processingWindowStart) {
      this.processingWindowStart = processingWindowStart;
    }

    public String getProcessingWindowEnd() {
      return processingWindowEnd;
    }

    public void setProcessingWindowEnd(String processingWindowEnd) {
      this.processingWindowEnd = processingWindowEnd;
    }

    public String getUpdatedBy() {
      return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
      this.updatedBy = updatedBy;
    }
  }

  public static class ActivateRtcAdapterRequest {
    private String activatedBy;

    public String getActivatedBy() {
      return activatedBy;
    }

    public void setActivatedBy(String activatedBy) {
      this.activatedBy = activatedBy;
    }
  }

  public static class DeactivateRtcAdapterRequest {
    private String reason;
    private String deactivatedBy;

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }

    public String getDeactivatedBy() {
      return deactivatedBy;
    }

    public void setDeactivatedBy(String deactivatedBy) {
      this.deactivatedBy = deactivatedBy;
    }
  }

  public static class AdapterStatusResponse {
    private String adapterId;
    private boolean active;
    private boolean valid;

    public AdapterStatusResponse(String adapterId, boolean active, boolean valid) {
      this.adapterId = adapterId;
      this.active = active;
      this.valid = valid;
    }

    public String getAdapterId() {
      return adapterId;
    }

    public boolean isActive() {
      return active;
    }

    public boolean isValid() {
      return valid;
    }
  }

  public static class ValidationResponse {
    private String adapterId;
    private boolean valid;

    public ValidationResponse(String adapterId, boolean valid) {
      this.adapterId = adapterId;
      this.valid = valid;
    }

    public String getAdapterId() {
      return adapterId;
    }

    public boolean isValid() {
      return valid;
    }
  }
}
