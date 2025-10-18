package com.payments.iam.exception;

/**
 * Exception thrown when user lacks required authorization.
 *
 * <p>Maps to HTTP 403 Forbidden
 * Used for: insufficient roles, missing permissions, access denied
 */
public class ForbiddenException extends IamException {

  public ForbiddenException(String message) {
    super(message, 403);
  }

  public ForbiddenException(String message, Throwable cause) {
    super(message, cause, 403);
  }
}
