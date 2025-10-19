package com.payments.samosadapter.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for SAMOS settlement account */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosSettlementAccountResponse {

  private String id;
  private String tenantId;
  private String accountNumber;
  private String bankCode;
  private String bankName;
  private BigDecimal currentBalance;
  private BigDecimal reservedBalance;
  private BigDecimal availableBalance;
  private BigDecimal dailyDebitLimit;
  private BigDecimal collateralRequirement;
  private BigDecimal collateralPledged;
  private Boolean hasAdequateCollateral;
  private String status;
  private String accountType;
  private String currency;
  private Instant lastBalanceUpdate;
  private Instant lastSettlementTime;
  private Integer settlementsToday;
  private BigDecimal amountSettledToday;
  private String sarbContactEmail;
  private String sarbContactPhone;
  private Instant createdAt;
  private Instant updatedAt;
  private String createdBy;
  private String updatedBy;
}

