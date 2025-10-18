package com.payments.audit.controller;

import com.payments.audit.entity.AuditEventEntity;
import com.payments.audit.service.AuditService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Audit Controller - REST API for compliance audit logs.
 *
 * <p>Endpoints:
 * - GET /api/audit/logs - Get audit logs (paginated)
 * - GET /api/audit/logs/user - Get logs by user
 * - GET /api/audit/logs/action - Get logs by action
 * - GET /api/audit/logs/denied - Get denied access attempts
 * - GET /api/audit/logs/errors - Get error events
 * - GET /api/audit/logs/search - Search by keyword
 * - GET /api/audit/logs/range - Search by time range
 * - GET /api/audit/logs/resource - Search by resource
 * - GET /api/audit/stats - Get audit statistics
 *
 * <p>Security:
 * - All endpoints require JWT token (OAuth2 Resource Server)
 * - All endpoints require X-Tenant-ID header
 * - Admin/Compliance/Auditor roles only
 *
 * <p>Multi-Tenancy:
 * - All queries filtered by X-Tenant-ID header
 * - No cross-tenant data exposure
 * - Enforced at controller and service layer
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "Compliance audit trail APIs (POPIA/FICA/PCI-DSS)")
@Validated
public class AuditController {

  private final AuditService auditService;

  /**
   * Get audit logs for a tenant with pagination.
   *
   * <p>Returns all audit events for the tenant, newest first.
   *
   * @param tenantId the tenant ID from X-Tenant-ID header
   * @param pageable pagination parameters
   * @return paginated audit logs
   */
  @GetMapping("/logs")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE', 'AUDITOR')")
  @Timed(value = "api.audit.logs.get", description = "Get audit logs")
  @Operation(
      summary = "Get audit logs",
      description = "Retrieve audit logs for a tenant with pagination (newest first)")
  public ResponseEntity<Page<AuditEventEntity>> getAuditLogs(
      @RequestHeader("X-Tenant-ID") UUID tenantId, Pageable pageable) {
    log.info("GET /api/audit/logs - tenant: {}, page: {}", tenantId, pageable.getPageNumber());

    Page<AuditEventEntity> logs = auditService.getAuditLogs(tenantId, pageable);

    return ResponseEntity.ok(logs);
  }

  /**
   * Get audit logs by user.
   *
   * <p>Returns all audit events for a specific user within a tenant.
   *
   * @param tenantId the tenant ID from X-Tenant-ID header
   * @param userId the user ID to filter
   * @param pageable pagination parameters
   * @return paginated audit logs for user
   */
  @GetMapping("/logs/user")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE', 'AUDITOR')")
  @Timed(value = "api.audit.logs.user", description = "Get audit logs by user")
  @Operation(summary = "Get audit logs by user", description = "Retrieve audit logs for a specific user")
  public ResponseEntity<Page<AuditEventEntity>> getAuditLogsByUser(
      @RequestHeader("X-Tenant-ID") UUID tenantId,
      @RequestParam @NotBlank String userId,
      Pageable pageable) {
    log.info(
        "GET /api/audit/logs/user - tenant: {}, user: {}, page: {}",
        tenantId,
        userId,
        pageable.getPageNumber());
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("User ID is required");
    }
    Page<AuditEventEntity> logs = auditService.getAuditLogsByUser(tenantId, userId, pageable);

