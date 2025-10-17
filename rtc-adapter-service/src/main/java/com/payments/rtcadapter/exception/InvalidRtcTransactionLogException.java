package com.payments.rtcadapter.exception;

/** Exception thrown when RTC transaction log operations are invalid */
public class InvalidRtcTransactionLogException extends RuntimeException {

  public InvalidRtcTransactionLogException(String message) {
    super(message);
  }

  public InvalidRtcTransactionLogException(String message, Throwable cause) {
    super(message, cause);
  }
}
