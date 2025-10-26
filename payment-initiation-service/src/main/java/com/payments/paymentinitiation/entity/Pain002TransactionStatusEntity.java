package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for pain.002 transaction status
 *
 * <p>Stores individual transaction status information from pain.002 messages.
 */
@Entity
@Table(name = "pain002_transaction_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain002TransactionStatusEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "status_report_id", nullable = false)
  private Pain002StatusReportEntity statusReport;

  @Column(name = "status_id", length = 35)
  private String statusId;

  @Column(name = "original_instruction_id", length = 35)
  private String originalInstructionId;

  @Column(name = "original_end_to_end_id", length = 35)
  private String originalEndToEndId;

  @Column(name = "original_transaction_id", length = 35)
  private String originalTransactionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_status", length = 20)
  private TransactionStatus transactionStatus;

  @Column(name = "status_reason_code", length = 4)
  private String statusReasonCode;

  @Column(name = "status_reason_proprietary", length = 35)
  private String statusReasonProprietary;

  @Column(name = "status_reason_additional_info", length = 105)
  private String statusReasonAdditionalInfo;

  @Column(name = "charges_amount", precision = 19, scale = 2)
  private BigDecimal chargesAmount;

  @Column(name = "charges_currency", length = 3)
  private String chargesCurrency;

  @Column(name = "charges_agent_bic", length = 11)
  private String chargesAgentBic;

  @Column(name = "acceptance_date_time")
  private Instant acceptanceDateTime;

  @Column(name = "account_servicer_reference", length = 35)
  private String accountServicerReference;

  @Column(name = "clearing_system_reference", length = 35)
  private String clearingSystemReference;

  @Column(name = "instigating_agent_bic", length = 11)
  private String instigatingAgentBic;

  @Column(name = "instructed_agent_bic", length = 11)
  private String instructedAgentBic;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** Transaction Status Enumeration */
  public enum TransactionStatus {
    ACCP("ACCP", "Accepted"),
    RJCT("RJCT", "Rejected"),
    PDNG("PDNG", "Pending"),
    ACSP("ACSP", "AcceptedSettlementInProcess"),
    ACSC("ACSC", "AcceptedSettlementCompleted"),
    ACWC("ACWC", "AcceptedWithChange"),
    ACWP("ACWP", "AcceptedWithoutPosting"),
    ACCT("ACCT", "AcceptedCreditTransfer"),
    CANC("CANC", "Cancelled"),
    PART("PART", "PartiallyAccepted");

    private final String code;
    private final String description;

    TransactionStatus(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
  }
}
