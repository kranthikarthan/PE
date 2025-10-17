package com.payments.payshapadapter.exception;

/** Exception thrown when PayShap adapter operation fails */
public class PayShapAdapterOperationException extends RuntimeException {

  public PayShapAdapterOperationException(String message) {
    super(message);
  }

  public PayShapAdapterOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public PayShapAdapterOperationException(String operation, String reason) {
    super("PayShap adapter operation failed: " + operation + " - " + reason);
  }
}
