package com.paymentengine.corebanking.exception;

/**
 * Exception thrown when certificate operations fail
 */
public class CertificateException extends RuntimeException {
    
    public CertificateException(String message) {
        super(message);
    }
    
    public CertificateException(String message, Throwable cause) {
        super(message, cause);
    }
}