package com.payments.iso20022.config;

import lombok.Getter;

/**
 * ISO 20022 Message Types
 *
 * <p>Supported message types for South African clearing systems.
 */
@Getter
public enum Iso20022MessageType {

  /** pacs.008.001.13 - FI to FI Customer Credit Transfer (Latest) */
  PACS_008(
      "pacs.008.001.13",
      "com.payments.iso20022.pacs008",
      "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.13",
      "pacs.008.001.13.xsd",
      "FI to FI Customer Credit Transfer"),

  /** pacs.002.001.15 - FI to FI Payment Status Report (Latest) */
  PACS_002(
      "pacs.002.001.15",
      "com.payments.iso20022.pacs002",
      "urn:iso:std:iso:20022:tech:xsd:pacs.002.001.15",
      "pacs.002.001.15.xsd",
      "FI to FI Payment Status Report"),

  /** pacs.004.001.09 - Payment Return */
  PACS_004(
      "pacs.004.001.09",
      "com.payments.iso20022.pacs004",
      "urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09",
      "pacs.004.001.09.xsd",
      "Payment Return"),

  /** pain.001.001.12 - Customer Credit Transfer Initiation (Latest) */
  PAIN_001(
      "pain.001.001.12",
      "com.payments.iso20022.pain001",
      "urn:iso:std:iso:20022:tech:xsd:pain.001.001.12",
      "pain.001.001.12.xsd",
      "Customer Credit Transfer Initiation"),

  /** pain.002.001.14 - Payment Status Report (Latest) */
  PAIN_002(
      "pain.002.001.14",
      "com.payments.iso20022.pain002",
      "urn:iso:std:iso:20022:tech:xsd:pain.002.001.14",
      "pain.002.001.14.xsd",
      "Payment Status Report"),

  /** pacs.028.001.06 - FI to FI Payment Status Request */
  PACS_028(
      "pacs.028.001.06",
      "com.payments.iso20022.pacs028",
      "urn:iso:std:iso:20022:tech:xsd:pacs.028.001.06",
      "pacs.028.001.06.xsd",
      "FI to FI Payment Status Request"),

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
   * Check if message type is pain (payment initiation)
   *
   * @return true if pain message
   */
  public boolean isPainMessage() {
    return messageId.startsWith("pain.");
  }

  /**
   * Check if message type is pain.001 (payment initiation)
   *
   * @return true if pain.001 message
   */
  public boolean isPain001Message() {
    return messageId.equals("pain.001.001.12");
  }

  /**
   * Check if message type is pain.002 (payment status report)
   *
   * @return true if pain.002 message
   */
  public boolean isPain002Message() {
    return messageId.equals("pain.002.001.14");
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
