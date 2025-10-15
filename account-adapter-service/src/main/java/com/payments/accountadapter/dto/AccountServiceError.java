package com.payments.accountadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Account Service Error DTO
 * 
 * Error response from account service:
 * - Error details
 * - Error codes
 * - Error messages
 * - Request context
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountServiceError {

    private String errorCode;
    private String errorMessage;
    private String errorType;
    private List<String> errorDetails;
    private String correlationId;
    private String requestId;
    private String accountNumber;
    private String tenantId;
    private Long timestamp;
    private String serviceName;
    private String operation;
    private String severity;
    private String category;
}
