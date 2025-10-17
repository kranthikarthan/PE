package com.payments.payshapadapter.domain;

import com.payments.domain.shared.ClearingMessageId;
import com.payments.payshapadapter.exception.InvalidPayShapPaymentMessageException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayShap Payment Message Entity
 *
 * <p>Represents a payment message for instant P2P transfers with proxy registry integration.
 * Supports mobile number and email address lookups for instant payments.
 */
@Entity
@Table(name = "payshap_payment_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayShapPaymentMessage {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "payshap_adapter_id", nullable = false)
  private String payshapAdapterId;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "message_type", nullable = false)
  private String messageType;

  @Column(name = "direction", nullable = false)
  private String direction;

  @Column(name = "message_id")
  private String messageId;

  @Column(name = "instruction_id")
  private String instructionId;

  @Column(name = "end_to_end_id")
  private String endToEndId;

  @Column(name = "transaction_type", nullable = false)
  private String transactionType;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false)
  private String currency;

  @Column(name = "debtor_name")
  private String debtorName;

  @Column(name = "debtor_account")
  private String debtorAccount;

  @Column(name = "debtor_bank_code")
  private String debtorBankCode;

  @Column(name = "debtor_mobile")
  private String debtorMobile;

  @Column(name = "debtor_email")
  private String debtorEmail;

  @Column(name = "creditor_name")
  private String creditorName;

  @Column(name = "creditor_account")
  private String creditorAccount;

  @Column(name = "creditor_bank_code")
  private String creditorBankCode;

  @Column(name = "creditor_mobile")
  private String creditorMobile;

  @Column(name = "creditor_email")
  private String creditorEmail;

  @Column(name = "payment_purpose")
  private String paymentPurpose;

  @Column(name = "reference")
  private String reference;

  @Column(name = "proxy_type")
  private String proxyType;

  @Column(name = "proxy_value")
  private String proxyValue;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "response_code")
  private String responseCode;

  @Column(name = "response_message")
  private String responseMessage;

  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Version
  @Column(name = "version")
  private Integer version;

  /** Create a new PayShap payment message */
  public static PayShapPaymentMessage create(
      String payshapAdapterId,
      String transactionId,
      String messageType,
      String direction,
      String messageId,
      String instructionId,
      String endToEndId,
      String transactionType,
      BigDecimal amount,
      String currency,
      String debtorName,
      String debtorAccount,
      String debtorBankCode,
      String debtorMobile,
      String debtorEmail,
      String creditorName,
      String creditorAccount,
      String creditorBankCode,
      String creditorMobile,
      String creditorEmail,
      String paymentPurpose,
      String reference,
      String proxyType,
      String proxyValue) {
    if (payshapAdapterId == null || payshapAdapterId.trim().isEmpty()) {
      throw new InvalidPayShapPaymentMessageException("PayShap adapter ID cannot be null or empty");
    }
    if (transactionId == null || transactionId.trim().isEmpty()) {
      throw new InvalidPayShapPaymentMessageException("Transaction ID cannot be null or empty");
    }
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidPayShapPaymentMessageException("Amount must be greater than zero");
    }
    if (amount.compareTo(new BigDecimal("3000.00")) > 0) {
      throw new InvalidPayShapPaymentMessageException(
          "Amount cannot exceed R3,000 for PayShap payments");
    }
    if (currency == null || currency.trim().isEmpty()) {
      throw new InvalidPayShapPaymentMessageException("Currency cannot be null or empty");
    }

    return PayShapPaymentMessage.builder()
        .id(ClearingMessageId.generate().toString())
        .payshapAdapterId(payshapAdapterId)
        .transactionId(transactionId)
        .messageType(messageType != null ? messageType : "pacs.008")
        .direction(direction != null ? direction : "OUTBOUND")
        .messageId(messageId)
        .instructionId(instructionId)
        .endToEndId(endToEndId)
        .transactionType(transactionType != null ? transactionType : "CREDIT")
        .amount(amount)
        .currency(currency)
        .debtorName(debtorName)
        .debtorAccount(debtorAccount)
        .debtorBankCode(debtorBankCode)
        .debtorMobile(debtorMobile)
        .debtorEmail(debtorEmail)
        .creditorName(creditorName)
        .creditorAccount(creditorAccount)
        .creditorBankCode(creditorBankCode)
        .creditorMobile(creditorMobile)
        .creditorEmail(creditorEmail)
        .paymentPurpose(paymentPurpose)
        .reference(reference)
        .proxyType(proxyType)
        .proxyValue(proxyValue)
        .status("PENDING")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .version(1)
        .build();
  }

  /** Mark message as submitted */
  public void markAsSubmitted() {
    this.status = "SUBMITTED";
    this.updatedAt = Instant.now();
    this.version++;
  }

  /** Process payment response */
  public void processResponse(String responseCode, String responseMessage, boolean success) {
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.status = success ? "COMPLETED" : "FAILED";
    this.processedAt = Instant.now();
    this.updatedAt = Instant.now();
    this.version++;
  }

  /** Check if payment is completed */
  public boolean isCompleted() {
    return "COMPLETED".equals(this.status);
  }

  /** Check if payment failed */
  public boolean isFailed() {
    return "FAILED".equals(this.status);
  }

  /** Check if payment is pending */
  public boolean isPending() {
    return "PENDING".equals(this.status);
  }
}
