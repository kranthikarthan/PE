package com.payments.payshapadapter.exception;

/** Exception thrown when PayShap settlement record validation fails */
public class InvalidPayShapSettlementRecordException extends RuntimeException {

  public InvalidPayShapSettlementRecordException(String message) {
    super(message);
  }

  public InvalidPayShapSettlementRecordException(String message, Throwable cause) {
    super(message, cause);
  }
}
