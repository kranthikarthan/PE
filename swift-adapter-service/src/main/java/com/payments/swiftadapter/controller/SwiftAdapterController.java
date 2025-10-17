package com.payments.swiftadapter.controller;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.swiftadapter.domain.SwiftAdapter;
import com.payments.swiftadapter.service.SwiftAdapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for managing SWIFT adapter configurations */
@RestController
@RequestMapping("/api/v1/swift-adapters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SWIFT Adapter", description = "SWIFT adapter management operations")
public class SwiftAdapterController {

  private final SwiftAdapterService swiftAdapterService;

  @PostMapping
  @Operation(
      summary = "Create SWIFT adapter",
      description = "Create a new SWIFT adapter configuration")
  public ResponseEntity<SwiftAdapter> createAdapter(
      @Valid @RequestBody CreateSwiftAdapterRequest request) {
    log.info("Creating SWIFT adapter: {}", request.getAdapterName());

    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    TenantContext tenantContext =
        TenantContext.of(
            request.getTenantId(),
            request.getTenantName(),
            request.getBusinessUnitId(),
            request.getBusinessUnitName());

    SwiftAdapter adapter =
        swiftAdapterService
            .createAdapter(
                adapterId,
                tenantContext,
                request.getAdapterName(),
                request.getEndpoint(),
                request.getCreatedBy())
            .join();

    return ResponseEntity.status(HttpStatus.CREATED).body(adapter);
  }

  @GetMapping("/{adapterId}")
  @Operation(summary = "Get SWIFT adapter", description = "Get SWIFT adapter by ID")
  public ResponseEntity<SwiftAdapter> getAdapter(@PathVariable String adapterId) {
    log.info("Getting SWIFT adapter: {}", adapterId);

    Optional<SwiftAdapter> adapter = swiftAdapterService.findById(ClearingAdapterId.of(adapterId));
    return adapter.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{adapterId}/configuration")
  @Operation(
      summary = "Update adapter configuration",
      description = "Update SWIFT adapter configuration")
  public ResponseEntity<SwiftAdapter> updateAdapterConfiguration(
      @PathVariable String adapterId,
      @Valid @RequestBody UpdateSwiftAdapterConfigurationRequest request) {
    log.info("Updating SWIFT adapter configuration: {}", adapterId);

    SwiftAdapter adapter =
        swiftAdapterService.updateAdapterConfiguration(
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

  @PostMapping("/{adapterId}/activate")
  @Operation(summary = "Activate adapter", description = "Activate SWIFT adapter")
  public ResponseEntity<SwiftAdapter> activateAdapter(
      @PathVariable String adapterId, @RequestBody ActivateSwiftAdapterRequest request) {
    log.info("Activating SWIFT adapter: {}", adapterId);

    SwiftAdapter adapter =
        swiftAdapterService.activateAdapter(
            ClearingAdapterId.of(adapterId), request.getActivatedBy());

    return ResponseEntity.ok(adapter);
  }

  @PostMapping("/{adapterId}/deactivate")
  @Operation(summary = "Deactivate adapter", description = "Deactivate SWIFT adapter")
  public ResponseEntity<SwiftAdapter> deactivateAdapter(
      @PathVariable String adapterId, @RequestBody DeactivateSwiftAdapterRequest request) {
    log.info("Deactivating SWIFT adapter: {}", adapterId);

    SwiftAdapter adapter =
        swiftAdapterService.deactivateAdapter(
            ClearingAdapterId.of(adapterId), request.getReason(), request.getDeactivatedBy());

    return ResponseEntity.ok(adapter);
  }

  @GetMapping("/tenant/{tenantId}")
  @Operation(
      summary = "Get adapters by tenant",
      description = "Get all SWIFT adapters for a tenant")
  public ResponseEntity<List<SwiftAdapter>> getAdaptersByTenant(
      @PathVariable String tenantId, @RequestParam(required = false) String businessUnitId) {
    log.info("Getting SWIFT adapters for tenant: {}", tenantId);

    List<SwiftAdapter> adapters = swiftAdapterService.getAdaptersByTenant(tenantId, businessUnitId);
    return ResponseEntity.ok(adapters);
  }

  @GetMapping("/tenant/{tenantId}/active")
  @Operation(
      summary = "Get active adapters by tenant",
      description = "Get active SWIFT adapters for a tenant")
  public ResponseEntity<List<SwiftAdapter>> getActiveAdaptersByTenant(
      @PathVariable String tenantId) {
    log.info("Getting active SWIFT adapters for tenant: {}", tenantId);

    List<SwiftAdapter> adapters = swiftAdapterService.getActiveAdaptersByTenant(tenantId);
    return ResponseEntity.ok(adapters);
  }

  @GetMapping
  @Operation(summary = "Get all adapters", description = "Get all SWIFT adapters")
  public ResponseEntity<List<SwiftAdapter>> getAllAdapters() {
    log.info("Getting all SWIFT adapters");

    List<SwiftAdapter> adapters = swiftAdapterService.getAllAdapters();
    return ResponseEntity.ok(adapters);
  }

  @DeleteMapping("/{adapterId}")
  @Operation(summary = "Delete adapter", description = "Delete SWIFT adapter")
  public ResponseEntity<Void> deleteAdapter(@PathVariable String adapterId) {
    log.info("Deleting SWIFT adapter: {}", adapterId);

    swiftAdapterService.deleteAdapter(ClearingAdapterId.of(adapterId));
    return ResponseEntity.noContent().build();
  }

  // DTOs
  public static class CreateSwiftAdapterRequest {
    private String tenantId;
    private String tenantName;
    private String businessUnitId;
    private String businessUnitName;
    private String adapterName;
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

  public static class UpdateSwiftAdapterConfigurationRequest {
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

  public static class ActivateSwiftAdapterRequest {
    private String activatedBy;

    public String getActivatedBy() {
      return activatedBy;
    }

    public void setActivatedBy(String activatedBy) {
      this.activatedBy = activatedBy;
    }
  }

  public static class DeactivateSwiftAdapterRequest {
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
}
