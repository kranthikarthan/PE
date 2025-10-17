package com.payments.bankservafricaadapter.exception;

/**
 * Exception thrown when BankservAfrica ACH transaction operations are invalid
 */
public class InvalidBankservAfricaAchTransactionException extends RuntimeException {
    
    public InvalidBankservAfricaAchTransactionException(String message) {
        super(message);
    }
    
    public InvalidBankservAfricaAchTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
