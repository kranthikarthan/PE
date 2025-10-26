package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/** JPA Entity for pain.001 audit logs */
@Entity
@Table(name = "pain001_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain001AuditLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pain001_message_id", nullable = false)
  private Pain001MessageEntity pain001Message;

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

  @Column(name = "tenant_id", nullable = false, length = 50)
  private String tenantId;

  @Column(name = "business_unit_id", nullable = false, length = 50)
  private String businessUnitId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Action Enumeration */
  public enum Action {
    CREATED("CREATED"),
    UPDATED("UPDATED"),
    VALIDATED("VALIDATED"),
    PROCESSED("PROCESSED"),
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
