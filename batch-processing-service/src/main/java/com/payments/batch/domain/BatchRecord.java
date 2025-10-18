package com.payments.batch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "batch_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "payment_id", nullable = false, length = 100)
  private String paymentId;

  @Column(name = "debtor_account", nullable = false, length = 50)
  private String debtorAccount;

  @Column(name = "creditor_account", nullable = false, length = 50)
  private String creditorAccount;

  @Column(name = "amount", nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;
}
