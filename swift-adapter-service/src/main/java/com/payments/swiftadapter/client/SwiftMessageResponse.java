package com.payments.swiftadapter.client;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SWIFT Message Response
 *
 * <p>Response from SWIFT Alliance Gateway after message submission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwiftMessageResponse {

  /** Whether message submission was successful */
  private boolean success;

  /** SWIFT-assigned message ID */
  private String swiftMessageId;

  /** Original message reference */
  private String messageReference;

  /** Message type (MT103, pacs.008, etc.) */
  private String messageType;

  /** Sender BIC */
  private String senderBic;

  /** Receiver BIC */
  private String receiverBic;

  /** Submission timestamp */
  private Instant submissionTime;

  /** Estimated delivery time */
  private Instant estimatedDeliveryTime;

  /** Message status (SUBMITTED, ACKNOWLEDGED, DELIVERED) */
  private String status;

  /** Error code (if success = false) */
  private String errorCode;

  /** Error message (if success = false) */
  private String errorMessage;
}
