package com.payments.notification.dto;

import com.payments.domain.entities.NotificationEntity;
import com.payments.domain.valueobjects.NotificationStatus;
import com.payments.domain.valueobjects.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * NotificationResponse DTO for REST API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
  private UUID id;
  private String tenantId;
  private String userId;
  private NotificationType notificationType;
  private NotificationStatus status;
  private String recipientAddress;
  private String externalId;
  private Integer attempts;
  private String failureReason;
  private LocalDateTime createdAt;
  private LocalDateTime lastAttemptAt;

  /**
   * Convert entity to response.
   */
  public static NotificationResponse from(NotificationEntity entity) {
    return NotificationResponse.builder()
        .id(UUID.fromString(entity.getId()))
        .tenantId(entity.getTenantId().getValue())
        .userId(entity.getUserId())
        .notificationType(entity.getType())
        .status(entity.getStatus())
        .recipientAddress(entity.getRecipientAddress())
        .externalId(entity.getExternalId())
        .attempts(entity.getAttempts())
        .failureReason(entity.getFailureReason())
        .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
        .lastAttemptAt(entity.getLastAttemptAt() != null ? entity.getLastAttemptAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
        .build();
  }
}
