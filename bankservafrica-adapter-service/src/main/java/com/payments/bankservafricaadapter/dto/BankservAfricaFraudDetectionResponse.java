package com.payments.bankservafricaadapter.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** BankservAfrica Fraud Detection Response */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaFraudDetectionResponse {
  private String adapterId;
  private String paymentId;
  private Boolean isFraudDetected;
  private Double fraudScore;
  private String fraudStatus;
  private List<String> appliedRules;
  private List<String> fraudAlerts;
  private List<String> fraudWarnings;
  private Instant detectedAt;
  private String tenantId;
  private String businessUnitId;
}
