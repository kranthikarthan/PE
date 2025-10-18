package com.payments.iam.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.payments.iam.entity.AuditEventEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuditLogResponse - Response DTO for audit log endpoints.
 *
 * <p>Contains: Complete audit event details for compliance reporting
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

  private UUID id;

  private UUID tenantId;

  private String userId;

  private String action;

  private String resource;

  private UUID resourceId;

  private String result;

  private String details;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime timestamp;

  private String ipAddress;

  private String userAgent;

  /**
   * Convert AuditEventEntity to AuditLogResponse.
   *
   * @param entity audit event entity
   * @return audit log response DTO
   */
  public static AuditLogResponse from(AuditEventEntity entity) {
    return AuditLogResponse.builder()
        .id(entity.getId())
        .tenantId(entity.getTenantId())
        .userId(entity.getUserId())
        .action(entity.getAction())
        .resource(entity.getResource())
        .resourceId(entity.getResourceId())
        .result(entity.getResult().name())
        .details(entity.getDetails())
        .timestamp(entity.getTimestamp())
        .ipAddress(entity.getIpAddress())
        .userAgent(entity.getUserAgent())
        .build();
  }
}
