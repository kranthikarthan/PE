package com.payments.samosadapter.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for SAMOS settlement account balance information */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosSettlementAccountBalanceResponse {

  private String accountId;
  private String accountNumber;
  private String bankCode;
  private BigDecimal currentBalance;
  private BigDecimal reservedBalance;
  private BigDecimal availableBalance;
  private BigDecimal collateralPledged;
  private BigDecimal collateralRequirement;
  private Boolean hasAdequateCollateral;
  private BigDecimal dailyDebitLimit;
  private BigDecimal amountSettledToday;
  private Integer settlementsToday;
  private BigDecimal remainingDailyLimit;
  private Instant lastBalanceUpdate;
  private Instant lastSettlementTime;
  private String status;
  private String currency;
}
