package com.payments.iam.service;

import com.payments.iam.entity.AuditEventEntity;
import com.payments.iam.repository.AuditEventRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AuditService - Compliance audit logging for all IAM operations.
 *
 * <p>Responsibilities:
 * - Log all access attempts (success and failure)
 * - Track who accessed what, when, where, why
 * - Provide audit trail queries for compliance reporting
 * - Immutable append-only log (never update/delete audit records)
 *
 * <p>Compliance: POPIA, FICA, PCI-DSS audit trail
 * Retention: 90+ days (managed by separate archival process)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

  private final AuditEventRepository auditEventRepository;

  /**
   * Log successful operation.
   *
   * @param tenantId tenant UUID
   * @param userId user ID
   * @param action operation performed (e.g., "ROLE_ASSIGNED")
   * @param resource resource type (e.g., "user_role")
   * @param resourceId resource ID (optional)
   * @param details detailed description
   */
  @Transactional
  @Timed(value = "audit.log_success", description = "Time to log successful audit event")
  public void logSuccess(
      UUID tenantId,
      String userId,
      String action,
      String resource,
      UUID resourceId,
      String details) {
    AuditEventEntity event = AuditEventEntity.builder()
        .tenantId(tenantId)
        .userId(userId)
        .action(action)
        .resource(resource)
        .resourceId(resourceId)
        .result(AuditEventEntity.AuditResult.SUCCESS)
        .details(details)
        .ipAddress(getClientIpAddress())
        .userAgent(getUserAgent())
        .build();

    auditEventRepository.save(event);
    log.info(
        "Audit[SUCCESS]: user={}, action={}, resource={}, tenant={}",
        userId,
        action,
        resource,
        tenantId);
  }

  /**
   * Log denied access attempt.
   *
   * @param tenantId tenant UUID
   * @param userId user ID
   * @param action attempted operation
   * @param resource resource type
   * @param reason why access was denied
   */
  @Transactional
  @Timed(value = "audit.log_denied", description = "Time to log denied audit event")
  public void logDenied(
      UUID tenantId,
      String userId,
      String action,
      String resource,
      String reason) {
    AuditEventEntity event = AuditEventEntity.builder()
        .tenantId(tenantId)
        .userId(userId)
        .action(action)
        .resource(resource)
        .result(AuditEventEntity.AuditResult.DENIED)
        .details("Access denied: " + reason)
        .ipAddress(getClientIpAddress())
        .userAgent(getUserAgent())
        .build();

    auditEventRepository.save(event);
    log.warn(
        "Audit[DENIED]: user={}, action={}, resource={}, reason={}, tenant={}",
        userId,
        action,
        resource,
        reason,
        tenantId);
  }

  /**
   * Log error event.
   *
   * @param tenantId tenant UUID
   * @param userId user ID
   * @param action attempted operation
   * @param resource resource type
   * @param errorMessage error details
   */
  @Transactional
  @Timed(value = "audit.log_error", description = "Time to log error audit event")
  public void logError(
      UUID tenantId,
      String userId,
      String action,
      String resource,
      String errorMessage) {
    AuditEventEntity event = AuditEventEntity.builder()
        .tenantId(tenantId)
        .userId(userId)
        .action(action)
        .resource(resource)
        .result(AuditEventEntity.AuditResult.ERROR)
        .details("Error: " + errorMessage)
        .ipAddress(getClientIpAddress())
        .userAgent(getUserAgent())
        .build();

    auditEventRepository.save(event);
    log.error(
        "Audit[ERROR]: user={}, action={}, resource={}, error={}, tenant={}",
        userId,
        action,
        resource,
        errorMessage,
        tenantId);
  }

  /**
   * Get all audit logs for a tenant (paginated).
   *
   * @param tenantId tenant UUID
   * @param pageable pagination info
   * @return page of audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.get_by_tenant", description = "Time to fetch audit logs by tenant")
  public Page<AuditEventEntity> getAuditLogsByTenant(UUID tenantId, Pageable pageable) {
    return auditEventRepository.findByTenantId(tenantId, pageable);
  }

  /**
   * Get audit logs by user (paginated).
   *
   * @param userId user ID
   * @param pageable pagination info
   * @return page of audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.get_by_user", description = "Time to fetch audit logs by user")
  public Page<AuditEventEntity> getAuditLogsByUser(String userId, Pageable pageable) {
    return auditEventRepository.findByUserId(userId, pageable);
  }

  /**
   * Get audit logs by action type (paginated).
   *
   * @param tenantId tenant UUID
   * @param action action type
   * @param pageable pagination info
   * @return page of audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.get_by_action", description = "Time to fetch audit logs by action")
  public Page<AuditEventEntity> getAuditLogsByAction(
      UUID tenantId,
      String action,
      Pageable pageable) {
    return auditEventRepository.findByTenantIdAndAction(tenantId, action, pageable);
  }

  /**
   * Get denied access attempts (security incidents).
   *
   * @param tenantId tenant UUID
   * @param pageable pagination info
   * @return page of denied audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.get_denied", description = "Time to fetch denied access attempts")
  public Page<AuditEventEntity> getDeniedAccessAttempts(UUID tenantId, Pageable pageable) {
    return auditEventRepository.findDeniedAccessAttempts(tenantId, pageable);
  }

  /**
   * Get error events.
   *
   * @param tenantId tenant UUID
   * @param pageable pagination info
   * @return page of error audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.get_errors", description = "Time to fetch error audit events")
  public Page<AuditEventEntity> getErrorEvents(UUID tenantId, Pageable pageable) {
    return auditEventRepository.findErrorEvents(tenantId, pageable);
  }

  /**
   * Get audit logs within a time range.
   *
   * @param tenantId tenant UUID
   * @param startTime start timestamp
   * @param endTime end timestamp
   * @return list of audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.get_by_date_range", description = "Time to fetch audit logs by date range")
  public List<AuditEventEntity> getAuditLogsByDateRange(
      UUID tenantId,
      LocalDateTime startTime,
      LocalDateTime endTime) {
    return auditEventRepository.findByTenantIdAndTimestampBetween(tenantId, startTime, endTime);
  }

  /**
   * Get audit statistics for a tenant (compliance reporting).
   *
   * @param tenantId tenant UUID
   * @return audit statistics
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.get_stats", description = "Time to fetch audit statistics")
  public AuditStats getAuditStats(UUID tenantId) {
    long successCount = auditEventRepository.countByTenantIdAndResult(tenantId, "SUCCESS");
    long deniedCount = auditEventRepository.countByTenantIdAndResult(tenantId, "DENIED");
    long errorCount = auditEventRepository.countByTenantIdAndResult(tenantId, "ERROR");

    return AuditStats.builder()
        .successCount(successCount)
        .deniedCount(deniedCount)
        .errorCount(errorCount)
        .totalCount(successCount + deniedCount + errorCount)
        .build();
  }

  /**
   * Get client IP address from HTTP request context.
   *
   * @return IP address or "UNKNOWN" if unavailable
   */
  private String getClientIpAddress() {
    try {
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        String ip = attributes.getRequest().getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
          ip = attributes.getRequest().getRemoteAddr();
        }
        return ip;
      }
    } catch (Exception e) {
      log.debug("Could not determine client IP", e);
    }
    return "UNKNOWN";
  }

  /**
   * Get user agent from HTTP request context.
   *
   * @return user agent or "UNKNOWN" if unavailable
   */
  private String getUserAgent() {
    try {
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        String userAgent = attributes.getRequest().getHeader("User-Agent");
        return userAgent != null ? userAgent : "UNKNOWN";
      }
    } catch (Exception e) {
      log.debug("Could not determine user agent", e);
    }
    return "UNKNOWN";
  }

  /**
   * Audit statistics DTO for compliance reporting.
   */
  @lombok.Data
  @lombok.Builder
  public static class AuditStats {
    private long successCount;
    private long deniedCount;
    private long errorCount;
    private long totalCount;
  }
}
