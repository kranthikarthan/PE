package com.payments.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.audit.entity.AuditEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit Event Processor - Deserializes and validates Kafka events.
 *
 * <p>Responsibilities:
 * - Parse JSON from Kafka into AuditEventEntity
 * - Validate event structure (fail-fast pattern)
 * - Handle deserialization errors
 * - Apply default values if needed
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditEventProcessor {

  private final ObjectMapper objectMapper;

  /**
   * Parse and validate audit event from Kafka JSON.
   *
   * <p>Process:
   * 1. Deserialize JSON to AuditEventPayload
   * 2. Validate required fields
   * 3. Create AuditEventEntity
   * 4. Return for persistence
   *
   * @param eventJson the JSON event from Kafka
   * @return validated AuditEventEntity
   * @throws IllegalArgumentException if validation fails
   */
  public AuditEventEntity parseAndValidate(String eventJson) {
    try {
      // Deserialize from Kafka JSON
      AuditEventPayload payload = objectMapper.readValue(eventJson, AuditEventPayload.class);

      // Validate payload
      validatePayload(payload);

      // Convert to entity
      return buildEntity(payload);

    } catch (IllegalArgumentException e) {
      // Validation errors - rethrow as-is
      log.warn("Validation error in audit event: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      // JSON parsing or other errors
      log.error("Failed to deserialize audit event JSON: {}", eventJson, e);
      throw new IllegalArgumentException("Invalid audit event JSON format", e);
    }
  }

  /**
   * Validate payload contains all required fields.
   *
   * @param payload the deserialized payload
   * @throws IllegalArgumentException if validation fails
   */
  private void validatePayload(AuditEventPayload payload) {
    if (payload == null) {
      throw new IllegalArgumentException("Audit event payload is null");
    }

    if (payload.getTenantId() == null || payload.getTenantId().isEmpty()) {
      throw new IllegalArgumentException("Missing tenantId in audit event");
    }

    if (payload.getUserId() == null || payload.getUserId().isEmpty()) {
      throw new IllegalArgumentException("Missing userId in audit event");
    }

    if (payload.getAction() == null || payload.getAction().isEmpty()) {
      throw new IllegalArgumentException("Missing action in audit event");
    }

    if (payload.getResult() == null || payload.getResult().isEmpty()) {
      throw new IllegalArgumentException("Missing result in audit event");
    }

    // Validate result is one of the allowed values
    try {
      AuditEventEntity.AuditResult.valueOf(payload.getResult().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format("Invalid result: %s. Must be SUCCESS, DENIED, or ERROR", payload.getResult()));
    }

    log.debug(
        "Validated audit event: tenant={}, user={}, action={}",
        payload.getTenantId(),
        payload.getUserId(),
        payload.getAction());
  }

  /**
   * Build AuditEventEntity from validated payload.
   *
   * @param payload the validated payload
   * @return AuditEventEntity ready for persistence
   */
  private AuditEventEntity buildEntity(AuditEventPayload payload) {
    return AuditEventEntity.builder()
        .tenantId(UUID.fromString(payload.getTenantId()))
        .userId(payload.getUserId())
        .action(payload.getAction())
        .resource(payload.getResource())
        .resourceId(
            payload.getResourceId() != null ? UUID.fromString(payload.getResourceId()) : null)
        .result(AuditEventEntity.AuditResult.valueOf(payload.getResult().toUpperCase()))
        .details(payload.getDetails())
        .timestamp(
            payload.getTimestamp() != null ? LocalDateTime.parse(payload.getTimestamp())
                : LocalDateTime.now())
        .ipAddress(payload.getIpAddress())
        .userAgent(payload.getUserAgent())
        .build();
  }

  /**
   * DTO for incoming Kafka audit event payload.
   *
   * <p>Matches the event schema from other services:
   * - tenant-management-service
   * - iam-service
   * - payment-initiation-service
   * etc.
   */
  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class AuditEventPayload {
    private String tenantId; // UUID string
    private String userId;
    private String action;
    private String resource;
    private String resourceId; // UUID string (optional)
    private String result; // SUCCESS, DENIED, ERROR
    private String details; // JSON or plain text
    private String timestamp; // ISO-8601 format
    private String ipAddress;
    private String userAgent;
  }
}
