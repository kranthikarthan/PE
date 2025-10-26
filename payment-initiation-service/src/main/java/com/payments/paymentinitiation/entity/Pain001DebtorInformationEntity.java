package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** JPA Entity for pain.001 debtor information */
@Entity
@Table(name = "pain001_debtor_information")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain001DebtorInformationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_information_id", nullable = false)
  private Pain001PaymentInformationEntity paymentInformation;

  @Column(name = "debtor_name", length = 140)
  private String debtorName;

  @Column(name = "debtor_id", length = 35)
  private String debtorId;

  @Column(name = "debtor_account", length = 35)
  private String debtorAccount;

  @Column(name = "debtor_agent_bic", length = 11)
  private String debtorAgentBic;

  @Column(name = "debtor_address", length = 200)
  private String debtorAddress;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
