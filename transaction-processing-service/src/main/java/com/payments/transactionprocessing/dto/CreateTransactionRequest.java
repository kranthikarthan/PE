package com.payments.transactionprocessing.dto;

import com.payments.domain.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
    
    @NotBlank(message = "Business Unit ID is required")
    private String businessUnitId;
    
    @NotBlank(message = "Payment ID is required")
    private String paymentId;
    
    @NotBlank(message = "Debit account is required")
    private String debitAccount;
    
    @NotBlank(message = "Credit account is required")
    private String creditAccount;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
}