    return ResponseEntity.ok(logs);
  }

  /**
   * Get audit logs by action.
   *
   * <p>Returns all audit events for a specific action type.
   *
   * @param tenantId the tenant ID from X-Tenant-ID header
   * @param action the action type to filter
   * @param pageable pagination parameters
   * @return paginated audit logs
   */
  @GetMapping("/logs/action")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE', 'AUDITOR')")
  @Timed(value = "api.audit.logs.action", description = "Get audit logs by action")
  @Operation(summary = "Get audit logs by action", description = "Retrieve audit logs for a specific action type")
  public ResponseEntity<Page<AuditEventEntity>> getAuditLogsByAction(
      @RequestHeader("X-Tenant-ID") UUID tenantId,
      @RequestParam @NotBlank String action,
      Pageable pageable) {
    log.info(
        "GET /api/audit/logs/action - tenant: {}, action: {}, page: {}",
        tenantId,
        action,
        pageable.getPageNumber());

    Page<AuditEventEntity> logs = auditService.getAuditLogsByAction(tenantId, action, pageable);

    return ResponseEntity.ok(logs);
  }

  /**
   * Get denied access attempts (security incidents).
   *
   * <p>Returns all DENIED result audit events for incident investigation.
   *
   * @param tenantId the tenant ID from X-Tenant-ID header
   * @param pageable pagination parameters
   * @return paginated denied access events
   */
  @GetMapping("/logs/denied")
  @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY', 'AUDITOR')")
  @Timed(value = "api.audit.denied", description = "Get denied access attempts")
  @Operation(
      summary = "Get denied access attempts",
      description = "Retrieve security incidents (access denied events)")
  public ResponseEntity<Page<AuditEventEntity>> getDeniedAccessAttempts(
      @RequestHeader("X-Tenant-ID") UUID tenantId, Pageable pageable) {
    log.warn("GET /api/audit/logs/denied - tenant: {}", tenantId);

    Page<AuditEventEntity> deniedEvents = auditService.getDeniedAccessAttempts(tenantId, pageable);

    return ResponseEntity.ok(deniedEvents);
  }

  /**
   * Get error events (system failures).
   *
   * <p>Returns all ERROR result audit events for troubleshooting.
   *
   * @param tenantId the tenant ID from X-Tenant-ID header
   * @param pageable pagination parameters
   * @return paginated error events
   */
  @GetMapping("/logs/errors")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'AUDITOR')")
  @Timed(value = "api.audit.errors", description = "Get error events")
  @Operation(
      summary = "Get error events",
      description = "Retrieve system failures (error events)")
  public ResponseEntity<Page<AuditEventEntity>> getErrorEvents(
      @RequestHeader("X-Tenant-ID") UUID tenantId, Pageable pageable) {
    log.warn("GET /api/audit/logs/errors - tenant: {}", tenantId);

    Page<AuditEventEntity> errors = auditService.getErrorEvents(tenantId, pageable);

    return ResponseEntity.ok(errors);
  }

  /**
   * Search audit logs by keyword.
   *
   * <p>Searches action and resource fields for the keyword.
   *
   * @param tenantId the tenant ID from X-Tenant-ID header
   * @param keyword the search keyword (minimum 2 characters)
   * @param pageable pagination parameters
   * @return paginated search results
   */
  @GetMapping("/logs/search")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE', 'AUDITOR')")
  @Timed(value = "api.audit.search", description = "Search audit logs")
  @Operation(
      summary = "Search audit logs by keyword",
      description = "Search action and resource fields for keyword (min 2 characters)")
  public ResponseEntity<Page<AuditEventEntity>> search(
      @RequestHeader("X-Tenant-ID") UUID tenantId,
      @RequestParam @Size(min = 2, message = "keyword must be at least 2 characters") String keyword,
      Pageable pageable) {
    log.info(
        "GET /api/audit/logs/search - tenant: {}, keyword: {}, page: {}",
        tenantId,
        keyword,
        pageable.getPageNumber());

    Page<AuditEventEntity> results = auditService.search(tenantId, keyword, pageable);

    return ResponseEntity.ok(results);
  }

  /**
   * Search audit logs by time range.
   *
   * <p>Returns audit events within the specified time window.
   *
   * @param tenantId the tenant ID from X-Tenant-ID header
   * @param startTime start timestamp (ISO-8601 format)
   * @param endTime end timestamp (ISO-8601 format)
   * @param pageable pagination parameters
   * @return paginated audit logs
   */
  @GetMapping("/logs/range")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE', 'AUDITOR')")
  @Timed(value = "api.audit.range", description = "Search audit logs by time range")
  @Operation(
      summary = "Search audit logs by time range",
      description = "Retrieve audit logs within a specific time window")
  public ResponseEntity<Page<AuditEventEntity>> searchByTimeRange(
      @RequestHeader("X-Tenant-ID") UUID tenantId,
      @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
      Pageable pageable) {
    log.info(
        "GET /api/audit/logs/range - tenant: {}, start: {}, end: {}, page: {}",
        tenantId,
        startTime,
        endTime,
        pageable.getPageNumber());
    if (startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("startTime must be before or equal to endTime");
    }
    Page<AuditEventEntity> logs =
        auditService.searchByTimeRange(tenantId, startTime, endTime, pageable);

    return ResponseEntity.ok(logs);
  }

  /**
   * Search audit logs by resource.
   *
   * <p>Returns audit events for a specific resource type.
   *
   * @param tenantId the tenant ID from X-Tenant-ID header
   * @param resource the resource type to filter
   * @param pageable pagination parameters
   * @return paginated audit logs
   */
  @GetMapping("/logs/resource")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE', 'AUDITOR')")
  @Timed(value = "api.audit.resource", description = "Search audit logs by resource")
  @Operation(
      summary = "Search audit logs by resource",
      description = "Retrieve audit logs for a specific resource type")
  public ResponseEntity<Page<AuditEventEntity>> searchByResource(
      @RequestHeader("X-Tenant-ID") UUID tenantId,
      @RequestParam @NotBlank String resource,
      Pageable pageable) {
    log.info(
        "GET /api/audit/logs/resource - tenant: {}, resource: {}, page: {}",
        tenantId,
        resource,
        pageable.getPageNumber());

    Page<AuditEventEntity> logs = auditService.searchByResource(tenantId, resource, pageable);

    return ResponseEntity.ok(logs);
  }

  /**
   * Get audit statistics.
   *
   * <p>Returns counts grouped by result type (SUCCESS, DENIED, ERROR) for reporting.
   *
   * @param tenantId the tenant ID from X-Tenant-ID header
   * @return audit statistics
   */
  @GetMapping("/stats")
  @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE', 'AUDITOR')")
  @Timed(value = "api.audit.stats", description = "Get audit statistics")
  @Operation(
      summary = "Get audit statistics",
      description = "Retrieve audit statistics (counts by result type)")
  public ResponseEntity<Map<String, Long>> getAuditStats(
      @RequestHeader("X-Tenant-ID") UUID tenantId) {
    log.info("GET /api/audit/stats - tenant: {}", tenantId);

    Map<String, Long> stats = auditService.getAuditStats(tenantId);

    return ResponseEntity.ok(stats);
  }

  /**
   * Health check endpoint for load balancers.
   *
   * @return health status
   */
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Audit service health status")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Audit Service is running");
  }
}
