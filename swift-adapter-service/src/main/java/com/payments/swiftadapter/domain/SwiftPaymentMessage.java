package com.payments.swiftadapter.domain;

import com.payments.domain.shared.ClearingMessageId;
import com.payments.swiftadapter.exception.InvalidSwiftPaymentMessageException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SWIFT Payment Message Entity
 *
 * <p>Represents a payment message for international transfers via SWIFT network. Supports
 * sanctions screening and FX conversion for cross-border payments.
 */
@Entity
@Table(name = "swift_payment_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwiftPaymentMessage {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "swift_adapter_id", nullable = false)
  private String swiftAdapterId;

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

  @Column(name = "debtor_bank_name")
  private String debtorBankName;

  @Column(name = "debtor_bank_country")
  private String debtorBankCountry;

  @Column(name = "debtor_bank_swift_code")
  private String debtorBankSwiftCode;

  @Column(name = "creditor_name")
  private String creditorName;

  @Column(name = "creditor_account")
  private String creditorAccount;

  @Column(name = "creditor_bank_code")
  private String creditorBankCode;

  @Column(name = "creditor_bank_name")
  private String creditorBankName;

  @Column(name = "creditor_bank_country")
  private String creditorBankCountry;

  @Column(name = "creditor_bank_swift_code")
  private String creditorBankSwiftCode;

  @Column(name = "payment_purpose")
  private String paymentPurpose;

  @Column(name = "reference")
  private String reference;

  @Column(name = "correspondent_bank_code")
  private String correspondentBankCode;

  @Column(name = "correspondent_bank_name")
  private String correspondentBankName;

  @Column(name = "correspondent_bank_swift_code")
  private String correspondentBankSwiftCode;

  @Column(name = "intermediary_bank_code")
  private String intermediaryBankCode;

  @Column(name = "intermediary_bank_name")
  private String intermediaryBankName;

  @Column(name = "intermediary_bank_swift_code")
  private String intermediaryBankSwiftCode;

  @Column(name = "charges_bearer")
  private String chargesBearer;

  @Column(name = "exchange_rate")
  private BigDecimal exchangeRate;

  @Column(name = "original_amount")
  private BigDecimal originalAmount;

  @Column(name = "original_currency")
  private String originalCurrency;

  @Column(name = "sanctions_screening_status")
  private String sanctionsScreeningStatus;

  @Column(name = "sanctions_screening_result")
  private String sanctionsScreeningResult;

  @Column(name = "sanctions_screening_details")
  private String sanctionsScreeningDetails;

  @Column(name = "fx_conversion_status")
  private String fxConversionStatus;

  @Column(name = "fx_conversion_result")
  private String fxConversionResult;

  @Column(name = "fx_conversion_rate")
  private BigDecimal fxConversionRate;

  @Column(name = "fx_conversion_amount")
  private BigDecimal fxConversionAmount;

  @Column(name = "fx_conversion_currency")
  private String fxConversionCurrency;

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

  /** Create a new SWIFT payment message */
  public static SwiftPaymentMessage create(
      String swiftAdapterId,
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
      String debtorBankName,
      String debtorBankCountry,
      String debtorBankSwiftCode,
      String creditorName,
      String creditorAccount,
      String creditorBankCode,
      String creditorBankName,
      String creditorBankCountry,
      String creditorBankSwiftCode,
      String paymentPurpose,
      String reference,
      String correspondentBankCode,
      String correspondentBankName,
      String correspondentBankSwiftCode,
      String intermediaryBankCode,
      String intermediaryBankName,
      String intermediaryBankSwiftCode,
      String chargesBearer) {
    if (swiftAdapterId == null || swiftAdapterId.trim().isEmpty()) {
      throw new InvalidSwiftPaymentMessageException("SWIFT adapter ID cannot be null or empty");
    }
    if (transactionId == null || transactionId.trim().isEmpty()) {
      throw new InvalidSwiftPaymentMessageException("Transaction ID cannot be null or empty");
    }
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidSwiftPaymentMessageException("Amount must be greater than zero");
    }
    if (currency == null || currency.trim().isEmpty()) {
      throw new InvalidSwiftPaymentMessageException("Currency cannot be null or empty");
    }

    return SwiftPaymentMessage.builder()
        .id(ClearingMessageId.generate().toString())
        .swiftAdapterId(swiftAdapterId)
        .transactionId(transactionId)
        .messageType(messageType != null ? messageType : "MT103")
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
        .debtorBankName(debtorBankName)
        .debtorBankCountry(debtorBankCountry)
        .debtorBankSwiftCode(debtorBankSwiftCode)
        .creditorName(creditorName)
        .creditorAccount(creditorAccount)
        .creditorBankCode(creditorBankCode)
        .creditorBankName(creditorBankName)
        .creditorBankCountry(creditorBankCountry)
        .creditorBankSwiftCode(creditorBankSwiftCode)
        .paymentPurpose(paymentPurpose)
        .reference(reference)
        .correspondentBankCode(correspondentBankCode)
        .correspondentBankName(correspondentBankName)
        .correspondentBankSwiftCode(correspondentBankSwiftCode)
        .intermediaryBankCode(intermediaryBankCode)
        .intermediaryBankName(intermediaryBankName)
        .intermediaryBankSwiftCode(intermediaryBankSwiftCode)
        .chargesBearer(chargesBearer != null ? chargesBearer : "OUR")
        .sanctionsScreeningStatus("PENDING")
        .fxConversionStatus("PENDING")
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

  /** Update sanctions screening result */
  public void updateSanctionsScreeningResult(
      String result, String details, Integer riskScore, String riskLevel) {
    this.sanctionsScreeningResult = result;
    this.sanctionsScreeningDetails = details;
    this.sanctionsScreeningStatus = "COMPLETED";
    this.updatedAt = Instant.now();
    this.version++;
  }

  /** Update FX conversion result */
  public void updateFxConversionResult(
      String result,
      BigDecimal rate,
      BigDecimal convertedAmount,
      String convertedCurrency,
      String details) {
    this.fxConversionResult = result;
    this.fxConversionRate = rate;
    this.fxConversionAmount = convertedAmount;
    this.fxConversionCurrency = convertedCurrency;
    this.fxConversionStatus = "COMPLETED";
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

  /** Check if sanctions screening is completed */
  public boolean isSanctionsScreeningCompleted() {
    return "COMPLETED".equals(this.sanctionsScreeningStatus);
  }

  /** Check if FX conversion is completed */
  public boolean isFxConversionCompleted() {
    return "COMPLETED".equals(this.fxConversionStatus);
  }
}
