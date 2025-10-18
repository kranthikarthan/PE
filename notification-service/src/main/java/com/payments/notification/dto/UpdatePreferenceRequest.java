package com.payments.notification.dto;

import com.payments.notification.domain.model.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Set;

/**
 * UpdatePreferenceRequest DTO for REST API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferenceRequest {
  private Set<NotificationChannel> preferredChannels;
  private Boolean transactionAlertsOptIn;
  private Boolean marketingOptIn;
  private Boolean systemNotificationsOptIn;
  private LocalTime quietHoursStart;
  private LocalTime quietHoursEnd;
}
