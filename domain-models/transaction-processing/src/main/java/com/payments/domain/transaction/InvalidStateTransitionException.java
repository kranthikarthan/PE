package com.payments.domain.transaction;

public class InvalidStateTransitionException extends RuntimeException {
  public InvalidStateTransitionException(String message) {
    super(message);
  }
}
