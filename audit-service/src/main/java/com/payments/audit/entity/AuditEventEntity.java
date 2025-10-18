package com.payments.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit Event Entity - Immutable compliance log.
 *
 * <p>Tracks all system operations for compliance (POPIA, FICA, PCI-DSS):
 * - Who performed the action
 * - What action was performed
 * - What resource was affected
 * - When it happened
 * - Result (SUCCESS, DENIED, ERROR)
 * - IP address and user agent for forensics
 *
 * <p>Never modified after creation. Retention: 7 years minimum.
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
      @Index(name = "idx_tenant_timestamp", columnList = "tenant_id,timestamp DESC"),
      @Index(name = "idx_user_timestamp", columnList = "user_id,timestamp DESC"),
      @Index(name = "idx_action_timestamp", columnList = "action,timestamp DESC"),
      @Index(name = "idx_result", columnList = "result"),
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"details"})
public class AuditEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "user_id", nullable = false, length = 255)
  private String userId;

  @Column(name = "action", nullable = false, length = 100)
  private String action;

  @Column(name = "resource", length = 100)
  private String resource;

  @Column(name = "resource_id")
  private UUID resourceId;

  @Column(name = "result", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private AuditResult result;

  @Column(name = "details", columnDefinition = "TEXT")
  private String details;

  @Column(name = "timestamp", nullable = false, updatable = false)
  private LocalDateTime timestamp;

  @Column(name = "ip_address", length = 50)
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  @PrePersist
  protected void onCreate() {
    timestamp = LocalDateTime.now();
  }

  /**
   * Result enum for audit outcomes.
   */
  public enum AuditResult {
    SUCCESS("Operation completed successfully"),
    DENIED("Operation denied (insufficient permissions)"),
    ERROR("Operation failed with error");

    private final String description;

    AuditResult(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}
