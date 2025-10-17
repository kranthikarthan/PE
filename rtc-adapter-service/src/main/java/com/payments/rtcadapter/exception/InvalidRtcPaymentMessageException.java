package com.payments.rtcadapter.exception;

/** Exception thrown when RTC payment message operations are invalid */
public class InvalidRtcPaymentMessageException extends RuntimeException {

  public InvalidRtcPaymentMessageException(String message) {
    super(message);
  }

  public InvalidRtcPaymentMessageException(String message, Throwable cause) {
    super(message, cause);
  }
}
