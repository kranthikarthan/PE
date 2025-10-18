package com.payments.notification.dto;

import com.payments.notification.domain.model.NotificationChannel;
import com.payments.notification.domain.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * SendNotificationRequest DTO for REST API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
  @NotBlank(message = "User ID is required")
  private String userId;

  @NotNull(message = "Notification type is required")
  private NotificationType notificationType;

  private NotificationChannel channelType;

  @NotBlank(message = "Recipient address is required")
  private String recipientAddress;

  @NotBlank(message = "Template data is required")
  private String templateData; // JSON string
}
