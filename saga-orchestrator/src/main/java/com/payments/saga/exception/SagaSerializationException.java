package com.payments.saga.exception;

/** Exception thrown when saga serialization/deserialization operations fail */
public class SagaSerializationException extends RuntimeException {

  public SagaSerializationException(String message) {
    super(message);
  }

  public SagaSerializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
