package com.payments.samosadapter.exception;

/** Exception thrown for SAMOS settlement account operations */
public class SamosSettlementAccountException extends RuntimeException {

  public SamosSettlementAccountException(String message) {
    super(message);
  }

  public SamosSettlementAccountException(String message, Throwable cause) {
    super(message, cause);
  }
}
