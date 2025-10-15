package com.payments.accountadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Account Balance Response DTO
 * 
 * Response containing account balance information:
 * - Account details
 * - Balance information
 * - Account status
 * - Response metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {

    private String accountNumber;
    private String accountHolderName;
    private String accountType;
    private String accountStatus;
    private BigDecimal availableBalance;
    private BigDecimal ledgerBalance;
    private String currency;
    private Instant lastTransactionDate;
    private Instant balanceAsOf;
    private String responseCode;
    private String responseMessage;
    private String correlationId;
    private Long responseTimestamp;
    private String requestId;
}
