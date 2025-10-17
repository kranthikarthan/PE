package com.payments.saga.exception;

/** Exception thrown when tenant context resolution fails */
public class TenantContextResolutionException extends RuntimeException {

  public TenantContextResolutionException(String message) {
    super(message);
  }

  public TenantContextResolutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
