package com.payments.samosadapter.exception;

/**
 * Exception thrown when SAMOS adapter operation fails
 */
public class SamosAdapterOperationException extends RuntimeException {

  public SamosAdapterOperationException(String message) {
    super(message);
  }

  public SamosAdapterOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public SamosAdapterOperationException(String operation, String reason) {
    super("SAMOS adapter operation failed: " + operation + " - " + reason);
  }
}
