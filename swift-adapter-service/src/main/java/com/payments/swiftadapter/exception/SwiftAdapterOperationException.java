package com.payments.swiftadapter.exception;

/**
 * Exception thrown when SWIFT adapter operation fails
 */
public class SwiftAdapterOperationException extends RuntimeException {

  public SwiftAdapterOperationException(String message) {
    super(message);
  }

  public SwiftAdapterOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public SwiftAdapterOperationException(String operation, String reason) {
    super("SWIFT adapter operation failed: " + operation + " - " + reason);
  }
}
