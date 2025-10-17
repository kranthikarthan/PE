package com.payments.bankservafricaadapter.exception;

/**
 * Exception thrown when BankservAfrica adapter already exists
 */
public class BankservAfricaAdapterAlreadyExistsException extends RuntimeException {

  public BankservAfricaAdapterAlreadyExistsException(String message) {
    super(message);
  }

  public BankservAfricaAdapterAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public BankservAfricaAdapterAlreadyExistsException(String adapterName, String tenantId) {
    super("BankservAfrica adapter already exists for tenant: " + adapterName + " (tenant: " + tenantId + ")");
  }
}
