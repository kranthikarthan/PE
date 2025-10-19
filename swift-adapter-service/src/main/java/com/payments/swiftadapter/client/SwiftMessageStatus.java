package com.payments.swiftadapter.client;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SWIFT Message Status
 *
 * <p>Represents the current status of a SWIFT message in the network.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwiftMessageStatus {

  /** SWIFT message ID */
  private String swiftMessageId;

  /** Message reference */
  private String messageReference;

  /** Current status (SUBMITTED, ACKNOWLEDGED, DELIVERED, FAILED) */
  private String status;

  /** Status timestamp */
  private Instant statusTimestamp;

  /** Delivery timestamp (if delivered) */
  private Instant deliveryTimestamp;

  /** Status description */
  private String statusDescription;

  /** Error code (if failed) */
  private String errorCode;

  /** Error message (if failed) */
  private String errorMessage;

  /**
   * Check if message was delivered successfully
   *
   * @return true if delivered
   */
  public boolean isDelivered() {
    return "DELIVERED".equalsIgnoreCase(status);
  }

  /**
   * Check if message failed
   *
   * @return true if failed
   */
  public boolean isFailed() {
    return "FAILED".equalsIgnoreCase(status);
  }
}
