package com.payments.payshapadapter.exception;

/** Exception thrown when PayShap payment message validation fails */
public class InvalidPayShapPaymentMessageException extends RuntimeException {

  public InvalidPayShapPaymentMessageException(String message) {
    super(message);
  }

  public InvalidPayShapPaymentMessageException(String message, Throwable cause) {
    super(message, cause);
  }
}
