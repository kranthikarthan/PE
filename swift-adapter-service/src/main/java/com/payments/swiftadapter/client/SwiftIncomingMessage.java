package com.payments.swiftadapter.client;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SWIFT Incoming Message
 *
 * <p>Represents an incoming SWIFT message from the Alliance Gateway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwiftIncomingMessage {

  /** SWIFT message ID */
  private String swiftMessageId;

  /** Message type (MT103, pacs.008, etc.) */
  private String messageType;

  /** Message format (MT, MX) */
  private String messageFormat;

  /** Sender BIC */
  private String senderBic;

  /** Receiver BIC */
  private String receiverBic;

  /** Message content (MT or MX format) */
  private String messageContent;

  /** Message reference */
  private String messageReference;

  /** Reception timestamp */
  private Instant receptionTime;

  /** Sending timestamp */
  private Instant sendingTime;

  /** Message priority (NORMAL, URGENT) */
  private String priority;

  /** Whether message requires acknowledgment */
  private boolean requiresAcknowledgment;
}
