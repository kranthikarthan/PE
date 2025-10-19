package com.payments.settlement.dto;

import com.payments.domain.settlement.SettlementMetrics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for settlement metrics response.
 *
 * <p>This DTO represents the response data for settlement
 * metrics operations. It includes comprehensive field
 * definitions and response formatting.
 *
 * @since PE-411
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementMetricsResponse {
  
  private String metricsId;
  
  private String metricsName;
  
  private String description;
  
  private SettlementMetrics.MetricsType metricsType;
  
  private SettlementMetrics.MetricsCategory category;
  
  private String metricName;
  
  private BigDecimal metricValue;
  
  private String metricUnit;
  
  private BigDecimal baselineValue;
  
  private BigDecimal targetValue;
  
  private BigDecimal thresholdMin;
  
  private BigDecimal thresholdMax;
  
  private LocalDateTime metricsTimestamp;
  
  private LocalDateTime periodStart;
  
  private LocalDateTime periodEnd;
  
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
