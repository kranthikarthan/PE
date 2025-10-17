package com.payments.rtcadapter.exception;

/** Exception thrown when RTC settlement record operations are invalid */
public class InvalidRtcSettlementRecordException extends RuntimeException {

  public InvalidRtcSettlementRecordException(String message) {
    super(message);
  }

  public InvalidRtcSettlementRecordException(String message, Throwable cause) {
    super(message, cause);
  }
}
