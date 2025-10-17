package com.payments.rtcadapter.exception;

/** Exception thrown when RTC adapter operations are invalid */
public class InvalidRtcAdapterException extends RuntimeException {

  public InvalidRtcAdapterException(String message) {
    super(message);
  }

  public InvalidRtcAdapterException(String message, Throwable cause) {
    super(message, cause);
  }
}
