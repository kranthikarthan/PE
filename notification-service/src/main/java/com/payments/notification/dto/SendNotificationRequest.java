package com.payments.notification.dto;

import com.payments.domain.valueobjects.NotificationType;
import com.payments.domain.valueobjects.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
