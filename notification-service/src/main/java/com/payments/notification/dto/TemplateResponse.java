package com.payments.notification.dto;

import com.payments.domain.entities.NotificationTemplateEntity;
import com.payments.domain.valueobjects.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TemplateResponse DTO for REST API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {
  private UUID id;
  private String tenantId;
  private NotificationType notificationType;
  private String name;
  private String emailSubject;
  private String emailTemplate;
  private String pushTitle;
  private String pushBody;
  private String smsTemplate;
  private boolean active;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Convert entity to response.
   */
  public static TemplateResponse from(NotificationTemplateEntity entity) {
    return TemplateResponse.builder()
        .id(UUID.fromString(entity.getTemplateId().getValue()))
        .tenantId(entity.getTenantId().getValue())
        .notificationType(entity.getType())
        .name(entity.getName())
        .emailSubject(entity.getEmailSubject())
        .emailTemplate(entity.getEmailTemplate())
        .pushTitle(entity.getPushTitle())
        .pushBody(entity.getPushBody())
        .smsTemplate(entity.getSmsTemplate())
        .active(entity.isActive())
        .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
        .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
        .build();
  }
}
