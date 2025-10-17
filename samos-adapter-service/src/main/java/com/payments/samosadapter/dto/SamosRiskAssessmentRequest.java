package com.payments.samosadapter.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SAMOS Risk Assessment Request */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosRiskAssessmentRequest {
  private String paymentId;
  private String requestId;
  private BigDecimal transactionAmount;
  private String currency;
  private BigDecimal creditExposure;
  private String counterpartyCreditRating;
  private Double interestRateRisk;
  private Double liquidityRisk;
  private String counterpartyReputation;
  private Double counterpartyFinancialStrength;
  private String counterpartyCountry;
  private Instant transactionTime;
  private String participantStatus;
  private Boolean regulatoryCompliance;
  private String tenantId;
  private String businessUnitId;
}
