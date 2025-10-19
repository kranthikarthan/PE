package com.payments.settlement.dto;

import com.payments.domain.settlement.SettlementMonitoring;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for settlement monitoring response.
 *
 * <p>This DTO represents the response data for settlement
 * monitoring operations. It includes comprehensive field
 * definitions and response formatting.
 *
 * @since PE-411
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementMonitoringResponse {
  
  private String monitoringId;
  
  private String monitoringName;
  
  private String description;
  
  private SettlementMonitoring.MonitoringType monitoringType;
  
  private SettlementMonitoring.MonitoringStatus status;
  
  private String metricName;
  
  private BigDecimal metricValue;
  
  private String metricUnit;
  
  private BigDecimal thresholdValue;
  
  private String alertLevel;
  
  private LocalDateTime monitoringTimestamp;
  
  private Long durationSeconds;
  
  private String participantId;
  
  private Long workflowId;
  
  private Long orchestrationId;
  
  private String currency;
  
  private String businessUnitId;
  
  private String metadata;
  
  private LocalDateTime createdAt;
  
  private String createdBy;
  
  private LocalDateTime updatedAt;
  
  private String updatedBy;
}
