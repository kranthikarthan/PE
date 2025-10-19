package com.payments.domain.exceptions;

/**
 * Domain exception for invalid payment operations
 */
public class InvalidPaymentException extends RuntimeException {
    
    public InvalidPaymentException(String message) {
        super(message);
    }
    
    public InvalidPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
