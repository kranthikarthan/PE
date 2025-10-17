package com.payments.samosadapter.exception;

import com.payments.domain.shared.ClearingAdapterId;

/**
 * Exception thrown when SAMOS adapter is not found
 */
public class SamosAdapterNotFoundException extends RuntimeException {

  public SamosAdapterNotFoundException(String message) {
    super(message);
  }

  public SamosAdapterNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public SamosAdapterNotFoundException(ClearingAdapterId adapterId) {
    super("SAMOS adapter not found: " + adapterId);
  }

  public SamosAdapterNotFoundException(ClearingAdapterId adapterId, Throwable cause) {
    super("SAMOS adapter not found: " + adapterId, cause);
  }
}
