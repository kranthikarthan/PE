package com.payments.accountadapter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account Balance Request DTO
 * 
 * Request for retrieving account balance:
 * - Account number
 * - Tenant context
 * - Request metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Business unit ID is required")
    private String businessUnitId;

    @NotNull(message = "Request timestamp is required")
    private Long requestTimestamp;

    private String correlationId;
    private String requestId;
    private String clientId;
}
