package com.payments.settlement.dto;

import com.payments.domain.settlement.SettlementAlert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for settlement alert response.
 *
 * <p>This DTO represents the response data for settlement
 * alert operations. It includes comprehensive field
 * definitions and response formatting.
 *
 * @since PE-411
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementAlertResponse {
  
  private String alertId;
  
  private String alertName;
  
  private String description;
  
  private SettlementAlert.AlertType alertType;
  
  private SettlementAlert.AlertSeverity severity;
  
  private SettlementAlert.AlertStatus status;
  
  private String alertMessage;
  
  private LocalDateTime alertTimestamp;
  
  private LocalDateTime acknowledgedAt;
  
  private String acknowledgedBy;
  
  private LocalDateTime resolvedAt;
  
  private String resolvedBy;
  
  private String resolutionNotes;
  
  private String participantId;
  
  private Long workflowId;
  
  private Long orchestrationId;
  
  private Long monitoringId;
  
  private String businessUnitId;
  
  private String metadata;
  
  private LocalDateTime createdAt;
  
  private String createdBy;
  
  private LocalDateTime updatedAt;
  
  private String updatedBy;
}
