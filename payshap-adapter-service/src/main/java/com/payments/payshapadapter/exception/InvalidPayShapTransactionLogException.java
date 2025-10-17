package com.payments.payshapadapter.exception;

/** Exception thrown when PayShap transaction log validation fails */
public class InvalidPayShapTransactionLogException extends RuntimeException {

  public InvalidPayShapTransactionLogException(String message) {
    super(message);
  }

  public InvalidPayShapTransactionLogException(String message, Throwable cause) {
    super(message, cause);
  }
}
