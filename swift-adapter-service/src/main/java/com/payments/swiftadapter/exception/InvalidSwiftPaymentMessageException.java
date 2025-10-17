package com.payments.swiftadapter.exception;

/**
 * Exception thrown when SWIFT payment message validation fails
 */
public class InvalidSwiftPaymentMessageException extends RuntimeException {

  public InvalidSwiftPaymentMessageException(String message) {
    super(message);
  }

  public InvalidSwiftPaymentMessageException(String message, Throwable cause) {
    super(message, cause);
  }
}
