package com.payments.swiftadapter.domain;

import com.payments.domain.shared.ClearingMessageId;
import com.payments.swiftadapter.exception.InvalidSwiftSettlementRecordException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SWIFT Settlement Record Entity
 *
 * <p>Records settlement information for SWIFT payments including correspondent bank settlements, FX
 * conversions, and final settlement amounts.
 */
@Entity
@Table(name = "swift_settlement_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwiftSettlementRecord {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "swift_adapter_id", nullable = false)
  private String swiftAdapterId;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "message_id")
  private String messageId;

  @Column(name = "instruction_id")
  private String instructionId;

  @Column(name = "end_to_end_id")
  private String endToEndId;

  @Column(name = "settlement_type", nullable = false)
  private String settlementType;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false)
  private String currency;

  @Column(name = "original_amount")
  private BigDecimal originalAmount;

  @Column(name = "original_currency")
  private String originalCurrency;

  @Column(name = "exchange_rate")
  private BigDecimal exchangeRate;

  @Column(name = "debtor_bank_swift_code")
  private String debtorBankSwiftCode;

  @Column(name = "creditor_bank_swift_code")
  private String creditorBankSwiftCode;

  @Column(name = "correspondent_bank_swift_code")
  private String correspondentBankSwiftCode;

  @Column(name = "intermediary_bank_swift_code")
  private String intermediaryBankSwiftCode;

  @Column(name = "settlement_bank_swift_code")
  private String settlementBankSwiftCode;

  @Column(name = "settlement_bank_name")
  private String settlementBankName;

  @Column(name = "settlement_bank_country")
  private String settlementBankCountry;

  @Column(name = "settlement_account")
  private String settlementAccount;

  @Column(name = "nostro_account")
  private String nostroAccount;

  @Column(name = "vostro_account")
  private String vostroAccount;

  @Column(name = "charges_amount")
  private BigDecimal chargesAmount;

  @Column(name = "charges_currency")
  private String chargesCurrency;

  @Column(name = "charges_bearer")
  private String chargesBearer;

  @Column(name = "commission_amount")
  private BigDecimal commissionAmount;

  @Column(name = "commission_currency")
  private String commissionCurrency;

  @Column(name = "net_amount")
  private BigDecimal netAmount;

  @Column(name = "net_currency")
  private String netCurrency;

  @Column(name = "settlement_date")
  private Instant settlementDate;

  @Column(name = "value_date")
  private Instant valueDate;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "response_code")
  private String responseCode;

  @Column(name = "response_message")
  private String responseMessage;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Version
  @Column(name = "version")
  private Integer version;

  /** Create a new SWIFT settlement record */
  public static SwiftSettlementRecord create(
      String swiftAdapterId,
      String transactionId,
      String messageId,
      String instructionId,
      String endToEndId,
      String settlementType,
      BigDecimal amount,
      String currency,
      BigDecimal originalAmount,
      String originalCurrency,
      BigDecimal exchangeRate,
      String debtorBankSwiftCode,
      String creditorBankSwiftCode,
      String correspondentBankSwiftCode,
      String intermediaryBankSwiftCode,
      String settlementBankSwiftCode,
      String settlementBankName,
      String settlementBankCountry,
      String settlementAccount,
      String nostroAccount,
      String vostroAccount,
      BigDecimal chargesAmount,
      String chargesCurrency,
      String chargesBearer,
      BigDecimal commissionAmount,
      String commissionCurrency) {
    if (swiftAdapterId == null || swiftAdapterId.trim().isEmpty()) {
      throw new InvalidSwiftSettlementRecordException("SWIFT adapter ID cannot be null or empty");
    }
    if (transactionId == null || transactionId.trim().isEmpty()) {
      throw new InvalidSwiftSettlementRecordException("Transaction ID cannot be null or empty");
    }
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidSwiftSettlementRecordException("Amount must be greater than zero");
    }
    if (currency == null || currency.trim().isEmpty()) {
      throw new InvalidSwiftSettlementRecordException("Currency cannot be null or empty");
    }

    BigDecimal netAmount = amount;
    if (chargesAmount != null) {
      netAmount = netAmount.subtract(chargesAmount);
    }
    if (commissionAmount != null) {
      netAmount = netAmount.subtract(commissionAmount);
    }

    return SwiftSettlementRecord.builder()
        .id(ClearingMessageId.generate().toString())
        .swiftAdapterId(swiftAdapterId)
        .transactionId(transactionId)
        .messageId(messageId)
        .instructionId(instructionId)
        .endToEndId(endToEndId)
        .settlementType(settlementType != null ? settlementType : "FINAL")
        .amount(amount)
        .currency(currency)
        .originalAmount(originalAmount)
        .originalCurrency(originalCurrency)
        .exchangeRate(exchangeRate)
        .debtorBankSwiftCode(debtorBankSwiftCode)
        .creditorBankSwiftCode(creditorBankSwiftCode)
        .correspondentBankSwiftCode(correspondentBankSwiftCode)
        .intermediaryBankSwiftCode(intermediaryBankSwiftCode)
        .settlementBankSwiftCode(settlementBankSwiftCode)
        .settlementBankName(settlementBankName)
        .settlementBankCountry(settlementBankCountry)
        .settlementAccount(settlementAccount)
        .nostroAccount(nostroAccount)
        .vostroAccount(vostroAccount)
        .chargesAmount(chargesAmount)
        .chargesCurrency(chargesCurrency)
        .chargesBearer(chargesBearer != null ? chargesBearer : "OUR")
        .commissionAmount(commissionAmount)
        .commissionCurrency(commissionCurrency)
        .netAmount(netAmount)
        .netCurrency(currency)
        .status("PENDING")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .version(1)
        .build();
  }

  /** Update settlement status */
  public void updateStatus(String status, String responseCode, String responseMessage) {
    this.status = status;
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.updatedAt = Instant.now();
    this.version++;
  }

  /** Mark settlement as completed */
  public void markAsCompleted(Instant settlementDate, Instant valueDate) {
    this.status = "COMPLETED";
    this.settlementDate = settlementDate;
    this.valueDate = valueDate;
    this.updatedAt = Instant.now();
    this.version++;
  }

  /** Mark settlement as failed */
  public void markAsFailed(String responseCode, String responseMessage) {
    this.status = "FAILED";
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.updatedAt = Instant.now();
    this.version++;
  }

  /** Update charges and recalculate net amount */
  public void updateCharges(
      BigDecimal chargesAmount,
      String chargesCurrency,
      BigDecimal commissionAmount,
      String commissionCurrency) {
    this.chargesAmount = chargesAmount;
    this.chargesCurrency = chargesCurrency;
    this.commissionAmount = commissionAmount;
    this.commissionCurrency = commissionCurrency;

    // Recalculate net amount
    BigDecimal netAmount = this.amount;
    if (chargesAmount != null) {
      netAmount = netAmount.subtract(chargesAmount);
    }
    if (commissionAmount != null) {
      netAmount = netAmount.subtract(commissionAmount);
    }
    this.netAmount = netAmount;

    this.updatedAt = Instant.now();
    this.version++;
  }

  /** Check if settlement is completed */
  public boolean isCompleted() {
    return "COMPLETED".equals(this.status);
  }

  /** Check if settlement failed */
  public boolean isFailed() {
    return "FAILED".equals(this.status);
  }

  /** Check if settlement is pending */
  public boolean isPending() {
    return "PENDING".equals(this.status);
  }
}
