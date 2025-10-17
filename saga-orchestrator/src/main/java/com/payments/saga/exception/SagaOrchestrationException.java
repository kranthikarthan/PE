package com.payments.saga.exception;

/** Exception thrown when saga orchestration operations fail */
public class SagaOrchestrationException extends RuntimeException {

  public SagaOrchestrationException(String message) {
    super(message);
  }

  public SagaOrchestrationException(String message, Throwable cause) {
    super(message, cause);
  }
}
