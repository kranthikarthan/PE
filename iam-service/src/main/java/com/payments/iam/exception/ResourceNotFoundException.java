package com.payments.iam.exception;

/**
 * Exception thrown when a requested resource is not found.
 *
 * <p>Maps to HTTP 404 Not Found
 */
public class ResourceNotFoundException extends IamException {

  public ResourceNotFoundException(String message) {
    super(message, 404);
  }

  public ResourceNotFoundException(String resourceType, String identifier) {
    super(String.format("%s not found: %s", resourceType, identifier), 404);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause, 404);
  }
}
