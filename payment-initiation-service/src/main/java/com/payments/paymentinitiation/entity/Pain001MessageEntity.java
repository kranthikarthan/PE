package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
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
 * JPA Entity for pain.001 messages
 *
 * <p>Stores ISO 20022 pain.001 Customer Credit Transfer Initiation messages with full validation,
 * correlation, and multi-tenancy support.
 */
@Entity
@Table(name = "pain001_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain001MessageEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "message_id", unique = true, nullable = false, length = 35)
  private String messageId;

  @Column(name = "creation_date_time", nullable = false)
  private Instant creationDateTime;

  @Column(name = "number_of_transactions", nullable = false)
  private Integer numberOfTransactions;

  @Column(name = "control_sum", nullable = false, precision = 19, scale = 2)
  private BigDecimal controlSum;

  @Column(name = "initiating_party_name", length = 140)
  private String initiatingPartyName;

  @Column(name = "initiating_party_id", length = 35)
  private String initiatingPartyId;

  @Enumerated(EnumType.STRING)
  @Column(name = "message_format", nullable = false, length = 10)
  private MessageFormat messageFormat;

  @Column(name = "raw_message", nullable = false, columnDefinition = "TEXT")
  private String rawMessage;

  @Column(name = "parsed_message", columnDefinition = "JSONB")
  private String parsedMessage;

  @Enumerated(EnumType.STRING)
  @Column(name = "validation_status", nullable = false, length = 20)
  private ValidationStatus validationStatus;

  @Column(name = "validation_errors", columnDefinition = "JSONB")
  private String validationErrors;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Multi-tenancy
  @Column(name = "tenant_id", nullable = false, length = 20)
  private String tenantId;

  @Column(name = "business_unit_id", nullable = false, length = 30)
  private String businessUnitId;

  // Relationships
  @OneToMany(mappedBy = "pain001Message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Pain001PaymentInformationEntity> paymentInformations;

  @OneToMany(mappedBy = "pain001Message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Pain001ValidationResultEntity> validationResults;

  @OneToMany(mappedBy = "pain001Message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Pain001AuditLogEntity> auditLogs;

  /** Message Format Enumeration */
  public enum MessageFormat {
    XML("XML"),
    JSON("JSON");

    private final String value;

    MessageFormat(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  /** Validation Status Enumeration */
  public enum ValidationStatus {
    PENDING("PENDING"),
    VALID("VALID"),
    INVALID("INVALID");

    private final String value;

    ValidationStatus(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
