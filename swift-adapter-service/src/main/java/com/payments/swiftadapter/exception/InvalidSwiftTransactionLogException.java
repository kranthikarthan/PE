package com.payments.swiftadapter.exception;

/**
 * Exception thrown when SWIFT transaction log validation fails
 */
public class InvalidSwiftTransactionLogException extends RuntimeException {

  public InvalidSwiftTransactionLogException(String message) {
    super(message);
  }

  public InvalidSwiftTransactionLogException(String message, Throwable cause) {
    super(message, cause);
  }
}
