package com.payments.domain.account;

/**
 * Domain Exception: Invalid Account Adapter
 */
public class InvalidAccountAdapterException extends RuntimeException {
    public InvalidAccountAdapterException(String message) {
        super(message);
    }
    
    public InvalidAccountAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
