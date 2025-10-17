package com.payments.saga.exception;

/** Exception thrown when saga persistence operations fail */
public class SagaPersistenceException extends RuntimeException {

  public SagaPersistenceException(String message) {
    super(message);
  }

  public SagaPersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
