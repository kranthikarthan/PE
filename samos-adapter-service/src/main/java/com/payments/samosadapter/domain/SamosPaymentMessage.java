package com.payments.samosadapter.domain;

import com.payments.domain.shared.TenantContext;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/**
 * SAMOS Payment Message Entity
 *
 * <p>Represents ISO 20022 payment messages sent to/received from SAMOS system. Supports pacs.008
 * (Credit Transfer), pacs.002 (Payment Status Report), and camt.054 (Notification).
 */
@Entity
@Table(name = "samos_payment_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class SamosPaymentMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Embedded
  @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id"))
  private TenantContext tenantContext;

  @Column(name = "payment_id", nullable = false)
  private String paymentId;

  @Column(name = "message_id", nullable = false, unique = true)
  private String messageId;

  @Enumerated(EnumType.STRING)
  @Column(name = "message_type", nullable = false)
  private MessageType messageType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Direction direction;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MessageStatus status = MessageStatus.PENDING;

  @Column(name = "iso20022_payload", nullable = false, columnDefinition = "TEXT")
  private String iso20022Payload;

  @Column(name = "payload_hash", nullable = false)
  private String payloadHash;

  @Column(name = "response_code")
  private String responseCode;

  @Column(name = "response_message", columnDefinition = "TEXT")
  private String responseMessage;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "received_at")
  private Instant receivedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "samos_adapter_id")
  private SamosAdapter samosAdapter;

  public static SamosPaymentMessage create(
      TenantContext tenantContext,
      String paymentId,
      String messageId,
      MessageType messageType,
      Direction direction,
      String iso20022Payload,
      String payloadHash) {

    if (paymentId == null || paymentId.isBlank()) {
      throw new IllegalArgumentException("Payment ID cannot be null or blank");
    }
    if (messageId == null || messageId.isBlank()) {
      throw new IllegalArgumentException("Message ID cannot be null or blank");
    }
    if (iso20022Payload == null || iso20022Payload.isBlank()) {
      throw new IllegalArgumentException("ISO 20022 payload cannot be null or blank");
    }

    SamosPaymentMessage message = new SamosPaymentMessage();
    message.tenantContext = tenantContext;
    message.paymentId = paymentId;
    message.messageId = messageId;
    message.messageType = messageType;
    message.direction = direction;
    message.iso20022Payload = iso20022Payload;
    message.payloadHash = payloadHash;
    message.createdAt = Instant.now();
    message.updatedAt = Instant.now();

    return message;
  }

  public void markAsSent() {
    if (this.status != MessageStatus.PENDING) {
      throw new IllegalStateException("Message must be in PENDING status to be marked as sent");
    }
    this.status = MessageStatus.SENT;
    this.sentAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public void markAsReceived(String responseCode, String responseMessage) {
    this.status = MessageStatus.RECEIVED;
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.receivedAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public void markAsFailed(String errorCode, String errorMessage) {
    this.status = MessageStatus.FAILED;
    this.responseCode = errorCode;
    this.responseMessage = errorMessage;
    this.updatedAt = Instant.now();
  }

  public boolean isOutbound() {
    return this.direction == Direction.OUTBOUND;
  }

  public boolean isInbound() {
    return this.direction == Direction.INBOUND;
  }

  public boolean isCreditTransfer() {
    return this.messageType == MessageType.PACS_008;
  }

  public boolean isStatusReport() {
    return this.messageType == MessageType.PACS_002;
  }

  public boolean isNotification() {
    return this.messageType == MessageType.CAMT_054;
  }

  public enum MessageType {
    PACS_008("pacs.008", "FIToFICstmrCdtTrf", "Credit Transfer"),
    PACS_002("pacs.002", "FIToFIPmtStsRpt", "Payment Status Report"),
    CAMT_054("camt.054", "BkToCstmrDbtCdtNtfctn", "Bank to Customer Debit/Credit Notification");

    private final String messageType;
    private final String documentType;
    private final String description;

    MessageType(String messageType, String documentType, String description) {
      this.messageType = messageType;
      this.documentType = documentType;
      this.description = description;
    }

    public String getMessageType() {
      return messageType;
    }

    public String getDocumentType() {
      return documentType;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum Direction {
    INBOUND,
    OUTBOUND
  }

  public enum MessageStatus {
    PENDING,
    SENT,
    RECEIVED,
    FAILED
  }
}
