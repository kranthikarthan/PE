package com.payments.samosadapter.exception;

/** Exception thrown when SAMOS adapter validation fails */
public class InvalidSamosAdapterException extends RuntimeException {

  public InvalidSamosAdapterException(String message) {
    super(message);
  }

  public InvalidSamosAdapterException(String message, Throwable cause) {
    super(message, cause);
  }
}
