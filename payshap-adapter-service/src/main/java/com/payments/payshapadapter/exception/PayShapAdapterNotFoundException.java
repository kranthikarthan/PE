package com.payments.payshapadapter.exception;

import com.payments.domain.shared.ClearingAdapterId;

/**
 * Exception thrown when PayShap adapter is not found
 */
public class PayShapAdapterNotFoundException extends RuntimeException {

  public PayShapAdapterNotFoundException(String message) {
    super(message);
  }

  public PayShapAdapterNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public PayShapAdapterNotFoundException(ClearingAdapterId adapterId) {
    super("PayShap adapter not found: " + adapterId);
  }

  public PayShapAdapterNotFoundException(ClearingAdapterId adapterId, Throwable cause) {
    super("PayShap adapter not found: " + adapterId, cause);
  }
}
