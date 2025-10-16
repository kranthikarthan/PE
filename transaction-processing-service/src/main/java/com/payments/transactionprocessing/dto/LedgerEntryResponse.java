package com.payments.transactionprocessing.dto;

import com.payments.domain.transaction.LedgerEntry;
import com.payments.domain.transaction.LedgerEntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryResponse {
    private String entryId;
    private String transactionId;
    private String tenantId;
    private String businessUnitId;
    private String accountNumber;
    private LedgerEntryType entryType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDate entryDate;
    private Instant createdAt;

    public static LedgerEntryResponse fromDomain(LedgerEntry entry) {
        return LedgerEntryResponse.builder()
                .entryId(entry.getId().getValue())
                .transactionId(entry.getTransactionId().getValue())
                .tenantId(entry.getTenantContext().getTenantId())
                .businessUnitId(entry.getTenantContext().getBusinessUnitId())
                .accountNumber(entry.getAccountNumber().getValue())
                .entryType(entry.getEntryType())
                .amount(entry.getAmount())
                .balanceBefore(entry.getBalanceBefore())
                .balanceAfter(entry.getBalanceAfter())
                .entryDate(entry.getEntryDate())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}






