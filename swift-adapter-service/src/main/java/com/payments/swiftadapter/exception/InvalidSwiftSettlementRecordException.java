package com.payments.swiftadapter.exception;

/** Exception thrown when SWIFT settlement record validation fails */
public class InvalidSwiftSettlementRecordException extends RuntimeException {

  public InvalidSwiftSettlementRecordException(String message) {
    super(message);
  }

  public InvalidSwiftSettlementRecordException(String message, Throwable cause) {
    super(message, cause);
  }
}
