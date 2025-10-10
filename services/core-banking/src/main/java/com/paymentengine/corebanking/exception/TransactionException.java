package com.paymentengine.corebanking.exception;

/**
 * Base exception for transaction-related errors
 */
public class TransactionException extends RuntimeException {
    
    private final String errorCode;
    private final String transactionReference;
    
    public TransactionException(String message) {
        super(message);
        this.errorCode = "TRANSACTION_ERROR";
        this.transactionReference = null;
    }
    
    public TransactionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.transactionReference = null;
    }
    
    public TransactionException(String message, String errorCode, String transactionReference) {
        super(message);
        this.errorCode = errorCode;
        this.transactionReference = transactionReference;
    }
    
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TRANSACTION_ERROR";
        this.transactionReference = null;
    }
    
    public TransactionException(String message, String errorCode, Throwable cause) {
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