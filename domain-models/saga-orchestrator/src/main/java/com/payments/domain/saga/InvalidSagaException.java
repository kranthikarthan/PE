package com.payments.domain.saga;

/**
 * Domain Exception: Invalid Saga
 */
public class InvalidSagaException extends RuntimeException {
    public InvalidSagaException(String message) {
        super(message);
    }
    
    public InvalidSagaException(String message, Throwable cause) {
        super(message, cause);
    }
}
