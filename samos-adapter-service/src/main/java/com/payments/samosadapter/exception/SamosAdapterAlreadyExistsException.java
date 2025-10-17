package com.payments.samosadapter.exception;

/** Exception thrown when SAMOS adapter already exists */
public class SamosAdapterAlreadyExistsException extends RuntimeException {

  public SamosAdapterAlreadyExistsException(String message) {
    super(message);
  }

  public SamosAdapterAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public SamosAdapterAlreadyExistsException(String adapterName, String tenantId) {
    super(
        "SAMOS adapter already exists for tenant: " + adapterName + " (tenant: " + tenantId + ")");
  }
}
