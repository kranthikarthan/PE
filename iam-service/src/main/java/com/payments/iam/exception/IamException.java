package com.payments.iam.exception;

/**
 * Base exception for all IAM service operations.
 *
 * <p>Used for: resource not found, invalid state, constraint violations
 */
public class IamException extends RuntimeException {

  private final int statusCode;

  public IamException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public IamException(String message, Throwable cause, int statusCode) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
