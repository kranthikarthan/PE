package com.payments.swiftadapter.exception;

/**
 * Exception thrown when SWIFT adapter validation fails
 */
public class InvalidSwiftAdapterException extends RuntimeException {

  public InvalidSwiftAdapterException(String message) {
    super(message);
  }

  public InvalidSwiftAdapterException(String message, Throwable cause) {
    super(message, cause);
  }
}
