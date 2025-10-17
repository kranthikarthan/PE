package com.payments.payshapadapter.controller;

import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.payshapadapter.domain.PayShapAdapter;
import com.payments.payshapadapter.service.PayShapAdapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for PayShap Adapter management */
@RestController
@RequestMapping("/api/v1/payshap/adapters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PayShap Adapter", description = "PayShap adapter management operations")
public class PayShapAdapterController {

  private final PayShapAdapterService payShapAdapterService;

  /** Create a new PayShap adapter */
  @PostMapping
  @Operation(
      summary = "Create PayShap adapter",
      description = "Create a new PayShap adapter for instant P2P payments")
  public ResponseEntity<PayShapAdapter> createPayShapAdapter(
      @Valid @RequestBody CreatePayShapAdapterRequest request) {
    log.info("Creating PayShap adapter: {}", request.getAdapterName());

    ClearingAdapterId adapterId = ClearingAdapterId.generate();
    TenantContext tenantContext =
        TenantContext.of(
            request.getTenantId(), request.getTenantName(),
            request.getBusinessUnitId(), request.getBusinessUnitName());

    PayShapAdapter adapter =
        payShapAdapterService
            .createAdapter(
                adapterId,
                tenantContext,
                request.getAdapterName(),
                request.getEndpoint(),
                request.getCreatedBy())
            .join();

    return ResponseEntity.ok(adapter);
  }

  /** Update PayShap adapter configuration */
  @PutMapping("/{adapterId}")
  @Operation(
      summary = "Update PayShap adapter configuration",
      description = "Update PayShap adapter configuration settings")
  public ResponseEntity<PayShapAdapter> updatePayShapAdapterConfiguration(
      @PathVariable String adapterId,
      @Valid @RequestBody UpdatePayShapAdapterConfigurationRequest request) {
    log.info("Updating PayShap adapter configuration: {}", adapterId);

    PayShapAdapter adapter =
        payShapAdapterService.updateAdapterConfiguration(
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

  /** Activate PayShap adapter */
  @PostMapping("/{adapterId}/activate")
  @Operation(
      summary = "Activate PayShap adapter",
      description = "Activate a PayShap adapter for processing")
  public ResponseEntity<PayShapAdapter> activatePayShapAdapter(
      @PathVariable String adapterId, @Valid @RequestBody ActivatePayShapAdapterRequest request) {
    log.info("Activating PayShap adapter: {}", adapterId);

    PayShapAdapter adapter =
        payShapAdapterService.activateAdapter(
            ClearingAdapterId.of(adapterId), request.getActivatedBy());

    return ResponseEntity.ok(adapter);
  }

  /** Deactivate PayShap adapter */
  @PostMapping("/{adapterId}/deactivate")
  @Operation(summary = "Deactivate PayShap adapter", description = "Deactivate a PayShap adapter")
  public ResponseEntity<PayShapAdapter> deactivatePayShapAdapter(
      @PathVariable String adapterId, @Valid @RequestBody DeactivatePayShapAdapterRequest request) {
    log.info("Deactivating PayShap adapter: {} - Reason: {}", adapterId, request.getReason());

    PayShapAdapter adapter =
        payShapAdapterService.deactivateAdapter(
            ClearingAdapterId.of(adapterId), request.getReason(), request.getDeactivatedBy());

    return ResponseEntity.ok(adapter);
  }

  /** Get PayShap adapter by ID */
  @GetMapping("/{adapterId}")
  @Operation(summary = "Get PayShap adapter", description = "Get PayShap adapter by ID")
  public ResponseEntity<PayShapAdapter> getPayShapAdapter(@PathVariable String adapterId) {
    Optional<PayShapAdapter> adapter =
        payShapAdapterService.findById(ClearingAdapterId.of(adapterId));
    return adapter.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /** Get PayShap adapters by tenant */
  @GetMapping("/tenant/{tenantId}/business-unit/{businessUnitId}")
  @Operation(
      summary = "Get PayShap adapters by tenant",
      description = "Get PayShap adapters for a specific tenant and business unit")
  public ResponseEntity<List<PayShapAdapter>> getPayShapAdaptersByTenant(
      @PathVariable String tenantId, @PathVariable String businessUnitId) {
    List<PayShapAdapter> adapters =
        payShapAdapterService.getAdaptersByTenant(tenantId, businessUnitId);
    return ResponseEntity.ok(adapters);
  }

  /** Get active PayShap adapters by tenant */
  @GetMapping("/tenant/{tenantId}/active")
  @Operation(
      summary = "Get active PayShap adapters",
      description = "Get active PayShap adapters for a specific tenant")
  public ResponseEntity<List<PayShapAdapter>> getActivePayShapAdaptersByTenant(
      @PathVariable String tenantId) {
    List<PayShapAdapter> adapters = payShapAdapterService.getActiveAdaptersByTenant(tenantId);
    return ResponseEntity.ok(adapters);
  }

  /** Get PayShap adapter status */
  @GetMapping("/{adapterId}/status")
  @Operation(
      summary = "Get PayShap adapter status",
      description = "Get PayShap adapter operational status")
  public ResponseEntity<AdapterStatusResponse> getPayShapAdapterStatus(
      @PathVariable String adapterId) {
    boolean isActive = payShapAdapterService.isAdapterActive(ClearingAdapterId.of(adapterId));
    boolean isValid =
        payShapAdapterService.validateAdapterConfiguration(ClearingAdapterId.of(adapterId));

    AdapterStatusResponse response = new AdapterStatusResponse();
    response.setActive(isActive);
    response.setValid(isValid);

    return ResponseEntity.ok(response);
  }

  /** Validate PayShap adapter configuration */
  @PostMapping("/{adapterId}/validate")
  @Operation(
      summary = "Validate PayShap adapter configuration",
      description = "Validate PayShap adapter configuration")
  public ResponseEntity<ValidationResponse> validatePayShapAdapterConfiguration(
      @PathVariable String adapterId) {
    boolean isValid =
        payShapAdapterService.validateAdapterConfiguration(ClearingAdapterId.of(adapterId));

    ValidationResponse response = new ValidationResponse();
    response.setValid(isValid);

    return ResponseEntity.ok(response);
  }

  // DTOs
  @lombok.Data
  public static class CreatePayShapAdapterRequest {
    private String tenantId;
    private String tenantName;
    private String businessUnitId;
    private String businessUnitName;
    private String adapterName;
    private ClearingNetwork network;
    private String endpoint;
    private String createdBy;
  }

  @lombok.Data
  public static class UpdatePayShapAdapterConfigurationRequest {
    private String endpoint;
    private String apiVersion;
    private Integer timeoutSeconds;
    private Integer retryAttempts;
    private Boolean encryptionEnabled;
    private Integer batchSize;
    private String processingWindowStart;
    private String processingWindowEnd;
    private String updatedBy;
  }

  @lombok.Data
  public static class ActivatePayShapAdapterRequest {
    private String activatedBy;
  }

  @lombok.Data
  public static class DeactivatePayShapAdapterRequest {
    private String reason;
    private String deactivatedBy;
  }

  @lombok.Data
  public static class AdapterStatusResponse {
    private boolean active;
    private boolean valid;
  }

  @lombok.Data
  public static class ValidationResponse {
    private boolean valid;
  }
}
