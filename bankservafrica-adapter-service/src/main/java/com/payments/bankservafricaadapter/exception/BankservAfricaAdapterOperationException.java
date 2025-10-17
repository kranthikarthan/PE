package com.payments.bankservafricaadapter.exception;

/**
 * Exception thrown when BankservAfrica adapter operation fails
 */
public class BankservAfricaAdapterOperationException extends RuntimeException {

  public BankservAfricaAdapterOperationException(String message) {
    super(message);
  }

  public BankservAfricaAdapterOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public BankservAfricaAdapterOperationException(String operation, String reason) {
    super("BankservAfrica adapter operation failed: " + operation + " - " + reason);
  }
}
