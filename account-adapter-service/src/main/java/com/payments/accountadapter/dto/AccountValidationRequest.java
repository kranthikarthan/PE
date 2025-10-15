package com.payments.accountadapter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account Validation Request DTO
 * 
 * Request for validating account information:
 * - Account details
 * - Validation criteria
 * - Request metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountValidationRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Business unit ID is required")
    private String businessUnitId;

    @NotNull(message = "Request timestamp is required")
    private Long requestTimestamp;

    private String accountHolderName;
    private String accountType;
    private String validationType;
    private String correlationId;
    private String requestId;
    private String clientId;
}
