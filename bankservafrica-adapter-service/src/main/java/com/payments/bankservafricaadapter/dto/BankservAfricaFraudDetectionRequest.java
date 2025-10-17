package com.payments.bankservafricaadapter.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** BankservAfrica Fraud Detection Request */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaFraudDetectionRequest {
  private String paymentId;
  private String requestId;
  private BigDecimal transactionAmount;
  private String currency;
  private String beneficiaryName;
  private String beneficiaryAccount;
  private String beneficiaryCountry;
  private String originCountry;
  private String paymentReference;
  private String transactionType;
  private Integer transactionCount;
  private Long timeSinceLastTransaction;
  private Instant transactionTime;
  private Integer accountAge;
  private Boolean isDuplicateTransaction;
  private String tenantId;
  private String businessUnitId;
}
