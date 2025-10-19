package com.payments.iso20022.config;

import lombok.Getter;

/**
 * ISO 20022 Message Types
 *
 * <p>Supported message types for South African clearing systems.
 */
@Getter
public enum Iso20022MessageType {

  /** pacs.008.001.08 - FI to FI Customer Credit Transfer */
  PACS_008(
      "pacs.008.001.08",
      "com.payments.iso20022.pacs008",
      "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08",
      "pacs.008.001.08.xsd",
      "FI to FI Customer Credit Transfer"),

  /** pacs.002.001.10 - FI to FI Payment Status Report */
  PACS_002(
      "pacs.002.001.10",
      "com.payments.iso20022.pacs002",
      "urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10",
      "pacs.002.001.10.xsd",
      "FI to FI Payment Status Report"),

  /** pacs.004.001.09 - Payment Return */
  PACS_004(
      "pacs.004.001.09",
      "com.payments.iso20022.pacs004",
      "urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09",
      "pacs.004.001.09.xsd",
      "Payment Return"),

  /** camt.054.001.08 - Bank to Customer Debit Credit Notification */
  CAMT_054(
      "camt.054.001.08",
      "com.payments.iso20022.camt054",
      "urn:iso:std:iso:20022:tech:xsd:camt.054.001.08",
      "camt.054.001.08.xsd",
      "Bank to Customer Debit Credit Notification");

  private final String messageId;
  private final String packageName;
  private final String namespace;
  private final String schemaFile;
  private final String description;

  Iso20022MessageType(
      String messageId,
      String packageName,
      String namespace,
      String schemaFile,
      String description) {
    this.messageId = messageId;
    this.packageName = packageName;
    this.namespace = namespace;
    this.schemaFile = schemaFile;
    this.description = description;
  }

  /**
   * Get message type from message ID
   *
   * @param messageId Message identifier (e.g., "pacs.008.001.08")
   * @return Message type
   * @throws IllegalArgumentException if message type not supported
   */
  public static Iso20022MessageType fromMessageId(String messageId) {
    for (Iso20022MessageType type : values()) {
      if (type.messageId.equals(messageId)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unsupported ISO 20022 message type: " + messageId);
  }

  /**
   * Check if message type is pacs (payment clearing and settlement)
   *
   * @return true if pacs message
   */
  public boolean isPacsMessage() {
    return messageId.startsWith("pacs.");
  }

  /**
   * Check if message type is camt (cash management)
   *
   * @return true if camt message
   */
  public boolean isCamtMessage() {
    return messageId.startsWith("camt.");
  }

  @Override
  public String toString() {
    return messageId + " - " + description;
  }
}
