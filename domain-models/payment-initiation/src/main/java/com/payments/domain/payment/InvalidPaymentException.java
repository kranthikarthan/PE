package com.payments.domain.payment;

public class InvalidPaymentException extends RuntimeException {
  public InvalidPaymentException(String message) {
    super(message);
  }

  public InvalidPaymentException(String message, Throwable cause) {
    super(message, cause);
  }
}
