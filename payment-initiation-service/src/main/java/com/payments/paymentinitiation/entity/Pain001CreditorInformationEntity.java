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

/** JPA Entity for pain.001 creditor information */
@Entity
@Table(name = "pain001_creditor_information")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain001CreditorInformationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_information_id", nullable = false)
  private Pain001PaymentInformationEntity paymentInformation;

  @Column(name = "creditor_name", length = 140)
  private String creditorName;

  @Column(name = "creditor_id", length = 35)
  private String creditorId;

  @Column(name = "creditor_account", length = 35)
  private String creditorAccount;

  @Column(name = "creditor_agent_bic", length = 11)
  private String creditorAgentBic;

  @Column(name = "creditor_address", length = 200)
  private String creditorAddress;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
