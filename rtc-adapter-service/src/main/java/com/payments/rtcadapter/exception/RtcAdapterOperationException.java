package com.payments.rtcadapter.exception;

/** Exception thrown when RTC adapter operation fails */
public class RtcAdapterOperationException extends RuntimeException {

  public RtcAdapterOperationException(String message) {
    super(message);
  }

  public RtcAdapterOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public RtcAdapterOperationException(String operation, String reason) {
    super("RTC adapter operation failed: " + operation + " - " + reason);
  }
}
