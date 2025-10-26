package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for pain.001 credit transfer transactions
 *
 * <p>Stores individual credit transfer transaction details from pain.001 messages.
 */
@Entity
@Table(name = "pain001_credit_transfer_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain001CreditTransferTransactionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_information_id", nullable = false)
  private Pain001PaymentInformationEntity paymentInformation;

  @Column(name = "instruction_id", length = 35)
  private String instructionId;

  @Column(name = "end_to_end_id", nullable = false, length = 35)
  private String endToEndId;

  @Column(name = "transaction_id", length = 35)
  private String transactionId;

  @Column(name = "instructed_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal instructedAmount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "creditor_name", length = 140)
  private String creditorName;

  @Column(name = "creditor_account", length = 35)
  private String creditorAccount;

  @Column(name = "creditor_agent_bic", length = 11)
  private String creditorAgentBic;

  @Column(name = "remittance_information", length = 140)
  private String remittanceInformation;

  @Column(name = "purpose_code", length = 4)
  private String purposeCode;

  @Column(name = "regulatory_reporting", length = 105)
  private String regulatoryReporting;

  @Column(name = "tax_information", length = 105)
  private String taxInformation;

  @Column(name = "additional_information", length = 500)
  private String additionalInformation;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
