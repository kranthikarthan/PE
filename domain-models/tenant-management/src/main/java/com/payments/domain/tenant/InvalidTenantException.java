package com.payments.domain.tenant;

/**
 * Domain Exception: Invalid Tenant
 */
public class InvalidTenantException extends RuntimeException {
    public InvalidTenantException(String message) {
        super(message);
    }
    
    public InvalidTenantException(String message, Throwable cause) {
        super(message, cause);
    }
}
