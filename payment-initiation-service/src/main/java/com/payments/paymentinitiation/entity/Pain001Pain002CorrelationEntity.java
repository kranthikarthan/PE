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

/**
 * JPA Entity for pain.001/pain.002 correlation
 *
 * <p>Maintains correlation between pain.001 messages and their corresponding pain.002 responses.
 */
@Entity
@Table(name = "pain001_pain002_correlation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain001Pain002CorrelationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pain001_message_id", nullable = false)
  private Pain001MessageEntity pain001Message;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pain002_status_report_id", nullable = false)
  private Pain002StatusReportEntity pain002StatusReport;

  @Column(name = "correlation_id", nullable = false, length = 255)
  private String correlationId;

  @Column(name = "original_message_id", nullable = false, length = 35)
  private String originalMessageId;

  @Column(name = "status_report_message_id", nullable = false, length = 35)
  private String statusReportMessageId;

  @Enumerated(EnumType.STRING)
  @Column(name = "correlation_status", nullable = false, length = 20)
  private CorrelationStatus correlationStatus;

  @Column(name = "correlation_notes", length = 500)
  private String correlationNotes;

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

  /** Correlation Status Enumeration */
  public enum CorrelationStatus {
    PENDING("PENDING"),
    CORRELATED("CORRELATED"),
    FAILED("FAILED"),
    TIMEOUT("TIMEOUT");

    private final String value;

    CorrelationStatus(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
