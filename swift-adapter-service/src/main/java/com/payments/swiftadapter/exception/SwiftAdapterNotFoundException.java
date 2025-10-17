package com.payments.swiftadapter.exception;

import com.payments.domain.shared.ClearingAdapterId;

/** Exception thrown when SWIFT adapter is not found */
public class SwiftAdapterNotFoundException extends RuntimeException {

  public SwiftAdapterNotFoundException(String message) {
    super(message);
  }

  public SwiftAdapterNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public SwiftAdapterNotFoundException(ClearingAdapterId adapterId) {
    super("SWIFT adapter not found: " + adapterId);
  }

  public SwiftAdapterNotFoundException(ClearingAdapterId adapterId, Throwable cause) {
    super("SWIFT adapter not found: " + adapterId, cause);
  }
}
