package com.payments.audit.service;

import com.payments.audit.entity.AuditEventEntity;
import com.payments.audit.repository.AuditEventRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Audit Service - Business logic for compliance audit logging.
 *
 * <p>Responsibilities:
 * - Query audit logs (with pagination and filtering)
 * - Search audit trail by keyword
 * - Track security incidents (denied access)
 * - Track system failures (errors)
 * - Generate audit statistics
 * - Manage archival/retention policies
 * - Enforce multi-tenancy
 *
 * <p>All operations are multi-tenant aware:
 * - Requires X-Tenant-ID header from caller
 * - Filters all queries by tenant_id
 * - Enforces data isolation at service layer
 *
 * <p>Compliance:
 * - POPIA: Data tracking and user audit trails
 * - FICA: Transaction audit trails
 * - PCI-DSS: Payment card operation logging
 * - 7-year retention policy
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

  private final AuditEventRepository auditEventRepository;

  /**
   * Get all audit logs for a tenant with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination info
   * @return paginated audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.logs.get", description = "Get audit logs for tenant")
  public Page<AuditEventEntity> getAuditLogs(UUID tenantId, Pageable pageable) {
    log.debug("Fetching audit logs for tenant: {}, page: {}", tenantId, pageable.getPageNumber());

    validateTenantId(tenantId);

    Page<AuditEventEntity> logs = auditEventRepository.findByTenantId(tenantId, pageable);

    log.debug("Retrieved {} audit logs for tenant: {}", logs.getNumberOfElements(), tenantId);
    return logs;
  }

  /**
   * Get audit logs for a specific user within a tenant.
   *
   * @param tenantId the tenant ID
   * @param userId the user ID
   * @param pageable pagination info
   * @return paginated audit events for user
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.logs.user", description = "Get audit logs by user")
  public Page<AuditEventEntity> getAuditLogsByUser(
      UUID tenantId, String userId, Pageable pageable) {
    log.debug("Fetching audit logs for user: {} in tenant: {}", userId, tenantId);

    validateTenantId(tenantId);
    validateUserId(userId);

    Page<AuditEventEntity> logs =
        auditEventRepository.findByTenantIdAndUserId(tenantId, userId, pageable);

    log.debug("Retrieved {} audit logs for user: {}", logs.getNumberOfElements(), userId);
    return logs;
  }

  /**
   * Get audit logs for a specific action type.
   *
   * @param tenantId the tenant ID
   * @param action the action type
   * @param pageable pagination info
   * @return paginated audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.logs.action", description = "Get audit logs by action")
  public Page<AuditEventEntity> getAuditLogsByAction(
      UUID tenantId, String action, Pageable pageable) {
    log.debug("Fetching audit logs for action: {} in tenant: {}", action, tenantId);

    validateTenantId(tenantId);
    validateAction(action);

    Page<AuditEventEntity> logs =
        auditEventRepository.findByTenantIdAndAction(tenantId, action, pageable);

    log.debug("Retrieved {} audit logs for action: {}", logs.getNumberOfElements(), action);
    return logs;
  }

  /**
   * Get denied access attempts (security incidents).
   *
   * @param tenantId the tenant ID
   * @param pageable pagination info
   * @return paginated denied access events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.security.denied", description = "Get denied access attempts")
  public Page<AuditEventEntity> getDeniedAccessAttempts(UUID tenantId, Pageable pageable) {
    log.debug("Fetching denied access attempts for tenant: {}", tenantId);

    validateTenantId(tenantId);

    Page<AuditEventEntity> deniedEvents =
        auditEventRepository.findDeniedAccessAttempts(tenantId, pageable);

    log.warn(
        "Found {} denied access attempts for tenant: {}",
        deniedEvents.getTotalElements(),
        tenantId);
    return deniedEvents;
  }

  /**
   * Get error events (system failures).
   *
   * @param tenantId the tenant ID
   * @param pageable pagination info
   * @return paginated error events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.errors", description = "Get error events")
  public Page<AuditEventEntity> getErrorEvents(UUID tenantId, Pageable pageable) {
    log.debug("Fetching error events for tenant: {}", tenantId);

    validateTenantId(tenantId);

    Page<AuditEventEntity> errors = auditEventRepository.findErrorEvents(tenantId, pageable);

    log.warn("Found {} error events for tenant: {}", errors.getTotalElements(), tenantId);
    return errors;
  }

  /**
   * Search audit logs by time range.
   *
   * @param tenantId the tenant ID
   * @param startTime start timestamp
   * @param endTime end timestamp
   * @param pageable pagination info
   * @return paginated audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.logs.timerange", description = "Search audit logs by time range")
  public Page<AuditEventEntity> searchByTimeRange(
      UUID tenantId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
    log.debug(
        "Searching audit logs for time range: {} to {} in tenant: {}",
        startTime,
        endTime,
        tenantId);

    validateTenantId(tenantId);
    validateTimeRange(startTime, endTime);

    Page<AuditEventEntity> logs =
        auditEventRepository.findByTenantIdAndTimestampBetween(
            tenantId, startTime, endTime, pageable);

    log.debug(
        "Found {} audit logs in time range for tenant: {}",
        logs.getTotalElements(),
        tenantId);
    return logs;
  }

  /**
   * Search audit logs by resource.
   *
   * @param tenantId the tenant ID
   * @param resource the resource type
   * @param pageable pagination info
   * @return paginated audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.logs.resource", description = "Search audit logs by resource")
  public Page<AuditEventEntity> searchByResource(
      UUID tenantId, String resource, Pageable pageable) {
    log.debug("Searching audit logs for resource: {} in tenant: {}", resource, tenantId);

    validateTenantId(tenantId);
    validateResource(resource);

    Page<AuditEventEntity> logs =
        auditEventRepository.findByTenantIdAndResource(tenantId, resource, pageable);

    log.debug("Found {} audit logs for resource: {}", logs.getTotalElements(), resource);
    return logs;
  }

  /**
   * Search audit logs by keyword.
   *
   * @param tenantId the tenant ID
   * @param keyword the search keyword
   * @param pageable pagination info
   * @return paginated audit events
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.logs.search", description = "Search audit logs by keyword")
  public Page<AuditEventEntity> search(UUID tenantId, String keyword, Pageable pageable) {
    log.debug("Searching audit logs for keyword: {} in tenant: {}", keyword, tenantId);

    validateTenantId(tenantId);
    validateKeyword(keyword);

    Page<AuditEventEntity> results =
        auditEventRepository.searchByKeyword(tenantId, keyword, pageable);

    log.debug("Search found {} results for keyword: {}", results.getTotalElements(), keyword);
    return results;
  }

  /**
   * Get audit statistics (counts by result type).
   *
   * <p>Provides overview of audit trail for compliance reporting:
   * - SUCCESS: Normal operations
   * - DENIED: Security incidents
   * - ERROR: System failures
   *
   * @param tenantId the tenant ID
   * @return map of result type to count
   */
  @Transactional(readOnly = true)
  @Timed(value = "audit.stats", description = "Get audit statistics")
  @Cacheable(
      value = "audit_stats",
      key = "#tenantId.toString()",
      cacheManager = "cacheManager")
  public Map<String, Long> getAuditStats(UUID tenantId) {
    log.debug("Fetching audit statistics for tenant: {}", tenantId);

    validateTenantId(tenantId);

    Map<String, Long> stats = new HashMap<>();

    // Count by result type
    long successCount = auditEventRepository.countByTenantIdAndResult(tenantId, "SUCCESS");
    long deniedCount = auditEventRepository.countByTenantIdAndResult(tenantId, "DENIED");
    long errorCount = auditEventRepository.countByTenantIdAndResult(tenantId, "ERROR");

    stats.put("total", successCount + deniedCount + errorCount);
    stats.put("success", successCount);
    stats.put("denied", deniedCount);
    stats.put("errors", errorCount);

    log.debug(
        "Audit statistics for tenant {}: total={}, success={}, denied={}, errors={}",
        tenantId,
        stats.get("total"),
        successCount,
        deniedCount,
        errorCount);

    return stats;
  }

  /**
   * Validate tenant ID for multi-tenancy enforcement.
   *
   * @param tenantId the tenant ID to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateTenantId(UUID tenantId) {
    if (tenantId == null) {
      throw new IllegalArgumentException("Tenant ID is required");
    }
  }

  /**
   * Validate user ID.
   *
   * @param userId the user ID to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateUserId(String userId) {
    if (userId == null || userId.isEmpty()) {
      throw new IllegalArgumentException("User ID is required");
    }
  }

  /**
   * Validate action.
   *
   * @param action the action to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateAction(String action) {
    if (action == null || action.isEmpty()) {
      throw new IllegalArgumentException("Action is required");
    }
  }

  /**
   * Validate resource.
   *
   * @param resource the resource to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateResource(String resource) {
    if (resource == null || resource.isEmpty()) {
      throw new IllegalArgumentException("Resource is required");
    }
  }

  /**
   * Validate keyword for search.
   *
   * @param keyword the keyword to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateKeyword(String keyword) {
    if (keyword == null || keyword.isEmpty()) {
      throw new IllegalArgumentException("Search keyword is required");
    }
    if (keyword.length() < 2) {
      throw new IllegalArgumentException("Search keyword must be at least 2 characters");
    }
  }

  /**
   * Validate time range.
   *
   * @param startTime start timestamp
   * @param endTime end timestamp
   * @throws IllegalArgumentException if validation fails
   */
  private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null || endTime == null) {
      throw new IllegalArgumentException("Start time and end time are required");
    }
    if (startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    if (startTime.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("Start time cannot be in the future");
    }
  }
}
