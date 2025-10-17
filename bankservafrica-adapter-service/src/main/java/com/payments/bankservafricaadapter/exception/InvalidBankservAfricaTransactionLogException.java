package com.payments.bankservafricaadapter.exception;

/**
 * Exception thrown when BankservAfrica transaction log operations are invalid
 */
public class InvalidBankservAfricaTransactionLogException extends RuntimeException {
    
    public InvalidBankservAfricaTransactionLogException(String message) {
        super(message);
    }
    
    public InvalidBankservAfricaTransactionLogException(String message, Throwable cause) {
        super(message, cause);
    }
}
