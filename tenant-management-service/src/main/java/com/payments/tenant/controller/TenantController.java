package com.payments.tenant.controller;

import com.payments.tenant.dto.CreateTenantRequest;
import com.payments.tenant.dto.StatusChangeRequest;
import com.payments.tenant.dto.TenantResponse;
import com.payments.tenant.dto.UpdateTenantRequest;
import com.payments.tenant.entity.TenantEntity;
import com.payments.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Tenant Controller - REST API for tenant management.
 *
 * <p>Endpoints:
 * - POST /tenants - Create tenant (ADMIN)
 * - GET /tenants/{id} - Get tenant by ID (any user)
 * - PUT /tenants/{id} - Update tenant (ADMIN)
 * - DELETE /tenants/{id} - Deactivate tenant (ADMIN)
 * - GET /tenants - List active tenants (any user)
 * - POST /tenants/{id}/activate - Activate tenant (ADMIN)
 * - POST /tenants/{id}/suspend - Suspend tenant (ADMIN)
 *
 * <p>Security:
 * - All endpoints require JWT token (X-Authorization header)
 * - X-Tenant-ID header required for multi-tenancy
 * - Role-based access control (ADMIN required for mutations)
 *
 * <p>Validation:
 * - @Valid annotations on DTOs
 * - TenantValidator called at service layer
 * - Spring Bean Validation for HTTP constraints
 */
@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Management", description = "Manage tenants (create, read, update, delete, lifecycle)")
public class TenantController {

  private final TenantService tenantService;

  /**
   * Create a new tenant.
   *
   * <p>HTTP: POST /tenants
   * Status: 201 Created
   *
   * @param request Tenant creation request
   * @param createdBy Current user (from JWT principal)
   * @return Created tenant response
   */
  @PostMapping
  @Operation(summary = "Create tenant", description = "Create a new tenant (ADMIN only)")
  public ResponseEntity<TenantResponse> createTenant(
      @Valid @RequestBody CreateTenantRequest request,
      @RequestHeader("X-User-ID") String createdBy) {
    log.info("Creating tenant: {}", request.getTenantName());

    TenantEntity entity =
        TenantEntity.builder()
            .tenantName(request.getTenantName())
            .tenantType(request.getTenantType())
            .contactEmail(request.getContactEmail())
            .contactPhone(request.getContactPhone())
            .registrationNumber(request.getRegistrationNumber())
            .taxNumber(request.getTaxNumber())
            .addressLine1(request.getAddressLine1())
            .addressLine2(request.getAddressLine2())
            .city(request.getCity())
            .province(request.getProvince())
            .postalCode(request.getPostalCode())
            .country(request.getCountry())
            .timezone(request.getTimezone())
            .currency(request.getCurrency())
            .build();

    TenantEntity created = tenantService.createTenant(entity, createdBy);
    return ResponseEntity.status(HttpStatus.CREATED).body(TenantResponse.from(created));
  }

  /**
   * Get tenant by ID.
   *
   * <p>HTTP: GET /tenants/{id}
   * Status: 200 OK
   *
   * @param tenantId Tenant identifier
   * @return Tenant response
   */
  @GetMapping("/{tenantId}")
  @Operation(summary = "Get tenant", description = "Get tenant by ID")
  public ResponseEntity<TenantResponse> getTenant(@PathVariable String tenantId) {
    log.debug("Fetching tenant: {}", tenantId);

    TenantEntity tenant = tenantService.getTenant(tenantId);
    return ResponseEntity.ok(TenantResponse.from(tenant));
  }

  /**
   * List active tenants with pagination.
   *
   * <p>HTTP: GET /tenants?page=0&size=20
   * Status: 200 OK
   *
   * @param pageable Pagination (page, size, sort)
   * @return Page of tenant responses
   */
  @GetMapping
  @Operation(summary = "List active tenants", description = "Get paginated list of active tenants")
  public ResponseEntity<Page<TenantResponse>> listActiveTenants(Pageable pageable) {
    log.debug("Listing active tenants, page: {}", pageable.getPageNumber());

    Page<TenantEntity> tenants = tenantService.listActiveTenants(pageable);
    return ResponseEntity.ok(tenants.map(TenantResponse::from));
  }

