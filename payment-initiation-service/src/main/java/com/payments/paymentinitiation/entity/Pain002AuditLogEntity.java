package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/** JPA Entity for pain.002 audit logs */
@Entity
@Table(name = "pain002_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain002AuditLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pain002_status_report_id", nullable = false)
  private Pain002StatusReportEntity statusReport;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false, length = 20)
  private Action action;

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "user_id", length = 50)
  private String userId;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", length = 200)
  private String userAgent;

  @Column(name = "additional_data", columnDefinition = "JSONB")
  private String additionalData;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Action Enumeration */
  public enum Action {
    CREATED("CREATED"),
    UPDATED("UPDATED"),
    SENT("SENT"),
    DELIVERED("DELIVERED"),
    FAILED("FAILED"),
    DELETED("DELETED");

    private final String value;

    Action(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
