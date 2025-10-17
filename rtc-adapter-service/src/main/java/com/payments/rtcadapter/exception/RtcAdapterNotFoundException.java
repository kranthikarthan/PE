package com.payments.rtcadapter.exception;

import com.payments.domain.shared.ClearingAdapterId;

/**
 * Exception thrown when RTC adapter is not found
 */
public class RtcAdapterNotFoundException extends RuntimeException {

  public RtcAdapterNotFoundException(String message) {
    super(message);
  }

  public RtcAdapterNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public RtcAdapterNotFoundException(ClearingAdapterId adapterId) {
    super("RTC adapter not found: " + adapterId);
  }

  public RtcAdapterNotFoundException(ClearingAdapterId adapterId, Throwable cause) {
    super("RTC adapter not found: " + adapterId, cause);
  }
}
