package com.paymentengine.middleware.exception;

/**
 * Exception for account-related errors in middleware
 */
public class AccountException extends CoreBankingException {
    
    private final String accountId;
    
    public AccountException(String message, String accountId) {
        super(message, "ACCOUNT_ERROR");
        this.accountId = accountId;
    }
    
    public AccountException(String message, String errorCode, String accountId) {
        super(message, errorCode);
        this.accountId = accountId;
    }
    
    public AccountException(String message, String accountId, Throwable cause) {
        super(message, "ACCOUNT_ERROR", cause);
        this.accountId = accountId;
    }
    
    public String getAccountId() {
        return accountId;
    }
}