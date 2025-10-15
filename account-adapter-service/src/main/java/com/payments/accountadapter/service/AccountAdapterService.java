package com.payments.accountadapter.service;

import com.payments.accountadapter.client.AccountServiceClient;
import com.payments.accountadapter.client.AccountServiceFallback;
import com.payments.accountadapter.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Account Adapter Service
 * 
 * Service for account system integration with resilience patterns:
 * - Circuit breaker protection
 * - Retry logic
 * - Timeout handling
 * - Fallback mechanisms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountAdapterService {

    private final AccountServiceClient accountServiceClient;
    private final AccountServiceFallback accountServiceFallback;
    private final OAuth2TokenService oAuth2TokenService;
    private final AccountCacheService accountCacheService;

    /**
     * Get account balance with resilience patterns
     * 
     * @param accountNumber Account number
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @param correlationId Correlation ID
     * @return Account balance response
     */
    @CircuitBreaker(name = "account-service", fallbackMethod = "getAccountBalanceFallback")
    @Retry(name = "account-service")
    @TimeLimiter(name = "account-service")
    public CompletableFuture<AccountBalanceResponse> getAccountBalance(
            String accountNumber, String tenantId, String businessUnitId, String correlationId) {
        
        log.debug("Getting account balance for account: {} with correlation: {}", 
                accountNumber, correlationId);
        
        try {
            // Check cache first
            AccountBalanceResponse cachedResponse = accountCacheService.getCachedAccountBalance(accountNumber, tenantId);
            if (cachedResponse != null) {
                log.debug("Using cached account balance for account: {}", accountNumber);
                return CompletableFuture.completedFuture(cachedResponse);
            }
            
            String accessToken = oAuth2TokenService.getAccessToken();
            
            AccountBalanceRequest request = AccountBalanceRequest.builder()
                    .accountNumber(accountNumber)
                    .tenantId(tenantId)
                    .businessUnitId(businessUnitId)
                    .requestTimestamp(System.currentTimeMillis())
                    .correlationId(correlationId)
                    .requestId(UUID.randomUUID().toString())
                    .build();
            
            AccountBalanceResponse response = accountServiceClient.getAccountBalance(
                    request, 
                    "Bearer " + accessToken, 
                    correlationId, 
                    tenantId
            );
            
            // Cache the response
            accountCacheService.cacheAccountBalance(accountNumber, tenantId, response);
            
            log.info("Successfully retrieved account balance for account: {} with correlation: {}", 
                    accountNumber, correlationId);
            
            return CompletableFuture.completedFuture(response);
            
        } catch (Exception e) {
            log.error("Failed to get account balance for account: {} with correlation: {}", 
                    accountNumber, correlationId, e);
            throw new AccountServiceException(
                    "Failed to get account balance", 
                    e, 
                    "ACCOUNT_BALANCE_ERROR", 
                    "SERVICE_ERROR",
                    correlationId,
                    UUID.randomUUID().toString(),
                    accountNumber,
                    tenantId,
                    "getAccountBalance"
            );
        }
    }

    /**
     * Validate account with resilience patterns
     * 
     * @param accountNumber Account number
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @param correlationId Correlation ID
     * @return Account validation response
     */
    @CircuitBreaker(name = "account-service", fallbackMethod = "validateAccountFallback")
    @Retry(name = "account-service")
    @TimeLimiter(name = "account-service")
    public CompletableFuture<AccountValidationResponse> validateAccount(
            String accountNumber, String tenantId, String businessUnitId, String correlationId) {
        
        log.debug("Validating account: {} with correlation: {}", accountNumber, correlationId);
        
        try {
            // Check cache first
            AccountValidationResponse cachedResponse = accountCacheService.getCachedAccountValidation(accountNumber, tenantId);
            if (cachedResponse != null) {
                log.debug("Using cached account validation for account: {}", accountNumber);
                return CompletableFuture.completedFuture(cachedResponse);
            }
            
            String accessToken = oAuth2TokenService.getAccessToken();
            
            AccountValidationRequest request = AccountValidationRequest.builder()
                    .accountNumber(accountNumber)
                    .tenantId(tenantId)
                    .businessUnitId(businessUnitId)
                    .requestTimestamp(System.currentTimeMillis())
                    .correlationId(correlationId)
                    .requestId(UUID.randomUUID().toString())
                    .build();
            
            AccountValidationResponse response = accountServiceClient.validateAccount(
                    request, 
                    "Bearer " + accessToken, 
                    correlationId, 
                    tenantId
            );
            
            // Cache the response
            accountCacheService.cacheAccountValidation(accountNumber, tenantId, response);
            
            log.info("Successfully validated account: {} with correlation: {}", 
                    accountNumber, correlationId);
            
            return CompletableFuture.completedFuture(response);
            
        } catch (Exception e) {
            log.error("Failed to validate account: {} with correlation: {}", 
                    accountNumber, correlationId, e);
            throw new AccountServiceException(
                    "Failed to validate account", 
                    e, 
                    "ACCOUNT_VALIDATION_ERROR", 
                    "SERVICE_ERROR",
                    correlationId,
                    UUID.randomUUID().toString(),
                    accountNumber,
                    tenantId,
                    "validateAccount"
            );
        }
    }

    /**
     * Get account status with resilience patterns
     * 
     * @param accountNumber Account number
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @param correlationId Correlation ID
     * @return Account status response
     */
    @CircuitBreaker(name = "account-service", fallbackMethod = "getAccountStatusFallback")
    @Retry(name = "account-service")
    @TimeLimiter(name = "account-service")
    public CompletableFuture<AccountStatusResponse> getAccountStatus(
            String accountNumber, String tenantId, String businessUnitId, String correlationId) {
        
        log.debug("Getting account status for account: {} with correlation: {}", 
                accountNumber, correlationId);
        
        try {
            // Check cache first
            AccountStatusResponse cachedResponse = accountCacheService.getCachedAccountStatus(accountNumber, tenantId);
            if (cachedResponse != null) {
                log.debug("Using cached account status for account: {}", accountNumber);
                return CompletableFuture.completedFuture(cachedResponse);
            }
            
            String accessToken = oAuth2TokenService.getAccessToken();
            
            AccountStatusRequest request = AccountStatusRequest.builder()
                    .accountNumber(accountNumber)
                    .tenantId(tenantId)
                    .businessUnitId(businessUnitId)
                    .requestTimestamp(System.currentTimeMillis())
                    .correlationId(correlationId)
                    .requestId(UUID.randomUUID().toString())
                    .build();
            
            AccountStatusResponse response = accountServiceClient.getAccountStatus(
                    request, 
                    "Bearer " + accessToken, 
                    correlationId, 
                    tenantId
            );
            
            // Cache the response
            accountCacheService.cacheAccountStatus(accountNumber, tenantId, response);
            
            log.info("Successfully retrieved account status for account: {} with correlation: {}", 
                    accountNumber, correlationId);
            
            return CompletableFuture.completedFuture(response);
            
        } catch (Exception e) {
            log.error("Failed to get account status for account: {} with correlation: {}", 
                    accountNumber, correlationId, e);
            throw new AccountServiceException(
                    "Failed to get account status", 
                    e, 
                    "ACCOUNT_STATUS_ERROR", 
                    "SERVICE_ERROR",
                    correlationId,
                    UUID.randomUUID().toString(),
                    accountNumber,
                    tenantId,
                    "getAccountStatus"
            );
        }
    }

    /**
     * Fallback method for account balance
     */
    public CompletableFuture<AccountBalanceResponse> getAccountBalanceFallback(
            String accountNumber, String tenantId, String businessUnitId, String correlationId, Exception ex) {
        
        log.warn("Using fallback for account balance: {} with correlation: {}", 
                accountNumber, correlationId);
        
        AccountBalanceRequest request = AccountBalanceRequest.builder()
                .accountNumber(accountNumber)
                .tenantId(tenantId)
                .businessUnitId(businessUnitId)
                .requestTimestamp(System.currentTimeMillis())
                .correlationId(correlationId)
                .requestId(UUID.randomUUID().toString())
                .build();
        
        AccountBalanceResponse response = accountServiceFallback.getAccountBalance(
                request, "Bearer fallback", correlationId, tenantId);
        
        return CompletableFuture.completedFuture(response);
    }

    /**
     * Fallback method for account validation
     */
    public CompletableFuture<AccountValidationResponse> validateAccountFallback(
            String accountNumber, String tenantId, String businessUnitId, String correlationId, Exception ex) {
        
        log.warn("Using fallback for account validation: {} with correlation: {}", 
                accountNumber, correlationId);
        
        AccountValidationRequest request = AccountValidationRequest.builder()
                .accountNumber(accountNumber)
                .tenantId(tenantId)
                .businessUnitId(businessUnitId)
                .requestTimestamp(System.currentTimeMillis())
                .correlationId(correlationId)
                .requestId(UUID.randomUUID().toString())
                .build();
        
        AccountValidationResponse response = accountServiceFallback.validateAccount(
                request, "Bearer fallback", correlationId, tenantId);
        
        return CompletableFuture.completedFuture(response);
    }

    /**
     * Fallback method for account status
     */
    public CompletableFuture<AccountStatusResponse> getAccountStatusFallback(
            String accountNumber, String tenantId, String businessUnitId, String correlationId, Exception ex) {
        
        log.warn("Using fallback for account status: {} with correlation: {}", 
                accountNumber, correlationId);
        
        AccountStatusRequest request = AccountStatusRequest.builder()
                .accountNumber(accountNumber)
                .tenantId(tenantId)
                .businessUnitId(businessUnitId)
                .requestTimestamp(System.currentTimeMillis())
                .correlationId(correlationId)
                .requestId(UUID.randomUUID().toString())
                .build();
        
        AccountStatusResponse response = accountServiceFallback.getAccountStatus(
                request, "Bearer fallback", correlationId, tenantId);
        
        return CompletableFuture.completedFuture(response);
    }
}
