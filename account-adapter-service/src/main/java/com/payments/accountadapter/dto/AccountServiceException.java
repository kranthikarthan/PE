package com.payments.accountadapter.dto;

import lombok.Getter;

/**
 * Account Service Exception
 * 
 * Custom exception for account service errors:
 * - Error details
 * - Error codes
 * - Request context
 * - Service information
 */
@Getter
public class AccountServiceException extends RuntimeException {

    private final String errorCode;
    private final String errorType;
    private final String correlationId;
    private final String requestId;
    private final String accountNumber;
    private final String tenantId;
    private final String serviceName;
    private final String operation;

    public AccountServiceException(String message, String errorCode, String errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.correlationId = null;
        this.requestId = null;
        this.accountNumber = null;
        this.tenantId = null;
        this.serviceName = "account-service";
        this.operation = null;
    }

    public AccountServiceException(String message, String errorCode, String errorType, 
                                 String correlationId, String requestId, String accountNumber, 
                                 String tenantId, String operation) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.correlationId = correlationId;
        this.requestId = requestId;
        this.accountNumber = accountNumber;
        this.tenantId = tenantId;
        this.serviceName = "account-service";
        this.operation = operation;
    }

    public AccountServiceException(String message, Throwable cause, String errorCode, String errorType) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.correlationId = null;
        this.requestId = null;
        this.accountNumber = null;
        this.tenantId = null;
        this.serviceName = "account-service";
        this.operation = null;
    }

    public AccountServiceException(String message, Throwable cause, String errorCode, String errorType,
                                 String correlationId, String requestId, String accountNumber, 
                                 String tenantId, String operation) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.correlationId = correlationId;
        this.requestId = requestId;
        this.accountNumber = accountNumber;
        this.tenantId = tenantId;
        this.serviceName = "account-service";
        this.operation = operation;
    }
}
