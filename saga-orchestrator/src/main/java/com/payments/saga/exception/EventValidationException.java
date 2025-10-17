package com.payments.saga.exception;

/** Exception thrown when event validation fails */
public class EventValidationException extends RuntimeException {

  public EventValidationException(String message) {
    super(message);
  }

  public EventValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
