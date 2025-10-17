package com.payments.bankservafricaadapter.exception;

/**
 * Exception thrown when BankservAfrica ISO 8583 message operations are invalid
 */
public class InvalidBankservAfricaIso8583MessageException extends RuntimeException {
    
    public InvalidBankservAfricaIso8583MessageException(String message) {
        super(message);
    }
    
    public InvalidBankservAfricaIso8583MessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
