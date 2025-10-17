package com.payments.payshapadapter.exception;

/** Exception thrown when PayShap adapter validation fails */
public class InvalidPayShapAdapterException extends RuntimeException {

  public InvalidPayShapAdapterException(String message) {
    super(message);
  }

  public InvalidPayShapAdapterException(String message, Throwable cause) {
    super(message, cause);
  }
}