  /**
   * Update tenant information.
   *
   * <p>HTTP: PUT /tenants/{id}
   * Status: 200 OK
   *
   * <p>Allowed updates: contact info, address, timezone, currency
   * Protected fields: tenantId, status (use activate/suspend endpoints)
   *
   * @param tenantId Tenant identifier
   * @param request Update request
   * @param updatedBy Current user
   * @return Updated tenant response
   */
  @PutMapping("/{tenantId}")
  @Operation(summary = "Update tenant", description = "Update tenant information (ADMIN only)")
  public ResponseEntity<TenantResponse> updateTenant(
      @PathVariable String tenantId,
      @Valid @RequestBody UpdateTenantRequest request,
      @RequestHeader("X-User-ID") String updatedBy) {
    log.info("Updating tenant: {}", tenantId);

    TenantEntity updates = TenantEntity.builder()
        .contactEmail(request.getContactEmail())
        .contactPhone(request.getContactPhone())
        .addressLine1(request.getAddressLine1())
        .addressLine2(request.getAddressLine2())
        .city(request.getCity())
        .province(request.getProvince())
        .postalCode(request.getPostalCode())
        .timezone(request.getTimezone())
        .currency(request.getCurrency())
        .build();

    TenantEntity updated = tenantService.updateTenant(tenantId, updates, updatedBy);
    return ResponseEntity.ok(TenantResponse.from(updated));
  }

  /**
   * Activate tenant (approve from PENDING_APPROVAL).
   *
   * <p>HTTP: POST /tenants/{id}/activate
   * Status: 200 OK
   *
   * @param tenantId Tenant identifier
   * @param activatedBy Current user
   * @return Activated tenant response
   */
  @PostMapping("/{tenantId}/activate")
  @Operation(summary = "Activate tenant", description = "Approve and activate tenant (ADMIN only)")
  public ResponseEntity<TenantResponse> activateTenant(
      @PathVariable String tenantId, @RequestHeader("X-User-ID") String activatedBy) {
    log.info("Activating tenant: {}", tenantId);

    TenantEntity activated = tenantService.activateTenant(tenantId, activatedBy);
    return ResponseEntity.ok(TenantResponse.from(activated));
  }

  /**
   * Suspend tenant (temporary deactivation).
   *
   * <p>HTTP: POST /tenants/{id}/suspend
   * Status: 200 OK
   *
   * @param tenantId Tenant identifier
   * @param suspendedBy Current user
   * @return Suspended tenant response
   */
  @PostMapping("/{tenantId}/suspend")
  @Operation(summary = "Suspend tenant", description = "Temporarily suspend tenant (ADMIN only)")
  public ResponseEntity<TenantResponse> suspendTenant(
      @PathVariable String tenantId, @RequestHeader("X-User-ID") String suspendedBy) {
    log.warn("Suspending tenant: {}", tenantId);

    TenantEntity suspended = tenantService.suspendTenant(tenantId, suspendedBy);
    return ResponseEntity.ok(TenantResponse.from(suspended));
  }

  /**
   * Deactivate tenant (soft delete).
   *
   * <p>HTTP: DELETE /tenants/{id}
   * Status: 200 OK
   *
   * @param tenantId Tenant identifier
   * @param deactivatedBy Current user
   * @return Deactivated tenant response
   */
  @DeleteMapping("/{tenantId}")
  @Operation(summary = "Deactivate tenant", description = "Deactivate tenant (soft delete, ADMIN only)")
  public ResponseEntity<TenantResponse> deactivateTenant(
      @PathVariable String tenantId, @RequestHeader("X-User-ID") String deactivatedBy) {
    log.warn("Deactivating tenant: {}", tenantId);

    TenantEntity deactivated = tenantService.deactivateTenant(tenantId, deactivatedBy);
    return ResponseEntity.ok(TenantResponse.from(deactivated));
  }
}
