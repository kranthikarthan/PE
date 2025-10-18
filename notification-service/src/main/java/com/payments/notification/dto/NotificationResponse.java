package com.payments.notification.dto;

import com.payments.notification.domain.model.NotificationEntity;
import com.payments.notification.domain.model.NotificationStatus;
import com.payments.notification.domain.model.NotificationType;
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
        .id(entity.getId())
        .tenantId(entity.getTenantId())
        .userId(entity.getUserId())
        .notificationType(entity.getNotificationType())
        .status(entity.getStatus())
        .recipientAddress(entity.getRecipientAddress())
        .externalId(entity.getExternalId())
        .attempts(entity.getAttempts())
        .failureReason(entity.getFailureReason())
        .createdAt(entity.getCreatedAt())
        .lastAttemptAt(entity.getLastAttemptAt())
        .build();
  }
}
