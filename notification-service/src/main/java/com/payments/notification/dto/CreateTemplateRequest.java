package com.payments.notification.dto;

import com.payments.domain.valueobjects.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * CreateTemplateRequest DTO for REST API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateRequest {
  @NotNull(message = "Notification type is required")
  private NotificationType notificationType;

  @NotBlank(message = "Template name is required")
  private String name;

  @NotBlank(message = "Email subject is required")
  private String emailSubject;

  @NotBlank(message = "Email template is required")
  private String emailTemplate;

  @NotBlank(message = "Push title is required")
  private String pushTitle;

  @NotBlank(message = "Push body is required")
  private String pushBody;

  @NotBlank(message = "SMS template is required")
  private String smsTemplate;
}
