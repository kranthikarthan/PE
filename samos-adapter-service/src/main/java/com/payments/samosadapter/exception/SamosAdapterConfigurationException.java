package com.payments.samosadapter.exception;

/**
 * Exception thrown when SAMOS adapter configuration is invalid
 */
public class SamosAdapterConfigurationException extends RuntimeException {

  public SamosAdapterConfigurationException(String message) {
    super(message);
  }

  public SamosAdapterConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  public SamosAdapterConfigurationException(String field, String value) {
    super("Invalid SAMOS adapter configuration: " + field + " = " + value);
  }
}
