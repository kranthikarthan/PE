package com.payments.accountadapter.client;

import com.payments.accountadapter.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Account Service Feign Client
 * 
 * Feign client for external account service integration:
 * - Account balance retrieval
 * - Account validation
 * - Account status checking
 * - OAuth2 authentication
 */
@FeignClient(
    name = "account-service",
    url = "${account.service.url}",
    configuration = AccountServiceClientConfig.class
)
public interface AccountServiceClient {

    /**
     * Get account balance
     * 
     * @param request Account balance request
     * @param authorization OAuth2 authorization header
     * @param correlationId Correlation ID header
     * @param tenantId Tenant ID header
     * @return Account balance response
     */
    @PostMapping("/api/v1/accounts/balance")
    AccountBalanceResponse getAccountBalance(
            @RequestBody AccountBalanceRequest request,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Correlation-ID") String correlationId,
            @RequestHeader("X-Tenant-ID") String tenantId
    );

    /**
     * Validate account
     * 
     * @param request Account validation request
     * @param authorization OAuth2 authorization header
     * @param correlationId Correlation ID header
     * @param tenantId Tenant ID header
     * @return Account validation response
     */
    @PostMapping("/api/v1/accounts/validate")
    AccountValidationResponse validateAccount(
            @RequestBody AccountValidationRequest request,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Correlation-ID") String correlationId,
            @RequestHeader("X-Tenant-ID") String tenantId
    );

    /**
     * Get account status
     * 
     * @param request Account status request
     * @param authorization OAuth2 authorization header
     * @param correlationId Correlation ID header
     * @param tenantId Tenant ID header
     * @return Account status response
     */
    @PostMapping("/api/v1/accounts/status")
    AccountStatusResponse getAccountStatus(
            @RequestBody AccountStatusRequest request,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Correlation-ID") String correlationId,
            @RequestHeader("X-Tenant-ID") String tenantId
    );
}
