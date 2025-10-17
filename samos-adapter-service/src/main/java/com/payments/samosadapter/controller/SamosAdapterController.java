package com.payments.samosadapter.controller;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.samosadapter.domain.SamosAdapter;
import com.payments.samosadapter.service.SamosAdapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SAMOS Adapter REST Controller
 *
 * <p>REST API for managing SAMOS adapter configurations and operations. Provides endpoints for
 * adapter lifecycle management and configuration.
 */
@RestController
@RequestMapping("/api/v1/adapters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SAMOS Adapter", description = "SAMOS clearing adapter management API")
public class SamosAdapterController {

  private final SamosAdapterService samosAdapterService;

  /** Create SAMOS adapter */
  @PostMapping
  @Operation(
      summary = "Create SAMOS adapter",
      description = "Create a new SAMOS adapter configuration")
  public ResponseEntity<SamosAdapter> createAdapter(
      @Valid @RequestBody CreateSamosAdapterRequest request,
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId,
      @Parameter(description = "Business Unit ID")
          @RequestHeader(value = "X-Business-Unit-Id", required = false)
          UUID businessUnitId,
      @Parameter(description = "User ID") @RequestHeader("X-User-Id") String userId) {

    log.info("Creating SAMOS adapter: {} for tenant: {}", request.getAdapterName(), tenantId);

    TenantContext tenantContext =
        TenantContext.builder()
            .tenantId(tenantId != null ? tenantId.toString() : null)
            .businessUnitId(businessUnitId != null ? businessUnitId.toString() : null)
            .build();

    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    SamosAdapter adapter =
        samosAdapterService
            .createAdapter(
                adapterId, tenantContext, request.getAdapterName(), request.getEndpoint(), userId)
            .join();

    return ResponseEntity.status(HttpStatus.CREATED).body(adapter);
  }

