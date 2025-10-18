package com.payments.iam.controller;

import com.payments.iam.annotation.RoleRequired;
import com.payments.iam.dto.AuditLogResponse;
import com.payments.iam.entity.AuditEventEntity;
import com.payments.iam.service.AuditService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AuditController - Compliance audit log endpoints (admin-only).
 *
 * <p>Endpoints:
 * - GET /api/audit/logs - Paginated audit trail
 * - GET /api/audit/denied - Denied access attempts
 * - GET /api/audit/errors - Error events
 * - GET /api/audit/stats - Compliance statistics
 *
 * <p>All endpoints require: bank_admin or support_agent role
 * Returns: Immutable audit trail for compliance (POPIA, FICA, PCI-DSS)
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "Compliance audit trail (admin-only)")
public class AuditController {

  private final AuditService auditService;

  /**
   * Get paginated audit logs for current tenant.
   *
   * @param page page number (0-based)
   * @param size page size
   * @return page of audit events
   */
  @GetMapping("/logs")
  @RoleRequired(
      roles = {"bank_admin", "support_agent"},
      description = "Query audit logs")
  @Timed(value = "audit.get_logs", description = "Time to fetch audit logs")
  @Operation(
      summary = "Get audit logs",
      description = "Get paginated audit logs for compliance reporting")
  @ApiResponse(
      responseCode = "200",
      description = "Audit logs",
      content = @Content(schema = @Schema(implementation = AuditLogResponse.class)))
  @ApiResponse(responseCode = "403", description = "Insufficient privileges")
  public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching audit logs: page={}, size={}", page, size);

    UUID tenantId = getCurrentTenantId();

    Page<AuditEventEntity> events = auditService.getAuditLogsByTenant(
        tenantId,
        PageRequest.of(page, size));

    Page<AuditLogResponse> response = events.map(AuditLogResponse::from);

    log.info("Retrieved {} audit logs", response.getNumberOfElements());

    return ResponseEntity.ok(response);
  }

  /**
   * Get denied access attempts (security incidents).
   *
   * @param page page number (0-based)
   * @param size page size
   * @return page of denied audit events
   */
  @GetMapping("/denied")
  @RoleRequired(
      roles = {"bank_admin", "support_agent"},
      description = "Query denied access attempts")
  @Timed(value = "audit.get_denied", description = "Time to fetch denied access attempts")
  @Operation(
      summary = "Get denied access attempts",
      description = "Get security incidents (denied access attempts)")
  @ApiResponse(
      responseCode = "200",
      description = "Denied access attempts")
  @ApiResponse(responseCode = "403", description = "Insufficient privileges")
  public ResponseEntity<Page<AuditLogResponse>> getDeniedAccessAttempts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching denied access attempts: page={}, size={}", page, size);

    UUID tenantId = getCurrentTenantId();

    Page<AuditEventEntity> events = auditService.getDeniedAccessAttempts(
        tenantId,
        PageRequest.of(page, size));

    Page<AuditLogResponse> response = events.map(AuditLogResponse::from);

    log.warn("Found {} denied access attempts", response.getNumberOfElements());

    return ResponseEntity.ok(response);
  }

  /**
   * Get error events.
   *
   * @param page page number (0-based)
   * @param size page size
   * @return page of error audit events
   */
  @GetMapping("/errors")
  @RoleRequired(
      roles = {"bank_admin", "support_agent"},
      description = "Query error events")
  @Timed(value = "audit.get_errors", description = "Time to fetch error events")
  @Operation(
      summary = "Get error events",
      description = "Get system error events from audit trail")
  @ApiResponse(
      responseCode = "200",
      description = "Error events")
  @ApiResponse(responseCode = "403", description = "Insufficient privileges")
  public ResponseEntity<Page<AuditLogResponse>> getErrorEvents(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching error events: page={}, size={}", page, size);

    UUID tenantId = getCurrentTenantId();

    Page<AuditEventEntity> events = auditService.getErrorEvents(
        tenantId,
        PageRequest.of(page, size));

    Page<AuditLogResponse> response = events.map(AuditLogResponse::from);

    log.error("Found {} error events", response.getNumberOfElements());

    return ResponseEntity.ok(response);
  }

  /**
   * Get audit statistics for compliance reporting.
   *
   * @return audit statistics (success, denied, error counts)
   */
  @GetMapping("/stats")
  @RoleRequired(
      roles = {"bank_admin"},
      description = "Query audit statistics")
  @Timed(value = "audit.get_stats", description = "Time to fetch audit statistics")
  @Operation(
      summary = "Get audit statistics",
      description = "Get audit trail statistics for compliance reporting")
  @ApiResponse(
      responseCode = "200",
      description = "Audit statistics")
  @ApiResponse(responseCode = "403", description = "Insufficient privileges")
  public ResponseEntity<AuditService.AuditStats> getAuditStats() {
    log.info("Fetching audit statistics");

    UUID tenantId = getCurrentTenantId();

    AuditService.AuditStats stats = auditService.getAuditStats(tenantId);

    log.info(
        "Audit stats: successes={}, denied={}, errors={}, total={}",
        stats.getSuccessCount(),
        stats.getDeniedCount(),
        stats.getErrorCount(),
        stats.getTotalCount());

    return ResponseEntity.ok(stats);
  }

  /**
   * Extract current tenant ID from SecurityContext.
   *
   * @return current tenant UUID
   */
  private UUID getCurrentTenantId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getAuthorities()
        .stream()
        .filter(auth -> auth.getAuthority().startsWith("TENANT_"))
        .map(auth -> auth.getAuthority().substring("TENANT_".length()))
        .findFirst()
        .map(UUID::fromString)
        .orElse(null);
  }
}
