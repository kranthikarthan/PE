package com.payments.accountadapter.client;

import com.payments.accountadapter.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Account Service Fallback
 * 
 * Fallback implementation for account service client:
 * - Circuit breaker fallback
 * - Error handling
 * - Default responses
 * - Logging
 */
@Slf4j
@Component
public class AccountServiceFallback implements AccountServiceClient {

    @Override
    public AccountBalanceResponse getAccountBalance(
            AccountBalanceRequest request,
            String authorization,
            String correlationId,
            String tenantId) {
        
        log.warn("Account service fallback triggered for balance request: {} with correlation: {}", 
                request.getAccountNumber(), correlationId);
        
        return AccountBalanceResponse.builder()
                .accountNumber(request.getAccountNumber())
                .accountHolderName("Unknown")
                .accountType("Unknown")
                .accountStatus("UNKNOWN")
                .availableBalance(BigDecimal.ZERO)
                .ledgerBalance(BigDecimal.ZERO)
                .currency("ZAR")
                .lastTransactionDate(Instant.now())
                .balanceAsOf(Instant.now())
                .responseCode("FALLBACK")
                .responseMessage("Account service unavailable - using fallback")
                .correlationId(correlationId)
                .responseTimestamp(System.currentTimeMillis())
                .requestId(request.getRequestId())
                .build();
    }

    @Override
    public AccountValidationResponse validateAccount(
            AccountValidationRequest request,
            String authorization,
            String correlationId,
            String tenantId) {
        
        log.warn("Account service fallback triggered for validation request: {} with correlation: {}", 
                request.getAccountNumber(), correlationId);
        
        return AccountValidationResponse.builder()
                .accountNumber(request.getAccountNumber())
                .accountHolderName("Unknown")
                .accountType("Unknown")
                .accountStatus("UNKNOWN")
                .isValid(false)
                .validationStatus("FALLBACK")
                .validationErrors(List.of("Account service unavailable"))
                .validationWarnings(List.of("Using fallback response"))
                .responseCode("FALLBACK")
                .responseMessage("Account service unavailable - validation failed")
                .correlationId(correlationId)
                .responseTimestamp(System.currentTimeMillis())
                .requestId(request.getRequestId())
                .validatedAt(Instant.now())
                .build();
    }

    @Override
    public AccountStatusResponse getAccountStatus(
            AccountStatusRequest request,
            String authorization,
            String correlationId,
            String tenantId) {
        
        log.warn("Account service fallback triggered for status request: {} with correlation: {}", 
                request.getAccountNumber(), correlationId);
        
        return AccountStatusResponse.builder()
                .accountNumber(request.getAccountNumber())
                .accountHolderName("Unknown")
                .accountType("Unknown")
                .accountStatus("UNKNOWN")
                .statusReason("Account service unavailable")
                .statusChangedAt(Instant.now())
                .previousStatus("UNKNOWN")
                .restrictions(List.of("Service unavailable"))
                .permissions(List.of())
                .responseCode("FALLBACK")
                .responseMessage("Account service unavailable - using fallback")
                .correlationId(correlationId)
                .responseTimestamp(System.currentTimeMillis())
                .requestId(request.getRequestId())
                .lastActivityDate(Instant.now())
                .build();
    }
}
