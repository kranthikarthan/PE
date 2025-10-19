package com.payments.settlement.dto;

import com.payments.domain.settlement.SettlementAlert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for settlement alert request.
 *
 * <p>This DTO represents the request data for creating
 * settlement alerts. It includes validation annotations
 * and comprehensive field definitions.
 *
 * @since PE-411
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementAlertRequest {
  
  @NotBlank(message = "Alert name is required")
  @Size(max = 255, message = "Alert name must not exceed 255 characters")
  private String alertName;
  
  @Size(max = 1000, message = "Description must not exceed 1000 characters")
  private String description;
  
  @NotNull(message = "Alert type is required")
  private SettlementAlert.AlertType alertType;
  
  @NotNull(message = "Severity is required")
  private SettlementAlert.AlertSeverity severity;
  
  @Size(max = 2000, message = "Alert message must not exceed 2000 characters")
  private String alertMessage;
  
  @Size(max = 50, message = "Participant ID must not exceed 50 characters")
  private String participantId;
  
  private Long workflowId;
  
  private Long orchestrationId;
  
  private Long monitoringId;
  
  @Size(max = 50, message = "Business unit ID must not exceed 50 characters")
  private String businessUnitId;
  
  private String metadata;
}