  /** Get SAMOS adapter by ID */
  @GetMapping("/{adapterId}")
  @Operation(summary = "Get SAMOS adapter", description = "Get SAMOS adapter by ID")
  public ResponseEntity<SamosAdapter> getAdapter(
      @Parameter(description = "Adapter ID") @PathVariable String adapterId) {

    log.info("Getting SAMOS adapter: {}", adapterId);

    return samosAdapterService
        .getAdapter(ClearingAdapterId.of(adapterId))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** Get SAMOS adapter by tenant and name */
  @GetMapping("/by-name/{adapterName}")
  @Operation(
      summary = "Get SAMOS adapter by name",
      description = "Get SAMOS adapter by tenant ID and adapter name")
  public ResponseEntity<SamosAdapter> getAdapterByName(
      @Parameter(description = "Adapter name") @PathVariable String adapterName,
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info("Getting SAMOS adapter by name: {} for tenant: {}", adapterName, tenantId);

    return samosAdapterService
        .getAdapterByTenantAndName(tenantId, adapterName)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** Get active SAMOS adapters for tenant */
  @GetMapping("/active")
  @Operation(
      summary = "Get active SAMOS adapters",
      description = "Get all active SAMOS adapters for tenant")
  public ResponseEntity<List<SamosAdapter>> getActiveAdapters(
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info("Getting active SAMOS adapters for tenant: {}", tenantId);

    List<SamosAdapter> adapters = samosAdapterService.getActiveAdapters(tenantId);
    return ResponseEntity.ok(adapters);
  }

  /** Get SAMOS adapters by business unit */
  @GetMapping("/by-business-unit/{businessUnitId}")
  @Operation(
      summary = "Get SAMOS adapters by business unit",
      description = "Get SAMOS adapters by tenant and business unit")
  public ResponseEntity<List<SamosAdapter>> getAdaptersByBusinessUnit(
      @Parameter(description = "Business Unit ID") @PathVariable UUID businessUnitId,
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info(
        "Getting SAMOS adapters for business unit: {} and tenant: {}", businessUnitId, tenantId);

    List<SamosAdapter> adapters =
        samosAdapterService.getAdaptersByTenantAndBusinessUnit(tenantId, businessUnitId);
    return ResponseEntity.ok(adapters);
  }

  /** Update SAMOS adapter configuration */
  @PutMapping("/{adapterId}/configuration")
  @Operation(
      summary = "Update SAMOS adapter configuration",
      description = "Update SAMOS adapter configuration")
  public ResponseEntity<SamosAdapter> updateAdapterConfiguration(
      @Parameter(description = "Adapter ID") @PathVariable String adapterId,
      @Valid @RequestBody UpdateSamosAdapterConfigurationRequest request,
      @Parameter(description = "User ID") @RequestHeader("X-User-Id") String userId) {

    log.info("Updating SAMOS adapter configuration: {}", adapterId);

    SamosAdapter adapter =
        samosAdapterService
            .updateAdapterConfiguration(
                ClearingAdapterId.of(adapterId),
                request.getEndpoint(),
                request.getApiVersion(),
                request.getTimeoutSeconds(),
                request.getRetryAttempts(),
                request.getEncryptionEnabled(),
                request.getCertificatePath(),
                request.getCertificatePassword(),
                userId)
            .join();

    return ResponseEntity.ok(adapter);
  }

  /** Activate SAMOS adapter */
  @PostMapping("/{adapterId}/activate")
  @Operation(summary = "Activate SAMOS adapter", description = "Activate SAMOS adapter")
  public ResponseEntity<SamosAdapter> activateAdapter(
      @Parameter(description = "Adapter ID") @PathVariable String adapterId,
      @Parameter(description = "User ID") @RequestHeader("X-User-Id") String userId) {

    log.info("Activating SAMOS adapter: {}", adapterId);

    SamosAdapter adapter =
        samosAdapterService.activateAdapter(ClearingAdapterId.of(adapterId), userId).join();
    return ResponseEntity.ok(adapter);
  }

  /** Deactivate SAMOS adapter */
  @PostMapping("/{adapterId}/deactivate")
  @Operation(summary = "Deactivate SAMOS adapter", description = "Deactivate SAMOS adapter")
  public ResponseEntity<SamosAdapter> deactivateAdapter(
      @Parameter(description = "Adapter ID") @PathVariable String adapterId,
      @Valid @RequestBody DeactivateSamosAdapterRequest request,
      @Parameter(description = "User ID") @RequestHeader("X-User-Id") String userId) {

    log.info("Deactivating SAMOS adapter: {} - Reason: {}", adapterId, request.getReason());

    SamosAdapter adapter =
        samosAdapterService
            .deactivateAdapter(ClearingAdapterId.of(adapterId), request.getReason(), userId)
            .join();
    return ResponseEntity.ok(adapter);
  }

  /** Check if SAMOS adapter is active */
  @GetMapping("/{adapterId}/status")
  @Operation(summary = "Get SAMOS adapter status", description = "Check if SAMOS adapter is active")
  public ResponseEntity<AdapterStatusResponse> getAdapterStatus(
      @Parameter(description = "Adapter ID") @PathVariable String adapterId) {

    log.info("Getting SAMOS adapter status: {}", adapterId);

    boolean isActive = samosAdapterService.isAdapterActive(ClearingAdapterId.of(adapterId));
    AdapterStatusResponse response = new AdapterStatusResponse(adapterId, isActive);

    return ResponseEntity.ok(response);
  }

  /** Get adapter count for tenant */
  @GetMapping("/count")
  @Operation(summary = "Get adapter count", description = "Get active adapter count for tenant")
  public ResponseEntity<AdapterCountResponse> getAdapterCount(
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info("Getting SAMOS adapter count for tenant: {}", tenantId);

    long count = samosAdapterService.getActiveAdapterCount(tenantId);
    AdapterCountResponse response = new AdapterCountResponse(tenantId, count);

    return ResponseEntity.ok(response);
  }

  /** Validate SAMOS adapter configuration */
  @GetMapping("/{adapterId}/validate")
  @Operation(
      summary = "Validate adapter configuration",
      description = "Validate SAMOS adapter configuration")
  public ResponseEntity<ValidationResponse> validateAdapterConfiguration(
      @Parameter(description = "Adapter ID") @PathVariable String adapterId) {

    log.info("Validating SAMOS adapter configuration: {}", adapterId);

    boolean isValid =
        samosAdapterService.validateAdapterConfiguration(ClearingAdapterId.of(adapterId));
    ValidationResponse response = new ValidationResponse(adapterId, isValid);

    return ResponseEntity.ok(response);
  }

  // Request/Response DTOs
  public static class CreateSamosAdapterRequest {
    private String adapterName;
    private String endpoint;

    // Getters and setters
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
  }

  public static class UpdateSamosAdapterConfigurationRequest {
    private String endpoint;
    private String apiVersion;
    private Integer timeoutSeconds;
    private Integer retryAttempts;
    private Boolean encryptionEnabled;
    private String certificatePath;
    private String certificatePassword;

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

    public String getCertificatePath() {
      return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
      this.certificatePath = certificatePath;
    }

    public String getCertificatePassword() {
      return certificatePassword;
    }

    public void setCertificatePassword(String certificatePassword) {
      this.certificatePassword = certificatePassword;
    }
  }

  public static class DeactivateSamosAdapterRequest {
    private String reason;

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }
  }

  public static class AdapterStatusResponse {
    private String adapterId;
    private boolean active;

    public AdapterStatusResponse(String adapterId, boolean active) {
      this.adapterId = adapterId;
      this.active = active;
    }

    public String getAdapterId() {
      return adapterId;
    }

    public boolean isActive() {
      return active;
    }
  }

  public static class AdapterCountResponse {
    private UUID tenantId;
    private long count;

    public AdapterCountResponse(UUID tenantId, long count) {
      this.tenantId = tenantId;
      this.count = count;
    }

    public UUID getTenantId() {
      return tenantId;
    }

    public long getCount() {
      return count;
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
