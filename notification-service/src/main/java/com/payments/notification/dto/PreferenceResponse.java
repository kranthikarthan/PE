package com.payments.notification.dto;

import com.payments.notification.domain.model.NotificationChannel;
import com.payments.notification.domain.model.NotificationPreferenceEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

/**
 * PreferenceResponse DTO for REST API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceResponse {
  private UUID id;
  private String tenantId;
  private String userId;
  private Set<NotificationChannel> preferredChannels;
  private Boolean transactionAlertsOptIn;
  private Boolean marketingOptIn;
  private Boolean systemNotificationsOptIn;
  private LocalTime quietHoursStart;
  private LocalTime quietHoursEnd;

  /**
   * Convert entity to response.
   */
  public static PreferenceResponse from(NotificationPreferenceEntity entity) {
    return PreferenceResponse.builder()
        .id(entity.getId())
        .tenantId(entity.getTenantId())
        .userId(entity.getUserId())
        .preferredChannels(entity.getPreferredChannels())
        .transactionAlertsOptIn(entity.isTransactionAlertsOptIn())
        .marketingOptIn(entity.isMarketingOptIn())
        .systemNotificationsOptIn(entity.isSystemNotificationsOptIn())
        .quietHoursStart(entity.getQuietHoursStart())
        .quietHoursEnd(entity.getQuietHoursEnd())
        .build();
  }
}
