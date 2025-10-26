package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
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
 * JPA Entity for pain.002 status reports
 *
 * <p>Stores ISO 20022 pain.002 Payment Status Report messages with full correlation and
 * multi-tenancy support.
 */
@Entity
@Table(name = "pain002_status_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain002StatusReportEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "message_id", unique = true, nullable = false, length = 35)
  private String messageId;

  @Column(name = "creation_date_time", nullable = false)
  private Instant creationDateTime;

  @Column(name = "instigating_agent_bic", length = 11)
  private String instigatingAgentBic;

  @Column(name = "instructed_agent_bic", length = 11)
  private String instructedAgentBic;

  @Enumerated(EnumType.STRING)
  @Column(name = "message_format", nullable = false, length = 10)
  private MessageFormat messageFormat;

  @Column(name = "raw_message", nullable = false, columnDefinition = "TEXT")
  private String rawMessage;

  @Column(name = "parsed_message", columnDefinition = "JSONB")
  private String parsedMessage;

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
  @OneToMany(mappedBy = "statusReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Pain002TransactionStatusEntity> transactionStatuses;

  @OneToMany(mappedBy = "statusReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Pain002AuditLogEntity> auditLogs;

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
}
