package com.payments.settlement.dto;

import com.payments.domain.settlement.SettlementMetrics;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for settlement metrics request.
 *
 * <p>This DTO represents the request data for creating
 * settlement metrics entries. It includes validation
 * annotations and comprehensive field definitions.
 *
 * @since PE-411
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementMetricsRequest {
  
  @NotBlank(message = "Metrics name is required")
  @Size(max = 255, message = "Metrics name must not exceed 255 characters")
  private String metricsName;
  
  @Size(max = 1000, message = "Description must not exceed 1000 characters")
  private String description;
  
  @NotNull(message = "Metrics type is required")
  private SettlementMetrics.MetricsType metricsType;
  
  @NotNull(message = "Category is required")
  private SettlementMetrics.MetricsCategory category;
  
  @NotBlank(message = "Metric name is required")
  @Size(max = 100, message = "Metric name must not exceed 100 characters")
  private String metricName;
  
  private BigDecimal metricValue;
  
  @Size(max = 20, message = "Metric unit must not exceed 20 characters")
  private String metricUnit;
  
  private BigDecimal baselineValue;
  
  private BigDecimal targetValue;
  
  private BigDecimal thresholdMin;
  
  private BigDecimal thresholdMax;
  
  private LocalDateTime periodStart;
  
  private LocalDateTime periodEnd;
  
  @Size(max = 50, message = "Participant ID must not exceed 50 characters")
  private String participantId;
  
  private Long workflowId;
  
  private Long orchestrationId;
  
  @Size(max = 3, message = "Currency must not exceed 3 characters")
  private String currency;
  
  @Size(max = 50, message = "Business unit ID must not exceed 50 characters")
  private String businessUnitId;
  
  private String metadata;
}
