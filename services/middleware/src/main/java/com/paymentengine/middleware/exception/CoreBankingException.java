package com.paymentengine.middleware.exception;

/**
 * Base exception for core banking-related errors
 */
public class CoreBankingException extends RuntimeException {
    
    private final String errorCode;
    private final String transactionReference;
    
    public CoreBankingException(String message) {
        super(message);
        this.errorCode = "CORE_BANKING_ERROR";
        this.transactionReference = null;
    }
    
    public CoreBankingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.transactionReference = null;
    }
    
    public CoreBankingException(String message, String errorCode, String transactionReference) {
        super(message);
        this.errorCode = errorCode;
        this.transactionReference = transactionReference;
    }
    
    public CoreBankingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CORE_BANKING_ERROR";
        this.transactionReference = null;
    }
    
    public CoreBankingException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.transactionReference = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getTransactionReference() {
        return transactionReference;
    }
}