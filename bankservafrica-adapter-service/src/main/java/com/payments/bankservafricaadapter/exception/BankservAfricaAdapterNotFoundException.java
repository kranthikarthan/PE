package com.payments.bankservafricaadapter.exception;

import com.payments.domain.shared.ClearingAdapterId;

/** Exception thrown when BankservAfrica adapter is not found */
public class BankservAfricaAdapterNotFoundException extends RuntimeException {

  public BankservAfricaAdapterNotFoundException(String message) {
    super(message);
  }

  public BankservAfricaAdapterNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public BankservAfricaAdapterNotFoundException(ClearingAdapterId adapterId) {
    super("BankservAfrica adapter not found: " + adapterId);
  }

  public BankservAfricaAdapterNotFoundException(ClearingAdapterId adapterId, Throwable cause) {
    super("BankservAfrica adapter not found: " + adapterId, cause);
  }
}
