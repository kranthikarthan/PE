package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for pain.001 payment information
 *
 * <p>Stores payment instruction details from pain.001 messages.
 */
@Entity
@Table(name = "pain001_payment_information")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain001PaymentInformationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pain001_message_id", nullable = false)
  private Pain001MessageEntity pain001Message;

  @Column(name = "payment_information_id", nullable = false, length = 35)
  private String paymentInformationId;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 10)
  private PaymentMethod paymentMethod;

  @Column(name = "batch_booking", nullable = false)
  private Boolean batchBooking;

  @Column(name = "number_of_transactions", nullable = false)
  private Integer numberOfTransactions;

  @Column(name = "control_sum", nullable = false, precision = 19, scale = 2)
  private BigDecimal controlSum;

  @Column(name = "required_execution_date", nullable = false)
  private LocalDate requiredExecutionDate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Relationships
  @OneToMany(mappedBy = "paymentInformation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Pain001DebtorInformationEntity> debtorInformations;

  @OneToMany(mappedBy = "paymentInformation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Pain001CreditorInformationEntity> creditorInformations;

  @OneToMany(mappedBy = "paymentInformation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Pain001CreditTransferTransactionEntity> creditTransferTransactions;

  /** Payment Method Enumeration */
  public enum PaymentMethod {
    TRF("TRF"),
    TEL("TEL"),
    CHK("CHK"),
    DDT("DDT"),
    TRA("TRA");

    private final String value;

    PaymentMethod(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
